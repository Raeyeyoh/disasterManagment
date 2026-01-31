const itemTypeSelect = document.getElementById("reqItemType");
const unitSelect = document.getElementById("reqUnit");
document.addEventListener("DOMContentLoaded", () => {
  loadLocalStock();
  loadRequestHistory();

  itemTypeSelect.addEventListener("change", () => {
    const selectedType = itemTypeSelect.value;
    const units = {
      FOOD: ["KG", "LITER", "BAG"],
      MEDICAL: ["PIECE", "BOX", "LITER"],
      EQUIPMENT: ["PIECE", "BOX"],
      OTHER: ["PIECE", "BAG", "BOX"],
    };

    unitSelect.innerHTML = units[selectedType]
      .map((u) => `<option value="${u}">${u}</option>`)
      .join("");
  });

  itemTypeSelect.dispatchEvent(new Event("change"));
});

const token = localStorage.getItem("token");

async function loadLocalStock() {
  try {
    const res = await fetch(
      "http://localhost:8080/api/regional-inventory/stock",
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    const LOW_STOCK_THRESHOLD = 10;
    const data = await res.json();

    const table = document.getElementById("local-stock-table");

    table.innerHTML = data
      .map((item) => {
        const isLowStock = item.quantity <= LOW_STOCK_THRESHOLD;

        return `
          <tr class="${isLowStock ? "low-stock" : ""}">
            <td>${item.itemName}</td>
            <td>${item.quantity}</td>
            <td>${item.type}</td>
            <td>${item.unit || "N/A"}</td>
            <td>${new Date(item.lastUpdated).toLocaleString()}</td>
          </tr>
        `;
      })
      .join("");
  } catch (err) {
    console.error("Error loading local stock:", err);
  }
}

async function submitSupplyRequest() {
  const itemName = document.getElementById("reqItemName").value;
  const quantity = document.getElementById("reqQuantity").value;
  const itemType = document.getElementById("reqItemType").value;
  if (!itemName || !quantity) return alert("Please fill in all fields");

  try {
    const res = await fetch(
      "http://localhost:8080/api/regional-inventory/request/submit",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          itemName,
          quantity: parseInt(quantity),
          itemType,
          unit: unitSelect.value.toUpperCase(),
        }),
      },
    );

    if (res.ok) {
      alert("Request sent successfully!");
      document.getElementById("request-form").reset();
      loadRequestHistory();
    } else {
      alert("Failed to send request.");
    }
  } catch (err) {
    console.error("Error submitting request:", err);
  }
}

async function loadRequestHistory() {
  try {
    const res = await fetch(
      "http://localhost:8080/api/regional-inventory/requests",
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );
    const data = await res.json();
    const table = document.getElementById("request-history-table");

    table.innerHTML = data
      .map(
        (req) => `
            <tr>
                <td>${new Date(req.createdAt).toLocaleDateString()}</td>
                <td>${req.itemName}</td>
                <td>${req.quantity}</td>
                  <td>${req.unit}</td>

                <td>${req.itemType}</td>
                <td style="color: ${req.status === "APPROVED" ? "green" : "orange"}; font-weight: bold;">
                    ${req.status}
                </td>
            </tr>
        `,
      )
      .join("");
  } catch (err) {
    console.error("Error loading history:", err);
  }
}
