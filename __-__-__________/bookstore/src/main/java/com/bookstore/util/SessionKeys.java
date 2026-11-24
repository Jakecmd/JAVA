package com.bookstore.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 会话跟踪相关的常量与 Cookie 工具方法。
 */
public final class SessionKeys {

    /** 保存用户名的 Cookie 名称 */
    public static final String COOKIE_USERNAME = "username";

    /** Session 域中保存登录用户名的属性名 */
    public static final String SESSION_USER = "loginUser";

    /** Session 域中保存购物车的属性名（值类型：Map&lt;Integer, CartItem&gt;） */
    public static final String SESSION_CART = "cart";

    /** “记住我的用户名”持久 Cookie 的存活时间：7 天（单位：秒） */
    public static final int REMEMBER_SECONDS = 7 * 24 * 60 * 60;

    private SessionKeys() {
    }

    /**
     * Cookie 的作用路径：设置为项目根路径（如 /bookstore），
     * 保证 login.html、/login 等所有页面都能读到该 Cookie；
     * 删除 Cookie 时也必须使用相同的 path，否则删不掉。
     */
    public static String cookiePath(HttpServletRequest request) {
        String ctx = request.getContextPath();
        return (ctx == null || ctx.isEmpty()) ? "/" : ctx;
    }
}
