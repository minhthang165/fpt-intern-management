document.addEventListener("DOMContentLoaded", async function () {
    const id = document.getElementById("user_id").value; // Get the user ID

    try {
        const response = await fetch(`/api/user/users/id/` + id); // Fetch user data
        if (!response.ok) throw new Error("User not found");

        const user = await response.json(); // Convert response to JSON

        // Insert user data into the HTML
        document.getElementById("picture").src = user.avatar_path;
        document.getElementById("full_name").textContent = `${user.first_name} ${user.last_name}`;
        document.getElementById("email").textContent = user.email;
        document.getElementById("phone").textContent = user.phone_number;
        document.getElementById("gender").textContent = user.gender;
        document.getElementById("role").textContent = user.role;

    } catch (error) {
        console.error("Error fetching user:", error);
        document.getElementById("error").textContent = "User not found";
    }
});
