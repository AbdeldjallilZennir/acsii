package com.mycreche.utils;

import java.sql.*;

public class TestDatabaseTables {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "users", new String[]{"TABLE"});
            
            if (tables.next()) {
                System.out.println("✅ 'users' table exists!");
            } else {
                System.out.println("❌ 'users' table missing!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}