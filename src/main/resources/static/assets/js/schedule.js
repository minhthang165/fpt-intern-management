// ============ CHỨC NĂNG CƠ BẢN CHO LỊCH ============

// Mảng lưu trữ events
let events = [];

// // Kiểm tra và lấy events từ localStorage
// function loadEventsFromStorage() {
//     const storedEvents = localStorage.getItem('calendarEvents');
//     if (storedEvents) {
//         events = JSON.parse(storedEvents);
//         renderEvents();
//     }
// }
//
// // Lưu events vào localStorage
// function saveEventsToStorage() {
//     localStorage.setItem('calendarEvents', JSON.stringify(events));
// }

// Tạo ID duy nhất cho event
function generateId() {
    return Date.now().toString() + Math.floor(Math.random() * 1000);
}

// Thêm event
function addEvent(title, Date, startHour, endDate, endHour, color = '#4f46e5') {
    const newEvent = {
        id: generateId(),
        title: title,
        startDate: Date, // Sửa key thành startDate để khớp với renderEvents
        startHour: startHour,
        endDate: endDate || Date, // Sử dụng Date nếu endDate không có
        endHour: endHour || (parseInt(startHour) + 1).toString(),
        color: color
    };
    console.log('Event vừa được thêm:', newEvent);
    events.push(newEvent);
    renderEvents(); // Chỉ render, không lưu
}
// Xóa event
function deleteEvent(eventId) {
    events = events.filter(event => event.id !== eventId);
    renderEvents(); // Chỉ render, không lưu
}

// Hàm fetch dữ liệu từ API và thêm vào events
async function fetchAndRenderSchedules() {
    try {
        console.log("Bắt đầu fetchAndRenderSchedules")

        // Xóa events cũ
        events = []
        
        // Lấy userId từ input hidden
        const userId = document.getElementById('userId').value;
        const userRole = document.getElementById('userRole').value;
        console.log(`Đang lấy lịch học cho user: ${userId}, vai trò: ${userRole}`);

        // Endpoint API dựa vào vai trò người dùng
        let endpoint = "/api/scheduling/all";
        
        // Nếu có userId và không phải ADMIN, lọc theo userId
        if (userId && userRole !== 'ADMIN') {
            endpoint = `/api/scheduling/user/${userId}`;
        }
        
        // Gọi API để lấy lịch học
        const response = await fetch(endpoint, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        })

        if (!response.ok) {
            throw new Error(`Failed to fetch schedules: ${response.status} ${response.statusText}`)
        }

        // Lấy dữ liệu từ response
        const schedules = await response.json()

        // Ghi log chi tiết dữ liệu để kiểm tra
        console.log("Dữ liệu từ API (chi tiết):", JSON.stringify(schedules, null, 2))

        // Kiểm tra xem schedules có phải là mảng không
        if (!Array.isArray(schedules)) {
            throw new Error("Dữ liệu trả về không phải là mảng")
        }

        console.log(`Đã nhận ${schedules.length} lịch từ API`)

        // Duyệt qua từng schedule và thêm vào events
        schedules.forEach((schedule, index) => {
            // Kiểm tra xem schedule có các trường cần thiết không
            if (!schedule.room || !schedule.subject || !schedule.classField) {
                console.warn("Schedule thiếu các trường cần thiết:", schedule)
                return // Bỏ qua schedule này
            }

            // Lấy các trường từ dữ liệu API
            const roomName = schedule.room.roomName || `Room ${schedule.room.id}`
            const subjectName = schedule.subject.subjectName || `Subject ${schedule.subject.id}`
            const className = schedule.classField.className || `Class ${schedule.classField.id}`
            
            // Lấy thông tin ngày từ dayOfWeek và startDate
            const dayOfWeek = schedule.dayOfWeek // MONDAY, TUESDAY, etc.
            const startDate = schedule.startDate // YYYY-MM-DD
            
            // Tính toán ngày thực tế dựa trên dayOfWeek và startDate
            let eventDate = startDate;
            
            // Chuyển đổi dayOfWeek thành số (0 = Chủ Nhật, 1 = Thứ 2, etc.)
            const dayMap = {
                "MONDAY": 1,
                "TUESDAY": 2,
                "WEDNESDAY": 3,
                "THURSDAY": 4,
                "FRIDAY": 5,
                "SATURDAY": 6,
                "SUNDAY": 0
            };
            
            // Lấy thời gian từ startTime và endTime
            const startTime = schedule.startTime // Định dạng HH:mm:ss
            const endTime = schedule.endTime // Định dạng HH:mm:ss

            // Tạo title từ tên phòng, tên môn học và tên lớp
            const title = `${className} - ${subjectName} - ${roomName}`

            // Lấy giờ bắt đầu và kết thúc từ startTime và endTime
            let startHour = "0" // Giá trị mặc định
            let endHour = "1" // Giá trị mặc định

            // Kiểm tra và xử lý startTime
            if (startTime && typeof startTime === "string") {
                const startTimeParts = startTime.split(":")
                if (startTimeParts.length >= 1) {
                    startHour = Number.parseInt(startTimeParts[0]).toString()
                }
            } else {
                console.warn("startTime không hợp lệ:", startTime)
            }

            // Kiểm tra và xử lý endTime
            if (endTime && typeof endTime === "string") {
                const endTimeParts = endTime.split(":")
                if (endTimeParts.length >= 1) {
                    endHour = Number.parseInt(endTimeParts[0]).toString()
                }
            } else {
                console.warn("endTime không hợp lệ:", endTime)
            }

            // Chọn màu dựa trên môn học
            let color = "#4f46e5"; // Màu mặc định
            if (subjectName.includes("Japanese")) {
                color = "#EF4444"; // Đỏ
            } else if (subjectName.includes("Korean")) {
                color = "#F97316"; // Cam
            } else if (subjectName.includes("Code")) {
                color = "#3B82F6"; // Xanh
            } else if (subjectName.includes("Advanced")) {
                color = "#8B5CF6"; // Tím
            }

            // Thêm sự kiện vào mảng events
            const newEvent = {
                id: generateId(),
                title: title,
                startDate: eventDate, // Đã đúng định dạng YYYY-MM-DD
                startHour: startHour,
                endDate: eventDate,
                endHour: endHour,
                color: color,
                // Lưu thêm thông tin chi tiết để hiển thị khi click
                details: {
                    className: className,
                    subjectName: subjectName,
                    roomName: roomName,
                    dayOfWeek: dayOfWeek,
                    startTime: startTime,
                    endTime: endTime,
                    location: roomName
                },
            }

            console.log(`Thêm sự kiện #${index}:`, newEvent)
            events.push(newEvent)
        })

        console.log(`Đã thêm ${events.length} sự kiện vào mảng events`)

        // Thêm CSS styles cho events
        addEventStyles()

        // Render events lên lịch
        renderEvents()
    } catch (error) {
        console.error("Error fetching schedules:", error)
    }
}
// Hiển thị tất cả events lên lịch
// Hiển thị tất cả events lên lịch - phiên bản với khối liên tục
function renderEvents() {
    console.log("renderEvents được gọi với", events.length, "sự kiện");

    // Xóa tất cả events hiện tại trên UI
    document.querySelectorAll('.calendar-event').forEach(el => el.remove());

    if (events.length === 0) {
        console.log("Không có sự kiện nào để hiển thị");
        return;
    }

    // Debug: In ra cấu trúc của calendar-body
    const calendarBody = document.querySelector('.calendar-body');
    if (!calendarBody) {
        console.error("Không tìm thấy .calendar-body");
        return;
    }

    // Lấy thông tin về các ngày hiển thị trên lịch
    const headerCells = document.querySelectorAll('.calendar-header-cell');
    if (headerCells.length <= 1) {
        console.error("Không tìm thấy đủ .calendar-header-cell");
        return;
    }

    // Lấy tháng và năm từ tiêu đề
    const currentMonthText = document.getElementById('current-month').textContent;
    console.log("Tháng hiện tại:", currentMonthText);

    // Xử lý trường hợp hiển thị 2 tháng (ví dụ: "March - April 2025")
    let firstMonthName, secondMonthName, year;

    if (currentMonthText.includes(' - ')) {
        // Trường hợp 2 tháng
        const parts = currentMonthText.split(' - ');
        firstMonthName = parts[0].trim();

        // Phần thứ hai có thể là "April 2025"
        const secondParts = parts[1].trim().split(' ');
        secondMonthName = secondParts[0].trim();
        year = parseInt(secondParts[1]);
    } else {
        // Trường hợp 1 tháng
        const parts = currentMonthText.split(' ');
        firstMonthName = parts[0].trim();
        secondMonthName = null;
        year = parseInt(parts[1]);
    }

    if (isNaN(year)) {
        console.error("Không thể phân tích năm từ:", currentMonthText);
        return;
    }

    const firstMonthIndex = getMonthNumber(firstMonthName);
    const secondMonthIndex = secondMonthName ? getMonthNumber(secondMonthName) : -1;

    if (firstMonthIndex === -1) {
        console.error("Tháng không hợp lệ:", firstMonthName);
        return;
    }

    console.log(`Đang hiển thị: Tháng ${firstMonthIndex + 1}${secondMonthIndex !== -1 ? ' và ' + (secondMonthIndex + 1) : ''} năm ${year}`);

    // Tạo mapping giữa ngày và cột, đồng thời lưu thông tin tháng của từng ngày
    const dayToColumnMap = new Map();
    const dayToMonthMap = new Map();

    // Bỏ qua ô đầu tiên vì nó trống
    for (let i = 1; i < headerCells.length; i++) {
        const dayNumber = headerCells[i].querySelector('.day-number');
        if (dayNumber) {
            const day = parseInt(dayNumber.textContent.trim());
            if (!isNaN(day)) {
                // Lưu mapping giữa ngày và cột (index bắt đầu từ 1)
                dayToColumnMap.set(day, i);

                // Xác định tháng của ngày này (dựa vào logic: nếu ngày nhỏ và có tháng thứ 2, thì thuộc tháng thứ 2)
                // Thông thường, ngày cuối tháng > 28, ngày đầu tháng < 7
                if (secondMonthIndex !== -1 && day < 15) {
                    dayToMonthMap.set(day, secondMonthIndex);
                } else {
                    dayToMonthMap.set(day, firstMonthIndex);
                }
            }
        }
    }

    console.log("Mapping ngày -> cột:", Object.fromEntries(dayToColumnMap));
    console.log("Mapping ngày -> tháng:", Object.fromEntries(dayToMonthMap));

    // Hiển thị từng sự kiện
    events.forEach((event, index) => {
        console.log(`Xử lý sự kiện #${index}:`, event);

        try {
            // Phân tích ngày từ startDate (định dạng YYYY-MM-DD)
            const dateParts = event.startDate.split('-');
            if (dateParts.length !== 3) {
                console.error(`Định dạng ngày không hợp lệ cho sự kiện #${index}:`, event.startDate);
                return;
            }

            const eventYear = parseInt(dateParts[0]);
            const eventMonth = parseInt(dateParts[1]) - 1; // Tháng trong JS bắt đầu từ 0
            const eventDay = parseInt(dateParts[2]);

            // Kiểm tra xem năm có khớp không
            if (eventYear !== year) {
                console.log(`Sự kiện #${index} không nằm trong năm hiện tại: ${eventYear} vs ${year}`);
                return;
            }

            // Kiểm tra xem tháng có khớp với một trong hai tháng hiển thị không
            const isInFirstMonth = (eventMonth === firstMonthIndex);
            const isInSecondMonth = (secondMonthIndex !== -1 && eventMonth === secondMonthIndex);

            if (!isInFirstMonth && !isInSecondMonth) {
                console.log(`Sự kiện #${index} không nằm trong tháng hiển thị: ${eventMonth + 1} vs ${firstMonthIndex + 1}${secondMonthIndex !== -1 ? ' hoặc ' + (secondMonthIndex + 1) : ''}`);
                return;
            }

            // Tìm cột tương ứng với ngày
            const column = dayToColumnMap.get(eventDay);
            if (!column) {
                console.log(`Không tìm thấy cột cho ngày ${eventDay} của sự kiện #${index}`);
                return;
            }

            // Kiểm tra xem tháng của ngày trên lịch có khớp với tháng của sự kiện không
            const dayMonth = dayToMonthMap.get(eventDay);
            if (dayMonth !== eventMonth) {
                console.log(`Ngày ${eventDay} trên lịch thuộc tháng ${dayMonth + 1}, nhưng sự kiện thuộc tháng ${eventMonth + 1}`);
                return;
            }

            // Lấy giờ bắt đầu và kết thúc
            const startHour = parseInt(event.startHour);
            const endHour = parseInt(event.endHour);

            if (isNaN(startHour) || startHour < 0 || startHour > 23) {
                console.error(`Giờ bắt đầu không hợp lệ cho sự kiện #${index}:`, event.startHour);
                return;
            }

            if (isNaN(endHour) || endHour < 0 || endHour > 23) {
                console.error(`Giờ kết thúc không hợp lệ cho sự kiện #${index}:`, event.endHour);
                return;
            }

            // Lấy tất cả các ô trong calendar-body
            const cells = Array.from(calendarBody.children);

            // Tính toán vị trí của ô bắt đầu và ô kết thúc
            const startCellIndex = (startHour * 6) + column;
            const endCellIndex = (endHour * 6) + column;

            if (startCellIndex >= cells.length) {
                console.error(`Vị trí ô bắt đầu (${startCellIndex}) vượt quá số lượng ô (${cells.length}) cho sự kiện #${index}`);
                return;
            }

            // Lấy ô bắt đầu
            const startCell = cells[startCellIndex];
            console.log(`Đã tìm thấy ô bắt đầu cho sự kiện #${index}:`, startCell);

            // Tính toán chiều cao của sự kiện (dựa trên số giờ)
            const hourHeight = 60; // Chiều cao mỗi ô giờ (px)
            const eventHeight = (endHour - startHour) * hourHeight;

            // Tạo phần tử hiển thị sự kiện
            const eventEl = document.createElement('div');
            eventEl.className = "calendar-event"
            eventEl.style.position = "absolute"
            eventEl.style.top = "5px"
            eventEl.style.left = "5px"
            eventEl.style.right = "5px"
            eventEl.style.height = `${eventHeight - 10}px` // Trừ đi padding
            eventEl.style.backgroundColor = event.color + "80"
            eventEl.style.borderLeft = `3px solid ${event.color}`
            eventEl.style.borderRadius = "0.125rem"
            eventEl.style.padding = "0.25rem"
            eventEl.style.fontSize = "0.75rem"
            eventEl.style.color = "white"
            eventEl.style.display = "block" // Thay đổi từ 'flex' thành 'block'
            eventEl.style.cursor = "pointer"
            eventEl.style.zIndex = "10"
            eventEl.style.overflow = "auto" // Cho phép scroll nếu nội dung quá dài
            eventEl.style.whiteSpace = "normal" // Cho phép xuống dòng
            eventEl.style.wordBreak = "break-word" // Cho phép ngắt từ khi cần thiết
            eventEl.style.lineHeight = "1.2" // Giảm khoảng cách giữa các dòng
            eventEl.innerHTML = `
              <div><strong>Class:</strong> ${event.details.className}</div>
              <div><strong>Subject:</strong> ${event.details.subjectName}</div>
              <div><strong>Room:</strong> ${event.details.roomName}</div>
            `
            // Thêm sự kiện vào ô
            startCell.appendChild(eventEl);
            console.log(`Đã thêm sự kiện #${index} vào ô`);

            // Thêm sự kiện click
            eventEl.addEventListener('click', function() {
                showEventDetails(event);
            });
        } catch (error) {
            console.error(`Lỗi khi xử lý sự kiện #${index}:`, error);
        }
    });
}

// Hiển thị chi tiết event khi click
function showEventDetails(event) {
    const modal = document.createElement("div")
    modal.className = "fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"

    // Tạo nội dung chi tiết hơn nếu có thông tin details
    let detailsHTML = ""
    if (event.details) {
        detailsHTML = `
            <p class="text-sm text-gray-700 mb-1"><strong>Lớp:</strong> ${event.details.className}</p>
            <p class="text-sm text-gray-700 mb-1"><strong>Môn học:</strong> ${event.details.subjectName}</p>
            <p class="text-sm text-gray-700 mb-1"><strong>Phòng:</strong> ${event.details.roomName}</p>
            <p class="text-sm text-gray-700 mb-4"><strong>Thời gian:</strong> ${formatTime(event.details.startTime)} - ${formatTime(event.details.endTime)}</p>
        `
    } else {
        detailsHTML = `
            <p class="text-sm text-gray-500 mb-4">
                ${formatDate(event.startDate)} ${event.startHour}:00 - ${event.endHour}:00
            </p>
        `
    }

    modal.innerHTML = `
        <div class="bg-white rounded-md p-4 max-w-md w-full">
            <h3 class="text-lg font-medium mb-2">${event.title}</h3>
            ${detailsHTML}
            <div class="flex justify-end gap-2">
                <button class="btn btn-outline delete-event">Delete</button>
                <button class="btn btn-primary close-modal">Close</button>
            </div>
        </div>
    `

    document.body.appendChild(modal)

    // Xử lý đóng modal
    modal.querySelector(".close-modal").addEventListener("click", () => {
        modal.remove()
    })

    // Xử lý xóa event
    modal.querySelector(".delete-event").addEventListener("click", () => {
        deleteEvent(event.id)
        modal.remove()
    })

    // Đóng modal khi click bên ngoài
    modal.addEventListener("click", (e) => {
        if (e.target === modal) {
            modal.remove()
        }
    })
}
// Định dạng ngày
function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
}
function addEventStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .calendar-event {
            position: absolute;
            top: 5px;
            left: 5px;
            right: 5px;
            height: 80%;
            border-radius: 0.125rem;
            padding: 0.25rem;
            font-size: 0.75rem;
            color: black;
            background-color: #ffcccc;
            display: flex;
            align-items: center;
            cursor: pointer;
            z-index: 10;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        
        .calendar-body > div {
            position: relative;
        }
    `;
    document.head.appendChild(style);
}
// Sửa lại hàm getMonthNumber để trả về index (0-11)
function getMonthNumber(monthName) {
    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months.findIndex(m => m.toLowerCase() === monthName.toLowerCase());
}


// Hàm cập nhật các ngày trong tuần
function updateMonthDisplay(startDate) {
    if (!startDate || isNaN(startDate.getTime())) {
        console.error("startDate không hợp lệ trong updateMonthDisplay:", startDate);
        return; // Không fallback về ngày hiện tại
    }

    try {
        const monday = new Date(startDate);
        const friday = new Date(startDate);
        friday.setDate(monday.getDate() + 4); // Ngày thứ 6

        const mondayMonth = monday.getMonth();
        const fridayMonth = friday.getMonth();

        const mondayMonthName = getMonthName(mondayMonth) || 'January';
        const fridayMonthName = getMonthName(fridayMonth) || 'January';

        if (mondayMonth === fridayMonth) {
            document.getElementById('current-month').textContent = `${mondayMonthName} ${monday.getFullYear()}`;
        } else {
            const mondayYear = monday.getFullYear();
            const fridayYear = friday.getFullYear();

            if (mondayYear === fridayYear) {
                document.getElementById('current-month').textContent = `${mondayMonthName} - ${fridayMonthName} ${mondayYear}`;
            } else {
                document.getElementById('current-month').textContent = `${mondayMonthName} ${mondayYear} - ${fridayMonthName} ${fridayYear}`;
            }
        }
    } catch (error) {
        console.error("Lỗi khi cập nhật hiển thị tháng:", error);
        // Không fallback về ngày hiện tại, giữ nguyên trạng thái
    }
}

function updateWeekDays(startDate) {
    if (!startDate || isNaN(startDate.getTime())) {
        console.error("startDate không hợp lệ trong updateWeekDays:", startDate);
        return; // Không fallback về ngày hiện tại
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
        // Không fallback về ngày hiện tại, giữ nguyên trạng thái
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

    // Fetch dữ liệu từ API và hiển thị
    fetchAndRenderSchedules();
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
            // Lấy ngày đầu tiên và cuối cùng hiển thị trên lịch
            const firstDayElement = document.querySelector('.calendar-header-cell:nth-child(2) .day-number');
            const lastDayElement = document.querySelector('.calendar-header-cell:nth-child(6) .day-number');

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
                    date.setDate(date.getDate() + 7); // Chuyển sang tuần sau (thêm 7 ngày)
                } else if (direction === 'prev') {
                    // Sử dụng tháng của ngày đầu tiên khi đi lui
                    const firstMonthYear = monthParts[0].split(' ');
                    month = getMonthNumber(firstMonthYear[0]);
                    year = parseInt(firstMonthYear[1]);
                    const firstDay = parseInt(firstDayElement.textContent.trim());

                    // Kiểm tra giá trị hợp lệ
                    if (isNaN(month) || isNaN(year) || isNaN(firstDay)) {
                        throw new Error("Invalid month, year, or firstDay");
                    }

                    date = new Date(year, month, firstDay);
                    console.log("Before subtracting 7 days:", date); // Debug

                    // Lùi về tuần trước
                    date.setDate(date.getDate() - 7);
                    console.log("After subtracting 7 days:", date); // Debug
                } else if (direction === 'today') {
                    date = new Date();
                }

                if (date) {
                    // Điều chỉnh về đầu tuần (thứ Hai)
                    const dayOfWeek = date.getDay() || 7;
                    date.setDate(date.getDate() - dayOfWeek + 1);
                    console.log("After adjusting to Monday:", date); // Debug
                }
            } else {
                throw new Error("Cannot find firstDayElement or lastDayElement");
            }
        } catch (error) {
            console.error("Lỗi khi phân tích ngày:", error);
            // Nếu có lỗi, thử lấy ngày từ tháng hiện tại
            const monthParts = currentMonth.split(' - ');
            let month, year;

            // Lấy tháng và năm từ phần đầu tiên của currentMonth
            const firstMonthYear = monthParts[0].split(' ');
            month = getMonthNumber(firstMonthYear[0]);
            year = parseInt(firstMonthYear[1]);

            if (isNaN(month) || isNaN(year)) {
                // Nếu không lấy được tháng/năm, sử dụng ngày hiện tại
                const now = new Date();
                month = now.getMonth();
                year = now.getFullYear();
            }

            // Tạo ngày từ tháng và năm hiện tại
            date = new Date(year, month, 1);

            if (direction === 'prev') {
                // Lùi về tháng trước
                // Cập nhật lại ngày để nằm trong tuần cuối của tháng trước
                date.setDate(1); // Đặt về ngày 1 của tháng
                date.setDate(date.getDate() - 7); // Lùi về tuần trước
            } else if (direction === 'next') {
                // Tiến tới tháng sau
                date.setMonth(date.getMonth() + 1);
            }

            // Điều chỉnh về đầu tuần (thứ Hai)
            const dayOfWeek = date.getDay() || 7;
            date.setDate(date.getDate() - dayOfWeek + 1);
        }

        if (!date || isNaN(date.getTime())) {
            console.error("Ngày không hợp lệ, sử dụng ngày hiện tại");
            date = new Date();
            const dayOfWeek = date.getDay() || 7;
            date.setDate(date.getDate() - dayOfWeek + 1);
        }

        // Cập nhật tiêu đề tháng/năm và các ngày trong tuần
        console.log("Before updating display:", date); // Debug
        updateMonthDisplay(date);
        updateWeekDays(date);
        renderEvents();
        console.log("After updating display:", document.getElementById('current-month').textContent); // Debug

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

    // Lấy thông tin tuần hiện tại từ lịch
    const currentMonthText = document.getElementById('current-month').textContent;
    const monthParts = currentMonthText.split(' - ');
    const firstMonthYear = monthParts[0].split(' ');
    let currentMonth = getMonthNumber(firstMonthYear[0]);
    let currentYear = parseInt(firstMonthYear[1]);

    // Lấy ngày đầu tiên (thứ Hai) của tuần hiện tại
    const firstDayElement = document.querySelector('.calendar-header-cell:nth-child(2) .day-number');
    const firstDay = parseInt(firstDayElement.textContent.trim());

    // Kiểm tra giá trị hợp lệ
    if (isNaN(currentMonth) || isNaN(currentYear) || isNaN(firstDay)) {
        console.error("Invalid month, year, or first day:", currentMonth, currentYear, firstDay);
        currentMonth = new Date().getMonth();
        currentYear = new Date().getFullYear();
    }

    // Tạo ngày thứ Hai của tuần hiện tại
    let currentMonday = new Date(currentYear, currentMonth, firstDay);
    if (isNaN(currentMonday.getTime())) {
        console.error("Invalid date for currentMonday:", currentMonday);
        currentMonday = new Date();
    }

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
        <span class="month-picker-title"></span>
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

    // Hàm xác định tháng hiển thị (ưu tiên tháng trước nếu giữa hai tháng)
    function getDisplayMonth(startDate) {
        const monday = new Date(startDate);
        const friday = new Date(startDate);
        friday.setDate(monday.getDate() + 4);

        const mondayMonth = monday.getMonth();
        const mondayYear = monday.getFullYear();

        // Ưu tiên tháng của ngày thứ Hai (tháng trước)
        return { month: mondayMonth, year: mondayYear };
    }

    // Hàm render tháng
    function renderMonth(date) {
        grid.innerHTML = '';
        const { month, year } = getDisplayMonth(date);

        // Kiểm tra giá trị hợp lệ
        if (isNaN(month) || isNaN(year)) {
            console.error("Invalid month or year in renderMonth:", month, year);
            return;
        }

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const startingDay = firstDay.getDay();
        const totalDays = lastDay.getDate();

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

            if (isCurrentMonth && i === today.getDate()) {
                dayEl.classList.add('today');
            }

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
        const monthName = getMonthName(month);
        if (!monthName) {
            console.error("Invalid month name for month:", month);
            return;
        }
        header.querySelector('.month-picker-title').textContent = `${monthName} ${year}`;
    }

    picker.appendChild(grid);
    renderMonth(currentMonday);

    // Thêm xử lý sự kiện cho nút điều hướng
    header.querySelector('#prev-picker-month').addEventListener('click', (e) => {
        e.stopPropagation();
        const current = getDisplayMonth(currentMonday);
        currentMonday = new Date(current.year, current.month - 1, 1); // Chuyển sang tháng trước
        renderMonth(currentMonday);
    });

    header.querySelector('#next-picker-month').addEventListener('click', (e) => {
        e.stopPropagation();
        const current = getDisplayMonth(currentMonday);
        currentMonday = new Date(current.year, current.month + 1, 1); // Chuyển sang tháng sau
        renderMonth(currentMonday);
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
