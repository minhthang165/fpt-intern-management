function getIdsFromPath() {
    let pathSegments = window.location.pathname.split('/');
    let userId = pathSegments[pathSegments.length - 1];
    return { userId };
}
const { userId } = getIdsFromPath();
localStorage.setItem("userId", userId);
console.log("✅ User ID:", userId);

function fetchUsers() {
    fetch('/api/user/users')
        .then(response => response.json())
        .then(users => {
            const userListDiv = document.getElementById("users-list");
            userListDiv.innerHTML = "";
            users.forEach(user => {
                const userDiv = document.createElement("div");
                userDiv.className = "user-item";
                userDiv.innerText = `User ${user.id}: ${user.first_name}`;
                userDiv.onclick = () => startCall(user.id);
                userListDiv.appendChild(userDiv);
            });
        })
        .catch(error => console.error("❌ Error fetching users:", error));
}

function startCall(receiverId) {
    if (!receiverId) {
        alert("Invalid Receiver ID!");
        return;
    }

    let roomID = Math.floor(Math.random() * 10000) + "";
    localStorage.setItem("roomID", roomID);

    fetch(`/calls/create/${userId}/${receiverId}/${roomID}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
    })
        .then(response => response.json())
        .then(data => {
            if (data.roomID) {
                window.location.href = `/video-call.html?room=${data.roomID}`;
            } else {
                alert("Receiver is busy or unavailable!");
            }
        })
        .catch(error => console.error("Error starting call:", error));
}
window.onload = fetchUsers;