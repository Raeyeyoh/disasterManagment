document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const tableBody = document.getElementById("incident-table-body");

  if (!tableBody) {
    console.error("HTML Error: Could not find 'incident-table-body' element.");
    return;
  }

  let apiUrl = "http://localhost:8080/api/incidents/my-region";

  if (role === "ROLE_SUPER_ADMIN") {
    apiUrl = "http://localhost:8080/api/incidents";
  }

  try {
    const response = await fetch(apiUrl, {
      method: "GET",
      headers: {
        Authorization: "Bearer " + token,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const errorMsg = await response.text();
      throw new Error(`Server returned ${response.status}: ${errorMsg}`);
    }

    const incidents = await response.json();
    tableBody.innerHTML = "";

    if (incidents.length === 0) {
      tableBody.innerHTML =
        '<tr><td colspan="5" style="text-align:center; padding:20px;">No incidents found.</td></tr>';
      return;
    }

    incidents.forEach((incident) => {
      tableBody.innerHTML += `
                <tr>
                    <td style="padding: 12px;">#${incident.reportId}</td>
                    <td style="padding: 12px;">${incident.title}</td>
                    <td style="padding: 12px;">${incident.status}</td>

                    <td style="padding: 12px;">${new Date(
                      incident.createdAt,
                    ).toLocaleString()}</td>
                    <td style="padding: 12px; text-align: center;">
                        <button onclick="viewDetails(${
                          incident.reportId
                        })">View</button>
                    </td>
                </tr>`;
    });
  } catch (error) {
    console.error("Detailed Error:", error);
    tableBody.innerHTML = `<tr><td colspan="5" style="color:red; text-align:center; padding:20px;">Error Loading: ${error.message}</td></tr>`;
  }
});

async function viewDetails(reportId) {
  const token = localStorage.getItem("token");
  const modal = document.getElementById("incidentModal");
  const feedbackContainer = document.getElementById("modalFeedback");

  feedbackContainer.innerHTML = "<em>Loading logs...</em>";

  try {
    const incidentResponse = await fetch(
      `http://localhost:8080/api/incidents/my-region`,
      {
        headers: { Authorization: "Bearer " + token },
      },
    );
    const incidents = await incidentResponse.json();
    const incident = incidents.find((i) => i.reportId === reportId);

    const feedbackResponse = await fetch(
      `http://localhost:8080/api/incidents/${reportId}/feedback`,
      {
        headers: { Authorization: "Bearer " + token },
      },
    );

    const logData = await feedbackResponse.json();

    if (incident) {
      document.getElementById("modalTitle").innerText =
        `Incident #${incident.reportId}: ${incident.title}`;
      document.getElementById("detDescription").innerText =
        incident.description || "No description.";
      document.getElementById("detLocation").innerText = incident.location;
      document.getElementById("detSeverity").innerText = incident.severity;

      if (logData && logData.message) {
        feedbackContainer.innerHTML = `
                    <div style="padding: 10px; border-left: 4px solid #007bff; background: #f0f7ff;">
                        <pre style="white-space: pre-wrap; font-family: 'Courier New', Courier, monospace; font-size: 0.9em;">${logData.message}</pre>
                        <hr>
                        <small>Last entry: ${new Date(logData.createdAt).toLocaleString()}</small>
                    </div>
                `;
      } else {
        feedbackContainer.innerHTML =
          "<p style='color: gray;'>No activity logs recorded yet.</p>";
      }

      modal.style.display = "block";
    }
  } catch (error) {
    console.error("Error fetching incident details/feedback:", error);
    feedbackContainer.innerHTML =
      "<p style='color:red;'>Failed to load details.</p>";
  }
}
function closeModal() {
  document.getElementById("incidentModal").style.display = "none";
}

window.onclick = function (event) {
  const modal = document.getElementById("incidentModal");
  if (event.target == modal) {
    modal.style.display = "none";
  }
};
