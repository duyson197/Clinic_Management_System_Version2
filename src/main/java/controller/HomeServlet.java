package controller;

import dal.AppointmentDAO;
import dal.DoctorDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Doctor;

public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String doctorKeyword = request.getParameter("doctorKeyword");
        List<Doctor> homeDoctors = new ArrayList<>();

        try {
            DoctorDAO dao =new DoctorDAO();
            if (doctorKeyword != null && !doctorKeyword.trim().isEmpty()) {
                homeDoctors = dao.filterDoctors(doctorKeyword.trim(), null, null, null, "rating");
            } else {
                homeDoctors = dao.getAllDoctors();
            }
        } catch (Exception ex) {
            request.setAttribute("homeDoctorError", "Không thể tải danh sách bác sĩ lúc này.");
        }

        request.setAttribute("doctorKeyword", doctorKeyword);
        request.setAttribute("homeDoctors", homeDoctors);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}