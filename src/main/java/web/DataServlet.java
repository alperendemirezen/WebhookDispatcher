package web;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/DataServlet")
public class DataServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String table = request.getParameter("table");
        String json = "[]";

        try {
            switch (table) {
                case "subscribers":
                    List<Subscriber> subscribers = WebManagerDB.getSubscribers();
                    json = gson.toJson(subscribers);
                    break;

                case "private_subscribers":
                    List<PrivateSubscriber> privates = WebManagerDB.getPrivateSubscribers();
                    json = gson.toJson(privates);
                    break;

                case "failed_messages":
                    List<FailedMessage> fails = WebManagerDB.getFailedMessages();
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
}