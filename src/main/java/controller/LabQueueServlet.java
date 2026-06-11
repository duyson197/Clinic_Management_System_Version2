package controller;

import dal.DoctorDAO;
import dal.LabRequestDAO;
import dal.NotificationDAO;
import model.LabRequest;
import util.PagingHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 12     // 12MB (file + form)
)
public class LabQueueServlet extends HttpServlet {

    /** Giới hạn dung lượng file kết quả (10MB). */
    private static final long MAX_RESULT_FILE_SIZE = 10L * 1024 * 1024;

    /** Các phần mở rộng file được phép upload (kết quả xét nghiệm). */
    private static final Set<String> ALLOWED_RESULT_EXTENSIONS = Arrays.stream(
            new String[]{"pdf", "jpg", "jpeg", "png", "gif", "webp", "bmp", "doc", "docx", "xls", "xlsx"}
    ).collect(Collectors.toSet());

    private LabRequestDAO labRequestDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        labRequestDAO = new LabRequestDAO();
    }

    private static final int PAGE_SIZE = 8;

    private boolean isCurrentActiveRequest(int requestId) {
        int activeRequestId = labRequestDAO.getActiveRequestId();
        return activeRequestId == requestId;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check if viewing send result page
        String action = request.getParameter("action");
        if ("viewDetail".equals(action)) {
            String requestIdParam = request.getParameter("requestId");
            if (requestIdParam != null) {
                try {
                    int requestId = Integer.parseInt(requestIdParam);
                    LabRequest labRequest = labRequestDAO.getLabRequestById(requestId);
                    if (labRequest != null) {
                        request.setAttribute("labRequest", labRequest);
                        model.LabResult labResult = labRequestDAO.getLabResultByRequestId(requestId);
                        request.setAttribute("labResult", labResult);
                    }
                } catch (NumberFormatException e) { }
            }
            request.getRequestDispatcher("/pages/lab/view-detail.jsp").forward(request, response);
            return;
        }
        if ("viewSendResult".equals(action)) {
            String requestIdParam = request.getParameter("requestId");
            if (requestIdParam != null) {
                try {
                    int requestId = Integer.parseInt(requestIdParam);
                    LabRequest labRequest = labRequestDAO.getLabRequestById(requestId);
                    if (labRequest != null) {
                        request.setAttribute("labRequest", labRequest);
                    }
                } catch (NumberFormatException e) {
                    // Invalid ID
                }
            }
            request.getRequestDispatcher("/pages/lab/send-result.jsp").forward(request, response);
            return;
        }
        
        // Get filter parameters
        String status = request.getParameter("status");
        String search = request.getParameter("search");

        // Count total records for pagination
        int totalRecords = labRequestDAO.countLabRequestsWithFilter(status, null, null, search);
        int requestedPage = PagingHelper.parsePage(request, "page", 1);
        PagingHelper.PagingMeta paging = PagingHelper.build(requestedPage, totalRecords, PAGE_SIZE, true);

        // Get lab requests with filters and pagination
        List<LabRequest> labRequests = labRequestDAO.getLabRequestsWithFilterAndPagination(
            status, null, null, search, paging.getCurrentPage(), PAGE_SIZE
        );

        // Get statistics
        int[] stats = labRequestDAO.getLabRequestStatisticsWithFilter(status, null, search);

        // Set attributes for JSP
        int activeRequestId = labRequestDAO.getActiveRequestId();

        request.setAttribute("labRequests", labRequests);
        request.setAttribute("stats", stats);
        request.setAttribute("filterStatus", status != null ? status : "");
        request.setAttribute("searchTerm", search != null ? search : "");
        request.setAttribute("activeRequestId", activeRequestId);
        request.setAttribute("hasProcessingRequest", activeRequestId != -1);
        PagingHelper.expose(request, paging);
        
        // Forward to JSP
        request.getRequestDispatcher("/pages/lab/lab-queue.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if ("updateStatus".equals(action)) {
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            String newStatus = request.getParameter("status");

            if ("processing".equalsIgnoreCase(newStatus)) {
                // Allow starting any pending request only when no other request is currently processing
                int activeId = labRequestDAO.getActiveRequestId();
                if (activeId != -1) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Đang có phiếu xét nghiệm đang xử lý. Vui lòng hoàn thành trước.\"}");
                    return;
                }
            } else {
                // For other status transitions (e.g. completed), must be the active processing request
                if (!isCurrentActiveRequest(requestId)) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Phiếu này chưa tới lượt xử lý\"}");
                    return;
                }
            }
            
            boolean success = labRequestDAO.updateLabRequestStatus(requestId, newStatus);
            
            if (success) {
                if ("completed".equalsIgnoreCase(newStatus)) {
                    LabRequest requestInfo = labRequestDAO.getLabRequestById(requestId);
                    notifyLabResultToPatient(requestInfo, requestId, "Đã có kết quả xét nghiệm", "lab_result_ready", "result_ready");
                }
                response.getWriter().write("{\"success\": true}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Cập nhật thất bại\"}");
            }
            
        } else         if ("updateNotes".equals(action)) {
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            String notes = request.getParameter("notes");

            if (!isCurrentActiveRequest(requestId)) {
                response.getWriter().write("{\"success\": false, \"message\": \"Phiếu này chưa tới lượt xử lý\"}");
                return;
            }
            
            boolean success = labRequestDAO.updateLabRequestNotes(requestId, notes);
            
            if (success) {
                response.getWriter().write("{\"success\": true}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Lưu ghi chú thất bại\"}");
            }
            
        } else if ("createLabRequest".equals(action)) {
            jakarta.servlet.http.HttpSession session = request.getSession();
            model.User account = (model.User) session.getAttribute("account");
            if (account == null || !RoleHelper.isDoctor(account)) {
                response.getWriter().write("{\"success\": false, \"message\": \"Chỉ bác sĩ mới được chỉ định xét nghiệm\"}");
                return;
            }
            try {
                long appointmentId = Long.parseLong(request.getParameter("appointmentId"));
                DoctorDAO doctorDAO = new DoctorDAO();
                model.Doctor doctor = doctorDAO.getDoctorByUserId(account.getUserId());
                if (doctor == null) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Không tìm thấy thông tin bác sĩ\"}");
                    return;
                }
                int requestId = labRequestDAO.insertLabRequest(appointmentId, doctor.getDoctorId());
                if (requestId > 0) {
                    // Tạo payment record cho phiếu xét nghiệm
                    dal.LabPaymentDAO labPaymentDAO = new dal.LabPaymentDAO();
                    java.math.BigDecimal price = labPaymentDAO.getLabTestPrice();
                    labPaymentDAO.createLabPayment(appointmentId, requestId, price, "cash");
                    // Ghi system log
                    util.SystemLogService.logWithSession(session, "CREATE_LAB_REQUEST",
                            "Bác sĩ " + account.getFullName() + " tạo phiếu xét nghiệm cho appointmentId=" + appointmentId
                            + ", requestId=" + requestId);
                    response.getWriter().write("{\"success\": true, \"message\": \"Đã chỉ định xét nghiệm. Bệnh nhân đã chuyển sang chờ xác nhận thanh toán.\", \"requestId\": " + requestId + "}");
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Không thể tạo phiếu xét nghiệm\"}");
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Mã lịch hẹn không hợp lệ\"}");
            }

        } else if ("cancelRequest".equals(action)) {
            jakarta.servlet.http.HttpSession session = request.getSession();
            model.User account = (model.User) session.getAttribute("account");
            if (account == null || (!RoleHelper.isTechnician(account) && !RoleHelper.isDoctor(account))) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không có quyền hủy phiếu\"}");
                return;
            }
            try {
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                boolean success = labRequestDAO.cancelLabRequest(requestId);
                if (success) {
                    response.getWriter().write("{\"success\": true, \"message\": \"Đã hủy phiếu xét nghiệm. Bệnh nhân đã trở lại danh sách chờ khám.\"}");
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Không thể hủy (phiếu đã hoàn thành hoặc không tồn tại)\"}");
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Mã phiếu không hợp lệ\"}");
            }

        } else if ("sendResult".equals(action)) {
            // Get technician ID from session (technician, doctor, admin đều được gửi kết quả)
            jakarta.servlet.http.HttpSession session = request.getSession();
            model.User account = (model.User) session.getAttribute("account");
            
            boolean canSend = account != null && (
                RoleHelper.isTechnician(account) || RoleHelper.isDoctor(account) || RoleHelper.isAdmin(account)
            );
            if (!canSend) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không có quyền thực hiện. Vui lòng đăng nhập với tài khoản kỹ thuật viên.\"}");
                return;
            }
            
            try {
                // Parse request ID from request code (LAB-2026-0001) or direct ID
                String requestIdParam = request.getParameter("requestId");
                int requestId;
                
                if (requestIdParam.contains("LAB-")) {
                    // Extract ID from code format LAB-YYYY-XXXX
                    String[] parts = requestIdParam.split("-");
                    if (parts.length >= 3) {
                        requestId = Integer.parseInt(parts[2]);
                    } else {
                        response.getWriter().write("{\"success\": false, \"message\": \"Mã phiếu không hợp lệ\"}");
                        return;
                    }
                } else {
                    requestId = Integer.parseInt(requestIdParam);
                }

                if (!isCurrentActiveRequest(requestId)) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Phiếu này chưa tới lượt xử lý\"}");
                    return;
                }
                
                String notes = request.getParameter("notes");
                
                // Xử lý upload file kết quả xét nghiệm
                String resultFilePath = null;
                Part filePart = request.getPart("resultFile");
                if (filePart != null && filePart.getSize() > 0) {
                    long fileSize = filePart.getSize();
                    if (fileSize > MAX_RESULT_FILE_SIZE) {
                        response.getWriter().write("{\"success\": false, \"message\": \"File quá lớn. Dung lượng tối đa 10MB.\"}");
                        return;
                    }
                    String submittedName = filePart.getSubmittedFileName();
                    if (submittedName == null || submittedName.trim().isEmpty()) {
                        response.getWriter().write("{\"success\": false, \"message\": \"Tên file không hợp lệ.\"}");
                        return;
                    }
                    String fileName = Paths.get(submittedName).getFileName().toString();
                    int lastDot = fileName.lastIndexOf('.');
                    String fileExtension = lastDot >= 0 ? fileName.substring(lastDot).toLowerCase(Locale.ROOT) : "";
                    String extOnly = lastDot >= 0 ? fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT) : "";
                    if (!ALLOWED_RESULT_EXTENSIONS.contains(extOnly)) {
                        response.getWriter().write("{\"success\": false, \"message\": \"Định dạng file không được phép. Chỉ chấp nhận: PDF, ảnh (JPG, PNG, GIF, WebP, BMP), tài liệu (DOC, DOCX, XLS, XLSX).\"}");
                        return;
                    }
                    String uniqueFileName = "LAB_" + requestId + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

                    // Lưu file vào thư mục uploads/lab-results (tạo thư mục nếu chưa có)
                    String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "lab-results";
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }

                    String fullPath = uploadPath + File.separator + uniqueFileName;
                    Files.copy(filePart.getInputStream(), Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);

                    // Lưu path tương đối vào database (để có thể truy cập qua URL)
                    resultFilePath = "uploads/lab-results/" + uniqueFileName;
                }
                
                Integer technicianId = account.getUserId();
                
                boolean success = labRequestDAO.sendLabResult(requestId, technicianId, resultFilePath, notes);
                
               if (success) {
                    LabRequest requestInfo = labRequestDAO.getLabRequestById(requestId);
                    notifyLabResultToPatient(requestInfo, requestId, "Kết quả xét nghiệm đã được gửi", "lab_result_sent", "result_sent");
                    util.SystemLogService.log(account.getUserId(), "UPLOAD_LAB_RESULT",
                            "Gửi kết quả xét nghiệm: requestId=" + requestId
                            + (resultFilePath != null ? ", file=" + resultFilePath : ", noFile"));
                    response.getWriter().write("{\"success\": true, \"message\": \"Gửi kết quả thành công\"}");
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Gửi kết quả thất bại\"}");
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Mã phiếu không hợp lệ\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().write("{\"success\": false, \"message\": \"Lỗi hệ thống: " + e.getMessage() + "\"}");
            }
            
        } else {
            // Default: redirect to GET
            doGet(request, response);
        }
    }
    private void notifyLabResultToPatient(LabRequest requestInfo, int requestId, String title, String type, String eventSuffix) {
        if (requestInfo == null) {
            return;
        }
        String patientName = requestInfo.getPatient() != null && requestInfo.getPatient().getFullName() != null
                ? requestInfo.getPatient().getFullName().trim() : "bệnh nhân";
        String message = "Xét nghiệm đã xong cho " + patientName + ". Bạn hãy đến bác sĩ vừa yêu cầu xét nghiệm để khám và xem kết quả xét nghiệm.";
        if ("lab_result_sent".equals(type)) {
            message = "Xét nghiệm đã xong cho " + patientName + ". Bạn hãy đến bác sĩ vừa yêu cầu xét nghiệm để khám và xem kết quả xét nghiệm.";
        }

        NotificationDAO notificationDAO = new NotificationDAO();
        boolean sent = notificationDAO.createNotificationForAppointment(
                requestInfo.getAppointmentId(),
                title,
                message,
                type,
                "lab_request:" + requestId + ":" + eventSuffix
        );

        if (!sent && requestInfo.getPatient() != null && requestInfo.getPatient().getUserId() > 0) {
            notificationDAO.createNotification(
                    requestInfo.getPatient().getUserId(),
                    title,
                    message,
                    type,
                    "lab_request:" + requestId + ":" + eventSuffix + ":fallback"
            );
        }
    }

}

