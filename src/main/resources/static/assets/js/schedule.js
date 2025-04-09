// ============ BASIC CALENDAR FUNCTIONALITY ============

// Array to store events
let events = []

// Generate unique ID for event
function generateId() {
    return Date.now().toString() + Math.floor(Math.random() * 1000)
}

// Consolidated addEvent function that supports both use cases
function addEvent(title, startDate, startHour, endDate, dayOfWeekOrEndHour, endHour, color = "#4f46e5") {
    // Check if the 5th parameter is dayOfWeek (string) or endHour (number)
    let finalEndHour, finalDayOfWeek

    if (
        typeof dayOfWeekOrEndHour === "string" &&
        ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"].includes(
            dayOfWeekOrEndHour.toUpperCase(),
        )
    ) {
        // Case: addEvent(title, startDate, startHour, endDate, dayOfWeek, endHour, color)
        finalDayOfWeek = dayOfWeekOrEndHour
        finalEndHour = endHour || (Number.parseInt(startHour) + 1).toString()
    } else {
        // Case: addEvent(title, startDate, startHour, endDate, endHour, color)
        finalEndHour = dayOfWeekOrEndHour || (Number.parseInt(startHour) + 1).toString()
        finalDayOfWeek = null
    }

    const newEvent = {
        id: generateId(),
        title: title,
        startDate: startDate,
        startHour: startHour,
        endDate: endDate || startDate,
        endHour: finalEndHour,
        color: color,
        dayOfWeek: finalDayOfWeek,
    }

    console.log("Event added:", newEvent)
    events.push(newEvent)
    renderEvents()
}

// Delete event
function deleteEvent(eventId) {
    events = events.filter((event) => event.id !== eventId)
    renderEvents()
}

// Fetch data from API and add to events
async function fetchAndRenderSchedules() {
    try {
        console.log("Starting fetchAndRenderSchedules")

        // Clear old events
        events = []

        // Get userId from hidden input
        const userId = document.getElementById("userId").value
        const userRole = document.getElementById("userRole").value
        console.log(`Fetching schedules for user: ${userId}, role: ${userRole}`)

        // API endpoint based on user role
        let endpoint = "/api/scheduling/all"

        // If userId exists and not ADMIN, filter by userId
        if (userId && userRole !== "ADMIN") {
            endpoint = `/api/scheduling/user/${userId}`
        }

        // Call API to get schedule
        const response = await fetch(endpoint, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        })

        if (!response.ok) {
            throw new Error(`Failed to fetch schedules: ${response.status} ${response.statusText}`)
        }

        // Get data from response
        let schedules = []
        try {
            const text = await response.text() // Get response as text first
            console.log("Raw API response:", text)

            // Check if response is empty or whitespace only
            if (!text || !text.trim()) {
                console.warn("API returned empty response - this is not an error, just no data available")
                schedules = []
            } else {
                try {
                    schedules = JSON.parse(text)
                } catch (parseError) {
                    console.error("Failed to parse API response as JSON:", parseError)
                    console.log("Raw API response text:", text)
                    console.warn("Continuing with empty schedules array")
                    schedules = []
                }
            }
        } catch (error) {
            console.error("Error processing API response:", error)
            console.warn("Continuing with empty schedules array")
            schedules = []
        }

        // Log detailed data for debugging
        console.log("Data from API (details):", JSON.stringify(schedules, null, 2))

        // Check if schedules is an array
        if (!Array.isArray(schedules)) {
            throw new Error("Returned data is not an array")
        }

        console.log(`Received ${schedules.length} schedules from API`)

        // Process each schedule and add to events
        schedules.forEach((schedule, index) => {
            if (!schedule.room || !schedule.subject || !schedule.classField) {
                console.warn("Schedule missing required fields:", schedule)
                return
            }

            const roomName = schedule.room.roomName || `Room ${schedule.room.id}`
            const subjectName = schedule.subject.subjectName || `Subject ${schedule.subject.id}`
            const className = schedule.classField.className || `Class ${schedule.classField.id}`

            const dayOfWeek = schedule.dayOfWeek
            const startDate = schedule.startDate
            const endDate = schedule.endDate || startDate

            const startTime = schedule.startTime
            const endTime = schedule.endTime

            const title = `${className} - ${subjectName} - ${roomName}`

            let startHour = "0"
            let endHour = "1"

            if (startTime && typeof startTime === "string") {
                const startTimeParts = startTime.split(":")
                if (startTimeParts.length >= 1) {
                    startHour = Number.parseInt(startTimeParts[0]).toString()
                }
            } else {
                console.warn("Invalid startTime:", startTime)
            }

            if (endTime && typeof endTime === "string") {
                const endTimeParts = endTime.split(":")
                if (endTimeParts.length >= 1) {
                    endHour = Number.parseInt(endTimeParts[0]).toString()
                }
            } else {
                console.warn("Invalid endTime:", endTime)
            }

            let color = "#4f46e5"
            if (subjectName.includes("Japanese")) {
                color = "#EF4444"
            } else if (subjectName.includes("Korean")) {
                color = "#F97316"
            } else if (subjectName.includes("Code")) {
                color = "#3B82F6"
            } else if (subjectName.includes("Advanced")) {
                color = "#8B5CF6"
            }

            const newEvent = {
                id: generateId(),
                title: title,
                startDate: startDate,
                endDate: endDate,
                startHour: startHour,
                endHour: endHour,
                color: color,
                dayOfWeek: dayOfWeek,
                details: {
                    className: className,
                    subjectName: subjectName,
                    roomName: roomName,
                    dayOfWeek: dayOfWeek,
                    startTime: startTime,
                    endTime: endTime,
                    location: roomName,
                    classId: schedule.classField.id,
                },
            }

            console.log(`Adding event #${index}:`, newEvent)
            events.push(newEvent)
        })

        console.log(`Added ${events.length} events to events array`)

        addEventStyles()
        renderEvents()
    } catch (error) {
        console.error("Error fetching schedules:", error)
    }
}

// Fetch Data from Attendance API
async function fetchAttendance(classId) {
    try {
        if (!classId) throw new Error("Thiếu classId để fetch attendance");

        console.log("📥 Đang gọi API attendance cho classId:", classId);

        const response = await fetch(`/api/attendance/class/${classId}`, {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });

        if (!response.ok) {
            throw new Error(`Không thể fetch attendance: ${response.status} ${response.statusText}`);
        }

        const attendanceData = await response.json();
        console.log("✅ Danh sách attendance:", attendanceData);

        window.currentClassId = classId; // lưu lại để reload sau update
        renderAttendance(attendanceData);
    } catch (error) {
        console.error("❌ Lỗi khi fetch attendance:", error.message);
    }
}
function updateStatus(attendanceId, newStatus) {
    const url = newStatus === 'PRESENT'
        ? `/api/attendance/updatePresent/${attendanceId}`
        : `/api/attendance/updateAbsent/${attendanceId}`;

    const payload = {
        id: attendanceId,
        status: newStatus
    };

    fetch(url, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) throw new Error("Cập nhật thất bại!");
            return response.json();
        })
        .then(() => {
            alert("✅ Cập nhật trạng thái thành công!");
            if (window.currentClassId) {
                fetchAttendance(window.currentClassId);
            }
        })
        .catch(err => {
            console.error("❌ Lỗi khi cập nhật:", err.message);
            alert("❌ Có lỗi xảy ra khi cập nhật trạng thái. Vui lòng thử lại!");
        });
}
function renderAttendance(data) {
    const container = document.getElementById("attendanceContainer");
    container.innerHTML = "";

    if (!data || data.length === 0) {
        container.innerHTML = '<div class="alert alert-warning text-center">Không có dữ liệu điểm danh.</div>';
        return;
    }

    // Tạo div container thay vì table
    const attendanceList = document.createElement("div");
    attendanceList.className = "attendance-list";

    // CSS inline cho attendance-list
    attendanceList.style.cssText = `
        display: flex;
        flex-direction: column;
        gap: 8px;
    `;

    // Header
    const header = document.createElement("div");
    header.className = "attendance-header";
    header.style.cssText = `
        display: grid;
        grid-template-columns: 120px 1fr 120px;
        background-color: #fff8e1;
        border-radius: 8px;
        padding: 10px;
        font-weight: 500;
    `;

    header.innerHTML = `
        <div class="text-center">Avatar</div>
        <div>Thông tin</div>
        <div class="text-center">Trạng thái</div>
    `;

    attendanceList.appendChild(header);

    // Render each attendance item
    data.forEach(item => {
        const user = item.user || {};
        const fullName = `${user.first_name || ""} ${user.last_name || ""}`;
        const username = user.userName || "N/A";
        const status = item.status || "UNKNOWN";
        const avatarPath = user.avatar_path || "https://cdn-icons-png.flaticon.com/512/149/149071.png";
        const attendanceId = item.id;

        // Xác định màu sắc dựa trên trạng thái
        let bgColor = "#ffffff";
        let statusBadgeClass = "badge bg-secondary";
        let statusText = "Chưa điểm danh";
        let borderColor = "#f0f0f0";

        if (status === "PRESENT") {
            bgColor = "#ffffff"; // Màu trắng
            statusBadgeClass = "badge bg-success";
            statusText = "Có mặt";
            borderColor = "#4caf50";
        } else if (status === "ABSENT") {
            bgColor = "#ffffff"; // Màu trắng
            statusBadgeClass = "badge bg-danger";
            statusText = "Vắng mặt";
            borderColor = "#f44336";
        }

        const attendanceItem = document.createElement("div");
        attendanceItem.id = `attendance-row-${attendanceId}`;
        attendanceItem.className = "attendance-item";
        attendanceItem.style.cssText = `
            display: grid;
            grid-template-columns: 120px 1fr 120px;
            background-color: ${bgColor};
            border-radius: 8px;
            padding: 12px;
            align-items: center;
            border-left: 4px solid ${borderColor};
        `;

        attendanceItem.innerHTML = `
            <div class="text-center">
                <img src="${avatarPath}" alt="Avatar" class="rounded-circle" 
                     style="width: 45px; height: 45px; object-fit: cover; border: 2px solid #ffe0c0;" />
            </div>
            <div>
                <div class="fw-medium fs-6">${fullName}</div>
                <div class="text-muted small">@${username}</div>
            </div>
            <div class="text-center">
                <div class="d-flex flex-column align-items-center gap-2">
                    <span class="${statusBadgeClass}" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">${statusText}</span>
                    <div class="d-flex gap-2">
                        <button onclick="handleAttendanceUpdate(${attendanceId}, 'PRESENT', '${status}')" 
                                class="btn btn-sm rounded-circle d-flex justify-content-center align-items-center" 
                                style="width: 32px; height: 32px; background-color: ${status === 'PRESENT' ? '#28a745' : '#ffffff'}; border: 1px solid #28a745; color: ${status === 'PRESENT' ? 'white' : '#28a745'};"
                                title="Có mặt">
                            <i class="bi bi-check-lg"></i>
                        </button>
                        <button onclick="handleAttendanceUpdate(${attendanceId}, 'ABSENT', '${status}')" 
                                class="btn btn-sm rounded-circle d-flex justify-content-center align-items-center" 
                                style="width: 32px; height: 32px; background-color: ${status === 'ABSENT' ? '#dc3545' : '#ffffff'}; border: 1px solid #dc3545; color: ${status === 'ABSENT' ? 'white' : '#dc3545'};"
                                title="Vắng mặt">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>
                    <div class="error-message text-danger small mt-1" id="error-${attendanceId}" style="display: none;"></div>
                </div>
            </div>
        `;

        attendanceList.appendChild(attendanceItem);
    });

    container.appendChild(attendanceList);

    // Thêm script xử lý cập nhật trạng thái
    const script = document.createElement("script");
    script.innerHTML =
      function handleAttendanceUpdate(id, newStatus, currentStatus) {
        // Nếu trạng thái mới giống trạng thái hiện tại, hiển thị thông báo lỗi
        if (newStatus === currentStatus) {
          const errorElement = document.getElementById('error-' + id);
          errorElement.textContent = "Sinh viên đã được đánh dấu " + (newStatus === "PRESENT" ? "có mặt" : "vắng mặt") + " rồi!";
          errorElement.style.display = "block";
          
          // Ẩn thông báo lỗi sau 3 giây
          setTimeout(() => {
            errorElement.style.display = "none";
          }, 3000);
          
          return;
        }
        
        // Cập nhật giao diện
        const row = document.getElementById('attendance-row-' + id);
        const statusBadge = row.querySelector('.badge');
        
        // Xác định màu sắc và văn bản mới
        let borderColor, statusText, statusClass;
        
        if (newStatus === "PRESENT") {
          borderColor = "#4caf50";
          statusText = "Có mặt";
          statusClass = "badge bg-success";
        } else {
          borderColor = "#f44336";
          statusText = "Vắng mặt";
          statusClass = "badge bg-danger";
        }
        
        // Cập nhật viền (giữ màu nền trắng)
        row.style.backgroundColor = "#ffffff";
        row.style.borderLeft = '4px solid ' + borderColor;
        
        // Cập nhật badge trạng thái
        statusBadge.className = statusClass;
        statusBadge.textContent = statusText;
        
        // Cập nhật màu nút
        const presentBtn = row.querySelector('button[title="Có mặt"]');
        const absentBtn = row.querySelector('button[title="Vắng mặt"]');
        
        presentBtn.style.backgroundColor = newStatus === "PRESENT" ? "#28a745" : "#ffffff";
        presentBtn.style.color = newStatus === "PRESENT" ? "white" : "#28a745";
        
        absentBtn.style.backgroundColor = newStatus === "ABSENT" ? "#dc3545" : "#ffffff";
        absentBtn.style.color = newStatus === "ABSENT" ? "white" : "#dc3545";
        
        // Ẩn thông báo lỗi nếu có
        document.getElementById('error-' + id).style.display = "none";
        
        // Gọi hàm cập nhật API
        updateStatus(id, newStatus);
      }
    ;
    document.body.appendChild(script);
}
function showEventDetails(event) {
    if (document.querySelector("#event-modal")) return;

    const modal = document.createElement("div");
    modal.id = "event-modal";
    modal.className = "modal fade show";
    modal.style.display = "block";
    modal.style.backgroundColor = "rgba(0,0,0,0.5)";

    modal.innerHTML = `
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-xl">
      <div class="modal-content">
        <div class="modal-header" style="background-color: #ff9d42; color: white;">
          <h5 class="modal-title">${event.title || "Chi tiết sự kiện"}</h5>
        </div>
        <div class="modal-body">
          <div class="card mb-4 border-warning">
            <div class="card-header bg-warning bg-opacity-25">
              <h5 class="mb-0">Thông tin lớp học</h5>
            </div>
            <div class="card-body">
              <div class="row">
                ${event.details ? `
                <div class="col-md-6">
                  <ul class="list-group list-group-flush">
                    <li class="list-group-item d-flex justify-content-between">
                      <span class="fw-bold">Lớp:</span>
                      <span>${event.details.className}</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                      <span class="fw-bold">Môn học:</span>
                      <span>${event.details.subjectName}</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                      <span class="fw-bold">Phòng:</span>
                      <span>${event.details.roomName}</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                      <span class="fw-bold">Thời gian:</span>
                      <span>${formatTime(event.details.startTime)} - ${formatTime(event.details.endTime)}</span>
                    </li>
                  </ul>
                </div>
                ` : `
                <div class="col-12">
                  <p class="card-text">
                    <i class="bi bi-calendar-event me-2"></i>
                    ${formatDate(event.startDate)} ${event.startHour}:00 - ${event.endHour}:00
                  </p>
                </div>
                `}
              </div>
            </div>
          </div>

          <!-- Vùng hiển thị Attendance -->
          <div id="attendanceContainer" class="mt-4">
            <div class="d-flex align-items-center mb-3">
              <h3 class="h5 mb-0 me-2">Danh sách điểm danh</h3>
              <div class="spinner-border spinner-border-sm text-warning" role="status">
                <span class="visually-hidden">Đang tải...</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" id="close-modal-btn" class="btn btn-warning">Đóng</button>
        </div>
      </div>
    </div>
    `;

    document.body.appendChild(modal);

    // Add Bootstrap modal backdrop
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop fade show";
    document.body.appendChild(backdrop);

    // Prevent body scrolling
    document.body.classList.add("modal-open");
    document.body.style.overflow = "hidden";
    document.body.style.paddingRight = "17px";

    // Close modal functions
    const closeModal = () => {
        modal.remove();
        backdrop.remove();
        document.body.classList.remove("modal-open");
        document.body.style.overflow = "";
        document.body.style.paddingRight = "";
    };

    modal.querySelector("#close-modal-btn").addEventListener("click", closeModal);
    modal.addEventListener("click", (e) => {
        if (e.target === modal) closeModal();
    });

    // Gọi API Attendance nếu có classId
    if (event.details && event.details.classId) {
        fetchAttendance(event.details.classId);
    }
}

// Display all events on calendar - version with continuous blocks
function renderEvents() {
    console.log("renderEvents called with", events.length, "events")

    // Remove all current events from UI
    document.querySelectorAll(".calendar-event").forEach((el) => el.remove())

    if (events.length === 0) {
        console.log("No events to display")
        return
    }

    // Debug: Print structure of calendar-body
    const calendarBody = document.querySelector(".calendar-body")
    if (!calendarBody) {
        console.error("Could not find .calendar-body")
        return
    }

    // Get information about days displayed on calendar
    const headerCells = document.querySelectorAll(".calendar-header-cell")
    if (headerCells.length <= 1) {
        console.error("Not enough .calendar-header-cell found")
        return
    }

    // Get month and year from title
    const currentMonthText = document.getElementById("current-month").textContent
    console.log("Current month:", currentMonthText)

    // Handle case of displaying 2 months (e.g., "March - April 2025")
    let firstMonthName, secondMonthName, year

    if (currentMonthText.includes(" - ")) {
        // Case of 2 months
        const parts = currentMonthText.split(" - ")
        firstMonthName = parts[0].trim()

        // Second part could be "April 2025"
        const secondParts = parts[1].trim().split(" ")
        secondMonthName = secondParts[0].trim()
        year = Number.parseInt(secondParts[1])
    } else {
        // Case of 1 month
        const parts = currentMonthText.split(" ")
        firstMonthName = parts[0].trim()
        secondMonthName = null
        year = Number.parseInt(parts[1])
    }

    if (isNaN(year)) {
        console.error("Could not parse year from:", currentMonthText)
        return
    }

    const firstMonthIndex = getMonthNumber(firstMonthName)
    const secondMonthIndex = secondMonthName ? getMonthNumber(secondMonthName) : -1

    if (firstMonthIndex === -1) {
        console.error("Invalid month:", firstMonthName)
        return
    }

    console.log(
        `Displaying: Month ${firstMonthIndex + 1}${secondMonthIndex !== -1 ? " and " + (secondMonthIndex + 1) : ""} year ${year}`,
    )

    // Create mapping between day and column, and store month info for each day
    const dayToColumnMap = new Map()
    const dayToMonthMap = new Map()
    const dayToDayOfWeekMap = new Map() // Map to store day of week for each day

    // Skip first cell because it's empty
    for (let i = 1; i < headerCells.length; i++) {
        const dayNumber = headerCells[i].querySelector(".day-number")
        const dayNameElement = headerCells[i].querySelector(".day-name")

        if (dayNumber && dayNameElement) {
            const day = Number.parseInt(dayNumber.textContent.trim())
            const dayName = dayNameElement.textContent.trim().toUpperCase()

            if (!isNaN(day)) {
                // Save mapping between day and column (index starts from 1)
                dayToColumnMap.set(day, i)

                // Save mapping between day and day of week
                dayToDayOfWeekMap.set(day, dayName)

                // Determine month of this day (logic: if day is small and there's a second month, it belongs to second month)
                // Typically, end of month > 28, start of month < 7
                if (secondMonthIndex !== -1 && day < 15) {
                    dayToMonthMap.set(day, secondMonthIndex)
                } else {
                    dayToMonthMap.set(day, firstMonthIndex)
                }
            }
        }
    }

    console.log("Day -> column mapping:", Object.fromEntries(dayToColumnMap))
    console.log("Day -> month mapping:", Object.fromEntries(dayToMonthMap))
    console.log("Day -> day of week mapping:", Object.fromEntries(dayToDayOfWeekMap))

    // Get the dates for the current week
    const currentWeekDates = []
    for (let i = 1; i < headerCells.length; i++) {
        const dayNumber = headerCells[i].querySelector(".day-number")
        if (dayNumber) {
            const day = Number.parseInt(dayNumber.textContent.trim())
            if (!isNaN(day)) {
                const month = dayToMonthMap.get(day)
                if (month !== undefined) {
                    const date = new Date(year, month, day)
                    currentWeekDates.push(date)
                }
            }
        }
    }

    // Display each event
    events.forEach((event, index) => {
        console.log(`Processing event #${index}:`, event)

        try {
            // Check if this event should be displayed based on dayOfWeek
            if (event.dayOfWeek) {
                // This is a recurring event based on day of week
                // Find all days in the current week that match this day of week
                const matchingDays = []

                for (const [day, dayOfWeek] of dayToDayOfWeekMap.entries()) {
                    if (dayOfWeek === event.dayOfWeek) {
                        matchingDays.push(day)
                    }
                }

                if (matchingDays.length === 0) {
                    console.log(`No matching days for event #${index} with dayOfWeek ${event.dayOfWeek}`)
                    return
                }

                // Check if the current week is within the event's date range
                const startDateObj = new Date(event.startDate)
                const endDateObj = new Date(event.endDate)

                // For each matching day, check if it's within the date range
                for (const day of matchingDays) {
                    const month = dayToMonthMap.get(day)
                    const dateObj = new Date(year, month, day)

                    // Check if this date is within the event's date range
                    if (dateObj >= startDateObj && dateObj <= endDateObj) {
                        // This day matches the dayOfWeek and is within the date range
                        renderEventOnDay(event, day, index)
                    } else {
                        console.log(`Day ${day} is outside the event date range (${event.startDate} to ${event.endDate})`)
                    }
                }
            } else {
                // This is a one-time event on a specific date
                // Parse date from startDate (format YYYY-MM-DD)
                const dateParts = event.startDate.split("-")
                if (dateParts.length !== 3) {
                    console.error(`Invalid date format for event #${index}:`, event.startDate)
                    return
                }

                const eventYear = Number.parseInt(dateParts[0])
                const eventMonth = Number.parseInt(dateParts[1]) - 1 // Month in JS starts from 0
                const eventDay = Number.parseInt(dateParts[2])

                // Check if year matches
                if (eventYear !== year) {
                    console.log(`Event #${index} not in current year: ${eventYear} vs ${year}`)
                    return
                }

                // Check if month matches one of the two displayed months
                const isInFirstMonth = eventMonth === firstMonthIndex
                const isInSecondMonth = secondMonthIndex !== -1 && eventMonth === secondMonthIndex

                if (!isInFirstMonth && !isInSecondMonth) {
                    console.log(
                        `Event #${index} not in displayed month: ${eventMonth + 1} vs ${firstMonthIndex + 1}${secondMonthIndex !== -1 ? " or " + (secondMonthIndex + 1) : ""}`,
                    )
                    return
                }

                // Find column corresponding to day
                const column = dayToColumnMap.get(eventDay)
                if (!column) {
                    console.log(`Column not found for day ${eventDay} of event #${index}`)
                    return
                }

                // Check if month of day on calendar matches month of event
                const dayMonth = dayToMonthMap.get(eventDay)
                if (dayMonth !== eventMonth) {
                    console.log(
                        `Day ${eventDay} on calendar belongs to month ${dayMonth + 1}, but event belongs to month ${eventMonth + 1}`,
                    )
                    return
                }

                renderEventOnDay(event, eventDay, index)
            }
        } catch (error) {
            console.error(`Error processing event #${index}:`, error)
        }
    })

    // Helper function to render an event on a specific day
    function renderEventOnDay(event, day, eventIndex) {
        // Find column corresponding to day
        const column = dayToColumnMap.get(day)
        if (!column) {
            console.log(`Column not found for day ${day} of event #${eventIndex}`)
            return
        }

        // Get start and end hours
        const startHour = Number.parseInt(event.startHour)
        const endHour = Number.parseInt(event.endHour)

        if (isNaN(startHour) || startHour < 0 || startHour > 23) {
            console.error(`Invalid start hour for event #${eventIndex}:`, event.startHour)
            return
        }

        if (isNaN(endHour) || endHour < 0 || endHour > 23) {
            console.error(`Invalid end hour for event #${eventIndex}:`, event.endHour)
            return
        }

        // Get all cells in calendar-body
        const cells = Array.from(calendarBody.children)

        // Calculate position of start and end cells
        const startCellIndex = startHour * 6 + column
        const endCellIndex = endHour * 6 + column

        if (startCellIndex >= cells.length) {
            console.error(
                `Start cell position (${startCellIndex}) exceeds number of cells (${cells.length}) for event #${eventIndex}`,
            )
            return
        }

        // Get start cell
        const startCell = cells[startCellIndex]
        console.log(`Found start cell for event #${eventIndex} on day ${day}:`, startCell)

        // Calculate height of event (based on number of hours)
        const hourHeight = 60 // Height of each hour cell (px)
        const eventHeight = (endHour - startHour) * hourHeight

        // Create element to display event
        const eventEl = document.createElement("div")
        eventEl.className = "calendar-event"
        eventEl.style.position = "absolute"
        eventEl.style.top = "5px"
        eventEl.style.left = "5px"
        eventEl.style.right = "5px"
        eventEl.style.height = `${eventHeight - 10}px` // Subtract padding
        eventEl.style.backgroundColor = event.color + "80"
        eventEl.style.borderLeft = `3px solid ${event.color}`
        eventEl.style.borderRadius = "0.125rem"
        eventEl.style.padding = "0.25rem"
        eventEl.style.fontSize = "0.75rem"
        eventEl.style.color = "white"
        eventEl.style.display = "block" // Changed from 'flex' to 'block'
        eventEl.style.cursor = "pointer"
        eventEl.style.zIndex = "10"
        eventEl.style.overflow = "auto" // Allow scrolling if content is too long
        eventEl.style.whiteSpace = "normal" // Allow line breaks
        eventEl.style.wordBreak = "break-word" // Allow word breaking when necessary
        eventEl.style.lineHeight = "1.2" // Reduce line spacing

        // Create content based on available details
        if (event.details) {
            eventEl.innerHTML = `
                <div><strong>Class:</strong> ${event.details.className}</div>
                <div><strong>Subject:</strong> ${event.details.subjectName}</div>
                <div><strong>Room:</strong> ${event.details.roomName}</div>
              `
        } else {
            eventEl.innerHTML = `<div>${event.title}</div>`
        }

        // Add event to cell
        startCell.appendChild(eventEl)
        console.log(`Added event #${eventIndex} to cell for day ${day}`)

        // Add click event
        eventEl.addEventListener("click", () => {
            showEventDetails(event)
        })
    }
}



// Định dạng ngày
function formatDate(dateString) {
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}
function formatTime(timeString) {
    const date = new Date(`1970-01-01T${timeString}`);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
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

            const dayElements = document.querySelectorAll('.calendar-header-cell:nth-child(n+2):nth-child(-n+6) .day-number');
            let todayColumnIndex = -1;

            // Tìm cột của ngày hôm nay
            dayElements.forEach((el, index) => {
                const dayNumber = parseInt(el.textContent);
                if (dayNumber === now.getDate()) {
                    const headerCell = el.closest('.calendar-header-cell');
                    if (headerCell.classList.contains('active-day')) {
                        todayColumnIndex = index + 1;
                    }
                }
            });

            // Xóa time indicator cũ nếu có
            const oldIndicator = document.querySelector('.current-time-indicator');
            if (oldIndicator) {
                oldIndicator.remove();
            }

            if (todayColumnIndex !== -1) {
                const hourHeight = 60;
                const minuteHeight = (minutes / 60) * hourHeight;
                const topPosition = (hours * hourHeight) + minuteHeight;

                // Tạo indicator
                const indicator = document.createElement('div');
                indicator.className = 'current-time-indicator';
                indicator.style.position = 'absolute';
                indicator.style.top = `${topPosition}px`;
                indicator.style.height = '2px';
                indicator.style.backgroundColor = 'red';
                indicator.style.zIndex = '10';

                // Tính toán vị trí cột
                const columnWidth = `calc((100% - 60px) / 5)`;
                const leftPosition = `calc(60px + ${columnWidth} * ${todayColumnIndex - 1})`;

                indicator.style.left = leftPosition;
                indicator.style.width = columnWidth;

                // Tạo label hiển thị giờ
                const timeLabel = document.createElement('div');
                timeLabel.className = 'current-time-label';
                timeLabel.textContent = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
                timeLabel.style.position = 'absolute';
                timeLabel.style.top = '-15px';
                timeLabel.style.left = '5px';
                timeLabel.style.backgroundColor = 'white';
                timeLabel.style.padding = '2px 5px';
                timeLabel.style.borderRadius = '5px';

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
