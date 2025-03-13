var stompClient = null;
var user_id = document.getElementById("user_id").value;
var user_avatar = document.getElementById("user_avatar").value;
var typeChat = "user";
var conversationAvatar = null;
var conversationName = null;
var back = null;
var rightSide = null;
var leftSide = null;
var conversationId = null;
var attachFile = null;
var imageFile = null;
var file = null;
var fileList = [];
var typeFile = "image";
var deleteAttach = null;
var listUserAdd = [];
var listUserDelete = [];
var numberMember = 0;

document.addEventListener("DOMContentLoaded", async function () {
    await loadConversation(user_id);
    connectWebSocket();
});

function connectWebSocket() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/messenger', printMessage);
    });
}

async function loadConversation(user_id) {
    try {
        let response = await fetch(`/api/conversation-user/conversation/` + user_id);
        if (!response.ok) {
            throw new Error("Failed to fetch conversations");
        }
        let conversation_list = await response.json();
        renderHtmlConversation(conversation_list);
    } catch (error) {
        console.log("Error fetching conversation", error);
    }
}

function displayFiles() {
    imageFile = document.getElementById("media-upload");
    file = document.querySelector(".list-file");
    deleteAttach = document.querySelectorAll(".delete-attach");

    imageFile.addEventListener("change", function (e) {
        let filesImage = e.target.files;

        for (const file of filesImage) {
            fileList.push(file);
        }

        renderFile("image");

        this.value = null;
    });
}

function deleteFile(idx) {
    if (!isNaN(idx))
        fileList.splice(idx, 1);

    renderFile(typeFile);
}

function renderFile(typeFile) {
    let listFileHTML = "";
    let idx = 0;

    if (typeFile == "image") {
        for (const file of fileList) {
            listFileHTML += `
            <li class="relative">
                <img src="${URL.createObjectURL(file)}" alt="Image file" class="w-24 h-24 rounded-md">
                <span data-idx="${idx}" onclick="deleteFile(${idx})" 
                      class="absolute w-6 h-6 opacity-100 -right-1 top-0 text-center text-white bg-gray-800 rounded-full cursor-pointer">
                      X
                </span>
            </li>`;
            idx++;
        }
    } else {
        for (const file of fileList) {
            listFileHTML += '<li><div class="file-input">' + file.name
                + '</div><span data-idx="'
                + (idx) + '" onclick="deleteFile('
                + idx + ')" class="delete-attach">X</span></li>';
            idx++;
        }
    }


    if (fileList.length == 0) {
        file.innerHTML = "";
        file.classList.remove("active");
    } else {
        file.innerHTML = listFileHTML;
        file.classList.add("active");
    }

    deleteAttach = document.querySelectorAll(".delete-attach");
}


function scrollToLatestMessage() {
    requestAnimationFrame(() => {
        const chatContainer = document.getElementById("chat");
        if (!chatContainer) return;
        chatContainer.scrollTop = chatContainer.scrollHeight;
    });
}


function renderHtmlConversation(conversation_list) {
    let chatListContainer = document.querySelector(".space-y-4");
    chatListContainer.innerHTML = ""; // Clear existing content

    if (conversation_list.length > 0) {
        conversation_list.forEach(conversation => {
            let conversationHTML = `    
                <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" data-id="${conversation.id}" data-conversationName="${conversation.conversationName}" data-conversationAvatar="${conversation.conversationAvatar}" onclick="setConversation(this)">
                    <img alt="User avatar" class="rounded-full" height="40" src="${conversation.conversationAvatar || 'https://storage.googleapis.com/a1aa/image/P3mTDAXzCcHqcSIVZqLFhn31Oc6SJ-ZYT5fCH91vHJ4.jpg'}" width="40"/>
                    <div>
                        <div class="font-bold">${conversation.conversationName}</div>
                        <div class="text-gray-500 text-sm">${conversation.last_message || "No messages yet"}</div>
                    </div>
                </div>
            `;
            chatListContainer.innerHTML += conversationHTML;
        });
    } else {
        chatListContainer.innerHTML = "<p class='text-gray-500 text-center'>No conversations found</p>";
    }
}

function customLoadMessage(message) {
    var imgSrc = "";
    var msgDisplay = '';
    if (message.messageType.startsWith("image")) {
        message.messageContent = '<img src="' + message.messageContent + '" alt="">'
    } else if (message.messageType.startsWith("video")) {
        message.messageContent = '<video width="400" controls>'
            + '<source src="' + message.messageContent + '">'
            + '</video>';
    }

    if (message.createdBy != user_id) {
        msgDisplay += '<div class="flex items-start space-x-2 message">';
        imgSrc = message.sender.avatar_path;
        return msgDisplay
            + '<div class="message-img">'
            + '<img class="rounded-full" width="40px" src="' + imgSrc + '" alt="">'
            + '</div>'
            + '<div class="message-text bg-white p-2 rounded-lg">' + message.messageContent
            + '</div>'
            + '</div>';
    } else {
        msgDisplay += '<div class="flex items-end justify-end space-x-2 message">';
        imgSrc = user_avatar;
        return msgDisplay
            + '<div class="message-text bg-white p-2 rounded-lg">' + message.messageContent
            + '</div>'
            + '<div class="message-img">'
            + '<img class="rounded-full" width="40px" src="' + imgSrc + '" alt="">'
            + '</div>'
            + '</div>';
    }
}

async function loadMessages(conversationId) {
    const chatContainer = document.getElementById("chat");
    if (!chatContainer) return;

    try {
        chatContainer.innerHTML = '<div class="text-center p-4"><i class="fas fa-spinner fa-spin"></i> Loading messages...</div>';

        const response = await fetch(`/api/message/get/${conversationId}`);
        if (!response.ok) throw new Error(`HTTP error ${response.status}`);

        const messages = await response.json();

        chatContainer.innerHTML = messages.map(msg => customLoadMessage(msg)).join('');

        scrollToLatestMessage();
    } catch (error) {
        console.error("Error loading messages:", error);
        chatContainer.innerHTML = '<div class="text-center p-4 text-red-500">Failed to load messages</div>';
    }
}

function sendMessage(e) {
    e.preventDefault();

    var inputText = document.getElementById("message").value;
    if (inputText !== '') {
        sendText();
    } else {
        sendAttachments();
    }
    return false;
}

function buildMessageToDTO(messageContent, messageType) {
    return {
        conversationId: conversationId,
        messageContent: messageContent,
        createdBy: user_id,
        messageType: messageType
    }
}

async function sendAttachments() {
    for (file of fileList) {
        let formData = new FormData();
        formData.append('file', file);
        let fileData = await fetch("cloudinary/upload", {
            method: "POST",
            cache: 'no-cache',
            body: formData,
        }).then(fileData => fileData.json())
            .then(async data => {
                let message = buildMessageToDTO(data.url, file.type)
                let response = await fetch("api/message/post", {
                    method: "POST",
                    body: JSON.stringify(message),
                    headers: {
                        "Content-type": "application/json; charset=UTF-8"
                    }
                });
                let responseData = await response.json();
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(responseData));
            }).catch(error => {
                console.log(error);
            })
    }
}

async function sendText() {
    var messageContent = document.getElementById("message").value;
    var messageType = "text";
    document.getElementById("message").value = ''; // Clear input field
    if (messageContent.trim() === '') {
        console.log("Empty message. Not sending.");
        return; // Do not send empty messages
    }
    try {
        var message = buildMessageToDTO(messageContent, messageType);
        let response = await fetch("api/message/post", {
            method: "POST",
            body: JSON.stringify(message),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        });

        let responseData = await response.json();
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(responseData));
    } catch (error) {
        console.log(error);
    }
}

function printMessage(payload) {
    var message = JSON.parse(payload.body);
    var currentChat = document.getElementById('chat');
    var newChatMsg = customLoadMessage(message);
    currentChat.innerHTML += newChatMsg;
    scrollToLatestMessage();
}


function setConversation(element) {
    conversationId = element.getAttribute('data-id');
    conversationName = element.getAttribute(`data-conversationName`);
    conversationAvatar = element.getAttribute(`data-conversationAvatar`);
    // New template HTML
    var rightSide = `
    <div class="flex items-center justify-between p-2">
    <div class="flex items-center space-x-2">
        <img alt="User  avatar" class="rounded-full" height="40" src="${conversationAvatar}" width="40"/>
        <div>
            <div class="font-bold">
                ${conversationName}
            </div>
            <div class="text-green-500 text-sm">
                Đang hoạt động
            </div>
        </div>
    </div>
    <i class="fas fa-ellipsis-v text-black"></i>
</div>
<div class="flex-grow space-y-4 overflow-y-auto p-4" id="chat" style="height: 400px;"> <!-- Set a fixed height -->
    <!-- Messages will be loaded dynamically here -->
</div>
<div class="flex items-center space-x-2 mt-4 relative">
    <label for="media-upload">
        <i class="fas fa-paperclip text-black text-xl"></i>
        <input id="media-upload" accept="*/*" multiple type="file" style="display: none;">
    </label>
    <i class="fas fa-smile text-black text-xl"></i>
    <div class="flex flex-col flex-grow">
        <ul class="w-100 flex bottom-full list-file space-x-2 absolute"></ul>
        <input class="flex-grow p-2 rounded-full bg-white border border-gray-300" id="message" placeholder="Enter Here" type="text"/>
    </div>
    <i class="fas fa-paper-plane text-black text-xl" onclick="sendMessage(event)"></i>
</div>
`;


    // Select the div and append the HTML
    document.querySelector('.flex.flex-col.flex-grow.bg-blue-200').innerHTML = rightSide;

    // After adding the HTML, load messages
    loadMessages(conversationId);

    //Handle file upload
    displayFiles();
}
