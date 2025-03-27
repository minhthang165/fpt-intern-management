var stompClient = null;
var user_id = document.getElementById("user_id").value;
var user_avatar = document.getElementById("user_avatar").value;
var typeChat = "user";
var messages;
var conversationAvatar = null;
var conversationName = null;
var converstionType = null;
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
    var chatDropdown = document.getElementById('chatDropdown');
    var userList = document.getElementById('userList');
    var userSearchInput = document.getElementById('userSearchInput');
    var groupModal = document.getElementById('groupModal');
    var groupNameInput = document.getElementById('groupNameInput');
    var addPeopleInput = document.getElementById('addPeopleInput');
    var selectedUsers = document.getElementById('selectedUsers');
    var createGroupBtn = document.getElementById('createGroupBtn');
    groupModal.addEventListener('click', (event) => {
        if (event.target === groupModal) {
            groupModal.classList.add('hidden');
        }
    });
    document.addEventListener('click', function(event) {
        const isClickInside = chatDropdown.contains(event.target) || event.target.closest('.bg-blue-500');
        if (!isClickInside) {
            chatDropdown.classList.add('hidden');
        }
    });
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
                <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" data-id="${conversation.id}" data-conversationName="${conversation.conversationName}" data-conversation-type="${conversation.type}" data-conversationAvatar="${conversation.conversationAvatar}" onclick="setConversation(this)">
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
    document.getElementById("chat-container").innerHTML = ''; // Clear UI
    conversationId = null;
    conversationName = '';
    conversationAvatar = '';
    conversationMember = [];
    messages = [];
    conversationId = element.getAttribute('data-id');
    conversationName = element.getAttribute(`data-conversationName`);
    conversationAvatar = element.getAttribute(`data-conversationAvatar`);
    converstionType = element.getAttribute(`data-conversation-type`);
    selectedRecipientId = element.getAttribute('data-track-user-id');
    let existingConversation = conversation_list.find(conver => conver.conversationName === conversationName);
    if (existingConversation) {
        conversationId = existingConversation.id;
    }
    console.log("Thanh cong");
    // New chat section
    var rightSide = `

                        <!-- Conversation Header -->
                        <div class="flex items-center justify-between p-2 border-b">
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
                            <div class="p-2 rounded-full hover:bg-gray-100 cursor-pointer btn_conversation_information" onclick="toggleConversationInfo()">
                                <i class="fas fa-ellipsis-v text-gray-600"></i>
                            </div>
                        </div>
                        
                        <!-- Chat Messages -->
                        <div class="flex-grow space-y-4 overflow-y-auto p-4" id="chat" style="height: 400px;">
                            <!-- Messages will be loaded dynamically here -->
                        </div>
                        
                        <!-- Message Input -->
                        <div class="flex items-center space-x-2 p-3 border-t">
                            <label for="media-upload" class="p-2 rounded-full hover:bg-gray-100 cursor-pointer">
                                <i class="fas fa-paperclip text-gray-600 text-xl"></i>
                                <input id="media-upload" accept="*/*" multiple type="file" style="display: none;">
                            </label>
                            <button class="p-2 rounded-full hover:bg-gray-100">
                                <i class="fas fa-smile text-gray-600 text-xl"></i>
                            </button>
                            <div class="flex flex-col flex-grow relative">
                                <ul class="w-100 flex bottom-full list-file space-x-2 absolute"></ul>
                                <input class="flex-grow p-2 rounded-full bg-white border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500" id="message" placeholder="Enter Here" type="text"/>
                            </div>
                            <button class="p-2 rounded-full hover:bg-gray-100" onclick="conversationId ? sendMessage() : startNewConversation(this)">
                                <i class="fas fa-paper-plane text-blue-500 text-xl"></i>
                            </button>
                        </div>
    `;


    // Select the div and append the HTML
    document.getElementById("chat-container").innerHTML += rightSide;

    // If for temp conversation scenario
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
    var isMediaMessage = message.messageType.startsWith("image") || message.messageType.startsWith("video");
    if (message.messageType.startsWith("image")) {
        // Don't modify the original messageContent, create a formatted HTML instead
        message.messageContent = '<img class="w-full h-auto max-w-xs rounded" src="' + message.messageContent + '" alt="Image">';
    } else if (message.messageType.startsWith("video")) {
        message.messageContent = '<video class="w-full max-w-xs rounded" controls>' +
            '<source src="' + message.messageContent + '">' +
            'Your browser does not support the video tag.' +
            '</video>';
    }

    var messageContainerClass = isMediaMessage
        ? "message-text rounded-lg shadow-sm overflow-hidden" // No padding for media
        : "message-text p-2 rounded-lg shadow-sm";

    if (message.createdBy != user_id) {
        messageContainerClass += isMediaMessage ? "" : " bg-slate-200";
    } else {
        messageContainerClass += isMediaMessage ? "" : " bg-blue-500 text-white";
    }

    // Handle different message types
    if (message.createdBy != user_id) {
        // Message from other user
        msgDisplay += '<div class="flex items-start space-x-2 message">';
        imgSrc = message.sender.avatar_path;
        return msgDisplay
            + '<div class="message-img">'
            + '<img class="rounded-full" width="40px" src="' + imgSrc + '" alt="">'
            + '</div>'
            + '<div class="flex flex-col">'
            + '<span class="text-xs text-gray-600 font-medium mb-1">' + message.sender.first_name + " " + message.sender.last_name + '</span>'
            + '<div class="' + messageContainerClass + '">' + message.messageContent
            + '</div>'
            + '</div>'
            + '</div>';
    } else {
        // Message from current user
        msgDisplay += '<div class="flex items-start justify-end space-x-2 message">';
        imgSrc = user_avatar;
        return msgDisplay
            + '<div class="flex flex-col items-end">'
            + '<span class="text-xs text-gray-600 font-medium mb-1"> You </span>'
            + '<div class="' + messageContainerClass + '">' + message.messageContent
            + '</div>'
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

        await waitForImagesToLoad(chatContainer);

        scrollToLatestMessage();
    } catch (error) {
        console.error("Error loading messages:", error);
        chatContainer.innerHTML = '<div class="text-center p-4 text-red-500">Failed to load messages</div>';
    }
}

function waitForImagesToLoad(container) {
    const images = container.querySelectorAll("img");
    return Promise.all([...images].map(img =>
        img.complete ? Promise.resolve() : new Promise(resolve => img.onload = img.onerror = resolve)
    ));
}

function sendMessage() {

    var inputText = document.getElementById("message").value;
    if (inputText !== '') {
        sendText();
    } else {
        sendAttachments();
    }
    scrollToLatestMessage()
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
                        <div class="flex align-middle w-auto items-center p-2" data-track-user-id="${member.id}" data-conversationName="${member.first_name} ${member.last_name}" data-conversationAvatar="${member.avatar_path}" data-conversation-type="OneToOne" onclick="if (${member.id} != user_id) setConversation(this);">
                            <img class="w-14 rounded-full" src=${member.avatar_path} alt="">
                            <span class="ml-2"> if (${member.id} != user_id) ${member.first_name} ${member.last_name}</span>
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
        case 'startOneToOneChat':
            if (chatDropdown.classList.contains('hidden')) {
                chatDropdown.classList.remove('hidden');
                let userSearchInput = document.getElementById('userSearchInput');
                userSearchInput.focus();
            } else {
                chatDropdown.classList.add('hidden');
            }
            break;
        case 'startGroupChat' :
            groupModal.classList.remove('hidden');
            groupNameInput.focus();
            listUserAdd = [];
            //updateSelectedUsers();
            break;
    }
}

function toggleConversationInfo() {
    const infoBox = document.getElementById('conversation-info-box');
    const chatContainer = document.getElementById('chat-container');

    if (infoBox.classList.contains('hidden')) {
        infoBox.classList.remove('hidden');
        chatContainer.classList.add('w-[70%]'); // Reduce chat width
    } else {
        infoBox.classList.add('hidden');
        chatContainer.classList.remove('w-[70%]'); // Reset to full width
    }

    infoBox.innerHTML = '';
    var conversationInfoBox = `
                        <!-- Profile Section -->
                        <div class="flex flex-col items-center pt-4 pb-6 border-b">
                            <div class="relative">
                                <img alt="Profile picture" id="conversation-avatar" class="rounded-full mb-2 border-4 border-blue-100" height="80" src="${conversationAvatar}" width="80"/>
                                <div class="absolute bottom-2 right-0 bg-green-500 h-3 w-3 rounded-full border-2 border-white"></div>
                            </div>
                            <div class="text-center">
                                <p class="text-lg font-semibold">
                                    ${conversationName}
                                </p>
                                <p class="text-xs text-gray-500">
                                    Active now
                                </p>
                            </div>
                        </div>
                        
                        <!-- Chat Info Section -->
                        <div class="py-3 px-4 border-b">
                            <div class="flex justify-between items-center section-header p-2 rounded-lg cursor-pointer">
                                <p class="font-medium text-gray-700 text-sm">
                                    Chat Info
                                </p>
                                <i class="fas fa-chevron-down text-gray-500 text-xs"></i>
                            </div>
                            <div class="mt-2 pl-2">
                                <div class="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded-lg cursor-pointer">
                                    <i class="fas fa-thumbtack text-blue-500 text-sm"></i>
                                    <p class="text-xs">
                                        View pinned messages
                                    </p>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Customize Chat Section -->
                        <div class="py-3 px-4 border-b">
                            <div class="flex justify-between items-center section-header p-2 rounded-lg cursor-pointer">
                                <p class="font-medium text-gray-700 text-sm">
                                    Customize chat
                                </p>
                                <i class="fas fa-chevron-down text-gray-500 text-xs"></i>
                            </div>
                            <div class="mt-1 space-y-1 pl-2">
                                <div class="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded-lg cursor-pointer" onclick="toggleModal('changeName')">
                                    <i class="fas fa-pencil-alt text-blue-500 text-sm"></i>
                                    <p class="text-xs">
                                        Change chat name
                                    </p>
                                </div>
                                <div class="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded-lg cursor-pointer" onclick="document.getElementById('conversationAvatarModal').click()">
                                    <i class="fas fa-image text-blue-500 text-sm"></i>
                                    <p class="text-xs">
                                        Change photo
                                    </p>
                                    <input type="file" id="conversationAvatarModal" style="display:none;" onchange="changeConversationAvatar(event)">
                                </div>
                                <div class="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded-lg cursor-pointer">
                                    <i class="fas fa-circle text-blue-500 text-sm"></i>
                                    <p class="text-xs">
                                        Change theme
                                    </p>
                                </div>
                                <div class="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded-lg cursor-pointer">
                                    <i class="fas fa-thumbs-up text-blue-500 text-sm"></i>
                                    <p class="text-xs">
                                        Change emoji
                                    </p>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Chat Members Section -->
                        <div class="py-3 px-4 border-b">
                            <div class="flex justify-between items-center section-header p-2 rounded-lg cursor-pointer" onclick="toggleModal('showMembers')">
                                <p class="font-medium text-gray-700 text-sm">
                                    Chat members
                                </p>
                                <i class="fas fa-chevron-down text-gray-500 text-xs"></i>
                            </div>
                            <div id="chat-members" class="hidden mt-2 pl-2">
                                <!-- Members will be appended here -->
                            </div>
                        </div>
                        
                        <!-- Media Files Section -->
                        <div class="py-3 px-4 border-b">
                            <div class="flex justify-between items-center section-header p-2 rounded-lg cursor-pointer" onclick="toggleModal('showMediaFiles')">
                                <p class="font-medium text-gray-700 text-sm">
                                    Media, files and links
                                </p>
                                <i class="fas fa-chevron-down text-gray-500 text-xs"></i>
                            </div>
                            <div id="media-files" class="hidden mt-2 pl-2">
                                <!-- Sent media file will be loaded here -->
                            </div>
                        </div>
                    `

    infoBox.innerHTML += conversationInfoBox;
}

async function startNewConversation(element) {
    //CREATE NEW CONVERSATION ONLY IF CONVERSATION IS NOT HAVE IN DATABASE YET, ELSE SET CONVERSATION
    //FIRST MESSAGE, SET THE SENT BUTTON CALL CREATE NEW CONVERSATION AND ADD CONVERSATION USER
    //ELSE SENT LIKE NORMAL CONVERSATION
    let chatListContainer = document.querySelector(".space-y-4");
    let tempConversation = {
        conversation_name: conversationName,
        conversation_avatar: conversationAvatar,
        type: converstionType
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
    sendButton.onclick = function (event) {
        sendMessage();
    };
    await sendMessage();
}