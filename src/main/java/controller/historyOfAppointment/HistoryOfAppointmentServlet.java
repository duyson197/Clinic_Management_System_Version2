/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.historyOfAppointment;

import dal.AppointmentDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.AppointmentDetail;
import model.User;
import util.SystemLogService;

/**
 *
 * @author Admin
 */
public class HistoryOfAppointmentServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet HistoryOfAppointmentServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet HistoryOfAppointmentServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Lấy user đang đăng nhập
        User user = (User) session.getAttribute("account");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        AppointmentDAO dao = new AppointmentDAO();
        dao.cancelPastBookedAppointments();

        List<AppointmentDetail> list
                = dao.getAppointmentsByPatientUserId(user.getUserId());
 Long highlightedAppointmentId = null;
        String appointmentIdRaw = request.getParameter("appointmentId");
        if (appointmentIdRaw != null && !appointmentIdRaw.isBlank()) {
            try {
                long value = Long.parseLong(appointmentIdRaw);
                if (value > 0) {
                    highlightedAppointmentId = value;
                }
            } catch (NumberFormatException ex) {
                highlightedAppointmentId = null;
            }
        }

        request.setAttribute("highlightedAppointmentId", highlightedAppointmentId);
        request.setAttribute("appointmentList", list);

        request.getRequestDispatcher(
                "/pages/profile/historyOfAppointment/historyOfAppointment.jsp"
        ).forward(request, response);

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {

            long appointmentId = Long.parseLong(request.getParameter("id"));
            String status = request.getParameter("status");

            AppointmentDAO dao = new AppointmentDAO();
            dao.updateStatus(appointmentId, status);

            HttpSession sessionLog = request.getSession(false);
            User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
            Integer logUserId = userLog != null ? userLog.getUserId() : null;
            SystemLogService.log(logUserId, "APPOINTMENT_STATUS_UPDATED",
                    "Cập nhật trạng thái lịch hẹn: appointmentId=" + appointmentId + ", status=" + status);

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("historyofappointmentservlet");

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
