package controller.userInformation;

import dal.DoctorDAO;
import dal.UserDAO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Doctor;
import model.EmailOtpService;
import model.User;
import util.SystemLogService;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class UserInformationServlet extends HttpServlet {

    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}]+(?: [\\p{L}]+)*$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$"); // Đúng chuẩn 10 số
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final String PROFILE_OTP_CODE = "profile_update_otp_code";
    private static final String PROFILE_OTP_EXPIRES = "profile_update_otp_expires";
    private static final String PROFILE_PENDING_EMAIL = "profile_pending_email";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User userSession = (User) session.getAttribute("account");
        int userId = userSession.getUserId();

        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById1(userId);

        request.setAttribute("user", user);
        request.setAttribute("roleName", user.getRole());
        DoctorDAO doc = new DoctorDAO();
        if ("doctor".equalsIgnoreCase(String.valueOf(user.getRole()))) {
            Doctor doctor = doc.getDoctorByUserId(userId);
            request.setAttribute("doctor", doctor);
        }

        request.getRequestDispatcher("/pages/profile/userInformation/userInformation.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UserDAO users = new UserDAO();
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User userSession = (User) session.getAttribute("account");
        int userId = userSession.getUserId();
        String action = trim(request.getParameter("action"));

        // GỬI OTP (AJAX)
        if ("ajaxSendOtp".equals(action)) {
            response.setContentType("application/json;charset=UTF-8");
            String newEmail = trim(request.getParameter("newEmail")).toLowerCase(Locale.ROOT);

            if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                response.getWriter().write("{\"success\":false, \"message\":\"Email không đúng định dạng.\"}");
                return;
            }
            User emailOwner = users.getUserByEmail(newEmail);
            if (emailOwner != null && emailOwner.getUserId() != userId) {
                response.getWriter().write("{\"success\":false, \"message\":\"Email đã được sử dụng bởi người khác.\"}");
                return;
            }

            boolean otpSent = sendProfileEmailOtp(session, newEmail, userSession.getFullName());
            if (otpSent) {
                response.getWriter().write("{\"success\":true}");
            } else {
                response.getWriter().write("{\"success\":false, \"message\":\"Lỗi hệ thống khi gửi email.\"}");
            }
            return;
        }

        // LƯU TOÀN BỘ THÔNG TIN
        if ("updateProfile".equals(action)) {
            User currentUser = users.getUserById1(userId);
            
            String name = trim(request.getParameter("txtName"));
            String phone = trim(request.getParameter("txtPhone"));
            String email = trim(request.getParameter("txtEmail")).toLowerCase(Locale.ROOT);
            String emailOtp = trim(request.getParameter("emailOtp"));

            String validationError = validateProfileInput(name, phone);
            if (!validationError.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=" + validationError + "&edit=1");
                return;
            }
            User phoneOwner = users.getUserByPhone(phone);
            if (phoneOwner != null && phoneOwner.getUserId() != userId) {
                response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=phoneExists&edit=1");
                return;
            }

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=invalidEmail&edit=1");
                return;
            }
            
            // Nếu đổi Email, bắt buộc xác thực OTP (Giữ lại Email đang nhập nếu lỗi)
            if (!email.equalsIgnoreCase(currentUser.getEmail())) {
                String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8); 
                if (emailOtp.isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=otpRequired&edit=1&pendingEmail=" + encodedEmail);
                    return;
                }
                if (!isValidProfileOtp(session, email, emailOtp)) {
                    response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=invalidOtp&edit=1&pendingEmail=" + encodedEmail);
                    return;
                }
            }

            String avatarUrl = currentUser.getImageUrl();
            if ("doctor".equalsIgnoreCase(String.valueOf(currentUser.getRole()))) {
                String uploadedAvatar = handleDoctorAvatarUpload(request);
                if (uploadedAvatar == null && "avatarTypeInvalid".equals(request.getAttribute("profileUploadError"))) {
                    response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=avatarTypeInvalid&edit=1");
                    return;
                }
                if (uploadedAvatar == null && "avatarTooLarge".equals(request.getAttribute("profileUploadError"))) {
                    response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=avatarTooLarge&edit=1");
                    return;
                }
                if (uploadedAvatar != null) {
                    avatarUrl = uploadedAvatar;
                }
            }

            users.updateUser(userId, name, phone, email, avatarUrl);
            clearProfileOtp(session);
            SystemLogService.log(userId, "PROFILE_UPDATED",
                    "Cập nhật thông tin cá nhân: name=" + name + ", phone=" + phone + ", email=" + email);
            User updatedUser = users.getUserById1(userId);
            session.setAttribute("account", updatedUser);
            response.sendRedirect(request.getContextPath() + "/userinformationservlet?success=profileUpdated");
            return;
        }

        // ĐỔI MẬT KHẨU
        if ("changePass".equals(action)) {
            String oldPass = trim(request.getParameter("txtOldPass"));
            String newPass = trim(request.getParameter("txtNewPass"));
            String rePass = trim(request.getParameter("txtReNewPass"));

            if (!users.checkOldPassword(userId, oldPass)) {
                response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=oldPasswordIncorrect");
                return;
            }
            if (newPass.length() < 6) {
                response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=passwordTooShort");
                return;
            }
            if (!newPass.equals(rePass)) {
                response.sendRedirect(request.getContextPath() + "/userinformationservlet?error=passwordMismatch");
                return;
            }

            users.updatePassword(userId, newPass);
            SystemLogService.log(userId, "PASSWORD_CHANGED", "Đổi mật khẩu thành công: userId=" + userId);
            response.sendRedirect(request.getContextPath() + "/userinformationservlet?success=passwordChanged");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/userinformationservlet");
    }

    private String handleDoctorAvatarUpload(HttpServletRequest request) {
        try {
            Part avatarPart = request.getPart("avatarFile");
            if (avatarPart == null || avatarPart.getSize() <= 0) return null;
            if (avatarPart.getSize() > 5L * 1024 * 1024) {
                request.setAttribute("profileUploadError", "avatarTooLarge");
                return null;
            }
            String submittedName = avatarPart.getSubmittedFileName();
            if (submittedName == null || submittedName.isBlank()) return null;
            
            String fileName = Paths.get(submittedName).getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex < 0) {
                request.setAttribute("profileUploadError", "avatarTypeInvalid");
                return null;
            }
            String ext = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            if (!ALLOWED_AVATAR_EXTENSIONS.contains(ext)) {
                request.setAttribute("profileUploadError", "avatarTypeInvalid");
                return null;
            }

            String uniqueName = "DOCTOR_AVATAR_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "avatars";
            Files.createDirectories(Path.of(uploadPath));
            Path target = Path.of(uploadPath, uniqueName);
            Files.copy(avatarPart.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/avatars/" + uniqueName;
        } catch (Exception ex) {
            request.setAttribute("profileUploadError", "avatarTypeInvalid");
            return null;
        }
    }

    private String validateProfileInput(String name, String phone) {
        if (name.isEmpty() || !FULL_NAME_PATTERN.matcher(name).matches()) return "invalidName";
        if (!PHONE_PATTERN.matcher(phone).matches()) return "invalidPhone";
        return "";
    }

    private boolean sendProfileEmailOtp(HttpSession session, String email, String fullName) {
        try {
            String otpCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            long expiresAt = System.currentTimeMillis() + 60 * 1000; 
            EmailOtpService.sendOtp(email, fullName, otpCode, 60); 
            session.setAttribute(PROFILE_OTP_CODE, otpCode);
            session.setAttribute(PROFILE_OTP_EXPIRES, expiresAt);
            session.setAttribute(PROFILE_PENDING_EMAIL, email);
            return true;
        } catch (Exception ex) {
            clearProfileOtp(session);
            return false;
        }
    }

    private boolean isValidProfileOtp(HttpSession session, String email, String otpInput) {
        Object code = session.getAttribute(PROFILE_OTP_CODE);
        Object expires = session.getAttribute(PROFILE_OTP_EXPIRES);
        Object pendingEmail = session.getAttribute(PROFILE_PENDING_EMAIL);

        if (code == null || expires == null || pendingEmail == null) return false;
        if (!email.equalsIgnoreCase(String.valueOf(pendingEmail))) return false;
        
        long expiresAt;
        try { expiresAt = Long.parseLong(String.valueOf(expires)); } 
        catch (NumberFormatException ex) { return false; }
        
        return System.currentTimeMillis() <= expiresAt && String.valueOf(code).equals(otpInput);
    }

    private void clearProfileOtp(HttpSession session) {
        session.removeAttribute(PROFILE_OTP_CODE);
        session.removeAttribute(PROFILE_OTP_EXPIRES);
        session.removeAttribute(PROFILE_PENDING_EMAIL);
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }

    @Override
    public String getServletInfo() { return "User information management"; }
}