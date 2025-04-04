package ChatServer;

import java.sql.*;
import java.time.LocalDateTime;

public class ConnectionDB {
    public static Connection conn;
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/ChatServer";

    public static void connectDB(String dbUrl) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl);
            System.out.println("Kết nối database MySQL thành công!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Lỗi kết nối database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean verifyLogin(String username, String md5Password) {
        try {
            String query = "SELECT COUNT(*) FROM User WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, md5Password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi xác thực đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static void saveMessage(String sender, String receiver, String messageText) {
        try {
            String query = "INSERT INTO Message (message_text, message_date, sender, receiver) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, messageText);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, sender);
            stmt.setString(4, receiver); // receiver có thể là null cho tin nhắn công khai
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Lỗi khi lưu tin nhắn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Đã đóng kết nối database!");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi đóng kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}