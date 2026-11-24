package com.bookstore.entity;

import java.io.Serializable;

/**
 * 购物车条目：一本书 + 购买数量。
 * 整个购物车是 Map&lt;Integer, CartItem&gt;，保存在 HttpSession 的 "cart" 属性中。
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Book book;     // 选购的图书
    private int quantity;  // 数量（同一本书多次加入时累加）

    public CartItem(Book book) {
        this.book = book;
        this.quantity = 1;
    }

    /** 同一本书再次加入购物车，数量 +1 */
    public void increase() {
        this.quantity++;
    }

    /** 小计 = 单价 × 数量 */
    public double getSubtotal() {
        return book.getPrice() * quantity;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
