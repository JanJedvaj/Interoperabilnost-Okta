const state = {
    accessToken: localStorage.getItem("accessToken"),
    refreshToken: localStorage.getItem("refreshToken"),
    role: localStorage.getItem("role")
};

const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", () => {
    bindTabs();
    bindAuth();
    bindApplications();
    bindImports();
    bindOkta();
    bindSoap();
    bindWeather();
    bindGraphql();
    renderSession();
});

function bindTabs() {
    document.querySelectorAll(".tab").forEach((tab) => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".tab").forEach((item) => item.classList.remove("active"));
            document.querySelectorAll(".tab-panel").forEach((panel) => panel.classList.add("hidden"));
            tab.classList.add("active");
            document.querySelector(`[data-panel="${tab.dataset.tab}"]`).classList.remove("hidden");
        });
    });
}

function bindAuth() {
    $("loginForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const response = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({
                username: $("username").value,
                password: $("password").value
            }),
            skipAuth: true
        });

        state.accessToken = response.accessToken;
        state.refreshToken = response.refreshToken;
        state.role = parseRole(response.accessToken);
        localStorage.setItem("accessToken", state.accessToken);
        localStorage.setItem("refreshToken", state.refreshToken);
        localStorage.setItem("role", state.role);
        renderSession();
        setStatus("Login uspješan.");
    });

    $("logoutButton").addEventListener("click", () => {
        localStorage.clear();
        state.accessToken = null;
        state.refreshToken = null;
        state.role = null;
        renderSession();
        setStatus("Odjavljeni ste.");
    });
}

function bindApplications() {
    $("loadApplicationsButton").addEventListener("click", loadApplications);
    $("resetApplicationForm").addEventListener("click", resetApplicationForm);
    $("applicationForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const id = $("applicationId").value;
        const payload = applicationPayload();
        const response = await api(id ? `/api/applications/${id}` : "/api/applications", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        setStatus(`Spremljeno: ${response.label}`);
        resetApplicationForm();
        await loadApplications();
    });
}

function bindImports() {
    $("jsonImportForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const response = await api("/api/applications/import/json", {
            method: "POST",
            body: $("jsonImportDocument").value
        });
        setStatus(`JSON import spremljen: ${response.label}`);
    });

    $("xmlImportForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const response = await api("/api/applications/import/xml", {
            method: "POST",
            headers: {"Content-Type": "application/xml"},
            body: $("xmlImportDocument").value
        });
        setStatus(`XML import spremljen: ${response.label}`);
    });
}

function bindOkta() {
    $("loadOktaButton").addEventListener("click", async () => {
        const applications = await api("/api/okta/applications");
        renderApplicationCards($("oktaResult"), applications, false);
        setStatus(`Okta vratila ${applications.length} aplikacija.`);
    });

    $("syncOktaButton").addEventListener("click", async () => {
        const response = await api("/api/applications/sync/okta", {method: "POST"});
        setStatus(`Sync gotov. Primljeno: ${response.receivedFromOkta}, spremljeno: ${response.savedLocally}.`);
    });
}

function bindSoap() {
    $("soapForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const term = escapeXml($("soapSearchTerm").value);
        const envelope = `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:app="https://algebra.hr/iis/okta/applications">
  <soapenv:Header/>
  <soapenv:Body>
    <app:searchApplications>
      <searchTerm>${term}</searchTerm>
    </app:searchApplications>
  </soapenv:Body>
</soapenv:Envelope>`;

        const text = await raw("/ws/applications", {
            method: "POST",
            headers: {
                "Content-Type": "text/xml;charset=UTF-8",
                "SOAPAction": ""
            },
            body: envelope
        });
        $("soapResult").textContent = text;
        setStatus("SOAP response dohvaćen.");
    });
}

function bindWeather() {
    $("weatherForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const query = encodeURIComponent($("weatherQuery").value);
        const observations = await api(`/api/weather/dhmz/cities?query=${query}`);
        $("weatherResult").innerHTML = observations.map((item) => `
            <article class="card">
                <h3>${escapeHtml(item.city)}</h3>
                <div class="meta">
                    Temperatura: ${escapeHtml(item.temperature)}<br>
                    Vlaga: ${escapeHtml(item.humidity)}<br>
                    Tlak: ${escapeHtml(item.pressure)}<br>
                    Vrijeme: ${escapeHtml(item.weather)}
                </div>
            </article>
        `).join("");
        setStatus(`DHMZ pronašao ${observations.length} mjesta.`);
    });
}

function bindGraphql() {
    $("graphqlForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const response = await api("/graphql", {
            method: "POST",
            body: JSON.stringify({query: $("graphqlQuery").value})
        });
        $("graphqlResult").textContent = JSON.stringify(response, null, 2);
        setStatus("GraphQL izvršen.");
    });
}

async function loadApplications() {
    const applications = await api("/api/applications");
    renderApplicationCards($("applicationsList"), applications, true);
    setStatus(`Učitano ${applications.length} aplikacija.`);
}

function renderApplicationCards(container, applications, editable) {
    container.innerHTML = applications.map((app) => `
        <article class="card">
            <h3>${escapeHtml(app.label)}</h3>
            <div class="meta">
                Resource ID: ${escapeHtml(app.resourceId ?? app.id ?? app.externalId ?? "")}<br>
                External ID: ${escapeHtml(app.externalId ?? "")}<br>
                Name: ${escapeHtml(app.name)}<br>
                Status: ${escapeHtml(app.status)}<br>
                Sign-on: ${escapeHtml(app.signOnMode)}
            </div>
            ${editable && isFullAccess() ? `
                <div class="button-row">
                    <button class="secondary" onclick='editApplication(${JSON.stringify(app)})'>Edit</button>
                    <button class="danger" onclick='deleteApplication("${escapeHtml(app.resourceId ?? app.id)}")'>Delete</button>
                </div>
            ` : ""}
        </article>
    `).join("");
}

window.editApplication = (app) => {
    $("applicationId").value = app.resourceId ?? app.id ?? "";
    $("externalId").value = app.externalId ?? "";
    $("name").value = app.name ?? "";
    $("label").value = app.label ?? "";
    $("status").value = app.status ?? "";
    $("signOnMode").value = app.signOnMode ?? "";
    setStatus(`Uređivanje: ${app.label}`);
};

window.deleteApplication = async (id) => {
    if (!confirm(`Obrisati aplikaciju ${id}?`)) {
        return;
    }
    await raw(`/api/applications/${encodeURIComponent(id)}`, {method: "DELETE"});
    setStatus(`Obrisano: ${id}`);
    await loadApplications();
};

function applicationPayload() {
    return {
        externalId: $("externalId").value || null,
        name: $("name").value,
        label: $("label").value,
        status: $("status").value,
        signOnMode: $("signOnMode").value
    };
}

function resetApplicationForm() {
    $("applicationId").value = "";
    $("externalId").value = "";
    $("name").value = "";
    $("label").value = "";
    $("status").value = "ACTIVE";
    $("signOnMode").value = "BOOKMARK";
}

async function api(path, options = {}) {
    const text = await raw(path, options);
    if (!text) {
        return null;
    }
    return JSON.parse(text);
}

async function raw(path, options = {}) {
    try {
        const headers = {
            "Content-Type": "application/json",
            ...(options.headers || {})
        };
        if (state.accessToken && !options.skipAuth) {
            headers.Authorization = `Bearer ${state.accessToken}`;
        }

        const response = await fetch(path, {...options, headers});
        const text = await response.text();

        if (!response.ok) {
            throw new Error(text || `${response.status} ${response.statusText}`);
        }

        return text;
    } catch (error) {
        setStatus(error.message);
        throw error;
    }
}

function renderSession() {
    const loggedIn = Boolean(state.accessToken);
    $("loginPanel").classList.toggle("hidden", loggedIn);
    $("appPanel").classList.toggle("hidden", !loggedIn);
    $("logoutButton").classList.toggle("hidden", !loggedIn);
    $("roleBadge").textContent = loggedIn ? `Role: ${state.role}` : "Niste prijavljeni";
    $("roleBadge").classList.toggle("ok", loggedIn);

    document.querySelectorAll(".full-access-only").forEach((element) => {
        element.classList.toggle("hidden", loggedIn && !isFullAccess());
    });
}

function isFullAccess() {
    return state.role === "FULL_ACCESS";
}

function parseRole(token) {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    const roles = payload.roles || [];
    if (roles.includes("ROLE_FULL_ACCESS")) {
        return "FULL_ACCESS";
    }
    if (roles.includes("ROLE_READ_ONLY")) {
        return "READ_ONLY";
    }
    return "UNKNOWN";
}

function setStatus(message) {
    $("statusBox").textContent = typeof message === "string" ? message : JSON.stringify(message, null, 2);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeXml(value) {
    return escapeHtml(value);
}
