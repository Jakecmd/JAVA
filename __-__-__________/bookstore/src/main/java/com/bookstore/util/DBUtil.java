package com.bookstore.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库工具类（沿用上次实验的 MySQL 数据库）。
 * <p>
 * 连接参数从 classpath 下的 db.properties 读取。
 * 如果数据库未启用、驱动缺失或连接失败，会把 available 标记为 false，
 * DAO 层自动降级为内置模拟数据，保证项目在没有数据库的机器上也能直接部署运行。
 */
public final class DBUtil {

    private static String url;
    private static String username;
    private static String password;

    /** 数据库当前是否可用（首次连接失败后置为 false，避免每次请求都等待连接超时） */
    private static volatile boolean available = false;

    static {
        Properties props = new Properties();
        // 使用 UTF-8 读取配置文件，避免注释/中文密码乱码
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } else {
                System.out.println("[DBUtil] 未找到 db.properties，使用内置模拟数据");
            }
        } catch (Exception e) {
            System.out.println("[DBUtil] 读取 db.properties 失败：" + e.getMessage());
        }

        boolean enabled = Boolean.parseBoolean(props.getProperty("jdbc.enabled", "false"));
        url = props.getProperty("jdbc.url");
        username = props.getProperty("jdbc.username");
        password = props.getProperty("jdbc.password");

        if (enabled && url != null) {
            try {
                // 加载 MySQL 驱动（mysql-connector-j 已放在 WEB-INF/lib 下）
                Class.forName(props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
                available = true;
            } catch (ClassNotFoundException e) {
                System.out.println("[DBUtil] 未找到 MySQL 驱动，使用内置模拟数据");
            }
        } else {
            System.out.println("[DBUtil] 数据库未启用（jdbc.enabled=false），使用内置模拟数据");
        }
    }

    private DBUtil() {
    }

    /** 数据库是否可用 */
    public static boolean isAvailable() {
        return available;
    }

    /** 当表结构不匹配或查询异常时标记数据库不可用，后续请求自动使用模拟数据 */
    public static void markUnavailable() {
        available = false;
    }

    /**
     * 获取数据库连接；连接失败时标记数据库不可用并抛出异常，由 DAO 层降级处理。
     */
    public static Connection getConnection() throws SQLException {
        if (!available) {
            throw new SQLException("数据库不可用");
        }
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            available = false;
            System.out.println("[DBUtil] 数据库连接失败，后续请求改用内置模拟数据：" + e.getMessage());
            throw e;
        }
    }
}
