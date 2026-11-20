"use strict";

const paymentHeaders = ["Date transaction", "payEvt", "payEvtMontant", "mail",
    "field1", "field2", "montant", "ID transaction"];

function readPayments(csv) {
    const rows = [];
    let row = [];
    let value = "";
    let quoted = false;
    let closedQuote = false;
    const input = csv.replace(/^\uFEFF/, "");

    for (let i = 0; i < input.length; i++) {
        const character = input[i];
        if (quoted) {
            if (character !== '"') {
                value += character;
            } else if (input[i + 1] === '"') {
                value += '"';
                i++;
            } else {
                quoted = false;
                closedQuote = true;
            }
        } else if (character === ";" || character === "\n" || character === "\r") {
            row.push(value);
            value = "";
            closedQuote = false;
            if (character !== ";") {
                rows.push(row);
                row = [];
                if (character === "\r" && input[i + 1] === "\n") i++;
            }
        } else if (character === '"' && !value && !closedQuote) {
            quoted = true;
        } else {
            if (closedQuote || character === '"') throw new Error("Le CSV contient un champ mal délimité.");
            value += character;
        }
    }
    if (quoted) throw new Error("Le CSV se termine au milieu d’un champ.");
    if (row.length || value || closedQuote) rows.push([...row, value]);

    const header = rows.shift();
    if (!header || header.length !== paymentHeaders.length
        || !paymentHeaders.every((column, index) => column === header[index])) {
        throw new Error("Les colonnes reçues ne correspondent pas à l’export esup-pay.");
    }
    if (rows.length > 1048575) throw new Error("L’export dépasse la capacité d’une feuille XLSX.");

    const ids = new Set();
    for (const payment of rows) {
        const id = payment[7];
        if (payment.length !== header.length || !id || ids.has(id)) {
            throw new Error("Le CSV contient une ligne incomplète ou un ID de transaction en double.");
        }
        ids.add(id);
        const amount = payment[6];
        if (amount && !/^\d+$/.test(amount)) throw new Error("Le CSV contient un montant invalide.");
        payment[6] = amount ? Number(amount) : "";
        if (amount && !Number.isSafeInteger(payment[6])) throw new Error("Un montant dépasse la précision du classeur.");

        for (const index of [0, 1, 2, 3, 4, 5, 7]) {
            const value = payment[index];
            if (/^[\s\x00-\x1f]*[=+@-]/u.test(value)) {
                throw new Error("Le CSV contient une valeur de texte non protégée.");
            }
        }
    }
    return rows;
}

// ONLYOFFICE sérialise les commandes et les exécute dans le contexte du classeur.
function readWorkbookSource() {
    const sheet = Api.GetSheet("Paiements esup-pay") || Api.GetSheet("Suivi esup-pay");
    const source = sheet && sheet.GetRange("J1").GetValue();
    return typeof source === "string" ? { source, id: sheet.GetRange("J2").GetValue() } : null;
}

function updateWorkbook() {
    const { source, workbookId, headers, payments } = Asc.scope.paymentExport;
    let paid = Api.GetSheet("Paiements esup-pay");
    let tracking = Api.GetSheet("Suivi esup-pay");

    for (const sheet of [paid, tracking]) {
        if (sheet && sheet.GetRange("J1").GetValue() !== source) {
            return { error: "Une feuille esup-pay existe déjà pour une autre source ou sans lien d’export associé." };
        }
    }

    const lastTrackingRow = tracking ? tracking.GetUsedRange().GetRowsCount() : 1;
    const ids = new Set();
    const corrections = [];
    const legacy = tracking && tracking.GetRange("K1").GetValue() !== "esup-pay:id-text";
    if (lastTrackingRow > 1) {
        const values = tracking.GetRange("A2:A" + lastTrackingRow).GetValue();
        const rows = Array.isArray(values) ? values : [[values]];
        for (let index = 0; index < rows.length; index++) {
            const row = rows[index];
            let id = Array.isArray(row) ? row[0] : row;
            if (id === "" || id == null) continue;
            if (typeof id !== "string") {
                return { error: "La feuille Suivi contient un ID enregistré comme nombre." };
            }
            if (legacy && id.startsWith("'")) {
                id = id.slice(1);
                if (!id || /^[\s\x00-\x1f]*[=+@-]/u.test(id)) {
                    return { error: "La feuille Suivi contient un ancien ID qui doit être corrigé manuellement." };
                }
                corrections.push([index + 2, id]);
            }
            if (ids.has(id)) {
                return { error: "La feuille Suivi contient un ID en double." };
            }
            ids.add(id);
        }
    }

    const newIds = payments.map(row => row[7]).filter(id => !ids.has(id));
    const nextTrackingRow = lastTrackingRow + 1;
    const finalTrackingRow = lastTrackingRow + newIds.length;
    if (finalTrackingRow > 1048576) return { error: "La feuille Suivi a atteint la capacité maximale du classeur." };

    if (!tracking) {
        tracking = Api.AddSheet("Suivi esup-pay");
        tracking.GetRange("A1:B1").SetValue([["ID transaction", "Vérifié"]]);
        tracking.GetRange("J1").SetValue(source);
        tracking.SetColumnWidth(0, 22);
        tracking.SetColumnWidth(1, 16);
    }
    for (const [row, id] of corrections) {
        if (tracking.GetRange("A" + row).SetValue(id) === false) {
            return { error: "ONLYOFFICE a refusé la correction d’un ancien ID dans la feuille Suivi." };
        }
    }
    if (tracking.GetRange("K1").GetValue() !== "esup-pay:id-text") {
        tracking.GetRange("K1").SetValue("esup-pay:id-text");
    }
    if (newIds.length) {
        const newIdRange = tracking.GetRange("A" + nextTrackingRow + ":A" + finalTrackingRow);
        newIdRange.SetNumberFormat("@");
        if (newIdRange.SetValue(newIds.map(id => [id])) === false) {
            return { error: "ONLYOFFICE a refusé l’ajout des identifiants dans la feuille Suivi." };
        }
        const checks = tracking.GetRange("B" + nextTrackingRow + ":B" + finalTrackingRow);
        const validation = checks.GetValidation();
        validation.Add("xlValidateList", null, null, "☐,☑");
        validation.SetInCellDropdown(true);
        checks.SetValue(newIds.map(() => ["☐"]));
    }

    if (!paid) {
        paid = Api.AddSheet("Paiements esup-pay");
        paid.GetRange("J1").SetValue(source);
    }
    if (workbookId && paid.GetRange("J2").GetValue() !== workbookId
        && paid.GetRange("J2").SetValue(workbookId) === false) {
        return { error: "ONLYOFFICE a refusé l’enregistrement de l’identifiant du classeur." };
    }
    const previousLastRow = paid.GetUsedRange().GetRowsCount();
    const lastRow = payments.length + 1;
    paid.GetRange("A1:H1").SetValue([headers]);
    if (payments.length) {
        paid.GetRange("A2:F" + lastRow).SetNumberFormat("@");
        paid.GetRange("G2:G" + lastRow).SetNumberFormat("0");
        paid.GetRange("H2:H" + lastRow).SetNumberFormat("@");
        if (paid.GetRange("A2:H" + lastRow).SetValue(payments) === false) {
            return { error: "ONLYOFFICE a refusé l’écriture des paiements. Vérifiez les droits d’édition." };
        }
    }
    if (previousLastRow > lastRow) {
        paid.GetRange("A" + (lastRow + 1) + ":H" + previousLastRow).Clear();
    }
    let pivots = 0;
    let pivotErrors = 0;
    // Une plage avec au moins une ligne sous les en-têtes garde les champs du TCD sur un export vide.
    const pivotLastRow = Math.max(2, lastRow);
    const pivotRange = paid.GetRange("A1:H" + pivotLastRow);
    for (const pivot of Api.GetAllPivotTables()) {
        try {
            const current = pivot.GetSource();
            if (!current || current.GetWorksheet().GetName() !== "Paiements esup-pay") continue;
            const address = current.GetAddress(true, true, "xlA1", false).replace(/\$/g, "");
            const data = /^A1:H(\d+)$/i.exec(address);
            if (data && Number(data[1]) !== pivotLastRow) {
                // Le changement de source actualise déjà le cache du TCD.
                pivot.SetSource(pivotRange);
            } else {
                pivot.RefreshTable();
            }
            pivots++;
        } catch {
            pivotErrors++;
        }
    }
    if (payments.length && !Api.GetSheet("Sommes esup-pay")) {
        try {
            const summary = Api.InsertPivotNewWorksheet(pivotRange, "Sommes esup-pay");
            summary.AddFields({ rows: ["payEvt", "payEvtMontant"] });
            const total = summary.AddDataField("montant");
            total.SetFunction("Sum");
            total.SetName("Total (centimes)");
            pivots++;
        } catch {
            pivotErrors++;
        }
    }
    return { imported: payments.length, added: newIds.length, pivots, pivotErrors };
}
