package com.bookstore.servlet;

import com.bookstore.dao.BookDao;
import com.bookstore.entity.Book;
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
import java.util.List;

/**
 * 书籍列表 Servlet，访问路径：/bookList
 * 登录后才能访问；每本书的“加入购物车”超链接都使用 encodeURL() 做 URL 重写。
 */
@WebServlet(name = "BookListServlet", urlPatterns = "/bookList")
public class BookListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String ctx = request.getContextPath();

        // ===== 未登录拦截：Session 不存在或 Session 中没有登录用户，重定向到登录页 =====
        HttpSession session = request.getSession(false);
        String username = (session == null) ? null : (String) session.getAttribute(SessionKeys.SESSION_USER);
        if (username == null) {
            response.sendRedirect(response.encodeRedirectURL(ctx + "/login"));
            return;
        }

        List<Book> books = bookDao.findAll();
        int cartCount = CartUtil.count(CartUtil.getCart(session));

        PrintWriter out = response.getWriter();
        out.print(PageUtil.header(request, "书籍列表"));
        out.print(PageUtil.nav(request, response, username, cartCount));
        out.print(PageUtil.sessionInfo(request, session));
        out.print("<h2>书籍列表</h2>\n<table>\n"
                + "<tr><th>编号</th><th>书名</th><th>作者</th><th>出版社</th><th>单价</th><th>操作</th></tr>\n");
        for (Book b : books) {
            // ===== URL 重写：超链接使用 encodeURL()，禁用 Cookie 时自动拼接 ;jsessionid=xxx =====
            String addUrl = response.encodeURL(ctx + "/addCart?bookId=" + b.getId());
            out.print("<tr><td>" + b.getId() + "</td>"
                    + "<td>《" + HtmlUtil.escape(b.getName()) + "》</td>"
                    + "<td>" + HtmlUtil.escape(b.getAuthor()) + "</td>"
                    + "<td>" + HtmlUtil.escape(b.getPublisher()) + "</td>"
                    + "<td class=\"price\">￥" + String.format("%.2f", b.getPrice()) + "</td>"
                    + "<td><a class=\"btn\" href=\"" + HtmlUtil.escape(addUrl) + "\">加入购物车</a></td></tr>\n");
        }
        out.print("</table>\n");
        out.print(PageUtil.footer());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
