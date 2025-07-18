package web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@WebServlet("/DeleteSubscriberServlet")
public class DeleteSubscriberServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idStr = request.getParameter("id");

        try {
            int id = Integer.parseInt(idStr);
            deleteSubscriberById(id);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting subscriber.");
        }
    }

    public static void deleteSubscriberById(int id) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Alperen Bey\\Desktop\\webhookDB\\webhok");
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM subscribers WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}