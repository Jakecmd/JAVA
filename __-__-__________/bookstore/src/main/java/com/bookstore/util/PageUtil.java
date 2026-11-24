package com.bookstore.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 页面公共片段生成工具（页头、导航栏、会话信息框、页脚）。
 * 注意：导航栏中的所有超链接都调用 response.encodeURL() 进行 URL 重写，
 * 浏览器禁用 Cookie 时会自动在地址后拼接 ;jsessionid=xxx，保证会话不丢失。
 */
public final class PageUtil {

    private PageUtil() {
    }

    /** 页面头部 */
    public static String header(HttpServletRequest request, String title) {
        return header(request, title, "");
    }

    /** 页面头部，extraHead 为额外放入 &lt;head&gt; 的内容（如自动跳转的 meta 标签） */
    public static String header(HttpServletRequest request, String title, String extraHead) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"zh-CN\">\n<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + extraHead
                + "<title>" + HtmlUtil.escape(title) + " - 简易电子书店</title>\n"
                + "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/css/style.css\">\n"
                + "</head>\n<body>\n<div class=\"container\">\n";
    }

    /** 顶部导航栏：欢迎信息 + 书籍列表 / 购物车 / 安全退出（全部 URL 重写） */
    public static String nav(HttpServletRequest request, HttpServletResponse response,
                             String username, int cartCount) {
        String ctx = request.getContextPath();
        // ===== URL 重写：页面中的超链接统一使用 encodeURL() =====
        String bookListUrl = response.encodeURL(ctx + "/bookList");
        String cartUrl = response.encodeURL(ctx + "/showCart");
        String logoutUrl = response.encodeURL(ctx + "/logout");
        return "<div class=\"nav\">\n"
                + "  <span class=\"brand\">简易电子书店</span>\n"
                + "  <span class=\"welcome\">欢迎您，<b>" + HtmlUtil.escape(username) + "</b></span>\n"
                + "  <a href=\"" + HtmlUtil.escape(bookListUrl) + "\">书籍列表</a>\n"
                + "  <a href=\"" + HtmlUtil.escape(cartUrl) + "\">我的购物车（" + cartCount + "）</a>\n"
                + "  <a class=\"logout\" href=\"" + HtmlUtil.escape(logoutUrl) + "\">安全退出</a>\n"
                + "</div>\n";
    }

    /** 会话信息框：显示 sessionId、传递方式、创建时间、超时时间，便于观察实验现象 */
    public static String sessionInfo(HttpServletRequest request, HttpSession session) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String way;
        if (request.isRequestedSessionIdFromCookie()) {
            way = "Cookie（请求头携带 JSESSIONID）";
        } else if (request.isRequestedSessionIdFromURL()) {
            way = "URL 重写（地址中携带 ;jsessionid=）";
        } else {
            way = "新建会话（本次请求未携带 SessionID）";
        }
        return "<div class=\"session-info\">\n"
                + "  <div><b>当前 SessionID：</b><code>" + session.getId() + "</code></div>\n"
                + "  <div><b>SessionID 传递方式：</b>" + way + "</div>\n"
                + "  <div><b>会话创建时间：</b>" + fmt.format(new Date(session.getCreationTime()))
                + "　<b>超时时间：</b>" + (session.getMaxInactiveInterval() / 60) + " 分钟</div>\n"
                + "</div>\n";
    }

    /** 页面尾部 */
    public static String footer() {
        return "<div class=\"footer\">Java Web 实验 · 基于会话跟踪技术的简易电子书店购物系统</div>\n"
                + "</div>\n</body>\n</html>\n";
    }
}
