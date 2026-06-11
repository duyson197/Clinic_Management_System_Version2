/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.appointments;

import dal.AppointmentDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import model.AppointmentDetail;
import model.User;
import util.SystemLogService;

/**
 *
 * @author Admin
 */
public class listOfAppointment extends HttpServlet {

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
            out.println("<title>Servlet listOfAppointment</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet listOfAppointment at " + request.getContextPath() + "</h1>");
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

        AppointmentDAO dao = new AppointmentDAO();
        dao.cancelPastBookedAppointments();
        List<AppointmentDetail> list = dao.getAllAppointments();

        request.setAttribute("list", list);

        request.getRequestDispatcher("/pages/appointments/listOfAppointment/listOfAppointment.jsp")
                .forward(request, response);

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

        long id = Long.parseLong(request.getParameter("id"));
        String status = request.getParameter("status");
        String up = request.getParameter("up");

        AppointmentDAO dao = new AppointmentDAO();

        dao.updateStatus(id, status);

        if ( "checked_in".equalsIgnoreCase(status)) {
            int doctorId = dao.getDoctorIdByAppointment(id);
            dao.addQueueWithPriority(id, doctorId);
        }

        HttpSession session = request.getSession(false);
        String action;

        if ("cancelled".equalsIgnoreCase(status)) {
            action = "CANCEL_APPOINTMENT";
        } else if ("checked_in".equalsIgnoreCase(status)) {
            action = "CHECKIN_APPOINTMENT";
        } else {
            action = "UPDATE_APPOINTMENT_STATUS";
        }

        SystemLogService.logWithSession(session, action,
                "Cập nhật trạng thái lịch hẹn appointmentId=" + id + " -> " + status);

        if (up != null) {
            response.sendRedirect("historyofappointmentservlet");
        } else {
            response.sendRedirect("listofappointment");
        }
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
