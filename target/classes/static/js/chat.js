let stompClient = null;
const token = localStorage.getItem('token');
const currentUsername = localStorage.getItem('username');
let currentChatUser = null;
let chatHistory = {};

document.getElementById('logged-user').textContent = currentUsername;

function startChat() {
    if (!currentUsername || !token) {
        alert('Please log in first.');
        window.location.href = 'login.html';
        return;
    }

    const socket = new SockJS('/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({ 
        Authorization: 'Bearer ' + token 
    }, onConnected, onError);

    document.getElementById('chat').style.display = 'flex';
    document.getElementById('startButton').style.display = 'none';
}

function onConnected() {
    stompClient.subscribe(`/user/${currentUsername}/queue/messages`, onMessageReceived);
    stompClient.subscribe('/topic/onlineUsers', onOnlineUsersReceived);

    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({ sender: currentUsername, type: 'JOIN' })
    );
}

function onError(error) {
    console.error('WebSocket connection error:', error);
    alert('Could not connect to WebSocket server. Please refresh this page and try again!');
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    const otherUser = message.sender === currentUsername ? message.recipient : message.sender;
    
    if (!chatHistory[otherUser]) {
        chatHistory[otherUser] = [];
    }
    
    chatHistory[otherUser].push(message);

    if (currentChatUser === otherUser) {
        displayMessage(message);
    } else {
        highlightUserWithNewMessage(otherUser);
    }
}

function displayMessage(message) {
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');
    messageElement.classList.add(message.sender === currentUsername ? 'sent' : 'received');
    
	const formattedDate = message.timestamp ? new Date(message.timestamp).toLocaleString() : '';

	messageElement.innerHTML = `
	    ${message.content}
	    <div class="timestamp">${formattedDate}</div>
	`;
    
    const messagesDiv = document.getElementById('messages');
    messagesDiv.appendChild(messageElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function onOnlineUsersReceived(payload) {
    const users = JSON.parse(payload.body);
    const usersList = document.getElementById('users-list');
    usersList.innerHTML = '';
    
    users.forEach(user => {
        if (user !== currentUsername) {
            const userElement = document.createElement('div');
            userElement.classList.add('user-item');
            userElement.textContent = user;
            userElement.onclick = () => selectUser(user);
            usersList.appendChild(userElement);
        }
    });
}

function selectUser(username) {
    currentChatUser = username;
    
    document.querySelectorAll('.user-item').forEach(item => {
        item.classList.remove('active');
        if (item.textContent === username) {
            item.classList.add('active');
        }
    });

    document.getElementById('current-chat-user').textContent = username;
    document.getElementById('no-chat').style.display = 'none';
    document.getElementById('chat-container').style.display = 'flex';

    const messagesDiv = document.getElementById('messages');
    messagesDiv.innerHTML = '';

    if (chatHistory[username]) {
        chatHistory[username].forEach(message => displayMessage(message));
    }
}

function highlightUserWithNewMessage(username) {
    const userElements = document.querySelectorAll('.user-item');
    userElements.forEach(element => {
        if (element.textContent === username) {
            element.style.fontWeight = 'bold';
        }
    });
}

function sendMessage() {
    const messageInput = document.getElementById('message');
    const messageContent = messageInput.value.trim();

    if (!messageContent || !currentChatUser) {
        alert('Please select a recipient and type a message.');
        return;
    }

    const chatMessage = {
        sender: currentUsername,
        recipient: currentChatUser,
        content: messageContent,
        timestamp: new Date().toISOString()
    };

    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    messageInput.value = '';
}

document.getElementById('message').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});