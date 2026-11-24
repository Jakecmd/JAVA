package com.bookstore.servlet;

import com.bookstore.dao.BookDao;
import com.bookstore.entity.Book;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加入购物车 Servlet，访问路径：/addCart?bookId=编号
 * <ol>
 *   <li>获取 HttpSession，未登录则重定向到登录页；</li>
 *   <li>把图书放入 Session 域中的购物车（Map），同一本书数量累加，多本书累积保存；</li>
 *   <li>控制台打印 sessionId，观察同一用户多次请求 sessionId 不变；</li>
 *   <li>跳转到购物车页面，跳转地址使用 response.encodeURL() 做 URL 重写。</li>
 * </ol>
 */
@WebServlet(name = "AddCartServlet", urlPatterns = "/addCart")
public class AddCartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String ctx = request.getContextPath();

        // ===== HttpSession：获取 Session 的方式二 getSession(false)，不存在时返回 null，不会新建 =====
        HttpSession session = request.getSession(false);
        // ===== 未登录拦截：Session 中没有登录用户名，重定向到登录页面 =====
        String username = (session == null) ? null : (String) session.getAttribute(SessionKeys.SESSION_USER);
        if (username == null) {
            response.sendRedirect(response.encodeRedirectURL(ctx + "/login"));
            return;
        }

        // 获取要加入购物车的图书
        Book book = null;
        try {
            book = bookDao.findById(Integer.parseInt(request.getParameter("bookId")));
        } catch (NumberFormatException e) {
            // bookId 缺失或不是数字，book 保持为 null
        }
        if (book == null) {
            response.sendRedirect(response.encodeRedirectURL(ctx + "/bookList"));
            return;
        }

        // ===== 购物车保存在 Session 域：key=图书编号，value=购物车条目 =====
        Map<Integer, CartItem> cart = CartUtil.getCart(session);
        if (cart == null) {
            cart = new LinkedHashMap<>();   // 第一次加入购物车时创建（LinkedHashMap 保持加入顺序）
        }
        CartItem item = cart.get(book.getId());
        if (item == null) {
            cart.put(book.getId(), new CartItem(book));   // 新书：放入购物车
        } else {
            item.increase();                              // 已有：数量 +1
        }
        session.setAttribute(SessionKeys.SESSION_CART, cart);   // 存入（覆盖）Session 域属性

        int count = CartUtil.count(cart);
        // ===== 控制台打印 sessionId：同一浏览器多次请求，sessionId 保持不变 =====
        System.out.println("[AddCartServlet] sessionId=" + session.getId()
                + " | 用户=" + username
                + " | 加入《" + book.getName() + "》"
                + " | 购物车共 " + count + " 本");

        // ===== URL 重写：跳转购物车的地址调用 response.encodeURL() =====
        String cartUrl = response.encodeURL(ctx + "/showCart");
        String listUrl = response.encodeURL(ctx + "/bookList");

        PrintWriter out = response.getWriter();
        // 页面 1 秒后自动跳转到（URL 重写后的）购物车地址
        String refresh = "<meta http-equiv=\"refresh\" content=\"1;url=" + HtmlUtil.escape(cartUrl) + "\">\n";
        out.print(PageUtil.header(request, "加入购物车", refresh));
        out.print(PageUtil.nav(request, response, username, count));
        out.print("<div class=\"msg success\">《" + HtmlUtil.escape(book.getName())
                + "》已加入购物车，当前共 " + count + " 本。1 秒后自动进入购物车……</div>\n");
        out.print("<div class=\"session-info\"><b>本次请求的 SessionID：</b><code>" + session.getId() + "</code></div>\n");
        out.print("<p><a class=\"btn\" href=\"" + HtmlUtil.escape(cartUrl) + "\">立即查看购物车</a> "
                + "<a class=\"btn gray\" href=\"" + HtmlUtil.escape(listUrl) + "\">继续购物</a></p>\n");
        out.print(PageUtil.footer());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
