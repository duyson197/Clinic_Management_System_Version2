/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.appointments;

import dal.AppointmentDAO;
import dal.DoctorDAO;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Doctor;
import model.User;

/**
 *
 * @author Admin
 */
@WebServlet("/listOfDoctor")
public class ListOfDoctorServlet extends HttpServlet {

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
            out.println("<title>Servlet ListOfDoctorServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ListOfDoctorServlet at " + request.getContextPath() + "</h1>");
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

        User user = (User) session.getAttribute("account");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        String doctorName = request.getParameter("doctorName");
        String priceFrom = request.getParameter("priceFrom");
        String priceTo = request.getParameter("priceTo");
        String experience = request.getParameter("experience");
        String sort = request.getParameter("sort");

        DoctorDAO dao = new DoctorDAO();
        List<Doctor> doctors;
        String error = "";
        boolean hasFilter
                = (doctorName != null && !doctorName.isEmpty())
                || (priceFrom != null && !priceFrom.isEmpty())
                || (priceTo != null && !priceTo.isEmpty())
                || (experience != null && !experience.isEmpty())
                || (sort != null && !sort.isEmpty());

        if (hasFilter) {
            if (priceTo != null && !priceTo.isEmpty() && priceFrom != null && !priceFrom.isEmpty() && checkPrice(priceFrom, priceTo)) {
                error = "Giá từ phải nhỏ hơn giá cao";
                doctors = dao.getAllDoctors();

            } else {
                doctors = dao.filterDoctors(doctorName, priceFrom, priceTo, experience, sort);
            }
        } else {
            doctors = dao.getAllDoctors();
        }
        request.setAttribute("doctors", doctors);
        request.setAttribute("error", error);
        request.setAttribute("doctorName", doctorName);
        request.setAttribute("priceFrom", priceFrom);
        request.setAttribute("priceTo", priceTo);
        request.setAttribute("user", user);

        request.getRequestDispatcher("/pages/appointments/listOfDoctors.jsp")
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

        HttpSession session = request.getSession();

        User user = (User) session.getAttribute("account");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        String doctorID = request.getParameter("doctorID");
        request.setAttribute("DoctorID", doctorID);
        request.setAttribute("user", user);

        request.getRequestDispatcher("/pages/profile/createPatients/createPatients.jsp")
                .forward(request, response);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private boolean checkPrice(String priceFrom, String priceTo) {
        double from = Double.parseDouble(priceFrom.trim());
        double to = Double.parseDouble(priceTo.trim());

        return from > to;
    }

}
