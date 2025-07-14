package hook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManagerDB {

    private static final String DB_PATH = "jdbc:sqlite:C:\\Users\\Alperen Bey\\Desktop\\webhookDB\\webhok";

    // MAIN WORKER PART START
    public static void getUrlList(List<Subscriber> list) {
        list.clear();
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT url, last_offset FROM subscribers");
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(new Subscriber(rs.getString("url"), rs.getLong("last_offset")));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateOffset(String url, long offset) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE subscribers SET last_offset = ? WHERE url = ?")) {

                stmt.setLong(1, offset);
                stmt.setString(2, url);
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFromSubscribers(String url) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM subscribers WHERE url = ?")) {

                stmt.setString(1, url);
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // MAIN WORKER PART FINISHED

    // RETRY WORKER PART START
    public static List<FailedMessage> getAllFailedMessages() {
        List<FailedMessage> list = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM failed_messages ORDER BY id ASC");
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(new FailedMessage(
                            rs.getInt("id"),
                            rs.getString("url"),
                            rs.getString("message"),
                            rs.getLong("offset"),
                            rs.getInt("retry_count"),
                            rs.getString("last_attempt")
                    ));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void insertToFailedMessages(String url, String message, long offset) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO failed_messages (url, message, offset, retry_count, last_attempt) " +
                                 "VALUES (?, ?, ?, 0, CURRENT_TIMESTAMP)")) {

                stmt.setString(1, url);
                stmt.setString(2, message);
                stmt.setLong(3, offset);

                stmt.executeUpdate();
                System.out.println("Failed message inserted: " + url + " | offset=" + offset);

            }
        } catch (Exception e) {
            System.err.println("Failed to insert into failed_messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteFromFailedMessages(int id, String url, String mode) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM failed_messages WHERE id = ?")) {

                stmt.setInt(1, id);
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void incrementRetryCount(int id) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE failed_messages SET retry_count = retry_count + 1, last_attempt = CURRENT_TIMESTAMP WHERE id = ?")) {

                stmt.setInt(1, id);
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertPrivateSubscriber(Subscriber subscriber) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT OR IGNORE INTO private_subscribers (url, last_offset) VALUES (?, ?)")) {

                stmt.setString(1, subscriber.getUrl());
                stmt.setLong(2, subscriber.getOffset());
                stmt.executeUpdate();

                System.out.println("Private subscriber added: " + subscriber.getUrl() + " | offset=" + subscriber.getOffset());

            }
        } catch (Exception e) {
            System.err.println("Failed to insert private subscriber: " + e.getMessage());
        }
    }

    public static void privateUpdateOffset(String url, long offset) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE private_subscribers SET last_offset = ? WHERE url = ?")) {

                stmt.setLong(1, offset);
                stmt.setString(2, url);
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // PRIVATE WORKER PART FINISHED
}
