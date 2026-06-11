package controller;

import dal.NotificationDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "NotificationReadServlet", urlPatterns = {"/notifications/read-all", "/notifications/read-item", "/notifications/delete-item"})
public class NotificationReadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập\"}");
            return;
        }

        User account = (User) session.getAttribute("account");
        NotificationDAO notificationDAO = new NotificationDAO();
        String servletPath = request.getServletPath();
        String notificationIdRaw = request.getParameter("notificationId");
        if (notificationIdRaw != null && !notificationIdRaw.isBlank()) {
            try {
                long notificationId = Long.parseLong(notificationIdRaw);
                if ("/notifications/delete-item".equals(servletPath)) {
                    boolean deleted = notificationDAO.deleteNotification(notificationId, account.getUserId());
                    response.getWriter().write("{\"success\":" + deleted + "}");
                } else {
                    boolean updated = notificationDAO.markAsRead(notificationId, account.getUserId());
                    response.getWriter().write("{\"success\":" + updated + "}");
                }
            } catch (NumberFormatException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"notificationId không hợp lệ\"}");
            }
            return;
        }

        int updatedRows = notificationDAO.markAllAsRead(account.getUserId());
        response.getWriter().write("{\"success\":true,\"updated\":" + updatedRows + "}");
    }
}

