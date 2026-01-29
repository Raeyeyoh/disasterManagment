function renderStatusChart(incidents) {
  const stats = { PENDING: 0, RESOLVED: 0 };
  incidents.forEach(
    (inc) => (stats[inc.status] = (stats[inc.status] || 0) + 1),
  );

  new Chart(document.getElementById("statusChart"), {
    type: "doughnut",
    data: {
      labels: Object.keys(stats),
      datasets: [
        {
          data: Object.values(stats),
          backgroundColor: ["#ffcc00", "#28a745"],
        },
      ],
    },
    options: {
      plugins: { title: { display: true, text: "National Status Summary" } },
    },
  });
}

function renderRegionalChart(incidents) {
  const regionCounts = {};

  incidents.forEach((inc) => {
    const regionName = inc.region?.regionName || "Unassigned";

    regionCounts[regionName] = (regionCounts[regionName] || 0) + 1;
  });

  const chartStatus = Chart.getChart("regionChart");
  if (chartStatus) {
    chartStatus.destroy();
  }

  new Chart(document.getElementById("regionChart"), {
    type: "bar",
    data: {
      labels: Object.keys(regionCounts),
      datasets: [
        {
          label: "Incident Count",
          data: Object.values(regionCounts),
          backgroundColor: "#4e73df",
        },
      ],
    },
    options: {
      indexAxis: "y",
      plugins: {
        title: { display: true, text: "Incidents by Region" },
        legend: { display: false },
      },
      scales: {
        x: { beginAtZero: true, ticks: { stepSize: 1 } },
      },
    },
  });
}
document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const chartsSection = document.getElementById("chartsSection");
  const mapSection = document.getElementById("map-section");

  try {
    let uriapi = "";
    if (role == "ROLE_SUPER_ADMIN") {
      uriapi = "http://localhost:8080/api/incidents";
      if (mapSection) mapSection.style.display = "none";
    } else {
      uriapi = "http://localhost:8080/api/incidents/my-region";
      if (chartsSection) chartsSection.style.display = "none";
    }
    const response = await fetch(uriapi, {
      headers: { Authorization: "Bearer " + token },
    });

    if (response.ok) {
      const incidents = await response.json();
      if (role === "ROLE_REGIONAL_ADMIN") {
        initRegionalMap(incidents);
      }
      if (role === "ROLE_SUPER_ADMIN") {
        renderStatusChart(incidents);
        renderRegionalChart(incidents);
        renderSeverityLineChart(incidents);
        loadStackedApprovedByItemType();
      }
    } else {
      console.error("Failed to fetch data for charts");
    }
  } catch (error) {
    console.error("Error connecting to server:", error);
  }
});
function initRegionalMap(incidents) {
  const mapContainer = document.getElementById("map");
  if (!mapContainer) return;

  const map = L.map("map").setView([0, 0], 2);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap contributors",
  }).addTo(map);

  const markers = [];

  incidents.forEach((incident) => {
    if (incident.location) {
      const coords = incident.location.split(",");
      const lat = parseFloat(coords[0]);
      const lng = parseFloat(coords[1]);

      if (!isNaN(lat) && !isNaN(lng)) {
        const marker = L.marker([lat, lng])
          .addTo(map)
          .bindPopup(
            `<b>Incident #${incident.reportId}</b><br>${incident.title}<br>Status: ${incident.status}`,
          );
        markers.push([lat, lng]);
      }
    }
  });

  if (markers.length > 0) {
    const bounds = L.latLngBounds(markers);
    map.fitBounds(bounds);
  }
}
function renderSeverityLineChart(incidents) {
  const dailySeverity = {};

  incidents.forEach((inc) => {
    const date = new Date(inc.createdAt).toLocaleDateString();

    if (!dailySeverity[date]) {
      dailySeverity[date] = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
    }

    if (dailySeverity[date][inc.severity] !== undefined) {
      dailySeverity[date][inc.severity]++;
    } else {
      dailySeverity[date]["LOW"]++;
    }
  });

  const dates = Object.keys(dailySeverity).sort(
    (a, b) => new Date(a) - new Date(b),
  );

  const lowData = dates.map((d) => dailySeverity[d].LOW);
  const mediumData = dates.map((d) => dailySeverity[d].MEDIUM);
  const highData = dates.map((d) => dailySeverity[d].HIGH);
  const criticalData = dates.map((d) => dailySeverity[d].CRITICAL);

  const ctx = document.getElementById("severityLineChart").getContext("2d");

  if (window.myLineChart) {
    window.myLineChart.destroy();
  }

  window.myLineChart = new Chart(ctx, {
    type: "line",
    data: {
      labels: dates,
      datasets: [
        {
          label: "LOW",
          data: lowData,
          borderColor: "#27ae60",
          backgroundColor: "rgba(39, 174, 96, 0.1)",
          fill: true,
          tension: 0.4,
        },
        {
          label: "MEDIUM",
          data: mediumData,
          borderColor: "#f39c12",
          backgroundColor: "rgba(243, 156, 18, 0.1)",
          fill: true,
          tension: 0.4,
        },
        {
          label: "HIGH",
          data: highData,
          borderColor: "#e67e22",
          backgroundColor: "rgba(230, 126, 34, 0.1)",
          fill: true,
          tension: 0.4,
        },
        {
          label: "CRITICAL",
          data: criticalData,
          borderColor: "#c0392b",
          backgroundColor: "rgba(192, 57, 43, 0.2)",
          fill: true,
          tension: 0.4,
        },
      ],
    },
    options: {
      responsive: true,
      plugins: {
        title: { display: true, text: "Incident Severity Trends by Level" },
      },
      scales: {
        y: {
          beginAtZero: true,
          stacked: true,
          title: { display: true, text: "Number of Incidents" },
        },
        x: {
          stacked: true,
        },
      },
    },
  });
}
const token = localStorage.getItem("token");

async function loadStackedApprovedByItemType() {
  const res = await fetch(
    "http://localhost:8080/api/central-inventory/analytics/approved-by-region",
    {
      headers: { Authorization: `Bearer ${token}` },
    },
  );

  const data = await res.json();

  const regions = [...new Set(data.map((d) => d.region))];
  const itemTypes = [...new Set(data.map((d) => d.itemType))];

  const datasets = itemTypes.map((type) => ({
    label: type,
    data: regions.map((region) => {
      const record = data.find(
        (d) => d.region === region && d.itemType === type,
      );
      return record ? record.totalQuantity : 0;
    }),
    borderWidth: 1,
  }));

  const ctx = document.getElementById("approvedQuantityChart").getContext("2d");

  new Chart(ctx, {
    type: "bar",
    data: {
      labels: regions,
      datasets: datasets,
    },
    options: {
      responsive: true,
      plugins: {
        title: {
          display: true,
          text: "Approved Resource Distribution by Region & Item Type",
        },
      },
      scales: {
        x: {
          stacked: true,
          title: {
            display: true,
            text: "Region",
          },
        },
        y: {
          stacked: true,
          beginAtZero: true,
          title: {
            display: true,
            text: "Total Quantity",
          },
        },
      },
    },
  });
}
function downloadChart(chartId, filename) {
  const canvas = document.getElementById(chartId);

  const link = document.createElement("a");

  link.href = canvas.toDataURL("image/png", 1.0);

  link.download = filename;

  link.click();
}
