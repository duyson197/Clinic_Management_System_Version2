package controller;

import dal.SystemLogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.Role;
import model.SystemLog;
import model.User;
import util.PagingHelper;

@WebServlet(name = "AdminSystemLogServlet", urlPatterns = {"/admin-system-logs"})
public class AdminSystemLogServlet extends HttpServlet {

    private static final int PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("account");
        if (user.getRole() != Role.admin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới được truy cập");
            return;
        }

        String actionFilter = request.getParameter("actionFilter");
        String keyword = request.getParameter("keyword");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Timestamp from = null;
        Timestamp to = null;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (fromDateStr != null && !fromDateStr.isEmpty()) {
                LocalDate d = LocalDate.parse(fromDateStr, df);
                from = Timestamp.valueOf(d.atStartOfDay());
            }
            if (toDateStr != null && !toDateStr.isEmpty()) {
                LocalDate d = LocalDate.parse(toDateStr, df);
                LocalDateTime endOfDay = d.atTime(23, 59, 59);
                to = Timestamp.valueOf(endOfDay);
            }
        } catch (Exception e) {
            // ignore parse errors, keep null
        }

        SystemLogDAO dao = new SystemLogDAO();

        int totalLogs = dao.countLogs(actionFilter, keyword, from, to);
        int requestedPage = PagingHelper.parsePage(request, "page", 1);
        PagingHelper.PagingMeta paging = PagingHelper.build(requestedPage, totalLogs, PAGE_SIZE, true);

        List<SystemLog> logs = dao.getLogs(actionFilter, keyword, from, to, paging.getCurrentPage(), PAGE_SIZE);

        request.setAttribute("logs", logs);
        request.setAttribute("totalLogs", totalLogs);
        PagingHelper.expose(request, paging);

        request.setAttribute("actionFilter", actionFilter);
        request.setAttribute("keyword", keyword);
        request.setAttribute("fromDate", fromDateStr);
        request.setAttribute("toDate", toDateStr);

        request.getRequestDispatcher("pages/admin/system-logs.jsp").forward(request, response);
    }
}

