/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.appointments;

import dal.AppointmentDAO;
import dal.DoctorDAO;
import dal.NotificationDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Appointment;
import model.Doctor;
import model.Patient;
import util.SystemLogService;

/**
 *
 * @author Admin
 */
public class AppointmentPaymentServlet extends HttpServlet {

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
            out.println("<title>Servlet AppointmentPaymentServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AppointmentPaymentServlet at " + request.getContextPath() + "</h1>");
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
        String code = request.getParameter("code");
        String status = request.getParameter("status");
        AppointmentDAO dao = new AppointmentDAO();
        HttpSession session = request.getSession();
        Appointment appointment = (Appointment) session.getAttribute("pendingAppointment");

        Patient pendingPatient = (Patient) session.getAttribute("pendingPatient");
        long patientId = pendingPatient.getPatientId();
        long doctorId = appointment.getDoctorId();
        request.setAttribute("patientID", patientId);
        request.setAttribute("doctorID", doctorId);

        if ("00".equals(code) && "PAID".equals(status)) {
            if (appointment != null) {
                long createdAppointmentId = dao.addAppointmentAndReturnId(appointment);

                Integer logUserId = (pendingPatient != null && pendingPatient.getUserId() != null) ? pendingPatient.getUserId() : null;
                String patientName = (pendingPatient != null) ? pendingPatient.getFullName() : "unknown";
                request.setAttribute("bookedPatientName", patientName);
                Doctor doctorInfo = new DoctorDAO().getDoctorById(String.valueOf(appointment.getDoctorId()));

                String doctorName = (doctorInfo != null && doctorInfo.getFullName() != null && !doctorInfo.getFullName().isBlank())
                        ? doctorInfo.getFullName().trim() : ("Bác sĩ #" + appointment.getDoctorId());

                SystemLogService.log(logUserId, "APPOINTMENT_BOOKED",
                        "Đặt lịch và thanh toán thành công: patientName=" + patientName
                        + ", doctorId=" + appointment.getDoctorId()
                        + ", date=" + appointment.getAppointmentDate()
                        + ", time=" + appointment.getAppointmentTime());
                if (pendingPatient != null && pendingPatient.getUserId() != null) {
                    NotificationDAO notificationDAO = new NotificationDAO();
                    notificationDAO.createNotification(
                            pendingPatient.getUserId(),
                            "Đặt lịch thành công",
                            "Đặt lịch thành công cho bệnh nhân " + patientName + " với " + doctorName + ". Nhấn vào để mở đúng lịch sử cuộc hẹn vừa đặt.",
                            "appointment_booked",
                            "appointment:" + createdAppointmentId + ":booking_success:" + doctorName
                    );
                }
              
            }
            session.removeAttribute("pendingAppointment");
            session.removeAttribute("pendingPatient");
            request.setAttribute("message", "Đặt lịch và thanh toán thành công!");
            request.getRequestDispatcher("/pages/appointments/appointment/appointmentCompleted.jsp")
                    .forward(request, response);
        } else {
            request.setAttribute("patientID", pendingPatient.getPatientId());
            request.setAttribute("doctorID", appointment.getDoctorId());
            request.setAttribute("message", "Thanh toán thất bại hoặc đã huỷ!");
            request.getRequestDispatcher("/pages/appointments/appointment/appointmentFailPayment.jsp")
                    .forward(request, response);
        }
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
        processRequest(request, response);
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
