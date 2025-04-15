let stompClient = null;
let notificationCount = 0;
let userId = null;
let notificationBell = null;
let notificationDropdown = null;
let notificationBadge = null;
let notificationList= null;

function connect() {
    userId = document.getElementById("user_id_header").value;
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    // Subscribe to the user's personal notification queue
    stompClient.subscribe('/topic/notifications/' + userId, onNotificationReceived);
    loadNotification(userId);
}

function onError(error) {
    console.error('Error connecting to WebSocket:', error);
    // Try to reconnect after 5 seconds
    setTimeout(connect, 5000);
}

function onNotificationReceived(payload) {
    const notification = JSON.parse(payload.body);
    showNotification(notification);
}

function showNotification(notification) {
    // Create notification element
    const notificationElement = document.createElement('li');
    const notificationList = document.querySelector('.notification-list');
    notificationElement.innerHTML = `
        <div class="notification-content">
             <p>${notification.content}</p>
        </div>
    `;
    notificationList.appendChild(notificationElement);
    updateNotificationBadge(++notificationCount);
}

function updateNotificationBadge(count) {
    const badge = document.querySelector('.notification-badge');
    if (badge) {
        notificationCount = count;
        badge.textContent = count;
        badge.style.display = count > 0 ? 'block' : 'none';
    }
}

async function loadNotification(user_id) {
    try {
        let response = await fetch(`/api/notification/get-notification/${user_id}`);
        if (!response.ok) throw new Error(`HTTP error ${response.status}`);
        const notifications = await response.json();
        updateNotificationBadge(notifications.length);
        const notificationList = document.querySelector('.notification-list');
        notificationList.innerHTML = ''; // Xóa nội dung cũ
        if (notifications && notifications.length > 0) {
            notifications.forEach(notification => {
                const li = document.createElement('li');
                li.innerHTML = `
            <a href="${notification.url}">
            <div class="flex px-4 py-3 hover:bg-gray-50 transition duration-200">
              <img class="h-10 w-10 rounded-full object-cover" src="${notification.actor.avatar_path}" alt="Avatar">
              <div class="ml-3 flex-1">
                <div class="flex items-baseline justify-between">
                  <p class="text-sm font-medium text-gray-900">${notification.notificationType}</p>
                  <p class="text-xs text-gray-500">${new Date (notification.createdAt).toLocaleDateString('en-GB')}</p>
                </div>
                <p class="text-xs text-gray-600 mt-1">${notification.content}</p>
              </div>
            </div>
            </a>
          `;
                notificationList.appendChild(li);
            });
        }
    } catch (error) {
        console.error('Lỗi khi tải thông báo:', error);
        const notificationList = document.querySelector('.notification-list');
        notificationList.innerHTML = '<li>Đã xảy ra lỗi khi tải thông báo</li>';
    }
}

// Thêm kiểm tra DOM trước khi kết nối
document.addEventListener('DOMContentLoaded', function() {
    connect();
    notificationBell = document.querySelector('.notification-bell');
    notificationDropdown = document.querySelector('.notification-dropdown');
    notificationBadge = document.querySelector('.notification-badge');
    notificationList = document.querySelector('.notification-list');
    let notificationCount = 0;

    // Toggle dropdown when clicking the bell
    notificationBell.addEventListener('click', function (e) {
        e.stopPropagation();
        notificationCount = 0;
        updateNotificationBadge(notificationCount);

        notificationDropdown.classList.toggle('show');
        if (notificationDropdown.classList.contains('show')) {
            notificationDropdown.classList.remove('pointer-events-none', 'opacity-0', 'scale-95');
            notificationDropdown.classList.add('opacity-100', 'scale-100');
        } else {
            notificationDropdown.classList.add('pointer-events-none', 'opacity-0', 'scale-95');
            notificationDropdown.classList.remove('opacity-100', 'scale-100');
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function (e) {
        if (!notificationDropdown.contains(e.target) && !notificationBell.contains(e.target)) {
            notificationDropdown.classList.remove('show');
            notificationDropdown.classList.add('pointer-events-none', 'opacity-0', 'scale-95');
            notificationDropdown.classList.remove('opacity-100', 'scale-100');
        }
    });
});

