var user_fullName = document.getElementById("user_fullName").value;
var user_id = document.getElementById("user_id").value;
var user_avatar = document.getElementById("user_avatar").value;
var messages;
var conversationAvatar = null;
var conversationName = null;
var conversationType = null;
var conversationMember = {};
var back = null;
var conversationId = null;
var attachFile = null;
var imageFile = null;
var file = null;
var fileList = [];
var typeFile = "image";
var deleteAttach = null;
var listUserAdd = [];
var selectedRecipientId = null;
var conversation_list = [];
let originalConversationList = []; // Lưu trữ danh sách conversation gốc
var isCurrentUserAdmin = false; // Thêm biến global mới
window.userId = user_id; // Gắn vào window
localStorage.setItem("userId", user_id);
console.log("✅ User ID:", window.userId);

document.addEventListener("DOMContentLoaded", async function () {
    await loadConversation(user_id);
    connectWebSocket();
    adjustHeight();
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
    document.addEventListener('click', function (event) {
        const isClickInside = chatDropdown.contains(event.target) || event.target.closest('.text-black');
        if (!isClickInside) {
            chatDropdown.classList.add('hidden');
        }
    });
    const urlParams = new URLSearchParams(window.location.search);
    const conversationIdFromUrl = urlParams.get('conversationId');

    if (conversationIdFromUrl) {
        // Tìm conversation element tương ứng
        const conversationElement = document.querySelector(`[data-id="${conversationIdFromUrl}"]`);
        if (conversationElement) {
            // Mở conversation
            setConversation(conversationElement);
        }
        // Xóa conversationId khỏi URL
        window.history.replaceState({}, document.title, "/messenger");
    }
});

//---------------------------------------- WEBSOCKET SETUP ----------------------------------------------

function connectWebSocket() {
    if (stompClient && stompClient.connected) {
        stompClient.subscribe(`/topic/messenger/${user_id}`, handleWebsocketPayload);
    } else {
        const waitForConnect = setInterval(() => {
            if (stompClient && stompClient.connected) {
                stompClient.subscribe(`/topic/messenger/${user_id}`, handleWebsocketPayload);
                clearInterval(waitForConnect);
            }
        }, 100);
    }
}

//---------------------------------------- Call SETUP ----------------------------------------------

window.makeCall = () => {
    if (!conversationId) {
        alert("Please select a conversation first!");
        return;
    }

    if (!conversationMember || conversationMember.length === 0) {
        alert("No members found in this conversation!");
        console.error("Conversation members not loaded:", conversationMember);
        return;
    }

    // Lấy danh sách receiverIds từ conversationMember, loại bỏ user_id của người hiện tại
    const receiverIds = conversationMember
        .filter(member => member.user.id != user_id)
        .map(member => member.user.id);

    if (receiverIds.length === 0) {
        alert("No other members to call in this conversation!");
        return;
    }

    console.log("✅ Initiating call with receiverIds:", receiverIds);
    startCall(receiverIds);
};

const startCall = (receiverIds) => {
    if (!receiverIds || receiverIds.length === 0) {
        alert("Invalid Receiver IDs!");
        return;
    }

    const roomID = Math.floor(Math.random() * 10000) + ""; // Tạo roomID duy nhất cho toàn bộ cuộc gọi
    localStorage.setItem("roomID", roomID);

    // Gửi yêu cầu gọi đến từng receiverId
    const callPromises = receiverIds.map(receiverId => {
        return fetch(`/calls/create/${window.userId}/${receiverId}/${roomID}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        })
            .then(response => response.json())
            .then(data => {
                if (data.roomID) {
                    console.log(`✅ Call request sent to ${receiverId} with roomID: ${data.roomID}`);
                    return { success: true, receiverId };
                } else {
                    console.warn(`❌ Receiver ${receiverId} is busy or unavailable`);
                    return { success: false, receiverId };
                }
            })
            .catch(error => {
                console.error(`❌ Error sending call to ${receiverId}:`, error);
                return { success: false, receiverId };
            });
    });

    // Chờ tất cả các yêu cầu hoàn tất
    Promise.all(callPromises)
        .then(results => {
            const successfulCalls = results.filter(result => result.success);
            if (successfulCalls.length > 0) {
                // Nếu ít nhất một yêu cầu thành công, mở tab video call
                window.open(`/video-call.html?room=${roomID}`, '_blank');
            } else {
                alert("All receivers are busy or unavailable!");
            }
        })
        .catch(error => {
            console.error("Error processing call requests:", error);
            alert("An error occurred while initiating the call.");
        });
};
function logAllMemberIds(members) {
    if (!members || members.length === 0) {
        console.log("⚠️ No members to log");
        return;
    }

    const memberIds = members.map(member => member.user.id);
    console.log("📋 All member IDs in conversation:", memberIds);
}

//---------------------------------------- CONVERSATION INTERACT (SET, LOAD) ------------------------------------------------
async function loadMember(conversationId, conversationType) {
    try {
        let response = await fetch(`/api/conversation-user/get-users/` + conversationId);
        if (!response.ok) {
            throw new Error("No user found for conversation " + conversationId);
        }
        conversationMember = await response.json();
        console.log("✅ Loaded members for conversation", conversationId, ":", conversationMember);
        console.log("✅ Current user_id:", user_id);

        // Kiểm tra nếu là hội thoại 1:1 và có ít nhất 2 thành viên
        if (conversationType === "OneToOne" && conversationMember.length >= 2) {
            // Sử dụng user_id thay vì userId để đảm bảo đúng người dùng hiện tại
            const otherMember = conversationMember.find(member => member.id != user_id);

            if (otherMember) {
                window.receiverId = otherMember.id;
                console.log("✅ Receiver ID set to:", window.receiverId);
            } else {
                console.error("❌ Could not find the other member in this conversation");
            }
        }
    } catch (error) {
        console.error("❌ Lỗi khi tải danh sách thành viên:", error);
    }
}

async function loadConversation(user_id) {
    try {
        let response = await fetch(`/api/conversation-user/conversation/` + user_id);
        if (!response.ok) {
            throw new Error("Failed to fetch conversations");
        }
        conversation_list = await response.json();
        originalConversationList = [...conversation_list]; // Lưu bản sao
        renderHtmlConversation(conversation_list);
    } catch (error) {
        console.log("Error fetching conversation", error);
    }
}

function renderHtmlConversation(conversation_list) {
    let chatListContainer = document.querySelector(".space-y-4");
    chatListContainer.innerHTML = "";

    if (conversation_list.length > 0) {
        conversation_list.forEach(conversation => {
            let conversationHTML = `    
                <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" 
                     data-id="${conversation.id}" 
                     data-conversationName="${conversation.conversationName.replace(/<[^>]*>/g, '')}" 
                     data-conversation-type="${conversation.type}" 
                     data-conversationAvatar="${conversation.conversationAvatar}" 
                     onclick="setConversation(this)">
                    <img alt="User avatar" 
                         id="conversation-avatar" 
                         class="rounded-full" 
                         height="40" 
                         src="${conversation.conversationAvatar || 'https://storage.googleapis.com/a1aa/image/P3mTDAXzCcHqcSIVZqLFhn31Oc6SJ-ZYT5fCH91vHJ4.jpg'}" 
                         width="40"/>
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
    document.getElementById('conversation-info-box').classList.add('hidden');
    document.getElementById('chat-container').classList.remove('w-[70%]'); // Reset to full width

    conversationId = element.getAttribute('data-id');
    conversationName = element.getAttribute('data-conversationName');
    conversationAvatar = element.getAttribute('data-conversationAvatar');
    conversationType = element.getAttribute('data-conversation-type'); // Sửa lỗi chính tả
    selectedRecipientId = element.getAttribute('data-track-user-id');

    let existingConversation = conversation_list.find(conver => conver.conversationName === conversationName);
    if (existingConversation) {
        conversationId = existingConversation.id;
    }

    var rightSide = `
        <!-- Conversation Header -->
        <div class="flex items-center justify-between p-2 border-b">
            <div class="flex items-center space-x-2">
                <img alt="User avatar" id="conversation-avatar" class="rounded-full" height="40" src="${conversationAvatar || 'https://storage.googleapis.com/a1aa/image/P3mTDAXzCcHqcSIVZqLFhn31Oc6SJ-ZYT5fCH91vHJ4.jpg'}" width="40"/>
                <div>
                    <div class="font-bold conversation-name">${conversationName}</div>
                    <div class="text-green-500 text-sm">Online</div>
                </div>
            </div>
            <div class="flex items-center space-x-2">
                <!-- Call Button -->
                <div class="p-2 rounded-full hover:bg-gray-100 cursor-pointer btn_call">
                    <i class="fas fa-phone text-gray-600"></i>
                </div>
                <!-- Info Button -->
                <div class="p-2 rounded-full hover:bg-gray-100 cursor-pointer btn_conversation_information" onclick="toggleConversationInfo('${conversationType}')">
                    <i class="fas fa-ellipsis-v text-gray-600"></i>
                </div>
            </div>
        </div>
        <!-- Chat Messages -->
        <div class="flex-grow space-y-4 overflow-y-auto p-4" id="chat" style="height: 400px;"></div>
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

    document.getElementById("chat-container").innerHTML += rightSide;

    // Add event listener for Enter key on message input
    document.getElementById("message").addEventListener("keypress", function(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            if (conversationId) {
                sendMessage();
            } else {
                startNewConversation(document.querySelector(".fa-paper-plane").parentElement);
            }
        }
    });

    if (conversationId) {
        loadMessages(conversationId);
        loadMember(conversationId,conversationType);
    }

    const callButton = document.querySelector(".btn_call");
    if (conversationType === "OneToOne") { // Sửa lỗi chính tả
        callButton.addEventListener("click", window.makeCall);
    } else {
        callButton.addEventListener("click", () => alert("Please select a one-to-one conversation"));
    }

    // Handle file upload
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

        renderFile(typeFile);

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
    const chatContainer = document.getElementById("chat");
    if (!chatContainer) return;
    chatContainer.scrollTo({
        top: chatContainer.scrollHeight,
        behavior: "smooth"
    });
}

function handleWebsocketPayload(payload) {
    var message = JSON.parse(payload.body);

    // Xử lý theo loại sự kiện
    switch(message.type) {
        case "REMOVE_USER":
            // Xử lý khi người dùng bị đá, out khỏi cuộc trò chuyện
            if (message.userId == user_id) {
                // Nếu chính người dùng hiện tại bị đá
                removeConversationFromList(message.conversationId);
                // Nếu đang xem cuộc trò chuyện đó, xóa nội dung
                if (conversationId === message.conversationId) {
                    document.getElementById("chat-container").innerHTML = '<div class="text-center p-4">Bạn đã bị xóa khỏi cuộc trò chuyện này</div>';
                    conversationId = null;
                }
            } else {
                const userElement = document.querySelector(`[data-track-user-id="${message.userId}"]`);
                if (userElement) {
                    const parentDiv = userElement.closest('.flex.items-center.p-3.hover\\:bg-gray-50.rounded-lg.group');
                    if (parentDiv) {
                        parentDiv.remove();
                    }
                }
            }
            break;

        case "ADD_USER":
            // Xử lý khi người dùng được thêm vào cuộc trò chuyện
            if (message.userId == user_id) {
                // Thêm cuộc trò chuyện mới vào danh sách
                addNewConversationToList(message.conversation);
            }
            break;

        default:
            // Xử lý tin nhắn thông thường
            if (!message.conversation) return;

            // Xử lý tên hiển thị cho cuộc trò chuyện one-to-one
            if (message.conversation.type === "OneToOne") {
                const conversationName = message.conversation.conversationName;
                if (conversationName.includes(" - ")) {
                    const nameParts = conversationName.split(" - ");
                    // Nếu người dùng hiện tại là người gửi tin nhắn
                    if (message.createdBy == user_id) {
                        // Hiển thị tên người nhận (phần sau dấu "-")
                        message.conversation.conversationName = nameParts[1];
                    } else {
                        // Nếu người dùng hiện tại là người nhận
                        // Hiển thị tên người gửi (phần trước dấu "-")
                        message.conversation.conversationName = nameParts[0];
                    }
                }
            }

            // Kiểm tra xem cuộc trò chuyện đã tồn tại trong danh sách chưa
            const existingConversationIndex = conversation_list.findIndex(conv => conv.id === message.conversation.id);

            if (existingConversationIndex === -1) {
                // Nếu chưa tồn tại, thêm vào đầu danh sách
                conversation_list.unshift(message.conversation);
                originalConversationList.unshift(message.conversation);
            } else {
                // Nếu đã tồn tại, di chuyển nó lên đầu danh sách
                const conversation = conversation_list[existingConversationIndex];
                conversation_list.splice(existingConversationIndex, 1);
                conversation_list.unshift(conversation);

                // Cập nhật thông tin cuộc trò chuyện
                conversation.last_message = message.messageContent;
            }

            // Cập nhật UI
            renderHtmlConversation(conversation_list);

            // Làm nổi bật tên cuộc trò chuyện
            const conversationElement = document.querySelector(`[data-id="${message.conversation.id}"] .font-bold`);
            if (conversationElement) {
                conversationElement.classList.add('text-blue-600');
                // Tự động bỏ nổi bật sau 3 giây
                setTimeout(() => {
                    conversationElement.classList.remove('text-blue-600');
                }, 3000);
            }

            // Nếu là cuộc trò chuyện hiện tại, hiển thị tin nhắn
            if (message.conversation.id === conversationId) {
                displayMessage(message);
            }
    }
}

// Thêm hàm để xóa cuộc trò chuyện khỏi danh sách
function removeConversationFromList(conversationIdToRemove) {
    // Lọc ra cuộc trò chuyện cần xóa
    conversation_list = conversation_list.filter(conv => conv.id !== conversationIdToRemove);
    originalConversationList = originalConversationList.filter(conv => conv.id !== conversationIdToRemove);

    // Cập nhật UI
    renderHtmlConversation(conversation_list);
}

// Thêm hàm để thêm cuộc trò chuyện mới vào danh sách
function addNewConversationToList(conversation) {
    // Kiểm tra nếu cuộc trò chuyện đã tồn tại
    if (conversation_list.some(conv => conv.id === conversation.id)) {
        return;
    }

    // Tạo một đối tượng conversation mới
    const newConversation = {
        id: conversation.id,
        conversationName: conversation.conversationName,
        conversationAvatar: conversation.conversationAvatar,
        last_message: "You have been added to the group",
        type: conversation.type
    };
    console.log(newConversation);

    // Thêm vào đầu danh sách
    conversation_list.unshift(newConversation);
    originalConversationList.unshift(newConversation);

    // Cập nhật UI
    renderHtmlConversation(conversation_list);
}

function handleNewConversation(message) {
    // Lấy tên cuộc trò chuyện từ message
    let conversationName = message.conversation.conversationName;
    let displayName = conversationName;

    // Xử lý tên cuộc trò chuyện dành cho OneToOne chat
    if (message.conversation.type == "OneToOne" && conversationName.includes(" - ")) {
        const nameParts = conversationName.split(" - ");

        // Nếu người dùng hiện tại là người gửi tin nhắn
        if (message.createdBy == user_id) {
            // Hiển thị tên người nhận (phần sau dấu "-")
            displayName = nameParts[1];
        } else {
            // Nếu người dùng hiện tại là người nhận
            // Hiển thị tên người gửi (phần trước dấu "-")
            displayName = nameParts[0];
        }
    }

    // Tạo một đối tượng conversation mới
    const newConversation = {
        id: message.conversation.id,
        conversationName: displayName, // Sử dụng tên hiển thị đã được xử lý
        conversationAvatar: message.conversation.conversationAvatar,
        last_message: message.messageContent,
        type: message.conversation.type
    };

    // Thêm vào đầu danh sách
    conversation_list.unshift(newConversation);
    originalConversationList.unshift(newConversation);

    // Cập nhật UI
    renderHtmlConversation(conversation_list);
}

function displayMessage(message) {
    var currentChat = document.getElementById('chat');
    if (!currentChat) return;

    // Tạo phần tử tin nhắn mới
    var tempDiv = document.createElement('div');
    tempDiv.innerHTML = customLoadMessage(message);

    // Lấy phần tử tin nhắn từ container tạm
    var messageElement = tempDiv.firstChild;

    // Thêm tin nhắn vào chat container bằng appendChild thay vì sử dụng innerHTML
    currentChat.appendChild(messageElement);

    // Nếu tin nhắn chứa hình ảnh, đợi hình ảnh tải xong trước khi cuộn
    if (message.messageType && message.messageType.startsWith("image")) {
        const imgElements = messageElement.querySelectorAll('img');
        Promise.all([...imgElements].map(img =>
            img.complete ? Promise.resolve() : new Promise(resolve => img.onload = img.onerror = resolve)
        )).then(() => scrollToLatestMessage());
    } else {
        scrollToLatestMessage();
    }
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
    tempFilelist = fileList;
    fileList = [];
    for (file of tempFilelist) {
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
                document.querySelector(".list-file").innerHTML = '';
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(responseData));
                // Create notification object
                let notification = {
                    actorId: user_id,
                    content: "You have new message from " + (conversationType === "Group" ? conversationName : user_fullName),
                    actor_avatar: user_avatar,
                    type: "MESSENGER",
                    recipientIds: conversationMember.map(member => member.user.id).filter(id => id !== user_id),
                    url: `/messenger?conversationId=${conversationId}`
                }
                //Websocket
                stompClient.send("/app/notification.sendNotification", {}, JSON.stringify(notification));
                await fetch("api/notification/create-notification", {
                    method: "POST",
                    body: JSON.stringify(notification),
                    headers: {
                        "Content-Type": "application/json; charset=UTF-8"
                    }
                })
            }).catch(error => {
                console.log(error);
            })
    }
    // Xóa file đính kèm sau khi gửi
    document.querySelector(".list-file").innerHTML = '';
    document.querySelector(".list-file").classList.remove("active");
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
        console.log("Response Data:", responseData);  // Log full response data
        // Create notification object
        let notification = {
            actorId: user_id,
            content: "You have new message from " + (conversationType === "Group" ? conversationName : user_fullName),
            actor_avatar: user_avatar,
            type: "MESSENGER",
            recipientIds: conversationMember.map(member => member.user.id).filter(id => id !== user_id),
            url: `/messenger?conversationId=${conversationId}`
        }
        // Send the entire response data object to the WebSocket
        const payload = {
            message: responseData,
            recipients: conversationMember.map(member => member.user.id)
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(payload));
        stompClient.send("/app/notification.sendNotification", {}, JSON.stringify(notification));
        await fetch("api/notification/create-notification", {
            method: "POST",
            body: JSON.stringify(notification),
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        })
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
            let memberModal = document.getElementById("member-modal");
            if (!memberModal) {
                memberModal = document.createElement("div");
                memberModal.id = "member-modal";
                memberModal.className = "fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50 z-50";
                memberModal.innerHTML = `
                    <div class="bg-white rounded-lg shadow-lg w-96 max-h-[80vh] overflow-hidden">
                        <div class="flex justify-between items-center border-b p-4">
                            <h3 class="text-lg font-semibold">Group Members (${conversationMember.length})</h3>
                            <button class="text-gray-500 hover:text-gray-700" onclick="document.getElementById('member-modal').remove()">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                        
                        <div class="p-4 border-b">
                            <button class="w-full flex items-center justify-center space-x-2 p-2 bg-blue-50 hover:bg-blue-100 rounded-lg text-blue-600" 
                                    onclick="toggleModal('addMembers')">
                                <i class="fas fa-user-plus"></i>
                                <span>Add Members</span>
                            </button>
                        </div>
                        <div id="members-list" class="p-4 overflow-y-auto max-h-[60vh]">
                        </div>
                    </div>
                `;
                document.body.appendChild(memberModal);
            }

            let membersList = document.getElementById("members-list");
            loadMember(conversationId, conversationType)
            membersList.innerHTML = "";
            conversationMember.forEach(member => {
                if (member.user.id == user_id && member.admin == true) {
                    isCurrentUserAdmin = true;
                }
                let memberItem = `
                    <div class="flex items-center p-3 hover:bg-gray-50 rounded-lg group">
                        <div class="flex items-center flex-grow cursor-pointer"
                             data-track-user-id="${member.user.id}" 
                             data-conversationName="${member.user.first_name} ${member.user.last_name}" 
                             data-conversationAvatar="${member.user.avatar_path}" 
                             data-conversation-type="OneToOne" 
                             ${member.user.id != user_id ? 'onclick="findOneToOneChatWithUser(' + member.user.id + '); document.getElementById(\'member-modal\').remove();"' : ''}>
                            <img class="w-10 h-10 rounded-full" src="${member.user.avatar_path}" alt="">
                            <div class="ml-3 flex-grow">
                                <div class="font-medium">${member.user.first_name} ${member.user.last_name}</div>
                                ${member.user.id == user_id ? '<div class="text-xs text-gray-500">You</div>' : ''}
                            </div>
                        </div>
                        ${isCurrentUserAdmin && member.user.id != user_id ? `
                            <button class="hidden group-hover:block text-red-500 hover:text-red-700 p-2" 
                                    onclick="removeMember(${member.user.id}, '${member.user.first_name} ${member.user.last_name}')">
                                <i class="fas fa-user-minus"></i>
                            </button>
                        ` : ''}
                    </div>
                `;
                membersList.innerHTML += memberItem;
            });

            // Add click event to close modal when clicking outside
            memberModal.addEventListener('click', (event) => {
                if (event.target === memberModal) {
                    memberModal.remove();
                }
            });
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
            console.log("test");
            if (chatDropdown.classList.contains('hidden')) {
                chatDropdown.classList.remove('hidden');
            } else {
                chatDropdown.classList.add('hidden');
            }
            break;
        case 'startGroupChat' :
            groupModal.classList.remove('hidden');
            listUserAdd = [];
            //updateSelectedUsers();
            break;
        case 'addMembers':
            let addMembersModal = document.getElementById("add-members-modal");
            if (!addMembersModal) {
                addMembersModal = document.createElement("div");
                addMembersModal.id = "add-members-modal";
                addMembersModal.className = "fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50 z-50";
                addMembersModal.innerHTML = `
                    <div class="bg-white rounded-lg shadow-lg w-96 max-h-[80vh] overflow-hidden">
                        <div class="flex justify-between items-center border-b p-4">
                            <h3 class="text-lg font-semibold">Add Members</h3>
                            <button class="text-gray-500 hover:text-gray-700" onclick="document.getElementById('add-members-modal').remove()">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                        
                        <!-- Search input -->
                        <div class="p-4 border-b">
                            <div class="relative">
                                <input type="text" 
                                       id="addGroupMembersInput" 
                                       class="w-full p-2 border border-gray-300 rounded-lg" 
                                       placeholder="Search by email..."
                                       oninput="searchUsersForGroup(event)">
                                <div class="searchResults absolute w-full bg-white mt-1 rounded-lg shadow-lg max-h-48 overflow-y-auto hidden">
                                    <!-- Search results will be added here -->
                                </div>
                            </div>
                        </div>

                        <!-- Selected users -->
                        <div class="p-4 border-b">
                            <div class="text-sm text-gray-600 mb-2">Selected users:</div>
                            <div id="selectedGroupMembers" class="flex flex-wrap gap-2">
                                <!-- Selected users will be added here -->
                            </div>
                        </div>

                        <!-- Action buttons -->
                        <div class="p-4 flex justify-end space-x-2">
                            <button onclick="document.getElementById('add-members-modal').remove()" 
                                    class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg">
                                Cancel
                            </button>
                            <button onclick="addMembersToGroup()" 
                                    id="addMembersBtn"
                                    class="px-4 py-2 bg-gray-300 text-white rounded-lg cursor-not-allowed"
                                    disabled>
                                Add Members
                            </button>
                        </div>
                    </div>
                `;
                document.body.appendChild(addMembersModal);
            }

            // Reset selected users list
            listUserAdd = [];
            updateSelectedGroupMembers();
            break;
    }
}

function toggleConversationInfo(conversationType) {
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

    let conversationInfoBox = `
        <!-- Profile Section -->
        <div class="flex flex-col items-center pt-4 pb-6 border-b">
            <div class="relative">
                <img alt="Profile picture" id="conversation-avatar" class="rounded-full mb-2 border-4 border-blue-100 h-[80px] w-[80px]" src="${conversationAvatar}"/>
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
                ${conversationType !== 'OneToOne' ? `
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
                ` : ''}
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
        ${conversationType !== 'OneToOne' ? `
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
        ` : ''}
        
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
        ${conversationType !== 'OneToOne' ? `
                <div class="ml-6 mt-3 flex items-center space-x-2 p-2 hover:bg-gray-50 rounded-lg cursor-pointer" onclick="leaveGroup(user_id)">
                    <i class="fas fa-sign-out-alt text-red-500 text-sm"></i>
                    <p class="text-xs text-red-500">
                        Leave group
                    </p>
                </div>
                ` : ''}
    `;

    infoBox.innerHTML += conversationInfoBox;
}

function leaveGroup(userId) {
    fetch(`/api/conversation-user/${conversationId}/users/${userId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                // Nếu người dùng hiện tại rời nhóm
                if (userId == user_id) {
                    // Gửi thông báo qua WebSocket
                    if (stompClient) {
                        // Lấy danh sách tất cả thành viên còn lại
                        const remainingRecipients = conversationMember
                            .filter(member => member.user.id != userId)
                            .map(member => member.user.id);

                        const payload = {
                            type: "REMOVE_USER",
                            conversationId: conversationId,
                            userId: userId,
                            recipients: remainingRecipients
                        };
                        stompClient.send("/app/chat.userRemoved", {}, JSON.stringify(payload));
                    }

                    // Xóa cuộc trò chuyện khỏi danh sách
                    removeConversationFromList(conversationId);

                    // Xóa giao diện trò chuyện
                    document.getElementById("conversation-info-box").classList.add('hidden');
                    document.getElementById("chat-container").innerHTML = '';
                    conversationId = null;
                }
            } else {
                console.error("Failed to leave the group");
            }
        });
}

async function startNewConversation(element) {
    //CREATE NEW CONVERSATION ONLY IF CONVERSATION IS NOT HAVE IN DATABASE YET, ELSE SET CONVERSATION
    //FIRST MESSAGE, SET THE SENT BUTTON CALL CREATE NEW CONVERSATION AND ADD CONVERSATION USER
    //ELSE SENT LIKE NORMAL CONVERSATION
    let chatListContainer = document.querySelector(".space-y-4");

    let selectedUser = null;
    if (selectedRecipientId) {
        try {
            let response = await fetch(`/api/user/${selectedRecipientId}`);
            if (response.ok) {
                selectedUser = await response.json();
                console.log("✅ Selected recipient:", selectedUser);
            }
        } catch (error) {
            console.error("❌ Lỗi khi tải thông tin người nhận:", error);
        }
    }

    // Tạo tên cuộc trò chuyện theo định dạng "người gửi - người nhận"
    let chatName = conversationName;
    let recipientName = `${selectedUser.first_name} ${selectedUser.last_name}`;
    if (conversationType === "OneToOne") {
        chatName = user_fullName + ` - ${selectedUser.first_name} ${selectedUser.last_name}`;
    }

    let tempConversation = {
        conversation_name: chatName,
        conversation_avatar: selectedUser.avatar_path,
        type: conversationType
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
    });

    // Step 4: Load conversation members
    await loadMember(conversationId, conversationType);

    // Step 5: Update the send button to send messages normally
    let sendButton = document.querySelector(".fa-paper-plane");
    sendButton.onclick = function (event) {
        sendMessage();
    };

    // Step 6: Send the first message
    await sendMessage();
}

let searchTimeout; // Global variable to track the timeout


function searchUserByEmail(event) {
    clearTimeout(searchTimeout); // Clear the previous timeout
    searchTimeout = setTimeout(() => {
        let searchInput = event.target.value.trim().toLowerCase();
        let searchResults = event.target.closest('div').querySelector('.searchResults');
        searchResults.innerHTML = '';

        if (searchInput === '') {
            searchResults.classList.add('hidden');
            return;
        }

        fetch(`/api/user/search/users?email=${searchInput}`)
            .then(res => res.json())
            .then(data => {
                searchResults.innerHTML = ''; // Clear previous results
                if (data && data.length > 0) {
                    searchResults.classList.remove('hidden');

                    data.forEach(user => {
                        // Skip if the user is the current user
                        if (user.id == user_id) return;

                        // Check if the input is from group modal
                        const isGroupChat = event.target.id === 'addPeopleInput';

                        if (isGroupChat) {
                            // Create user element
                            const userElement = document.createElement('div');
                            userElement.className = 'flex items-center p-2 hover:bg-gray-100 cursor-pointer';
                            userElement.innerHTML = `
                            <div class="w-6 h-6 rounded-full overflow-hidden mr-2">
                                <img src="${user.avatar_path}" alt="Avatar" class="w-full h-full object-cover">
                            </div>
                            <div class="text-xs text-gray-500 ml-2">${user.email}</div>
                            `;

                            // Add checkbox for group selection
                            const checkbox = document.createElement('input');
                            checkbox.type = 'checkbox';
                            checkbox.className = 'mr-2';

                            // Check if user is already selected
                            checkbox.checked = listUserAdd.some(selectedUser => selectedUser.id === user.id);

                            checkbox.addEventListener('change', (e) => {
                                toggleUserSelection(user, e.target.checked);
                            });

                            userElement.addEventListener('click', () => {
                                checkbox.checked = !checkbox.checked; // Toggle checkbox state
                                toggleUserSelection(user, checkbox.checked); // Call the selection handler
                            });

                            userElement.prepend(checkbox);
                            searchResults.appendChild(userElement);
                        } else {
                            // Create user element
                            const userElement = document.createElement('div');
                            userElement.className = 'flex items-center p-2 hover:bg-gray-100 cursor-pointer';
                            userElement.setAttribute('data-track-user-id', user.id);
                            userElement.setAttribute('data-conversationName', `${user.first_name} ${user.last_name}`);
                            userElement.setAttribute('data-conversationAvatar', user.avatar_path);
                            userElement.setAttribute('data-conversation-type', 'OneToOne');
                            userElement.setAttribute('onclick', 'setConversation(this)');

                            userElement.innerHTML = `
                            <div class="w-6 h-6 rounded-full overflow-hidden mr-2">
                                <img src="${user.avatar_path}" alt="Avatar" class="w-full h-full object-cover">
                            </div>
                            <div class="text-xs text-gray-500 ml-2">${user.email}</div>
                            `;
                            searchResults.appendChild(userElement);
                        }
                    });
                } else {
                    searchResults.classList.remove('hidden');
                    searchResults.innerHTML = '<div class="p-3 text-center text-gray-500">No users found</div>';
                }
            })
            .catch(error => {
                console.error('Error searching for users:', error);
                searchResults.classList.remove('hidden');
                searchResults.innerHTML = '<div class="p-3 text-center text-red-500">Error searching for users</div>';
            });
    }, 200); // Delay API call by 50ms
}


function toggleUserSelection(user, isSelected) {
    if (isSelected) {
        // Add user if not already in the list
        if (!listUserAdd.some(selectedUser => selectedUser.id === user.id)) {
            listUserAdd.push(user);
            updateSelectedUsersDisplay();
        }
    } else {
        // Remove user from the list
        listUserAdd = listUserAdd.filter(selectedUser => selectedUser.id !== user.id);
        updateSelectedUsersDisplay();
    }
}

function updateSelectedUsersDisplay() {
    const selectedUsersContainer = document.getElementById('selectedUsers');
    selectedUsersContainer.innerHTML = '';

    listUserAdd.forEach(user => {
        const userTag = document.createElement('div');
        userTag.className = 'flex items-center bg-gray-100 rounded-full px-3 py-1 text-sm';
        userTag.innerHTML = `
            <div class="w-5 h-5 rounded-full overflow-hidden mr-1">
                <img src="${user.avatar_path}" alt="Avatar" class="w-full h-full object-cover">
            </div>
            <span class="mr-1">${user.email}</span>
            <button class="text-gray-500 hover:text-gray-700" data-user-id="${user.id}">×</button>
        `;

        // Add click event to remove button
        const removeButton = userTag.querySelector('button');
        removeButton.addEventListener('click', () => {
            removeSelectedUser(user.id);
        });

        selectedUsersContainer.appendChild(userTag);
    });

    // Update create button state
    updateCreateButtonState();
}

function removeSelectedUser(userId) {
    listUserAdd = listUserAdd.filter(user => user.id !== userId);
    updateSelectedUsersDisplay();

    // Update checkboxes in search results if visible
    const searchResults = document.getElementById('searchResults');
    if (!searchResults.classList.contains('hidden')) {
        const checkboxes = searchResults.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            const userElement = checkbox.closest('div');
            const userEmail = userElement.querySelector('.text-gray-500').textContent;
            const matchingUser = listUserAdd.find(u => u.email === userEmail);
            if (matchingUser && matchingUser.id === userId) {
                checkbox.checked = false;
            }
        });
    }
}

function updateCreateButtonState() {
    const createButton = document.getElementById('createGroupBtn');
    const groupNameInput = document.getElementById('groupNameInput');

    if (groupNameInput.value.trim() !== '' && listUserAdd.length > 0) {
        createButton.disabled = false;
        createButton.classList.remove('bg-gray-300', 'cursor-not-allowed');
        createButton.classList.add('bg-blue-500');
    } else {
        createButton.disabled = true;
        createButton.classList.remove('bg-blue-500');
        createButton.classList.add('bg-gray-300', 'cursor-not-allowed');
    }
}

document.getElementById('groupNameInput').addEventListener('input', updateCreateButtonState);

document.addEventListener('click', (e) =>   {
    const searchResults = event.target.closest('div').querySelector('.searchResults');
    const addPeopleInput = document.getElementById('addPeopleInput');

    if (searchResults && !searchResults.classList.contains('hidden') &&
        !searchResults.contains(e.target) && e.target !== addPeopleInput) {
        searchResults.classList.add('hidden');
    }
});

function createGroup() {
    const groupName = document.getElementById('groupNameInput').value.trim();
    const currentUserId = document.getElementById('user_id').value; // Get current user's ID
    let newGroup;
    if (groupName === '' || listUserAdd.length === 0) {
        alert("Please enter a group name and select at least one user.");
        return;
    }
    updateCreateButtonState();
    // Step 1: Create the group conversation
    fetch('/api/conversation/group/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            type: "Group",
            is_Active: true,
            conversation_avatar: '/assets/img/venue/venue-03.jpg',
            conversation_name: groupName
        })
    })
        .then(res => res.json())
        .then(data => {
            if (data.id) {
                newGroup = data;
                const conversationId = data.id;
                let addUserPromises = [];

                // Add the current user to the group
                let currentUserData = {
                    conversation_id: conversationId,
                    user_id: currentUserId,
                    admin: true
                };

                let addCurrentUserPromise = fetch('/api/conversation-user/add-user', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(currentUserData)
                }).then(res => res.json());
                addUserPromises.push(addCurrentUserPromise);

                // Step 2: Add each other user (excluding the creator) to the group
                listUserAdd.forEach(user => {
                    let userData = {
                        conversation_id: conversationId,
                        user_id: user.id,
                        admin: false
                    };

                    let addUserPromise = fetch('/api/conversation-user/add-user', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(userData)
                    }).then(res => res.json());

                    addUserPromises.push(addUserPromise);
                });
                // Wait for all users to be added
                return Promise.all(addUserPromises);
            } else {
                throw new Error('Failed to create group');
            }
        })

        .then(() => {
            document.getElementById('groupNameInput').value = '';
            listUserAdd = [];
            updateSelectedUsersDisplay();
            const newConversation = {
                id: newGroup.id,
                conversationName: newGroup.conversationName,
                conversationAvatar: newGroup.conversationAvatar,
                last_message: "You have been added to the group",
                type: "Group"
            };
            conversation_list.unshift(newConversation);
            originalConversationList.unshift(newConversation);
            renderHtmlConversation(conversation_list);
            // Đóng modal tạo nhóm
            document.getElementById('groupModal').classList.add('hidden');
            const conversationElement = document.querySelector(`[data-id="${newGroup.id}"]`);
            if (conversationElement) {
                setConversation(conversationElement);
            }
        })
        .catch(error => {
            console.error('Error creating group:', error);
        });
}



function removeMember(userId, userName) {
    if (!isCurrentUserAdmin) {
        alert("Only admin can remove members from the group.");
        return;
    }

    if (confirm(`Are you sure you want to remove ${userName} from the group?`)) {
        fetch(`/api/conversation-user/${conversationId}/users/${userId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    // Cập nhật danh sách thành viên cục bộ
                    conversationMember = conversationMember.filter(member => member.user.id != userId);

                    // Gửi thông báo qua WebSocket
                    if (stompClient) {
                        // Lấy danh sách tất cả thành viên còn lại
                        const remainingRecipients = conversationMember.map(member => member.user.id);

                        const payload = {
                            type: "REMOVE_USER",
                            conversationId: conversationId,
                            userId: userId,
                            recipients: remainingRecipients
                        };
                        stompClient.send("/app/chat.userRemoved", {}, JSON.stringify(payload));
                    }
                } else {
                    console.error("Failed to remove the user from group");
                }
            });
    }
}

function searchUsersForGroup(event) {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        let searchInput = event.target.value.trim().toLowerCase();
        let searchResults = event.target.closest('div').querySelector('.searchResults');
        searchResults.innerHTML = '';

        if (searchInput === '') {
            searchResults.classList.add('hidden');
            return;
        }

        fetch(`/api/user/search/users?email=${searchInput}`)
            .then(res => res.json())
            .then(data => {
                searchResults.innerHTML = '';
                if (data && data.length > 0) {
                    searchResults.classList.remove('hidden');

                    data.forEach(user => {
                        // Skip if user is current user or already in the group
                        if (user.id == user_id || conversationMember.some(member => member.id === user.id)) return;

                        const userElement = document.createElement('div');
                        userElement.className = 'flex items-center p-2 hover:bg-gray-100 cursor-pointer';
                        userElement.innerHTML = `
                            <input type="checkbox" class="mr-2" 
                                   ${listUserAdd.some(selected => selected.id === user.id) ? 'checked' : ''}>
                            <div class="w-8 h-8 rounded-full overflow-hidden mr-2">
                                <img src="${user.avatar_path}" alt="Avatar" class="w-full h-full object-cover">
                            </div>
                            <div class="flex-grow">
                                <div class="text-sm">${user.first_name} ${user.last_name}</div>
                                <div class="text-xs text-gray-500">${user.email}</div>
                            </div>
                        `;

                        const checkbox = userElement.querySelector('input[type="checkbox"]');
                        checkbox.addEventListener('change', () => {
                            toggleGroupMemberSelection(user, checkbox.checked);
                        });

                        userElement.addEventListener('click', (e) => {
                            if (e.target !== checkbox) {
                                checkbox.checked = !checkbox.checked;
                                toggleGroupMemberSelection(user, checkbox.checked);
                            }
                        });

                        searchResults.appendChild(userElement);
                    });
                } else {
                    searchResults.classList.remove('hidden');
                    searchResults.innerHTML = '<div class="p-3 text-center text-gray-500">No results found</div>';
                }
            })
            .catch(error => {
                console.error('Error searching for users:', error);
                if (filteredConversations.length === 0) {
                    searchResults.innerHTML = "<p class='text-gray-500 text-center'>No results found</p>";
                }
            });
    }, 200);
}

function toggleGroupMemberSelection(user, isSelected) {
    if (isSelected) {
        if (!listUserAdd.some(selected => selected.id === user.id)) {
            listUserAdd.push(user);
        }
    } else {
        listUserAdd = listUserAdd.filter(selected => selected.id !== user.id);
    }
    updateSelectedGroupMembers();
}

function updateSelectedGroupMembers() {
    const container = document.getElementById('selectedGroupMembers');
    const addButton = document.getElementById('addMembersBtn');

    if (!container) return;

    container.innerHTML = '';

    listUserAdd.forEach(user => {
        const tag = document.createElement('div');
        tag.className = 'flex items-center bg-blue-50 rounded-full px-3 py-1 text-sm';
        tag.innerHTML = `
            <img src="${user.avatar_path}" alt="Avatar" class="w-5 h-5 rounded-full mr-2">
            <span class="text-blue-600">${user.first_name} ${user.last_name}</span>
            <button class="ml-2 text-blue-400 hover:text-blue-600" onclick="toggleGroupMemberSelection('${user.id}', false)">×</button>
        `;
        container.appendChild(tag);
    });

    // Update add button state
    if (listUserAdd.length > 0) {
        addButton.disabled = false;
        addButton.classList.remove('bg-gray-300', 'cursor-not-allowed');
        addButton.classList.add('bg-blue-500', 'hover:bg-blue-600');
    } else {
        addButton.disabled = true;
        addButton.classList.remove('bg-blue-500', 'hover:bg-blue-600');
        addButton.classList.add('bg-gray-300', 'cursor-not-allowed');
    }
}

async function addMembersToGroup() {
    if (!isCurrentUserAdmin) {
        alert("Only admin can add members to the group.");
        return;
    }

    if (listUserAdd.length === 0) return;

    try {
        // Thêm người dùng vào nhóm
        for (const user of listUserAdd) {
            const response = await fetch('/api/conversation-user/add-user', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    conversation_id: conversationId,
                    user_id: user.id,
                    admin: false
                })
            });

            if (!response.ok) {
                throw new Error(`Failed to add user ${user.id}`);
            }

            const result = await response.json();

            // Gửi thông báo qua WebSocket
            if (stompClient) {
                // Lấy danh sách tất cả thành viên hiện tại
                const allRecipients = conversationMember
                    .map(member => member.user.id)
                    .concat(user.id); // Thêm người dùng mới vào danh sách

                const payload = {
                    type: "ADD_USER",
                    userId: user.id,
                    conversation: {
                        id: conversationId,
                        conversationName: conversationName,
                        conversationAvatar: conversationAvatar,
                        type: conversationType
                    },
                    recipients: allRecipients
                };
                stompClient.send("/app/chat.userAdded", {}, JSON.stringify(payload));
            }
        }

        // Cập nhật danh sách thành viên cục bộ
        const updatedMembers = [...conversationMember];
        listUserAdd.forEach(user => {
            updatedMembers.push({
                user: {
                    id: user.id,
                    first_name: user.first_name,
                    last_name: user.last_name,
                    avatar_path: user.avatar_path
                },
                admin: false
            });
        });
        conversationMember = updatedMembers;

        // Đóng modal thêm thành viên
        document.getElementById('add-members-modal').remove();

        // Làm mới modal thành viên
        toggleModal('showMembers');

        // Đặt lại danh sách người dùng đã chọn
        listUserAdd = [];
    } catch (error) {
        console.error('Error adding members:', error);
        alert('Failed to add members to the group. Please try again.');
    }
}

// Add search function
function searchConversations(event) {
    clearTimeout(searchTimeout); // Cancel any previous timeout

    searchTimeout = setTimeout(() => {
        const searchTerm = event.target.value.toLowerCase();
        const conversationList = document.getElementById("conversationList");
        const searchResults = document.getElementById("searchResults");
        const backButton = document.getElementById("backButton");

        if (searchTerm.trim() === "") {
            conversationList.classList.remove("hidden");
            searchResults.classList.add("hidden");
            backButton.classList.add("hidden");
            renderHtmlConversation(originalConversationList);
            return;
        }

        conversationList.classList.add("hidden");
        searchResults.classList.remove("hidden");
        backButton.classList.remove("hidden");

        const filteredConversations = originalConversationList.filter(conversation => {
            const conversationName = conversation.conversationName.toLowerCase();
            return conversationName.includes(searchTerm);
        });

        searchResults.innerHTML = "";

        if (filteredConversations.length > 0) {
            searchResults.innerHTML += `
                <div class="mb-4">
                    <h3 class="text-sm font-semibold text-gray-500 mb-2">Conversations</h3>
                    ${filteredConversations.map(conversation => `
                        <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" 
                             data-id="${conversation.id}" 
                             data-conversationName="${conversation.conversationName.replace(/<[^>]*>/g, '')}" 
                             data-conversation-type="${conversation.type}" 
                             data-conversationAvatar="${conversation.conversationAvatar}" 
                             onclick="setConversation(this)">
                            <img alt="User avatar" 
                                 id="conversation-avatar" 
                                 class="rounded-full h-[40px]" 
                                 src="${conversation.conversationAvatar || 'https://storage.googleapis.com/a1aa/image/P3mTDAXzCcHqcSIVZqLFhn31Oc6SJ-ZYT5fCH91vHJ4.jpg'}"/>
                            <div>
                                <div class="font-bold">${conversation.conversationName}</div>
                                <div class="text-gray-500 text-sm">${conversation.last_message || "No messages yet"}</div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        fetch(`/api/user/search/users?email=${searchTerm}`)
            .then(response => response.json())
            .then(users => {
                if (users && users.length > 0) {
                    searchResults.innerHTML += `
                        <div class="space-y-4">
                            <h3 class="text-sm font-semibold text-gray-500 mb-2">Users</h3>
                            ${users.map(user => `
                                <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" 
                                     data-track-user-id="${user.id}" 
                                     data-conversationName="${user.first_name} ${user.last_name}" 
                                     data-conversationAvatar="${user.avatar_path}" 
                                     data-conversation-type="OneToOne" 
                                     onclick="setConversation(this)">
                                    <img src="${user.avatar_path}" alt="Avatar" class="h-[40px] object-cover">
                                    <div>
                                        <div class="font-bold">${user.first_name} ${user.last_name}</div>
                                        <div class="text-gray-500 text-sm">${user.email}</div>
                                    </div>
                                </div>
                            `).join('')}
                        </div>
                    `;
                }

                if (filteredConversations.length === 0 && (!users || users.length === 0)) {
                    searchResults.innerHTML = "<p class='text-gray-500 text-center'>No results found</p>";
                }
            })
            .catch(error => {
                console.error('Error searching for users:', error);
                if (filteredConversations.length === 0) {
                    searchResults.innerHTML = "<p class='text-gray-500 text-center'>No results found</p>";
                }
            });
    }, 200); // Delay of 200ms
}

// Thêm sự kiện click cho nút quay lại
document.getElementById("backButton").addEventListener("click", function() {
    const conversationList = document.getElementById("conversationList");
    const searchResults = document.getElementById("searchResults");
    const backButton = document.getElementById("backButton");
    const searchInput = document.querySelector("input[type='text']");

    // Xóa nội dung tìm kiếm
    searchInput.value = "";

    // Hiển thị lại danh sách hội thoại ban đầu
    conversationList.classList.remove("hidden");
    searchResults.classList.add("hidden");
    backButton.classList.add("hidden");
    renderHtmlConversation(originalConversationList);
});

async function findOrCreateOneToOneConversation(currentUserId, recipientId) {
    try {
        // Trước tiên, tìm kiếm xem đã có cuộc trò chuyện one-to-one giữa hai người dùng chưa
        const response = await fetch(`/api/conversation/find-one-to-one/${currentUserId}/${recipientId}`);

        if (response.ok) {
            const conversationData = await response.json();

            // Nếu đã tìm thấy cuộc trò chuyện
            if (conversationData && conversationData.id) {
                console.log("✅ Đã tìm thấy cuộc trò chuyện:", conversationData);
                return conversationData;
            }
        }

        // Nếu chưa có cuộc trò chuyện, tạo mới
        // Tải thông tin người nhận
        const recipientResponse = await fetch(`/api/user/${recipientId}`);
        if (!recipientResponse.ok) throw new Error("Không thể tải thông tin người nhận");

        const recipientUser = await recipientResponse.json();
        const chatName = user_fullName + ` - ${recipientUser.first_name} ${recipientUser.last_name}`;

        // Tạo cuộc trò chuyện mới
        const createResponse = await fetch('/api/conversation/group/create', {
            method: 'POST',
            body: JSON.stringify({
                conversation_name: chatName,
                conversation_avatar: recipientUser.avatar_path,
                type: "OneToOne"
            }),
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        });

        if (!createResponse.ok) throw new Error("Không thể tạo cuộc trò chuyện mới");

        const newConversation = await createResponse.json();

        // Thêm người dùng hiện tại vào cuộc trò chuyện
        await fetch('/api/conversation-user/add-user', {
            method: 'POST',
            body: JSON.stringify({
                conversation_id: newConversation.id,
                user_id: currentUserId,
                admin: false
            }),
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        });

        // Thêm người nhận vào cuộc trò chuyện
        await fetch('/api/conversation-user/add-user', {
            method: 'POST',
            body: JSON.stringify({
                conversation_id: newConversation.id,
                user_id: recipientId,
                admin: false
            }),
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        });

        console.log("✅ Đã tạo cuộc trò chuyện mới:", newConversation);
        return newConversation;
    } catch (error) {
        console.error("❌ Lỗi khi tìm/tạo cuộc trò chuyện:", error);
        return null;
    }
}

// Cập nhật hàm này để chỉ tìm kiếm cuộc trò chuyện hiện có, không tạo mới
async function findOneToOneChatWithUser(recipientId) {
    try {
        document.getElementById("chat-container").innerHTML = '<div class="text-center p-4"><i class="fas fa-spinner fa-spin"></i> Đang tải cuộc trò chuyện...</div>';

        // Chỉ tìm kiếm cuộc trò chuyện hiện có, không tạo mới
        const response = await fetch(`/api/conversation/find-one-to-one/${user_id}/${recipientId}`);
        let conversation = null;

        if (response.ok) {
            conversation = await response.json();
        }

        // Nếu tìm thấy cuộc trò chuyện
        if (conversation && conversation.id) {

            // Kiểm tra xem cuộc trò chuyện có trong danh sách chưa
            if (!conversation_list.some(conv => conv.id === conversation.id)) {
                // Thêm vào danh sách nếu chưa có
                let conversationHTML = `    
                    <div class="flex items-center space-x-2 p-2 bg-white rounded-lg cursor-pointer hover:bg-gray-200" 
                         data-id="${conversation.id}" 
                         data-conversationName="${conversation.conversationName}" 
                         data-conversationAvatar="${conversation.conversationAvatar}" 
                         data-conversation-type="OneToOne"
                         onclick="setConversation(this)">
                        <img alt="User avatar" id="conversation-avatar" class="rounded-full" height="40" src="${conversation.conversationAvatar}" width="40"/>
                        <div>
                            <div class="font-bold">${conversation.conversationName}</div>
                            <div class="text-gray-500 text-sm">No messages yet</div>
                        </div>
                    </div>
                `;

                // Thêm vào đầu danh sách HTML
                let chatListContainer = document.querySelector(".space-y-4");
                chatListContainer.innerHTML = conversationHTML + chatListContainer.innerHTML;

                // Thêm vào danh sách JavaScript
                const newConversation = {
                    id: conversation.id,
                    conversationName: conversation.conversationName,
                    conversationAvatar: conversation.conversationAvatar,
                    last_message: "No messages yet",
                    type: "OneToOne"
                };

                conversation_list.unshift(newConversation);
                originalConversationList.unshift(newConversation);
            }

            // Tìm element để hiển thị cuộc trò chuyện
            const conversationElement = document.querySelector(`[data-id="${conversation.id}"]`);
            if (conversationElement) {
                setConversation(conversationElement);
            } else {
                console.error("Cannot find html element");
            }
        } else {
            // Nếu không tìm thấy cuộc trò chuyện, hiển thị chat box tạm
            console.log("Cannot find conversation, open temp chat box")

            // Lấy thông tin người nhận
            const recipientResponse = await fetch(`/api/user/${recipientId}`);
            if (!recipientResponse.ok) {
                throw new Error("Cannot load recipient's data");
            }

            const recipientUser = await recipientResponse.json();

            // Set các biến global để sử dụng cho hàm startNewConversation
            selectedRecipientId = recipientId;
            conversationId = null; // Đặt là null để khi bấm gửi sẽ gọi startNewConversation
            conversationName = recipientUser.first_name + " " + recipientUser.last_name;
            conversationAvatar = recipientUser.avatar_path;
            conversationType = "OneToOne";

            // Hiển thị chat box tạm
            var rightSide = `
                <!-- Conversation Header -->
                <div class="flex items-center justify-between p-2 border-b">
                    <div class="flex items-center space-x-2">
                        <img alt="User avatar" id="conversation-avatar" class="rounded-full" height="40" src="${conversationAvatar}" width="40"/>
                        <div>
                            <div class="font-bold conversation-name">${conversationName}</div>
                            <div class="text-green-500 text-sm">Online</div>
                        </div>
                    </div>
                    <div class="flex items-center space-x-2">
                        <!-- Call Button -->
                        <div class="p-2 rounded-full hover:bg-gray-100 cursor-pointer btn_call">
                            <i class="fas fa-phone text-gray-600"></i>
                        </div>
                        <!-- Info Button -->
                        <div class="p-2 rounded-full hover:bg-gray-100 cursor-pointer btn_conversation_information" onclick="toggleConversationInfo('${conversationType}')">
                            <i class="fas fa-ellipsis-v text-gray-600"></i>
                        </div>
                    </div>
                </div>
                <!-- Chat Messages -->
                <div class="flex-grow space-y-4 overflow-y-auto p-4" id="chat" style="height: 400px;">
                    <div class="text-center p-4 text-gray-500">Hãy gửi tin nhắn để bắt đầu cuộc trò chuyện</div>
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
                    <button class="p-2 rounded-full hover:bg-gray-100" onclick="startNewConversation(this)">
                        <i class="fas fa-paper-plane text-blue-500 text-xl"></i>
                    </button>
                </div>
            `;

            document.getElementById("chat-container").innerHTML = rightSide;

            // Add event listener for Enter key on message input
            document.getElementById("message").addEventListener("keypress", function(event) {
                if (event.key === "Enter") {
                    event.preventDefault();
                    startNewConversation(document.querySelector(".fa-paper-plane").parentElement);
                }
            });

            // Handle file upload
            displayFiles();

            // Setup call button
            const callButton = document.querySelector(".btn_call");
            callButton.addEventListener("click", window.makeCall);
        }
    } catch (error) {
        console.error("❌ Lỗi khi tìm cuộc trò chuyện:", error);
        document.getElementById("chat-container").innerHTML = '<div class="text-center p-4 text-red-500">Lỗi khi tải cuộc trò chuyện.</div>';
    }
}

function adjustHeight() {
    const header = document.querySelector('header');
    const content = document.getElementById('messengerBody');
    const headerHeight = header.offsetHeight;
    content.style.height = `calc(100vh - ${headerHeight}px)`;
}
window.addEventListener('load', adjustHeight);
window.addEventListener('resize', adjustHeight);