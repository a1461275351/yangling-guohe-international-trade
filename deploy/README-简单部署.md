# 最简部署（服务器有phpStudy）

## 只需3步

### 第1步：数据库
打开 phpStudy → 启动 MySQL → 导入 sql/init.sql

### 第2步：后端
修改 application-prod.yml 中的 MySQL 密码，然后：
```cmd
java -jar trade-platform-1.0.0.jar --spring.config.location=application-prod.yml
```
后台运行：
```cmd
start /b java -jar trade-platform-1.0.0.jar --spring.config.location=application-prod.yml > app.log 2>&1
```

### 第3步：前端
方式A（phpStudy的Nginx）：
1. 把 dist/ 文件夹复制到 phpStudy 的 Nginx 站点目录
2. phpStudy 中创建网站，根目录指向 dist/
3. 添加反向代理规则：/api → http://127.0.0.1:8081

方式B（直接用Node开发服务器）：
```cmd
cd frontend
npm run dev
```
然后访问 http://服务器IP:5173

## 访问
http://服务器IP
账号: GUOHE / admin / admin123
