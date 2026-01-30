let map;
document.addEventListener("DOMContentLoaded", () => {
  map = L.map("map").setView([9.145, 40.489], 6);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(map);
});

function captureGPS() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      const lat = position.coords.latitude.toFixed(6);
      const lng = position.coords.longitude.toFixed(6);
      document.getElementById("location").value = `${lat}, ${lng}`;
      map.setView([lat, lng], 15);
      L.marker([lat, lng]).addTo(map).bindPopup("Your Location").openPopup();
    });
  }
}

document
  .getElementById("incidentForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const incidentData = {
      title: document.getElementById("title").value,
      location: document.getElementById("location").value,
      description: document.getElementById("description").value,
    };

    const token = localStorage.getItem("token");

    const response = await fetch("http://localhost:8080/api/incidents/report", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(incidentData),
    });

    if (response.ok) {
      alert("Incident Reported Successfully!");

      document.getElementById("incidentForm").style.display = "none";
      document.getElementById("main-nav").style.display = "none";
      document.getElementById("post-report-actions").style.display = "block";
    }
  });

async function findNearby(type) {
  const locationStr = document.getElementById("location").value;
  if (!locationStr) return;

  const [lat, lng] = locationStr.split(",").map(Number);
  const radius = 5000;

  const amenityMap = {
    hospital: "hospital",
    police: "police",
    water_point: "drinking_water",
  };

  const amenity = amenityMap[type];
  const query = `[out:json];node["amenity"="${amenity}"](around:${radius},${lat},${lng});out;`;
  const url =
    "https://overpass-api.de/api/interpreter?data=" + encodeURIComponent(query);

  const color =
    type === "hospital" ? "green" : type === "police" ? "blue" : "gold";
  const customIcon = L.icon({
    iconUrl: `https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-${color}.png`,
    shadowUrl:
      "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
  });

  try {
    const res = await fetch(url);
    const data = await res.json();

    data.elements.forEach((item) => {
      L.marker([item.lat, item.lon], { icon: customIcon })
        .addTo(map)
        .bindPopup(
          `<b>${type.toUpperCase()}</b><br>${item.tags.name || "Facility"}`,
        );
    });
    map.setView([lat, lng], 13);
  } catch (error) {
    console.error("Error:", error);
  }
}
