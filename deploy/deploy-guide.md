# 外综服平台 - Windows服务器部署指南

## 一、服务器环境要求

| 组件 | 版本 | 用途 |
|------|------|------|
| JDK | 1.8+ | 后端运行 |
| MySQL | 8.0+ | 数据库 |
| Nginx | 1.18+ | 前端静态文件 + 反向代理 |
| Node.js | 16+ | 前端构建（仅构建时需要） |

## 二、部署文件清单

```
deploy/
├── trade-platform-1.0.0.jar       # 后端JAR包
├── application-prod.yml            # 生产环境配置（需修改）
├── dist/                           # 前端构建产物
├── sql/init.sql                    # 数据库初始化脚本
├── nginx.conf                      # Nginx配置
├── start.bat                       # 启动脚本
├── stop.bat                        # 停止脚本
└── deploy-guide.md                 # 本文档
```

## 三、部署步骤

### 步骤1：安装环境

1. 安装 JDK 1.8+，配置 JAVA_HOME
2. 安装 MySQL 8.0+
3. 安装 Nginx

### 步骤2：初始化数据库

```cmd
mysql -u root -p < sql/init.sql
```

### 步骤3：修改配置文件

编辑 `application-prod.yml`，修改以下内容：

```yaml
spring:
  datasource:
    password: 你的MySQL密码    # ← 改这里

jwt:
  secret: 改为32位以上随机字符串  # ← 改这里

file:
  upload-path: D:/deploy/uploads/  # ← 改为服务器实际路径
```

### 步骤4：部署前端（Nginx）

1. 将 `dist/` 目录复制到 Nginx 的 html 目录下
2. 使用提供的 `nginx.conf` 配置文件
3. 启动 Nginx

### 步骤5：启动后端

```cmd
双击 start.bat
```

或手动运行：
```cmd
java -jar trade-platform-1.0.0.jar --spring.config.location=application-prod.yml
```

### 步骤6：验证

- 前端访问：http://服务器IP
- 后端API：http://服务器IP:8081
- 默认账号：企业号 GUOHE，用户名 admin，密码 Admin@123（首次登录后请立即修改）

## 四、常见问题

**Q: 端口被占用？**
修改 `application-prod.yml` 中的 `server.port` 和 `nginx.conf` 中的代理地址

**Q: 文件上传失败？**
检查 `file.upload-path` 目录是否存在且有写入权限

**Q: 数据库连接失败？**
检查 MySQL 服务是否运行，用户名密码是否正确
