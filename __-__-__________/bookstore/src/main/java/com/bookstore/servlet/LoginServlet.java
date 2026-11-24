package com.bookstore.servlet;

import com.bookstore.dao.UserDao;
import com.bookstore.util.HtmlUtil;
import com.bookstore.util.PageUtil;
import com.bookstore.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 登录处理 Servlet，访问路径：/login
 * <ul>
 *   <li>GET  /login：读取浏览器带来的 Cookie 数组，遍历取出用户名，回显到登录表单。</li>
 *   <li>POST /login：校验账号；按“记住我的用户名”创建持久/临时 Cookie；
 *       登录成功把用户名存入 HttpSession 并重定向（URL 重写）到书籍列表；
 *       登录失败使用请求转发回到 login.html。</li>
 * </ul>
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 预设的正确账号（模拟账号校验）；同时也支持上次实验数据库 t_user 表中的账号 */
    private static final String PRESET_USERNAME = "admin";
    private static final String PRESET_PASSWORD = "123456";

    private final UserDao userDao = new UserDao();

    /**
     * 显示登录页面：读取 Cookie 实现用户名回显。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String ctx = request.getContextPath();

        // 已登录用户直接进入书籍列表（getSession(false)：不存在会话时不新建）
        HttpSession session = request.getSession(false);
        String currentUser = (session == null) ? null : (String) session.getAttribute(SessionKeys.SESSION_USER);

        // ===== Cookie：读取浏览器发送来的 Cookie 数组，遍历查找用户名 Cookie =====
        String rememberedName = "";
        Cookie[] cookies = request.getCookies();   // 没有任何 Cookie 时返回 null，必须判空
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SessionKeys.COOKIE_USERNAME.equals(cookie.getName())) {
                    // 写入时做了 URL 编码（支持中文用户名），读取时解码
                    try {
                        rememberedName = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (IllegalArgumentException e) {
                        rememberedName = "";   // Cookie 值被篡改、格式不合法时忽略
                    }
                    break;
                }
            }
        }

        // 提示信息：从 LogoutServlet 重定向过来时显示“已安全退出”
        String tip = "";
        if ("1".equals(request.getParameter("logout"))) {
            tip = "<div class=\"msg success\">您已安全退出：会话已销毁，用户名 Cookie 已删除。</div>\n";
        } else if (currentUser != null) {
            String listUrl = response.encodeURL(ctx + "/bookList");
            tip = "<div class=\"msg success\">当前会话已登录：<b>" + HtmlUtil.escape(currentUser)
                    + "</b>，<a href=\"" + HtmlUtil.escape(listUrl) + "\">直接进入书籍列表 &raquo;</a></div>\n";
        }

        // 表单提交地址同样做 URL 重写（存在会话且未使用 Cookie 时会拼接 jsessionid）
        String action = response.encodeURL(ctx + "/login");

        PrintWriter out = response.getWriter();
        out.print(PageUtil.header(request, "用户登录"));
        out.print("<div class=\"login-box\">\n"
                + "<h2>用户登录</h2>\n"
                + tip
                + (rememberedName.isEmpty() ? ""
                : "<div class=\"msg info\">已从 Cookie 中读取到用户名：<b>" + HtmlUtil.escape(rememberedName) + "</b></div>\n")
                + "<form action=\"" + HtmlUtil.escape(action) + "\" method=\"post\">\n"
                + "  <label>用户名：<input type=\"text\" name=\"username\" value=\""
                + HtmlUtil.escape(rememberedName) + "\" placeholder=\"请输入用户名\" required></label>\n"
                + "  <label>密　码：<input type=\"password\" name=\"password\" placeholder=\"请输入密码\" required></label>\n"
                + "  <label class=\"remember\"><input type=\"checkbox\" name=\"remember\" value=\"yes\"> 记住我的用户名（7天）</label>\n"
                + "  <button type=\"submit\">登　录</button>\n"
                + "</form>\n"
                + "<p class=\"hint\">测试账号：admin / 123456（或数据库 t_user 表中的账号）</p>\n"
                + "</div>\n");
        out.print(PageUtil.footer());
    }

    /**
     * 处理登录表单提交。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ===== 处理 POST 中文乱码：必须在第一次 getParameter() 之前设置请求体编码 =====
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String ctx = request.getContextPath();

        // 1. 接收表单数据
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // 复选框：勾选时提交 value（"yes"），未勾选时浏览器根本不提交该参数，得到 null
        String remember = request.getParameter("remember");
        username = (username == null) ? "" : username.trim();

        // 2. 账号校验：预设账号 或 数据库中的账号
        boolean success = !username.isEmpty() && password != null
                && ((PRESET_USERNAME.equals(username) && PRESET_PASSWORD.equals(password))
                || userDao.login(username, password));

        if (!success) {
            // ===== 登录失败：使用请求转发跳转回登录页面（服务器内部跳转，地址栏仍为 /login）=====
            request.getRequestDispatcher("/login.html").forward(request, response);
            return;
        }

        // 3. ===== Cookie：保存用户名 =====
        // Cookie 值不能直接包含中文、空格等字符，先做 URL 编码
        Cookie nameCookie = new Cookie(SessionKeys.COOKIE_USERNAME, URLEncoder.encode(username, "UTF-8"));
        nameCookie.setPath(SessionKeys.cookiePath(request));   // 整个项目内有效
        if (remember != null) {
            // 勾选“记住我的用户名”：持久 Cookie，存活 7 天，浏览器会写入硬盘，关闭浏览器后仍然存在
            nameCookie.setMaxAge(SessionKeys.REMEMBER_SECONDS);
        }
        // 未勾选：不调用 setMaxAge()，默认值 -1，即临时 Cookie，只保存在浏览器内存中，关闭浏览器即失效
        response.addCookie(nameCookie);   // 通过响应头 Set-Cookie 发送给浏览器

        // 4. ===== HttpSession：登录成功，把用户名存入会话域 =====
        // 获取 Session 的方式一：request.getSession() 等价于 getSession(true)，不存在则新建
        HttpSession session = request.getSession();
        session.setAttribute(SessionKeys.SESSION_USER, username);
        System.out.println("[LoginServlet] 登录成功：用户=" + username
                + "，sessionId=" + session.getId()
                + "，记住用户名=" + (remember != null ? "是（持久Cookie 7天）" : "否（临时Cookie）"));

        // 5. ===== 登录成功：重定向到书籍列表，重定向地址使用 encodeRedirectURL() 做 URL 重写 =====
        response.sendRedirect(response.encodeRedirectURL(ctx + "/bookList"));
    }
}
