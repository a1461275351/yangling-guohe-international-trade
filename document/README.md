# README

杨凌国合外贸综合服务平台资料
外综服平台  - 部署说明
由 Markdown 阅读资料转换⽣成  · 通⽤⾃适应阅读版  · 适合转发和专家审阅
外综服平台  - 部署说明
⼀、项⽬概述
外综服平台（ Foreign Trade Comprehensive Service Platform ）是⼀套
前后端分离的外贸综合服务平台。
技术栈
后端：Java 17 + Spring Boot 2.7 + MyBatis-Plus + MySQL 8.0 +
JWT
前端：Vue 3 + Vite + Element Plus + Pinia + Axios
⼆、⽂件清单
autoWorkPlatform/
├── sql/
│   └── init.sql                          # 数据库初始化脚本
├── backend/
│   └── target/
│       └── trade-platform-1.0.0.jar      # 后端⽣产 JAR 包
├── frontend/

---

│   └── dist/                             # 前端静态⽂件（ nginx 部署
⽤）
└── README.md                              # 本⽂档
三、环境要求
组件 版本要求
JDK 17+
MySQL 8.0+
Nginx 1.18+（或任意静态⽂件服务器）
Redis 可选（当前版本未使⽤）
四、部署步骤
1. 创建数据库
# 登录 MySQL
mysql -u root -p
# 执⾏初始化脚本
source sql/init.sql
或直接：
mysql -u root -p < sql/init.sql

---

初始化后会⾃动创建：  - 数据库  trade_platform - 默认租户：
企业号 GUOHE，名称 杨凌国合跨境贸易有限公司 - 默认管理员账
户：⽤户名 admin，密码 Admin@123
2. 修改后端配置
编辑 backend/src/main/resources/application-prod.yml
（或同级⽬录新建  application.yml）：
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/trade_platform?
useUnicode=true&characterEncoding=utf-
8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetri
eval=true
    username: root
    password: 你的 MySQL 密码    # ← 修改这⾥
    driver-class-name: com.mysql.cj.jdbc.Driver
jwt:
  secret: 你的 JWT 密钥         # ← 修改这⾥，建议 32 位以上随机字符串
  expiration: 86400000
file:
  upload-path: ./uploads/    # ← ⽂件上传存储路径

---

3. 启动后端
# ⽅式⼀：直接运⾏ JAR
java -jar trade-platform-1.0.0.jar --
spring.profiles.active=prod
# ⽅式⼆：指定配置⽂件启动
java -jar trade-platform-1.0.0.jar --
spring.config.location=./application.yml
# ⽅式三：后台运⾏
nohup java -jar trade-platform-1.0.0.jar --
spring.profiles.active=prod > app.log 2>&1 &
后端启动后访问：http://localhost:8080
4. 部署前端（ nginx ）
将 frontend/dist/ ⽬录下的所有⽂件上传到服务器：
# nginx.conf 示例配置
server {
    listen       80;
    server_name  your-domain.com;
    # 前端静态⽂件
    location / {
        root   /var/www/autoWorkPlatform/dist;
        index  index.html;
        try_files $uri $uri/ /index.html;   # 解决 SPA 路由问题
    }

---

    # 代理后端 API
    location /api {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
        proxy_set_header   X-Forwarded-For  
$proxy_add_x_forwarded_for;
    }
    # 上传⽂件访问
    location /uploads {
        alias /path/to/uploads;
        autoindex on;
    }
}
# 重载nginx 配置
nginx -s reload
五、⻆⾊权限说明
⻆⾊ 说明 可访问模块
ADMIN 系统管理员 全部功能（⽤户 / 租户 / 配置 / 模
板/业务模块）
GUOHE 国合员⼯ 业务模块（合同 / 订单 / 货物 / 报
关单/⽂件/ 供应商 / 退税）
ENTERPRISE 企业⽤户 本企业业务数据（业务模块）

---

六、登录说明
1. 系统管理员（ADMIN ）：
企业号：GUOHE（固定，系统级⽤户不区分租户）
⽤户名：admin
密码：Admin@123
1. 新增⽤户流程：
填写注册信息  → 待审批状态  → ADMIN 在 " ⽤户审批 " 中审核通过  →
登录使⽤
七、接⼝说明
所有API均在  /api/ 路径下，认证⽅式为  Authorization: Bearer
<token>
主要接⼝前缀：
/api/auth - 认证相关（登录 / 注册 / 忘记密码）
/api/tenants - 租户管理（ ADMIN ）
/api/users - ⽤户管理（ ADMIN ）
/api/user-applies - ⽤户审批（ ADMIN ）
/api/contracts - 合同管理
/api/orders - 订单管理
/api/goods - 商品管理

---

/api/customs - 报关单管理
/api/files - ⽂件管理
/api/partners - 供应商/ 客户管理
/api/config - 配置管理（ ADMIN ）
/api/templates - 模板管理（ ADMIN ）
/api/tax - 退税信息
⼋、⽬录结构
backend/src/main/java/com/trade/platform/
├── config/           # 配置类（ CORS/MyBatisPlus/MVC ）
├── common/           # 公共类（ Result/PageResult/BaseEntity/ 异常
处理）
├── security/          # 安全认证（ JWT/ 权限拦截）
├── module/
│   ├── auth/         # 认证模块
│   ├── user/          # ⽤户管理
│   ├── tenant/        # 租户管理
│   ├── contract/      # 合同管理
│   ├── order/         # 订单管理
│   ├── goods/         # 商品管理
│   ├── customs/        # 报关单管理
│   ├── file/          # ⽂件管理
│   ├── partner/       # 供应商 / 客户管理
│   ├── config/        # 配置管理
│   └── template/      # 模板管理

---

九、常⻅问题
Q: 启动报数据库连接失败？
检查 MySQL 是否运⾏，⽤户名密码是否正确，数据库
trade_platform 是否已创建。
Q: 注册⽤户后⽆法登录？
注册申请需要  ADMIN 在 " ⽤户审批 " 中审核通过后才能登录。
Q: 前端⻚⾯空⽩？
检查 nginx 配置中  try_files $uri $uri/ /index.html 是
否正确配置。
Q: ⽂件上传失败？
检查 application-prod.yml 中 file.upload-path ⽬录是
否存在且有写⼊权限。
本⽂档为内部研究和项⽬推进资料，请结合正式政策⽂件、合同⽂本和业务凭证审慎使⽤。