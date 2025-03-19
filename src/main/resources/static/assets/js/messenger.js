var stompClient = null;
var user_id = document.getElementById("user_id").value;
var user_avatar = document.getElementById("user_avatar").value;
var typeChat = "user";
var messages;
var conversationAvatar = null;
var conversationName = null;
var conversationMember = {};
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
var selectedRecipientId = null;
var conversation_list = [];

document.addEventListener("DOMContentLoaded", async function () {
    await loadConversation(user_id);
    connectWebSocket();
});

//---------------------------------------- WEBSOCKET SETUP ----------------------------------------------

function connectWebSocket() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/messenger', printMessage);
    });
}

//---------------------------------------- CONVERSATION INTERACT (SET, LOAD) ------------------------------------------------
async function loadMember(conversationId) {
    try {
        let response = await fetch(`/api/conversation-user/get-users/` + conversationId);
        if (!response.ok) {
            throw new Error("No user found for conversation " + conversationId);
        }
        conversationMember = await response.json();
    } catch (error) {
        console.error("Error fetching conversation's member " + error);
    }
}

async function loadConversation(user_id) {
    try {
        let response = await fetch(`/api/conversation-user/conversation/` + user_id);
        if (!response.ok) {
            throw new Error("Failed to fetch conversations");
        }
        conversation_list = await response.json();
        renderHtmlConversation(conversation_list);
    } catch (error) {
        console.log("Error fetching conversation", error);
    }
}

function renderHtmlConversation(conversation_list) {
    let chatListContainer = document.querySelector(".space-y-4");
    chatListContainer.innerHTML = ""; // Clear existing content

    if (conversation_list.length > 0) {
        conversation_list.forEach(conversation => {
            let conversationHTML = `    
                <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" data-id="${conversation.id}" data-conversationName="${conversation.conversationName}" data-conversationAvatar="${conversation.conversationAvatar}" onclick="setConversation(this)">
                    <img alt="User avatar" id="conversation-avatar" class="rounded-full" height="40" src="${conversation.conversationAvatar || 'https://storage.googleapis.com/a1aa/image/P3mTDAXzCcHqcSIVZqLFhn31Oc6SJ-ZYT5fCH91vHJ4.jpg'}" width="40"/>
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

function setConversation(element) {
    document.getElementById('messengerBox').innerHTML = ''; // Clear UI
    conversationId = null;
    conversationName = '';
    conversationAvatar = '';
    conversationMember = [];
    messages = [];
    conversationId = element.getAttribute('data-id');
    conversationName = element.getAttribute(`data-conversationName`);
    conversationAvatar = element.getAttribute(`data-conversationAvatar`);
    selectedRecipientId = element.getAttribute('data-track-user-id');
    let existingConversation = conversation_list.find(conver => conver.conversationName === conversationName);
    if (existingConversation) {
        conversationId = existingConversation.id;
    }
    console.log("Thanh cong");
    // New chat section
    var rightSide = `
    <div class="flex flex-col flex-grow pb-1" data-id="${conversationId}">
        <div class="flex items-center justify-between p-2">
            <div class="flex items-center space-x-2">
                <img alt="User avatar" id="conversation-avatar" class="rounded-full" height="40" src="${conversationAvatar}" width="40"/>
                <div>
                    <div class="font-bold conversation-name">
                        ${conversationName}
                    </div>
                    <div class="text-green-500 text-sm">
                        Đang hoạt động
                    </div>
                </div>
            </div>
            <div class="p-5 btn_conversation_information" onclick="toggleConversationInfo()">
                <i class="fas fa-ellipsis-v text-black"></i>
            </div>
        </div>
        <div class="flex-grow space-y-4 overflow-y-auto p-4" id="chat" style="height: 400px;"> <!-- Set a fixed height -->
            <!-- Messages will be loaded dynamically here -->
        </div>
        <div class="flex items-center space-x-2 mb-1 mt-1 mr-3 relative">
            <label for="media-upload">
                <i class="fas fa-paperclip text-black text-xl"></i>
                <input id="media-upload" accept="*/*" multiple type="file" style="display: none;">
            </label>
            <i class="fas fa-smile text-black text-xl"></i>
            <div class="flex flex-col flex-grow">
                <ul class="w-100 flex bottom-full list-file space-x-2 absolute"></ul>
                <input class="flex-grow p-2 rounded-full bg-white border border-gray-300" id="message" placeholder="Enter Here" type="text"/>
            </div>
        <i class="fas fa-paper-plane text-black text-xl" onclick="conversationId ? sendMessage() : startNewConversation(this)"></i>
    </div>
</div>
`;


    // Select the div and append the HTML
    document.getElementById('messengerBox').innerHTML = rightSide;

    if (conversationId != null) {
        // After adding the HTML, load messages
        loadMessages(conversationId);
        // Load member in group (store at client's side)
        conversationMember = loadMember(conversationId);
    }

    //Handle file upload
    displayFiles();
}

//------------------------------------------ HTML RENDER HANDLE ----------------------------------------------

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

function printMessage(payload) {
    var message = JSON.parse(payload.body);
    var currentChat = document.getElementById('chat');
    var newChatMsg = customLoadMessage(message);
    currentChat.innerHTML += newChatMsg;
    scrollToLatestMessage();
}

function customLoadMessage(message) {
    var imgSrc = "";
    var msgDisplay = '';
    if (message.messageType.startsWith("image")) {
        message.messageContent = '<img class="w-full h-auto" src="' + message.messageContent + '" alt="">'
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

//--------------------------------------------- MESSAGE HANDLE (CRUD API CALL) -----------------------------------------

async function loadMessages(conversationId) {
    const chatContainer = document.getElementById("chat");
    if (!chatContainer) return;

    try {
        chatContainer.innerHTML = '<div class="text-center p-4"><i class="fas fa-spinner fa-spin"></i> Loading messages...</div>';

        const response = await fetch(`/api/message/get/${conversationId}`);
        if (!response.ok) throw new Error(`HTTP error ${response.status}`);

        messages = await response.json();

        chatContainer.innerHTML = messages.map(msg => customLoadMessage(msg)).join('');

        scrollToLatestMessage();
    } catch (error) {
        console.error("Error loading messages:", error);
        chatContainer.innerHTML = '<div class="text-center p-4 text-red-500">Failed to load messages</div>';
    }
}

function sendMessage() {

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

//--------------------------------- CONVERSATION MENU (ADD, DELETE, VIEW, MEMBER) --------------------------------------------

async function saveChatName() {
    let newName = document.getElementById("newChatName").value;
    var conversationDTO = {
        conversation_name: newName
    }
    let response = await fetch("/api/conversation/group/update/" + conversationId, {
        method: "PATCH",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(conversationDTO)
    })
    if (!response.ok) {
        console.error("Error saving conversation name.")
    } else {
        conversationName = newName;

        // Find and update the conversation name in the UI
        let conversationElement = document.querySelector(`[data-id="${conversationId}"] .font-bold`);
        if (conversationElement) {
            conversationElement.textContent = newName;
        }

        // Update the conversation name in the sidebar if necessary
        let nameElement = document.querySelector(".conversation-name");
        if (nameElement) {
            nameElement.textContent = newName;
        }
        document.getElementById("modal-overlay").remove();
    }
}

async function changeConversationAvatar(event) {
    var file = event.target.files[0];
    let formData = new FormData();
    formData.append('file', file);
    let fileData = await fetch("cloudinary/upload", {
        method: "POST",
        cache: 'no-cache',
        body: formData,
    }).then(fileData => fileData.json())
        .then(async data => {
            let conversationDTO = {
                conversation_avatar: data.url
            }
            let response = await fetch("/api/conversation/group/update/" + conversationId, {
                method: "PATCH",
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(conversationDTO)
            })
            if (!response.ok) {
                throw new Error("Error saving conversation avatar.")
            } else {
                let conversationImgElement = document.getElementById("conversation-avatar");
                if (conversationImgElement) {
                    conversationImgElement.forEach(img => {
                        img.src = data.url;
                    });
                }
            }
        })
}

function toggleModal(option) {
    switch (option) {
        case 'changeName':
            let modal = document.getElementById("modal-overlay");
            if (!modal) {
                modal = document.createElement("div");
                modal.id = "modal-overlay";
                modal.className = "fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50 z-50";
                modal.innerHTML = `
            <div class="bg-white p-5 rounded-lg shadow-lg w-1/3">
                <div class="flex justify-between items-center border-b pb-2">
                    <h2 class="text-lg font-bold">Change Chat Name</h2>
                    <button onclick="toggleModal('changeName')" class="text-gray-500">&times;</button>
                </div>
                <div class="mt-4">
                    <input type="text" id="newChatName" class="w-full p-2 border border-gray-300 rounded" placeholder="Enter new chat name">
                </div>
                <div class="mt-4 flex justify-end space-x-2">
                    <button onclick="toggleModal('changeName')" class="bg-gray-500 text-white px-4 py-2 rounded">Cancel</button>
                    <button onclick="saveChatName()" class="bg-blue-500 text-white px-4 py-2 rounded">Save</button>
                </div>
            </div>
        `;
                document.body.appendChild(modal);
            } else {
                modal.remove();
            }
            break;
            case 'showMembers':
                let membersContainer = document.getElementById("chat-members");
                membersContainer.innerHTML = "";
                conversationMember.forEach(member => {
                    let memberItem = `
                        <div class="flex align-middle w-auto items-center p-2" data-track-user-id="${member.id}" data-conversationName="${member.first_name} ${member.last_name}" data-conversationAvatar="${member.avatar_path}" onclick="if (${member.id} != user_id) setConversation(this);">
                            <img class="w-14 rounded-full" src=${member.avatar_path} alt="">
                            <span class="ml-2">${member.first_name} ${member.last_name}</span>
                        </div>
                        `
                    ;
                    membersContainer.innerHTML += memberItem;
                });

                membersContainer.classList.toggle("hidden");
                break;
        case 'showMediaFiles':
            let mediaContainer = document.getElementById("media-files");
            mediaContainer.innerHTML = "";
            // Add CSS class for controlled height
            messages.forEach(msg => {
                if (msg.messageType.toLowerCase() !== "text") {
                    let mediaItems = `
                <div class="w-40 p-1">
                    <div class="media-item">
                        ${msg.messageContent}
                    </div>
                </div>
            `;
                    mediaContainer.innerHTML += mediaItems;
                }
            });

            mediaContainer.classList.toggle("hidden");
            break;
    }
}

function toggleConversationInfo() {

    let container = document.querySelector('.flex.flex-grow.bg-blue-100.right-side.p-3');
    let existingBox = document.querySelector(".conversation-info-box");

    if (existingBox) {
        // If the info box exists, remove it (close)
        existingBox.remove();
    } else {
        var conversationInfoBox = `
            <div class="w-2/6 p-5 conversation-info-box overflow-y-auto">
           <div class="flex flex-col items-center">
            <img alt="Profile picture of a whale with a small fish" id="conversation-avatar" class="rounded-full mb-2" height="100" src="${conversationAvatar}" width="100"/>
            <div class="text-center">
             <p class="text-lg font-semibold">
              ${conversationName}
             </p>
             <p class="text-sm text-gray-400">
              Active now
             </p>
            </div>
           </div>
           <div class="mt-6">
            <div class="flex justify-between items-center">
             <p class="text-sm">
              Chat Info
             </p>
             <i class="fas fa-chevron-down">
             </i>
            </div>
            <div class="mt-2">
             <div class="flex items-center space-x-1">
              <i class="fas fa-thumbtack">
              </i>
              <p class="text-sm">
               View pinned messages
              </p>
             </div>
            </div>
           </div>
           <div class="mt-6">
            <div class="flex justify-between items-center p-1">
             <p class="text-sm">
              Customise chat
             </p>
             <i class="fas fa-chevron-down">
             </i>
            </div>
            <div class="mt-2 space-y-2">
             <div class="flex items-center space-x-2 p-1" onclick="toggleModal('changeName')">
              <i class="fas fa-pencil-alt">
              </i>
              <p class="text-sm">
               Change chat name
              </p>
             </div>
             <div class="flex items-center space-x-2 p-1" onclick="document.getElementById('conversationAvatarModal').click()">
              <i class="fas fa-image">
              </i>
              <p class="text-sm">
               Change photo
              </p>
              <input type="file" id="conversationAvatarModal" style="display:none;" onchange="changeConversationAvatar(event)">
             </div>
             <div class="flex items-center space-x-2 p-1">
              <i class="fas fa-circle text-blue-500">
              </i>
              <p class="text-sm">
               Change theme
              </p>
             </div>
             <div class="flex items-center space-x-2 p-1">
              <i class="fas fa-thumbs-up text-blue-500">
              </i>
              <p class="text-sm">
               Change emoji
              </p>
             </div>
            </div>
           </div>
           <div class="mt-6">
            <div>
                 <div>
                     <div class="flex justify-between items-center" onclick="toggleModal('showMembers')">
                        <p class="text-sm">
                            Chat members
                        </p>
                        <i class="fas fa-chevron-down">
                        </i>
                     </div>
                     <div id="chat-members" class="hidden mt-3 flex flex-col">
                        <!-- Members will be appended here -->
                     </div>                    
                 </div>   
            </div>
           </div>
           <div class="mt-6">
           <div onclick="toggleModal('showMediaFiles')">
           <div>
            <div class="flex justify-between items-center">
             <p class="text-sm">
              Media, files and links
             </p>
             <i class="fas fa-chevron-down">
             </i>
             </div>
             <div id="media-files" class="hidden mt-3 flex flex-col flex-grow">
                <!-- Sent media file will be loaded here -->
             </div>
             </div>
            </div>`
        container.innerHTML += conversationInfoBox;
    }
}

async function startNewConversation(element) {
    //CREATE NEW CONVERSATION ONLY IF CONVERSATION IS NOT HAVE IN DATABASE YET, ELSE SET CONVERSATION
    //FIRST MESSAGE, SET THE SENT BUTTON CALL CREATE NEW CONVERSATION AND ADD CONVERSATION USER
    //ELSE SENT LIKE NORMAL CONVERSATION
    let chatListContainer = document.querySelector(".space-y-4");
    let tempConversation = {
        conversation_name: conversationName,
        conversation_avatar: conversationAvatar
    };

    let response = await fetch('/api/conversation/group/create', {
        method: 'POST',
        body: JSON.stringify(tempConversation),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    });

    if (!response.ok) {
        console.error("Failed to create conversation");
        return;
    }

    let conversationData = await response.json();
    conversationId = conversationData.id;

    // Step 2: Add current user to the conversation
    await fetch('/api/conversation-user/add-user', {
        method: 'POST',
        body: JSON.stringify({
            conversation_id: conversationId,
            user_id: user_id,
            admin: false
        }),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    });

    // Step 3: Add the clicked user to the conversation
    await fetch('/api/conversation-user/add-user', {
        method: 'POST',
        body: JSON.stringify({
            conversation_id: conversationId,
            user_id: selectedRecipientId,
            admin: false
        }),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    })
    ;

    let conversationHTML = `    
                <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" data-id="${conversationData.id}" data-conversationName="${conversationData.conversationName}" data-conversationAvatar="${conversationData.conversationAvatar}" onclick="setConversation(this)">
                    <img alt="User avatar" id="conversation-avatar" class="rounded-full" height="40" src="${conversationData.conversationAvatar || 'https://storage.googleapis.com/a1aa/image/P3mTDAXzCcHqcSIVZqLFhn31Oc6SJ-ZYT5fCH91vHJ4.jpg'}" width="40"/>
                    <div>
                        <div class="font-bold">${conversationData.conversationName}</div>
                        <div class="text-gray-500 text-sm">${conversationData.last_message || "No messages yet"}</div>
                    </div>
                </div>
            `;
    chatListContainer.innerHTML += conversationHTML;

    // Step 4: Update the send button to send messages normally
    let sendButton = document.querySelector(".fa-paper-plane");
    sendButton.onclick = function(event) {
        sendMessage();
    };
    await sendMessage();
}