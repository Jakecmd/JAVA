package com.bookstore.servlet;

import com.bookstore.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 安全退出 Servlet，访问路径：/logout
 * <ol>
 *   <li>session.invalidate() 销毁当前会话，清空会话中的全部数据（登录用户、购物车）；</li>
 *   <li>setMaxAge(0) 删除保存用户名的 Cookie；</li>
 *   <li>encodeRedirectURL() 重写重定向地址，重定向回登录页面。</li>
 * </ol>
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // ===== HttpSession：销毁当前会话 =====
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            Object user = session.getAttribute(SessionKeys.SESSION_USER);
            session.invalidate();   // 会话立即失效，会话域中所有属性被解除绑定
            System.out.println("[LogoutServlet] 用户 " + user + " 安全退出，已销毁会话 sessionId=" + sessionId);
        }

        // ===== Cookie：删除保存用户名的 Cookie =====
        // 新建同名、同 path 的 Cookie，setMaxAge(0) 表示立即过期，回写给浏览器后浏览器会删除它
        Cookie cookie = new Cookie(SessionKeys.COOKIE_USERNAME, "");
        cookie.setPath(SessionKeys.cookiePath(request));   // path 必须与创建时一致，否则删除不掉
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // ===== URL 重写：重定向地址使用 encodeRedirectURL()，重定向回登录页面 =====
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/login?logout=1"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
