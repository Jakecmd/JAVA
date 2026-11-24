package com.bookstore.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 会话监听器：在控制台输出 Session 的创建与销毁，用于观察 Session 生命周期。
 * <ul>
 *   <li>创建：服务器端第一次调用 request.getSession() 时；</li>
 *   <li>销毁：调用 session.invalidate()，或超过 web.xml 中配置的超时时间无访问。</li>
 * </ul>
 */
@WebListener
public class SessionListener implements HttpSessionListener {

    /** 当前存活的会话数量（即在线会话数） */
    private static final AtomicInteger ACTIVE = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("[SessionListener] 会话创建 sessionId=" + se.getSession().getId()
                + "，当前会话数=" + ACTIVE.incrementAndGet());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("[SessionListener] 会话销毁 sessionId=" + se.getSession().getId()
                + "，当前会话数=" + ACTIVE.decrementAndGet());
    }
}
