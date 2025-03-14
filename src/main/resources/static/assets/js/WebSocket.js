document.addEventListener("DOMContentLoaded", function () {
    const userId = document.getElementById("userId").value;
    console.log("User ID:", userId);

    let stompClient = null;

    function connectWebSocket() {
        let socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            console.log("✅ WebSocket connected!");

            stompClient.subscribe(`/topic/call/${userId}`, function (message) {
                let data = JSON.parse(message.body);
                showCallNotification(data.roomID, data.callerId);
            });
        }, function (error) {
            console.error("❌ WebSocket connection error:", error);
        });
    }

    function showCallNotification(roomID, callerID) {
        fetch(`/api/user/users/id/${callerID}`)
            .then(response => response.json())
            .then(user => {
                let callerName = user.first_name || "Unknown";
                displayCallPopup(roomID, callerID, callerName);
            })
            .catch(error => {
                console.error("❌ Fetch error:", error);
                displayCallPopup(roomID, callerID, "Unknown");
            });
    }

    function displayCallPopup(roomID, callerID, callerName) {
        let callDialog = document.createElement("div");
        callDialog.innerHTML = `
            <div id="call-popup" style="position: fixed; top: 20%; left: 50%; transform: translate(-50%, -50%);
                background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
                text-align: center;">
                <p>📞 Incoming Call from <b>${callerName}</b></p>
                <button onclick="window.location.href='/video-call.html?room=${roomID}&user=${userId}'">Accept</button>
                <button onclick="document.getElementById('call-popup').remove()">Reject</button>
            </div>`;
        document.body.appendChild(callDialog);
    }

    connectWebSocket();
});