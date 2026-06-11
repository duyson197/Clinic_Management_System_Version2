package controller;

import dal.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import model.Role;
import model.User;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin-reports"})
public class AdminReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

        ReportDAO dao = new ReportDAO();

        // filter by doctor
        int doctorId = 0;
        String doctorIdParam = request.getParameter("doctorId");
        if (doctorIdParam != null && !doctorIdParam.isEmpty()) {
            try { doctorId = Integer.parseInt(doctorIdParam); } catch (NumberFormatException ignored) {}
        }

        int[] apptStats = dao.getAppointmentStatusStats(doctorId);
        int[] labStats = dao.getLabRequestStatusStats(doctorId);
        BigDecimal[] paymentSummary = dao.getPaymentSummary(doctorId);
        java.util.List<model.DoctorProductivity> doctorProductivity = dao.getDoctorProductivity(doctorId);
        java.util.List<model.DoctorProductivity> allDoctors = dao.getAllDoctors();

        request.setAttribute("apptStats", apptStats);
        request.setAttribute("labStats", labStats);
        request.setAttribute("paymentSummary", paymentSummary);
        request.setAttribute("doctorProductivity", doctorProductivity);
        request.setAttribute("allDoctors", allDoctors);
        request.setAttribute("selectedDoctorId", doctorId);

        // scalar values tiện dùng trong EL/JS
        request.setAttribute("apptTotal", apptStats[0]);
        request.setAttribute("apptBooked", apptStats[1]);
        request.setAttribute("apptCheckedIn", apptStats[2]);
        request.setAttribute("apptWaiting", apptStats[3]);
        request.setAttribute("apptCompleted", apptStats[4]);
        request.setAttribute("apptCancelled", apptStats[5]);

        request.getRequestDispatcher("pages/admin/admin-reports.jsp").forward(request, response);
    }
}

