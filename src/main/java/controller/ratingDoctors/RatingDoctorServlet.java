/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.ratingDoctors;

import dal.AppointmentDAO;
import dal.DoctorDAO;
import dal.RatingDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Doctor;
import model.Rating_review;
import model.User;
import util.SystemLogService;

/**
 *
 * @author Admin
 */
public class RatingDoctorServlet extends HttpServlet {

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
            out.println("<title>Servlet RatingDoctorServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RatingDoctorServlet at " + request.getContextPath() + "</h1>");
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
        String doctorId = request.getParameter("id");
        String appointmentID = request.getParameter("appointmentId");
        DoctorDAO doctordao = new DoctorDAO();
        Doctor doctor = doctordao.getDoctorById(doctorId);
        RatingDAO daos = new RatingDAO();
        List<Rating_review> list = daos.getQuestions();
        request.setAttribute("doctor", doctor);
        request.setAttribute("appointmentID", appointmentID);
        request.setAttribute("list", list);
        request.getRequestDispatcher("/pages/rating/ratingDoctor/reportDoctor.jsp")
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
        int doctorID = Integer.parseInt(request.getParameter("doctorID"));
        String appointmentId = request.getParameter("appointmentId");
        int appointmentID = Integer.parseInt(appointmentId);
        HttpSession session = request.getSession();
        User u = (User) session.getAttribute("account");

        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        int userID = u.getUserId();

        RatingDAO dao = new RatingDAO();
        dao.deleteRatingByAppointment(appointmentID);
    
        List<Rating_review> questions = dao.getQuestions();

        for (Rating_review q : questions) {

            if (q.getId() == 5) {
                String noteValue = request.getParameter("note_" + q.getId());

                if (noteValue != null && !noteValue.trim().isEmpty()) {
                    dao.insertReviewAnswer(
                            q.getId(),
                            null,
                            userID,
                            doctorID,
                            appointmentID,
                            noteValue
                    );
                }

            } else {
                String ratingValue = request.getParameter("rating_" + q.getId());

                if (ratingValue != null && !ratingValue.isEmpty()) {
                    int stars = Integer.parseInt(ratingValue);

                    dao.insertReviewAnswer(
                            q.getId(),
                            stars,
                            userID,
                            doctorID,
                            appointmentID,
                            null
                    );
                }
            }
        }

        Double avg = dao.getAverageRating(doctorID);

        if (avg != null) {
            dao.updateDoctorRating(doctorID, avg);
        }
        SystemLogService.log(userID, "DOCTOR_REVIEWED",
                "Đánh giá bác sĩ: doctorId=" + doctorID + ", userId=" + userID);
        response.sendRedirect(request.getContextPath() + "/historyofappointmentservlet");

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
