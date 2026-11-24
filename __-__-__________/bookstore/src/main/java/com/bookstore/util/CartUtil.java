package com.bookstore.util;

import com.bookstore.entity.CartItem;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 购物车工具：从 Session 域中取出购物车、统计图书数量。
 */
public final class CartUtil {

    private CartUtil() {
    }

    /** 从 Session 域中取出购物车，不存在返回 null */
    @SuppressWarnings("unchecked")
    public static Map<Integer, CartItem> getCart(HttpSession session) {
        Object obj = session.getAttribute(SessionKeys.SESSION_CART);
        return (obj instanceof Map) ? (Map<Integer, CartItem>) obj : null;
    }

    /** 购物车中图书的总册数 */
    public static int count(Map<Integer, CartItem> cart) {
        int n = 0;
        if (cart != null) {
            for (CartItem item : cart.values()) {
                n += item.getQuantity();
            }
        }
        return n;
    }

    /** 购物车总金额 */
    public static double total(Map<Integer, CartItem> cart) {
        double sum = 0;
        if (cart != null) {
            for (CartItem item : cart.values()) {
                sum += item.getSubtotal();
            }
        }
        return sum;
    }
}
