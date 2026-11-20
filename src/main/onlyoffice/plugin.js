"use strict";

const form = document.getElementById("export-form");
const refresh = document.getElementById("refresh");
const status = document.getElementById("status");
const urlField = document.getElementById("export-url");
const tokenField = document.getElementById("export-token");
const remember = document.getElementById("remember-token");
const forget = document.getElementById("forget-token");
const storagePrefix = "esup-pay:onlyoffice:bearer:";
const tokenPattern = /^[A-Za-z0-9_-]{43}$/;
let initialized = false;
let workbookId = "";

function runCommand(command, recalculate = false) {
    return new Promise(resolve => Asc.plugin.callCommand(command, false, recalculate, resolve));
}

function exportUrl(value) {
    let url;
    try {
        url = new URL(value.trim());
    } catch {
        throw new Error("Collez l’URL HTTPS affichée par esup-pay.");
    }
    if (url.protocol !== "https:" || url.username || url.password || url.search || url.hash
        || !/\/api\/payment-exports\/\d+\/payments\.csv$/.test(url.pathname)) {
        throw new Error("Collez l’URL HTTPS affichée par esup-pay.");
    }
    return url.href;
}

function storageKey(source) {
    return storagePrefix + workbookId + ":" + source;
}

function savedToken(source) {
    if (!workbookId) return "";
    try {
        const token = localStorage.getItem(storageKey(source));
        return tokenPattern.test(token || "") ? token : "";
    } catch {
        return "";
    }
}

Asc.plugin.init = async function () {
    if (initialized) return;
    initialized = true;
    Asc.scope = Asc.scope || {};
    try {
        const binding = await runCommand(readWorkbookSource);
        if (!binding?.source) return;
        const source = exportUrl(binding.source);
        workbookId = typeof binding.id === "string" && /^[a-f0-9-]{36}$/.test(binding.id) ? binding.id : "";
        urlField.value = source;
        urlField.readOnly = true;
        const token = savedToken(source);
        if (token) {
            remember.checked = true;
            forget.hidden = false;
            await refreshPayments(source, token);
        } else {
            status.textContent = "Saisissez le jeton pour actualiser ce classeur.";
        }
    } catch (error) {
        status.textContent = error.message;
    } finally {
        refresh.disabled = false;
    }
};

Asc.plugin.button = function () {
    Asc.plugin.executeCommand("close", "");
};

forget.addEventListener("click", () => {
    try {
        localStorage.removeItem(storageKey(exportUrl(urlField.value)));
        remember.checked = false;
        forget.hidden = true;
        status.textContent = "Le jeton a été effacé de ce navigateur.";
    } catch {
        status.textContent = "Impossible d’effacer le jeton de ce navigateur.";
    }
});

form.addEventListener("submit", async event => {
    event.preventDefault();
    if (refresh.disabled) return;
    refresh.disabled = true;

    try {
        const source = exportUrl(urlField.value);
        const token = tokenField.value.trim().replace(/^Bearer +/i, "") || savedToken(source);
        if (!tokenPattern.test(token)) throw new Error("Collez le jeton généré par esup-pay.");

        if (remember.checked && !workbookId) workbookId = crypto.randomUUID();
        await refreshPayments(source, token);
        urlField.value = source;
        urlField.readOnly = true;
        tokenField.value = "";

        try {
            if (remember.checked) localStorage.setItem(storageKey(source), token);
            else if (workbookId) localStorage.removeItem(storageKey(source));
            forget.hidden = !remember.checked;
        } catch {
            status.textContent += " Le navigateur a refusé la modification du jeton enregistré.";
        }
    } catch (error) {
        status.textContent = error.message;
    } finally {
        refresh.disabled = false;
    }
});

function refreshPayments(source, token) {
    const synchronize = async () => {
        status.textContent = "Chargement des paiements…";
        let response;
        try {
            response = await fetch(source, {
                headers: { Authorization: "Bearer " + token },
                credentials: "omit",
                cache: "no-store",
                redirect: "error"
            });
        } catch {
            throw new Error("Export inaccessible. Vérifiez l’URL, le proxy et l’origine CORS du plugin.");
        }
        if (response.status === 401) throw new Error("Jeton refusé. Saisissez un jeton actif pour cet événement.");
        if (!response.ok) throw new Error("L’export a renvoyé une erreur HTTP " + response.status + ".");
        if (!/^text\/csv(?:;|$)/i.test(response.headers.get("content-type") || "")) {
            throw new Error("La réponse d’esup-pay n’est pas un CSV.");
        }

        const payments = readPayments(await response.text());
        status.textContent = "Mise à jour du classeur…";
        Asc.scope.paymentExport = { source, workbookId, headers: paymentHeaders, payments };
        let result;
        try {
            result = await runCommand(updateWorkbook, true);
        } finally {
            delete Asc.scope.paymentExport;
        }
        if (!result || result.error) throw new Error(result?.error || "ONLYOFFICE n’a pas confirmé la mise à jour.");
        status.textContent = result.imported + " paiement(s) actualisé(s), " + result.added
            + " ajouté(s) au suivi et " + result.pivots + " TCD actualisé(s).";
        if (result.pivotErrors) {
            status.textContent += " " + result.pivotErrors + " TCD en erreur. Vérifiez leur plage et leurs droits d’édition.";
        }
    };

    // Le verrou est partagé entre les onglets du même navigateur et de la même origine.
    return navigator.locks
        ? navigator.locks.request(storagePrefix + source, synchronize)
        : synchronize();
}
