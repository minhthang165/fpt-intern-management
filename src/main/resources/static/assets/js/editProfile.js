document.addEventListener("DOMContentLoaded", async function () {
    const id = document.getElementById("user_id").value; // Get the user ID

    try {
        const response = await fetch(`/api/user/users/id/` + id); // Fetch user data
        if (!response.ok) throw new Error("User not found");

        const user = await response.json(); // Convert response to JSON

        // Correctly assign values to input fields
        document.getElementById("picture").src = user.avatar_path;
        document.getElementById("first_name").value = user.first_name;
        document.getElementById("last_name").value = user.last_name;
        document.getElementById("email").value = user.email;
        document.getElementById("phone_number").value = user.phone_number;
        document.getElementById("gender").value = user.gender;

    } catch (error) {
        console.error("Error fetching user:", error);
        document.getElementById("error").textContent = "User not found";
    }
});



// Function to handle form submission
async function updateUser() {
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
        const response = await fetch('/api/user/users/update/' + document.getElementById("user_id").value, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (!response.ok) throw new Error('Network response was not ok');

        const result = await response.json();
        console.log('User created successfully:', result);

        // Redirect to user profile after creation
        window.location.href = `/profile`;


    } catch (error) {
        console.error('Error creating user:', error);
    }
}
document.querySelector('form').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the default form submission
    updateUser();
});
