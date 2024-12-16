import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Work_MySql {

    private static final String url = "jdbc:mysql://localhost:3306/users";
    private static final String user = "root";
    private static final String pass = "l+oimy:^`5Kf-n+";

    public static int ADDandDEL(long chatid, String firstname, String lastname, String username, int status) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                String query = "";

                if (status == 0) {
                    query = "SELECT * FROM User";
                    try (PreparedStatement pstmt = conn.prepareStatement(query);
                         ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int ID = rs.getInt("chatID");
                            String FirstName = rs.getString("FirstName");
                            String LastName = rs.getString("LastName");
                            String UserName = rs.getString("UserName");
                            System.out.println("chatID:" + ID + "; Firstname:" + FirstName + "; LastName: " + LastName + "; UserName: " + UserName);
                        }
                    }
                } else if (status == 1) {
                    String checkQuery = "SELECT COUNT(*) FROM users.user WHERE chatID = ?";
                    try (PreparedStatement checkPstmt = conn.prepareStatement(checkQuery)) {
                        checkPstmt.setLong(1, chatid);
                        try (ResultSet checkRs = checkPstmt.executeQuery()) {
                            if (checkRs.next() && checkRs.getInt(1) > 0) {
                                System.out.println("Пользователь с chatID " + chatid + " уже существует.");
                                return 1;
                            }
                        }
                    }

                    String insertQuery = "INSERT INTO users.user (chatID, FirstName, LastName, UserName) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertPstmt = conn.prepareStatement(insertQuery)) {
                        insertPstmt.setLong(1, chatid);
                        insertPstmt.setString(2, firstname);
                        insertPstmt.setString(3, lastname);
                        insertPstmt.setString(4, username);
                        int affectedRows = insertPstmt.executeUpdate();
                        System.out.println("Добавлено строк: " + affectedRows);
                    }
                } else if (status == 2) {
                    String checkQuery = "SELECT COUNT(*) FROM users.user WHERE chatID = ?";
                    try (PreparedStatement checkPstmt = conn.prepareStatement(checkQuery)) {
                        checkPstmt.setLong(1, chatid);
                        try (ResultSet checkRs = checkPstmt.executeQuery()) {
                            if (checkRs.next() && checkRs.getInt(1) > 0) {
                                System.out.println("Пользователь с chatID " + chatid + " удаляется.");
                                query = "DELETE FROM users.user WHERE chatID = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                                    pstmt.setLong(1, chatid);
                                    pstmt.executeUpdate();
                                }
                            }
                            else{
                                return 2;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
        }
        return 0;
    }

    public static List<User> getAllUsers(int status) {
        List<User> users = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                String query = "SELECT chatID, FirstName, LastName, UserName FROM users.user";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            long chatID = rs.getLong("chatID");
                            String firstName = rs.getString("FirstName");
                            String lastName = rs.getString("LastName");
                            String userName = rs.getString("UserName");
                            users.add(new User(chatID, firstName, lastName, userName));
                        }
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при получении списка пользователей: " + e.getMessage());
            e.printStackTrace();
        }
        if (status == 1) {
            return users;
        }
        else {
            if (users.isEmpty()) {
                return new ArrayList<>();
            }
            Random random = new Random();
            int randomIndex = random.nextInt(users.size());
            List<User> userList = new ArrayList<>();
            userList.add(users.get(randomIndex));
            return userList;
        }
    }
}