const role = localStorage.getItem("role"); // get role from token storage
const isSuperAdmin = role === "ROLE_SUPER_ADMIN";
let tableHeader = `
<tr  class="table-head">
    ${isSuperAdmin ? "<th>Region</th>" : ""}
    <th>Role-Name</th>
    <th>Username</th>
    <th>Email (Contact)</th>
    <th>Action</th>
</tr>
`;

document.getElementById("usersTableHead").innerHTML = tableHeader;
async function loadPendingUsers() {
  const response = await fetch("http://localhost:8080/api/users/pending", {
    method: "GET",
    headers: {
      Authorization: "Bearer " + localStorage.getItem("token"),
      "Content-Type": "application/json",
    },
  });
  const users = await response.json();
  const tableBody = document.getElementById("pending-users-table");
  tableBody.innerHTML = "";

  users.forEach((user) => {
    const roleNames =
      user.userRoles && Array.isArray(user.userRoles)
        ? user.userRoles.map((ur) => ur.role.roleName).join(", ")
        : "No Roles Requested";
    tableBody.innerHTML += `
    <tr style="border-bottom: 1px solid #ddd;">
${isSuperAdmin ? `<td style="padding: 12px;">${user.region ? user.region.regionName : "No Region Assigned"}</td>` : ""}
        <td style="padding: 12px;">${roleNames}</td>
        <td style="padding: 12px;">${user.username}</td>
        <td style="padding: 12px;">${user.contact}</td>
        <td style="padding: 12px; text-align: center;">
            <button class="btn-primary" onclick="approveUser(${user.userId})">APPROVE</button>
        </td>
    </tr>
`;
  });
}

async function approveUser(userId) {
  if (!confirm("Approve this user? An activation email will be sent.")) return;

  const response = await fetch(
    `http://localhost:8080/api/users/approve/${userId}`,
    {
      method: "PUT",
      headers: { Authorization: "Bearer " + localStorage.getItem("token") },
    },
  );

  if (response.ok) {
    alert("User Approved and Activated!");
    loadPendingUsers();
  }
}

document.addEventListener("DOMContentLoaded", loadPendingUsers);
