package controller;

import dal.DoctorScheduleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import model.Role;
import model.ScheduleChangeRequest;
import model.User;
import util.SystemLogService;

public class AdminScheduleRequestServlet extends HttpServlet {

    private static final String VIEW_PATH = "/pages/admin/schedule-requests.jsp";
    private static final int PAGE_SIZE = 10;

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("account") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("account");
        if (user.getRole() != Role.admin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới được truy cập");
            return;
        }

        String action = trimOrEmpty(req.getParameter("action"));
        String statusFilter = normalizeStatusFilter(req.getParameter("status"));
        String requestTypeFilter = normalizeRequestTypeFilter(req.getParameter("requestType"));
        String actionTypeFilter = normalizeActionTypeFilter(req.getParameter("actionType"));
        String keyword = trimOrEmpty(req.getParameter("keyword"));
        int page = parseInt(req.getParameter("page"), 1);

        try {
            if ("review".equalsIgnoreCase(action)) {
                handleReview(req);
                resp.sendRedirect(req.getContextPath() + "/admin-schedule-requests"
                        + buildFilterQuery(statusFilter, requestTypeFilter, actionTypeFilter, keyword, page));
                return;
            }

            loadPage(req, resp);
        } catch (Exception e) {
            session.setAttribute("scheduleReviewError",
                    "Lỗi xử lý yêu cầu đổi lịch: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin-schedule-requests"
                    + buildFilterQuery(statusFilter, requestTypeFilter, actionTypeFilter, keyword, page));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void loadPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DoctorScheduleDAO requestDAO = new DoctorScheduleDAO();
        String statusFilter = normalizeStatusFilter(req.getParameter("status"));
        String requestTypeFilter = normalizeRequestTypeFilter(req.getParameter("requestType"));
        String actionTypeFilter = normalizeActionTypeFilter(req.getParameter("actionType"));
        String keyword = trimOrEmpty(req.getParameter("keyword"));

        List<ScheduleChangeRequest> requests = requestDAO.getScheduleChangeRequestsForAdmin(
                statusFilter, requestTypeFilter, actionTypeFilter, keyword
        );
        int pendingCount = requestDAO.countPendingScheduleChangeRequests();
        List<ScheduleChangeRequest> safeRequests = requests != null ? requests : List.of();
        int currentPage = parseInt(req.getParameter("page"), 1);
        int totalRecords = safeRequests.size();
        int totalPages = calculateTotalPages(totalRecords, PAGE_SIZE);

        if (totalPages == 0) {
            currentPage = 1;
        } else if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        req.setAttribute("statusFilter", statusFilter);
        req.setAttribute("requestTypeFilter", requestTypeFilter);
        req.setAttribute("actionTypeFilter", actionTypeFilter);
        req.setAttribute("keyword", keyword);
        req.setAttribute("requests", paginate(safeRequests, currentPage, PAGE_SIZE));
        req.setAttribute("pendingCount", pendingCount);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalRecords", totalRecords);
        req.setAttribute("pageSize", PAGE_SIZE);
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void handleReview(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        DoctorScheduleDAO requestDAO = new DoctorScheduleDAO();

        int requestId = parseInt(req.getParameter("requestId"), -1);
        String decision = normalizeDecision(req.getParameter("decision"));
        String adminNote = req.getParameter("adminNote");

        if (requestId <= 0 || decision.isEmpty()) {
            session.setAttribute("scheduleReviewError", "Dữ liệu duyệt đơn chưa hợp lệ.");
            return;
        }

        boolean reviewed = requestDAO.reviewScheduleChangeRequest(requestId, decision, adminNote);
        if (!reviewed) {
            String reviewError = requestDAO.getLastReviewError();
            if (reviewError == null || reviewError.isBlank()) {
                reviewError = "Không thể xử lý đơn. Đơn đã được duyệt trước đó";
            }
            session.setAttribute("scheduleReviewError", reviewError);
            return;
        }

        User admin = (User) session.getAttribute("account");
        String actionName = "APPROVED".equals(decision) ? "ADMIN_APPROVE_SCHEDULE_REQUEST" : "ADMIN_REJECT_SCHEDULE_REQUEST";
        String details = "Admin " + admin.getFullName() + " đã " + ("APPROVED".equals(decision) ? "duyệt" : "từ chối")
                + " đơn đổi lịch #" + requestId + ".";
        SystemLogService.logWithSession(session, actionName, details);

        String successMessage = "APPROVED".equals(decision)
                ? "Đã duyệt đơn thành công."
                : "Đã từ chối đơn thành công.";
        session.setAttribute("scheduleReviewSuccess", successMessage);
    }

    private int parseInt(String raw, int fallback) {
        try {
            if (raw == null || raw.isBlank()) {
                return fallback;
            }
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String normalizeDecision(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("APPROVED".equals(normalized) || "REJECTED".equals(normalized)) {
            return normalized;
        }
        return "";
    }

    private String normalizeStatusFilter(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("PENDING".equals(normalized) || "APPROVED".equals(normalized) || "REJECTED".equals(normalized)) {
            return normalized;
        }
        return "ALL";
    }

    private String normalizeRequestTypeFilter(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("TEMPORARY".equals(normalized) || "PERMANENT".equals(normalized)) {
            return normalized;
        }
        return "ALL";
    }

    private String normalizeActionTypeFilter(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("ADD".equals(normalized) || "UPDATE".equals(normalized) || "REMOVE".equals(normalized)) {
            return normalized;
        }
        return "ALL";
    }

    private int calculateTotalPages(int totalRecords, int pageSize) {
        if (totalRecords <= 0 || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    private <T> List<T> paginate(List<T> data, int page, int pageSize) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }

        int from = (page - 1) * pageSize;
        if (from < 0 || from >= data.size()) {
            return List.of();
        }

        int to = Math.min(from + pageSize, data.size());
        return data.subList(from, to);
    }

    private String buildFilterQuery(String statusFilter, String requestTypeFilter, String actionTypeFilter, String keyword, int page) {
        StringBuilder query = new StringBuilder("?status=").append(encode(statusFilter));
        query.append("&requestType=").append(encode(requestTypeFilter));
        query.append("&actionType=").append(encode(actionTypeFilter));
        query.append("&keyword=").append(encode(keyword));
        query.append("&page=").append(Math.max(page, 1));
        return query.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
