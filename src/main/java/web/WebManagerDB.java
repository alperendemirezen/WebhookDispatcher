package web;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebManagerDB {

    private static final String DB_PATH = "jdbc:sqlite:C:\\Users\\Alperen Bey\\Desktop\\webhookDB\\webhok";
    public static boolean validateAdmin(String username, String password) {
        boolean isValid = false;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM admins WHERE username = ? AND password = ?")) {

                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        isValid = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    public static List<Subscriber> getSubscribers() {
        List<Subscriber> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM subscribers");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Subscriber(rs.getInt("id"), rs.getString("url"), rs.getLong("last_offset")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<PrivateSubscriber> getPrivateSubscribers() {
        List<PrivateSubscriber> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM private_subscribers");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new PrivateSubscriber(rs.getInt("id"), rs.getString("url"), rs.getLong("last_offset")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<FailedMessage> getFailedMessages() {
        List<FailedMessage> messages = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM failed_messages");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                FailedMessage msg = new FailedMessage(
                        rs.getInt("id"),
                        rs.getString("url"),
                        rs.getLong("offset"),
                        rs.getString("message"),
                        rs.getInt("retry_count"),
                        rs.getString("last_attempt")
                );
                messages.add(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return messages;
    }

    public static void insertToSubscribers(Subscriber subscriber){
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO subscribers (url, last_offset) VALUES (?, ?)")) {

                stmt.setString(1, subscriber.getUrl());
                stmt.setLong(2, subscriber.getOffset());
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteSubscriberById(int id) {
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM subscribers WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


