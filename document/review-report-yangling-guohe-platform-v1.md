# review-report-yangling-guohe-platform-v1

杨凌国合外贸综合服务平台资料
杨凌国合外贸综合服务平台  1.0 代码复查与建设建议
由 Markdown 阅读资料转换⽣成  · 通⽤⾃适应阅读版  · 适合转发和专家审阅
杨凌国合外贸综合服务平台  1.0 代码复查与建设建 议
复查⽇期： 2026-04-25
复查对象：/Users/wangbin/Documents/Codex/review_src2/autoWorkPlatform-v1.0.0
⼀、总体结论
这次已经复查到真实源码和部署包。平台  1.0 具备演示型  MVP 的雏形：有登录注册、租户、⽤户、配置、模
板、合同、商品、订单、报关、进⼝台账、⽂件、供应商客户、退税展示等模块。但按 “ 省级外贸综合服务平
台”和“国有农产品外贸企业⽣产系统 ” 的标准，⽬前还不适合直接上线⽣产。
核⼼判断：
1. 当前最严重问题不是界⾯不够漂亮，⽽是数据库脚本与代码不⼀致、安全边界薄弱、部署配置不规范、业
务闭环不⾜。
2. 1.0 更像内部演示系统，离 “ 可审计、可监管、可接⼊、可运营、可扩展 ” 的省级平台还有明显差距。
3. 不建议⽴刻⽤  Rust 全量重写。建议先⽤  2 到  8 周把现有  Java/Spring Boot 版本修到可⽤、可测、可部
署，再选择部分⾼并发、⾼安全、⾼性能模块⽤  Rust 渐进式改造。
4. 平台建设重点应从 “ 做  CRUD ⻚⾯ ” 转向 “ 围绕外贸真实业务闭环建平台 ” ：订单、合同、单证、报关、物
流、收汇、结汇、退税、信保、融资、⻛控、审计、统计决策。
⼆、代码复查发现
P0-1 数据库脚本与代码严重不⼀致
这是当前第⼀优先级问题。sql/init.sql、deploy/sql/init.sql、
backend/src/main/resources/db/init.sql 都只建到了  biz_partner，没有创建多张代码已经
使⽤的表。
证据：
ImportLedger 映射到 biz_import_ledger：
backend/src/main/java/com/trade/platform/module/ledger/entity/ImportLedger.ja
但 SQL 初始化⽂件没有  biz_import_ledger、biz_import_ledger_goods、
biz_import_ledger_file。
CustomsReview 映射到 biz_customs_review，SQL 也没有该表。

---

CustomsDeclaration 代码⾥有 ledgerId、splitSeq、reviewStatus、submitTime、
submitBy、reviewTime、reviewBy、deleted：
backend/src/main/java/com/trade/platform/module/customs/entity/CustomsDeclara
SQL ⾥的 biz_customs_declaration 没有这些字段：sql/init.sql:213
Template 代码⾥有 filePath、fileName、fileSize、pdfPath：
backend/src/main/java/com/trade/platform/module/template/entity/Template.java
SQL ⾥的 biz_template 没有这些字段：sql/init.sql:102
影响：
进⼝台账、报关审核、模板上传编辑等功能很可能运⾏时报  “ 表不存在 ” 或  “ 字段不存在 ” 。
部署包内的  jar 也包含台账代码，但内置  SQL 仍然缺表，说明不是单纯源码遗漏，⽽是交付包整体不⼀
致。
整改建议：
1. ⽴即补⻬数据库迁移脚本，不要再只靠  init.sql。
2. 引⼊ Flyway 或  Liquibase ，建⽴  V1__init.sql、V2__ledger.sql、
V3__customs_review.sql 这种可追踪迁移。
3. 所有实体字段、 Mapper 、 SQL 、测试数据必须做⼀次全量对账。
4. 以后每次新增表字段必须配套迁移脚本和回滚说明。
P0-2 认证和权限存在⽣产⻛险
证据：
注册 DTO 要求前端传⼊⻆⾊：
backend/src/main/java/com/trade/platform/module/user/dto/RegisterDTO.java:25
注册服务直接保存前端提交的⻆⾊：
backend/src/main/java/com/trade/platform/module/user/service/UserService.java
审批通过时直接⽤申请表⾥的⻆⾊创建⽤户：
backend/src/main/java/com/trade/platform/module/user/service/UserService.java
权限拦截只判断⻆⾊字符串是否匹配注解：
backend/src/main/java/com/trade/platform/security/PermissionInterceptor.java
报关审核的  approve/reject/release/revoke 控制器没有⻆⾊注解：
backend/src/main/java/com/trade/platform/module/customs/controller/CustomsCon
影响：
前端可以申请  ADMIN 或 GUOHE 等⾼权限⻆⾊，如果审批⼈员误点通过，就会产⽣越权账号。
报关审核、放⾏等业务动作没有区分制单⼈、审核⼈、国合员⼯、企业员⼯。

---

对省级平台来说，缺少 “ 岗位、部⻔、租户、数据范围、审批流节点 ” 的权限模型。
整改建议：
1. 注册接⼝只能申请普通企业⽤户，国合员⼯和管理员必须由后台创建或邀请。
2. 建⽴ RBAC 权限表：⽤户、⻆⾊、菜单、按钮、接⼝、数据范围。
3. 关键业务动作必须使⽤流程权限：制单、复核、审批、放⾏、退回、撤回不能由同⼀个岗位任意完成。
4. 增加操作审计表，记录操作者、 IP 、设备、前后状态、业务编号、时间。
P0-3 密码和  JWT 配置不安全
证据：
JWT 过期时间计算为  expiration * 1000：
backend/src/main/java/com/trade/platform/security/JwtUtil.java:22
配置⾥ jwt.expiration: 86400000，看起来是毫秒，实际会被乘  1000 ，约等于  1000 天。
重置密码直接改成  123456：
backend/src/main/java/com/trade/platform/module/user/service/UserService.java
审批密码重置也改成  123456：
backend/src/main/java/com/trade/platform/module/user/service/UserService.java
默认管理员  admin/admin123 写⼊ SQL ：sql/init.sql:306
开发配置⾥有  root/root、固定 JWT secret 、内⽹  OnlyOffice 地址：
backend/src/main/resources/application.yml:11
影响：
Token 过期过⻓，账号泄露后⻛险窗⼝⾮常⼤。
默认密码、固定密钥、数据库  root ⽤户都不符合国有企业⽣产系统安全要求。
测试账号和运维⽇志打进部署包，容易造成信息泄露。
整改建议：
1. JWT 过期时间统⼀按毫秒或秒，建议  access token 2 ⼩时、 refresh token 7 到  14 天。
2. 重置密码改为⼀次性随机临时密码或短信 / 邮件重置链接，⾸次登录强制改密。
3. 上线前删除所有默认账号，管理员⾸次初始化由部署脚本⽣成随机密码。
4. 密钥、数据库密码、 OnlyOffice 密钥全部进⼊环境变量或密钥管理系统，不进源码、不进  jar 。
P0-4 OnlyOffice 回调存在  SSRF 和未授权⽂件替换⻛险
证据：
OnlyOffice callback 被排除  JWT ：
backend/src/main/java/com/trade/platform/config/WebMvcConfig.java:31

---

callback 说明 “ 不需要  JWT 认证 ” ：
backend/src/main/java/com/trade/platform/module/template/controller/OnlyOffic
回调体⾥的任意  url 会被后端直接下载并覆盖模板⽂件：
backend/src/main/java/com/trade/platform/module/template/controller/OnlyOffic
下载使⽤ new URL(downloadUrl).openConnection()：
backend/src/main/java/com/trade/platform/module/template/controller/OnlyOffic
OnlyOffice JWT secret 为空：backend/src/main/resources/application.yml:41
影响：
外部请求可伪造回调，让服务器访问内⽹地址，形成  SSRF 。
可诱导后端下载恶意⽂件，替换模板⽂件。
对涉及合同、报关、发票、装箱单的系统，这是严重安全⻛险。
整改建议：
1. 启⽤ OnlyOffice JWT ，并校验回调签名。
2. callback URL 增加⼀次性  token 、⽂档  key 、来源  IP ⽩名单。
3. 后端下载 URL 必须限制域名或私有⽹段禁⽌访问，禁⽌访问  localhost、内⽹、云元数据地址。
4. ⽂件替换前校验⽂档类型、⼤⼩、 hash ，并保留历史版本。
P1-1 ⽂件上传和公开访问不符合⽣产要求
证据：
上传只限制  5MB ，没有通⽤⽂件类型⽩名单：
backend/src/main/java/com/trade/platform/module/file/service/FileService.java
originalFilename 没有空值和⽆后缀处理：
backend/src/main/java/com/trade/platform/module/file/service/FileService.java
/uploads/** 直接映射为静态资源：
backend/src/main/java/com/trade/platform/config/WebMvcConfig.java:52
删除⽂件只删数据库，不删物理⽂件：
backend/src/main/java/com/trade/platform/module/file/service/FileService.java
影响：
企业合同、证照、报关单、发票等敏感⽂件可能被  URL 猜测访问。
恶意⽂件上传后可能带来安全和合规问题。
删除记录后⽂件仍残留，形成数据泄露和合规⻛险。
整改建议：
1. ⽂件下载必须⾛鉴权接⼝，不直接暴露⽬录。

---

2. 建⽴⽂件类型⽩名单、 MIME 校验、病毒扫描、⼤⼩分级限制。
3. ⽂件存储迁移到  MinIO/S3 兼容对象存储，使⽤临时签名  URL 。
4. 删除采⽤软删除加物理归档策略，按档案制度保留和销毁。
P1-2 CORS 和异常处理不适合⽣产
证据：
CORS 允许任意来源且允许凭据：
backend/src/main/java/com/trade/platform/config/CorsConfig.java:13
全局异常把内部异常消息返回给前端：
backend/src/main/java/com/trade/platform/common/GlobalExceptionHandler.java:3
影响：
跨域策略过宽，未来若改为  Cookie 或接⼊第三⽅系统，会扩⼤攻击⾯。
SQL 错误、路径、内部配置可能暴露给⽤户。
整改建议：
1. ⽣产环境只允许企业正式域名、政务⽹域名、内⽹管理域名。
2. 前端只显示统⼀错误码和友好提示，详细堆栈只写安全⽇志。
3. 增加 traceId ，便于运维排障。
P1-3 前端路由声明⻆⾊但没有真正拦截
证据：
路由⾥声明了  roles ：frontend/src/router/index.js:40
路由守卫只检查  token ，没有检查  roles ：frontend/src/router/index.js:212
Token 存在  localStorage ：frontend/src/utils/auth.js:5
影响：
⽤户可以直接打开隐藏菜单路由，虽然后端部分会挡住，但前端体验和安全模型不完整。
localStorage 中的  token 更容易受  XSS 影响。
整改建议：
1. 路由守卫补充⻆⾊检查。
2. 接⼝权限仍以后端为准。
3. 做 CSP、防  XSS 、输⼊输出编码。
4. 中⻓期可改为  httpOnly Secure Cookie 加  CSRF 防护，或短  token + refresh token 机制。

---

P1-4 部署和交付包混杂
发现：
压缩包⾥包含  deploy/trade-platform-1.0.0.jar、deploy/dist、
deploy/logs/app.log、测试产物、前端构建产物、 IDE ⽂件、上传⽂件、 node_modules 、
backend target 等。
交付⽬录 73MB ，其中  deploy 68MB。
deploy/logs/app.log 暴露了数据库连接失败信息：deploy/logs/app.log:29
README 写  Java 17 ，但  pom.xml 是 Java 8 ：backend/pom.xml:21
本机没有 Java/Maven ，⽆法完成后端编译；前端因  node_modules 未提取，npm run build 失败。
整改建议：
1. 建⽴标准仓库结构，只提交源码、脚本、⽂档，不提交⽇志、 target 、 dist 、 node_modules 、 uploads 。
2. 增加根⽬录  .gitignore。
3. 增加 Dockerfile 、 docker-compose 、⽣产部署脚本。
4. 建⽴ CI：后端编译、单元测试、前端构建、依赖漏洞扫描、镜像构建。
5. 统⼀ Java 版本，建议新项⽬采⽤  Java 17 或  21 LTS 。
P2-1 业务能⼒仍停留在基础管理
当前有：
租户、⽤户、配置、模板
合同、商品、订单
报关、进⼝台账、⽂件、供应商客户
退税⻚⾯
主要不⾜：
退税模块只是静态介绍⻚，没有业务数据和流程：frontend/src/views/tax/index.vue:1
缺少出⼝退税申报、单证归集、发票认证、退税进度、异常处理。
缺少收汇、结汇、付汇、核销、汇率⻛险管理。
缺少物流轨迹、冷链温控、仓储、集装箱、⼝岸节点。
缺少信保、买⽅资信、国家⻛险、应收账款预警。
缺少融资、授信、保理、订单融资、退税贷。
缺少农产品特有能⼒：溯源、检验检疫、农残检测、产地证、冷链温控、批次管理。
缺少省级监管驾驶舱：企业分布、商品结构、⽬的国、贸易额、⻛险预警、政策兑现。

---

三、对标国内先进平台的⽅向
建议对标时不要只看⻚⾯，⽽要看 “ 服务链条 ” 和 “ 数据闭环 ” 。
1. 对标国际贸易单⼀窗⼝
中国国际贸易 “ 单⼀窗⼝ ” 覆盖货物申报、舱单、许可证件、出⼝退税、⾦融服务、企业资质、原产地证、税
费办理、⼝岸物流、公共查询等多类服务。参考⻚⾯：
中国国际贸易单⼀窗⼝应⽤列表： https://nbapp.singlewindow.cn/
云南单⼀窗⼝介绍提到国家标准版包括  14 ⼤基本服务功能： https://www.csa-expo.com/special/145
对你们平台的启发：
国合平台不要重复建设海关政务系统，⽽要做企业侧业务协同和数据归集。
应把合同、订单、单证、报关、退税、物流、收结汇的数据打通，形成企业外贸档案。
与单⼀窗⼝、电⼦⼝岸、海关、税务、外汇、商务部⻔系统通过接⼝或导⼊导出衔接。
2. 对标阿⾥⼀达通
阿⾥⼀达通官⽅介绍的核⼼服务是通关、外汇、退税，并配套物流和⾦融。参考⻚⾯：
服务介绍： https://onetouch.alibaba.com/activities/onetouch-services.html
合作流程： https://onetouch.alibaba.com/activities/onetouch-procedure.html
对你们平台的启发：
平台价值不在 “ 登记合同 ” ，⽽在 “ 企业把资料交上来后，平台能帮它完成外贸全流程 ” 。
要设计服务⼯单：客户经理、单证员、报关员、财务、⻛控、审核⼈共同处理。
要沉淀贸易信⽤数据，服务后续融资、授信、⻛险定价。
3. 对标 XTransfer 等跨境⾦融平台
XTransfer 公开介绍其能⼒包括全球收款、本地收款、付款、结汇、换汇、多币种账户、⻛控服务。参考⻚
⾯：
XTransfer 官⽹： https://www.xtransfer.cn/
关于我们： https://www.xtransfer.cn/about
对你们平台的启发：
外贸服务平台必须重视资⾦流和⻛控，不只是单证流。
需要多币种、到账提醒、汇率、锁汇、收款合规、付款⽤途、反洗钱⻛控等能⼒。
国有企业平台可与银⾏、⽀付机构合作，不⼀定⾃建⽀付牌照能⼒。

---

4. 对标中国信保
中国政府⽹发布的政策⽂件明确出⼝信⽤保险在防⻛险、补损失、促融资、拓市场等⽅⾯的作⽤。参考⻚
⾯：
中国政府⽹关于出⼝信⽤保险⽀持贸易⾼质量发展：
https://www.gov.cn/lianbo/bumen/202405/content_6952952.htm
中国出⼝信⽤保险公司监督管理办法：
https://www.gov.cn/zhengce/zhengceku/202412/content_6993969.htm
对你们平台的启发：
平台要把买⽅资信、国家⻛险、逾期应收、信保投保、理赔材料纳⼊流程。
对农产品出⼝尤其要关注买⽅拒收、质量争议、⽬的国政策变化、物流延误等⻛险。
四、建议建设的软件功能蓝图
1. 基础平台
组织与租户：国合、企业、合作社、报关⾏、货代、银⾏、保险、监管单位。
⽤户与权限：⻆⾊、岗位、数据范围、菜单、按钮、接⼝、审批权限。
流程引擎：申请、受理、补正、复核、审批、退回、办结、归档。
消息中⼼：站内信、短信、邮件、微信 / 企业微信。
审计中⼼：登录、导出、下载、审批、删除、配置变更全部留痕。
数据字典：国家地区、币种、港⼝、运输⽅式、贸易⽅式、 HS 编码、监管条件。
2. 外贸业务闭环
客户与供应商：资质、联系⼈、信⽤等级、历史交易、⻛险标签。
商品与农产品档案： HS 编码、产地、批次、检验检疫、农残检测、包装规格、温控要求。
合同管理：合同评审、价格条款、付款条款、履约节点、到期预警。
订单管理：采购、销售、交付、装运、收款、开票、利润测算。
单证中⼼：发票、装箱单、合同、提单、产地证、植检证、报关草单、报关单、退税资料。
报关协同：台账、制单、复核、申报状态、回执、异常处理。
物流与仓储：订舱、集港、装柜、海运 / 铁路 / 空运轨迹、冷链温度、到港签收。
收汇结汇：应收账款、到账认领、结汇、汇率损益、逾期预警。
退税管理：发票采集、报关单匹配、退税率、申报批次、退税进度、差异处理。
信保与⻛控：买⽅资信、国家⻛险、限额申请、保单、出运申报、报损理赔。
融资服务：订单融资、退税贷、应收账款质押、信保保单融资。

---

3. 省级监管与运营
企业画像：注册地、⾏业、商品、⽬的国、贸易规模、活跃度。
外贸驾驶舱：进出⼝额、订单数、报关量、退税额、融资额、⻛险事件。
农产品专题：品类、产地、出⼝国家、价格指数、冷链异常、质量追溯。
政策匹配：补贴、展会、信保、融资、外综服政策⾃动匹配。
服务绩效：国合内部员⼯处理时效、⼯单积压、客户满意度。
⻛险预警：买⽅逾期、异常⾼频导出、报关退单、退税异常、敏感国家交易。
五、Rust 重构判断
是否建议全量  Rust 重写
不建议现在全量重写。
原因：
1. 当前最⼤问题是业务模型、数据库、权限、安全和部署，不是  Java 性能瓶颈。
2. 团队基础较弱，全量  Rust 会提⾼招聘、培训、维护成本。
3. Rust ⽣态很强，但企业管理系统的后台  CRUD 、权限、流程、报表、 Excel 、 Office 集成，⽤
Java/Spring Boot 或  Go 更容易快速交付。
4. 全量重写会让  1.0 的业务问题重新⾛⼀遍，⻛险⼤、周期⻓。
Rust 适合⽤在哪些地⽅
建议采⽤“Java 主平台  + Rust ⾼价值模块 ” 的路线。
适合 Rust 的模块：
⾼性能⽂件解析：⼤批量  Excel 、 CSV 、报关单、物流轨迹解析。
批处理任务：海量数据清洗、⻛控特征计算、报表预聚合。
安全⽹关：签名验签、加解密、接⼝限流、审计⽇志采集。
数据同步 Agent ：与单⼀窗⼝、银⾏、物流、信保系统做稳定同步。
⽂档转换服务：单证⽣成、 PDF ⽔印、⽂件  hash 、病毒扫描前置。
⻛控规则引擎：多维度规则匹配和低延迟预警。
Rust 的好处：
性能⾼，适合⼤⽂件和⾼并发。
内存安全，减少空指针、越界、并发数据竞争等问题。
单⽂件部署⽅便，运⾏资源占⽤低。
对安全敏感服务更有优势。

---

Rust 的代价：
学习曲线陡，团队需要  3 到  6 个⽉适应。
⽣态在后台管理、低代码报表、 Office 处理⽅⾯不如  Java 成熟。
开发效率前期可能下降。
需要建⽴跨语⾔接⼝规范、⽇志规范、部署规范。
推荐路线：
1. 近期 0 到 3 个⽉：修好  Java 版  1.0 ，不重写。
2. 中期 3 到 6 个⽉：把⽂件解析、数据同步、⻛控任务做成独⽴  Rust 服务。
3. ⻓期 6 到 18 个⽉：如果团队成熟，再考虑将⽹关、批处理、核⼼⻛控逐步  Rust 化。
六、推荐技术架构
当前修复版  1.1
前端：Vue 3 、 Vite 、 Element Plus 。
后端：Java 17 或  21 、 Spring Boot 3.x 、 Spring Security 、 MyBatis-Plus 或  JPA 。
数据库：MySQL 8 主从或  PostgreSQL 16 。
缓存：Redis 7 。
⽂件：MinIO 。
搜索：OpenSearch 或  Elasticsearch ，先不上也可以。
流程：Flowable 、 Camunda 8 或轻量⾃研审批流。
消息：RabbitMQ 或  RocketMQ ，前期可⽤  Redis Stream 。
部署：Docker Compose 起步，⽣产建议  Kubernetes 或⾄少容器化部署。
中⻓期 2.0
API ⽹关： Nginx/OpenResty 或  Spring Cloud Gateway 。
认证：统⼀身份认证、 MFA 、 OAuth2/OIDC 、 CA/ 国密可选。
微服务拆分：⽤户权限、贸易业务、单证中⼼、⽂件中⼼、⻛控中⼼、数据中⼼。
Rust 服务：⽂件解析、数据同步、⻛控计算、安全⽹关。
数据仓库： Doris/ClickHouse ，⽤于驾驶舱和统计。
⽇志监控： Prometheus 、 Grafana 、 Loki/ELK 、 SkyWalking 。
七、硬件配置建议
⽅案 A：试点版， 50 到  200 家企业
适合内部试运⾏、县区或园区试点。

---

应⽤服务器  2 台： 8 核  CPU 、 32GB 内存、 300GB SSD 。
数据库服务器  1 台： 8 到  16 核  CPU 、 64GB 内存、 1TB SSD ，定期备份。
⽂件服务器或  MinIO 1 台： 8 核  CPU 、 32GB 内存、 4TB 存储。
备份服务器  1 台： 4 核  CPU 、 16GB 内存、 4TB 到  8TB 存储。
带宽：公⽹  100Mbps 起，政务 / 内⽹按实际接⼊。
⽅案 B：省级⽣产版， 1000 到  5000 家企业
负载均衡 2 台： Nginx/HAProxy ，主备。
应⽤服务器  3 到  6 台： 16 核  CPU 、 64GB 内存、 500GB SSD 。
数据库主库  1 台： 32 核  CPU 、 128GB 内存、 2TB NVMe SSD 。
数据库从库  1 到  2 台：同主库或略低，⽤于读扩展和容灾。
Redis 3 节点： 8 核  CPU 、 32GB 内存。
MinIO 4 节点：每节点  8 到  16 核、 32GB 内存、 8TB 到  24TB 存储。
⽇志监控 2 台： 16 核  CPU 、 64GB 内存、 2TB 存储。
备份：本地每⽇、异地每周，关键数据⾄少保留  6 到  12 个⽉。
安全设备：WAF 、防⽕墙、堡垒机、数据库审计、漏洞扫描。
⽅案 C：云上政企版
如果允许上云，建议使⽤政务云或国资认可云资源：
SLB + ECS/ 容器服务。
RDS MySQL/PostgreSQL ⾼可⽤版。
Redis ⾼可⽤版。
OSS/对象存储。
WAF、云防⽕墙、⽇志服务、密钥管理  KMS 。
异地备份和容灾。
⼋、部署环境建议
⽣产基础：
操作系统： Rocky Linux 9 、 Anolis OS 、 openEuler 或  Ubuntu Server LTS 。
JDK：Temurin/OpenJDK 17 或  21 。
Node.js： 20 LTS 或  22 LTS ，不建议⽣产使⽤过新的⾮  LTS 。
数据库：MySQL 8.0 或  PostgreSQL 16 。
Redis：7.x 。
Nginx：1.24+ 。

---

容器：Docker + Compose 起步，后续  Kubernetes 。
证书：HTTPS 全站启⽤，内部接⼝也建议  TLS 。
安全基线：
禁⽌ root 账号直连数据库。
数据库、Redis 、 MinIO 不暴露公⽹。
所有密码进⼊环境变量或密钥管理。
管理后台启⽤  MFA 。
上传⽂件⾛病毒扫描和权限下载。
开启数据库审计、登录审计、接⼝审计。
建⽴漏洞扫描和依赖升级制度。
九、推荐实施路线图
第 0 阶段：⽴即⽌⾎， 1 到  2 周
补⻬数据库表和字段，确保当前功能能跑通。
修复 JWT 过期时间、默认密码、重置密码、 CORS 、异常泄露。
关闭公开 /uploads，改为鉴权下载。
修复 OnlyOffice callback 安全校验。
删除交付包⾥的⽇志、 target 、 dist 、 node_modules 、 uploads 。
建⽴ .gitignore、README 部署说明、环境变量模板。
第 1 阶段： 1.1 可⽤版， 1 到  2 个⽉
引⼊ Flyway/Liquibase 。
引⼊ Spring Security 或完善现有认证授权。
建⽴ RBAC 和审计⽇志。
把退税从静态⻚⾯改为真实业务模块。
完善单证中⼼、⽂件中⼼、业务流程状态机。
建⽴⾃动化测试和  CI 。
第 2 阶段： 1.5 业务闭环版， 3 到  6 个⽉
合同、订单、单证、报关、物流、收汇、退税全流程打通。
建⽴服务⼯单和内部协同。
对接银⾏、物流、信保、单⼀窗⼝数据导⼊导出。
建⽴省级驾驶舱和农产品专题。

---

上线⻛险预警和企业画像。
第 3 阶段： 2.0 平台化版， 6 到  18 个⽉
多租户、多机构、多⻆⾊、多数据范围。
微服务或模块化单体演进。
Rust 服务⽤于⽂件解析、⻛控、同步。
建⽴数据仓库和  BI 。
通过等保测评、代码审计、渗透测试。
⼗、适合你们基础较弱团队的组织建议
建议⻆⾊配置：
产品经理 1 名：懂外贸流程，负责需求和验收。
后端⼯程师  2 到  3 名： Java/Spring Boot 。
前端⼯程师  1 到  2 名： Vue 。
测试⼯程师  1 名：接⼝测试、业务流程测试。
运维/DevOps 1 名：服务器、数据库、备份、安全。
外贸业务专家  2 到  3 名：国合内部⼈员兼职也可以，负责流程校验。
安全顾问阶段性参与：上线前做代码审计和渗透测试。
管理⽅法：
不要⼀次性追求 “ ⼤⽽全 ” 。
每两周⼀个版本，固定演示和验收。
业务需求必须写成流程图、状态图、字段表、权限表。
每个功能必须有测试⽤例和验收标准。
先把 10 条最常⻅业务流程跑顺，再做复杂功能。
⼗⼀、本次本地验证情况
已完成：
成功解压真实项⽬源码。
检查后端配置、 pom 、核⼼实体、 Controller 、 Service 、 SQL 、部署包。
检查前端路由、认证存储、退税⻚、仪表盘。
检查部署⽬录、⽇志、 jar 内配置、测试脚本。
⽆法完成：
本机没有 Java Runtime 和  Maven ，后端⽆法编译运⾏。

---

前端 node_modules 未完整提取，npm run build 报 vite: command not found。
因此本次结论以静态代码审查、部署包检查、⽇志检查为主。
建议下⼀步由开发商提供：
完整 Git 仓库。
数据库现⽹结构导出。
可运⾏测试环境。
最近⼀次部署包⽣成过程。
需求⽂档、接⼝⽂档、数据库设计⽂档。
管理员、国合员⼯、企业员⼯三类账号的测试数据。
本⽂档为内部研究和项⽬推进资料，请结合正式政策⽂件、合同⽂本和业务凭证审慎使⽤。