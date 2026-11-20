const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const script = fs.readFileSync(path.join(__dirname, "../../main/onlyoffice/payments.js"), "utf8");
const headers = "Date transaction;payEvt;payEvtMontant;mail;field1;field2;montant;ID transaction";
const csv = (...ids) => [headers, ...ids.map(id => '2026-11-20;Inscription;Tarif;a@example.org;"A; B";;1200;' + id)]
    .join("\r\n");

function worksheet() {
    const cells = new Map();
    return {
        SetColumnWidth() {},
        GetUsedRange() {
            return { GetRowsCount: () => Math.max(1, ...[...cells.keys()].map(key => Number(key.slice(1)))) };
        },
        GetRange(address) {
            const [first, last = first] = address.split(":");
            const column = first.charCodeAt(0);
            const firstRow = Number(first.slice(1));
            const lastRow = Number(last.slice(1));
            const keys = Array.from({ length: lastRow - firstRow + 1 }, (_, row) =>
                Array.from({ length: last.charCodeAt(0) - column + 1 }, (_, col) =>
                    String.fromCharCode(column + col) + (firstRow + row)));
            return {
                address,
                SetNumberFormat() {},
                GetValidation: () => ({ Add() {}, SetInCellDropdown() {} }),
                GetValue() {
                    const values = keys.map(row => row.map(key => cells.get(key) ?? ""));
                    return first === last ? values[0][0] : values;
                },
                SetValue(data) {
                    keys.forEach((row, r) => row.forEach((key, c) => {
                        const value = Array.isArray(data) ? data[r][c] : data;
                        cells.set(key, value);
                    }));
                    return true;
                },
                Clear() {
                    keys.flat().forEach(key => cells.delete(key));
                }
            };
        }
    };
}

test("refresh keeps checks by ID through reordering, removal and return", () => {
    const sheets = new Map();
    const Api = {
        GetSheet: name => sheets.get(name),
        GetAllPivotTables: () => [],
        AddSheet(name) {
            const sheet = worksheet();
            sheets.set(name, sheet);
            return sheet;
        }
    };
    const context = vm.createContext({ Api, Asc: { scope: {} } });
    const { readPayments, paymentHeaders } = vm.runInContext(script + "\n({readPayments, paymentHeaders})", context);
    const sync = (source, ...ids) => {
        context.Asc.scope.paymentExport = { source, headers: paymentHeaders, payments: readPayments(csv(...ids)) };
        return vm.runInContext("updateWorkbook()", context);
    };
    const source = "https://pay.example.org/api/payment-exports/1/payments.csv";
    assert.equal(sync(source, "0001", "1234567890123456789").added, 2);
    const tracking = sheets.get("Suivi esup-pay");
    const payments = sheets.get("Paiements esup-pay");
    tracking.GetRange("B2").SetValue("☑");
    tracking.GetRange("K1").Clear();
    tracking.GetRange("A2").SetValue("'0001");

    const refreshes = [];
    let address = "$A$1:$H$3";
    let observedAmount;
    Api.GetAllPivotTables = () => [{
        GetSource: () => ({
            GetWorksheet: () => ({ GetName: () => "Paiements esup-pay" }),
            GetAddress: () => address
        }),
        SetSource: range => {
            address = range.address;
            refreshes.push(address);
            observedAmount = payments.GetRange("G4").GetValue();
        },
        RefreshTable: () => refreshes.push(address)
    }, {
        GetSource: () => ({ GetWorksheet: () => ({ GetName: () => "Autre source" }) }),
        RefreshTable: () => assert.fail("Unrelated pivot must not be refreshed")
    }];
    const updated = sync(source, "1234567890123456789", "0003", "0001");
    assert.equal(updated.added, 1);
    assert.equal(updated.pivots, 1);
    assert.equal(updated.pivotErrors, 0);
    assert.equal(observedAmount, 1200);
    assert.deepEqual(refreshes, ["A1:H4"]);
    assert.equal(sync(source, "1234567890123456789", "0003", "0001").pivots, 1);
    assert.deepEqual(refreshes, ["A1:H4", "A1:H4"]);
    assert.equal(payments.GetRange("H2").GetValue(), "1234567890123456789");
    assert.equal(tracking.GetRange("A2").GetValue(), "0001");
    assert.equal(tracking.GetRange("B2").GetValue(), "☑");
    assert.equal(tracking.GetRange("B4").GetValue(), "☐");

    assert.equal(sync(source).imported, 0);
    assert.equal(refreshes.at(-1), "A1:H2");
    assert.equal(observedAmount, "");
    assert.equal(payments.GetRange("H4").GetValue(), "");
    assert.equal(sync(source, "0001").added, 0);
    assert.equal(tracking.GetRange("B2").GetValue(), "☑");
    assert.ok(sync(source.replace("/1/", "/2/"), "0001").error);
});

test("CSV parser validates the format before the workbook is touched", () => {
    const readPayments = vm.runInNewContext(script + "\nreadPayments");
    const [payment] = readPayments("\uFEFF" + csv("0001").replace('"A; B"', '"Équipe; ""A""\nParis"'));
    assert.equal(payment[4], 'Équipe; "A"\nParis');
    assert.equal(payment[6], 1200);
    assert.equal(payment[7], "0001");
    assert.throws(() => readPayments(csv("1", "1")), /double/);
    assert.throws(() => readPayments(csv("1").replace('"A; B"', '=1+2')), /non protégée/);
    assert.throws(() => readPayments(csv("1").replace('"A; B"', '"incomplet')), /milieu/);
});

test("opening the plugin refreshes only the event linked to the workbook with its saved token", async () => {
    const plugin = fs.readFileSync(path.join(__dirname, "../../main/onlyoffice/plugin.js"), "utf8");
    const source = "https://pay.example.org/api/payment-exports/1/payments.csv";
    const token = "a".repeat(43);
    const id = "12345678-1234-1234-1234-123456789abc";
    const storage = new Map([["esup-pay:onlyoffice:bearer:" + id + ":" + source, token]]);
    const elements = new Map();
    const requests = [];
    let workbookUpdate;
    const Asc = { plugin: {
        callCommand(command, close, recalculate, callback) {
            if (command.name === "readWorkbookSource") {
                callback({ source, id });
            } else {
                workbookUpdate = Asc.scope.paymentExport;
                callback({ imported: 1, added: 0, pivots: 1, pivotErrors: 0 });
            }
        }
    } };
    vm.runInNewContext(script + "\n" + plugin, {
        Asc, URL, navigator: {},
        localStorage: { getItem: key => storage.get(key) },
        document: {
            getElementById(id) {
                if (!elements.has(id)) elements.set(id, { value: "", addEventListener() {} });
                return elements.get(id);
            }
        },
        async fetch(url, options) {
            requests.push([url, options.headers.Authorization]);
            return { ok: true, headers: { get: () => "text/csv" }, text: async () => csv("0001") };
        }
    });

    await Asc.plugin.init();
    await Asc.plugin.init();
    assert.deepEqual(requests, [[source, "Bearer " + token]]);
    assert.equal(workbookUpdate.source, source);
    assert.equal(workbookUpdate.payments[0][7], "0001");
    assert.ok(!JSON.stringify(workbookUpdate).includes(token));
    assert.equal(elements.get("export-token").value, "");
    assert.match(elements.get("status").textContent, /1 TCD actualisé/);
});
