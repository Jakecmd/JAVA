package com.bookstore.dao;

import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户数据访问类：校验用户名和密码。
 * 优先查询上次实验的数据库表 t_user；数据库不可用时使用内置模拟账号。
 */
public class UserDao {

    /** 内置模拟账号（数据库不可用时使用）：用户名 -> 密码 */
    private static final Map<String, String> MOCK_USERS = new HashMap<>();

    static {
        MOCK_USERS.put("admin", "123456");
        MOCK_USERS.put("张三", "123456");
        MOCK_USERS.put("lisi", "654321");
    }

    /**
     * 校验用户名、密码是否正确。
     *
     * @return true 表示校验通过
     */
    public boolean login(String username, String password) {
        if (DBUtil.isAvailable()) {
            // 使用 PreparedStatement 预编译 SQL，防止 SQL 注入
            String sql = "SELECT COUNT(*) FROM t_user WHERE username = ? AND password = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                DBUtil.markUnavailable();
                System.out.println("[UserDao] 查询数据库失败，改用模拟账号校验：" + e.getMessage());
            }
        }
        // 降级：使用内置模拟账号校验
        String pwd = MOCK_USERS.get(username);
        return pwd != null && pwd.equals(password);
    }
}
