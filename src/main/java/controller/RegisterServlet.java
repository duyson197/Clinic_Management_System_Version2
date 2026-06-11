package controller;

import dal.UserDAO;
import model.EmailOtpService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final String OTP_SESSION_KEY = "registerOtp";
    private static final String OTP_EXPIRES_SESSION_KEY = "registerOtpExpires";
    private static final String PENDING_REGISTER_SESSION_KEY = "pendingRegisterData";
    private static final long OTP_TTL_MS = 60 * 1000; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = normalizeSpace(request.getParameter("fullname"));
        String phone = normalizeSpace(request.getParameter("phone"));
        String email = normalizeSpace(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");

        UserDAO dao = new UserDAO();
        String error = null;

        // --- HÀM VALIDATE ĐÃ FIX CÚ PHÁP ---
        if (fullName == null || fullName.isBlank()) {
            error = "Vui lòng nhập họ và tên.";
        } else if (fullName.length() < 2) {
            error = "Họ và tên phải có ít nhất 2 ký tự.";
        } else if (!fullName.matches("^[\\p{L}\\s'.-]+$")) {
            error = "Họ và tên không hợp lệ.";
        } else if (phone == null || !phone.matches("0\\d{9}")) {
            error = "Số điện thoại không hợp lệ (10 số, bắt đầu bằng số 0).";
        } else if (email == null || email.isBlank()) {
            error = "Vui lòng nhập email.";
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email)) {
            error = "Email không hợp lệ.";
        } else if (password == null || password.length() < 6) {
            error = "Mật khẩu phải có ít nhất 6 ký tự.";
        } else if (confirm == null || confirm.isBlank()) {
            error = "Vui lòng nhập xác nhận mật khẩu.";
        } else if (!password.equals(confirm)) {
            error = "Mật khẩu xác nhận không khớp.";
        } else if (dao.isPhoneExist(phone)) {
            error = "Số điện thoại đã được đăng ký.";
        } else if (dao.isEmailExist(email)) {
            error = "Email đã tồn tại trong hệ thống.";
        }

        if (error != null) {
            setBackData(request, fullName, phone, email);
            request.setAttribute("error", error);
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }

        // --- LƯU SESSION ---
        HttpSession session = request.getSession();
        Map<String, String> pendingData = new HashMap<>();
        pendingData.put("fullName", fullName);
        pendingData.put("phone", phone);
        pendingData.put("email", email);
        pendingData.put("password", password);

        String otpCode = generateOtp();
        long expiredAt = System.currentTimeMillis() + OTP_TTL_MS;

        session.setAttribute(PENDING_REGISTER_SESSION_KEY, pendingData);
        session.setAttribute(OTP_SESSION_KEY, otpCode);
        session.setAttribute(OTP_EXPIRES_SESSION_KEY, expiredAt);

        sendOtpEmail(email, fullName, otpCode);
        
        request.setAttribute("success", "Đã gửi OTP đến Gmail của bạn. Mã có hiệu lực trong 60 giây.");
        request.getRequestDispatcher("/pages/auth/verify-email.jsp").forward(request, response);
    }

    // Các hàm static helper giữ nguyên
    public static String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    public static long getOtpTtlMs() { return OTP_TTL_MS; }

    public static String sendOtpEmail(String email, String fullName, String otpCode) {
        if (email == null || email.isBlank()) return "Bạn cần nhập Gmail.";
        new Thread(() -> {
            try {
                EmailOtpService.sendOtp(email, fullName, otpCode, OTP_TTL_MS / 1000);
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
        return null; 
    }

    public static Map<String, String> getPendingData(HttpSession session) {
        return (Map<String, String>) session.getAttribute(PENDING_REGISTER_SESSION_KEY);
    }

    public static String getOtp(HttpSession session) {
        Object otp = session.getAttribute(OTP_SESSION_KEY);
        return otp == null ? null : otp.toString();
    }

    public static Long getOtpExpires(HttpSession session) {
        return (Long) session.getAttribute(OTP_EXPIRES_SESSION_KEY);
    }

    public static void clearPendingRegister(HttpSession session) {
        session.removeAttribute(PENDING_REGISTER_SESSION_KEY);
        session.removeAttribute(OTP_SESSION_KEY);
        session.removeAttribute(OTP_EXPIRES_SESSION_KEY);
    }

    private void setBackData(HttpServletRequest request, String fullName, String phone, String email) {
        request.setAttribute("fullname", fullName);
        request.setAttribute("phone", phone);
        request.setAttribute("email", email);
    }

    private String normalizeSpace(String value) {
        return (value == null) ? null : value.trim().replaceAll("\\s+", " ");
    }
}