document.addEventListener("DOMContentLoaded", function () {
    let userId = document.getElementById("userId").value;
    let stompClient = null;

    function connectWebSocket() {
        let socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            console.log("✅ WebSocket connected!");

            stompClient.subscribe(`/topic/call/${userId}`, function (message) {
                let data = JSON.parse(message.body);
                window.location.href = `/video-call.html?room=${data.roomID}`;
            });
        });
    }

    connectWebSocket();
});