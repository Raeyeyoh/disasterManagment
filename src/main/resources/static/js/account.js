document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("display-username").innerText =
    localStorage.getItem("username");
  document.getElementById("display-role").innerText =
    localStorage.getItem("role");
});

document
  .getElementById("changePasswordForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (newPassword !== confirmPassword) {
      alert("New passwords do not match!");
      return;
    }

    const response = await fetch(
      "http://localhost:8080/api/auth/change-password",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ currentPassword, newPassword }),
      },
    );

    if (response.ok) {
      alert("Password updated! Please log in again.");
      localStorage.clear();
      window.location.href = "index.html";
    } else {
      const error = await response.text();
      alert("Error: " + error);
    }
  });
