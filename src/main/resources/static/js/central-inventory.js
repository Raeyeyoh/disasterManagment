document.addEventListener("DOMContentLoaded", () => {
  loadCentralStock();
  loadPendingRequests();
});

const token = localStorage.getItem("token");

async function loadCentralStock() {
  try {
    const res = await fetch(
      "http://localhost:8080/api/central-inventory/stock",
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );
    const data = await res.json();
    const table = document.getElementById("central-stock-table");

    table.innerHTML = data
      .map(
        (item) => `
            <tr>
                <td>${item.itemName}</td>
                <td>${item.type}</td>
                <td>${item.quantity}</td>
                <td>${item.unit || "N/A"}</td>
            </tr>
        `,
      )
      .join("");
  } catch (err) {
    console.error("Error loading central stock:", err);
  }
}

async function restockCentral() {
  const item = {
    itemName: document.getElementById("itemName").value,
    quantity: parseInt(document.getElementById("quantity").value),
    type: document.getElementById("itemType").value,
    unit: document.getElementById("unit").value,
  };

  try {
    const res = await fetch(
      "http://localhost:8080/api/central-inventory/stock/add",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(item),
      },
    );

    if (res.ok) {
      alert("Central Catalog Updated!");
      document.getElementById("add-resource-form").reset();
      loadCentralStock();
    }
  } catch (err) {
    console.error("Error restocking:", err);
  }
}

async function loadPendingRequests() {
  try {
    const res = await fetch(
      "http://localhost:8080/api/central-inventory/requests/pending",
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );
    const data = await res.json();
    const table = document.getElementById("pending-requests-table");

    table.innerHTML = data
      .map((req) => {
        let badgeClass = "";
        switch (req.priority) {
          case "HIGH":
            badgeClass = "high";
            break;
          case "MEDIUM":
            badgeClass = "medium";
            break;
          case "LOW":
            badgeClass = "low";
            break;
          default:
            badgeClass = "";
        }

        return `
          <tr>
            <td>${req.region.regionName}</td>
            <td>${req.itemName}</td>
            <td>${req.quantity}</td>
            <td>${req.unit}</td>
            <td>${req.itemType}</td>
            <td>${req.requestedBy.username}</td>
            <td><span class="priority-badge ${badgeClass}">${req.priority}</span></td>
            <td>
              <button onclick="approveRequest(${req.requestId})" style="background: green; color: white;">Approve</button>
              <button onclick="rejectRequest(${req.requestId})" style="background: red; color: white;">Reject</button>
            </td>
          </tr>
        `;
      })
      .join("");
  } catch (err) {
    console.error("Error loading pending requests:", err);
  }
}

async function approveRequest(id) {
  if (
    !confirm(
      "Are you sure you want to approve this transfer? Stock will be deducted from Central.",
    )
  )
    return;

  try {
    const res = await fetch(
      `http://localhost:8080/api/central-inventory/requests/${id}/approve`,
      {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (res.ok) {
      alert("Transfer Complete!");
      loadPendingRequests();
      loadCentralStock();
    } else {
      const errorMsg = await res.text();
      alert("Failed: " + errorMsg);
    }
  } catch (err) {
    console.error("Error approving request:", err);
  }
}
async function rejectRequest(requestId) {
  try {
    const res = await fetch(
      `http://localhost:8080/api/central-inventory/requests/${requestId}/reject`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (res.ok) {
      alert("Request rejected .");
      loadPendingRequests();
    } else {
      const errorData = await res.json();
      alert(
        "Failed to reject request: " + (errorData.message || res.statusText),
      );
    }
  } catch (err) {
    console.error("Error rejecting request:", err);
    alert("An error occurred while rejecting the request.");
  }
}
