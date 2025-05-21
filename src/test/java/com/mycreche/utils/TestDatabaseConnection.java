package com.mycreche.utils;

import java.sql.*;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection()) {
            System.out.println("✅ Database connection successful!");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}