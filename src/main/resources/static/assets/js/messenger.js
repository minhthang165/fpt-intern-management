var websocket = null;
var user_id = document.getElementById("user_id").value;
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
var listFile = [];
var typeFile = "image";
var deleteAttach = null;
var listUserAdd = [];
var listUserDelete = [];
var numberMember = 0;

document.addEventListener("DOMContentLoaded", async function () {
    await loadConversation(user_id);
});

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

function goLastestMsg() {
    var msgLiTags = document.querySelectorAll(".message");
    var last = msgLiTags[msgLiTags.length - 1];
    try {
        last.scrollIntoView();
    } catch (ex) {
    }
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
    console.log("Message object:", message); // Log the entire message object to see its structure

    var imgSrc = "";
    var msgDisplay = '';
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
        imgSrc = message.sender.avatar_path;
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
    var currentChatBox = document.getElementById("chat")
    try {
        let response = await fetch(`/api/message/get/` + conversationId)
        if (!response.ok) {
            throw new Error("Fetch messages failed")
        }
        var messages = await response.json();
        var chatbox = ""
        var imageCount = 0;
        var loadedImages = 0;
        messages.forEach(msg => {
            var messageHtml = customLoadMessage(msg);
            chatbox += messageHtml;
            var tempDiv = document.createElement("div");
            tempDiv.innerHTML = messageHtml;

            var imgs = tempDiv.getElementsByTagName("img");
            imageCount += imgs.length;

            for (let img of imgs) {
                img.onload = () => {
                    loadedImages++;
                    if (loadedImages === imageCount) {
                        goLastestMsg();
                    }
                };
                img.onerror = () => {
                    loadedImages++;
                    if (loadedImages === imageCount) {
                        goLastestMsg();
                    }
                };
            }
        })
        currentChatBox.innerHTML = chatbox;

        if (imageCount === 0) {
            goLastestMsg();
        }

    } catch (error) {
        console.log("/api/message/get/{conversation_id} Call failed")
    }
}

function handleResponsive() {
    back = document.querySelector(".back");
    rightSide = document.querySelector(".right-side");
    leftSide = document.querySelector(".left-side");

    if (back) {
        back.addEventListener("click", function () {
            rightSide.classList.remove("active");
            leftSide.classList.add("active");
            listFile = [];
            //renderFile();
        });
    }

    rightSide.classList.add("active");
    leftSide.classList.remove("active");

}

function sendMessage(e) {
    e.preventDefault();

    var inputText = document.getElementById("message").value;
    if (inputText != '') {
        sendText();
    } else {
        sendAttachments();
    }
    return false;
}

function sendText() {
    var messageContent = document.getElementById("message").value;

    document.getElementById("message").value = ''; // Clear input field

    if (messageContent.trim() === '') {
        console.log("Empty message. Not sending.");
        return; // Do not send empty messages
    }
    var message = buildMessageToDTO(messageContent, "text");
    if (websocket.readyState === WebSocket.OPEN) {
        websocket.send(JSON.stringify(message));
    } else {
        console.log("WebSocket is not open.");
    }
    printMessage(message);
}

function buildMessageToDTO(message, type) {
    return {
        senderId: message.senderId, // Ensure user_id is globally defined
        conversationId: message.conversationId,
        messageContent: message.messageContent,
        messageType: type,
    };
}

function printMessage(msg) {
    var currentChat = document.getElementById('chat');
    var newChatMsg = customLoadMessage(msg);
    currentChat.innerHTML += newChatMsg;
    goLastestMsg();
}

function setConversation(element) {
    conversationId = element.getAttribute('data-id');
    conversationName = element.getAttribute(`data-conversationName`);
    conversationAvatar = element.getAttribute(`data-conversationAvatar`);
    // New template HTML
    var rightSide = `
        <div class="flex items-center justify-between mb-4 right-side">
            <div class="flex items-center space-x-2">
                <img alt="User avatar" class="rounded-full" height="40" src="${conversationAvatar}" width="40"/>
                <div>
                    <div class="font-bold">
                        ${conversationName}
                    </div>
                    <div class="text-green-500 text-sm">
                        Đang hoạt động
                    </div>
                </div>
            </div>
            <i class="fas fa-ellipsis-v text-black">
            </i>
        </div>
        <div class="flex-grow space-y-4" id="chat">
            <!-- Messages will be loaded dynamically here -->
        </div>
        <div class="flex items-center space-x-2 mt-4">
            <i class="fas fa-paperclip text-black text-xl">
            </i>
            <i class="fas fa-smile text-black text-xl">
            </i>
            <input class="flex-grow p-2 rounded-full bg-white border border-gray-300" id="message" placeholder="Enter Here" type="text"/>
            <i class="fas fa-paper-plane text-black text-xl" onclick="sendMessage(event)">
            </i>
        </div>
    `;

    // Select the div and append the HTML
    document.querySelector('.flex.flex-col.flex-grow.bg-blue-200.p-4').innerHTML = rightSide;

    // After adding the HTML, load messages
    loadMessages(conversationId);

    handleResponsive();
}
