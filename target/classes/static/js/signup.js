const signupForm = document.getElementById('signupForm');
const signupMessageDiv = document.getElementById('signupMessage');

signupForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const username = document.getElementById('signupUsername').value;
    const password = document.getElementById('signupPassword').value;

    const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (response.ok) {
        window.location.href = 'login.html';
    } else {
        const data = await response.text();
        signupMessageDiv.innerText = data;
    }
});