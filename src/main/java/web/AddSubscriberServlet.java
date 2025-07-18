package web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@WebServlet("/AddSubscriberServlet")
public class AddSubscriberServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getParameter("url");
        String offsetStr = request.getParameter("offset");

        try {
            int offset = Integer.parseInt(offsetStr);

            Subscriber subscriber = new Subscriber();
            subscriber.setUrl(url);
            subscriber.setOffset(offset);

            insertToSubscribers(subscriber);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Offset must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error while adding subscriber.");
        }
    }

    public static void insertToSubscribers(Subscriber subscriber) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Alperen Bey\\Desktop\\webhookDB\\webhok");
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
}