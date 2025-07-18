package web;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/DataServlet")
public class DataServlet extends HttpServlet {

    private static final String DB_PATH = "jdbc:sqlite:C:\\Users\\Alperen Bey\\Desktop\\webhookDB\\webhok";
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String table = request.getParameter("table");
        String json = "[]";

        try {
            switch (table) {
                case "subscribers":
                    List<Subscriber> subscribers = getSubscribers();
                    json = gson.toJson(subscribers);
                    break;

                case "private_subscribers":
                    List<PrivateSubscriber> privates = getPrivateSubscribers();
                    json = gson.toJson(privates);
                    break;

                case "failed_messages":
                    List<FailedMessage> fails = getFailedMessages();
                    json = gson.toJson(fails);
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json = "{\"error\": \"Invalid table name.\"}";
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json = "{\"error\": \"Server error.\"}";
            e.printStackTrace();
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
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

}