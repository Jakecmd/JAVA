# 简易电子书店购物系统（Java Web 会话跟踪实验）

基于 **Servlet + Cookie + HttpSession + URL 重写** 实现：用户登录、记住用户名、书籍浏览、购物车、安全退出。

## 一、目录结构

```
bookstore/
├── pom.xml                                   Maven 构建文件（war 包，Servlet API 为 provided）
├── sql/bookstore.sql                         数据库脚本（t_user 用户表、t_book 图书表）
└── src/main/
    ├── java/com/bookstore/
    │   ├── entity/Book.java                  图书实体
    │   ├── entity/CartItem.java              购物车条目（图书 + 数量）
    │   ├── dao/UserDao.java                  用户校验（数据库 / 模拟账号）
    │   ├── dao/BookDao.java                  图书查询（数据库 / 模拟数据）
    │   ├── util/DBUtil.java                  数据库连接工具（读取 db.properties）
    │   ├── util/SessionKeys.java             Cookie / Session 属性名常量、Cookie 路径
    │   ├── util/CartUtil.java                购物车读取、统计
    │   ├── util/PageUtil.java                公共页面片段（导航栏超链接全部 URL 重写）
    │   ├── util/HtmlUtil.java                HTML 转义，防 XSS
    │   ├── listener/SessionListener.java     会话监听器（控制台输出会话创建/销毁）
    │   └── servlet/
    │       ├── LoginServlet.java             /login     登录（Cookie 读写、Session、转发/重定向）
    │       ├── BookListServlet.java          /bookList  书籍列表
    │       ├── AddCartServlet.java           /addCart   加入购物车（控制台打印 sessionId）
    │       ├── ShowCartServlet.java          /showCart  展示购物车
    │       └── LogoutServlet.java            /logout    安全退出（invalidate + 删除 Cookie）
    ├── resources/db.properties               数据库连接配置
    └── webapp/
        ├── login.html                        登录页面（JS 读取 Cookie 回显用户名）
        ├── css/style.css                     样式表
        └── WEB-INF/
            ├── web.xml                       欢迎页、Session 超时 30 分钟、跟踪方式
            └── lib/mysql-connector-j-8.0.33.jar
```

## 二、部署运行

### 方式 1：IDEA（推荐）
1. `File → Open` 选择 `bookstore` 文件夹（含 pom.xml），按 Maven 项目导入。
2. `Run → Edit Configurations → + → Tomcat Server → Local`，选择本机 Tomcat 8.5/9.0。
3. `Deployment` 选项卡点 `+ → Artifact → bookstore:war exploded`，`Application context` 填 `/bookstore`。
4. 运行后访问：<http://localhost:8080/bookstore/>

### 方式 2：Eclipse
`File → Import → Maven → Existing Maven Projects` 选择 `bookstore` 文件夹，右键项目 `Run As → Run on Server`。

### 方式 3：命令行打包
```
mvn clean package
```
把 `target/bookstore.war` 复制到 `Tomcat/webapps/`，启动 Tomcat 后访问 <http://localhost:8080/bookstore/>。

### 数据库（可选）
1. 在 MySQL 中执行 `sql/bookstore.sql`（或直接使用上次实验的数据库）。
2. 修改 `src/main/resources/db.properties` 中的地址、用户名、密码。
3. **没有数据库也能运行**：连接失败会自动使用内置模拟数据。

测试账号：`admin / 123456`、`张三 / 123456`、`lisi / 654321`

## 三、功能测试步骤

| 测试项 | 操作 | 预期结果 |
| --- | --- | --- |
| 记住用户名 | 勾选“记住我的用户名”登录 → 关闭浏览器 → 重新打开访问登录页 | 用户名自动回显 |
| 临时 Cookie | 不勾选登录 → 关闭浏览器 → 重新打开 | 不回显（临时 Cookie 已随浏览器关闭失效） |
| 登录失败 | 输入错误密码 | 请求转发回登录页，地址栏仍为 `/login`，显示错误提示 |
| 购物车累积 | 多次点击“加入购物车” | 购物车累计多本书，控制台打印的 sessionId 不变 |
| 会话隔离 | 换另一个浏览器（或无痕窗口）登录 | 购物车为空，sessionId 不同 |
| 禁用 Cookie | 浏览器禁用 localhost 的 Cookie 后访问 | 地址栏出现 `;jsessionid=...`，仍可登录、加购 |
| 安全退出 | 点击“安全退出” | 会话销毁、用户名 Cookie 删除，再访问 `/bookstore/showCart` 跳回登录页 |

**禁用 Cookie 方法（Chrome / Edge）**：设置 → 隐私和安全 → Cookie 和其他网站数据 → “不允许使用 Cookie 的网站” → 添加 `localhost`（或 `[*.]localhost`）。测试完记得移除。

> 注意：Chrome/Edge 若开启了“继续浏览上次打开的网页”，关闭浏览器后临时 Cookie 可能被恢复，测试临时 Cookie 时请关闭该选项，或彻底退出浏览器进程。
