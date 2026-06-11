package controller;

import dal.LabRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.SystemLogService;

@WebServlet(name = "TechnicianDashboardServlet", urlPatterns = {"/technician-dashboard"})
public class TechnicianDashboardServlet extends HttpServlet {

    private LabRequestDAO labRequestDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        labRequestDAO = new LabRequestDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User account = (User) session.getAttribute("account");
        
        // Check authentication
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        
        // Check role (dùng RoleHelper để tương thích User có getRole() hoặc getRoleId())
        if (!RoleHelper.isTechnician(account)) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        
        // Check if this is an AJAX request for statistics
        String action = request.getParameter("action");
        if ("stats".equals(action)) {
            // Ghi log xem thống kê dashboard của kỹ thuật viên
            SystemLogService.logWithSession(session, "VIEW_TECH_DASHBOARD_STATS",
                    "Xem thống kê dashboard kỹ thuật viên");

            // Return statistics as JSON
            int[] stats = labRequestDAO.getLabRequestStatisticsWithFilter(null, null, null);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            out.print("{");
            out.print("\"success\": true,");
            out.print("\"total\": " + stats[0] + ",");
            out.print("\"pending\": " + stats[1] + ",");
            out.print("\"processing\": " + stats[2] + ",");
            out.print("\"completed\": " + stats[3]);
            out.print("}");
            out.flush();
            return;
        }
        
        // Forward to dashboard JSP
        request.getRequestDispatcher("/common/dashboard.jsp").forward(request, response);
    }
}
