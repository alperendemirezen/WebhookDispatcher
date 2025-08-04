package hook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManagerDB {

    private static final String DB_PATH = "jdbc:sqlite:C:\\Users\\Alperen Bey\\Desktop\\webhookDB\\webhok";

    public static void getSubscribers(List<Subscriber> list, String topic) {
        list.clear();
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT url, last_offset, topic FROM subscribers WHERE topic = ?")) {

                stmt.setString(1, topic);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(new Subscriber(rs.getString("url"), rs.getLong("last_offset"), rs.getString("topic")));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getTopics(){
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT topic FROM subscribers");
                 ResultSet rs = stmt.executeQuery()) {

                ArrayList<String> topics = new ArrayList<>();
                while (rs.next()) {
                    if(!topics.contains(rs.getString("topic"))) {
                        topics.add(rs.getString("topic"));
                    }
                }
                return topics;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
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

    public static void deleteFromSubscribers(String url, String topic) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM subscribers WHERE url = ? AND topic = ?")) {

                stmt.setString(1, url);
                stmt.setString(2, topic);
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<FailedMessage> getAllFailedMessages(String topic) {
        List<FailedMessage> list = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM failed_messages WHERE topic= ? ORDER BY id ASC")) {
                stmt.setString(1, topic);
                ResultSet rs = stmt.executeQuery(); {

                    while (rs.next()) {
                        list.add(new FailedMessage(
                                rs.getInt("id"),
                                rs.getString("url"),
                                rs.getString("message"),
                                rs.getLong("offset"),
                                rs.getInt("retry_count"),
                                rs.getString("last_attempt"),
                                rs.getString("topic"
                                )));
                    }
            }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void insertToFailedMessages(String url, String message, long offset, String topic) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO failed_messages (url, message, offset, retry_count, last_attempt, topic) " +
                                 "VALUES (?, ?, ?, 0, CURRENT_TIMESTAMP, ?)")) {

                stmt.setString(1, url);
                stmt.setString(2, message);
                stmt.setLong(3, offset);
                stmt.setString(4, topic);

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
                         "INSERT INTO private_subscribers (url, last_offset, topic) VALUES (?, ?, ?)")) {

                stmt.setString(1, subscriber.getUrl());
                stmt.setLong(2, subscriber.getOffset());
                stmt.setString(3, subscriber.getTopic());
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

    public static void insertToSubscribers(Subscriber subscriber) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO subscribers (url, last_offset, topic) VALUES (?, ?, ?)")) {

                stmt.setString(1, subscriber.getUrl());
                stmt.setLong(2, subscriber.getOffset());
                stmt.setString(3, subscriber.getTopic());
                stmt.executeUpdate();

                System.out.println("Subscriber added: " + subscriber.getUrl() + " | offset=" + subscriber.getOffset());

            }
        } catch (Exception e) {
            System.err.println("Failed to insert subscriber: " + e.getMessage());
        }
    }

    public static void deleteFromPrivate(Subscriber subscriber) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM private_subscribers WHERE url = ?")) {

                stmt.setString(1, subscriber.getUrl());
                stmt.executeUpdate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Subscriber> getPrivateWorkersFromDB(String topic) {
        ArrayList<Subscriber> privateSubscribers = new ArrayList<>();

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement stmt = conn.prepareStatement("SELECT url, last_offset, topic FROM private_subscribers WHERE topic = ?")) {
                 stmt.setString(1, topic);
                 ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    privateSubscribers.add(new Subscriber(rs.getString("url"), rs.getLong("last_offset"), rs.getString("topic")));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return privateSubscribers;
    }
}
