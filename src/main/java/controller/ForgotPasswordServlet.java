package controller;

import dal.UserDAO;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.EmailOtpService;
import model.User;
import util.SystemLogService;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final String FP_EMAIL = "forgotPasswordEmail";
    private static final String FP_OTP = "forgotPasswordOtp";
    private static final String FP_EXPIRES = "forgotOtpExpires"; 
    private static final String FP_VERIFIED = "forgotPasswordVerified";
    private static final long OTP_TTL_MS = 60 * 1000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("sendOtp".equals(action) || "resend".equals(action)) {
            handleSendOtp(request, session);
        } else if ("verifyOtp".equals(action)) {
            handleVerifyOtp(request, session);
        } else if ("resetPassword".equals(action)) {
            handleResetPassword(request, response, session);
            return;
        }

        request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleSendOtp(HttpServletRequest request, HttpSession session) {
        String emailInput = request.getParameter("email");
        if (emailInput != null) emailInput = emailInput.trim();
        
        final String email = emailInput; 
        request.setAttribute("email", email);

        UserDAO dao = new UserDAO();
        if (email == null || email.isBlank() || !dao.isEmailExist(email)) {
            request.setAttribute("error", "Gmail không tồn tại trong hệ thống.");
            return;
        }

        final String otpCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiresAt = System.currentTimeMillis() + OTP_TTL_MS;

     
        new Thread(() -> {
            try {
                
                EmailOtpService.sendOtp(email, "Người dùng", otpCode, 60);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        session.setAttribute(FP_EMAIL, email);
        session.setAttribute(FP_OTP, otpCode);
        session.setAttribute(FP_EXPIRES, expiresAt);
        session.setAttribute(FP_VERIFIED, false); 
        
        request.setAttribute("success", "Đã gửi OTP thành công. Vui lòng kiểm tra Gmail.");
        request.setAttribute("showVerifyForm", true);
    }

    private void handleVerifyOtp(HttpServletRequest request, HttpSession session) {
        String submittedOtp = request.getParameter("otp");
        String storedOtp = (String) session.getAttribute(FP_OTP);
        Long expiresAt = (Long) session.getAttribute(FP_EXPIRES);

        request.setAttribute("showVerifyForm", true);

        if (storedOtp == null || expiresAt == null || System.currentTimeMillis() > expiresAt) {
            request.setAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại.");
            return;
        }
        if (submittedOtp == null || !submittedOtp.equals(storedOtp)) {
            request.setAttribute("error", "Mã OTP không chính xác.");
            return;
        }

        session.setAttribute(FP_VERIFIED, true);
        session.removeAttribute(FP_EXPIRES); 
        request.setAttribute("success", "Xác thực thành công. Hãy đặt mật khẩu mới.");
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ServletException, IOException {
    String storedEmail = (String) session.getAttribute("forgotPasswordEmail");
    Boolean isVerified = (Boolean) session.getAttribute("forgotPasswordVerified");
    String newPassword = request.getParameter("newPassword");
    String confirmPassword = request.getParameter("confirmPassword");

    
    if (storedEmail == null || isVerified == null || !isVerified) {
        response.sendRedirect(request.getContextPath() + "/forgot-password");
        return;
    }

    if (!newPassword.equals(confirmPassword)) {
        request.setAttribute("error", "Mật khẩu xác nhận không khớp.");
        request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
        return;
    }

    UserDAO dao = new UserDAO();
    User user = dao.getUserByEmail(storedEmail);

    if (user != null) {
        
        if (dao.checkOldPassword(user.getUserId(), newPassword)) {
            request.setAttribute("error", "Mật khẩu mới không được giống mật khẩu cũ.");
            request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        
        try {
            dao.updatePassword(user.getUserId(), newPassword);
            SystemLogService.log(user.getUserId(), "PASSWORD_RESET",
                    "Đặt lại mật khẩu thành công: email=" + storedEmail);

            session.invalidate();

            String encodedEmail = java.net.URLEncoder.encode(storedEmail, "UTF-8");
            response.sendRedirect(request.getContextPath() + "/login?reset=true&email=" + encodedEmail);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi CSDL: " + e.getMessage());
            request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
        }
    } else {
        request.setAttribute("error", "Không tìm thấy người dùng.");
        request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
    }
}
}