# 知错就改 - 基于错题共享 + 知识点溯源的学习平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.38-4FC08D.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.8.6-409EFF.svg)](https://element-plus.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📖 项目简介

"知错就改"是一个面向学生和教师的智能学习平台，通过**错题共享**和**知识点溯源**两大核心功能，帮助学生高效管理错题、精准定位薄弱知识点，实现个性化学习。

### ✨ 核心特性

- 📝 **智能错题管理**：支持拍照上传、手动录入，自动分类整理
- 🔄 **错题共享池**：班级/年级/校级共享优质错题和解析
- 🔍 **知识点溯源**：错题关联课本原文、教师笔记、讲解视频
- 🎯 **个性化推荐**：基于错题画像智能推荐练习题
- 📊 **数据分析**：教师端班级错题统计和薄弱点分析
- 📚 **资源共享**：教学资料、历年真题在线共享

---

## 🏗️ 技术架构

### 后端技术栈
- **框架**：Spring Boot 3.3.2
- **数据库**：MySQL 8.0
- **ORM**：Spring Data JPA + Hibernate
- **安全认证**：Spring Security + JWT
- **构建工具**：Maven

### 前端技术栈
- **框架**：Vue 3 (Composition API)
- **构建工具**：Vite 5
- **UI 组件库**：Element Plus 2.8
- **图表库**：ECharts 5
- **HTTP 客户端**：Axios
- **路由**：Vue Router 4
- **状态管理**：Pinia

---

## 📁 项目结构

```
知错就改/
├── backend/                    # 后端项目
│   └── zszg-backend/
│       ├── src/
│       │   └── main/
│       │       ├── java/com/zszg/
│       │       │   ├── auth/           # 认证模块
│       │       │   ├── errorbook/      # 错题管理
│       │       │   ├── knowledge/      # 知识点管理
│       │       │   ├── sharepool/      # 共享池
│       │       │   ├── resource/       # 资源管理
│       │       │   ├── question/       # 题库管理
│       │       │   ├── service/        # 业务服务
│       │       │   ├── security/       # 安全配置
│       │       │   ├── config/         # 系统配置
│       │       │   └── common/         # 公共组件
│       │       └── resources/
│       │           ├── application.yml # 应用配置
│       │           ├── schema.sql      # 数据库结构
│       │           └── init-data.sql   # 初始化数据
│       └── pom.xml
│
└── frontend/                   # 前端项目
    └── zszg-frontend/
        ├── src/
        │   ├── views/              # 页面组件
        │   │   ├── auth/           # 登录注册
        │   │   ├── student/        # 学生端
        │   │   ├── teacher/        # 教师端
        │   │   └── admin/          # 管理员端
        │   ├── layouts/            # 布局组件
        │   ├── router/             # 路由配置
        │   ├── lib/                # 工具库
        │   ├── style.css           # 全局样式
        │   ├── App.vue
        │   └── main.ts
        ├── index.html
        ├── package.json
        └── vite.config.ts
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 后端启动

1. **克隆项目**
```bash
cd backend/zszg-backend
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE zszg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'zszg'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON zszg.* TO 'zszg'@'localhost';
FLUSH PRIVILEGES;

# 执行数据库脚本
mysql -u zszg -p zszg < src/main/resources/schema.sql
mysql -u zszg -p zszg < src/main/resources/init-data.sql
```

3. **修改配置文件**
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zszg
    username: zszg
    password: your_password
```

4. **启动后端**
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. **安装依赖**
```bash
cd frontend/zszg-frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 访问系统

打开浏览器访问：`http://localhost:5173`

**测试账号**：
- 学生：`student` / `123456`
- 教师：`teacher` / `123456`
- 管理员：`admin` / `123456`

---

## 📱 功能模块

### 学生端
- ✅ 错题本管理（增删改查）
- ✅ 知识点溯源（查看关联资源）
- ✅ 错题共享池（浏览、收藏、点赞）
- ✅ 智能推荐（个性化题目推荐）
- ✅ 个人中心（学习统计）

### 教师端
- ✅ 班级错题统计分析
- ✅ 资源上传和管理
- ✅ 共享内容审核
- ✅ 教学反馈

### 管理员端
- ✅ 用户管理（增删改查）
- ✅ 题库管理
- ✅ 内容审核（共享/资源）
- ✅ 系统设置

---

## 🎨 界面预览

> 精美的UI设计，流畅的动画效果，带来极致的用户体验

### 学生端
- 🎯 直观的错题管理界面
- 📊 可视化的学习统计图表
- 🌈 精美的卡片式布局

### 教师端
- 📈 丰富的数据图表（ECharts）
- 📋 清晰的班级统计报表
- 🎨 专业的仪表盘设计

### 管理员端
- 🛠️ 强大的管理后台
- 📦 高效的批量操作
- 🔧 灵活的系统配置

---

## 📊 数据库设计

### 核心数据表

| 表名 | 说明 |
|------|------|
| `t_user` | 用户信息表 |
| `t_question` | 题目信息表 |
| `t_knowledge` | 知识点表 |
| `t_question_knowledge` | 题目-知识点关联表 |
| `t_error_book` | 错题表 |
| `t_share_pool` | 共享错题池 |
| `t_resource` | 学习资源表 |

详细设计请查看：[schema.sql](backend/zszg-backend/src/main/resources/schema.sql)

---

## 🔌 API 接口

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 错题管理
- `GET /api/errorbook` - 获取错题列表
- `POST /api/errorbook` - 创建错题
- `PUT /api/errorbook/{id}` - 更新错题
- `DELETE /api/errorbook/{id}` - 删除错题

### 共享池
- `GET /api/sharepool` - 获取共享列表
- `POST /api/sharepool` - 分享错题
- `POST /api/sharepool/{id}/review` - 审核共享

### 知识点
- `GET /api/knowledge` - 获取知识点列表
- `GET /api/knowledge/root` - 获取根知识点
- `GET /api/knowledge/{id}/children` - 获取子知识点

更多接口请参考各 Controller 文件

---

## 🎯 项目特色

### 1. 智能推荐算法
基于学生的错题画像和知识点掌握情况，智能推荐相关练习题，实现个性化学习。

### 2. 知识图谱构建
建立题目-知识点关联体系，形成"错题→知识点→同类题"的学习闭环。

### 3. 精美UI设计
- 渐变色卡片设计
- 流畅的过渡动画
- 响应式布局
- 丰富的数据可视化

### 4. 完善的权限管理
基于角色的访问控制（RBAC），三种角色权限清晰分离。

---

## 📝 待优化功能

- [ ] 文件上传功能（当前为演示状态）
- [ ] 批量导入题目
- [ ] 导出分析报告
- [ ] 移动端适配
- [ ] 消息通知系统
- [ ] 学习计划功能

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 开源协议

本项目采用 MIT 协议开源，详见 [LICENSE](LICENSE) 文件

---

## 👨‍💻 作者信息

- **作者**：大三软件工程专业学生
- **项目类型**：实训项目
- **开发时间**：2025年

---

## 📮 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 Issue
- 发送邮件

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [ECharts](https://echarts.apache.org/)

---

<div align="center">
  <p>⭐ 如果这个项目对你有帮助，请给一个星标支持！</p>
  <p>Made with ❤️ by Software Engineering Student</p>
</div>

