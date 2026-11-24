package com.bookstore.servlet;

import com.bookstore.entity.CartItem;
import com.bookstore.util.CartUtil;
import com.bookstore.util.HtmlUtil;
import com.bookstore.util.PageUtil;
import com.bookstore.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 展示购物车 Servlet，访问路径：/showCart
 * <ul>
 *   <li>从 Session 域取出购物车并输出；未登录重定向到登录页面；</li>
 *   <li>action=remove&amp;bookId=x：移除某本书；action=clear：清空购物车（removeAttribute 删除会话属性）；</li>
 *   <li>页面中所有超链接全部使用 encodeURL() 做 URL 重写。</li>
 * </ul>
 */
@WebServlet(name = "ShowCartServlet", urlPatterns = "/showCart")
public class ShowCartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String ctx = request.getContextPath();

        // ===== 获取 Session 并判断是否登录，未登录重定向到登录页面 =====
        HttpSession session = request.getSession(false);
        String username = (session == null) ? null : (String) session.getAttribute(SessionKeys.SESSION_USER);
        if (username == null) {
            response.sendRedirect(response.encodeRedirectURL(ctx + "/login"));
            return;
        }

        // ===== 从 Session 域中取出购物车数据 =====
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // 购物车维护操作：移除一本 / 清空，处理完后重定向回购物车（PRG，防止刷新重复提交）
        String action = request.getParameter("action");
        if ("remove".equals(action) && cart != null) {
            try {
                cart.remove(Integer.parseInt(request.getParameter("bookId")));
                session.setAttribute(SessionKeys.SESSION_CART, cart);
            } catch (NumberFormatException ignored) {
                // 参数不合法时忽略
            }
            response.sendRedirect(response.encodeRedirectURL(ctx + "/showCart"));
            return;
        }
        if ("clear".equals(action)) {
            // ===== 删除 Session 域属性 =====
            session.removeAttribute(SessionKeys.SESSION_CART);
            response.sendRedirect(response.encodeRedirectURL(ctx + "/showCart"));
            return;
        }

        PrintWriter out = response.getWriter();
        out.print(PageUtil.header(request, "我的购物车"));
        out.print(PageUtil.nav(request, response, username, CartUtil.count(cart)));
        out.print(PageUtil.sessionInfo(request, session));
        out.print("<h2>我的购物车</h2>\n");

        // ===== URL 重写：页面所有超链接都调用 encodeURL() =====
        String listUrl = response.encodeURL(ctx + "/bookList");
        String clearUrl = response.encodeURL(ctx + "/showCart?action=clear");
        String logoutUrl = response.encodeURL(ctx + "/logout");

        if (cart == null || cart.isEmpty()) {
            out.print("<div class=\"msg info\">购物车还是空的，快去挑选图书吧！</div>\n");
        } else {
            out.print("<table>\n<tr><th>编号</th><th>书名</th><th>作者</th><th>单价</th>"
                    + "<th>数量</th><th>小计</th><th>操作</th></tr>\n");
            for (CartItem item : cart.values()) {
                String removeUrl = response.encodeURL(ctx + "/showCart?action=remove&bookId=" + item.getBook().getId());
                out.print("<tr><td>" + item.getBook().getId() + "</td>"
                        + "<td>《" + HtmlUtil.escape(item.getBook().getName()) + "》</td>"
                        + "<td>" + HtmlUtil.escape(item.getBook().getAuthor()) + "</td>"
                        + "<td>￥" + String.format("%.2f", item.getBook().getPrice()) + "</td>"
                        + "<td>" + item.getQuantity() + "</td>"
                        + "<td class=\"price\">￥" + String.format("%.2f", item.getSubtotal()) + "</td>"
                        + "<td><a class=\"btn gray\" href=\"" + HtmlUtil.escape(removeUrl) + "\">移除</a></td></tr>\n");
            }
            out.print("<tr class=\"total\"><td colspan=\"4\">合计</td><td>" + CartUtil.count(cart) + " 本</td>"
                    + "<td class=\"price\">￥" + String.format("%.2f", CartUtil.total(cart)) + "</td><td></td></tr>\n"
                    + "</table>\n");
        }

        out.print("<p class=\"actions\">"
                + "<a class=\"btn\" href=\"" + HtmlUtil.escape(listUrl) + "\">继续购物</a> "
                + ((cart == null || cart.isEmpty()) ? ""
                : "<a class=\"btn gray\" href=\"" + HtmlUtil.escape(clearUrl)
                + "\" onclick=\"return confirm('确定清空购物车吗？')\">清空购物车</a> ")
                + "<a class=\"btn red\" href=\"" + HtmlUtil.escape(logoutUrl) + "\">安全退出</a>"
                + "</p>\n");
        out.print(PageUtil.footer());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
