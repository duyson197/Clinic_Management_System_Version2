/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.ratingDoctors;

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
import model.Rating_note;
import model.Rating_review;
import model.ReviewAnswer;
import model.User;

/**
 *
 * @author Admin
 */
public class ListOfRating extends HttpServlet {

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
            out.println("<title>Servlet ListOfRating</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ListOfRating at " + request.getContextPath() + "</h1>");
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
        User u = (User) session.getAttribute("account");
        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        String doc = request.getParameter("btnDoctorID");
        if (doc == null) {
            doc = request.getParameter("doctorId");
        }
        if (doc == null) {
            doc = request.getParameter("id");
        }

        if (doc == null || doc.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/listofdoctorservlet");
            return;
        }

        String appointmentId = request.getParameter("appointmentId");
        int doctorId = Integer.parseInt(doc);

        DoctorDAO doctorDAO = new DoctorDAO();
        RatingDAO ratingDAO = new RatingDAO();
        Doctor doctor = doctorDAO.getDoctorById(doc);

        List<Rating_review> questions = ratingDAO.getQuestions();
        for (Rating_review q : questions) {
            if (q.getId() != 5) {
                double avg = ratingDAO.getAverageRating(q.getId(), doctorId);
                int total = ratingDAO.getTotalReview(q.getId(), doctorId);
                q.setAvgRating(avg);
                q.setTotalReviews(total);
            }
        }

        List<Rating_note> notes = ratingDAO.getNotesByDoctor(doctorId);

        request.setAttribute("doctor", doctor);
        request.setAttribute("questions", questions);
        request.setAttribute("user", u);
        request.setAttribute("appointmentID", appointmentId);
        request.setAttribute("notes", notes);

        request.getRequestDispatcher("/pages/rating/ListRatingOfDoctor/ListOfRating.jsp")
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

        String action = request.getParameter("action");

        int doctorID = Integer.parseInt(request.getParameter("doctorID"));
        int appointmentID = Integer.parseInt(request.getParameter("appointmentId"));

        HttpSession session = request.getSession();
        User u = (User) session.getAttribute("account");

        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        int userID = u.getUserId();
        RatingDAO dao = new RatingDAO();

        if ("delete".equals(action)) {

            dao.deleteRatingByAppointment(appointmentID);

            response.sendRedirect(
                    request.getContextPath() + "/listofrating?btnDoctorID=" + doctorID
            );
            return;
        }
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

        response.sendRedirect(
                request.getContextPath() + "/listofrating?btnDoctorID=" + doctorID
        );
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
