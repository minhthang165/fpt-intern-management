document.addEventListener("DOMContentLoaded", async function () {
    const id = document.getElementById("user_id").value; // Get the user ID

    try {
        const response = await fetch(`/api/user/` + id); // Fetch user data
        if (!response.ok) throw new Error("User not found");

        const user = await response.json(); // Convert response to JSON

        // Assign values to input fields
        document.getElementById("picture").src = user.avatar_path || "/placeholder.svg?height=120&width=120";
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

let newAvatarUrl = null; // Store uploaded image URL

async function updateAvatar(event) {
    const file = event.target.files[0];
    if (!file) return;

    let formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch(`cloudinary/upload`, {
            method: 'POST',
            cache: 'no-cache',
            body: formData,
        });

        if (!response.ok) throw new Error("File upload failed");

        const data = await response.json();
        newAvatarUrl = data.url; // Store new avatar URL

        await updateUser();

        // Update the displayed profile picture
        document.getElementById("picture").src = newAvatarUrl;

    } catch (error) {
        console.error("Error uploading file:", error);
    }
}

// Function to handle form submission
async function updateUser() {
    const finalAvatarUrl = newAvatarUrl ? newAvatarUrl : document.getElementById("picture").src; // Use new or existing avatar
    const userData = {
        first_name: document.getElementById('first_name').value,
        last_name: document.getElementById('last_name').value,
        gender: document.getElementById('gender').value.toUpperCase(),
        email: document.getElementById('email').value,
        phone_number: document.getElementById('phone_number').value,
        avatar_path: finalAvatarUrl, // Set avatar
    };

    console.log(userData);

    try {
        const response = await fetch('/api/user/update/' + document.getElementById("user_id").value, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (!response.ok) throw new Error('Network response was not ok');

        const result = await response.json();
        console.log('User updated successfully:', result);

        // Redirect to user profile after update
        window.location.href="/profile"
    } catch (error) {
        console.error('Error updating user:', error);
    }
}

document.querySelector('form').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the default form submission
    updateUser();
});
