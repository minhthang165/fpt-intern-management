// Function to handle form submission
async function createUser() {
    // Gather user data from form inputs
    const userData = {
        first_name: document.getElementById('first_name').value,
        last_name: document.getElementById('last_name').value,
        gender: document.getElementById('gender').value.toUpperCase(),
        email: document.getElementById('email').value, // Read-only field
        phone_number: document.getElementById('phone_number').value,
        picture: document.querySelector('.profile-image').src,
        role: 'GUEST'
    };
    try {
        const response = await fetch('/api/user/users/create-employee', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            throw new Error('Network response was not ok ' + response.statusText);
        }

        const result = await response.json();
        console.log('User  updated successfully:', result);
        // Optionally, you can redirect or update the UI here
    } catch (error) {
        console.error('Error updating user:', error);
        // Handle error (e.g., show a message to the user)
    }
}

// Example of attaching the function to a form submission
document.querySelector('form').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the default form submission
    createUser();
});