-- ==============================================================
-- 简易电子书店 数据库脚本（MySQL 5.7 / 8.0）
-- 如果上次实验已经建好了数据库，只需确认存在 t_user、t_book 两张表，
-- 或修改 db.properties 与 DAO 中的 SQL，使其指向上次实验的表。
-- ==============================================================
CREATE DATABASE IF NOT EXISTS bookstore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE bookstore;

-- 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id       INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户编号',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(50) NOT NULL COMMENT '密码'
) COMMENT '用户表';

INSERT INTO t_user (username, password) VALUES
('admin', '123456'),
('张三', '123456'),
('lisi', '654321');

-- 图书表
DROP TABLE IF EXISTS t_book;
CREATE TABLE t_book (
    id        INT PRIMARY KEY AUTO_INCREMENT COMMENT '图书编号',
    name      VARCHAR(100) NOT NULL COMMENT '书名',
    author    VARCHAR(50) COMMENT '作者',
    publisher VARCHAR(50) COMMENT '出版社',
    price     DECIMAL(8, 2) NOT NULL COMMENT '单价'
) COMMENT '图书表';

INSERT INTO t_book (id, name, author, publisher, price) VALUES
(1, 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', 119.00),
(2, 'Head First Servlets & JSP', 'Bryan Basham', '中国电力出版社', 98.00),
(3, '深入理解Java虚拟机', '周志明', '机械工业出版社', 129.00),
(4, 'Effective Java（第3版）', 'Joshua Bloch', '机械工业出版社', 119.00),
(5, '图解HTTP', '上野宣', '人民邮电出版社', 49.00),
(6, 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', 39.00);
