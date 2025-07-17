package web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

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

            WebManagerDB.insertToSubscribers(subscriber);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Offset must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error while adding subscriber.");
        }
    }
}