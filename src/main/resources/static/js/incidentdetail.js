const params = new URLSearchParams(window.location.search);
const incidentId = params.get("id");
const token = localStorage.getItem("token");
const currentUserId = localStorage.getItem("userId");

if (incidentId) {
  fetch(`http://localhost:8080/api/incidents/${incidentId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  })
    .then((res) => res.json())
    .then((inc) => {
      document.getElementById("main-title").innerText = inc.title;
      document.getElementById("display-title").innerText = inc.title;
      document.getElementById("description").innerText = inc.description;
      document.getElementById("severity").innerText = inc.severity;
      document.getElementById("status").innerText = inc.status;
      document.getElementById("region").innerText = inc.region.regionName;

      const [lat, lng] = inc.location.split(",").map((v) => Number(v.trim()));
      window.incidentLat = lat;
      window.incidentLng = lng;

      // Calculate distance
      navigator.geolocation.getCurrentPosition((pos) => {
        const d = calculateDistance(
          pos.coords.latitude,
          pos.coords.longitude,
          lat,
          lng,
        );
        document.getElementById("distance").innerText = d + " km";
      });

      checkMyStatus();
    })
    .catch((err) => console.error("Error loading details:", err));
}

async function checkMyStatus() {
  const currentUserId = localStorage.getItem("userId");
  const btnAccept = document.getElementById("btn-accept");
  const actionGroup = document.getElementById("action-group");

  console.log("Checking status for User:", currentUserId); // DEBUG

  try {
    const res = await fetch(
      `http://localhost:8080/api/incidents/incident/${incidentId}`,
      {
        headers: { Authorization: "Bearer " + token },
      },
    );

    if (res.ok) {
      const data = await res.json();
      console.log("Status Data received:", data); // DEBUG

      // Ensure data is an array before using .find()
      const acks = Array.isArray(data) ? data : [data];

      const myAck = acks.find(
        (ack) => ack.user && String(ack.user.userId) === String(currentUserId),
      );

      if (myAck && myAck.status === "RESPONDING") {
        console.log("MATCH FOUND: Setting UI to Responding");
        btnAccept.style.display = "none";
        actionGroup.style.display = "flex";
      } else {
        console.log("NO MATCH: Showing Accept Button");
        btnAccept.style.display = "block";
        actionGroup.style.display = "none";
      }
    }
  } catch (err) {
    console.error("Status check crashed:", err);
  }
}

async function updateMyStatus(newStatus) {
  try {
    await sendAcknowledgement(incidentId, newStatus);
    alert("Status updated!");
    location.reload();
  } catch (err) {
    console.error(err);
  }
}

async function sendAcknowledgement(id, status) {
  const response = await fetch(
    `http://localhost:8080/api/incidents/acknowledge/${id}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ status: status }),
    },
  );
  if (!response.ok) throw new Error("Failed update");
  return true;
}

// --- 4. Utility Functions ---
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = deg2rad(lat2 - lat1);
  const dLon = deg2rad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(deg2rad(lat1)) *
      Math.cos(deg2rad(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return (R * c).toFixed(2);
}

function deg2rad(deg) {
  return deg * (Math.PI / 180);
}

function goToAidDistribution() {
  window.location.href = `Distribution.html?incidentId=${incidentId}`;
}
function goToFeedback() {
  window.location.href = `feedback.html?incidentId=${incidentId}`;
}
function navigate() {
  window.open(
    `https://www.google.com/maps?q=${window.incidentLat},${window.incidentLng}`,
    "_blank",
  );
}
