# 知错就改 - 安装和运行指南

## 📋 目录
- [环境准备](#环境准备)
- [后端配置和启动](#后端配置和启动)
- [前端配置和启动](#前端配置和启动)
- [常见问题](#常见问题)

---

## 环境准备

### 必备软件

1. **JDK 17+**
   - 下载地址：https://www.oracle.com/java/technologies/downloads/
   - 安装后验证：`java -version`

2. **Node.js 16+**
   - 下载地址：https://nodejs.org/
   - 安装后验证：`node -v` 和 `npm -v`

3. **MySQL 8.0+**
   - 下载地址：https://dev.mysql.com/downloads/mysql/
   - 安装后验证：`mysql --version`

4. **Maven 3.6+**（可选，IDEA自带）
   - 下载地址：https://maven.apache.org/download.cgi
   - 验证：`mvn -v`

5. **开发工具**
   - 推荐使用 **IntelliJ IDEA**（后端）
   - 推荐使用 **VS Code**（前端）

---

## 后端配置和启动

### 步骤 1：创建数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行以下SQL命令
CREATE DATABASE zszg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建数据库用户（可选）
CREATE USER 'zszg'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON zszg.* TO 'zszg'@'localhost';
FLUSH PRIVILEGES;

# 退出
EXIT;
```

### 步骤 2：初始化数据库表

```bash
# 进入后端目录
cd backend/zszg-backend

# 执行建表脚本
mysql -u zszg -p zszg < src/main/resources/schema.sql

# 执行初始化数据脚本
mysql -u zszg -p zszg < src/main/resources/init-data.sql
```

### 步骤 3：配置数据库连接

编辑文件：`backend/zszg-backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zszg?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: zszg          # 修改为你的数据库用户名
    password: your_password  # 修改为你的数据库密码
```

### 步骤 4：启动后端

**方式一：使用 IntelliJ IDEA**
1. 打开 `backend/zszg-backend` 项目
2. 等待 Maven 依赖下载完成
3. 找到 `ZszgBackendApplication.java`
4. 右键点击 → Run 'ZszgBackendApplication'

**方式二：使用命令行**
```bash
cd backend/zszg-backend

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**验证后端启动成功**
- 控制台输出：`Started ZszgBackendApplication in X seconds`
- 访问：http://localhost:8080

---

## 前端配置和启动

### 步骤 1：安装依赖

```bash
cd frontend/zszg-frontend

# 安装依赖（首次运行或package.json更新后）
npm install
```

### 步骤 2：启动开发服务器

```bash
npm run dev
```

启动成功后，终端会显示：
```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 步骤 3：访问系统

打开浏览器访问：**http://localhost:5173**

---

## 测试账号

系统已预置以下测试账号：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 拥有所有权限 |
| 教师 | teacher | 123456 | 可管理资源、查看统计 |
| 学生 | student | 123456 | 可管理错题、浏览共享池 |

---

## 常见问题

### 1. 数据库连接失败

**问题**：`Access denied for user 'zszg'@'localhost'`

**解决方案**：
- 检查数据库用户名和密码是否正确
- 确认 MySQL 服务已启动
- 重新创建数据库用户并授权

```sql
DROP USER 'zszg'@'localhost';
CREATE USER 'zszg'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON zszg.* TO 'zszg'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 端口被占用

**问题**：`Port 8080 is already in use`

**解决方案**：
- 修改后端端口：编辑 `application.yml` 中的 `server.port`
- 或者关闭占用8080端口的程序

```yaml
server:
  port: 8081  # 改为其他端口
```

### 3. 前端依赖安装失败

**问题**：`npm install` 报错

**解决方案**：
```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 4. 跨域问题

**问题**：前端请求后端时出现 CORS 错误

**解决方案**：
- 确保后端的 `GlobalCorsConfig.java` 配置正确
- 检查前端的 `vite.config.ts` 代理配置

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 5. JWT认证失败

**问题**：登录后仍然跳转到登录页

**解决方案**：
- 检查浏览器控制台是否有错误信息
- 清除浏览器 localStorage：`localStorage.clear()`
- 确保后端 JWT secret 配置正确

### 6. 文件上传失败

**问题**：上传文件时报错

**解决方案**：
- 检查 `application.yml` 中文件大小限制
- 确保 `uploads` 目录存在且有写入权限

```bash
# 在项目根目录创建uploads文件夹
mkdir uploads
```

### 7. Maven 依赖下载慢

**解决方案**：配置国内镜像

编辑 `~/.m2/settings.xml`（如果不存在则创建）：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

### 8. npm 依赖下载慢

**解决方案**：使用淘宝镜像

```bash
# 设置淘宝镜像
npm config set registry https://registry.npmmirror.com

# 或使用 cnpm
npm install -g cnpm --registry=https://registry.npmmirror.com
cnpm install
```

---

## 生产环境部署

### 后端打包

```bash
cd backend/zszg-backend
mvn clean package -DskipTests

# 生成的jar包位于 target/zszg-backend-0.0.1-SNAPSHOT.jar
```

### 前端打包

```bash
cd frontend/zszg-frontend
npm run build

# 生成的静态文件位于 dist/ 目录
```

### 运行生产版本

**后端**：
```bash
java -jar target/zszg-backend-0.0.1-SNAPSHOT.jar
```

**前端**：
将 `dist/` 目录部署到 Nginx 或其他 Web 服务器

---

## 开发建议

1. **使用热重载**
   - 后端：IDEA 开启自动编译
   - 前端：Vite 默认支持热重载

2. **查看日志**
   - 后端日志在控制台输出
   - 前端错误查看浏览器控制台（F12）

3. **数据库管理工具**
   - 推荐使用 Navicat 或 DBeaver 管理数据库

4. **API测试**
   - 推荐使用 Postman 或 Apifox 测试接口

---

## 技术支持

如遇到其他问题，请：
1. 查看项目 README.md
2. 检查控制台错误信息
3. 提交 GitHub Issue

---

<div align="center">
  <p>祝你使用愉快！🎉</p>
</div>


