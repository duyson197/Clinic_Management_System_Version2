/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.doctor;

import dal.DoctorDAO;
import dal.DoctorScheduleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import model.Doctor;
import model.DoctorDashboardStats;
import model.DoctorQueueItem;
import model.DoctorShift;
import model.User;
import util.PagingHelper;
import util.SystemLogService;

/**
 *
 * @author anngu
 */
public class DoctorDashboardServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

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
        doGet(request, response);
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
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User account = (User) session.getAttribute("account");
        if (account == null || account.getRole() == null || !"doctor".equalsIgnoreCase(account.getRole().name())) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
        Doctor doctor = doctorDAO.getDoctorByUserId(account.getUserId());
        if (doctor == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        int doctorId = doctor.getDoctorId();
        session.setAttribute("doctorName", doctor.getFullName());

//        int doctorId = 3;// test 
        //lấy thông tin lọc
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        if (status == null || status.isBlank()) {
            status = "active";
        } else {
            status = status.trim().toLowerCase();
            if ("completed".equals(status)) {
                status = "done";
            }
            if (!"active".equals(status)
                    && !"waiting".equals(status)
                    && !"waiting_return".equals(status)
                    && !"in_lab".equals(status)
                    && !"examining".equals(status)
                    && !"done".equals(status)) {
                status = "active";
            }
        }

        int requestedPage = PagingHelper.parsePage(request, "page", 1);

        int totalRecords = doctorDAO.countQueueByDoctorWithFilter(doctorId, status, keyword);
        PagingHelper.PagingMeta paging = PagingHelper.build(requestedPage, totalRecords, PAGE_SIZE, true);
        //Danh sách chờ khám
        List<DoctorQueueItem> queueList
                = doctorDAO.getQueueByDoctorWithFilterPaging(doctorId, status, keyword, paging.getCurrentPage(), paging.getPageSize());
        DoctorQueueItem currentExamining = doctorDAO.getCurrentExaminingQueueItem(doctorId);
        DoctorQueueItem nextWaiting = doctorDAO.getNextWaitingQueueItem(doctorId);
        DoctorQueueItem startTarget = currentExamining != null ? currentExamining : nextWaiting;
        
        // số liệu dashboard
        DoctorDashboardStats stats
                = doctorDAO.getDashboardStats(doctorId);

        // Ghi log xem thống kê dashboard bác sĩ
        SystemLogService.logWithSession(session, "VIEW_DOCTOR_DASHBOARD",
                "Bác sĩ " + doctor.getFullName()
                + " xem dashboard với status=" + status
                + ", keyword=" + (keyword == null ? "" : keyword.trim()));

        //Ca làm việc trong ngày
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue() % 7; // CN = 0
//        int dayOfWeek = 1; // test 
        List<DoctorShift> shifts
                = scheduleDAO.getShiftsByDoctorAndDay(doctorId, dayOfWeek);

        request.setAttribute("queueList", queueList);
        request.setAttribute("stats", stats);
        request.setAttribute("shifts", shifts);
        PagingHelper.expose(request, paging);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.setAttribute("startAppointmentId", startTarget == null ? null : startTarget.getAppointmentId());
        request.setAttribute("startQueuePosition", startTarget == null ? null : startTarget.getQueuePosition());
        request.setAttribute("startPatientName", startTarget == null ? null : startTarget.getPatientName());
        
        request.getRequestDispatcher("/pages/examination/doctorDashboard.jsp")
                .forward(request, response);
//        String queueId = request.getParameter("queueId");
//        request.setAttribute("queueId", queueId); //  để sau dùng
//
//        request.getRequestDispatcher("/pages/doctors/exam.jsp").forward(request, response);
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
         doGet(request, response);
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
