# Tree Hole (树洞)

## 简介
这是一个基于 **Spring Boot** 和 **MyBatis** 构建的树洞社区练习项目。前端采用原生 **HTML/CSS/JS** 实现，无框架依赖，追求极致的交互体验。

本项目是大学期末作业/练习项目，旨在通过原生技术栈深入理解 Web 开发的核心流程。

## ✨ 核心亮点

### 1. 安全与交互并重的验证码系统
为了抵御恶意攻击并提供有趣的交互体验，本项目集成了多种验证码类型：
- **滑动拼图验证**：基于 Java 2D 的图片缺口生成与匹配，包含容错校验。
- **文字点选验证**：随机生成汉字，验证点击顺序与坐标（半径判定）。
- **GalGame 趣味验证**：二次元风格的“找不同”色块验证（特色功能）。
- **常规干扰验证**：线段、圆圈、扭曲、GIF 动态验证码。
- **智能切换**：支持随机切换验证方式，且带有炫酷的旋转动画。

### 2. 沉浸式前端体验
- **动态背景**：登录页面的飘落树叶动画。
- **隧道转场**：登录成功后独特的隧道缩放转场效果。
- **原生实现**：不依赖 Vue/React，完全使用原生 JS 实现拖拽、弹窗、动画逻辑，展示扎实的 DOM 操作功底。

## 🛠️ 技术栈

- **后端**：
  - Spring Boot 3.x
  - MyBatis
  - Hutool (工具库：用于图片处理、验证码生成、随机数等)
- **前端**：
  - HTML5 / CSS3
  - JavaScript (ES6+)
- **数据库**：
  - MySQL 8.0

## 🚀 快速开始

### 1. 环境准备
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
1. 创建数据库 `tree_hole`。
2. 确保 `user` 表和其他相关表结构已创建。
3. 修改 `src/main/resources/application.yml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/tree_hole?serverTimezone=UTC
       username: root
       password: your_password
   ```

### 3. 运行项目
在项目根目录下运行：
```bash
mvn spring-boot:run
```
或者在 IDE 中运行 `TreeHoleApplication.java`。

访问地址：`http://localhost:8080/login.html`

## 📂 目录结构说明
```
src/main/java/org/example/treehole/
├── controller/      # 控制层 (处理 HTTP 请求)
├── service/         # 业务逻辑层 (复杂校验、事务)
├── mapper/          # 数据访问层 (MyBatis 接口)
├── entry/           # 实体类 (User, Hole 等)
├── util/            # 工具类 (CaptchaUtil 等核心工具)
└── ...
```

## 📝 学习心得
本项目实现了完整的 MVC 分层架构，深入理解了 Session 状态管理、MyBatis 动态 SQL 以及原生 JS 的事件处理机制。

---
*本项目仅供学习交流使用。*