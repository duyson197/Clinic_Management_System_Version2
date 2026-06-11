package controller;

import dal.DoctorDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import util.PagingHelper;
import util.SystemLogService;

public class AdminUserServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private static final String SUCCESS_FLASH_KEY = "adminUserSuccess";
    private static final String RESEND_USER_FLASH_KEY = "adminUserResendUserId";
    private static final String APP_PENDING_RESEND_KEY = "adminUserPendingResendPasswordIds";
    private static final String FORM_FLASH_KEY = "adminUserFormFlash";
    private final AccountProvisionService accountProvisionService = new AccountProvisionService();
    private final AdminUserValidator adminUserValidator = new AdminUserValidator();

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

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                handleAddUser(request, response);
            } else if ("edit".equals(action)) {
                handleEditUser(request, response);
            } else if ("resendPassword".equals(action)) {
                handleResendPassword(request, response);
            } else if ("toggleStatus".equals(action)) {
                handleToggleStatus(request, response);
            } else if ("search".equals(action)) {
                handleSearch(request, response);
            } else if ("filter".equals(action)) {
                handleFilter(request, response);
            } else {
                loadUsers(request, response);
            }
        } catch (SQLException e) {
            request.setAttribute("error", "Lỗi cơ sở dữ liệu: " + e.getMessage());
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                redirectWithFlashState(request, response);
                return;
            }
            request.getRequestDispatcher("pages/admin/users.jsp").forward(request, response);
        }
    }

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String fullName = trim(request.getParameter("fullname"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String roleStr = trim(request.getParameter("role"));

        UserDAO userDAO = new UserDAO();

        ValidationResult validationResult = adminUserValidator.validateAddUser(
                fullName, phone, email, roleStr, userDAO
        );

        if (!validationResult.isValid()) {
            keepAddForm(request, fullName, phone, email, roleStr);
            applyValidationResult(request, validationResult);
            redirectWithFlashState(request, response);
            return;
        }

        try {
            Role targetRole = validationResult.getTargetRole();
            if (!isInternalManageableRole(targetRole)) {
                keepAddForm(request, fullName, phone, email, roleStr);
                request.setAttribute("addRoleError", "Chỉ được tạo tài khoản nhân sự nội bộ tại trang này");
                request.setAttribute("error", "Chỉ được tạo tài khoản nhân sự nội bộ tại trang này");
                redirectWithFlashState(request, response);
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
            if (targetRole == Role.doctor) {
                if (createdUser == null || createdUser.getUserId() <= 0) {
                    throw new SQLException("Không tìm thấy tài khoản vừa tạo để cập nhật hồ sơ bác sĩ");
                }

                new DoctorDAO().syncDoctorRowsForAllDoctorUsers();
            }

            ProvisionResult deliveryResult = accountProvisionService.sendTemporaryPassword(
                    createdUser,
                    provisionResult.getTemporaryPassword()
            );
            boolean mailFailed = !deliveryResult.isMailSent();
            String successMessage = "Tạo tài khoản thành công. Mật khẩu tạm đã được gửi qua email.";

            if (mailFailed) {
                successMessage = "Tạo tài khoản thành công nhưng gửi email thất bại. Vui lòng gửi lại mật khẩu tạm cho người dùng.";
                if (createdUser != null && createdUser.getUserId() > 0) {
                    markPendingResend(request, createdUser.getUserId());
                    setFlashResendUserId(request, createdUser.getUserId());
                }
            } else {
                if (createdUser != null && createdUser.getUserId() > 0) {
                    clearPendingResend(request, createdUser.getUserId());
                }
                clearFlashResendUserId(request);
            }

            HttpSession session = request.getSession(false);
            SystemLogService.logWithSession(session, "CREATE_USER",
                    "Tạo tài khoản: " + fullName + " (" + email + "), role=" + targetRole.name());

            if (!mailFailed && createdUser != null && createdUser.getUserId() > 0) {
                redirectToStaffProfile(request, response, createdUser.getUserId(),
                        "Tạo tài khoản thành công. Hãy hoàn thiện hồ sơ nhân viên.");
                return;
            }

            redirectSuccess(request, response, successMessage);
            return;
        } catch (Exception e) {
            keepAddForm(request, fullName, phone, email, roleStr);
            request.setAttribute("error", "Lỗi khi tạo tài khoản: " + e.getMessage());
            request.setAttribute("addModalOpen", true);
        }

        redirectWithFlashState(request, response);
    }

    private void handleEditUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String userIdStr = trim(request.getParameter("userId"));
        String originalRole = trim(request.getParameter("originalRole"));
        String fullName = trim(request.getParameter("fullname"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String roleStr = trim(request.getParameter("role"));

        int userId = parsePositiveId(userIdStr);
        if (userId <= 0) {
            request.setAttribute("error", "Người dùng không hợp lệ");
            redirectWithFlashState(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();
        DoctorDAO doctorDAO = new DoctorDAO();

        User existingUser = userDAO.getUserById(userId);
        if (existingUser == null) {
            request.setAttribute("error", "Người dùng không hợp lệ");
            redirectWithFlashState(request, response);
            return;
        }

        if (existingUser.getRole() == Role.admin) {
            request.setAttribute("error", "Không được chỉnh sửa tài khoản admin");
            redirectWithFlashState(request, response);
            return;
        }

        if (existingUser.getRole() != Role.doctor && "doctor".equalsIgnoreCase(roleStr)) {
            redirectToStaffCompletion(request, response, userId, fullName, phone, email,
                    "Vui lòng bổ sung thông tin bác sĩ để hoàn tất thay đổi vai trò.");
            return;
        }

        ValidationResult validationResult = adminUserValidator.validateEditUser(
                existingUser, userId, fullName, phone, email, roleStr, userDAO, doctorDAO
        );

        if (!validationResult.isValid()) {
            String editRoleValue = validationResult.getTargetRole() != null
                    ? validationResult.getTargetRole().name()
                    : existingUser.getRole().name();

            keepEditForm(request, userIdStr, originalRole, fullName, phone, email, editRoleValue);
            applyValidationResult(request, validationResult);
            redirectWithFlashState(request, response);
            return;
        }

        try {
            Role targetRole = validationResult.getTargetRole();
            if (!isInternalManageableRole(targetRole)) {
                keepEditForm(request, userIdStr, originalRole, fullName, phone, email, roleStr);
                request.setAttribute("editRoleError", "Tài khoản bệnh nhân được quản lý ở trang riêng");
                request.setAttribute("error", "Tài khoản bệnh nhân được quản lý ở trang riêng");
                redirectWithFlashState(request, response);
                return;
            }
            User user = new User();
            user.setUserId(userId);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setEmail(email);

            userDAO.updateUser(user);

            if (targetRole != existingUser.getRole()) {
                userDAO.updateUserRole(userId, targetRole);
            }

            if (targetRole == Role.doctor || existingUser.getRole() == Role.doctor) {
                doctorDAO.syncDoctorRowsForAllDoctorUsers();
            }

            HttpSession session = request.getSession(false);
            SystemLogService.logWithSession(session, "UPDATE_USER",
                    "Cập nhật tài khoản userId=" + userId + ", fullName=" + fullName + ", role=" + targetRole.name());
            redirectSuccess(request, response, "Cập nhật tài khoản thành công");
            return;

        } catch (SQLException e) {
            keepEditForm(request, userIdStr, originalRole, fullName, phone, email, roleStr);
            request.setAttribute("error", "Lỗi khi cập nhật thông tin: " + e.getMessage());
            request.setAttribute("editModalOpen", true);
        } catch (Exception e) {
            keepEditForm(request, userIdStr, originalRole, fullName, phone, email, roleStr);
            request.setAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
            request.setAttribute("editModalOpen", true);
        }

        redirectWithFlashState(request, response);
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String userIdStr = trim(request.getParameter("userId"));
        int userId = parsePositiveId(userIdStr);

        if (userId <= 0) {
            request.setAttribute("error", "Người dùng không hợp lệ");
            redirectWithFlashState(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();

        try {
            User user = userDAO.getUserById(userId);

            if (user == null) {
                request.setAttribute("error", "Không tìm thấy người dùng");
                redirectWithFlashState(request, response);
                return;
            }

            if (user.getRole() == Role.admin) {
                request.setAttribute("error", "Không được thay đổi trạng thái tài khoản admin");
                redirectWithFlashState(request, response);
                return;
            }

            userDAO.toggleUserStatusById(userId);

            HttpSession session = request.getSession(false);
            SystemLogService.logWithSession(session, "TOGGLE_USER_STATUS",
                    "Thay đổi trạng thái tài khoản: " + user.getFullName() + " (" + user.getEmail() + ")");
            redirectSuccess(request, response, "Cập nhật trạng thái của " + user.getFullName() + " thành công");
            return;

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }

        redirectWithFlashState(request, response);
    }

    private void handleResendPassword(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String userIdStr = trim(request.getParameter("userId"));
        int userId = parsePositiveId(userIdStr);

        if (userId <= 0) {
            request.setAttribute("error", "Người dùng không hợp lệ");
            redirectWithFlashState(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User targetUser = userDAO.getUserById(userId);

        if (targetUser == null) {
            request.setAttribute("error", "Người dùng không hợp lệ");
            redirectWithFlashState(request, response);
            return;
        }

        if (targetUser.getRole() == Role.admin) {
            request.setAttribute("error", "Không hỗ trợ gửi lại mật khẩu cho tài khoản admin");
            redirectWithFlashState(request, response);
            return;
        }

        ProvisionResult provisionResult = accountProvisionService.resetTemporaryPassword(targetUser, userDAO);

        if (!provisionResult.isPasswordUpdated()) {
            request.setAttribute("error", "Người dùng không hợp lệ");
            redirectWithFlashState(request, response);
            return;
        }

        boolean mailFailed = !provisionResult.isMailSent();

        HttpSession session = request.getSession(false);
        SystemLogService.logWithSession(session, "RESEND_USER_PASSWORD",
                "Gửi lại mật khẩu tạm cho userId=" + userId + ", email=" + targetUser.getEmail()
                + ", mailStatus=" + (mailFailed ? "failed" : "success"));

        if (mailFailed) {
            markPendingResend(request, userId);
            redirectSuccess(request, response, "Đã đặt mật khẩu tạm mới nhưng gửi email thất bại. Vui lòng thử gửi lại.");
        } else {
            clearPendingResend(request, userId);
            redirectToStaffProfile(request, response, userId,
                    "Đã gửi lại mật khẩu tạm qua email cho " + targetUser.getFullName() + ".");
        }
    }

    private void loadUsers(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        UserDAO userDAO = new UserDAO();
        List<User> users = getUsersByRoleAndKeyword(userDAO, "all", "");

        applyPaging(request, users);
        exposeSuccess(request);
        exposeFlashResendUserId(request);
        consumeFormFlash(request);

        request.setAttribute("currentAction", "list");
        request.setAttribute("filterRole", "all");
        request.setAttribute("filterStatus", "all");
        request.setAttribute("searchKeyword", "");
        request.getRequestDispatcher("pages/admin/users.jsp").forward(request, response);
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String roleStr = request.getParameter("role");
        String statusStr = request.getParameter("status");

        if (keyword == null) {
            keyword = "";
        }
        if (roleStr == null || roleStr.isEmpty()) {
            roleStr = "all";
        }
        if (statusStr == null || statusStr.isEmpty()) {
            statusStr = "all";
        }

        UserDAO userDAO = new UserDAO();
        List<User> users = getUsersByRoleAndKeyword(userDAO, roleStr, keyword);
        users = filterByStatus(users, statusStr);

        applyPaging(request, users);
        exposeSuccess(request);
        exposeFlashResendUserId(request);

        request.setAttribute("currentAction", "search");
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("filterRole", roleStr);
        request.setAttribute("filterStatus", statusStr);
        request.getRequestDispatcher("pages/admin/users.jsp").forward(request, response);
    }

    private void handleFilter(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String statusStr = request.getParameter("status");
        String roleStr = request.getParameter("role");
        String keyword = request.getParameter("keyword");

        if (statusStr == null || statusStr.isEmpty()) {
            statusStr = "all";
        }
        if (roleStr == null || roleStr.isEmpty()) {
            roleStr = "all";
        }
        if (keyword == null) {
            keyword = "";
        }

        UserDAO userDAO = new UserDAO();
        List<User> users = getUsersByRoleAndKeyword(userDAO, roleStr, keyword);
        users = filterByStatus(users, statusStr);

        applyPaging(request, users);
        exposeSuccess(request);
        exposeFlashResendUserId(request);

        request.setAttribute("currentAction", "filter");
        request.setAttribute("filterRole", roleStr);
        request.setAttribute("filterStatus", statusStr);
        request.setAttribute("searchKeyword", keyword);
        request.getRequestDispatcher("pages/admin/users.jsp").forward(request, response);
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
        return "Admin User Management Servlet";
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
        int totalRecords = safeUsers.size();
        int requestedPage = PagingHelper.parsePage(request, "page", 1);
        PagingHelper.PagingMeta paging = PagingHelper.build(requestedPage, totalRecords, PAGE_SIZE, true);

        request.setAttribute("users", paginate(safeUsers, paging.getCurrentPage(), paging.getPageSize()));
        PagingHelper.expose(request, paging);
        request.setAttribute("pendingResendMap", buildPendingResendMap(request, safeUsers));
    }

    private List<User> getUsersByRoleAndKeyword(UserDAO userDAO, String roleStr, String keyword) {
        List<User> users = new ArrayList<>();
        String safeRole = roleStr != null ? roleStr : "all";
        String safeKeyword = keyword != null ? keyword.trim() : "";
        boolean hasKeyword = !safeKeyword.isEmpty();

        if ("all".equals(safeRole)) {
            users.addAll(getUsersByRoleWithKeyword(userDAO, Role.admin, safeKeyword, hasKeyword));
            users.addAll(getUsersByRoleWithKeyword(userDAO, Role.doctor, safeKeyword, hasKeyword));
            users.addAll(getUsersByRoleWithKeyword(userDAO, Role.receptionist, safeKeyword, hasKeyword));
            users.addAll(getUsersByRoleWithKeyword(userDAO, Role.technician, safeKeyword, hasKeyword));
            users.addAll(getUsersByRoleWithKeyword(userDAO, Role.patient_manager, safeKeyword, hasKeyword));
            return users;
        }

        try {
            Role role = Role.valueOf(safeRole);
            users.addAll(getUsersByRoleWithKeyword(userDAO, role, safeKeyword, hasKeyword));
        } catch (Exception e) {
            users.addAll(getUsersByRoleAndKeyword(userDAO, "all", safeKeyword));
        }

        return users;
    }

    private List<User> getUsersByRoleWithKeyword(UserDAO userDAO, Role role, String keyword, boolean hasKeyword) {
        List<User> data;
        if (hasKeyword) {
            data = userDAO.searchUsers(keyword, role);
        } else {
            data = userDAO.getUsersByRole(role);
        }
        return data != null ? data : new ArrayList<>();
    }

    private List<User> filterByStatus(List<User> users, String statusStr) {
        List<User> safeUsers = users != null ? users : new ArrayList<>();

        if (statusStr == null || "all".equals(statusStr)) {
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

    private void keepAddForm(HttpServletRequest request, String fullName, String phone, String email, String role) {
        request.setAttribute("addModalOpen", true);
        request.setAttribute("addRoleValue", role);
        request.setAttribute("addFullName", fullName);
        request.setAttribute("addPhone", phone);
        request.setAttribute("addEmail", email);
    }

    private void keepEditForm(HttpServletRequest request, String userId, String originalRole, String fullName,
            String phone, String email, String role) {
        request.setAttribute("editModalOpen", true);
        request.setAttribute("editOriginalRole", originalRole);
        request.setAttribute("editUserId", userId);
        request.setAttribute("editFullName", fullName);
        request.setAttribute("editPhone", phone);
        request.setAttribute("editEmail", email);
        request.setAttribute("editRoleValue", role);
        request.setAttribute("editResendAvailable", isPendingResend(request, parsePositiveId(userId)));
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

    private boolean isInternalManageableRole(Role role) {
        return role == Role.doctor
                || role == Role.receptionist
                || role == Role.technician
                || role == Role.patient_manager;
    }

    private void exposeSuccess(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Object flash = session.getAttribute(SUCCESS_FLASH_KEY);
        if (flash != null) {
            request.setAttribute("success", String.valueOf(flash));
            session.removeAttribute(SUCCESS_FLASH_KEY);
        }
    }

    private void exposeFlashResendUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Object flash = session.getAttribute(RESEND_USER_FLASH_KEY);
        if (flash != null) {
            request.setAttribute("flashResendUserId", flash);
            try {
                int userId = Integer.parseInt(String.valueOf(flash));
                User user = new UserDAO().getUserById(userId);
                if (user != null) {
                    request.setAttribute("flashResendFullName", user.getFullName());
                    request.setAttribute("flashResendPhone", user.getPhone());
                    request.setAttribute("flashResendEmail", user.getEmail());
                    request.setAttribute("flashResendRole", user.getRole() != null ? user.getRole().name() : "");
                }
            } catch (Exception ignored) {
            }
            session.removeAttribute(RESEND_USER_FLASH_KEY);
        }
    }

    private void redirectSuccess(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        request.getSession().setAttribute(SUCCESS_FLASH_KEY, message);
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void redirectWithFlashState(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        flashCurrentFormState(request);
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void redirectToStaffProfile(HttpServletRequest request, HttpServletResponse response, int userId, String message)
            throws IOException {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin-staffs?editUserId=" + userId + "&success=" + encodedMessage);
    }

    private void redirectToStaffCompletion(HttpServletRequest request, HttpServletResponse response, int userId,
            String fullName, String phone, String email, String message) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/admin-staffs?editUserId=").append(userId)
                .append("&prefillRole=doctor");
        appendQueryParam(url, "prefillFullName", fullName);
        appendQueryParam(url, "prefillPhone", phone);
        appendQueryParam(url, "prefillEmail", email);
        appendQueryParam(url, "notice", message);
        response.sendRedirect(url.toString());
    }

    private void appendQueryParam(StringBuilder url, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append("&")
                .append(key)
                .append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private void setFlashResendUserId(HttpServletRequest request, int userId) {
        if (userId > 0) {
            request.getSession().setAttribute(RESEND_USER_FLASH_KEY, userId);
        }
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
                || key.equals("editResendAvailable")
                || key.equals("flashResendUserId")
                || key.equals("flashResendFullName")
                || key.equals("flashResendPhone")
                || key.equals("flashResendEmail")
                || key.equals("flashResendRole")
                || key.startsWith("add")
                || key.startsWith("edit");
    }

    private void clearFlashResendUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(RESEND_USER_FLASH_KEY);
        }
    }
}
