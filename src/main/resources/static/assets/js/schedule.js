// ============ CHỨC NĂNG CƠ BẢN CHO LỊCH ============

// Mảng lưu trữ events
let events = [];

// Kiểm tra và lấy events từ localStorage
function loadEventsFromStorage() {
    const storedEvents = localStorage.getItem('calendarEvents');
    if (storedEvents) {
        events = JSON.parse(storedEvents);
        renderEvents();
    }
}

// Lưu events vào localStorage
function saveEventsToStorage() {
    localStorage.setItem('calendarEvents', JSON.stringify(events));
}

// Tạo ID duy nhất cho event
function generateId() {
    return Date.now().toString() + Math.floor(Math.random() * 1000);
}

// Thêm event
function addEvent(title, startDate, startHour, endDate, endHour, color = '#4f46e5') {
    const newEvent = {
        id: generateId(),
        title: title,
        startDate: startDate,
        startHour: startHour,
        endDate: endDate || startDate,
        endHour: endHour || (parseInt(startHour) + 1).toString(),
        color: color
    };

    events.push(newEvent);
    saveEventsToStorage();
    renderEvents();
}

// Xóa event
function deleteEvent(eventId) {
    events = events.filter(event => event.id !== eventId);
    saveEventsToStorage();
    renderEvents();
}

// Hiển thị tất cả events lên lịch
function renderEvents() {
    // Xóa tất cả events hiện tại trên UI
    const eventElements = document.querySelectorAll('.calendar-event');
    eventElements.forEach(el => el.remove());

    // Lấy ngày hiện tại trên lịch
    const currentDates = [];
    document.querySelectorAll('.calendar-header-cell .day-number').forEach((el, index) => {
        if (index > 0) { // Bỏ qua ô đầu tiên (trống)
            currentDates.push(parseInt(el.textContent.trim()));
        }
    });

    const currentMonth = document.getElementById('current-month').textContent;
    const monthYear = currentMonth.split(' ');
    const month = getMonthNumber(monthYear[0]);
    const year = parseInt(monthYear[1]);

    // Hiển thị events
    events.forEach(event => {
        const eventDate = new Date(event.startDate);
        const eventDay = eventDate.getDate();

        // Kiểm tra xem event có trong tuần hiện tại không
        if (currentDates.includes(eventDay)) {
            const dayIndex = currentDates.indexOf(eventDay) + 1;
            const hourIndex = parseInt(event.startHour);

            // Tìm cell tương ứng
            const rows = document.querySelectorAll('.calendar-body');
            if (rows.length > 0) {
                const timeRow = rows[0].children[hourIndex * 6]; // Mỗi hàng có 6 ô (1 cho giờ, 5 cho các ngày)
                if (timeRow) {
                    const dayCell = rows[0].children[hourIndex * 6 + dayIndex];

                    if (dayCell) {
                        // Tạo event element
                        const eventEl = document.createElement('div');
                        eventEl.className = 'calendar-event';
                        eventEl.style.position = 'absolute';
                        eventEl.style.inset = '0.25rem';
                        eventEl.style.backgroundColor = event.color + '80'; // Thêm độ trong suốt
                        eventEl.style.borderLeft = `3px solid ${event.color}`;
                        eventEl.style.borderRadius = '0.125rem';
                        eventEl.style.zIndex = '10';
                        eventEl.style.cursor = 'pointer';
                        eventEl.setAttribute('data-event-id', event.id);

                        // Thêm nội dung cho event
                        eventEl.innerHTML = `
                                    <div style="padding: 0.25rem; font-size: 0.75rem; font-weight: 500; color: white;">${event.title}</div>
                                `;

                        // Thêm event vào cell
                        dayCell.style.position = 'relative';
                        dayCell.appendChild(eventEl);

                        // Thêm event listener cho event
                        eventEl.addEventListener('click', function() {
                            showEventDetails(event);
                        });
                    }
                }
            }
        }
    });
}

// Hiển thị chi tiết event khi click
function showEventDetails(event) {
    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    modal.innerHTML = `
                <div class="bg-white rounded-md p-4 max-w-md w-full">
                    <h3 class="text-lg font-medium mb-2">${event.title}</h3>
                    <p class="text-sm text-gray-500 mb-4">
                        ${formatDate(event.startDate)} ${event.startHour}:00 - ${event.endHour}:00
                    </p>
                    <div class="flex justify-end gap-2">
                        <button class="btn btn-outline delete-event">Delete</button>
                        <button class="btn btn-primary close-modal">Close</button>
                    </div>
                </div>
            `;

    document.body.appendChild(modal);

    // Xử lý đóng modal
    modal.querySelector('.close-modal').addEventListener('click', function() {
        modal.remove();
    });

    // Xử lý xóa event
    modal.querySelector('.delete-event').addEventListener('click', function() {
        deleteEvent(event.id);
        modal.remove();
    });

    // Đóng modal khi click bên ngoài
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.remove();
        }
    });
}

// Định dạng ngày
function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
}

// Lấy số tháng từ tên tháng
function getMonthNumber(monthName) {
    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months.indexOf(monthName);
}

// Hàm cập nhật các ngày trong tuần
function updateWeekDays(startDate) {
    if (!startDate || isNaN(startDate.getTime())) {
        // Nếu ngày không hợp lệ, sử dụng ngày hiện tại
        startDate = new Date();
        const dayOfWeek = startDate.getDay() || 7;
        startDate.setDate(startDate.getDate() - dayOfWeek + 1);
    }

    try {
        const dayElements = document.querySelectorAll('.calendar-header-cell:nth-child(n+2):nth-child(-n+6) .day-number');
        const dayNameElements = document.querySelectorAll('.calendar-header-cell:nth-child(n+2):nth-child(-n+6) .day-name');
        const dayHeaderCells = document.querySelectorAll('.calendar-header-cell:nth-child(n+2):nth-child(-n+6)');

        // Lấy ngày hôm nay theo localDate
        const today = new Date();

        // Ngày bắt đầu là thứ Hai
        for (let i = 0; i < Math.min(5, dayElements.length); i++) {
            const date = new Date(startDate);
            date.setDate(date.getDate() + i);

            // Cập nhật số ngày
            if (dayElements[i]) {
                dayElements[i].textContent = date.getDate().toString();
            }

            // Lấy tên ngày trong tuần
            const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

            // Cập nhật tên ngày
            if (dayNameElements[i]) {
                dayNameElements[i].textContent = dayNames[date.getDay()];
            }

            // Xóa class active-day khỏi tất cả cells
            if (dayHeaderCells[i]) {
                dayHeaderCells[i].classList.remove('active-day');

                // Kiểm tra xem có phải là ngày hôm nay không
                const isToday = date.getDate() === today.getDate() &&
                    date.getMonth() === today.getMonth() &&
                    date.getFullYear() === today.getFullYear();

                // Nếu là ngày hôm nay, thêm class active-day
                if (isToday) {
                    dayHeaderCells[i].classList.add('active-day');
                }
            }
        }
    } catch (error) {
        console.error("Lỗi khi cập nhật ngày trong tuần:", error);
        // Trong trường hợp lỗi, không làm gì để tránh việc hiển thị không đúng
    }
}

// Hàm cập nhật hiển thị tháng (có thể chuyển giao giữa 2 tháng)
function updateMonthDisplay(startDate) {
    if (!startDate || isNaN(startDate.getTime())) {
        // Nếu ngày không hợp lệ, sử dụng ngày hiện tại
        startDate = new Date();
        const dayOfWeek = startDate.getDay() || 7;
        startDate.setDate(startDate.getDate() - dayOfWeek + 1);
    }

    try {
        const monday = new Date(startDate);
        const friday = new Date(startDate);
        friday.setDate(monday.getDate() + 4); // Ngày thứ 6

        const mondayMonth = monday.getMonth();
        const fridayMonth = friday.getMonth();

        // Lấy tên tháng một cách an toàn
        const mondayMonthName = getMonthName(mondayMonth) || 'January';
        const fridayMonthName = getMonthName(fridayMonth) || 'January';

        if (mondayMonth === fridayMonth) {
            // Cùng một tháng
            document.getElementById('current-month').textContent = `${mondayMonthName} ${monday.getFullYear()}`;
        } else {
            // Khác tháng
            const mondayYear = monday.getFullYear();
            const fridayYear = friday.getFullYear();

            if (mondayYear === fridayYear) {
                // Cùng năm, khác tháng
                document.getElementById('current-month').textContent = `${mondayMonthName} - ${fridayMonthName} ${mondayYear}`;
            } else {
                // Khác cả năm và tháng
                document.getElementById('current-month').textContent = `${mondayMonthName} ${mondayYear} - ${fridayMonthName} ${fridayYear}`;
            }
        }
    } catch (error) {
        console.error("Lỗi khi cập nhật hiển thị tháng:", error);
        // Trong trường hợp có lỗi, hiển thị tháng hiện tại
        const now = new Date();
        document.getElementById('current-month').textContent = `${getMonthName(now.getMonth())} ${now.getFullYear()}`;
    }
}

// Lấy tên tháng từ số tháng
function getMonthName(monthNumber) {
    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[monthNumber];
}

// Tạo sự kiện mới
function showNewEventModal() {
    // Lấy ngày hiện tại trên lịch
    const currentMonthText = document.getElementById('current-month').textContent;
    // Xử lý trường hợp có thể có 2 tháng (March 2025 - April 2025)
    const monthYear = currentMonthText.split(' - ')[0].split(' ');
    const month = getMonthNumber(monthYear[0]);
    const year = parseInt(monthYear[1]);

    // Ngày đầu tiên hiển thị trên lịch
    const firstDay = parseInt(document.querySelector('.calendar-header-cell:nth-child(2) .day-number').textContent.trim());

    // Tạo đối tượng ngày
    const startDate = new Date(year, month, firstDay);

    // Tạo HTML cho các ngày trong tuần
    let daysOptions = '';
    for (let i = 0; i < 5; i++) {
        const date = new Date(startDate);
        date.setDate(date.getDate() + i);
        const dayValue = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;

        // Lấy tên ngày trong tuần
        const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        const dayName = dayNames[date.getDay()];
        const dayLabel = `${dayName} (${date.getDate()})`;

        daysOptions += `<option value="${dayValue}">${dayLabel}</option>`;
    }

    // Tạo HTML cho các giờ
    let hoursOptions = '';
    for (let i = 0; i < 24; i++) {
        hoursOptions += `<option value="${i}">${i}:00</option>`;
    }

    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    modal.innerHTML = `
                <div class="bg-white rounded-md p-4 max-w-md w-full">
                    <h3 class="text-lg font-medium mb-4">Create New Meeting</h3>
                    <form id="new-event-form">
                        <div class="mb-4">
                            <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                            <input type="text" id="event-title" class="w-full p-2 border border-gray-300 rounded-md" required>
                        </div>
                        <div class="mb-4">
                            <label class="block text-sm font-medium text-gray-700 mb-1">Day</label>
                            <select id="event-day" class="w-full p-2 border border-gray-300 rounded-md">
                                ${daysOptions}
                            </select>
                        </div>
                        <div class="grid grid-cols-2 gap-4 mb-4">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
                                <select id="event-start" class="w-full p-2 border border-gray-300 rounded-md">
                                    ${hoursOptions}
                                </select>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">End Time</label>
                                <select id="event-end" class="w-full p-2 border border-gray-300 rounded-md">
                                    ${hoursOptions}
                                </select>
                            </div>
                        </div>
                        <div class="mb-4">
                            <label class="block text-sm font-medium text-gray-700 mb-1">Color</label>
                            <select id="event-color" class="w-full p-2 border border-gray-300 rounded-md">
                                <option value="#4f46e5">Indigo</option>
                                <option value="#16a34a">Green</option>
                                <option value="#ea580c">Orange</option>
                                <option value="#dc2626">Red</option>
                                <option value="#2563eb">Blue</option>
                            </select>
                        </div>
                        <div class="flex justify-end gap-2">
                            <button type="button" class="btn btn-outline cancel-modal">Cancel</button>
                            <button type="submit" class="btn btn-primary">Create</button>
                        </div>
                    </form>
                </div>
            `;

    document.body.appendChild(modal);

    // Xử lý đóng modal
    modal.querySelector('.cancel-modal').addEventListener('click', function() {
        modal.remove();
    });

    // Xử lý tạo event mới
    document.getElementById('new-event-form').addEventListener('submit', function(e) {
        e.preventDefault();

        const title = document.getElementById('event-title').value;
        const day = document.getElementById('event-day').value;
        const startHour = document.getElementById('event-start').value;
        const endHour = document.getElementById('event-end').value;
        const color = document.getElementById('event-color').value;

        addEvent(title, day, startHour, day, endHour, color);
        modal.remove();
    });

    // Đóng modal khi click bên ngoài
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.remove();
        }
    });
}

// Khởi tạo chức năng kéo thả để highlight các ô thời gian
function initDragToCreate() {
    let isDragging = false;
    let startCell = null;
    let currentHighlight = [];

    // Thêm class vào các cell để nhận biết
    const cells = document.querySelectorAll('.calendar-body > div');
    cells.forEach(cell => {
        // Bỏ qua các cell hiển thị giờ
        if (!cell.querySelector('.time-label')) {
            cell.classList.add('time-cell');
            cell.setAttribute('draggable', 'false');

            // Thêm thuộc tính để biết ngày và giờ
            const cellIndex = Array.from(cell.parentNode.children).indexOf(cell);
            const rowIndex = Math.floor(cellIndex / 6);
            const colIndex = cellIndex % 6;

            if (colIndex > 0) { // Bỏ qua cột đầu tiên (cột giờ)
                const dayElement = document.querySelectorAll('.calendar-header-cell:nth-child(n+2):nth-child(-n+6) .day-number')[colIndex - 1];
                const day = dayElement.textContent.trim();
                const hour = rowIndex; // Không cần trừ 1 vì giờ đã nằm trong container riêng

                cell.setAttribute('data-day', day);
                cell.setAttribute('data-hour', hour);
            }
        }
    });

    // Thêm các event handler
    document.addEventListener('mousedown', function(e) {
        const cell = e.target.closest('.time-cell');
        if (cell) {
            isDragging = true;
            startCell = cell;

            // Highlight cell
            highlightCells(startCell, startCell);

            // Prevent text selection
            e.preventDefault();
        }
    });

    document.addEventListener('mousemove', function(e) {
        if (!isDragging) return;

        const cell = e.target.closest('.time-cell');
        if (cell) {
            // Highlight từ startCell đến cell hiện tại
            highlightCells(startCell, cell);
        }
    });

    document.addEventListener('mouseup', function(e) {
        if (!isDragging) return;

        const cell = e.target.closest('.time-cell') || startCell;

        // Reset
        isDragging = false;
        startCell = null;

        // Xóa highlight
        clearHighlight();
    });

    // Hàm highlight các cell
    function highlightCells(from, to) {
        // Xóa highlight cũ
        clearHighlight();

        // Đảm bảo from có data-day
        if (!from.getAttribute('data-day')) return;

        const fromDay = from.getAttribute('data-day');
        const fromHour = parseInt(from.getAttribute('data-hour'));

        const toDay = to.getAttribute('data-day') || fromDay;
        const toHour = parseInt(to.getAttribute('data-hour') || fromHour);

        // Highlight các cell
        document.querySelectorAll('.calendar-body > div').forEach(cell => {
            const day = cell.getAttribute('data-day');
            const hour = parseInt(cell.getAttribute('data-hour'));

            if (day && day === fromDay && hour >= fromHour && hour <= toHour) {
                cell.classList.add('highlight');
                currentHighlight.push(cell);
            }
        });
    }

    // Hàm xóa highlight
    function clearHighlight() {
        currentHighlight.forEach(cell => {
            cell.classList.remove('highlight');
        });
        currentHighlight = [];
    }

    // Hàm mở modal nhanh để tạo event
    function openQuickEventModal(day, hour) {
        // Lấy ngày thực tế
        const currentMonth = document.getElementById('current-month').textContent;
        const monthYear = currentMonth.split(' ');
        const month = getMonthNumber(monthYear[0]);
        const year = parseInt(monthYear[1]);

        // Tạo đối tượng ngày
        const date = new Date(year, month, parseInt(day));
        const formattedDate = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;

        // Mở modal
        const modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        modal.innerHTML = `
                    <div class="bg-white rounded-md p-4 max-w-md w-full">
                        <h3 class="text-lg font-medium mb-2">New Meeting</h3>
                        <form id="quick-event-form">
                            <div class="mb-4">
                                <input type="text" id="quick-event-title" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Meeting title" required>
                            </div>
                            <p class="text-sm text-gray-500 mb-4">
                                ${formatDate(formattedDate)} at ${hour}:00
                            </p>
                            <div class="flex justify-end gap-2">
                                <button type="button" class="btn btn-outline cancel-modal">Cancel</button>
                                <button type="submit" class="btn btn-primary">Create</button>
                            </div>
                        </form>
                    </div>
                `;

        document.body.appendChild(modal);

        // Focus vào input
        setTimeout(() => {
            document.getElementById('quick-event-title').focus();
        }, 100);

        // Xử lý đóng modal
        modal.querySelector('.cancel-modal').addEventListener('click', function() {
            modal.remove();
        });

        // Xử lý tạo event nhanh
        document.getElementById('quick-event-form').addEventListener('submit', function(e) {
            e.preventDefault();

            const title = document.getElementById('quick-event-title').value;
            addEvent(title, formattedDate, hour, formattedDate, parseInt(hour) + 1);
            modal.remove();
        });

        // Đóng modal khi click bên ngoài
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });
    }
}

// Thêm CSS cho highlight
function addCustomStyles() {
    const style = document.createElement('style');
    style.textContent = `
                .time-cell {
                    cursor: pointer;
                }
                .time-cell:hover {
                    background-color: rgba(79, 70, 229, 0.1);
                }
                .time-cell.highlight {
                    background-color: rgba(79, 70, 229, 0.2);
                    border: 1px dashed #4f46e5;
                }
                .calendar-event {
                    transition: transform 0.2s, box-shadow 0.2s;
                }
                .calendar-event:hover {
                    transform: scale(1.02);
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                }
                .calendar-body::-webkit-scrollbar {
                    width: 8px;
                }
                .calendar-body::-webkit-scrollbar-track {
                    background: #f1f5f9;
                }
                .calendar-body::-webkit-scrollbar-thumb {
                    background-color: #cbd5e1;
                    border-radius: 4px;
                }
                .calendar-body::-webkit-scrollbar-thumb:hover {
                    background-color: #94a3b8;
                }
            `;
    document.head.appendChild(style);
}

// Khởi tạo các chức năng
document.addEventListener('DOMContentLoaded', function() {
    // Xác định ngày hôm nay theo localDate
    const today = new Date();

    // Xác định ngày đầu tuần (thứ Hai)
    const dayOfWeek = today.getDay() || 7; // 0 là Chủ Nhật, chuyển thành 7
    const monday = new Date(today);
    monday.setDate(today.getDate() - dayOfWeek + 1); // Đặt lại thành thứ Hai

    // Cập nhật hiển thị tháng/năm (có thể chuyển giao giữa 2 tháng)
    updateMonthDisplay(monday);

    // Cập nhật các ngày trong tuần và active-day
    updateWeekDays(monday);

    // Thêm styles
    addCustomStyles();

    // Khởi tạo kéo thả
    initDragToCreate();

    // Load events từ localStorage
    loadEventsFromStorage();

    // Xử lý nút Today
    document.querySelector('.btn-today').addEventListener('click', function() {
        navigateToDate('today');
    });

    // Xử lý nút Previous
    document.getElementById('prev-month').addEventListener('click', function() {
        navigateToDate('prev');
    });

    // Xử lý nút Next
    document.getElementById('next-month').addEventListener('click', function() {
        navigateToDate('next');
    });

    // Xử lý nút New meeting
    document.querySelector('.btn-primary').addEventListener('click', function() {
        showNewEventModal();
    });

    // Cuộn lịch đến giờ làm việc (9AM)
    setTimeout(() => {
        const scrollContainer = document.querySelector('.calendar-body');
        if (scrollContainer) {
            // Tính toán vị trí cuộn dựa trên chiều cao của mỗi ô giờ
            const hourHeight = 60; // Chiều cao mỗi ô giờ
            scrollContainer.scrollTop = 9 * hourHeight; // Cuộn đến 9AM
        }
    }, 300); // Thời gian dài hơn để đảm bảo tất cả đã render

    // Khai báo biến để lưu trữ instance của setInterval
    let dateUpdateInterval;
    let calendarUpdateInterval;

    // Hàm cập nhật liên tục thời gian và ngày hiện tại
    function startContinuousUpdate() {
        // Cập nhật ngay lập tức
        updateCurrentDateTime();
        updateTimeIndicator();

        // Cập nhật mỗi giây
        dateUpdateInterval = setInterval(() => {
            updateCurrentDateTime();
            updateTimeIndicator();
        }, 1000);

        // Cập nhật lịch mỗi phút
        calendarUpdateInterval = setInterval(updateCalendar, 60000);
    }

    // Hàm cập nhật thời gian và ngày hiện tại
    function updateCurrentDateTime() {
        try {
            const now = new Date();

            // Kiểm tra xem có phải là tuần hiện tại không
            const currentFirstDayElement = document.querySelector('.calendar-header-cell:nth-child(2) .day-number');
            if (!currentFirstDayElement) return;

            const currentFirstDay = currentFirstDayElement.textContent;
            if (!currentFirstDay || isNaN(parseInt(currentFirstDay))) return;

            // Xác định ngày thứ Hai của tuần hiện tại
            const thisMonday = new Date(now);
            const thisDayOfWeek = thisMonday.getDay() || 7;
            thisMonday.setDate(thisMonday.getDate() - thisDayOfWeek + 1);

            // Kiểm tra xem có phải đang hiển thị tuần hiện tại không
            if (parseInt(currentFirstDay) === thisMonday.getDate()) {
                // Nếu đang hiển thị tuần hiện tại, cập nhật highlight ngày hiện tại
                updateWeekDays(thisMonday);
            }
        } catch (error) {
            console.error("Lỗi khi cập nhật thời gian hiện tại:", error);
        }
    }

    // Hàm cập nhật toàn bộ lịch
    function updateCalendar() {
        const now = new Date();

        // Nếu đổi ngày (ví dụ 23:59 -> 00:00), cần cập nhật lại hiển thị
        if (now.getDate() !== today.getDate() ||
            now.getMonth() !== today.getMonth() ||
            now.getFullYear() !== today.getFullYear()) {
            navigateToDate('today');

            // Cập nhật biến today để so sánh lần sau
            today.setDate(now.getDate());
            today.setMonth(now.getMonth());
            today.setFullYear(now.getFullYear());
        }
    }

    // Hàm cập nhật time indicator
    function updateTimeIndicator() {
        try {
            const now = new Date();
            const hours = now.getHours();
            const minutes = now.getMinutes();

            // Kiểm tra xem ngày hôm nay có trong tuần hiện tại không
            const dayElements = document.querySelectorAll('.calendar-header-cell:nth-child(n+2):nth-child(-n+6) .day-number');
            let todayColumnIndex = -1;

            // Tìm cột của ngày hôm nay
            dayElements.forEach((el, index) => {
                const dayNumber = parseInt(el.textContent);
                if (dayNumber === now.getDate()) {
                    const headerCell = el.closest('.calendar-header-cell');
                    if (headerCell.classList.contains('active-day')) {
                        todayColumnIndex = index + 1; // +1 vì cột đầu tiên là cột giờ
                    }
                }
            });

            // Xóa time indicator cũ nếu có
            const oldIndicator = document.querySelector('.current-time-indicator');
            if (oldIndicator) {
                oldIndicator.remove();
            }

            // Chỉ hiển thị time indicator nếu ngày hôm nay nằm trong tuần hiện tại
            if (todayColumnIndex !== -1) {
                // Tính toán vị trí của time indicator
                const hourHeight = 60; // Chiều cao của mỗi ô giờ
                const minuteHeight = (minutes / 60) * hourHeight;
                const topPosition = (hours * hourHeight) + minuteHeight;

                // Tạo time indicator mới
                const indicator = document.createElement('div');
                indicator.className = 'current-time-indicator';
                indicator.style.top = `${topPosition}px`;

                // Tính toán left position dựa vào cột của ngày hôm nay
                const columnWidth = `calc((100% - 60px) / 5)`; // Trừ đi cột giờ và chia cho 5 ngày
                const leftPosition = `calc(60px + ${columnWidth} * ${todayColumnIndex - 1})`;
                const rightPosition = `calc(100% - 60px - ${columnWidth} * ${todayColumnIndex})`;

                indicator.style.left = leftPosition;
                indicator.style.width = columnWidth;

                // Tạo label hiển thị giờ
                const timeLabel = document.createElement('div');
                timeLabel.className = 'current-time-label';
                timeLabel.textContent = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;

                // Thêm vào calendar body
                const calendarBody = document.querySelector('.calendar-body');
                if (calendarBody) {
                    calendarBody.style.position = 'relative';
                    calendarBody.appendChild(indicator);
                    indicator.appendChild(timeLabel);
                }
            }
        } catch (error) {
            console.error("Lỗi khi cập nhật time indicator:", error);
        }
    }

    // Bắt đầu cập nhật liên tục
    startContinuousUpdate();

    // Xử lý sự kiện khi tab/cửa sổ được focus trở lại
    document.addEventListener('visibilitychange', function() {
        if (document.visibilityState === 'visible') {
            // Khi tab được mở lại, cập nhật ngay lập tức
            updateCalendar();
            updateCurrentDateTime();

            // Đảm bảo các intervals vẫn đang chạy
            if (!dateUpdateInterval) {
                startContinuousUpdate();
            }
        } else {
            // Khi tab bị ẩn, dừng cập nhật để tiết kiệm tài nguyên
            clearInterval(dateUpdateInterval);
            clearInterval(calendarUpdateInterval);
            dateUpdateInterval = null;
            calendarUpdateInterval = null;
        }
    });

    // Xử lý khi người dùng trở lại từ chế độ ngủ/hibernate
    window.addEventListener('online', function() {
        // Khi máy tính vừa kết nối lại, cập nhật ngay lập tức
        updateCalendar();
        updateCurrentDateTime();

        // Đảm bảo các intervals vẫn đang chạy
        if (!dateUpdateInterval) {
            startContinuousUpdate();
        }
    });
});

// Chuyển đổi giữa các tuần
function navigateToDate(direction) {
    try {
        const currentMonth = document.getElementById('current-month').textContent;
        let date;

        // Xử lý chuyển đổi giữa các tuần
        try {
            // Lấy ngày cuối cùng hiển thị trên lịch để xử lý chuyển tháng chính xác hơn
            const lastDayElement = document.querySelector('.calendar-header-cell:nth-child(6) .day-number');
            const firstDayElement = document.querySelector('.calendar-header-cell:nth-child(2) .day-number');

            if (firstDayElement && lastDayElement) {
                const monthParts = currentMonth.split(' - ');
                let month, year;

                if (direction === 'next') {
                    // Sử dụng tháng của ngày cuối cùng khi đi tới
                    const lastMonthYear = monthParts[monthParts.length - 1].split(' ');
                    month = getMonthNumber(lastMonthYear[0]);
                    year = parseInt(lastMonthYear[lastMonthYear.length - 1]);
                    const lastDay = parseInt(lastDayElement.textContent.trim());
                    date = new Date(year, month, lastDay);
                    date.setDate(date.getDate() + 3); // Thêm 3 ngày để đảm bảo sang tuần mới
                } else if (direction === 'prev') {
                    // Sử dụng tháng của ngày đầu tiên khi đi lui
                    const firstMonthYear = monthParts[0].split(' ');
                    month = getMonthNumber(firstMonthYear[0]);
                    year = parseInt(firstMonthYear[1]);
                    const firstDay = parseInt(firstDayElement.textContent.trim());
                    date = new Date(year, month, firstDay);
                    date.setDate(date.getDate() - 3); // Trừ 3 ngày để đảm bảo lùi về tuần trước
                } else if (direction === 'today') {
                    date = new Date();
                }

                if (date) {
                    // Điều chỉnh về đầu tuần (thứ Hai)
                    const dayOfWeek = date.getDay() || 7;
                    date.setDate(date.getDate() - dayOfWeek + 1);
                }
            }
        } catch (error) {
            console.error("Lỗi khi phân tích ngày:", error);
            // Sử dụng ngày hiện tại nếu có lỗi
            date = new Date();
            const dayOfWeek = date.getDay() || 7;
            date.setDate(date.getDate() - dayOfWeek + 1);
        }

        if (!date) {
            date = new Date();
            const dayOfWeek = date.getDay() || 7;
            date.setDate(date.getDate() - dayOfWeek + 1);
        }

        // Cập nhật tiêu đề tháng/năm và các ngày trong tuần
        updateMonthDisplay(date);
        updateWeekDays(date);
        renderEvents();

    } catch (error) {
        console.error("Lỗi khi chuyển đổi giữa các tuần:", error);
        // Trong trường hợp lỗi nghiêm trọng, cập nhật về ngày hiện tại
        const today = new Date();
        const dayOfWeek = today.getDay() || 7;
        const monday = new Date(today);
        monday.setDate(today.getDate() - dayOfWeek + 1);

        updateMonthDisplay(monday);
        updateWeekDays(monday);
        renderEvents();
    }
}

// Thêm hàm tạo month picker
function showMonthPicker() {
    // Xóa month picker cũ nếu có
    const oldPicker = document.querySelector('.month-picker');
    if (oldPicker) oldPicker.remove();

    // Lấy thông tin tháng hiện tại
    const currentMonthText = document.getElementById('current-month').textContent;
    const monthYear = currentMonthText.split(' - ')[0].split(' ');
    const currentMonth = getMonthNumber(monthYear[0]);
    const currentYear = parseInt(monthYear[1]);

    // Tạo month picker container
    const picker = document.createElement('div');
    picker.className = 'month-picker';

    // Thêm header với nút điều hướng và tháng/năm
    const header = document.createElement('div');
    header.className = 'month-picker-nav';
    header.innerHTML = `
                <button class="month-nav-btn" id="prev-picker-month">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="15 18 9 12 15 6"></polyline>
                    </svg>
                </button>
                <span class="month-picker-title">${getMonthName(currentMonth)} ${currentYear}</span>
                <button class="month-nav-btn" id="next-picker-month">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="9 18 15 12 9 6"></polyline>
                    </svg>
                </button>
            `;
    picker.appendChild(header);

    // Thêm header cho các ngày trong tuần
    const weekDays = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
    const daysHeader = document.createElement('div');
    daysHeader.className = 'month-picker-header';
    weekDays.forEach(day => {
        const dayEl = document.createElement('div');
        dayEl.textContent = day;
        daysHeader.appendChild(dayEl);
    });
    picker.appendChild(daysHeader);

    // Tạo grid cho các ngày
    const grid = document.createElement('div');
    grid.className = 'month-picker-grid';

    function renderMonth(year, month) {
        grid.innerHTML = '';
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const startingDay = firstDay.getDay();
        const totalDays = lastDay.getDate();

        // Lấy ngày hiện tại theo LocalDate
        const today = new Date();
        const isCurrentMonth = month === today.getMonth() && year === today.getFullYear();

        // Thêm ngày từ tháng trước
        const prevMonthLastDay = new Date(year, month, 0).getDate();
        for (let i = startingDay - 1; i >= 0; i--) {
            const dayEl = document.createElement('div');
            dayEl.className = 'month-picker-cell other-month';
            dayEl.textContent = prevMonthLastDay - i;
            grid.appendChild(dayEl);
        }

        // Thêm ngày của tháng hiện tại
        for (let i = 1; i <= totalDays; i++) {
            const dayEl = document.createElement('div');
            dayEl.className = 'month-picker-cell';
            dayEl.textContent = i;

            // Chỉ thêm chấm tròn cho ngày hôm nay nếu đang ở tháng hiện tại
            if (isCurrentMonth && i === today.getDate()) {
                dayEl.classList.add('today');
            }

            // Thêm sự kiện click để chọn ngày
            dayEl.addEventListener('click', () => {
                const selectedDate = new Date(year, month, i);
                const dayOfWeek = selectedDate.getDay() || 7;
                const monday = new Date(selectedDate);
                monday.setDate(selectedDate.getDate() - dayOfWeek + 1);

                updateMonthDisplay(monday);
                updateWeekDays(monday);
                renderEvents();
                picker.remove();
            });

            grid.appendChild(dayEl);
        }

        // Thêm ngày từ tháng sau
        const remainingDays = 42 - (startingDay + totalDays);
        for (let i = 1; i <= remainingDays; i++) {
            const dayEl = document.createElement('div');
            dayEl.className = 'month-picker-cell other-month';
            dayEl.textContent = i;
            grid.appendChild(dayEl);
        }

        // Cập nhật tiêu đề
        header.querySelector('.month-picker-title').textContent =
            `${getMonthName(month)} ${year}`;
    }

    picker.appendChild(grid);
    renderMonth(currentYear, currentMonth);

    // Thêm xử lý sự kiện cho nút điều hướng
    header.querySelector('#prev-picker-month').addEventListener('click', (e) => {
        e.stopPropagation();
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        renderMonth(currentYear, currentMonth);
    });

    header.querySelector('#next-picker-month').addEventListener('click', (e) => {
        e.stopPropagation();
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        renderMonth(currentYear, currentMonth);
    });

    // Định vị month picker
    const monthElement = document.getElementById('current-month');
    const rect = monthElement.getBoundingClientRect();
    picker.style.top = `${rect.bottom + window.scrollY + 5}px`;
    picker.style.left = `${rect.left + window.scrollX}px`;

    // Thêm vào document
    document.body.appendChild(picker);

    // Thêm event listener để đóng month picker
    document.addEventListener('click', function closeMonthPicker(e) {
        if (!picker.contains(e.target) && e.target !== monthElement) {
            picker.remove();
            document.removeEventListener('click', closeMonthPicker);
        }
    });
}

// Thêm event listener cho current-month
document.getElementById('current-month').addEventListener('click', showMonthPicker);
