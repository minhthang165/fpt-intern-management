document.addEventListener('DOMContentLoaded', function() {
    let currentDisplayDate = new Date(); // Sử dụng ngày hiện tại thay vì ngày cố định
    
    // Mảng lưu trữ lịch
    let schedules = [];
    
    // Cập nhật ngày hiện tại
    const updateCurrentDate = (date) => {
        const day = date.getDate();
        const month = date.getMonth() + 1;
        const year = date.getFullYear();

        document.querySelector('.current-date').textContent = `${day} tháng ${month}, ${year}`;
    };

    // Khởi tạo ngày hiển thị ban đầu
    updateCurrentDate(currentDisplayDate);

    // Xử lý sự kiện khi nhấp vào nút Hôm nay
    document.getElementById('todayBtn').addEventListener('click', function() {
        currentDisplayDate = new Date(); // Reset về ngày hiện tại
        updateCurrentDate(currentDisplayDate);
        fetchAndRenderSchedules(currentDisplayDate);
    });

    // Xử lý sự kiện khi nhấp vào nút Next Day
    document.getElementById('nextDay').addEventListener('click', function() {
        // Tăng ngày hiện tại lên 1
        currentDisplayDate.setDate(currentDisplayDate.getDate() + 1);
        updateCurrentDate(currentDisplayDate);
        console.log('Next day clicked - New date:', currentDisplayDate.toLocaleDateString());
        fetchAndRenderSchedules(currentDisplayDate);
    });

    // Xử lý sự kiện khi nhấp vào nút Previous Day
    document.getElementById('prevDay').addEventListener('click', function() {
        // Giảm ngày hiện tại đi 1
        currentDisplayDate.setDate(currentDisplayDate.getDate() - 1);
        updateCurrentDate(currentDisplayDate);
        console.log('Previous day clicked - New date:', currentDisplayDate.toLocaleDateString());
        fetchAndRenderSchedules(currentDisplayDate);
    });

    document.getElementById('generateScheduleBtn').addEventListener('click', function() {
        generateSchedule();
    });

    function generateSchedule() {
        // Create modal for file upload with drag and drop
        const modal = document.createElement('div');
        modal.className = 'upload-modal';
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100%';
        modal.style.height = '100%';
        modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        modal.style.display = 'flex';
        modal.style.justifyContent = 'center';
        modal.style.alignItems = 'center';
        modal.style.zIndex = '1000';
        
        // Create content container
        const content = document.createElement('div');
        content.style.backgroundColor = 'white';
        content.style.padding = '30px';
        content.style.borderRadius = '8px';
        content.style.maxWidth = '500px';
        content.style.width = '100%';
        content.style.textAlign = 'center';
        
        // Create title
        const title = document.createElement('h3');
        title.textContent = 'Upload Schedule Template';
        title.style.marginBottom = '20px';
        title.style.fontWeight = '600';
        
        // Create drag and drop area
        const dropArea = document.createElement('div');
        dropArea.style.border = '2px dashed #ccc';
        dropArea.style.borderRadius = '5px';
        dropArea.style.padding = '40px 20px';
        dropArea.style.textAlign = 'center';
        dropArea.style.marginBottom = '20px';
        dropArea.style.cursor = 'pointer';
        dropArea.style.backgroundColor = '#f9f9f9';
        
        const dropText = document.createElement('p');
        dropText.innerHTML = 'Drag and drop your file here<br>or<br>click to select file';
        dropText.style.margin = '0';
        dropText.style.fontSize = '16px';
        dropText.style.color = '#555';
        
        // Create file input (hidden)
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = '.xlsx, .xls';
        fileInput.style.display = 'none';
        
        // Create status text area
        const statusText = document.createElement('p');
        statusText.style.margin = '10px 0';
        statusText.style.color = '#666';
        
        // Create buttons container
        const buttonContainer = document.createElement('div');
        buttonContainer.style.display = 'flex';
        buttonContainer.style.justifyContent = 'space-between';
        buttonContainer.style.marginTop = '20px';
        
        // Create cancel button
        const cancelButton = document.createElement('button');
        cancelButton.textContent = 'Cancel';
        cancelButton.className = 'btn btn-outline';
        cancelButton.style.padding = '8px 20px';
        cancelButton.style.backgroundColor = '#fff';
        cancelButton.style.border = '1px solid #ccc';
        cancelButton.style.borderRadius = '4px';
        
        // Create upload button (disabled initially)
        const uploadButton = document.createElement('button');
        uploadButton.textContent = 'Generate Schedule';
        uploadButton.className = 'btn btn-primary';
        uploadButton.style.padding = '8px 20px';
        uploadButton.style.backgroundColor = '#4a6cf7';
        uploadButton.style.color = 'white';
        uploadButton.style.border = 'none';
        uploadButton.style.borderRadius = '4px';
        uploadButton.disabled = true;
        uploadButton.style.opacity = '0.5';
        uploadButton.style.cursor = 'not-allowed';
        
        // Store the selected file
        let selectedFile = null;
        
        // Add dragover event
        dropArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropArea.style.backgroundColor = '#eef5ff';
            dropArea.style.borderColor = '#4a6cf7';
        });
        
        // Add dragleave event
        dropArea.addEventListener('dragleave', () => {
            dropArea.style.backgroundColor = '#f9f9f9';
            dropArea.style.borderColor = '#ccc';
        });
        
        // Add drop event
        dropArea.addEventListener('drop', (e) => {
            e.preventDefault();
            dropArea.style.backgroundColor = '#f9f9f9';
            dropArea.style.borderColor = '#ccc';
            
            if (e.dataTransfer.files.length) {
                selectedFile = e.dataTransfer.files[0];
                updateFileStatus(selectedFile);
            }
        });
        
        // Add click event to dropArea
        dropArea.addEventListener('click', () => {
            fileInput.click();
        });
        
        // Add change event to fileInput
        fileInput.addEventListener('change', () => {
            if (fileInput.files.length) {
                selectedFile = fileInput.files[0];
                updateFileStatus(selectedFile);
            }
        });
        
        // Function to update file status
        function updateFileStatus(file) {
            if (file) {
                const fileType = file.name.split('.').pop().toLowerCase();
                if (fileType === 'xlsx' || fileType === 'xls') {
                    statusText.textContent = `File selected: ${file.name}`;
                    statusText.style.color = '#4a6cf7';
                    uploadButton.disabled = false;
                    uploadButton.style.opacity = '1';
                    uploadButton.style.cursor = 'pointer';
                } else {
                    statusText.textContent = 'Please select a valid Excel file (.xlsx or .xls)';
                    statusText.style.color = 'red';
                    uploadButton.disabled = true;
                    uploadButton.style.opacity = '0.5';
                    uploadButton.style.cursor = 'not-allowed';
                }
            }
        }
        
        // Add click event to cancel button
        cancelButton.addEventListener('click', () => {
            document.body.removeChild(modal);
        });
        
        // Add click event to upload button
        uploadButton.addEventListener('click', async () => {
            if (!selectedFile) return;
            
            try {
                // Show loading state
                uploadButton.disabled = true;
                uploadButton.textContent = 'Generating...';
                statusText.textContent = 'Generating schedule, please wait...';
                statusText.style.color = '#4a6cf7';
                
                const formData = new FormData();
                formData.append('file', selectedFile);
                
                // Call the API
                const response = await fetch('/api/scheduling/generate', {
                    method: 'POST',
                    body: formData
                });
                
                if (!response.ok) {
                    throw new Error(`Failed to generate schedule: ${response.status} ${response.statusText}`);
                }
                
                const result = await response.json();
                
                // Success - close modal and refresh schedules
                document.body.removeChild(modal);
                fetchAndRenderSchedules(currentDisplayDate);
            } catch (error) {
                console.error('Error generating schedule:', error);
                statusText.textContent = `Error: ${error.message}`;
                statusText.style.color = 'red';
                uploadButton.disabled = false;
                uploadButton.textContent = 'Generate Schedule';
            }
        });
        
        // Assemble components
        dropArea.appendChild(dropText);
        
        buttonContainer.appendChild(cancelButton);
        buttonContainer.appendChild(uploadButton);
        
        content.appendChild(title);
        content.appendChild(dropArea);
        content.appendChild(fileInput);
        content.appendChild(statusText);
        content.appendChild(buttonContainer);
        
        modal.appendChild(content);
        document.body.appendChild(modal);
    }

    document.getElementById('downloadTemplateBtn').addEventListener('click', function() {
        downloadScheduleTemplate();
    });

    function downloadScheduleTemplate() {
        try {
            window.location.href = "/api/scheduling/template";
            
        } catch (error) {
            console.error("Error downloading template:", error);
        }
    }

    // Xử lý sự kiện khi nhấp vào checkbox Alpha
    document.getElementById('alpha')?.addEventListener('change', function() {
        const isChecked = this.checked;
        // Thực hiện logic khi checkbox thay đổi trạng thái
        console.log('Alpha checkbox:', isChecked ? 'đã chọn' : 'đã bỏ chọn');
    });

    // Xử lý sự kiện khi nhấp vào các nút khác
    const allButtons = document.querySelectorAll('.btn, .nav-btn');
    allButtons.forEach(button => {
        button.addEventListener('click', function() {
            const buttonText = this.textContent.trim();
            console.log(`Đã nhấp vào nút: ${buttonText}`);
        });
    });

    // Thêm hiệu ứng hover cho các ô lịch
    const scheduleCells = document.querySelectorAll('.schedule-cell');
    scheduleCells.forEach(cell => {
        cell.addEventListener('mouseenter', function() {
            if (this.querySelector('.schedule-item')) {
                this.style.cursor = 'pointer';
            }
        });

        cell.addEventListener('click', function() {
            const scheduleItem = this.querySelector('.schedule-item');
            if (scheduleItem) {
                const time = scheduleItem.querySelector('.schedule-time')?.textContent;
                const code = scheduleItem.querySelector('div:nth-child(2)')?.textContent;
            }
        });
    });
    
    // Fetch dữ liệu lịch từ API và hiển thị
    fetchAndRenderSchedules(currentDisplayDate);
    
    // Hàm để fetch dữ liệu lịch học từ API
    async function fetchAndRenderSchedules(date) {
        try {
            console.log("Starting fetchAndRenderSchedules for date:", date.toLocaleDateString());
            
            // Format ngày thành chuỗi YYYY-MM-DD
            const formattedDate = formatDateToString(date);
            
            // API endpoint
            const endpoint = `/api/scheduling/date/${formattedDate}`;
            console.log("Fetching schedules from:", endpoint);
            
            // Call API để lấy lịch
            const response = await fetch(endpoint, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch schedules: ${response.status} ${response.statusText}`);
            }

            // Xử lý dữ liệu JSON từ response
            let scheduleData = await response.json();
            console.log("Data from API:", scheduleData);
            
            // Lưu lịch và render
            schedules = scheduleData;
            renderSchedules();
            
        } catch (error) {
            console.error("Error fetching schedules:", error);
            // Nếu có lỗi, reset lại schedules
            schedules = [];
            renderSchedules();
        }
    }
    
    // Hàm để render lịch lên bảng
    function renderSchedules() {
        // Xóa tất cả các lịch hiện tại
        clearAllSchedules();
        
        // Nếu không có lịch, dừng xử lý
        if (!schedules || schedules.length === 0) {
            console.log("No schedules to display");
            updateColorLegend(); // Vẫn gọi để xóa chú thích nếu có
            return;
        }
        
        console.log(`Rendering ${schedules.length} schedules`);
        
        const currentDay = currentDisplayDate.getDay() || 7;
        const dayNames = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
        const currentDayName = dayNames[currentDisplayDate.getDay()];
        
        console.log("Current day:", currentDayName);
        
        // Xử lý mỗi lịch
        schedules.forEach(schedule => {
            try {
                // Kiểm tra xem lịch có phải ngày hiện tại không
                if (schedule.dayOfWeek && schedule.dayOfWeek !== currentDayName) {
                    console.log(`Schedule day ${schedule.dayOfWeek} doesn't match current day ${currentDayName}`);
                    return; // Bỏ qua nếu không phải ngày hiện tại
                }
                
                const roomName = schedule.room?.roomName || `Room ${schedule.room?.id}`;
                const subjectName = schedule.subject?.subjectName || `Subject ${schedule.subject?.id}`;
                const className = schedule.classField?.className || `Class ${schedule.classField?.id}`;
                
                // Lấy thông tin thời gian
                const startTime = schedule.startTime || "00:00";
                const endTime = schedule.endTime || "00:00";
                
                // Phân tích thời gian chi tiết
                const [startHour, startMinute] = startTime.split(":").map(val => parseInt(val));
                const [endHour, endMinute] = endTime.split(":").map(val => parseInt(val));
                
                // Tìm phòng trong bảng lịch
                const roomRows = document.querySelectorAll('.schedule-table tbody tr');
                let roomRow = null;
                
                roomRows.forEach(row => {
                    const roomCell = row.querySelector('.room-column');
                    if (roomCell && roomCell.textContent.trim() === roomName) {
                        roomRow = row;
                    }
                });
                
                if (!roomRow) {
                    console.log(`Room not found in table: ${roomName}`);
                    return;
                }
                
                // Tính toán vị trí và độ rộng của schedule item
                const hourWidth = roomRow.querySelector('.schedule-cell').offsetWidth;
                const minuteWidth = hourWidth / 60; // Độ rộng của 1 phút
                
                // Đảm bảo thời gian nằm trong phạm vi 7:00 - 18:00
                if (startHour < 7 || startHour >= 18 || endHour < 7 || endHour > 18) {
                    console.log(`Time out of range: ${startTime} - ${endTime}`);
                    return;
                }
                
                // Tính toán cột và vị trí trái của schedule item
                const startColumn = startHour - 7 + 1; // +1 vì cột đầu tiên là tên phòng
                const endColumn = endHour - 7 + 1;
                
                // Nếu cùng 1 cột (cùng giờ)
                if (startColumn === endColumn) {
                    const cell = roomRow.querySelector(`.schedule-cell:nth-child(${startColumn + 1})`);
                    if (!cell) return;
                    
                    // Tạo schedule item
                    createScheduleItem(cell, schedule, startTime, endTime, className, subjectName);
                } else {
                    // Span nhiều giờ
                    const startCell = roomRow.querySelector(`.schedule-cell:nth-child(${startColumn + 1})`);
                    if (!startCell) return;
                    
                    // Tính độ rộng dựa trên giờ
                    const durationHours = (endHour - startHour) + (endMinute - startMinute) / 60;
                    const width = durationHours * hourWidth;
                    
                    // Tạo schedule item
                    const scheduleItem = document.createElement('div');
                    scheduleItem.className = 'schedule-item';
                    scheduleItem.style.backgroundColor = getColorForClass(className, schedule.classField?.id);
                    scheduleItem.style.color = '#333'; // Text color tối hơn 
                    scheduleItem.style.padding = '4px';
                    scheduleItem.style.borderRadius = '4px';
                    scheduleItem.style.fontSize = '0.8rem';
                    scheduleItem.style.height = 'calc(100% - 4px)';
                    scheduleItem.style.position = 'absolute';
                    scheduleItem.style.top = '2px';
                    scheduleItem.style.left = `${startMinute * minuteWidth}px`;
                    scheduleItem.style.width = `${width - 4}px`; // -4 for padding
                    scheduleItem.style.overflow = 'hidden';
                    scheduleItem.style.zIndex = '10';
                    scheduleItem.style.fontWeight = '500';
                    scheduleItem.style.border = '1px solid rgba(0,0,0,0.1)';
                    scheduleItem.style.boxShadow = '0 1px 2px rgba(0,0,0,0.05)';
                    
                    // Format thời gian để chỉ hiển thị giờ và phút
                    const formattedStartTime = formatTime(startTime);
                    const formattedEndTime = formatTime(endTime);
                    
                    // Thêm nội dung vào schedule item
                    scheduleItem.innerHTML = `
                        <div class="schedule-time">${formattedStartTime} - ${formattedEndTime}</div>
                        <div>${className}</div>
                        <div>${subjectName}</div>
                    `;
                    
                    // Thêm event cho schedule item
                    scheduleItem.addEventListener('click', function() {
                        showScheduleDetails(schedule);
                    });
                    
                    // Thêm vào cell
                    startCell.style.position = 'relative';
                    startCell.appendChild(scheduleItem);
                }
            } catch (error) {
                console.error("Error rendering schedule:", error);
            }
        });
        updateColorLegend();
    }

    function formatTime(timeString) {
        if (!timeString) return "00:00";
        
        if (timeString.length === 5 && timeString.includes(":")) {
            return timeString;
        }
        

        if (timeString.length === 8 && timeString.split(":").length === 3) {
            return timeString.substring(0, 5);
        }
    
        try {
            const parts = timeString.split(":");
            const hours = parts[0].padStart(2, "0");
            const minutes = (parts.length > 1) ? parts[1].padStart(2, "0") : "00";
            return `${hours}:${minutes}`;
        } catch (error) {
            console.error("Error formatting time:", error);
            return "00:00";
        }
    }

    function createScheduleItem(cell, schedule, startTime, endTime, className, subjectName) {
        const scheduleItem = document.createElement('div');
        scheduleItem.className = 'schedule-item';
        scheduleItem.style.backgroundColor = getColorForClass(className, schedule.classField?.id);
        scheduleItem.style.color = '#333'; // Text color tối hơn 
        scheduleItem.style.padding = '4px';
        scheduleItem.style.borderRadius = '4px';
        scheduleItem.style.fontSize = '0.8rem';
        scheduleItem.style.height = '100%';
        scheduleItem.style.overflow = 'hidden';
        scheduleItem.style.fontWeight = '500';
        scheduleItem.style.border = '1px solid rgba(0,0,0,0.1)';
        scheduleItem.style.boxShadow = '0 1px 2px rgba(0,0,0,0.05)';
        
        const formattedStartTime = formatTime(startTime);
        const formattedEndTime = formatTime(endTime);
        
        // Thêm nội dung vào schedule item
        scheduleItem.innerHTML = `
            <div class="schedule-time">${formattedStartTime} - ${formattedEndTime}</div>
            <div>${className}</div>
            <div>${subjectName}</div>
        `;
        
        // Thêm event cho schedule item
        scheduleItem.addEventListener('click', function() {
            showScheduleDetails(schedule);
        });
        
        // Thêm vào cell
        cell.appendChild(scheduleItem);
    }
    
    // Hàm để xóa tất cả lịch hiện tại
    function clearAllSchedules() {
        const scheduleCells = document.querySelectorAll('.schedule-cell');
        scheduleCells.forEach(cell => {
            cell.innerHTML = '';
        });
    }
    
    // Hàm để hiển thị chi tiết lịch
    function showScheduleDetails(schedule) {
        const roomName = schedule.room?.roomName || `Room ${schedule.room?.id}`;
        const subjectName = schedule.subject?.subjectName || `Subject ${schedule.subject?.id}`;
        const className = schedule.classField?.className || `Class ${schedule.classField?.id}`;
        const startTime = formatTime(schedule.startTime || "00:00");
        const endTime = formatTime(schedule.endTime || "00:00");
        const mentorUsername = schedule.mentor?.userName || "N/A";
        const mentorName = schedule.mentor?.first_name && schedule.mentor?.last_name 
            ? `${schedule.mentor.first_name} ${schedule.mentor.last_name}`
            : mentorUsername;
        
        // Create modal for details display
        const modal = document.createElement('div');
        modal.className = 'schedule-detail-modal';
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100%';
        modal.style.height = '100%';
        modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        modal.style.display = 'flex';
        modal.style.justifyContent = 'center';
        modal.style.alignItems = 'center';
        modal.style.zIndex = '1000';
        
        // Create content
        const content = document.createElement('div');
        content.style.backgroundColor = 'white';
        content.style.padding = '20px';
        content.style.borderRadius = '8px';
        content.style.maxWidth = '400px';
        content.style.width = '100%';
        content.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
        
        // Create content HTML
        content.innerHTML = `
            <h3 style="margin-bottom: 16px; font-size: 18px; color: #1f2937;">Schedule Info</h3>
            <div style="margin-bottom: 12px;"><strong>Class:</strong> ${className}</div>
            <div style="margin-bottom: 12px;"><strong>Subject:</strong> ${subjectName}</div>
            <div style="margin-bottom: 12px;"><strong>Room:</strong> ${roomName}</div>
            <div style="margin-bottom: 12px;"><strong>Mentor:</strong> ${mentorUsername}</div>
            <div style="margin-bottom: 12px;"><strong>Time:</strong> ${startTime} - ${endTime}</div>
            <div style="text-align: right; margin-top: 20px;">
                <button id="close-detail-btn" style="padding: 8px 16px; background-color: #e5e7eb; border: none; border-radius: 6px; cursor: pointer;">Close</button>
            </div>
        `;
        
        modal.appendChild(content);
        document.body.appendChild(modal);
        
        // Handle close button
        document.getElementById('close-detail-btn').addEventListener('click', function() {
            document.body.removeChild(modal);
        });
        
        // Close when clicking outside
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                document.body.removeChild(modal);
            }
        });
    }

    // Hàm để lấy màu cho lớp học
    function getColorForClass(className, classId) {
        // Ưu tiên sử dụng classId nếu có
        if (classId) {
            return getColorById(classId);
        } 
        
        // Fallback sử dụng tên lớp nếu không có classId
        if (!className) return '#4f46e5'; // Màu mặc định nếu không có thông tin
        
        // Sử dụng hash tên lớp
        const hash = hashString(className);
        
        // Danh sách các màu đẹp
        const colors = getColorPalette();
        
        // Lấy màu từ danh sách dựa vào hash
        return colors[Math.abs(hash) % colors.length];
    }
    
    // Hàm lấy màu từ ID
    function getColorById(id) {
        if (!id) return '#4f46e5'; // Màu mặc định nếu không có ID
        
        // Danh sách các màu đẹp
        const colors = getColorPalette();
        
        // Lấy màu cố định từ ID, đảm bảo cùng ID luôn cùng màu
        return colors[id % colors.length];
    }
    
    // Hàm lấy bảng màu
    function getColorPalette() {
        return [
            "rgba(79, 70, 229, 0.6)",   // Indigo nhạt
            "rgba(239, 68, 68, 0.6)",   // Red nhạt
            "rgba(249, 115, 22, 0.6)",  // Orange nhạt
            "rgba(245, 158, 11, 0.6)",  // Amber nhạt
            "rgba(16, 185, 129, 0.6)",  // Emerald nhạt
            "rgba(59, 130, 246, 0.6)",  // Blue nhạt
            "rgba(139, 92, 246, 0.6)",  // Violet nhạt
            "rgba(236, 72, 153, 0.6)",  // Pink nhạt
            "rgba(20, 184, 166, 0.6)",  // Teal nhạt
            "rgba(99, 102, 241, 0.6)",  // Indigo variant nhạt
            "rgba(14, 165, 233, 0.6)",  // Sky nhạt
            "rgba(217, 70, 239, 0.6)"   // Fuchsia nhạt
        ];
    }
    
    // Hàm để tạo màu trong suốt từ mã màu HEX
    function createLighterColor(hexColor, opacity = 0.6) {
        // Chuyển đổi hex sang rgb
        let r = parseInt(hexColor.slice(1, 3), 16);
        let g = parseInt(hexColor.slice(3, 5), 16);
        let b = parseInt(hexColor.slice(5, 7), 16);
        
        // Làm nhạt màu bằng cách thêm trắng (255,255,255)
        r = Math.floor(r * 0.8 + 255 * 0.2);
        g = Math.floor(g * 0.8 + 255 * 0.2);
        b = Math.floor(b * 0.8 + 255 * 0.2);
        
        // Trả về dạng rgba
        return `rgba(${r}, ${g}, ${b}, ${opacity})`;
    }
    
    // Hàm tạo hash từ chuỗi
    function hashString(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            const char = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash;
    }
    
    // Hàm để chuyển đổi Date thành chuỗi YYYY-MM-DD
    function formatDateToString(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
    // Hàm hiển thị date picker để chọn ngày
    function showDatePicker() {
        // Tạo modal chọn ngày
        const modal = document.createElement('div');
        modal.className = 'date-picker-modal';
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100%';
        modal.style.height = '100%';
        modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        modal.style.display = 'flex';
        modal.style.justifyContent = 'center';
        modal.style.alignItems = 'center';
        modal.style.zIndex = '1000';
        
        // Tạo content
        const content = document.createElement('div');
        content.style.backgroundColor = 'white';
        content.style.padding = '20px';
        content.style.borderRadius = '8px';
        content.style.maxWidth = '400px';
        content.style.width = '100%';
        
        // Tạo tiêu đề
        const title = document.createElement('h3');
        title.textContent = 'Chọn ngày';
        title.style.marginBottom = '15px';
        title.style.fontWeight = '600';
        
        // Tạo input date
        const dateInput = document.createElement('input');
        dateInput.type = 'date';
        dateInput.style.width = '100%';
        dateInput.style.padding = '8px';
        dateInput.style.marginBottom = '15px';
        dateInput.style.borderRadius = '4px';
        dateInput.style.border = '1px solid #ddd';
        
        // Mặc định là ngày hiện tại
        const today = new Date();
        dateInput.value = formatDateToString(today);
        
        // Tạo button container
        const buttonContainer = document.createElement('div');
        buttonContainer.style.display = 'flex';
        buttonContainer.style.justifyContent = 'flex-end';
        buttonContainer.style.gap = '10px';
        
        // Tạo button hủy
        const cancelButton = document.createElement('button');
        cancelButton.textContent = 'Hủy';
        cancelButton.className = 'btn btn-outline';
        cancelButton.addEventListener('click', () => {
            document.body.removeChild(modal);
        });
        
        // Tạo button đồng ý
        const confirmButton = document.createElement('button');
        confirmButton.textContent = 'Chọn';
        confirmButton.className = 'btn btn-primary';
        confirmButton.addEventListener('click', () => {
            const selectedDate = new Date(dateInput.value);
            currentDisplayDate = selectedDate;
            updateCurrentDate(currentDisplayDate);
            fetchAndRenderSchedules(currentDisplayDate);
            document.body.removeChild(modal);
        });
        
        // Thêm các element vào modal
        buttonContainer.appendChild(cancelButton);
        buttonContainer.appendChild(confirmButton);
        
        content.appendChild(title);
        content.appendChild(dateInput);
        content.appendChild(buttonContainer);
        
        modal.appendChild(content);
        document.body.appendChild(modal);
    }

    
    function updateColorLegend() {
        // Nếu không có lịch, không cần hiển thị chú thích
        if (!schedules || schedules.length === 0) {
            return;
        }
        
        // Lấy danh sách lớp học duy nhất từ lịch
        const uniqueClasses = new Map();
        schedules.forEach(schedule => {
            const classId = schedule.classField?.id;
            if (!classId) return;
            
            const className = schedule.classField?.className || `Class ${classId}`;
            const key = `${classId}-${className}`;
            
            if (!uniqueClasses.has(key)) {
                uniqueClasses.set(key, {
                    id: classId,
                    name: className,
                    color: getColorById(classId)
                });
            }
        });
    }
});