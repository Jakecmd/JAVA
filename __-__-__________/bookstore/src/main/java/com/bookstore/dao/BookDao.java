package com.bookstore.dao;

import com.bookstore.entity.Book;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 图书数据访问类。
 * 优先从数据库表 t_book 读取；数据库不可用时返回内置的模拟图书列表。
 */
public class BookDao {

    /** 内置模拟图书数据（数据库不可用时使用） */
    private static final List<Book> MOCK_BOOKS;

    static {
        List<Book> list = new ArrayList<>();
        list.add(new Book(1, "Java核心技术 卷I", "Cay S. Horstmann", "机械工业出版社", 119.00));
        list.add(new Book(2, "Head First Servlets & JSP", "Bryan Basham", "中国电力出版社", 98.00));
        list.add(new Book(3, "深入理解Java虚拟机", "周志明", "机械工业出版社", 129.00));
        list.add(new Book(4, "Effective Java（第3版）", "Joshua Bloch", "机械工业出版社", 119.00));
        list.add(new Book(5, "图解HTTP", "上野宣", "人民邮电出版社", 49.00));
        list.add(new Book(6, "MySQL必知必会", "Ben Forta", "人民邮电出版社", 39.00));
        MOCK_BOOKS = Collections.unmodifiableList(list);
    }

    /** 查询全部图书 */
    public List<Book> findAll() {
        if (DBUtil.isAvailable()) {
            String sql = "SELECT id, name, author, publisher, price FROM t_book ORDER BY id";
            List<Book> books = new ArrayList<>();
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(toBook(rs));
                }
                if (!books.isEmpty()) {
                    return books;
                }
                System.out.println("[BookDao] 数据库 t_book 表为空，回退使用内置模拟图书数据");
            } catch (SQLException e) {
                DBUtil.markUnavailable();
                System.out.println("[BookDao] 查询数据库失败，改用模拟图书数据：" + e.getMessage());
            }
        }
        return MOCK_BOOKS;
    }

    /** 根据编号查询图书，不存在返回 null */
    public Book findById(int id) {
        if (DBUtil.isAvailable()) {
            String sql = "SELECT id, name, author, publisher, price FROM t_book WHERE id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return toBook(rs);
                    }
                }
            } catch (SQLException e) {
                DBUtil.markUnavailable();
                System.out.println("[BookDao] 查询数据库失败，改用模拟图书数据：" + e.getMessage());
            }
        }
        for (Book b : MOCK_BOOKS) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    /** 把结果集当前行封装为 Book 对象 */
    private Book toBook(ResultSet rs) throws SQLException {
        return new Book(rs.getInt("id"), rs.getString("name"), rs.getString("author"),
                rs.getString("publisher"), rs.getDouble("price"));
    }
}
