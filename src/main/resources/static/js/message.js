let currentReceiverId = null;
let currentUser = null; 
let chatBackgrounds = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Close popovers when clicking outside
    document.addEventListener('click', (e) => {
        const pickers = document.querySelectorAll('.picker-popover.active');
        pickers.forEach(p => {
            // Check if click target is inside the picker or is the toggle button
            // This is a simple check, might need refinement if buttons have specific classes
            if (!p.contains(e.target) && !e.target.closest('.tool-btn')) {
                p.classList.remove('active');
            }
        });
    });

    // Load backgrounds
    try {
        const bgRes = await fetch('/message/backgrounds');
        if (bgRes.ok) {
            chatBackgrounds = await bgRes.json();
        }
    } catch (e) {
        console.error("Failed to load backgrounds", e);
    }

    try {
        const res = await fetch('/user/info');
        if (res.ok) {
            const text = await res.text();
            if (text) {
                currentUser = JSON.parse(text);
                await loadConversations();
                
                // 检查是否有指定的用户
                const urlParams = new URLSearchParams(window.location.search);
                const targetId = urlParams.get('userId');
                const targetName = urlParams.get('nickname');
                const targetAvatar = urlParams.get('avatar');
                
                if (targetId && !currentReceiverId) {
                    // 如果列表里没选中（可能是新对话），手动初始化
                    // 检查是否已在列表中
                    const existingItem = document.querySelector(`.chat-user-item[onclick*="${targetId}"]`);
                    if (!existingItem) {
                        // 新对话，手动添加到列表顶部
                        const listEl = document.getElementById('conversation-list');
                        const tempHtml = `
                            <div class="chat-user-item active" onclick="selectUser(${targetId}, '${targetName}', '${targetAvatar}', this)">
                                <img src="${targetAvatar || '/picture/user-default.png'}" class="chat-user-avatar">
                                <div class="chat-user-info">
                                    <div class="chat-user-name">${targetName}</div>
                                    <div class="chat-user-last">新对话</div>
                                </div>
                            </div>
                        `;
                        // Remove empty tip if exists
                        if (listEl.querySelector('.empty-tip')) listEl.innerHTML = '';
                        listEl.insertAdjacentHTML('afterbegin', tempHtml);
                        selectUser(targetId, targetName, targetAvatar);
                    }
                }
            } else {
                window.location.href = 'login.html';
            }
        } else {
            window.location.href = 'login.html';
        }
    } catch (e) {
        console.error(e);
        window.location.href = 'login.html';
    }
});

async function loadConversations() {
    const listEl = document.getElementById('conversation-list');
    try {
        const res = await fetch('/message/conversations');
        if (res.ok) {
            const list = await res.json();
            if (list.length === 0) {
                listEl.innerHTML = '<div class="empty-tip" style="padding: 20px; text-align: center; color: #999;">暂无消息</div>';
                return;
            }
            
            let html = '';
            const urlParams = new URLSearchParams(window.location.search);
            const targetId = urlParams.get('userId');

            list.forEach(c => {
                const avatar = c.avatar || '/picture/user-default.png';
                const isActive = (targetId && c.userId == targetId);
                const activeClass = isActive ? 'active' : '';
                
                html += `
                    <div class="chat-user-item ${activeClass}" onclick="selectUser(${c.userId}, '${c.nickname}', '${avatar}', this)">
                        <img src="${avatar}" class="chat-user-avatar">
                        <div class="chat-user-info">
                            <div class="chat-user-name">${c.nickname}</div>
                            <div class="chat-user-last">${c.lastMessage}</div>
                        </div>
                    </div>
                `;
                
                if (isActive) {
                    selectUser(c.userId, c.nickname, avatar);
                }
            });
            listEl.innerHTML = html;
        }
    } catch (e) {
        console.error(e);
        listEl.innerHTML = '<div class="empty-tip" style="padding: 20px; text-align: center; color: #999;">加载失败</div>';
    }
}

async function selectUser(userId, nickname, avatar, el) {
    if (currentReceiverId == userId) return;
    currentReceiverId = userId;
    
    // Highlight sidebar
    const items = document.querySelectorAll('.chat-user-item');
    items.forEach(i => i.classList.remove('active'));
    if (el) el.classList.add('active');
    else {
        // Find by attribute if el not passed (e.g. initial load)
        // This selector is tricky with quotes, skipping for simplicity or using data attribute
    }
    
    // Update Header
    const header = document.getElementById('chat-header');
    header.innerHTML = `
        <div style="display:flex; align-items:center;">
            <img src="${avatar || '/picture/user-default.png'}" 
                 style="width: 30px; height: 30px; border-radius: 50%; margin-right: 10px; object-fit: cover; cursor: pointer;" 
                 onclick="showTargetUserHoles(${userId}, '${nickname}')" 
                 title="查看TA的树洞">
            <span>${nickname}</span>
        </div>
        <div class="tool-btn" onclick="toggleBgSettings(event)" title="背景设置" style="font-size: 20px; margin-left: auto;">
            <i class="ri-settings-3-line"></i>
        </div>
    `;
    
    // Show Input
    document.getElementById('input-area').style.display = 'block';
    
    // Load History
    await loadHistory(userId);
}

async function loadHistory(userId) {
    const container = document.getElementById('message-list');
    container.innerHTML = '<div class="empty-tip" style="text-align: center; padding: 20px;">加载中...</div>';
    
    try {
        const res = await fetch(`/message/history?otherUserId=${userId}`);
        if (res.ok) {
            const messages = await res.json();
            container.innerHTML = '';
            
            if (messages.length === 0) {
                container.innerHTML = '<div class="empty-tip" style="text-align: center; color: #999; margin-top: 20px;">暂无历史消息</div>';
            } else {
                messages.forEach(msg => {
                    const isMe = msg.senderId === currentUser.id;
                    const type = isMe ? 'sent' : 'received';
                    const div = document.createElement('div');
                    div.className = `message-bubble ${type}`;
                    div.textContent = msg.content;
                    container.appendChild(div);
                });
                // Scroll to bottom
                container.scrollTop = container.scrollHeight;
            }
        }
    } catch (e) {
        console.error(e);
    }
}

// Background Settings
function toggleBgSettings(e) {
    e.stopPropagation();
    const popover = document.getElementById('bg-settings-popover');
    popover.classList.toggle('active');
    // Hide others
    document.getElementById('emoji-picker').classList.remove('active');
    document.getElementById('kaomoji-picker').classList.remove('active');
}

function randomChatBg() {
    if(!chatBackgrounds || chatBackgrounds.length === 0) {
        // Try fetch again if empty
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

async function toggleTargetHoleComments(btn, holeId) {
    const container = document.getElementById(`hole-comments-${holeId}`);
    if (container.style.display === 'block') {
        container.style.display = 'none';
        return;
    }
    
    // 如果已经加载过，直接显示
    if (container.innerHTML !== '') {
        container.style.display = 'block';
        return;
    }
    
    container.style.display = 'block';
    container.innerHTML = '<div style="text-align:center;color:#999;padding:10px;">加载评论中...</div>';
    
    try {
        const res = await fetch(`/comment/getComments?holeId=${holeId}`);
        if (res.ok) {
            const comments = await res.json();
            if (comments.length === 0) {
                container.innerHTML = '<div style="text-align:center;color:#999;padding:10px;">暂无评论</div>';
            } else {
                let html = '';
                comments.forEach(c => {
                    const avatar = c.userAvatar || '/picture/user-default.png';
                    const nickname = c.userNickname || '匿名用户';
                    const time = new Date(c.createTime).toLocaleString();
                    
                    html += `
                        <div class="hole-comment-item">
                            <img src="${avatar}" class="h-c-avatar">
                            <div class="h-c-content">
                                <div class="h-c-user">${nickname}</div>
                                <div class="h-c-text">${c.content}</div>
                                <div class="h-c-meta">
                                    <span>${time}</span>
                                    <span>❤️ ${c.likeCount || 0}</span>
                                </div>
                            </div>
                        </div>
                    `;
                });
                container.innerHTML = html;
            }
        } else {
            container.innerHTML = '<div style="text-align:center;color:red;padding:10px;">加载失败</div>';
        }
    } catch (e) {
        console.error(e);
        container.innerHTML = '<div style="text-align:center;color:red;padding:10px;">网络错误</div>';
    }
}

async function sendMsg() {
    if (!currentReceiverId) return;
    const input = document.getElementById('chat-input');
    const content = input.value.trim();
    if (!content) return;
    
    try {
        const formData = new FormData();
        formData.append('receiveId', currentReceiverId);
        formData.append('content', content);
        
        const res = await fetch('/message/send', {
            method: 'POST',
            body: formData
        });
        
        if (res.ok) {
            const result = await res.json();
            if (result.status === 'SUCCESS' || result.statusCode === 100) {
                // Append message locally
                const container = document.getElementById('message-list');
                const div = document.createElement('div');
                div.className = 'message-bubble sent';
                div.textContent = content;
                container.appendChild(div);
                container.scrollTop = container.scrollHeight;
                input.value = '';
                
                // Update sidebar last message if exists
            } else {
                alert(result.errorMessage || '发送失败');
            }
        }
    } catch (e) {
        console.error(e);
        alert('网络错误');
    }
}

// Emoji & Kaomoji
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

function toggleKaomojiPicker(e) {
    e.stopPropagation();
    const picker = document.getElementById('kaomoji-picker');
    document.getElementById('emoji-picker').classList.remove('active');
    
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
    // Hide pickers
    document.getElementById('kaomoji-picker').classList.remove('active');
    document.getElementById('emoji-picker').classList.remove('active');
}

// Close pickers on click outside
document.addEventListener('click', () => {
    const k = document.getElementById('kaomoji-picker');
    const e = document.getElementById('emoji-picker');
    if(k) k.classList.remove('active');
    if(e) e.classList.remove('active');
});

if(document.getElementById('kaomoji-picker')) {
    document.getElementById('kaomoji-picker').addEventListener('click', e => e.stopPropagation());
}
if(document.getElementById('emoji-picker')) {
    document.getElementById('emoji-picker').addEventListener('click', e => e.stopPropagation());
}

// --- 查看用户树洞相关功能 ---

let currentTargetHoleUserId = null;

function showTargetUserHoles(userId, nickname) {
    currentTargetHoleUserId = userId;
    document.getElementById('target-user-holes-title').innerText = `${nickname} 的树洞`;
    document.getElementById('target-user-holes-modal').classList.add('show');
    document.getElementById('target-user-holes-modal').style.display = 'flex'; // Ensure flex for centering
    loadTargetUserHoles(userId);
}

function closeTargetUserHoles() {
    document.getElementById('target-user-holes-modal').classList.remove('show');
    setTimeout(() => {
        document.getElementById('target-user-holes-modal').style.display = 'none';
    }, 300);
}

async function loadTargetUserHoles(userId) {
    const container = document.getElementById('target-user-holes-list');
    container.innerHTML = '<div style="text-align:center;padding:20px;color:#999;">加载中...</div>';
    
    try {
        const res = await fetch(`/hole/userHole?targetUserId=${userId}`);
        if (res.ok) {
            const holes = await res.json();
            if (holes.length === 0) {
                container.innerHTML = '<div style="text-align:center;padding:20px;color:#999;">TA还没有发布过树洞哦</div>';
                return;
            }
            
            let html = '';
            holes.forEach(hole => {
                const date = new Date(hole.createTime).toLocaleString();
                // 根据分类设置边框颜色
                let borderColor = '#ddd';
                if(hole.category === 'day') borderColor = '#d4b106';
                if(hole.category === 'night') borderColor = '#096dd9';
                if(hole.category === 'happy') borderColor = '#faad14';
                if(hole.category === 'unhappy') borderColor = '#a8a8a8';
                
                const likeClass = hole.isLiked ? 'liked' : '';
                const likeIcon = hole.isLiked ? 'ri-heart-fill' : 'ri-heart-line';
                
                html += `
                    <div class="hole-card" style="border-left: 4px solid ${borderColor}; padding: 15px; margin-bottom: 15px; background: rgba(255,255,255,0.8); border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                        <div class="hole-header" style="margin-bottom: 10px;">
                            <span class="post-time">${date}</span>
                            <span class="category-tag">${getCategoryName(hole.category)}</span>
                        </div>
                        <div class="hole-body" style="font-size: 14px; margin-bottom: 10px; line-height: 1.5;">${hole.content}</div>
                        <div class="hole-footer" style="padding-top: 10px; border-top: 1px solid rgba(0,0,0,0.05); display: flex; gap: 20px;">
                            <div class="action-btn ${likeClass}" onclick="toggleTargetHoleLike(this, ${hole.id})" style="cursor: pointer; display: flex; align-items: center; gap: 5px;">
                                <i class="${likeIcon}"></i> 
                                <span class="count">${hole.likeCount || 0}</span>
                            </div>
                            <!-- 展示评论数，点击查看评论 -->
                            <div class="action-btn" onclick="toggleTargetHoleComments(this, ${hole.id})" style="cursor: pointer; display: flex; align-items: center; gap: 5px; color: var(--text-sub);">
                                <i class="ri-message-3-line"></i> 
                                <span class="count">${hole.commentCount || 0}</span>
                            </div>
                        </div>
                        <!-- 评论区容器 -->
                        <div id="hole-comments-${hole.id}" class="hole-comments-section"></div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
             container.innerHTML = '<div style="text-align:center;padding:20px;color:red;">加载失败</div>';
        }
    } catch (e) {
        console.error(e);
        container.innerHTML = '<div style="text-align:center;padding:20px;color:red;">网络错误</div>';
    }
}

function getCategoryName(key) {
    const map = {
        'day': '☀️ 白日',
        'night': '🌙 黑夜',
        'happy': '😄 开心',
        'unhappy': '😞 不开心'
    };
    return map[key] || '🌲 树洞';
}

async function toggleTargetHoleLike(btn, holeId) {
    try {
        const res = await fetch(`/hole/like?id=${holeId}`);
        if (res.ok) {
            const isLiked = await res.json(); 
            
            const icon = btn.querySelector('i');
            const countSpan = btn.querySelector('.count');
            let count = parseInt(countSpan.innerText);
            
            if (isLiked) {
                btn.classList.add('liked');
                icon.className = 'ri-heart-fill';
                countSpan.innerText = count + 1;
                btn.style.color = '#ff7675';
            } else {
                btn.classList.remove('liked');
                icon.className = 'ri-heart-line';
                countSpan.innerText = Math.max(0, count - 1);
                btn.style.color = '';
            }
        }
    } catch (e) {
        console.error(e);
    }
}