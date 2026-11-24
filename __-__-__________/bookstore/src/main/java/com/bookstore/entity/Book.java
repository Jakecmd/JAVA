package com.bookstore.entity;

import java.io.Serializable;

/**
 * 图书实体类，对应数据库表 t_book。
 * 实现 Serializable：对象会被放入 HttpSession（购物车），
 * Tomcat 重启/会话持久化时需要把 Session 中的对象序列化到磁盘。
 */
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;          // 图书编号
    private String name;     // 书名
    private String author;   // 作者
    private String publisher;// 出版社
    private double price;    // 单价

    public Book() {
    }

    public Book(int id, String name, String author, String publisher, double price) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
