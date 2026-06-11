package controller;

import dal.DoctorDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Doctor;
import model.Role;
import model.Status;
import model.User;
import util.AccountProvisionService;
import util.AccountProvisionService.ProvisionResult;
import util.PendingResendStore;
import util.PagingHelper;
import util.SystemLogService;

public class AdminStaffServlet extends HttpServlet {

    private static final String VIEW_PATH = "/pages/admin/staffs.jsp";
    private static final String SUCCESS_FLASH_KEY = "adminStaffSuccess";
    private static final String RESEND_USER_FLASH_KEY = "adminStaffResendUserId";
    private static final String APP_PENDING_RESEND_KEY = "adminStaffPendingResendPasswordIds";
    private static final String FORM_FLASH_KEY = "adminStaffFormFlash";
    private static final int PAGE_SIZE = 10;
    private static final int MIN_EXPERIENCE = 0;
    private static final int MAX_EXPERIENCE = 50;
    private static final int MIN_PRICE = 0;
    private static final int MAX_PRICE = 10_000_000;
    private static final List<String> ROLE_OPTIONS = Arrays.asList(
            "doctor",
            "receptionist",
            "technician",
            "patient_manager"
    );
    private static final List<String> ACADEMIC_DEGREE_OPTIONS = Arrays.asList(
            "bachelor",
            "master",
            "doctorate"
    );
    private static final List<String> GENDER_OPTIONS = Arrays.asList(
            "male",
            "female",
            "other"
    );
    private static final List<String> ACADEMIC_TITLE_OPTIONS = Arrays.asList(
            "professor",
            "associate_professor"
    );
    private static final List<String> PROFESSIONAL_QUALIFICATION_OPTIONS = Arrays.asList(
            "resident_doctor",
            "specialist_level_1",
            "specialist_level_2"
    );
    private static final List<String> EXPERTISE_OPTIONS = Arrays.asList(
            "Da liễu tổng quát",
            "Da liễu dị ứng",
            "Da liễu nhiễm trùng",
            "Điều trị mụn"
    );

    private final AccountProvisionService accountProvisionService = new AccountProvisionService();

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("account");
        if (user.getRole() != Role.admin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chi admin moi duoc truy cap");
            return;
        }

        String action = trim(req.getParameter("action"));
        try {
            if ("add".equals(action) && "POST".equalsIgnoreCase(req.getMethod())) {
                if (handleAdd(req)) {
                    String successMessage = "Thêm nhân viên thành công.";
                    if (Boolean.TRUE.equals(req.getAttribute("addStaffMailFailed"))) {
                        successMessage = "Thêm nhân viên thành công nhưng gửi email thất bại. Vui lòng thử gửi lại.";
                    }
                    redirectSuccess(resp, req, successMessage);
                    return;
                }
                redirectWithFlashState(resp, req);
                return;
            } else if ("resendPassword".equals(action) && "POST".equalsIgnoreCase(req.getMethod())) {
                if (handleResendPassword(req)) {
                    redirectSuccess(resp, req, "Đã gửi lại mật khẩu tạm qua email.");
                    return;
                }
                redirectWithFlashState(resp, req);
                return;
            } else if ("edit".equals(action) && "POST".equalsIgnoreCase(req.getMethod())) {
                if (handleEdit(req)) {
                    redirectSuccess(resp, req, "Cập nhật nhân viên thành công.");
                    return;
                }
                redirectWithFlashState(resp, req);
                return;
            }

            loadPage(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Lỗi xử lý quản lý nhân viên: " + e.getMessage());
            if ("POST".equalsIgnoreCase(req.getMethod())) {
                redirectWithFlashState(resp, req);
                return;
            }
            loadPage(req, resp);
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
        DoctorDAO doctorDAO = new DoctorDAO();
        String keyword = resolveListValue(req, "keyword", "listKeyword");
        String role = resolveListValue(req, "role", "listRole");
        String academicDegree = resolveListValue(req, "qualification", "listQualification");
        int requestedPage = PagingHelper.parsePage(req, "listPage", PagingHelper.parsePage(req, "page", 1));
        String success = "";

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object flashMessage = session.getAttribute(SUCCESS_FLASH_KEY);
            if (flashMessage != null) {
                success = trim(String.valueOf(flashMessage));
                session.removeAttribute(SUCCESS_FLASH_KEY);
            }
        }
        if (success.isEmpty()) {
            success = trim(req.getParameter("success"));
        }
        String notice = trim(req.getParameter("notice"));

        List<Doctor> staffs = doctorDAO.getStaffsForAdmin(keyword, role, academicDegree);
        applyPaging(req, staffs, requestedPage);
        req.setAttribute("pendingResendMap", buildPendingResendMap(req, staffs));
        req.setAttribute("roleOptions", ROLE_OPTIONS);
        req.setAttribute("qualificationOptions", ACADEMIC_DEGREE_OPTIONS);
        req.setAttribute("genderOptions", GENDER_OPTIONS);
        req.setAttribute("academicTitleOptions", ACADEMIC_TITLE_OPTIONS);
        req.setAttribute("professionalQualificationOptions", PROFESSIONAL_QUALIFICATION_OPTIONS);
        req.setAttribute("expertiseOptions", EXPERTISE_OPTIONS);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedRole", role);
        req.setAttribute("selectedQualification", academicDegree);
        if (!success.isEmpty()) {
            req.setAttribute("success", success);
        }
        exposeFlashResendUserId(req);
        if (!notice.isEmpty()) {
            req.setAttribute("notice", notice);
        }
        consumeFormFlash(req);

        prepareEditModalFromQuery(req, doctorDAO);

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void applyPaging(HttpServletRequest req, List<Doctor> staffs, int requestedPage) {
        List<Doctor> safeStaffs = staffs != null ? staffs : List.of();
        PagingHelper.PagingMeta paging = PagingHelper.build(requestedPage, safeStaffs.size(), PAGE_SIZE, true);

        int fromIndex = paging.getOffset();
        int toIndex = Math.min(fromIndex + paging.getPageSize(), safeStaffs.size());
        List<Doctor> pagedStaffs = fromIndex >= safeStaffs.size() ? List.of() : safeStaffs.subList(fromIndex, toIndex);

        req.setAttribute("staffs", safeStaffs);
        req.setAttribute("staffsPaged", pagedStaffs);
        PagingHelper.expose(req, paging);
    }

    private boolean handleAdd(HttpServletRequest req) throws SQLException {
        String fullName = trim(req.getParameter("fullName"));
        String phone = trim(req.getParameter("phone"));
        String email = trim(req.getParameter("email"));
        String role = trim(req.getParameter("role"));
        String academicDegree = trim(req.getParameter("qualification"));
        String gender = trim(req.getParameter("gender"));
        String dobRaw = trim(req.getParameter("dob"));
        String specialization = normalizeSpecialization(trim(req.getParameter("specialization")));
        String academicTitle = trim(req.getParameter("academicTitle"));
        String professionalQualification = trim(req.getParameter("professionalQualification"));
        String experienceRaw = trim(req.getParameter("experienceYears"));
        String priceRaw = trim(req.getParameter("priceBooking"));

        keepAddForm(req, fullName, phone, email, role, academicDegree, gender, dobRaw,
                specialization, academicTitle, professionalQualification, experienceRaw, priceRaw);

        UserDAO userDAO = new UserDAO();
        boolean valid = validateCommonFields(req, fullName, phone, email, role, academicDegree, gender, dobRaw, true);
        valid = validateDoctorOnlyFields(req, role, academicDegree, specialization, academicTitle,
                professionalQualification, experienceRaw, priceRaw, true) && valid;

        if (valid && userDAO.isPhoneExist(phone)) {
            req.setAttribute("addPhoneError", "Số điện thoại đã tồn tại");
            valid = false;
        }
        if (valid && userDAO.isEmailExist(email)) {
            req.setAttribute("addEmailError", "Email đã tồn tại");
            valid = false;
        }

        if (!valid) {
            req.setAttribute("error", "Dữ liệu thêm nhân viên không hợp lệ");
            req.setAttribute("addModalOpen", true);
            return false;
        }

        Date dob = parseSqlDate(dobRaw);
        int experienceYears = isDoctorRole(role) ? Integer.parseInt(experienceRaw) : 0;
        int priceBooking = isDoctorRole(role) ? Integer.parseInt(priceRaw) : 0;

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setEmail(email);
        newUser.setRole(Role.valueOf(role));
        newUser.setStatus(Status.active);

        ProvisionResult provisionResult = accountProvisionService.createAccountWithTemporaryPassword(newUser, userDAO);
        User createdUser = provisionResult.getUser();
        if (!provisionResult.isPasswordUpdated() || createdUser == null || createdUser.getUserId() <= 0) {
            req.setAttribute("error", "Không thể thêm nhân viên");
            req.setAttribute("addModalOpen", true);
            return false;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        if (isDoctorRole(role)) {
            doctorDAO.upsertDoctorProfileByUserId(
                    createdUser.getUserId(),
                    academicDegree,
                    dob,
                    gender,
                    specialization,
                    experienceYears,
                    academicTitle,
                    professionalQualification,
                    priceBooking
            );
        } else {
            doctorDAO.upsertStaffProfileByUserId(createdUser.getUserId(), academicDegree, dob, gender);
        }

        ProvisionResult deliveryResult = accountProvisionService.sendTemporaryPassword(
                createdUser,
                provisionResult.getTemporaryPassword()
        );
        if (!deliveryResult.isMailSent()) {
            markPendingResend(req, createdUser.getUserId());
            setFlashResendUserId(req, createdUser.getUserId());
            req.setAttribute("success", "Thêm nhân viên thành công nhưng gửi email thất bại. Vui lòng thử gửi lại.");
            req.setAttribute("addStaffMailFailed", true);
        } else {
            clearPendingResend(req, createdUser.getUserId());
            clearFlashResendUserId(req);
        }

        HttpSession sessionLog = req.getSession(false);
        User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
        SystemLogService.log(userLog != null ? userLog.getUserId() : null, "STAFF_ADDED",
                "Them nhan vien: fullName=" + fullName + ", email=" + email + ", role=" + role);
        return true;
    }

    private boolean handleEdit(HttpServletRequest req) throws SQLException {
        String userIdRaw = trim(req.getParameter("userId"));
        String fullName = trim(req.getParameter("fullName"));
        String phone = trim(req.getParameter("phone"));
        String email = trim(req.getParameter("email"));
        String role = trim(req.getParameter("role"));
        String academicDegree = trim(req.getParameter("qualification"));
        String gender = trim(req.getParameter("gender"));
        String dobRaw = trim(req.getParameter("dob"));
        String specialization = normalizeSpecialization(trim(req.getParameter("specialization")));
        String academicTitle = trim(req.getParameter("academicTitle"));
        String professionalQualification = trim(req.getParameter("professionalQualification"));
        String experienceRaw = trim(req.getParameter("experienceYears"));
        String priceRaw = trim(req.getParameter("priceBooking"));

        keepEditForm(req, userIdRaw, fullName, phone, email, role, academicDegree, gender, dobRaw,
                specialization, academicTitle, professionalQualification, experienceRaw, priceRaw);

        int userId = parsePositiveInt(userIdRaw);
        if (userId <= 0) {
            req.setAttribute("error", "Nhân viên không hợp lệ");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        Doctor existing = doctorDAO.getStaffByUserIdForAdmin(userId);
        keepEditReadonlyFields(req, existing);
        keepEditOriginalFields(req, existing);
        if (existing == null) {
            req.setAttribute("error", "Nhân viên không tồn tại");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        boolean valid = validateCommonFields(req, fullName, phone, email, role, academicDegree, gender, dobRaw, false);
        valid = validateDoctorOnlyFields(req, role, academicDegree, specialization, academicTitle,
                professionalQualification, experienceRaw, priceRaw, false) && valid;
        String existingRole = trim(existing.getRole());
        if (valid && !isAllowedEditableRoleTransition(existingRole, role)) {
            req.setAttribute("editRoleError", "Chỉ được đổi vai trò trong nhóm nhân viên");
            valid = false;
        }

        UserDAO userDAO = new UserDAO();
        User phoneOwner = userDAO.getUserByPhone(phone);
        if (valid && phoneOwner != null && phoneOwner.getUserId() != existing.getUserId()) {
            req.setAttribute("editPhoneError", "Số điện thoại đã tồn tại");
            valid = false;
        }
        User emailOwner = userDAO.getUserByEmail(email);
        if (valid && emailOwner != null && emailOwner.getUserId() != existing.getUserId()) {
            req.setAttribute("editEmailError", "Email đã tồn tại");
            valid = false;
        }

        if (!valid) {
            req.setAttribute("error", "Dữ liệu cập nhật nhân viên không hợp lệ");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        Date dob = parseSqlDate(dobRaw);
        int experienceYears = isDoctorRole(role) ? Integer.parseInt(experienceRaw) : 0;
        int priceBooking = isDoctorRole(role) ? Integer.parseInt(priceRaw) : 0;

        User user = new User();
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        userDAO.updateUser(user);

        if (!existingRole.equals(role)) {
            userDAO.updateUserRole(userId, Role.valueOf(role));
        }

        if (isDoctorRole(role)) {
            doctorDAO.upsertDoctorProfileByUserId(
                    userId,
                    academicDegree,
                    dob,
                    gender,
                    specialization,
                    experienceYears,
                    academicTitle,
                    professionalQualification,
                    priceBooking
            );
        } else {
            doctorDAO.upsertStaffProfileByUserId(userId, academicDegree, dob, gender);
        }

        HttpSession sessionLog = req.getSession(false);
        User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
        SystemLogService.log(userLog != null ? userLog.getUserId() : null, "STAFF_UPDATED",
                "Cap nhat nhan vien: userId=" + userId + ", fullName=" + fullName + ", role=" + role);
        return true;
    }

    private boolean handleResendPassword(HttpServletRequest req) throws SQLException {
        int userId = parsePositiveInt(req.getParameter("userId"));
        if (userId <= 0) {
            req.setAttribute("error", "Nhân viên không hợp lệ");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        Doctor staff = doctorDAO.getStaffByUserIdForAdmin(userId);
        if (staff == null) {
            req.setAttribute("error", "Nhân viên không tồn tại");
            req.setAttribute("editModalOpen", true);
            return false;
        }
        if (false && !isDoctorRole(staff.getRole())) {
            req.setAttribute("error", "Chỉ bác sĩ mới hỗ trợ gửi lại mật khẩu ở trang này");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        keepEditForm(req, String.valueOf(userId), staff.getFullName(), staff.getPhone(), staff.getEmail(),
                staff.getRole(), staff.getAcademicDegree(), staff.getGender(), formatDate(staff.getDob()),
                staff.getSpecialization(), staff.getAcademicTitle(), staff.getProfessionalQualification(),
                String.valueOf(staff.getExperience_years()), String.valueOf((int) staff.getPrice()));
        keepEditReadonlyFields(req, staff);

        UserDAO userDAO = new UserDAO();
        User targetUser = userDAO.getUserById(userId);
        if (targetUser == null) {
            req.setAttribute("error", "Không tìm thấy tài khoản người dùng của bác sĩ");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        ProvisionResult provisionResult = accountProvisionService.resetTemporaryPassword(targetUser, userDAO);
        if (!provisionResult.isPasswordUpdated()) {
            req.setAttribute("error", "Không thể cập nhật mật khẩu tạm cho bác sĩ");
            req.setAttribute("editModalOpen", true);
            return false;
        }

        if (!provisionResult.isMailSent()) {
            markPendingResend(req, targetUser.getUserId());
            req.setAttribute("success", "Đã tạo mật khẩu tạm mới nhưng gửi email thất bại. Vui lòng thử gửi lại.");
            req.setAttribute("editModalOpen", true);
            req.setAttribute("editResendAvailable", true);
            return false;
        }

        clearPendingResend(req, targetUser.getUserId());
        clearFlashResendUserId(req);
        HttpSession sessionLog = req.getSession(false);
        User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
        SystemLogService.log(userLog != null ? userLog.getUserId() : null, "STAFF_RESEND_PASSWORD",
                "Gui lai mat khau tam cho nhan vien: userId=" + userId + ", email=" + staff.getEmail());
        return true;
    }

    private boolean validateCommonFields(HttpServletRequest req, String fullName, String phone, String email,
            String role, String academicDegree, String gender, String dobRaw, boolean isAdd) {
        boolean valid = true;
        String prefix = isAdd ? "add" : "edit";

        if (fullName.isEmpty()) {
            req.setAttribute(prefix + "FullNameError", "Họ tên không được để trống");
            valid = false;
        } else if (fullName.length() < 2 || fullName.length() > 100) {
            req.setAttribute(prefix + "FullNameError", "Họ tên phải từ 2 đến 100 ký tự");
            valid = false;
        }
        if (phone.isEmpty()) {
            req.setAttribute(prefix + "PhoneError", "Số điện thoại không được để trống");
            valid = false;
        } else if (!isValidPhone(phone)) {
            req.setAttribute(prefix + "PhoneError", "Số điện thoại phải gồm 10 số và bắt đầu bằng 0");
            valid = false;
        }
        if (email.isEmpty()) {
            req.setAttribute(prefix + "EmailError", "Email không được để trống");
            valid = false;
        } else if (!isValidEmail(email)) {
            req.setAttribute(prefix + "EmailError", "Email không đúng định dạng");
            valid = false;
        }
        if (role.isEmpty()) {
            req.setAttribute(prefix + "RoleError", "Vai trò là bắt buộc");
            valid = false;
        } else if (!ROLE_OPTIONS.contains(role)) {
            req.setAttribute(prefix + "RoleError", "Vai trò không hợp lệ");
            valid = false;
        }
        if (academicDegree.isEmpty()) {
            req.setAttribute(prefix + "QualificationError", "Bằng cấp là bắt buộc");
            valid = false;
        } else if (!ACADEMIC_DEGREE_OPTIONS.contains(academicDegree)) {
            req.setAttribute(prefix + "QualificationError", "Bằng cấp không hợp lệ");
            valid = false;
        }
        if (gender.isEmpty()) {
            req.setAttribute(prefix + "GenderError", "Giới tính là bắt buộc");
            valid = false;
        } else if (!GENDER_OPTIONS.contains(gender)) {
            req.setAttribute(prefix + "GenderError", "Giới tính không hợp lệ");
            valid = false;
        }
        if (dobRaw.isEmpty()) {
            req.setAttribute(prefix + "DobError", "Ngày sinh là bắt buộc");
            valid = false;
        } else if (!isValidAdultDate(dobRaw)) {
            req.setAttribute(prefix + "DobError", "Ngày sinh không hợp lệ, chỉ nhận nhân viên từ 18 tuổi");
            valid = false;
        }

        return valid;
    }

    private boolean validateDoctorOnlyFields(HttpServletRequest req, String role, String academicDegree, String specialization,
            String academicTitle, String professionalQualification, String experienceRaw, String priceRaw,
            boolean isAdd) {
        if (!isDoctorRole(role)) {
            return true;
        }

        String prefix = isAdd ? "add" : "edit";
        boolean valid = true;
        if (specialization.isEmpty()) {
            req.setAttribute(prefix + "SpecializationError", "Chuyên khoa là bắt buộc");
            valid = false;
        } else if (!EXPERTISE_OPTIONS.contains(specialization)) {
            req.setAttribute(prefix + "SpecializationError", "Chuyên môn không hợp lệ");
            valid = false;
        }
        if (!academicTitle.isEmpty() && !ACADEMIC_TITLE_OPTIONS.contains(academicTitle)) {
            req.setAttribute(prefix + "AcademicTitleError", "Học hàm không hợp lệ");
            valid = false;
        }
        if (!academicTitle.isEmpty() && !"doctorate".equals(academicDegree)) {
            req.setAttribute(prefix + "AcademicTitleError", "Bác sĩ có học hàm bắt buộc phải có bằng tiến sĩ");
            valid = false;
        }
        if (!professionalQualification.isEmpty() && !PROFESSIONAL_QUALIFICATION_OPTIONS.contains(professionalQualification)) {
            req.setAttribute(prefix + "ProfessionalQualificationError", "Trình độ hành nghề không hợp lệ");
            valid = false;
        }
        if ("bachelor".equals(academicDegree) && professionalQualification.isEmpty()) {
            req.setAttribute(prefix + "ProfessionalQualificationError", "Bác sĩ có bằng cấp cử nhân bắt buộc phải có trình độ hành nghề");
            valid = false;
        }
        if (experienceRaw.isEmpty()) {
            req.setAttribute(prefix + "ExperienceError", "Kinh nghiệm là bắt buộc");
            valid = false;
        } else if (!experienceRaw.matches("\\d+")) {
            req.setAttribute(prefix + "ExperienceError", "Kinh nghiệm phải là số nguyên");
            valid = false;
        } else {
            int experience = Integer.parseInt(experienceRaw);
            if (experience < MIN_EXPERIENCE || experience > MAX_EXPERIENCE) {
                req.setAttribute(prefix + "ExperienceError",
                        "Kinh nghiệm phải từ " + MIN_EXPERIENCE + " đến " + MAX_EXPERIENCE);
                valid = false;
            }
        }
        if (priceRaw.isEmpty()) {
            req.setAttribute(prefix + "PriceError", "Giá khám là bắt buộc");
            valid = false;
        } else if (!priceRaw.matches("\\d+")) {
            req.setAttribute(prefix + "PriceError", "Giá khám phải là số nguyên không âm");
            valid = false;
        } else {
            int price = Integer.parseInt(priceRaw);
            if (price < MIN_PRICE || price > MAX_PRICE) {
                req.setAttribute(prefix + "PriceError", "Giá khám phải từ " + MIN_PRICE + " đến " + MAX_PRICE);
                valid = false;
            }
        }
        return valid;
    }

    private void keepAddForm(HttpServletRequest req, String fullName, String phone, String email,
            String role, String academicDegree, String gender, String dobRaw, String specialization,
            String academicTitle, String professionalQualification, String experienceRaw, String priceRaw) {
        req.setAttribute("addModalOpen", true);
        req.setAttribute("addFullName", fullName);
        req.setAttribute("addPhone", phone);
        req.setAttribute("addEmail", email);
        req.setAttribute("addRole", role);
        req.setAttribute("addQualification", academicDegree);
        req.setAttribute("addGender", gender);
        req.setAttribute("addDob", dobRaw);
        req.setAttribute("addSpecialization", specialization);
        req.setAttribute("addAcademicTitle", academicTitle);
        req.setAttribute("addProfessionalQualification", professionalQualification);
        req.setAttribute("addExperience", experienceRaw);
        req.setAttribute("addPrice", priceRaw);
    }

    private void keepEditForm(HttpServletRequest req, String userIdRaw, String fullName, String phone, String email,
            String role, String academicDegree, String gender, String dobRaw, String specialization,
            String academicTitle, String professionalQualification, String experienceRaw, String priceRaw) {
        req.setAttribute("editModalOpen", true);
        req.setAttribute("editUserId", userIdRaw);
        req.setAttribute("editFullName", fullName);
        req.setAttribute("editPhone", phone);
        req.setAttribute("editEmail", email);
        req.setAttribute("editRole", role);
        req.setAttribute("editQualification", academicDegree);
        req.setAttribute("editGender", gender);
        req.setAttribute("editDob", dobRaw);
        req.setAttribute("editSpecialization", specialization);
        req.setAttribute("editAcademicTitle", academicTitle);
        req.setAttribute("editProfessionalQualification", professionalQualification);
        req.setAttribute("editExperience", experienceRaw);
        req.setAttribute("editPrice", priceRaw);
        req.setAttribute("editResendAvailable", false);
    }

    private void keepEditReadonlyFields(HttpServletRequest req, Doctor staff) {
        if (staff == null) {
            return;
        }
        req.setAttribute("editStatus", staff.getStatus());
        req.setAttribute("editRating", staff.getRating());
        req.setAttribute("editResendAvailable", isPendingResend(req, staff.getUserId()));
    }

    private void exposeFlashResendUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object flash = session.getAttribute(RESEND_USER_FLASH_KEY);
        if (flash != null) {
            req.setAttribute("flashResendUserId", flash);
            session.removeAttribute(RESEND_USER_FLASH_KEY);
        }
    }

    private void keepEditOriginalFields(HttpServletRequest req, Doctor staff) {
        if (staff == null) {
            return;
        }
        req.setAttribute("editOriginalFullName", staff.getFullName());
        req.setAttribute("editOriginalPhone", staff.getPhone());
        req.setAttribute("editOriginalEmail", staff.getEmail());
        req.setAttribute("editOriginalRole", staff.getRole());
        req.setAttribute("editOriginalQualification", staff.getAcademicDegree());
        req.setAttribute("editOriginalGender", staff.getGender());
        req.setAttribute("editOriginalDob", formatDate(staff.getDob()));
        req.setAttribute("editOriginalSpecialization", staff.getSpecialization());
        req.setAttribute("editOriginalAcademicTitle", staff.getAcademicTitle());
        req.setAttribute("editOriginalProfessionalQualification", staff.getProfessionalQualification());
        req.setAttribute("editOriginalExperience", staff.getExperience_years());
        req.setAttribute("editOriginalPrice", staff.getPrice());
    }

    private void prepareEditModalFromQuery(HttpServletRequest req, DoctorDAO doctorDAO) {
        if (Boolean.TRUE.equals(req.getAttribute("editModalOpen")) || !"GET".equalsIgnoreCase(req.getMethod())) {
            return;
        }

        int editUserId = parsePositiveInt(req.getParameter("editUserId"));
        if (editUserId <= 0) {
            Object flashResendUserId = req.getAttribute("flashResendUserId");
            editUserId = parsePositiveInt(flashResendUserId != null ? String.valueOf(flashResendUserId) : "");
        }
        if (editUserId <= 0) {
            return;
        }

        Doctor staff = doctorDAO.getStaffByUserIdForAdmin(editUserId);
        if (staff == null) {
            return;
        }

        String prefillFullName = trim(req.getParameter("prefillFullName"));
        String prefillPhone = trim(req.getParameter("prefillPhone"));
        String prefillEmail = trim(req.getParameter("prefillEmail"));
        String prefillRole = trim(req.getParameter("prefillRole"));

        keepEditForm(req, String.valueOf(staff.getUserId()),
                prefillFullName.isEmpty() ? staff.getFullName() : prefillFullName,
                prefillPhone.isEmpty() ? staff.getPhone() : prefillPhone,
                prefillEmail.isEmpty() ? staff.getEmail() : prefillEmail,
                prefillRole.isEmpty() ? staff.getRole() : prefillRole,
                staff.getAcademicDegree(), staff.getGender(), formatDate(staff.getDob()),
                staff.getSpecialization(), staff.getAcademicTitle(), staff.getProfessionalQualification(),
                String.valueOf(staff.getExperience_years()), String.valueOf((int) staff.getPrice()));
        keepEditReadonlyFields(req, staff);
        keepEditOriginalFields(req, staff);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^0\\d{9}$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String normalizeSpecialization(String specialization) {
        String value = trim(specialization);
        if (value.isEmpty()) {
            return value;
        }

        switch (value) {
            case "Da liễu tổng quát":
                return "Da liễu tổng quát";
            case "Da liễu dị ứng":
                return "Da liễu dị ứng";
            case "Da liễu nhiễm trùng":
                return "Da liễu nhiễm trùng";
            case "Điều trị mụn":
                return "Điều trị mụn";
            default:
                return value;
        }
    }

    private boolean isValidAdultDate(String dateValue) {
        try {
            LocalDate date = LocalDate.parse(dateValue);
            LocalDate today = LocalDate.now();
            return !date.isAfter(today) && !date.plusYears(18).isAfter(today);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private Date parseSqlDate(String dateValue) {
        return Date.valueOf(LocalDate.parse(dateValue));
    }

    private String formatDate(Date date) {
        return date == null ? "" : date.toLocalDate().toString();
    }

    private boolean isDoctorRole(String role) {
        return "doctor".equals(trim(role));
    }

    private boolean isAllowedEditableRoleTransition(String currentRole, String targetRole) {
        String safeCurrentRole = trim(currentRole);
        String safeTargetRole = trim(targetRole);
        return ROLE_OPTIONS.contains(safeCurrentRole)
                && ROLE_OPTIONS.contains(safeTargetRole);
    }

    private String resolveListValue(HttpServletRequest req, String getParamName, String postParamName) {
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            return trim(req.getParameter(postParamName));
        }
        return trim(req.getParameter(getParamName));
    }

    private void redirectSuccess(HttpServletResponse resp, HttpServletRequest req, String message) throws IOException {
        req.getSession().setAttribute(SUCCESS_FLASH_KEY, message);
        resp.sendRedirect(req.getContextPath() + "/admin-staffs");
    }

    private void redirectWithFlashState(HttpServletResponse resp, HttpServletRequest req) throws IOException {
        flashCurrentFormState(req);
        resp.sendRedirect(req.getContextPath() + "/admin-staffs");
    }

    private void flashCurrentFormState(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Map<String, Object> flash = new LinkedHashMap<>();
        Enumeration<String> attributeNames = req.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (shouldFlashAttribute(name)) {
                flash.put(name, req.getAttribute(name));
            }
        }
        session.setAttribute(FORM_FLASH_KEY, flash);
    }

    private void consumeFormFlash(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
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
                req.setAttribute((String) key, entry.getValue());
            }
        }
        session.removeAttribute(FORM_FLASH_KEY);
    }

    private boolean shouldFlashAttribute(String name) {
        String key = trim(name);
        return key.equals("error")
                || key.equals("notice")
                || key.equals("success")
                || key.equals("addModalOpen")
                || key.equals("editModalOpen")
                || key.equals("editResendAvailable")
                || key.equals("flashResendUserId")
                || key.equals("addStaffMailFailed")
                || key.startsWith("add")
                || key.startsWith("edit");
    }

    private int parsePositiveInt(String value) {
        try {
            int id = Integer.parseInt(trim(value));
            return id > 0 ? id : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private Map<Integer, Boolean> buildPendingResendMap(HttpServletRequest req, List<Doctor> staffs) {
        Map<Integer, Boolean> map = new HashMap<>();
        Set<Integer> pendingIds = getPendingResendSet(req, false);
        if (staffs == null || pendingIds == null || pendingIds.isEmpty()) {
            return map;
        }
        for (Doctor staff : staffs) {
            if (staff != null) {
                map.put(staff.getUserId(), pendingIds.contains(staff.getUserId()));
            }
        }
        return map;
    }

    private void markPendingResend(HttpServletRequest req, int userId) {
        if (userId <= 0) {
            return;
        }
        Set<Integer> pendingIds = getPendingResendSet(req, true);
        if (pendingIds != null) {
            pendingIds.add(userId);
        }
    }

    private void clearPendingResend(HttpServletRequest req, int userId) {
        if (userId <= 0) {
            return;
        }
        Set<Integer> pendingIds = getPendingResendSet(req, false);
        if (pendingIds != null) {
            pendingIds.remove(userId);
        }
    }

    private boolean isPendingResend(HttpServletRequest req, int userId) {
        Set<Integer> pendingIds = getPendingResendSet(req, false);
        return pendingIds != null && pendingIds.contains(userId);
    }

    private void setFlashResendUserId(HttpServletRequest req, int userId) {
        if (userId > 0) {
            req.getSession().setAttribute(RESEND_USER_FLASH_KEY, userId);
        }
    }

    private void clearFlashResendUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.removeAttribute(RESEND_USER_FLASH_KEY);
        }
    }

    private Set<Integer> getPendingResendSet(HttpServletRequest req, boolean create) {
        return PendingResendStore.getSet(req.getServletContext(), APP_PENDING_RESEND_KEY, create);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
