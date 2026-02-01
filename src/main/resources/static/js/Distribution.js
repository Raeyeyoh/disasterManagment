document.addEventListener("DOMContentLoaded", () => {
  loadAvailableItems();

  const params = new URLSearchParams(window.location.search);
  const incidentIdFromUrl = params.get("incidentId");
  const select = document.getElementById("incidentId");
  const contextDisplay = document.getElementById("incident-context");

  if (incidentIdFromUrl) {
    select.innerHTML = `<option value="${incidentIdFromUrl}" selected>Incident #${incidentIdFromUrl}</option>`;

    const formGroup = select.closest(".form-group");
    if (formGroup) formGroup.style.display = "none";

    if (contextDisplay) {
      contextDisplay.innerText =
        "Assigning aid for Incident Case: #" + incidentIdFromUrl;
    }
  } else {
    loadIncidents();
  }
});

document
  .getElementById("aidDistributionForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const token = localStorage.getItem("token");
    const distributionData = {
      incidentId: document.getElementById("incidentId").value,
      nationalId: document.getElementById("nationalId").value,
      victimName: document.getElementById("victimName").value,
      itemName: document.getElementById("itemName").value,
      quantity: parseInt(document.getElementById("quantity").value),
    };
    console.log("SENDING DATA:", distributionData);
    try {
      const response = await fetch(
        "http://localhost:8080/api/distribution/give-aid",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(distributionData),
        },
      );

      if (response.ok) {
        alert("Aid distributed successfully!");
        const params = new URLSearchParams(window.location.search);
        window.location.href = `feedback.html?id=${params.get("incidentId")}`;
      } else {
        const errorMsg = await response.text();
        alert("Distribution Failed: " + errorMsg);
      }
    } catch (error) {
      console.error("Network Error:", error);
      alert("Server is unreachable.");
    }
  });

async function loadAvailableItems() {
  const token = localStorage.getItem("token");
  const response = await fetch(
    "http://localhost:8080/api/regional-inventory/stock", //for loading the items to give
    {
      headers: { Authorization: `Bearer ${token}` },
    },
  );

  if (response.ok) {
    const items = await response.json();
    const select = document.getElementById("itemName");
    select.innerHTML = items
      .map(
        (item) =>
          `<option value="${item.itemName}">${item.itemName} (Available: ${item.quantity}) (${item.unit})</option>`,
      )
      .join("");
  }
}
//removed
async function loadIncidents() {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(
      "http://localhost:8080/api/incidents/my-region",
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      },
    );

    if (response.ok) {
      const incidents = await response.json();
      const select = document.getElementById("incidentId");

      if (incidents.length === 0) {
        select.innerHTML =
          '<option value="">No incidents found in your region</option>';
        return;
      }

      select.innerHTML = incidents
        .map(
          (inc) =>
            `<option value="${inc.reportId}">${inc.title} (${new Date(inc.createdAt).toLocaleString()})</option>`,
        )
        .join("");
    } else {
      console.error("Failed to load incidents. Status:", response.status);
    }
  } catch (error) {
    console.error("Network error while loading incidents:", error);
  }
}
