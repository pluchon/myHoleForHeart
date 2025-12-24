let currentAIType = null;
let currentUser = null;
let chatBackgrounds = [];

const kaomojis = [
    "(・ω・)", "(>_<)", "(T_T)", "(*^▽^*)", "(QAQ)", 
    "(o_o)", "(¬_¬ )", "(uxu)", "(UwU)", "(^o^)/",
    "(=^･ω･^=)", "(｡♥‿♥｡)", "╮(╯▽╰)╭", "(￣▽￣)", "(⊙_⊙)",
    "(˘•ω•˘)", "⁽˙³˙⁾", "(ฅ´ω`ฅ)", "(-_-|||)", "(*_*)"
];
const emojis = [
    "😀", "😂", "😍", "😭", "😡", "👍", "👎", "🎉", "❤️", "💔", "🤔", "😎", 
    "🙏", "👀", "🔥", "✨", "💯", "🚀", "🌈", "🎈", "🎁", "🌹"
];

document.addEventListener('DOMContentLoaded', () => {
    // 检查登录状态 (使用 /user/info 保持一致性)
    fetch('/user/info')
        .then(res => {
            if (res.ok) return res.text();
            throw new Error('Check login failed');
        })
        .then(text => {
            if (!text) {
                alert('请先登录');
                window.location.href = 'login.html';
                return;
            }
            try {
                const user = JSON.parse(text);
                if (!user || !user.id) {
                    alert('请先登录');
                    window.location.href = 'login.html';
                }
                currentUser = user;
            } catch(e) {
                alert('请先登录');
                window.location.href = 'login.html';
            }
        })
        .catch(e => {
            console.error(e);
            // 如果请求出错，可能也是未登录或网络问题
        });

    // 加载背景图片
    fetch('/message/backgrounds')
        .then(res => res.json())
        .then(data => {
            chatBackgrounds = data;
        })
        .catch(e => console.error("Failed to load backgrounds", e));

    // 回车发送
    document.getElementById('chat-input').addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMsg();
        }
    });

    // 点击外部关闭弹窗
    document.addEventListener('click', (e) => {
        const pickers = document.querySelectorAll('.picker-popover.active');
        pickers.forEach(p => {
            if (!p.contains(e.target) && !e.target.closest('.tool-btn')) {
                p.classList.remove('active');
            }
        });
    });
});

function selectAI(aiType, element) {
    currentAIType = aiType;
    
    // 更新界面
    document.querySelectorAll('.chat-user-item').forEach(el => el.classList.remove('active'));
    element.classList.add('active');
    
    const name = element.querySelector('.chat-user-name').innerText;
    document.getElementById('current-ai-name').innerText = name;
    document.getElementById('input-area').style.display = 'block';
    
    // 显示设置按钮
    document.getElementById('bg-setting-btn').style.display = 'block';
    
    loadHistory(aiType);
}

// 背景设置
function toggleBgSettings(e) {
    e.stopPropagation();
    const popover = document.getElementById('bg-settings-popover');
    popover.classList.toggle('active');
    // 隐藏其他弹窗
    if(document.getElementById('emoji-picker')) document.getElementById('emoji-picker').classList.remove('active');
    if(document.getElementById('kaomoji-picker')) document.getElementById('kaomoji-picker').classList.remove('active');
}

function randomChatBg() {
    if(!chatBackgrounds || chatBackgrounds.length === 0) {
        fetch('/message/backgrounds').then(r=>r.json()).then(data=>{
            chatBackgrounds = data;
            if(chatBackgrounds.length > 0) {
                randomChatBg();
            } else {
                alert('暂无背景图片');
            }
        }).catch(e => alert('加载背景失败'));
        return;
    }
    const idx = Math.floor(Math.random() * chatBackgrounds.length);
    const bgName = chatBackgrounds[idx];
    const bgUrl = `/chatPicture/${bgName}`;
    document.getElementById('chat-custom-bg').style.backgroundImage = `url('${bgUrl}')`;
}

function changeBgOpacity(val) {
    document.getElementById('chat-custom-bg').style.opacity = val / 100;
    document.getElementById('bg-opacity-val').innerText = val + '%';
}

// 表情和颜文字
function toggleKaomojiPicker(e) {
    e.stopPropagation();
    const picker = document.getElementById('kaomoji-picker');
    document.getElementById('emoji-picker').classList.remove('active');
    if(document.getElementById('bg-settings-popover')) document.getElementById('bg-settings-popover').classList.remove('active');
    
    if (picker.innerHTML === '') {
        kaomojis.forEach(k => {
            const span = document.createElement('span');
            span.className = 'kaomoji-item';
            span.textContent = k;
            span.onclick = () => insertText(k);
            picker.appendChild(span);
        });
    }
    picker.classList.toggle('active');
}

function toggleEmojiPicker(e) {
    e.stopPropagation();
    const picker = document.getElementById('emoji-picker');
    document.getElementById('kaomoji-picker').classList.remove('active');
    if(document.getElementById('bg-settings-popover')) document.getElementById('bg-settings-popover').classList.remove('active');
    
    if (picker.innerHTML === '') {
        emojis.forEach(k => {
            const span = document.createElement('span');
            span.className = 'emoji-item';
            span.textContent = k;
            span.onclick = () => insertText(k);
            picker.appendChild(span);
        });
    }
    picker.classList.toggle('active');
}

function insertText(text) {
    const input = document.getElementById('chat-input');
    input.value += text;
    input.focus();
    document.getElementById('kaomoji-picker').classList.remove('active');
    document.getElementById('emoji-picker').classList.remove('active');
}


function loadHistory(aiType) {
    const list = document.getElementById('message-list');
    list.innerHTML = '<div style="text-align:center; padding:20px; color:#999;">加载历史记录...</div>';
    
    fetch(`/ai/history?aiType=${aiType}`)
        .then(res => {
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(`Server Error: ${res.status} ${text}`);
                });
            }
            return res.json();
        })
        .then(data => {
            list.innerHTML = '';
            
            // 检查是否为异常返回 (AllExceptionResult)
            if (data && !Array.isArray(data)) {
                // -10: NOT_LOGIN
                if (data.statusCode === -10 || (data.status && data.status === 'NOT_LOGIN')) {
                     list.innerHTML = '<div class="empty-tip" style="margin-top:20px; text-align:center; color:#999;">请先登录</div>';
                     setTimeout(() => window.location.href = '/login.html', 1500);
                     return;
                }
                // 其他错误
                if (data.errorMessage) {
                     console.error("API Error:", data);
                     list.innerHTML = `<div class="empty-tip" style="margin-top:20px; text-align:center; color:red;">加载失败: ${data.errorMessage}</div>`;
                     return;
                }
            }

            if (data && Array.isArray(data) && data.length > 0) {
                data.forEach(msg => appendMessage(msg));
            } else {
                list.innerHTML = '<div class="empty-tip" style="margin-top:20px; text-align:center; color:#999;">暂无聊天记录，开始对话吧</div>';
            }
            scrollToBottom();
        })
        .catch(err => {
            console.error("Load history error:", err);
            list.innerHTML = '<div class="empty-tip" style="margin-top:20px; text-align:center; color:red;">加载历史记录失败，请检查网络或稍后重试</div>';
        });
}

function appendMessage(msg) {
    const list = document.getElementById('message-list');
    
    // 移除暂无记录提示
    const emptyTip = list.querySelector('.empty-tip');
    if (emptyTip) {
        emptyTip.remove();
    }

    const isSelf = msg.sender === 'user';
    
    const div = document.createElement('div');
    div.className = `chat-msg ${isSelf ? 'user' : 'ai'}`;
    
    // 头像
    let avatarHtml = '';
    if (isSelf) {
        // 用户头像
        if (currentUser && currentUser.avatar) {
            avatarHtml = `<div class="msg-avatar user"><img src="${currentUser.avatar}" alt="用户头像"></div>`;
        } else {
            avatarHtml = `<div class="msg-avatar user"><i class="ri-user-smile-line"></i></div>`;
        }
    } else {
        // AI 头像
        let icon = 'ri-robot-line';
        let typeClass = 'general';
        // 优先使用消息自带的 aiType (防止切换过快导致头像错乱)
        const type = msg.aiType || currentAIType;
        
        if (type === 'emotional') { icon = 'ri-heart-3-line'; typeClass = 'emotional'; }
        if (type === 'guardian') { icon = 'ri-shield-user-line'; typeClass = 'guardian'; }
        if (type === 'funny') { icon = 'ri-emotion-laugh-line'; typeClass = 'funny'; }
        
        avatarHtml = `<div class="msg-avatar ai ${typeClass}"><i class="${icon}"></i></div>`;
    }
    
    // 统一将头像放在内容之前，通过 flex-direction: row-reverse 控制用户消息显示顺序
    div.innerHTML = `
        ${avatarHtml}
        <div class="msg-content">${formatContent(msg.content)}</div>
    `;
    
    list.appendChild(div);
}

function formatContent(content) {
    return content.replace(/\n/g, '<br>');
}

function scrollToBottom() {
    const list = document.getElementById('message-list');
    list.scrollTop = list.scrollHeight;
}

function sendMsg() {
    if (!currentAIType) return;
    const input = document.getElementById('chat-input');
    const content = input.value.trim();
    if (!content) return;
    
    input.value = '';
    
    // 添加用户消息
    appendMessage({
        sender: 'user',
        content: content
    });
    scrollToBottom();
    
    // 添加加载中提示
    const list = document.getElementById('message-list');
    const loadingDiv = document.createElement('div');
    loadingDiv.className = 'chat-msg ai loading-msg';
    
    // AI 头像样式需要匹配当前AI
    let icon = 'ri-robot-line';
    let typeClass = 'general';
    if (currentAIType === 'emotional') { icon = 'ri-heart-3-line'; typeClass = 'emotional'; }
    if (currentAIType === 'guardian') { icon = 'ri-shield-user-line'; typeClass = 'guardian'; }
    if (currentAIType === 'funny') { icon = 'ri-emotion-laugh-line'; typeClass = 'funny'; }

    loadingDiv.innerHTML = `
        <div class="msg-avatar ai ${typeClass}"><i class="${icon}"></i></div>
        <div class="msg-content">Thinking...</div>
    `;
    list.appendChild(loadingDiv);
    scrollToBottom();
    
    // 发送给后端
    fetch('/ai/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            aiType: currentAIType,
            content: content
        })
    })
    .then(res => res.json())
    .then(result => {
        loadingDiv.remove();
        if (result.status === 'SUCCESS') { // 成功状态码
            appendMessage({
                sender: 'ai',
                content: result.data
            });
        } else {
            appendMessage({
                sender: 'ai',
                content: '[Error] ' + result.errorMessage
            });
        }
        scrollToBottom();
    })
    .catch(err => {
        loadingDiv.remove();
        appendMessage({
            sender: 'ai',
            content: '[Network Error] 请求失败'
        });
    });
}
