package com.fitmanager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;

public final class DBConnection {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection(ServletContext ctx) throws SQLException {
        String url = ctx.getInitParameter("jdbc.url");
        String user = ctx.getInitParameter("jdbc.user");
        String password = ctx.getInitParameter("jdbc.password");
        if (url == null || user == null) {
            throw new SQLException("Configuration JDBC manquante (jdbc.url, jdbc.user dans web.xml).");
        }
        if (password == null) {
            password = "";
        }
        return DriverManager.getConnection(url, user, password);
    }
}
