package controller;

import dal.DoctorDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Role;
import model.Status;
import model.User;
import util.AccountProvisionService;
import util.AccountProvisionService.ProvisionResult;
import util.AdminUserValidator;
import util.AdminUserValidator.ValidationResult;
import util.PendingResendStore;
import util.SystemLogService;

public class PatientAccountServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private static final String VIEW_PATH = "/pages/admin/patient-accounts.jsp";
    private static final String APP_PENDING_RESEND_KEY = "patientAccountPendingResendPasswordIds";
    private static final String FORM_FLASH_KEY = "patientAccountFormFlash";

    private final AccountProvisionService accountProvisionService = new AccountProvisionService();
    private final AdminUserValidator adminUserValidator = new AdminUserValidator();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User actor = (User) session.getAttribute("account");
        boolean isAdmin = actor.getRole() == Role.admin;
        boolean canManagePatients = actor.getRole() == Role.patient_manager;

        if (!isAdmin && !canManagePatients) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này");
            return;
        }

        request.setAttribute("canManagePatients", canManagePatients);
        request.setAttribute("patientAccountViewerRole", isAdmin ? "admin" : "patient_manager");

        String action = trim(request.getParameter("action"));

        try {
            switch (action) {
                case "add":
                    if (!requirePatientManager(canManagePatients, response)) {
                        return;
                    }
                    handleAddPatientAccount(request);
                    redirectWithFlashState(request, response);
                    return;
                case "edit":
                    if (!requirePatientManager(canManagePatients, response)) {
                        return;
                    }
                    handleEditPatientAccount(request);
                    redirectWithFlashState(request, response);
                    return;
                case "resendPassword":
                    if (!requirePatientManager(canManagePatients, response)) {
                        return;
                    }
                    handleResendPassword(request);
                    redirectWithFlashState(request, response);
                    return;
                case "toggleStatus":
                    if (!requirePatientManager(canManagePatients, response)) {
                        return;
                    }
                    handleToggleStatus(request);
                    redirectWithFlashState(request, response);
                    return;
                case "search":
                    loadPatients(request, request.getParameter("keyword"), request.getParameter("status"), "search");
                    forward(request, response);
                    return;
                case "filter":
                    loadPatients(request, request.getParameter("keyword"), request.getParameter("status"), "filter");
                    forward(request, response);
                    return;
                default:
                    loadPatients(request, "", "all", "list");
                    forward(request, response);
                    return;
            }
        } catch (SQLException e) {
            request.setAttribute("error", "Lỗi cơ sở dữ liệu: " + e.getMessage());
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            redirectWithFlashState(request, response);
            return;
        }

        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");

        if (status == null || status.isBlank()) {
            status = (String) request.getAttribute("filterStatus");
        }
        if (keyword == null) {
            keyword = (String) request.getAttribute("searchKeyword");
        }

        try {
            loadPatients(request, keyword, status, "list");
        } catch (SQLException e) {
            request.setAttribute("error", "Lỗi cơ sở dữ liệu: " + e.getMessage());
        }

        forward(request, response);
    }

    private boolean requirePatientManager(boolean canManagePatients, HttpServletResponse response) throws IOException {
        if (!canManagePatients) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ patient manager mới được thao tác tài khoản bệnh nhân");
            return false;
        }
        return true;
    }

    private void handleAddPatientAccount(HttpServletRequest request) throws SQLException {
        String fullName = trim(request.getParameter("fullname"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String requestedRole = trim(request.getParameter("role"));
        String roleStr = requestedRole.isEmpty() ? "patient" : requestedRole;

        UserDAO userDAO = new UserDAO();
        ValidationResult validationResult = adminUserValidator.validateAddUser(
                fullName, phone, email, roleStr,
                "", "", "", "",
                userDAO
        );

        if (!validationResult.isValid()) {
            keepAddForm(request, fullName, phone, email);
            applyValidationResult(request, validationResult);
            return;
        }

        try {
            Role targetRole = validationResult.getTargetRole();
            if (!isPatientManageableRole(targetRole)) {
                keepAddForm(request, fullName, phone, email);
                request.setAttribute("error", "Chỉ được tạo tài khoản bệnh nhân tại trang này");
                return;
            }

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setEmail(email);
            newUser.setRole(targetRole);
            newUser.setStatus(Status.active);

            ProvisionResult provisionResult = accountProvisionService.createAccountWithTemporaryPassword(newUser, userDAO);
            User createdUser = provisionResult.getUser();

            if (!provisionResult.isPasswordUpdated() || createdUser == null) {
                throw new SQLException(provisionResult.getErrorMessage());
            }

            ProvisionResult deliveryResult = accountProvisionService.sendTemporaryPassword(
                    createdUser,
                    provisionResult.getTemporaryPassword()
            );

            boolean mailFailed = !deliveryResult.isMailSent();

            if (mailFailed) {
                markPendingResend(request, createdUser.getUserId());
                request.setAttribute("success", "Tạo tài khoản bệnh nhân thành công nhưng gửi email thất bại. Vui lòng gửi lại email.");
            } else {
                clearPendingResend(request, createdUser.getUserId());
                request.setAttribute("success", "Tạo tài khoản bệnh nhân thành công. Mật khẩu tạm đã được gửi qua email.");
            }

            SystemLogService.logWithSession(request.getSession(false), "CREATE_PATIENT_ACCOUNT",
                    "Tạo tài khoản bệnh nhân: " + fullName + " (" + email + ")");
        } catch (Exception e) {
            keepAddForm(request, fullName, phone, email);
            request.setAttribute("error", "Lỗi khi tạo tài khoản bệnh nhân: " + e.getMessage());
        }
    }

    private void handleEditPatientAccount(HttpServletRequest request) throws SQLException {
        String userIdStr = trim(request.getParameter("userId"));
        String fullName = trim(request.getParameter("fullname"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String requestedRole = trim(request.getParameter("role"));
        String roleStr = requestedRole.isEmpty() ? "patient" : requestedRole;

        int userId = parsePositiveId(userIdStr);
        if (userId <= 0) {
            request.setAttribute("error", "Tài khoản bệnh nhân không hợp lệ");
            return;
        }

        UserDAO userDAO = new UserDAO();
        DoctorDAO doctorDAO = new DoctorDAO();
        User existingUser = userDAO.getUserById(userId);

        if (existingUser == null || existingUser.getRole() != Role.patient) {
            request.setAttribute("error", "Tài khoản bệnh nhân không hợp lệ");
            return;
        }

        ValidationResult validationResult = adminUserValidator.validateEditUser(
                existingUser, userId, fullName, phone, email, roleStr,
                "", "", "", "",
                userDAO, doctorDAO
        );

        if (!validationResult.isValid()) {
            keepEditForm(request, userIdStr, fullName, phone, email);
            applyValidationResult(request, validationResult);
            return;
        }

        Role targetRole = validationResult.getTargetRole();
        if (!isPatientManageableRole(targetRole)) {
            keepEditForm(request, userIdStr, fullName, phone, email);
            request.setAttribute("error", "Tài khoản nhân sự nội bộ được quản lý ở trang riêng");
            return;
        }

        User user = new User();
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        userDAO.updateUser(user);

        request.setAttribute("success", "Cập nhật tài khoản bệnh nhân thành công");
        request.setAttribute("editStatusValue", existingUser.getStatus().toString());

        SystemLogService.logWithSession(request.getSession(false), "UPDATE_PATIENT_ACCOUNT",
                "Cập nhật tài khoản bệnh nhân userId=" + userId + ", email=" + email);
    }

    private void handleToggleStatus(HttpServletRequest request) throws SQLException {
        int userId = parsePositiveId(request.getParameter("userId"));

        if (userId <= 0) {
            request.setAttribute("error", "Tài khoản bệnh nhân không hợp lệ");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        if (user == null || user.getRole() != Role.patient) {
            request.setAttribute("error", "Tài khoản bệnh nhân không hợp lệ");
            return;
        }

        userDAO.toggleUserStatusById(userId);
        request.setAttribute("success", "Cập nhật trạng thái của " + user.getFullName() + " thành công");

        SystemLogService.logWithSession(request.getSession(false), "TOGGLE_PATIENT_ACCOUNT_STATUS",
                "Thay đổi trạng thái tài khoản bệnh nhân userId=" + userId + ", email=" + user.getEmail());
    }

    private void handleResendPassword(HttpServletRequest request) throws SQLException {
        int userId = parsePositiveId(request.getParameter("userId"));

        if (userId <= 0) {
            request.setAttribute("error", "Tài khoản bệnh nhân không hợp lệ");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User targetUser = userDAO.getUserById(userId);

        if (targetUser == null || targetUser.getRole() != Role.patient) {
            request.setAttribute("error", "Tài khoản bệnh nhân không hợp lệ");
            return;
        }

        ProvisionResult provisionResult = accountProvisionService.resetTemporaryPassword(targetUser, userDAO);
        if (!provisionResult.isPasswordUpdated()) {
            request.setAttribute("error", "Không thể cập nhật mật khẩu tạm cho tài khoản bệnh nhân");
            return;
        }

        boolean mailFailed = !provisionResult.isMailSent();

        if (mailFailed) {
            markPendingResend(request, userId);
            request.setAttribute("success", "Đã tạo mật khẩu tạm mới nhưng gửi email thất bại. Vui lòng thử gửi lại.");
            request.setAttribute("resendModalOpen", true);
            request.setAttribute("resendModalUserId", targetUser.getUserId());
            request.setAttribute("resendModalFullName", targetUser.getFullName());
            request.setAttribute("resendModalPhone", targetUser.getPhone());
            request.setAttribute("resendModalEmail", targetUser.getEmail());
            request.setAttribute("resendModalStatus", targetUser.getStatus().toString());
        } else {
            clearPendingResend(request, userId);
            request.setAttribute("success", "Đã gửi lại mật khẩu tạm qua email cho " + targetUser.getFullName() + ".");
        }

        SystemLogService.logWithSession(request.getSession(false), "RESEND_PATIENT_ACCOUNT_PASSWORD",
                "Gửi lại mật khẩu tạm cho tài khoản bệnh nhân userId=" + userId + ", email=" + targetUser.getEmail()
                + ", mailStatus=" + (mailFailed ? "failed" : "success"));
    }

    private void loadPatients(HttpServletRequest request, String keyword, String statusStr, String currentAction) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String safeStatus = (statusStr == null || statusStr.isBlank()) ? "all" : statusStr;

        consumeFormFlash(request);

        UserDAO userDAO = new UserDAO();
        List<User> users = safeKeyword.isEmpty()
                ? userDAO.getUsersByRole(Role.patient)
                : userDAO.searchUsers(safeKeyword, Role.patient);

        users = filterByStatus(users, safeStatus);
        applyPaging(request, users);

        request.setAttribute("currentAction", currentAction);
        request.setAttribute("searchKeyword", safeKeyword);
        request.setAttribute("filterStatus", safeStatus);
    }

    private List<User> filterByStatus(List<User> users, String statusStr) {
        List<User> safeUsers = users != null ? users : new ArrayList<>();

        if ("all".equals(statusStr)) {
            return safeUsers;
        }

        List<User> filteredUsers = new ArrayList<>();
        try {
            Status status = Status.valueOf(statusStr);
            for (User user : safeUsers) {
                if (user != null && user.getStatus() == status) {
                    filteredUsers.add(user);
                }
            }
        } catch (Exception e) {
            return safeUsers;
        }

        return filteredUsers;
    }

    private void applyValidationResult(HttpServletRequest request, ValidationResult validationResult) {
        if (validationResult == null) {
            return;
        }

        if (validationResult.getFormError() != null) {
            request.setAttribute("error", validationResult.getFormError());
        }

        for (Map.Entry<String, String> entry : validationResult.getFieldErrors().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private boolean isPatientManageableRole(Role role) {
        return role == Role.patient;
    }

    private void keepAddForm(HttpServletRequest request, String fullName, String phone, String email) {
        request.setAttribute("addModalOpen", true);
        request.setAttribute("addFullName", fullName);
        request.setAttribute("addPhone", phone);
        request.setAttribute("addEmail", email);
    }

    private void keepEditForm(HttpServletRequest request, String userId, String fullName, String phone, String email) {
        request.setAttribute("editModalOpen", true);
        request.setAttribute("editUserId", userId);
        request.setAttribute("editFullName", fullName);
        request.setAttribute("editPhone", phone);
        request.setAttribute("editEmail", email);
        request.setAttribute("editResendAvailable", isPendingResend(request, parsePositiveId(userId)));
    }

    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(VIEW_PATH).forward(request, response);
    }

    private void redirectWithFlashState(HttpServletRequest request, HttpServletResponse response) throws IOException {
        flashCurrentFormState(request);
        response.sendRedirect(buildRedirectUrl(request));
    }

    private String buildRedirectUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/patient-accounts");
        appendQueryParam(url, "keyword", trim(request.getParameter("keyword")));
        appendQueryParam(url, "status", trim(request.getParameter("status")));
        appendQueryParam(url, "page", trim(request.getParameter("page")));
        return url.toString();
    }

    private void appendQueryParam(StringBuilder url, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append(url.indexOf("?") >= 0 ? "&" : "?")
                .append(key)
                .append("=")
                .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
    }

    private void flashCurrentFormState(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String, Object> flash = new LinkedHashMap<>();
        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (shouldFlashAttribute(name)) {
                flash.put(name, request.getAttribute(name));
            }
        }
        session.setAttribute(FORM_FLASH_KEY, flash);
    }

    private void consumeFormFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object flash = session.getAttribute(FORM_FLASH_KEY);
        if (!(flash instanceof Map<?, ?>)) {
            return;
        }
        Map<?, ?> flashMap = (Map<?, ?>) flash;
        for (Map.Entry<?, ?> entry : flashMap.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                request.setAttribute((String) key, entry.getValue());
            }
        }
        session.removeAttribute(FORM_FLASH_KEY);
    }

    private boolean shouldFlashAttribute(String name) {
        String key = trim(name);
        return key.equals("error")
                || key.equals("success")
                || key.equals("addModalOpen")
                || key.equals("editModalOpen")
                || key.equals("resendModalOpen")
                || key.equals("resendModalUserId")
                || key.equals("resendModalFullName")
                || key.equals("resendModalPhone")
                || key.equals("resendModalEmail")
                || key.equals("resendModalStatus")
                || key.equals("editResendAvailable")
                || key.startsWith("add")
                || key.startsWith("edit");
    }

    private int parsePage(String pageParam, int defaultValue) {
        try {
            int page = Integer.parseInt(pageParam);
            return page < 1 ? 1 : page;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int calculateTotalPages(int totalRecords, int pageSize) {
        if (totalRecords <= 0 || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    private <T> List<T> paginate(List<T> data, int page, int pageSize) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        int from = (page - 1) * pageSize;
        if (from < 0 || from >= data.size()) {
            return new ArrayList<>();
        }

        int to = Math.min(from + pageSize, data.size());
        return data.subList(from, to);
    }

    private void applyPaging(HttpServletRequest request, List<User> users) {
        List<User> safeUsers = users != null ? users : new ArrayList<>();
        int currentPage = parsePage(request.getParameter("page"), 1);
        int totalRecords = safeUsers.size();
        int totalPages = calculateTotalPages(totalRecords, PAGE_SIZE);

        if (totalPages == 0) {
            currentPage = 1;
        } else if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        request.setAttribute("users", paginate(safeUsers, currentPage, PAGE_SIZE));
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("pageSize", PAGE_SIZE);
        request.setAttribute("pendingResendMap", buildPendingResendMap(request, safeUsers));
    }

    private Map<Integer, Boolean> buildPendingResendMap(HttpServletRequest request, List<User> users) {
        Map<Integer, Boolean> map = new HashMap<>();
        Set<Integer> pendingIds = getPendingResendSet(request, false);

        if (users == null || users.isEmpty() || pendingIds == null || pendingIds.isEmpty()) {
            return map;
        }

        for (User user : users) {
            if (user != null) {
                map.put(user.getUserId(), pendingIds.contains(user.getUserId()));
            }
        }

        return map;
    }

    private Set<Integer> getPendingResendSet(HttpServletRequest request, boolean create) {
        return PendingResendStore.getSet(request.getServletContext(), APP_PENDING_RESEND_KEY, create);
    }

    private void markPendingResend(HttpServletRequest request, int userId) {
        Set<Integer> set = getPendingResendSet(request, true);
        if (set != null && userId > 0) {
            set.add(userId);
        }
    }

    private void clearPendingResend(HttpServletRequest request, int userId) {
        Set<Integer> set = getPendingResendSet(request, false);
        if (set != null) {
            set.remove(userId);
        }
    }

    private boolean isPendingResend(HttpServletRequest request, int userId) {
        if (userId <= 0) {
            return false;
        }
        Set<Integer> set = getPendingResendSet(request, false);
        return set != null && set.contains(userId);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private int parsePositiveId(String raw) {
        try {
            int id = Integer.parseInt(trim(raw));
            return id > 0 ? id : -1;
        } catch (Exception e) {
            return -1;
        }
    }

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

    @Override
    public String getServletInfo() {
        return "Patient Account Management Servlet";
    }
}

