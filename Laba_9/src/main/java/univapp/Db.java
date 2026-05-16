package univapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final String URL =
            "jdbc:derby://localhost:1527/UnivDB;create=true";
    private static final String USER = "db_user";
    private static final String PASSWORD = "db_user";

    private static Connection conn;

    public static Connection get() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("org.apache.derby.client.ClientAutoloadedDriver");
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName("org.apache.derby.jdbc.ClientDriver");
                } catch (ClassNotFoundException ex) {
                    throw new SQLException("Derby client driver not found", ex);
                }
            }
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conn;
    }

    public static void close() {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
            conn = null;
        }
    }
}
