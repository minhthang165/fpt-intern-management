// Function to handle form submission
async function createUser() {
    const userData = {
        first_name: document.getElementById('first_name').value,
        last_name: document.getElementById('last_name').value,
        gender: document.getElementById('gender').value.toUpperCase(),
        email: document.getElementById('email').value,
        phone_number: document.getElementById('phone_number').value,
        picture: document.querySelector('.profile-image').src,
        role: 'GUEST'
    };

    try {
        const response = await fetch('/api/user/users/create-employee', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (!response.ok) throw new Error('Network response was not ok');

        const result = await response.json();
        console.log('User created successfully:', result);

        // Redirect to user profile after creation
        window.location.href = `/profile/${result.id}`;

    } catch (error) {
        console.error('Error creating user:', error);
    }
}
document.querySelector('form').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the default form submission
    createUser();
});