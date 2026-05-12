# 外综服平台 - 部署说明

## 一、项目概述

外综服平台（Foreign Trade Comprehensive Service Platform）是一套前后端分离的外贸综合服务平台。

### 技术栈
- **后端**：Java 17 + Spring Boot 2.7 + MyBatis-Plus + MySQL 8.0 + JWT
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Axios

---

## 二、文件清单

```
autoWorkPlatform/
├── sql/
│   └── init.sql                          # 数据库初始化脚本
├── backend/
│   └── target/
│       └── trade-platform-1.0.0.jar      # 后端生产JAR包
├── frontend/
│   └── dist/                             # 前端静态文件（nginx部署用）
└── README.md                              # 本文档
```

---

## 三、环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Nginx | 1.18+（或任意静态文件服务器） |
| Redis | 可选（当前版本未使用） |

---

## 四、部署步骤

### 1. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init.sql
```

或直接：
```bash
mysql -u root -p < sql/init.sql
```

> 初始化后会自动创建：
> - 数据库 `trade_platform`
> - 默认租户：企业号 `GUOHE`，名称 `杨凌国合跨境贸易有限公司`
> - 默认管理员账户：**用户名 `admin`，密码 `Admin@123`**（首次登录后请立即修改）

### 2. 修改后端配置

编辑 `backend/src/main/resources/application-prod.yml`（或同级目录新建 `application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/trade_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码   # ← 修改这里
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: 你的JWT密钥        # ← 修改这里，建议32位以上随机字符串
  expiration: 86400000

file:
  upload-path: ./uploads/    # ← 文件上传存储路径
```

### 3. 启动后端

```bash
# 方式一：直接运行JAR
java -jar trade-platform-1.0.0.jar --spring.profiles.active=prod

# 方式二：指定配置文件启动
java -jar trade-platform-1.0.0.jar --spring.config.location=./application.yml

# 方式三：后台运行
nohup java -jar trade-platform-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
```

后端启动后访问：`http://localhost:8080`

### 4. 部署前端（nginx）

将 `frontend/dist/` 目录下的所有文件上传到服务器：

```nginx
# nginx.conf 示例配置
server {
    listen       80;
    server_name  your-domain.com;

    # 前端静态文件
    location / {
        root   /var/www/autoWorkPlatform/dist;
        index  index.html;
        try_files $uri $uri/ /index.html;   # 解决SPA路由问题
    }

    # 代理后端API
    location /api {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 上传文件访问
    location /uploads {
        alias /path/to/uploads;
        autoindex on;
    }
}
```

```bash
# 重载nginx配置
nginx -s reload
```

---

## 五、角色权限说明

| 角色 | 说明 | 可访问模块 |
|------|------|-----------|
| `ADMIN` | 系统管理员 | 全部功能（用户/租户/配置/模板/业务模块） |
| `GUOHE` | 国合员工 | 业务模块（合同/订单/货物/报关单/文件/供应商/退税） |
| `ENTERPRISE` | 企业用户 | 本企业业务数据（业务模块） |

---

## 六、登录说明

1. **系统管理员**（ADMIN）：
   - 企业号：`GUOHE`（固定，系统级用户不区分租户）
   - 用户名：`admin`
   - 密码：`Admin@123`（首次登录后请立即修改）

2. **新增用户**流程：
   - 填写注册信息 → 待审批状态 → ADMIN在"用户审批"中审核通过 → 登录使用

---

## 七、接口说明

所有API均在 `/api/` 路径下，认证方式为 `Authorization: Bearer <token>`

主要接口前缀：
- `/api/auth` - 认证相关（登录/注册/忘记密码）
- `/api/tenants` - 租户管理（ADMIN）
- `/api/users` - 用户管理（ADMIN）
- `/api/user-applies` - 用户审批（ADMIN）
- `/api/contracts` - 合同管理
- `/api/orders` - 订单管理
- `/api/goods` - 商品管理
- `/api/customs` - 报关单管理
- `/api/files` - 文件管理
- `/api/partners` - 供应商/客户管理
- `/api/config` - 配置管理（ADMIN）
- `/api/templates` - 模板管理（ADMIN）
- `/api/tax` - 退税信息

---

## 八、目录结构

```
backend/src/main/java/com/trade/platform/
├── config/           # 配置类（CORS/MyBatisPlus/MVC）
├── common/           # 公共类（Result/PageResult/BaseEntity/异常处理）
├── security/          # 安全认证（JWT/权限拦截）
├── module/
│   ├── auth/         # 认证模块
│   ├── user/          # 用户管理
│   ├── tenant/        # 租户管理
│   ├── contract/      # 合同管理
│   ├── order/         # 订单管理
│   ├── goods/         # 商品管理
│   ├── customs/        # 报关单管理
│   ├── file/          # 文件管理
│   ├── partner/       # 供应商/客户管理
│   ├── config/        # 配置管理
│   └── template/      # 模板管理
```

---

## 九、常见问题

**Q: 启动报数据库连接失败？**
> 检查 MySQL 是否运行，用户名密码是否正确，数据库 `trade_platform` 是否已创建。

**Q: 注册用户后无法登录？**
> 注册申请需要 ADMIN 在"用户审批"中审核通过后才能登录。

**Q: 前端页面空白？**
> 检查 nginx 配置中 `try_files $uri $uri/ /index.html` 是否正确配置。

**Q: 文件上传失败？**
> 检查 `application-prod.yml` 中 `file.upload-path` 目录是否存在且有写入权限。
