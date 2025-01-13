import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ClientServer";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Aditya@2005";

    public ProblemDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }

    public void addProblem(String clientIp, String deviceName, String problemDesc) {
        String sql = "INSERT INTO ClientProblems (client_ip, device_name, problem_desc, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, 'Open', NOW(), NOW())";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, clientIp);
            stmt.setString(2, deviceName);
            stmt.setString(3, problemDesc);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPendingProblems() {
        List<String> problems = new ArrayList<>();
        String sql = "SELECT * FROM ClientProblems WHERE status = 'Open'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                problems.add("ID: " + rs.getInt("id") +
                             ", IP: " + rs.getString("client_ip") +
                             ", Device: " + rs.getString("device_name") +
                             ", Problem: " + rs.getString("problem_desc") +
                             ", Status: " + rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return problems;
    }
}

