let allAcknowledgements = [];

async function loadFullResponseDashboard() {
  const res = await fetch(
    `http://localhost:8080/api/incidents/regional-activity`,
    {
      headers: { Authorization: "Bearer " + localStorage.getItem("token") },
    },
  );
  allAcknowledgements = await res.json();

  populateFilter(allAcknowledgements);
  renderCards(allAcknowledgements);
}

function populateFilter(data) {
  const filter = document.getElementById("incident-filter");
  const titles = [...new Set(data.map((ack) => ack.incident.title))];

  filter.innerHTML =
    '<option value="ALL">All Active Incidents</option>' +
    titles.map((t) => `<option value="${t}">${t}</option>`).join("");
}

function filterResponseData() {
  const selectedTitle = document.getElementById("incident-filter").value;

  if (selectedTitle === "ALL") {
    renderCards(allAcknowledgements);
  } else {
    const filtered = allAcknowledgements.filter(
      (ack) => ack.incident.title === selectedTitle,
    );
    renderCards(filtered);
  }
}

function renderCards(data) {
  const grid = document.getElementById("response-grid");

  const grouped = data.reduce((acc, current) => {
    const incidentId = current.incident.reportId;
    if (!acc[incidentId]) {
      acc[incidentId] = {
        title: current.incident.title,
        severity: current.incident.severity,
        createdAt: current.incident.createdAt,
        responders: [],
      };
    }
    acc[incidentId].responders.push(current);
    return acc;
  }, {});

  grid.innerHTML = Object.values(grouped)
    .map(
      (group) => `
    <div class="incident-card">
        <h4>${group.title} <span class="severity-${group.severity}">${group.severity}</span></h4>
        <h3>${new Date(group.createdAt).toLocaleDateString()}</h3>
        <hr>
        <ul style="list-style: none; padding: 0;">
            ${group.responders
              .map(
                (person) => `
                <li style="margin-bottom: 10px; display: flex; justify-content: space-between;">
                    <span><strong>${person.user.username}</strong> (${person.role})</span>
                    <span class="status-pill ${person.status.toLowerCase()}">${person.status}</span>
                </li>
            `,
              )
              .join("")}
        </ul>
    </div>
  `,
    )
    .join("");
}
window.onload = loadFullResponseDashboard;
