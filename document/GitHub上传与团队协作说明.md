# GitHub上传与团队协作说明

杨凌国合外贸综合服务平台资料
GitHub 上传与团队协作说明
由 Markdown 阅读资料转换⽣成  · 通⽤⾃适应阅读版  · 适合转发和专家审阅
GitHub 上传与团队协作说明
更新时间： 2026-05-02
适⽤项⽬：杨凌国合外贸综合服务平台  1.0
⼀、先理解⼏个概念
名词 通俗理解
Git 你电脑⾥的 “ 代码修改记录本 ” ，能记录谁改了
什么、什么时候改的
GitHub 放在⽹上的代码仓库，团队成员都能同步代码
仓库 repository ⼀个项⽬的代码库
commit ⼀次修改记录，⽐如 “ 修复配置⻚错误 ”
push 把你电脑上的修改上传到  GitHub
pull 把 GitHub 上别⼈改的内容拉到你电脑
branch 分⽀，适合每个⼈单独改功能，最后再合并
Pull Request 合并申请，团队看完代码后再合并到主分⽀
你们现在 3 个⼈协作，建议⼀开始就养成这个习惯：

---

每个⼈单独建分⽀修改
改完提交 commit
推送到 GitHub
通过 Pull Request 合并到  main
不要 3 个⼈都直接改  main，否则很容易互相覆盖。
⼆、上传前不要传哪些东⻄
项⽬根⽬录已增加  .gitignore，默认不会上传这些内容：
frontend/node_modules/：前端依赖，太⼤，别⼈⽤  npm ci
安装即可。
frontend/dist/：前端构建产物，可重新构建。
backend/target/：后端 JAR 构建产物，可重新打包。
deploy/dist/、deploy/*.jar：部署构建产物，可后续发布
版本时单独处理。
uploads/：业务上传⽂件，可能包含合同、发票、报关资料，不
应上传。
logs/、*.log：运⾏⽇志，不应上传。
.env、本地配置⽂件、压缩包：避免泄露密码或产⽣巨⼤仓库。
特别提醒： GitHub 私有仓库也不要上传真实合同、报关单、发票、客户
资料、身份证、营业执照、银⾏流⽔、真实密码。

---

三、推荐上传⽅式
你有两种⽅式：
1. GitHub Desktop ：适合第⼀次使⽤，界⾯化。
2. 命令⾏：更像正式开发流程，建议慢慢学会。
我建议你这次⽤ “ ⽹⻚建仓库  + 命令⾏上传 ” ，因为以后团队协作迟早要
⽤命令⾏。
四、在 GitHub ⽹站创建私⼈仓库
1. 打开 GitHub 。
2. 登录你的账号。
3. 点击右上⻆  +。
4. 选择 New repository。
5. Repository name 建议填写：
yangling-guohe-trade-platform
1. Description 可填写：
杨凌国合外贸综合服务平台  1.0
1. 选择 Private，⼀定要选私⼈仓库。
2. 不要勾选 Add a README file。
3. 不要勾选 .gitignore。

---

4. 不要选择 License 。
5. 点击 Create repository。
创建完成后， GitHub 会给你⼀个仓库地址，类似：
https://github.com/ 你的⽤户名 /yangling-guohe-trade-platform.git
后⾯命令⾥要⽤这个地址。
五、第⼀次本地初始化并上传
进⼊项⽬⽬录：
cd /Users/wangbin/Documents/Codex/review_src2/autoWorkPlatform-
v1.0.0
初始化 Git ：
git init
设置主分⽀名称：
git branch -M main
查看将要上传的⽂件：
git status
添加⽂件：

---

git add .
提交第⼀版：
git commit -m " 初始化外贸综合服务平台 1.0 修复版 "
绑定 GitHub 仓库地址，把下⾯地址换成你⾃⼰  GitHub 创建后的地址：
git remote add origin https://github.com/ 你的⽤户名 /yangling-
guohe-trade-platform.git
上传：
git push -u origin main
如果提示登录  GitHub ，按提示登录即可。
六、以后每天怎么协作
每天开始⼯作前先拉最新代码：
git pull
创建⾃⼰的分⽀，例如你要改服务企业台账：
git checkout -b feature/service-company-ledger
修改代码后查看改了什么：

---

git status
提交修改：
git add .
git commit -m " 新增服务企业台账基础⻚⾯ "
上传分⽀：
git push -u origin feature/service-company-ledger
然后到 GitHub ⽹站上创建  Pull Request ，请另外两个⼈看⼀下，再合并
到 main。
七、团队  3 ⼈最简单分⼯建议
成员 建议分⽀ 负责⽅向
你 feature/platform-
planning需求、认定材料、业务流程、
验收
同事 A feature/frontend-
pages前端⻚⾯、菜单、表单、交互
同事 B feature/backend-api 后端接⼝、数据库、权限、部
署
也可以按功能分：
功能 分⽀名
服务企业台账 feature/service-company-ledger

---

功能 分⽀名
外综服合同模板管理 feature/service-contract-
template
认定指标统计 feature/recognition-dashboard
PVE Docker 部署 feature/docker-deploy
⼋、commit 信息怎么写
好的 commit 信息应该让⼈⼀眼看懂。
建议：
修复配置管理状态更新错误
新增外综服补充服务确认函模板
新增服务企业台账字段设计
修复合同列表路由空⽩⻚
补⻬进⼝台账数据库表
不建议：
修改
111
更新⼀下
临时改改
九、第⼀次上传前建议检查
上传前运⾏：

---

git status
如果看到这些⽬录，不要上传：
frontend/node_modules/
backend/target/
frontend/dist/
deploy/dist/
uploads/
logs/
如果不确定，把  git status 的输出发给我，我帮你判断。
⼗、以后如何恢复或查看谁改了什么
查看提交记录：
git log --oneline
查看某个⽂件改了什么：
git diff ⽂件路径
查看当前⼯作区改了什么：
git diff
查看每个⽂件是谁最后改的：

---

git blame ⽂件路径
⼗⼀、重要安全提醒
1. 仓库必须是  Private。
2. 不要上传真实客户资料和业务附件。
3. 不要上传数据库备份。
4. 不要上传真实服务器密码。
5. 不要上传真实  JWT 密钥、数据库密码、 GitHub Token 。
6. 后续⽣产服务器部署⽤的密码，放在服务器  .env 或⽣产配置⾥，
不提交到 GitHub 。
⼗⼆、你们⽬前最适合的节奏
第⼀步：把当前  1.0 修复版上传到  GitHub ，作为基线版本。
第⼆步：三个⼈都从  GitHub 拉代码。
第三步：先做  1.1 版本，不要⼀开始改太⼤。
1. 服务企业台账。
2. 外综服服务事项记录。
3. 认定指标统计。
4. Docker/PVE 测试部署。
5. 操作⽇志和权限细化。
第四步：每周固定合并⼀次，形成⼀个⼩版本。

---

建议版本节奏：
v1.0.0 当前修复版
v1.1.0 服务企业台账和认定基础功能
v1.2.0 PVE Docker 测试部署
v1.5.0 外综服认定材料管理
v2.0.0 省级外综服平台增强版
⼗三、我建议你下⼀步做什么
你先在 GitHub 上创建私⼈仓库，拿到仓库地址。
然后把地址发给我，我可以帮你在本机完成：
git init
git add
git commit
git remote add
git push
上传前我会再帮你检查⼀遍，确认没有把  node_modules、构建产
物、上传⽂件、⽇志、压缩包传上去。
本⽂档为内部研究和项⽬推进资料，请结合正式政策⽂件、合同⽂本和业务凭证审慎使⽤。