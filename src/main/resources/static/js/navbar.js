document.addEventListener("DOMContentLoaded", () => {
  const nav = document.getElementById("main-nav");

  const userRole = localStorage.getItem("role");

  const links = [
    {
      name: "Report Incident",
      href: "report-incident.html",
      roles: ["ROLE_REGIONAL_STAFF"],
    },

    {
      name: "View Incident",
      href: "view-incident.html",
      roles: ["ROLE_REGIONAL_ADMIN", "ROLE_SUPER_ADMIN"],
    },
    {
      name: "account-managment  ",
      href: "Account.html",
      roles: [
        "ROLE_REGIONAL_STAFF",
        "ROLE_REGIONAL_ADMIN",
        "ROLE_VOLUNTEER",
        "ROLE_POLICE",
        "ROLE_SUPER_ADMIN",
      ],
    },
    {
      name: "Manage-Inventory",
      href: "Central-Inventory.html",
      roles: ["ROLE_SUPER_ADMIN"],
    },
    {
      name: "view-responders",
      href: "response-dashboard.html",
      roles: ["ROLE_REGIONAL_ADMIN"],
    },
    {
      name: "Manage-Inventory",
      href: "Regional-Inventory.html",
      roles: ["ROLE_REGIONAL_ADMIN"],
    },

    {
      name: "Manage Users",
      href: "manage-users.html",
      roles: ["ROLE_SUPER_ADMIN", "ROLE_REGIONAL_ADMIN"],
    },
    {
      name: "Analytics",
      href: "Analytics.html",
      roles: ["ROLE_SUPER_ADMIN", "ROLE_REGIONAL_ADMIN"],
    },
    {
      name: "Feed-Back",
      href: "FeedBack.html",
      roles: ["ROLE_REGIONAL_ADMIN"],
    },
  ];

  const filteredLinks = links.filter(
    (link) => link.roles.includes("ANY") || link.roles.includes(userRole),
  );

  const notificationHtml =
    userRole === "ROLE_SUPER_ADMIN"
      ? ""
      : `
        <li style="position: relative; margin-left: auto; margin-right: 20px;">
            <button id="notif-bell" onclick="toggleNotifDropdown()" style="background: none; border: none; cursor: pointer; font-size: 20px; position: relative;">
                🔔 <span id="notif-count" style="background: red; color: white; border-radius: 50%; padding: 2px 6px; font-size: 10px; position: absolute; top: -5px; right: -5px; display: none;">0</span>
            </button>
            <ul id="notif-list" style="display: none; position: absolute; right: 0; top: 35px; background: white; border: 1px solid #ccc; width: 280px; list-style: none; padding: 0; box-shadow: 0 4px 12px rgba(0,0,0,0.15); z-index: 1000; color: black; max-height: 300px; overflow-y: auto;">
                <li style="padding: 10px; text-align: center;">No new notifications</li>
            </ul>
        </li>
    `;

  if (nav) {
    nav.innerHTML = `
    <ul style="display: flex; list-style: none; gap: 20px; align-items: center; background: #013237; padding: 15px; margin: 0;">
      <li>
        <a href="home.html">
          <img src="../img/logo.png" alt="Home" style="width: 35px; height: 35px; margin-right: 2px;"> 
        </a>
      </li>

      ${filteredLinks
        .map(
          (link) => `
        <li><a href="${link.href}" style="color: white; text-decoration: none;">${link.name}</a></li>
      `,
        )
        .join("")}

      ${notificationHtml}

      <li>
        <a href="index.html" id="logout-btn" onclick="localStorage.clear()" style="color: white; text-decoration: none; font-weight: bold;">Logout</a>
      </li>
    </ul>
  `;
  }
  {
  }
});

fetchNotifications();

setInterval(fetchNotifications, 30000);

window.addEventListener("click", (e) => {
  const list = document.getElementById("notif-list");
  const bell = document.getElementById("notif-bell");
  if (list && !bell.contains(e.target) && !list.contains(e.target)) {
    list.style.display = "none";
  }
});

async function fetchNotifications() {
  const token = localStorage.getItem("token");
  if (!token) return;

  const countElement = document.getElementById("notif-count");
  const listElement = document.getElementById("notif-list");

  try {
    const res = await fetch("http://localhost:8080/api/notifications/unread", {
      headers: { Authorization: "Bearer " + token },
    });

    if (!res.ok) return;

    const notifications = await res.json();

    if (countElement) {
      countElement.innerText = notifications.length;
      countElement.style.display =
        notifications.length > 0 ? "inline-block" : "none";
    }

    if (listElement) {
      if (notifications.length === 0) {
        listElement.innerHTML =
          '<li style="padding:15px; text-align:center; color: #666;">No new alerts</li>';
      } else {
        listElement.innerHTML = notifications
          .map((n) => {
            const isUrgent =
              n.message.includes("🚨") ||
              n.message.includes("CRITICAL") ||
              n.message.includes("HIGH");
            const bgColor = isUrgent ? "#fff5f5" : "white";
            const borderLeft = isUrgent
              ? "4px solid #d9534f"
              : "1px solid #eee";

            return `
              <li id="notif-${n.notificationId}"style="border-bottom: 1px solid #eee; padding: 12px; opacity: 0.9; background-color: ${bgColor}; border-left: ${borderLeft};">
                 <a href="#" onclick="handleNotificationClick(${n.notificationId}, ${n.referenceId})" style="text-decoration: none; color: #333; display: block;">

                      <div style="font-weight: ${isUrgent ? "bold" : "normal"}; font-size: 13px; margin-bottom: 3px;">
                        ${n.message}
                      </div>
                      <div style="font-size: 11px; color: #888;">
                        ${new Date(n.createdAt).toLocaleString()}
                      </div>
                  </a>
              </li>
            `;
          })
          .join("");
      }
    }
  } catch (err) {
    console.error("Error fetching notifications:", err);
  }
}

async function markAsRead(id) {
  await fetch(`http://localhost:8080/api/notifications/read/${id}`, {
    method: "PUT",
    headers: { Authorization: "Bearer " + localStorage.getItem("token") },
  });
}

function toggleNotifDropdown() {
  const list = document.getElementById("notif-list");
  if (!list) return;
  list.style.display =
    list.style.display === "none" || list.style.display === ""
      ? "block"
      : "none";
}

window.addEventListener("click", (e) => {
  const list = document.getElementById("notif-list");
  const bell = document.getElementById("notif-bell");
  if (list && !bell.contains(e.target) && !list.contains(e.target)) {
    list.style.display = "none";
  }
});

function openIncident(incidentId, notificationId) {
  markAsRead(notificationId);

  sendAcknowledgement(incidentId, "VIEWED");
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "/html/login.html";
    return;
  }
  window.location.href = "incidentdetail.html?id=" + incidentId;
}
async function sendAcknowledgement(incidentId, status) {
  const token = localStorage.getItem("token");
  try {
    await fetch(
      `http://localhost:8080/api/incidents/acknowledge/${incidentId}`,
      {
        method: "POST",
        headers: {
          Authorization: "Bearer " + token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ status: status }),
      },
    );
    console.log(`Acknowledgement sent: ${status}`);
  } catch (err) {
    console.error("Failed to update acknowledgement status", err);
  }
}
function handleNotificationClick(notificationId, incidentId) {
  const role = localStorage.getItem("role");

  // Always mark as read
  markAsRead(notificationId);

  // REGIONAL ADMIN → just dismiss
  if (role === "ROLE_REGIONAL_ADMIN") {
    removeNotificationFromUI(notificationId);
    return;
  }

  // Others → normal behavior
  window.location.href = "incidentdetail.html?id=" + incidentId;
}
function removeNotificationFromUI(notificationId) {
  const item = document.getElementById("notif-" + notificationId);
  if (item) item.remove();
}
