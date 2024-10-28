const loginForm = document.getElementById('loginForm');
const loginMessageDiv = document.getElementById('loginMessage');

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (response.ok) {
        const token = await response.text();
        localStorage.setItem('token', token);
        localStorage.setItem('username', username);
        window.location.href = 'chat.html';
    } else {
        const data = await response.text();
        loginMessageDiv.innerText = data;
    }
});
