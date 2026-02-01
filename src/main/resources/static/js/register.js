document
  .getElementById("registerForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const regData = {
      name: document.getElementById("name").value,
      username: document.getElementById("username").value,
      contact: document.getElementById("contact").value,
      password: document.getElementById("password").value,
      roleName: document.getElementById("requestedRole").value,
      location: document.getElementById("location").value,
      regionId: parseInt(document.getElementById("regionId").value),
    };

    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(regData),
      });

      if (response.ok) {
        alert(
          "Registration Successful! Your account is PENDING approval. You will receive an email once approved.",
        );
        window.location.href = "index.html";
      } else {
        const msg = await response.text();
        alert("Registration Failed: " + msg);
      }
    } catch (error) {
      alert("Server Error. Please try again later.");
    }
  });
async function loadRegions() {
  try {
    const response = await fetch("http://localhost:8080/api/regions");
    const regions = await response.json();
    const select = document.getElementById("regionId");

    regions.forEach((reg) => {
      let opt = document.createElement("option");
      opt.value = reg.regionId;
      opt.innerHTML = reg.regionName;
      select.appendChild(opt);
    });
  } catch (error) {
    console.error("Could not load regions", error);
  }
}
//register
window.onload = loadRegions;
function getLocation() {
  if (navigator.geolocation) {
    const btn = event.target;
    const originalText = btn.innerText;
    btn.innerText = "⌛...";

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude.toFixed(6);
        const lng = position.coords.longitude.toFixed(6);

        document.getElementById("location").value = `${lat}, ${lng}`;
        btn.innerText = "✅";
        btn.style.background = "#28a745";
      },
      (error) => {
        btn.innerText = "❌";
        alert("Please enable location permissions in your browser.");
      },
    );
  } else {
    alert("Geolocation is not supported by this browser.");
  }
}
