# 🌳 Soul Tree Hole (心灵树洞)

## 📖 项目简介
**Soul Tree Hole** 是一个基于 **Spring Boot** 和 **MyBatis** 构建的匿名社交树洞平台。项目旨在为用户提供一个安全、私密的倾诉空间。

本项目不仅仅是一个简单的 CRUD 练习，更是一个在**用户交互体验**、**安全验证机制**和**业务逻辑完整性**上追求极致的 Web 全栈项目。前端完全采用**原生 JavaScript** 实现，摒弃了 Vue/React 等框架的依赖，回归 Web 开发的本源，展现了扎实的 DOM 操作与事件处理功底。

## ✨ 核心亮点与实现原理

### 🛡️ 七重安全验证卫士 (Captcha System)
本项目实现了一套高度可配置、支持热切换的混合验证码系统，有效抵御自动化脚本攻击。

#### 1. 滑动拼图验证 (Jigsaw Captcha) - *交互与安全的平衡*
*   **核心逻辑**：
    *   **后端 (`CaptchaUtil.java`)**：使用 Java 2D `GeneralPath` 绘制不规则拼图形状。随机生成缺口坐标 `(x, y)`，从原图中裁剪出滑块并添加描边，同时在原图绘制半透明蒙版。将正确的 `x` 坐标加密存储于 `HttpSession`。
    *   **前端**：监听 `mousedown/mousemove/mouseup` 事件实现拖拽。用户松开时，计算滑块位移并发送给后端。
    *   **校验**：后端比对用户提交的偏移量与 Session 中的值，允许 **±5px** 的误差范围，兼顾安全性与用户体验。

#### 2. 文字点选验证 (TextClick Captcha) - *高强度人机识别*
*   **核心逻辑**：
    *   **后端**：随机选取 3 个汉字（如“梦、云、海”），随机颜色、角度绘制在背景图上。采用简单的**碰撞检测算法**防止文字重叠。将 3 个汉字的中心坐标列表存入 Session。
    *   **前端**：用户点击图片时，在点击处动态生成数字标记 (1, 2, 3)。
    *   **校验**：计算用户点击坐标与预存坐标的**欧氏距离**。只有当每个点击点都在对应汉字的判定半径（30px）内时，验证通过。

#### 3. GalGame 趣味验证 - *二次元特色*
*   **核心逻辑**：
    *   **自动分类**：后端启动时扫描 `static/captchaForGal` 目录，根据文件名（如`白毛1.jpg`, `紫毛2.jpg`）自动提取特征标签。
    *   **出题逻辑**：随机选择一个“目标特征”（如“白毛”），并从其他分类中随机抽取干扰图片，组成选项列表。
    *   **校验**：比对用户选择的图片索引与正确答案索引。

#### 4. 常规图形验证 - *Hutool 强力驱动*
*   集成 Hutool 工具库，提供线段干扰、圆圈干扰、扭曲干扰、GIF 动态帧四种基础验证模式，作为备用方案。

---

### 💻 业务功能与架构设计

#### 1. 树洞内容管理 (Hole Core)
*   **匿名/实名发布**：支持用户发布树洞内容，可选择分类。未登录用户发布时 `userId` 记录为 0（匿名状态）。
*   **分页查询**：实现基于偏移量 (`offset`) 的分页算法，支持按 `category`（类别）或 `userId`（我的树洞）筛选。
*   **点赞系统**：
    *   实现**状态反转**逻辑：用户点击点赞时，后端先查询 `checkLike`。若已赞则执行 `delete`（取消点赞），若未赞则执行 `insert`（点赞），并更新总点赞数。
    *   **并发安全**：点赞数更新使用数据库原子操作 `update like_count = like_count + 1`，避免并发覆盖问题。

#### 2. 评论与互动 (Comment System)
*   **二级内容结构**：树洞 (Hole) -> 评论 (Comment)。
*   **评论点赞**：复用了点赞系统的逻辑，实现了对评论的独立点赞功能。

#### 3. 用户与会话 (User & Session)
*   **状态管理**：使用 `HttpSession` 存储用户登录态 (`USER_ID`)。全站接口通过拦截器或在 Controller 层校验 Session 状态。
*   **游客模式**：允许未登录用户浏览内容，但限制点赞、评论等写操作，引导用户注册。

#### 4. 前端沉浸式体验 (Frontend Magic)
*   **动态背景系统**：
    *   **落叶动画**：使用原生 JS 定时器动态创建 DOM 元素，通过 CSS3 `animation` 控制下落轨迹、旋转角度和透明度渐变，模拟真实的秋日氛围。
    *   **背景视差**：登录页与主页采用不同的背景图，营造空间感。
*   **电影级转场**：
    *   登录成功瞬间，触发 CSS3 `transform: scale(5)` 与 `opacity` 组合动画，实现“隧道穿越”般的视觉冲击，无缝切换至主页。
*   **原生组件封装**：
    *   **ModalManager**：手写模态框类，管理弹窗的打开、关闭、堆叠层级 (`z-index`)。
    *   **Toast提示**：封装轻量级消息提示框，替代丑陋的 `alert`。

## 🛠️ 技术栈清单

### 后端 (Backend)
- **Framework**: Spring Boot 3.x (极速开发)
- **ORM**: MyBatis (灵活的 SQL 控制)
- **Database**: MySQL 8.0 (数据持久化)
- **Tools**: Hutool (瑞士军刀般的 Java 工具库)
- **Build**: Maven

### 前端 (Frontend)
- **Core**: HTML5, CSS3, Vanilla JavaScript (ES6+)
- **Network**: Fetch API (现代化的网络请求)
- **Icons**: RemixIcon (精美开源图标库)

## 🚀 快速启动指南

### 1. 环境准备
- JDK 17 或更高版本
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
1. 创建数据库：`CREATE DATABASE tree_hole DEFAULT CHARACTER SET utf8mb4;`
2. 导入数据表结构（参考 `entry` 包下的实体类字段）。
3. 修改配置文件 `src/main/resources/application.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/tree_hole?serverTimezone=Asia/Shanghai&useSSL=false
       username: root  # 你的数据库账号
       password: root  # 你的数据库密码
   ```

### 3. 运行项目
在项目根目录下执行：
```bash
mvn spring-boot:run
```
或在 IDEA 中运行 `TreeHoleApplication.java`。

访问地址：`http://localhost:8080/login.html`

## 📂 核心目录结构
```
src/main/java/org/example/treehole/
├── controller/      # 控制层：接收前端请求 (HoleController, UserController)
├── service/         # 业务层：核心逻辑 (HoleService, CommentService)
├── mapper/          # 持久层：MyBatis 接口
├── entry/           # 实体类：Hole, User, Comment
├── util/            # 工具类：CaptchaUtil (核心验证码生成逻辑)
└── ...
```

---
*本项目代码清晰，注释详尽，适合作为 Java Web 入门进阶的参考案例。*