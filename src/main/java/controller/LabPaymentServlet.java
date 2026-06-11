package controller;

import dal.LabPaymentDAO;
import model.LabPayment;
import model.User;
import util.PagingHelper;
import util.SystemLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LabPaymentServlet", urlPatterns = {"/lab-payment"})
public class LabPaymentServlet extends HttpServlet {

    private LabPaymentDAO labPaymentDAO;

    private static final int PAGE_SIZE = 10;

    @Override
    public void init() throws ServletException {
        super.init();
        labPaymentDAO = new LabPaymentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check authentication
        HttpSession session = request.getSession();
        User account = (User) session.getAttribute("account");

        if (account == null || !RoleHelper.isReceptionist(account)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Get filter parameters
        String paymentStatus = request.getParameter("status");
        String search = request.getParameter("search");

        // Count total records for pagination
        int totalRecords = labPaymentDAO.countPaymentsWithFilter(paymentStatus, search);
        int requestedPage = PagingHelper.parsePage(request, "page", 1);
        PagingHelper.PagingMeta paging = PagingHelper.build(requestedPage, totalRecords, PAGE_SIZE, true);

        // Get payment waiting list with filters and pagination
        List<LabPayment> payments = labPaymentDAO.getPaymentWaitingListWithFilter(
                paymentStatus, search, paging.getCurrentPage(), PAGE_SIZE
        );

        // Get statistics
        int[] stats = labPaymentDAO.getPaymentStatistics(search);

        // Set attributes for JSP
        request.setAttribute("payments", payments);
        request.setAttribute("stats", stats);
        request.setAttribute("filterStatus", paymentStatus != null ? paymentStatus : "");
        request.setAttribute("searchTerm", search != null ? search : "");
        PagingHelper.expose(request, paging);

        // Forward to JSP
        request.getRequestDispatcher("/pages/lab/lab-payment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check authentication
        HttpSession session = request.getSession();
        User account = (User) session.getAttribute("account");

        if (account == null || !RoleHelper.isReceptionist(account)) {
            response.getWriter().write("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        String action = request.getParameter("action");

        if ("confirmPayment".equals(action)) {
            try {
                long paymentId = Long.parseLong(request.getParameter("paymentId"));

                boolean success = labPaymentDAO.confirmPayment(paymentId);

                if (success) {
                    SystemLogService.log(account.getUserId(), "LAB_PAYMENT_CONFIRMED",
                            "Xác nhận thanh toán xét nghiệm: paymentId=" + paymentId);
                    response.getWriter().write("{\"success\": true, \"message\": \"Payment confirmed successfully\"}");
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Failed to confirm payment\"}");
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Invalid payment ID\"}");
            }
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid action\"}");
        }
    }
}
