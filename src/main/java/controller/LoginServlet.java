package controller;

import dal.UserDAO;
import model.User;
import util.SystemLogService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         HttpSession session = request.getSession(false);
        if (session != null) {
            Object prefillEmail = session.getAttribute("prefillLoginEmail");
            Object prefillRole = session.getAttribute("prefillLoginRole");
            if (prefillEmail != null) {
                request.setAttribute("email", prefillEmail.toString());
                session.removeAttribute("prefillLoginEmail");
            }
            if (prefillRole != null) {
                request.setAttribute("role", prefillRole.toString());
                session.removeAttribute("prefillLoginRole");
            }
        }


        request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy dữ liệu từ form login.jsp

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String formRole = request.getParameter("role");


        UserDAO dao = new UserDAO();
        User user = dao.checkLogin(email, password);
        String error = null;

        if (user != null) {

            String dbRole = user.getRole().toString().toLowerCase();


            boolean isTabValid = false;
            if (formRole.equals("patient") && dbRole.equals("patient")) {
                isTabValid = true;
            } else if (formRole.equals("staff") && !dbRole.equals("patient")) {

                isTabValid = true;
            }


            if (isTabValid) {

                if (user.getStatus().toString().equalsIgnoreCase("inactive")) {
                    error = "Tài khoản của bạn đã bị khóa! Vui lòng liên hệ Admin.";
                } else {

                    HttpSession session = request.getSession();
                    session.setAttribute("account", user);


                    // Ghi log đăng nhập thành công
                    SystemLogService.log(user.getUserId(), "LOGIN_SUCCESS",
                            "Đăng nhập thành công với role=" + dbRole + ", email=" + email);

                    if (dbRole.equals("admin")) {
                        response.sendRedirect(request.getContextPath() + "/users");
                    } else if (dbRole.equals("doctor")) {
                        response.sendRedirect(request.getContextPath() + "/doctorDashboard");
                    } else if (dbRole.equals("technician")) {
                        response.sendRedirect(request.getContextPath() + "/lab-queue");
                    } else if (dbRole.equals("receptionist")) {
                        response.sendRedirect(request.getContextPath() + "/lab-payment");
                    } else if (dbRole.equals("patient_manager")) {
                        response.sendRedirect(request.getContextPath() + "/patient-accounts");
                    } else {

                        response.sendRedirect(request.getContextPath() + "/index.jsp");
                    }
                    return;
                }
            } else {
                error = "Vui lòng chọn đúng tab (Nhân viên hoặc Bệnh nhân) để đăng nhập!";
            }
        } else {
            error = "Gmail hoặc mật khẩu không chính xác!";
        }


        if (error != null) {
            request.setAttribute("error", error);


            request.setAttribute("email", email);
            request.setAttribute("role", formRole);


            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);

        }
    }
}
