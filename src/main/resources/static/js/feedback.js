document.addEventListener("DOMContentLoaded", () => {
  const userRole = localStorage.getItem("role");
  const params = new URLSearchParams(window.location.search);
  const incidentIdFromUrl = params.get("incidentId");

  const submissionSection = document.getElementById("submission-section");
  const adminFeedSection = document.getElementById("admin-feed-section");
  const feedbackContainer = document.getElementById("feedbackContainer");
  const feedbackForm = document.getElementById("feedbackForm");
  const incidentSelectGroup = document.getElementById("admin");
  const submitBtn = document.getElementById("Submit");

  if (userRole === "ROLE_REGIONAL_ADMIN") {
    if (adminFeedSection) adminFeedSection.style.display = "block";
    if (submissionSection) submissionSection.style.display = "block";
    if (incidentSelectGroup) incidentSelectGroup.style.display = "block";
    submitBtn.innerText = "Submit & Resolve Incident";
    submitBtn.style.backgroundColor = "#e74c3c";
    if (feedbackContainer) fetchFeedback();
  } else if (
    ["ROLE_POLICE", "ROLE_VOLUNTEER", "ROLE_REGIONAL_STAFF"].includes(userRole)
  ) {
    if (submissionSection) submissionSection.style.display = "block";
    if (adminFeedSection) adminFeedSection.style.display = "none";
    submitBtn.innerText = "Submit Field Report";
  }

  const select = document.getElementById("incidentSelect");
  if (incidentIdFromUrl) {
    select.innerHTML = `<option value="${incidentIdFromUrl}" selected>Reporting for Incident #${incidentIdFromUrl}</option>`;
    const formGroup = select.closest(".form-group");
    if (formGroup) formGroup.style.display = "none";
  } else {
    loadIncidentsForFeedback();
  }

  if (feedbackForm) {
    feedbackForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const token = localStorage.getItem("token");
      const incidentId = select.value;

      const feedbackData = {
        message: document.getElementById("msg").value,
        rating: parseInt(document.getElementById("rate").value),
      };

      try {
        const response = await fetch(
          `http://localhost:8080/api/incidents/${incidentId}/feedback`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(feedbackData),
          },
        );

        if (response.ok) {
          const resultMsg = await response.text();
          alert(resultMsg);
          window.location.href = `home.html?id=${incidentId}`;
        } else {
          const error = await response.text();
          alert("Failed to submit: " + error);
        }
      } catch (err) {
        console.error("Submission error:", err);
        alert("Server unreachable.");
      }
    });
  }
});
async function loadIncidentsForFeedback() {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(
      "http://localhost:8080/api/incidents/my-region",
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (response.ok) {
      const incidents = await response.json();
      const select = document.getElementById("incidentSelect");
      if (select) {
        select.innerHTML =
          incidents.length > 0
            ? incidents
                .map(
                  (inc) =>
                    `<option value="${inc.reportId}">${inc.title} (${inc.status}) (${inc.createdAt})</option>`,
                )
                .join("")
            : '<option value="">No active incidents in your region</option>';
      }
    }
  } catch (err) {
    console.error("Error loading incidents:", err);
  }
}

async function fetchFeedback() {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(
      "http://localhost:8080/api/incidents/feedback/my-region",
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (response.ok) {
      const feedbackLogs = await response.json();
      const unresolvedLogs = feedbackLogs.filter(
        (log) => log.report && log.report.status !== "RESOLVED",
      );

      renderFeedback(unresolvedLogs);
    }
  } catch (error) {
    console.error("Network Error:", error);
  }
}

function renderFeedback(logs) {
  const container = document.getElementById("feedbackContainer");
  const userRole = localStorage.getItem("role");
  if (!container) return;

  container.innerHTML = logs
    .map((log) => {
      const isResolved = log.report && log.report.status === "RESOLVED";

      return `
        <div class="card feedback-card" style="margin-bottom: 20px; border-left: 5px solid ${isResolved ? "#95a5a6" : "#2ecc71"}">
            <div class="feedback-meta">
                <strong>Case: ${log.report ? log.report.title : "Unknown"}</strong>
                <span style="float: right;">${new Date(log.createdAt).toLocaleString()}</span>
            </div>
            <div class="feedback-body" style="background: #f8f9fa; padding: 10px; border-radius: 4px; margin: 10px 0; white-space: pre-wrap;">
                ${log.message}
            </div>
            <div class="feedback-footer" style="display: flex; justify-content: space-between; align-items: center;">
                <span>By: ${log.submittedBy ? log.submittedBy.username : "System"}</span>
                
                ${
                  userRole === "ROLE_REGIONAL_ADMIN" && !isResolved
                    ? `<button onclick="handleResolve(${log.report.reportId})" class="btn-primary" style="background: #e74c3c; border:none; padding: 5px 10px; border-radius:4px;">Resolve Incident</button>`
                    : isResolved
                      ? '<span style="color: #7f8c8d; font-weight: bold;">✅ RESOLVED</span>'
                      : ""
                }
            </div>
        </div>`;
    })
    .join("");
}

async function handleResolve(id) {
  if (!confirm("Confirming this will close the incident  Proceed?")) return;

  const token = localStorage.getItem("token");
  try {
    const response = await fetch(
      `http://localhost:8080/api/incidents/${id}/resolve`,
      {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (response.ok) {
      alert("Incident has been officially RESOLVED.");
      location.reload();
    } else {
      const err = await response.text();
      alert("Error: " + err);
    }
  } catch (err) {
    console.error("Resolve failed:", err);
  }
}
