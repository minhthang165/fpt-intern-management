
document.addEventListener('DOMContentLoaded', function() {
// Get all classroom cards
const classroomCards = document.querySelectorAll('.sidebar-card');
let currentlySelectedId = null;

// Add click event to each card
classroomCards.forEach(card => {
card.addEventListener('click', function() {
const classId = this.getAttribute('data-id');
const className = this.getAttribute('data-class');
const username = this.getAttribute('data-user');
const time = this.getAttribute('data-time');
const date = this.getAttribute('data-date');

// Check if this card is already selected
if (currentlySelectedId === classId) {
// If already selected, deselect it and hide details
this.classList.remove('active');
hideDetails();
currentlySelectedId = null;
} else {
// If not selected, select it and show details
classroomCards.forEach(c => c.classList.remove('active'));
this.classList.add('active');
currentlySelectedId = classId;

// Only load details if we have a class name
if (className) {
loadClassroomDetails(classId, className, username, time, date);
}
}
});
});

function hideDetails() {
// Reset details container to placeholder
document.getElementById('detailsContainer').innerHTML = `
                <div class="detail-placeholder">
                    <p>Chọn một lớp học để xem chi tiết</p>
                </div>
            `;

// Clear chat container
document.getElementById('chatContainer').innerHTML = '';
}
});

function loadClassroomDetails(classId, className, username, time, date) {
// In a real application, you would fetch data from an API here
// For now, we'll use the template with the data we have

// Load details
const detailsContainer = document.getElementById('detailsContainer');
const detailsTemplate = document.getElementById('detailsTemplate').innerHTML;

// Replace placeholders with actual data
let detailsHtml = detailsTemplate
.replace('{className}', className)
.replace('{time}', time)
.replace('{date}', date);

detailsContainer.innerHTML = detailsHtml;

// Load chat
const chatContainer = document.getElementById('chatContainer');
const chatTemplate = document.getElementById('chatTemplate').innerHTML;

// Replace placeholders with actual data
let chatHtml = chatTemplate.replace('{username}', username || 'system');

chatContainer.innerHTML = chatHtml;

// In a real application, you would make an API call like this:
/*
fetch(`/api/classrooms/${classId}`)
    .then(response => response.json())
    .then(data => {
        // Update UI with the fetched data
        // ...
    })
    .catch(error => {
        console.error('Error fetching classroom details:', error);
    });
*/
}