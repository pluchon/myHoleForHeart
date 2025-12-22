let isLoading = false;
let currentPage = 1;
let hasMore = true;
let currentUser = null;

// Toast 提示函数
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = '<i class="ri-information-line"></i>';
    if (type === 'success') icon = '<i class="ri-checkbox-circle-line"></i>';
    if (type === 'error') icon = '<i class="ri-close-circle-line"></i>';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    // 强制重绘以触发动画
    toast.offsetHeight;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentElement) {
                toast.parentElement.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// 替换原生 alert
window.alert = function(msg) {
    showToast(msg, 'info');
};

// 通知相关逻辑
function showNotificationModal() {
    if (!currentUser) {
        showToast("请先登录查看通知", "error");
        return;
    }
    
    const modal = document.getElementById('notification-modal');
    modal.style.display = 'flex';
    modal.offsetHeight; // force reflow
    modal.classList.add('show');
    
    loadNotifications();
}

function closeNotificationModal() {
    const modal = document.getElementById('notification-modal');
    modal.classList.remove('show');
    setTimeout(() => {
        modal.style.display = 'none';
    }, 300);
}

async function loadNotifications() {
    const list = document.getElementById('notification-list');
    list.innerHTML = '<div style="text-align:center; padding:20px; color:#999;">加载中...</div>';
    
    try {
        const res = await fetch('/hole/getLikeMessage');
        if (res.ok) {
            const msgs = await res.json();
            if (!msgs || msgs.length === 0) {
                list.innerHTML = `
                    <div class="empty-notif">
                        <i class="ri-notification-off-line" style="font-size: 32px; display: block; margin-bottom: 10px;"></i>
                        暂无新的点赞通知
                    </div>
                `;
                return;
            }
            
            let html = '';
            msgs.forEach(msg => {
                // 点击头像查看用户树洞 (只读模式)
                const avatarClick = `onclick="showTargetUserHoles(${msg.userId}, '${escapeHtml(msg.nickname)}', true)"`;
                
                html += `
                    <div class="notification-item">
                        <img src="${msg.avatar || '/picture/user-default.png'}" class="notif-avatar" ${avatarClick} style="cursor: pointer;" title="点击查看TA的树洞">
                        <div class="notif-content">
                            <div class="notif-header">
                                <span class="notif-nickname" ${avatarClick} style="cursor: pointer;">${escapeHtml(msg.nickname)}</span>
                                <span class="notif-action">赞了你的树洞</span>
                            </div>
                            <div class="notif-hole-text">${escapeHtml(msg.holeContent)}</div>
                        </div>
                    </div>
                `;
            });
            list.innerHTML = html;
        } else {
            list.innerHTML = '<div class="empty-notif">加载失败</div>';
        }
    } catch (e) {
        console.error(e);
        list.innerHTML = '<div class="empty-notif">网络错误</div>';
    }
}

// 初始化
function init() {
    checkLoginStatus();
    // 初始化背景和主题，传入 false 防止二次加载 (因为 onCategoryChange 内部现在会调用 loadHoles)
    onCategoryChange(false);
    // 初始加载一次 (或者让 onCategoryChange(true) 来做)
    // 既然 onCategoryChange 负责根据分类加载，那我们就让它来加载
    loadHoles(true);
}

// 重置回首页默认状态
function resetToDefault() {
    const categorySelect = document.getElementById('hole-category');
    if (categorySelect) {
        categorySelect.value = 'all';
        onCategoryChange(true);
    }
}

function setTheme(isDark) {
    isDarkMode = isDark;
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
}

// 监听分类变化，自动切换氛围
function onCategoryChange(shouldReload = true) {
    const categorySelect = document.getElementById('hole-category');
    if (!categorySelect) return;
    
    const category = categorySelect.value;
    const root = document.documentElement;

    // 定义每个分类的配置
    const config = {
        'all': {
             // 默认全部分类使用一个中性或默认的背景，或者和 Day 保持一致
             image: '/picture/defaultForest.jpg',
             dark: false,
             primary: '#76d275',
             hover: '#4caf50'
        },
        'happy': { 
            image: '/picture/happyForest.jpg', 
            dark: false,
            primary: '#76d275',
            hover: '#4caf50'
        },
        'unhappy': { 
            image: '/picture/unhappyForest.jpg', 
            dark: true,
            primary: '#2e7d32',
            hover: '#1b5e20'
        },
        'day': { 
            image: '/picture/dayForest.jpg', 
            dark: false,
            primary: '#76d275',
            hover: '#4caf50'
        },
        'night': { 
            image: '/picture/nightForest.jpg', 
            dark: true,
            primary: '#2e7d32',
            hover: '#1b5e20'
        }
    };

    const currentConfig = config[category] || config['all'];

    // 设置主题模式
    setTheme(currentConfig.dark);
    
    // 设置背景图片
    // root.style.setProperty('--bg-image', `url('${currentConfig.image}')`);
    // Ensure the URL is correctly quoted
    root.style.setProperty('--bg-image', `url("${currentConfig.image}")`);

    // 设置主题色
    root.style.setProperty('--primary-color', currentConfig.primary);
    root.style.setProperty('--primary-hover', currentConfig.hover);

    // 重新加载列表
    if (shouldReload) {
        // 重置页码
        currentPage = 1;
        hasMore = true;
        // 清空列表
        const container = document.getElementById('feed-container');
        if(container) container.innerHTML = '<div class="empty-tip">加载中...</div>';
        
        loadHoles(true);
    }
}

async function checkLoginStatus() {
    try {
        const res = await fetch('/user/info');
        if (res.ok) {
            // 如果未登录，后端可能返回空或者null
            const text = await res.text();
            if (!text) {
                renderLoginLink();
                return;
            }
            const user = JSON.parse(text);
            if (user && user.id) {
                currentUser = user;
                renderUserInfo(user);
            } else {
                renderLoginLink();
            }
        } else {
            renderLoginLink();
        }
    } catch (e) {
        console.error(e);
        renderLoginLink();
    }
}

function renderLoginLink() {
    const userStatusDiv = document.getElementById('user-status');
    userStatusDiv.innerHTML = `
        <span>想看更多？</span>
        <a href="login.html" class="btn-link">去登录/注册</a>
    `;
}

function renderUserInfo(user) {
    const userStatusDiv = document.getElementById('user-status');
    // Add timestamp to avoid cache
    const avatarUrl = user.avatar ? (user.avatar + '?t=' + new Date().getTime()) : '';
    const avatarHtml = user.avatar 
        ? `<img src="${avatarUrl}" style="width: 24px; height: 24px; border-radius: 50%; object-fit: cover;">`
        : `<i class="ri-user-line" style="font-size: 20px;"></i>`;
        
    userStatusDiv.innerHTML = `
        <a href="message.html" class="nav-icon-btn" title="消息中心">
            <i class="ri-mail-line"></i>
        </a>
        <span style="cursor: pointer; display: flex; align-items: center; gap: 5px;" onclick="showUserCenter()">
            ${avatarHtml}
            <span class="btn-link">${escapeHtml(user.nickname || user.username)}</span>
        </span>
    `;
}

function showPublishModal() {
    // Check login first
    if (!currentUser) {
        showToast("请先登录", "error");
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 1500);
        return;
    }
    const modal = document.getElementById('publish-modal');
    modal.style.display = 'flex';
    // 强制重绘
    modal.offsetHeight;
    modal.classList.add('show');
    
    // Auto focus
    setTimeout(() => {
        document.getElementById('hole-content').focus();
    }, 100);
}

function closePublishModal() {
    const modal = document.getElementById('publish-modal');
    modal.classList.remove('show');
    setTimeout(() => {
        modal.style.display = 'none';
    }, 300);
}

function showUserCenter() {
    const modal = document.getElementById('user-center-modal');
    modal.style.display = 'flex';
    // 强制重绘
    modal.offsetHeight;
    modal.classList.add('show');
    
    if (currentUser) {
        document.getElementById('uc-nickname-input').value = currentUser.nickname || '';
        document.getElementById('uc-username').innerText = '@' + currentUser.username;
        
        const img = document.getElementById('uc-avatar-img');
        const defaultIcon = document.getElementById('uc-avatar-default');
        
        if (currentUser.avatar) {
            img.src = currentUser.avatar;
            img.style.display = 'block';
            defaultIcon.style.display = 'none';
        } else {
            img.style.display = 'none';
            defaultIcon.style.display = 'flex'; // Ensure flex for centering
        }
        
        loadMyHoles();
    }
}

function previewAvatar(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.getElementById('uc-avatar-img');
            const defaultIcon = document.getElementById('uc-avatar-default');
            img.src = e.target.result;
            img.style.display = 'block';
            defaultIcon.style.display = 'none';
        }
        reader.readAsDataURL(input.files[0]);
    }
}

async function saveUserInfo() {
    const nickname = document.getElementById('uc-nickname-input').value.trim();
    const avatarInput = document.getElementById('avatar-input');

    // 昵称校验
    if (nickname) {
        if (nickname.length < 6 || nickname.length > 20) {
            showToast('昵称长度必须在 6-20 个字符之间', 'error');
            return;
        }
        if (!/^(?!\d+$)\S+$/.test(nickname)) {
            showToast('昵称不能为纯数字且不能包含空格', 'error');
            return;
        }
    }
    
    // 客户端检查文件大小 (例如限制为 10MB)
    if (avatarInput.files[0] && avatarInput.files[0].size > 10 * 1024 * 1024) {
        showToast('头像图片大小不能超过 10MB', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('nickname', nickname);
    if (avatarInput.files[0]) {
        formData.append('avatarFile', avatarInput.files[0]);
    }
    
    try {
        const res = await fetch('/user/update', {
            method: 'POST',
            body: formData
        });

        if (res.status === 413) {
            showToast('上传的文件过大，请选择更小的图片', 'error');
            return;
        }

        if (!res.ok) {
            showToast('请求失败: ' + res.status, 'error');
            return;
        }

        let success = false;
        try {
            success = await res.json();
        } catch (err) {
             console.error('JSON Parse Error:', err);
             // 有可能后端返回了 HTML 错误页
             showToast('服务器响应格式错误，可能是文件过大或服务器内部错误', 'error');
             return;
        }

        if (success) {
            showToast('修改成功', 'success');
            // Reload user info
            checkLoginStatus();
            // Close modal
            closeUserCenter();
        } else {
            showToast('修改失败', 'error');
        }
    } catch (e) {
        console.error(e);
        showToast('网络错误: ' + e.message, 'error');
    }
}

function closeUserCenter() {
    const modal = document.getElementById('user-center-modal');
    modal.classList.remove('show');
    setTimeout(() => {
        modal.style.display = 'none';
        // Reset password area
        document.getElementById('password-change-area').style.display = 'none';
        document.getElementById('old-password').value = '';
        document.getElementById('new-password').value = '';
    }, 300);
}

function toggleChangePassword() {
    const area = document.getElementById('password-change-area');
    if (area.style.display === 'none') {
        area.style.display = 'block';
    } else {
        area.style.display = 'none';
    }
}

async function submitChangePassword() {
    const oldPassword = document.getElementById('old-password').value;
    const newPassword = document.getElementById('new-password').value;
    
    if (!oldPassword || !newPassword) {
        showToast("请输入旧密码和新密码", "error");
        return;
    }
    
    // 前端简单校验新密码
    if (newPassword.length < 6 || newPassword.length > 20) {
        showToast('新密码长度必须在 6-20 个字符之间', 'error');
        return;
    }
    
    try {
        const formData = new FormData();
        formData.append('oldPassword', oldPassword);
        formData.append('newPassword', newPassword);
        
        const res = await fetch('/user/updatePassword', {
            method: 'POST',
            body: formData
        });
        
        const result = await res.json();
        
        console.log("Update Password Result:", result);

        // 优先使用 statusCode (数字), 兼容 status (字符串或枚举对象)
        if ((result.statusCode && result.statusCode === 100) || 
            result.status === 'SUCCESS' || 
            result.status === 100) { 
            
            showToast("密码修改成功", "success");
            toggleChangePassword();
            document.getElementById('old-password').value = '';
            document.getElementById('new-password').value = '';
        } else {
            // 调试用：显示具体返回了什么
            const debugInfo = JSON.stringify(result);
            showToast("修改失败: " + (result.errorMessage || "未知错误"), "error");
        }
    } catch (e) {
        console.error(e);
        showToast("网络请求失败", "error");
    }
}

async function loadMyHoles() {
    const container = document.getElementById('my-holes-list');
    container.innerHTML = '<div style="text-align: center; color: #999; padding: 20px;">加载中...</div>';
    try {
        const res = await fetch('/hole/myHole');
        const list = await res.json();
        
        if (!list || list.length === 0) {
            container.innerHTML = '<div style="text-align: center; color: #999; padding: 20px;">你还没有发布过树洞哦</div>';
            return;
        }
        
        let html = '';
        list.forEach(hole => {
             const timeDisplay = hole.createTime ? new Date(hole.createTime).toLocaleString() : '刚刚';
             html += `
                <div class="mini-hole-card" id="mini-hole-${hole.id}">
                    <div class="mini-content">${escapeHtml(hole.content)}</div>
                    <div class="mini-meta">
                        <span>${timeDisplay}</span>
                        <span><i class="ri-heart-line"></i> ${hole.likeCount || 0}</span>
                        <span onclick="deleteHole(${hole.id}, true)" style="color: #ff4d4f; cursor: pointer; margin-left: 10px;"><i class="ri-delete-bin-line"></i> 删除</span>
                    </div>
                </div>
             `;
        });
        container.innerHTML = html;
    } catch (e) {
        console.error(e);
        container.innerHTML = '<div style="text-align: center; color: #999; padding: 20px;">加载失败</div>';
    }
}

// 展开/收起评论区
async function toggleComments(holeId) {
    const section = document.getElementById(`comment-section-${holeId}`);
    const listContainer = document.getElementById(`comment-list-${holeId}`);
    
    if (section.style.display === 'none') {
        section.style.display = 'block';
        // 每次打开都重新加载评论，以确保获取最新数据
        loadComments(holeId);
    } else {
        section.style.display = 'none';
    }
}

// 加载评论列表
async function loadComments(holeId) {
    const listContainer = document.getElementById(`comment-list-${holeId}`);
    listContainer.innerHTML = '<div class="loading-comments">加载评论中...</div>';
    
    try {
        const res = await fetch(`/comment/getComments?holeId=${holeId}`);
        const list = await res.json();
        
        if (!list || list.length === 0) {
            listContainer.innerHTML = '<div class="loading-comments">暂无评论，快来抢沙发~</div>';
            return;
        }
        
        let html = '';
        list.forEach(comment => {
            const timeStr = comment.createTime ? new Date(comment.createTime).toLocaleString() : '刚刚';
            const userIdStr = (comment.userId === 0 || !comment.userId) ? '匿名用户' : `用户 ${comment.userId}`;
            const isLiked = comment.isLiked ? 'liked' : '';
            const likeIconClass = comment.isLiked ? 'ri-heart-fill' : 'ri-heart-line';
            const likeCount = comment.likeCount || 0;
            
            html += `
                <div class="comment-item animate-fade-in">
                    <div class="comment-avatar"><i class="ri-chat-1-line"></i></div>
                    <div class="comment-content-box">
                        <div class="comment-user">${userIdStr}</div>
                        <div class="comment-text">${escapeHtml(comment.content)}</div>
                        <div class="comment-footer">
                            <div class="comment-time">${timeStr}</div>
                            <div class="comment-actions">
                                <span class="like-btn ${isLiked}" onclick="likeComment(${comment.id}, this)">
                                    <i class="${likeIconClass}"></i> <span class="count">${likeCount}</span>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        listContainer.innerHTML = html;
    } catch (e) {
        console.error(e);
        listContainer.innerHTML = '<div class="loading-comments" style="color:red;">加载失败</div>';
    }
}

// 评论点赞
async function likeComment(commentId, btn) {
    // 乐观更新：立即切换 UI 状态
    const isLiked = btn.classList.contains('liked');
    const icon = btn.querySelector('i');
    const countSpan = btn.querySelector('.count');
    let count = parseInt(countSpan.innerText) || 0;

    // 预先切换样式
    if (isLiked) {
        // 变为未赞
        btn.classList.remove('liked');
        icon.classList.remove('ri-heart-fill');
        icon.classList.add('ri-heart-line');
        countSpan.innerText = Math.max(0, count - 1);
    } else {
        // 变为已赞
        btn.classList.add('liked');
        icon.classList.remove('ri-heart-line');
        icon.classList.add('ri-heart-fill');
        countSpan.innerText = count + 1;
        // 动画
        icon.style.transform = 'scale(1.3)';
        setTimeout(() => icon.style.transform = 'scale(1)', 200);
    }

    try {
        const res = await fetch(`/comment/like?commentId=${commentId}`, { method: 'POST' });
        
        // 检查未登录状态 (401)
        if (res.status === 401) {
             showToast("请先登录后点赞", "error");
             // 回滚状态
             throw new Error("未登录");
        }

        // 后端现在返回的是操作后的最终状态：true=已赞，false=未赞
        // 如果网络请求失败或未登录，我们需要回滚状态，但如果是正常响应，则不需要做额外操作，因为我们已经乐观更新了
        
        if (!res.ok) {
            throw new Error('Network response was not ok');
        }
        
        const finalIsLiked = await res.json();
        
        // 校验后端返回的状态是否与我们的乐观更新一致
        // 我们的预期：如果是 isLiked (原本已赞)，操作后应该是 false (未赞)
        // 如果 !isLiked (原本未赞)，操作后应该是 true (已赞)
        // 如果不一致，说明可能后端判断不一致（比如未登录），这里可以做一次强制同步
        
        const expectedState = !isLiked;
        if (finalIsLiked !== expectedState) {
            // 状态不一致，回滚或强制同步为后端状态
             if (finalIsLiked) {
                btn.classList.add('liked');
                icon.classList.remove('ri-heart-line');
                icon.classList.add('ri-heart-fill');
                // 这里很难准确回滚数字，最好重新拉取，或者简单 +1/-1 修正
                if (!isLiked) { 
                    // 我们刚才 +1 了，结果后端说没赞成？那再 -1
                    // 这种情况比较少见，通常是未登录导致直接返回 false（这里后端逻辑里未登录应该返回 false 或 error）
                    // 简单起见，如果后端返回 false，我们确保 UI 是 false
                }
            } else {
                btn.classList.remove('liked');
                icon.classList.remove('ri-heart-fill');
                icon.classList.add('ri-heart-line');
            }
        }

    } catch (e) {
        if (e.message !== "未登录") console.error(e);
        // showToast("操作失败，请重试", "error"); // 已经在上面提示了
        // 回滚 UI
        if (isLiked) {
            btn.classList.add('liked');
            icon.classList.remove('ri-heart-line');
            icon.classList.add('ri-heart-fill');
            countSpan.innerText = count;
        } else {
            btn.classList.remove('liked');
            icon.classList.remove('ri-heart-fill');
            icon.classList.add('ri-heart-line');
            countSpan.innerText = count;
        }
    }
}

// 提交评论
async function submitComment(holeId) {
    const input = document.getElementById(`comment-input-${holeId}`);
    const content = input.value.trim();
    
    if (!content) {
        showToast("评论内容不能为空", "error");
        return;
    }
    
    try {
        const formData = new FormData();
        formData.append('holeId', holeId);
        formData.append('content', content);
        
        const res = await fetch('/comment/add', {
            method: 'POST',
            body: formData
        });
        
        // 解析返回结果
        const result = await res.json();
        
        // 检查状态码：100 或 SUCCESS 表示成功
        if ((result.statusCode && result.statusCode === 100) || result.status === 'SUCCESS') {
            input.value = '';
            // 刷新评论列表
            loadComments(holeId);
            // 更新评论数显示
            const countSpan = document.getElementById(`comment-count-${holeId}`);
            if (countSpan) {
                let count = parseInt(countSpan.innerText) || 0;
                countSpan.innerText = count + 1;
            }
            showToast("评论成功", "success");
        } else if ((result.statusCode && result.statusCode === -10) || result.status === 'NOT_LOGIN') {
            showToast("请先登录发表评论", "error");
        } else {
            showToast(result.errorMessage || "评论失败", "error");
        }
        
    } catch (e) {
        console.error(e);
        showToast("网络错误，请重试", "error");
    }
}

async function logout() {
    if(!confirm('确定要退出登录吗？')) return;
    try {
        await fetch('/user/logout');
        location.reload();
    } catch (e) {
        showToast('退出失败', 'error');
    }
}

async function publishHole() {
    const content = document.getElementById('hole-content').value;
    // 优先使用弹窗内的分类选择，如果不存在则回退（虽然弹窗模式下应该存在）
    const categorySelect = document.getElementById('publish-category');
    const category = categorySelect ? categorySelect.value : document.getElementById('hole-category').value;
    
    if (!content) {
        showToast("请填写内容！", "error");
        return;
    }
    
    if (content.length < 10 || content.length > 200) {
        showToast("内容长度需在 10 - 200 字之间", "error");
        return;
    }
    
    // 简单的防抖/防重复提交
    // 尝试获取弹窗内的按钮，或者通用的
    const submitBtn = document.querySelector('#publish-modal .btn-submit') || document.querySelector('.btn-submit');
    if(submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = '发布中...';
    }

    const hole = {
        content: content,
        category: category
    };

    try {
        const res = await fetch('/hole/publish', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(hole)
        });

        const result = await res.json();
        
        // 判断结果
        // 兼容 status (String) 和 statusCode (Integer)
        // SUCCESS(100)
        if ((result.statusCode && result.statusCode === 100) || result.status === 'SUCCESS') {
            showToast(result.errorMessage || "发布成功!", "success"); // 后端 errorMessage 现在是 "发布成功"
            document.getElementById('hole-content').value = ''; // 清空
            closePublishModal(); // 关闭弹窗
            loadHoles(true); // 刷新列表，回到第一页
        } else if ((result.statusCode && result.statusCode === -10) || result.status === 'NOT_LOGIN') {
             // 未登录
             showToast("请先登录", "error");
             // 1.5秒后跳转登录页
             setTimeout(() => {
                 window.location.href = 'login.html';
             }, 1500);
        } else {
            // 其他错误
            showToast(result.errorMessage || "发布失败，请稍后重试", "error");
        }
    } catch (e) {
        console.error(e);
        showToast("网络错误", "error");
    } finally {
        if(submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = '发布';
        }
    }
}

// 删除树洞
async function deleteHole(holeId, isMiniCard = false) {
    if (!confirm("确定要删除这条树洞吗？此操作不可恢复。")) return;

    try {
        const res = await fetch(`/hole/deleteByUser?id=${holeId}`);
        const success = await res.json();
        
        if (success) {
            showToast("删除成功", "success");
            // 移除 DOM 元素
            if (isMiniCard) {
                const card = document.getElementById(`mini-hole-${holeId}`);
                if (card) {
                    card.style.opacity = '0';
                    setTimeout(() => {
                        card.remove();
                        // 如果删完了，显示空提示
                        const container = document.getElementById('my-holes-list');
                        if (container.children.length === 0) {
                            container.innerHTML = '<div style="text-align: center; color: #999; padding: 20px;">你还没有发布过树洞哦</div>';
                        }
                        
                        // 强制刷新主页列表
                        loadHoles(true);
                    }, 300);
                }
            } else {
                // 原来的主列表删除逻辑（虽然现在不在主列表删了，但保留着也没坏处，万一以后又要加）
                const card = document.getElementById(`hole-card-${holeId}`);
                if (card) {
                    card.style.transition = 'all 0.3s ease';
                    card.style.opacity = '0';
                    card.style.transform = 'scale(0.9)';
                    setTimeout(() => card.remove(), 300);
                } else {
                    loadHoles(true);
                }
            }
        } else {
            showToast("删除失败，可能您没有权限或树洞不存在", "error");
        }
    } catch (e) {
        console.error(e);
        showToast("请求出错", "error");
    }
}

// 加载树洞列表
async function loadHoles(isRefresh = false) {
    if (isLoading) return;
    
    const container = document.getElementById('feed-container');
    const loadMoreBtn = document.getElementById('load-more-btn');

    if (isRefresh) {
        currentPage = 1;
        hasMore = true;
        container.innerHTML = ''; // 清空列表
        // 重置加载更多按钮状态
        if(loadMoreBtn) {
            loadMoreBtn.style.display = 'none';
            loadMoreBtn.innerText = '加载更多';
            loadMoreBtn.disabled = false;
        }
        isLoading = false; // 确保强制刷新时重置loading状态，防止死锁
    }

    if (!hasMore) return;

    isLoading = true;
    // 如果不是刷新，且没有loading提示，可以加一个临时的
    if (!isRefresh && loadMoreBtn) {
        loadMoreBtn.innerText = '加载中...';
    } else if (isRefresh) {
        // 这里原来会显示加载中，现在我们留白，或者用一个更优雅的骨架屏
        // container.innerHTML = '<div class="empty-tip">加载中...</div>'; 
        // 暂时什么都不做，或者给个小转圈
    }

    try {
        const categorySelect = document.getElementById('hole-category');
        const category = categorySelect ? categorySelect.value : 'all';
        
        let url = `/hole/getHoleContent?page=${currentPage}`;
        if (category && category !== 'all') {
            url += `&category=${category}`;
        }

        const res = await fetch(url);
        const list = await res.json();

        // 如果是刷新且没数据
        if (isRefresh) {
            // container.innerHTML = ''; // 清掉可能存在的"加载中"
            if (!list || list.length === 0) {
                container.innerHTML = '<div class="empty-tip">暂无内容，快来发布第一条树洞吧~</div>';
                isLoading = false;
                return;
            }
        }

        // 如果是加载更多但没数据了
        if (!list || list.length === 0) {
            hasMore = false;
            isLoading = false;
            if (loadMoreBtn) {
                loadMoreBtn.innerText = '没有更多了';
                loadMoreBtn.disabled = true;
                // 如果总数很少（比如第一页就不满），可能不需要显示这个按钮
                // 这里简单处理：只要触发了没数据，就显示没有更多
                loadMoreBtn.style.display = 'block';
            }
            return;
        }

        let html = '';
        list.forEach(hole => {
            const categoryMap = {
                'day': { icon: '<i class="ri-sun-line"></i>', text: '白日树洞', theme: 'theme-day' },
                'night': { icon: '<i class="ri-moon-line"></i>', text: '黑夜树洞', theme: 'theme-night' },
                'happy': { icon: '<i class="ri-emotion-happy-line"></i>', text: '开心树洞', theme: 'theme-happy' },
                'unhappy': { icon: '<i class="ri-emotion-unhappy-line"></i>', text: '不开心树洞', theme: 'theme-unhappy' }
            };
            
            const catInfo = categoryMap[hole.category] || { icon: '<i class="ri-tree-line"></i>', text: '树洞', theme: '' };
            
            let avatarHtml = `<span class="user-avatar" style="display:flex;align-items:center;justify-content:center;">${catInfo.icon}</span>`;
            let userDisplay = '匿名用户';

            // 如果有userId且不为0，显示用户信息
            if (hole.userId && hole.userId !== 0) {
                userDisplay = escapeHtml(hole.userNickname || `用户 ${hole.userId}`);
                // 转义单引号
                const safeNickname = userDisplay.replace(/'/g, "\\'");
                const safeAvatar = (hole.userAvatar || '').replace(/'/g, "\\'");
                // 添加点击事件 (事件委托到 hole-header 上可能更好，但这里直接绑在元素上也可以，注意 z-index)
                // 注意：如果 event.stopPropagation() 没有被调用，点击头像可能会触发卡片的其他点击事件（如果有的话）
                // 我们在 showUserCard 里处理一下
                // 修复：确保 safeNickname 和 safeAvatar 已经正确转义
                const clickAttr = `onclick="event.stopPropagation(); showUserCard(${hole.userId}, this, '${safeNickname}', '${safeAvatar}')" style="cursor: pointer; position: relative; z-index: 10;"`;

                if (hole.userAvatar) {
                    avatarHtml = `<img src="${hole.userAvatar}" class="user-avatar-img" ${clickAttr}>`;
                } else {
                    // 已登录但无头像，显示默认头像图标
                    avatarHtml = `<span class="user-avatar" style="background:#f0f2f5; color:#666; display:flex;align-items:center;justify-content:center;" ${clickAttr}><i class="ri-user-line"></i></span>`;
                }
            }
            
            // 简单的日期格式化 (如果没有 createTime 字段，可能需要调整)
            const timeDisplay = hole.createTime ? new Date(hole.createTime).toLocaleString() : '刚刚';

            // 判断是否已点赞，添加样式
            const likedClass = hole.isLiked ? 'liked' : '';
            const likedStyle = hole.isLiked ? 'style="color: #ff7675;"' : '';
            const heartIcon = hole.isLiked ? '<i class="ri-heart-3-fill"></i>' : '<i class="ri-heart-3-line"></i>';

            html += `
                <div class="hole-card animate-fade-in ${catInfo.theme}" id="hole-card-${hole.id}">
                    <div class="hole-header">
                        ${avatarHtml}
                        <div class="user-meta">
                            <div class="username">${userDisplay}</div>
                            <div class="post-time">${timeDisplay} · <span class="category-tag">${catInfo.text}</span></div>
                        </div>
                    </div>
                    <div class="hole-body">${escapeHtml(hole.content)}</div>
                    <div class="hole-footer">
                        <span class="action-btn ${likedClass}" ${likedStyle} onclick="likeHole(${hole.id}, this)">
                            <span class="heart-icon">${heartIcon}</span> 
                            <span class="like-count">${hole.likeCount || 0}</span>
                        </span>
                        <span class="action-btn" onclick="toggleComments(${hole.id})">
                            <i class="ri-chat-1-line"></i> <span id="comment-count-${hole.id}">${hole.commentCount || 0}</span>
                        </span>
                        <span class="action-btn"><i class="ri-share-forward-line"></i> ${hole.forwardCount || 0}</span>
                    </div>
                    
                    <!-- 评论区 (默认隐藏) -->
                    <div id="comment-section-${hole.id}" class="comment-section" style="display: none;">
                        <div class="comment-input-area">
                            <input type="text" id="comment-input-${hole.id}" class="comment-input" placeholder="写下你的评论..." onkeydown="if(event.key === 'Enter') submitComment(${hole.id})">
                            <button class="comment-submit-btn" onclick="submitComment(${hole.id})">发送</button>
                        </div>
                        <div id="comment-list-${hole.id}" class="comment-list">
                            <!-- 评论加载区 -->
                        </div>
                    </div>
                </div>
            `;
        });
        
        // 插入HTML
        container.insertAdjacentHTML('beforeend', html);
        
        // 准备下一页
        currentPage++;
        
        // 维护加载按钮状态
        // 只有当获取到的数据数量等于 pageSize (15) 时，才显示加载更多，说明可能还有下一页
        // 如果小于 15，说明是最后一页了
        if(loadMoreBtn) {
            if (list.length >= 15) {
                loadMoreBtn.style.display = 'block';
                loadMoreBtn.innerText = '加载更多';
                loadMoreBtn.disabled = false;
            } else {
                // 如果第一页就不足15条，直接隐藏按钮；或者如果是加载更多后发现不足15条，显示没有更多
                // 逻辑优化：如果是第一页且不足15条 -> 隐藏
                if (currentPage === 2 && isRefresh) { 
                    loadMoreBtn.style.display = 'none';
                } else {
                    // 到了某一页不足15条，说明结束了
                    loadMoreBtn.innerText = '没有更多了';
                    loadMoreBtn.disabled = true;
                    loadMoreBtn.style.display = 'block';
                    hasMore = false;
                }
            }
        }

    } catch (e) {
        console.error(e);
        if (isRefresh) container.innerHTML = '<div class="empty-tip">加载失败，请刷新重试</div>';
        else showToast("加载失败", "error");
    } finally {
        isLoading = false;
    }
}

// 喜欢/点赞
async function likeHole(id, btn) {
    // 乐观更新
    const isLiked = btn.classList.contains('liked');
    const countSpan = btn.querySelector('.like-count');
    const iconSpan = btn.querySelector('.heart-icon');
    let count = parseInt(countSpan.innerText) || 0;

    // 预先切换样式
    if (isLiked) {
        // 变为未赞
        btn.classList.remove('liked');
        btn.style.color = ''; // 移除内联颜色，恢复默认
        iconSpan.innerHTML = '<i class="ri-heart-3-line"></i>';
        countSpan.innerText = Math.max(0, count - 1);
    } else {
        // 变为已赞
        btn.classList.add('liked');
        btn.style.color = '#ff7675';
        iconSpan.innerHTML = '<i class="ri-heart-3-fill"></i>';
        countSpan.innerText = count + 1;
        // 动画
        const icon = iconSpan.querySelector('i');
        if (icon) {
            icon.style.transform = 'scale(1.5)';
            setTimeout(() => icon.style.transform = 'scale(1)', 200);
        }
    }
    
    try {
        const res = await fetch(`/hole/like?id=${id}`);
        
        // 检查未登录状态 (401)
        if (res.status === 401) {
             showToast("请先登录后点赞", "error");
             // 回滚状态
             throw new Error("未登录");
        }
        
        if (!res.ok) {
            throw new Error('Network response was not ok');
        }
        
        const finalIsLiked = await res.json();
        const expectedState = !isLiked;
        
        if (finalIsLiked !== expectedState) {
            if (finalIsLiked) {
                btn.classList.add('liked');
                btn.style.color = '#ff7675';
                iconSpan.innerHTML = '<i class="ri-heart-3-fill"></i>';
            } else {
                btn.classList.remove('liked');
                btn.style.color = '';
                iconSpan.innerHTML = '<i class="ri-heart-3-line"></i>';
                if (!isLiked) {
                    countSpan.innerText = count;
                    showToast("点赞失败或未登录", "info");
                }
            }
        }
    } catch (e) {
        if (e.message !== "未登录") console.error(e);
        // showToast("操作失败", "error");
        if (isLiked) {
            btn.classList.add('liked');
            btn.style.color = '#ff7675';
            iconSpan.innerHTML = '<i class="ri-heart-3-fill"></i>';
            countSpan.innerText = count;
        } else {
            btn.classList.remove('liked');
            btn.style.color = '';
            iconSpan.innerHTML = '<i class="ri-heart-3-line"></i>';
            countSpan.innerText = count;
        }
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// 初始化调用
// Use DOMContentLoaded to ensure elements are present
document.addEventListener('DOMContentLoaded', init);

/* --- 用户卡片 & 私信功能 --- */

let currentTargetUserId = 0;

function showUserCard(userId, element, nickname, avatar) {
    if (!currentUser) {
        showToast("请先登录", "error");
        return;
    }
    
    // 防止点自己
    if (currentUser.id === userId) {
        showToast("这是你自己哦", "info");
        return; 
    }

    currentTargetUserId = userId;
    const popup = document.getElementById('user-card-popup');
    const overlay = document.getElementById('user-card-overlay');
    
    // 生成内容
    let avatarImg = avatar ? `<img src="${avatar}" class="user-card-avatar">` : `<div class="user-card-avatar" style="background:#f0f2f5;display:flex;align-items:center;justify-content:center;"><i class="ri-user-line" style="font-size:30px;color:#999"></i></div>`;
    
    popup.innerHTML = `
        <div class="user-card-header">
            ${avatarImg}
            <div class="user-card-info">
                <div class="user-card-name">${nickname}</div>
                <div class="user-card-id">ID: ${userId}</div>
            </div>
        </div>
        <div class="user-card-actions" style="gap:10px;">
            <button class="dm-btn" title="发送私信" onclick="toggleDmInput()">
                <i class="ri-mail-send-line"></i> 快捷私信
            </button>
            <button class="dm-btn" title="完整聊天" onclick="location.href='/message.html?userId=${userId}&nickname=${encodeURIComponent(nickname)}&avatar=${encodeURIComponent(avatar || '')}'" style="background:#fff;color:var(--primary-color);border:1px solid var(--primary-color);">
                <i class="ri-chat-1-line"></i> 完整聊天
            </button>
        </div>
        <div id="dm-input-area" class="dm-input-area">
            <textarea id="dm-content" class="dm-textarea" placeholder="写下你想对TA说的话... (5-200字)" oninput="updateDmCount(this)"></textarea>
            <div class="dm-footer">
                <span class="char-count" id="dm-char-count">0/200</span>
                <button class="dm-send-btn" onclick="sendPrivateMessage()">发送</button>
            </div>
        </div>
        <div class="user-card-holes" id="user-card-holes-${userId}">
            <div style="text-align:center;color:#999;padding:10px;">加载中...</div>
        </div>
    `;

    // 计算位置
    const rect = element.getBoundingClientRect();
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
    
    // 默认显示在下方
    let top = rect.bottom + scrollTop + 10;
    let left = rect.left + scrollLeft - (280 - rect.width) / 2; // 居中
    
    // 边界检查
    if (left < 10) left = 10;
    if (left + 320 > window.innerWidth) left = window.innerWidth - 330;

    popup.style.top = `${top}px`;
    popup.style.left = `${left}px`;
    
    overlay.style.display = 'block';
    popup.classList.add('show');
    popup.style.display = 'block'; // Ensure it's block
    popup.style.display = 'flex'; // Use flex for column layout

    // 加载该用户的最近树洞
    loadUserCardHoles(userId);
}

async function loadUserCardHoles(userId) {
    const containerId = `user-card-holes-${userId}`;
    const container = document.getElementById(containerId);
    if(!container) return;

    try {
        const res = await fetch(`/hole/userHole?targetUserId=${userId}`);
        if(res.ok) {
            const holes = await res.json();
            if(holes.length === 0) {
                container.innerHTML = '<div style="text-align:center;color:#999;padding:10px;">暂无发布</div>';
                return;
            }
            let html = '';
            // 只显示最近5条
            holes.slice(0, 5).forEach(h => {
                const date = new Date(h.createTime).toLocaleDateString();
                html += `
                    <div class="mini-hole-item">
                        <div class="mini-hole-content">${h.content}</div>
                        <div class="mini-hole-meta">
                            <span>${date}</span>
                            <span>❤️ ${h.likeCount}</span>
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = '<div style="text-align:center;color:#999;padding:10px;">加载失败</div>';
        }
    } catch(e) {
        console.error(e);
        container.innerHTML = '<div style="text-align:center;color:#999;padding:10px;">网络错误</div>';
    }
}

function closeUserCard() {
    const popup = document.getElementById('user-card-popup');
    const overlay = document.getElementById('user-card-overlay');
    popup.classList.remove('show');
    popup.style.display = 'none';
    overlay.style.display = 'none';
}

function toggleDmInput() {
    const area = document.getElementById('dm-input-area');
    if (area.style.display === 'block') {
        area.style.display = 'none';
    } else {
        area.style.display = 'block';
        area.classList.add('show');
        setTimeout(() => document.getElementById('dm-content').focus(), 100);
    }
}

function updateDmCount(textarea) {
    const len = textarea.value.length;
    const countSpan = document.getElementById('dm-char-count');
    countSpan.innerText = `${len}/200`;
    if (len < 5 || len > 200) {
        countSpan.style.color = '#ff4d4f';
    } else {
        countSpan.style.color = '#999';
    }
}

async function sendPrivateMessage() {
    const content = document.getElementById('dm-content').value;
    if (!content || content.length < 5 || content.length > 200) {
        showToast("私信内容长度需在5-200字之间", "error");
        return;
    }
    
    try {
        const res = await fetch('/message/send', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `receiveId=${currentTargetUserId}&content=${encodeURIComponent(content)}`
        });
        
        const result = await res.json();
        if ((result.statusCode && result.statusCode === 100) || result.status === 'SUCCESS') {
            showToast("私信发送成功", "success");
            closeUserCard();
        } else if ((result.statusCode && result.statusCode === -10) || result.status === 'NOT_LOGIN') {
            showToast("请先登录", "error");
        } else if (result.status === 'SEND_MESSAGE_MYSELF') {
             showToast("不能给自己发私信", "warning");
        } else if (result.status === 'TEXT_INSUFFICIENT' || result.status === 'TEXT_OVER') {
             showToast("字数不符合要求", "warning");
        } else {
            showToast(result.errorMessage || "发送失败", "error");
        }
    } catch (e) {
        console.error(e);
        showToast("网络错误", "error");
    }
}

// --- 查看用户树洞 (从通知或其他入口) ---
let currentTargetHoleUserId = null;

function showTargetUserHoles(userId, nickname, readOnly = false) {
    currentTargetHoleUserId = userId;
    const title = document.getElementById('target-user-holes-title');
    if(title) title.innerText = `${nickname} 的树洞`;
    
    const modal = document.getElementById('target-user-holes-modal');
    modal.classList.add('show');
    modal.style.display = 'flex'; 
    
    loadTargetUserHoles(userId, readOnly);
}

function closeTargetUserHoles() {
    const modal = document.getElementById('target-user-holes-modal');
    modal.classList.remove('show');
    setTimeout(() => {
        modal.style.display = 'none';
    }, 300);
}

async function loadTargetUserHoles(userId, readOnly) {
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
                // 简单的分类映射
                const catMap = {
                    'day': { color: '#d4b106', text: '☀️ 白日' },
                    'night': { color: '#096dd9', text: '🌙 黑夜' },
                    'happy': { color: '#faad14', text: '😄 开心' },
                    'unhappy': { color: '#a8a8a8', text: '😞 不开心' }
                };
                const catInfo = catMap[hole.category] || { color: '#ddd', text: '🌲 树洞' };
                
                const likeIcon = hole.isLiked ? 'ri-heart-fill' : 'ri-heart-line';
                const likeColor = hole.isLiked ? '#ff7675' : '';
                
                // 如果是 readOnly，不绑定点击事件，鼠标样式为默认
                let likeAction = '';
                let cursorStyle = 'cursor: pointer;';
                if (readOnly) {
                    likeAction = ''; // 无点击事件
                    cursorStyle = 'cursor: default;';
                } else {
                    // 如果未来有非只读入口，可以绑定 toggleTargetHoleLike
                }

                html += `
                    <div class="hole-card" style="border-left: 4px solid ${catInfo.color}; padding: 15px; margin-bottom: 15px; background: rgba(255,255,255,0.8); border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                        <div class="hole-header" style="margin-bottom: 10px; display:flex; justify-content:space-between; color:#999; font-size:12px;">
                            <span class="post-time">${date}</span>
                            <span class="category-tag" style="color:${catInfo.color}">${catInfo.text}</span>
                        </div>
                        <div class="hole-body" style="font-size: 14px; margin-bottom: 10px; line-height: 1.5; color: var(--text-color);">${hole.content}</div>
                        <div class="hole-footer" style="padding-top: 10px; border-top: 1px solid rgba(0,0,0,0.05); display: flex; gap: 20px;">
                            <div class="action-btn" ${likeAction} style="${cursorStyle} display: flex; align-items: center; gap: 5px; color: ${likeColor || 'var(--text-sub)'};">
                                <i class="${likeIcon}"></i> 
                                <span class="count">${hole.likeCount || 0}</span>
                            </div>
                        </div>
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
