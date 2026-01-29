document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  try {
    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    if (response.ok) {
      const data = await response.json();

      localStorage.clear();
      localStorage.setItem("token", data.token);
      localStorage.setItem("role", data.role);
      localStorage.setItem("username", username);
      const idToStore = data.userId || data.id;
      localStorage.setItem("userId", idToStore);

      console.log("Saved User ID:", idToStore);
      window.location.href = "home.html";
    } else {
      alert("Invalid credentials. Access denied.");
    }
  } catch (error) {
    console.error("Connection error:", error);
    alert("Could not connect to the authentication server.");
  }
});
