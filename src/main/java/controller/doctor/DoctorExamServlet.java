/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.doctor;

import dal.DoctorDAO;
import dal.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Doctor;
import model.ExaminationHistoryItem;
import model.ExamLabItem;
import model.DoctorQueueItem;
import model.User;
import model.MedicalRecord;
import model.Medicine;
import model.PrescriptionItem;
import util.SystemLogService;

/**
 *
 * @author anngu
 */
public class DoctorExamServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorExamServlet.class.getName());

    private static final String SECTION_HISTORY = "TIỀN SỬ";
    private static final String SECTION_CLINICAL_RESULT = "KẾT QUẢ KHÁM LÂM SÀNG";
    private static final String SECTION_DOCTOR_NOTE = "GHI CHÚ BÁC SĨ";
    private static final String SECTION_TREATMENT_PLAN = "PHƯƠNG ÁN ĐIỀU TRỊ";
    private static final String SECTION_LAB_REQUEST = "YÊU CẦU XÉT NGHIỆM";
    
    public static final class LabRequestDraft {
        private final String testType;
        private final String priority;
        private final String collectionMethod;
        private final String note;

        private LabRequestDraft(String testType, String priority, String collectionMethod, String note) {
            this.testType = testType;
            this.priority = priority;
            this.collectionMethod = collectionMethod;
            this.note = note;
        }
        
        public String getTestType() {
            return testType;
        }

        public String getPriority() {
            return priority;
        }

        public String getCollectionMethod() {
            return collectionMethod;
        }

        public String getNote() {
            return note;
        }
    }
    
    private DoctorQueueItem resolveQueueForSequentialExam(DoctorDAO doctorDAO, int doctorId, Long requestedAppointmentId) {
        DoctorQueueItem currentExamining = doctorDAO.getCurrentExaminingQueueItem(doctorId);
        if (currentExamining != null) {
            if (requestedAppointmentId == null || requestedAppointmentId == currentExamining.getAppointmentId()) {
                return currentExamining;
            }
            return null;
        }

        DoctorQueueItem nextWaiting = doctorDAO.getNextWaitingQueueItem(doctorId);
        if (nextWaiting == null) {
            return null;
        }

        if (requestedAppointmentId != null && requestedAppointmentId != nextWaiting.getAppointmentId()) {
            return null;
        }

        doctorDAO.startExamination(nextWaiting.getAppointmentId());
        nextWaiting.setStatus("examining");
        return nextWaiting;
    }
    
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

    // validateDoctor:
    // Xác thực session + role doctor + profile bác sĩ hợp lệ.
    // Đọc session/account, kiểm role, sau đó đối chiếu doctor theo userId.
    private Doctor validateDoctor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return null;
        }

        User account = (User) session.getAttribute("account");
        if (account == null || account.getRole() == null || !"doctor".equalsIgnoreCase(account.getRole().name())) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return null;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        Doctor doctor = doctorDAO.getDoctorByUserId(account.getUserId());
        if (doctor == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return null;
        }

        session.setAttribute("doctorName", doctor.getFullName());
        return doctor;
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
        // Nạp dữ liệu của appointment
        // validate doctor -> validate appointmentId -> kiểm tra queue ownership
        //  chuyển waiting thành examining -> nạp medical record/lab/history -> forward exam.jsp.
        try {
            Doctor doctor = validateDoctor(request, response);
            if (doctor == null) {
                return;
            }

            String appointmentParam = request.getParameter("appointmentId");
            
            Long appointmentId = null;
            if (appointmentParam != null && !appointmentParam.trim().isEmpty()) {
                try {
                    appointmentId = Long.parseLong(appointmentParam.trim());
                } catch (NumberFormatException ex) {
                    request.setAttribute("pageError", "Mã lịch khám không hợp lệ.");
                    request.getRequestDispatcher("/pages/examination/exam.jsp").forward(request, response);
                    return;
                }
            }

            DoctorDAO doctorDAO = new DoctorDAO();
            DoctorQueueItem examData = resolveQueueForSequentialExam(doctorDAO, doctor.getDoctorId(), appointmentId);
            if (examData == null) {
                request.setAttribute("pageError", "Chỉ được khám tuần tự theo thứ tự hàng đợi. Vui lòng bắt đầu từ bệnh nhân đầu danh sách.");
                request.getRequestDispatcher("/pages/examination/exam.jsp").forward(request, response);
                return;
            }

            long resolvedAppointmentId = examData.getAppointmentId();

            populateExamPageAttributes(request, doctorDAO, examData);
            String activeTab = cleanText(request.getParameter("tab"));
            if (activeTab.isEmpty()) {
                activeTab = "info";
            }
            request.setAttribute("activeTab", activeTab);
            request.setAttribute("success", request.getParameter("success"));
            request.setAttribute("error", request.getParameter("error"));
            request.getRequestDispatcher("/pages/examination/exam.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load doctor exam screen.", ex);
            request.setAttribute("pageError", "Đã xảy ra lỗi khi tải màn hình khám bệnh. Vui lòng thử lại.");
            request.getRequestDispatcher("/pages/examination/exam.jsp").forward(request, response);
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
        // Ghi bệnh án, đóng phiên khám hoặc chuyển phiên khám sang luồng xét nghiệm.
        // Validate input bắt buộc theo action rồi gọi DAO transactional tương ứng.
        Doctor doctor = validateDoctor(request, response);
        if (doctor == null) {
            return;
        }

        String appointmentParam = request.getParameter("appointmentId");
        String action = request.getParameter("action");
        if (appointmentParam == null || appointmentParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctorDashboard?error=missingAppointment");
            return;
        }

        long appointmentId;
        try {
            appointmentId = Long.parseLong(appointmentParam.trim());
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/doctorDashboard?error=invalidAppointment");
            return;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        DoctorQueueItem examData = resolveQueueForSequentialExam(doctorDAO, doctor.getDoctorId(), appointmentId);
        if (examData == null) {
            response.sendRedirect(request.getContextPath() + "/doctorDashboard?error=notAllowedQueueOrder");
            return;
        }

        String symptoms = cleanText(request.getParameter("symptoms"));
        String diagnosis = cleanText(request.getParameter("diagnosis"));

        String allergies = cleanText(request.getParameter("historyAllergies"));
        String chronic = cleanText(request.getParameter("historyChronic"));
        String family = cleanText(request.getParameter("historyFamily"));
        String social = cleanText(request.getParameter("historySocial"));
        String vaccination = cleanText(request.getParameter("historyVaccination"));

        String clinicalResult = cleanText(request.getParameter("clinicalResult"));
        String doctorNote = cleanText(request.getParameter("doctorNote"));
        String treatmentPlan = cleanText(request.getParameter("treatmentPlan"));
         List<LabRequestDraft> labRequestDrafts = parseLabRequestDrafts(request);

        String labRequestInstruction = cleanText(request.getParameter("labRequestInstruction"));
        if ("createLabRequest".equalsIgnoreCase(action)) {
            labRequestInstruction = buildLabRequestInstruction(labRequestDrafts);
        }
        String requiredFieldError = validateRequiredFields(action, diagnosis, clinicalResult, treatmentPlan,
                labRequestDrafts);
        if (!requiredFieldError.isEmpty()) {
            String errorTab = "createLabRequest".equalsIgnoreCase(action) ? "lab" : "info";
            forwardExamWithDraftData(request, response, doctorDAO, examData, errorTab, requiredFieldError,
                    symptoms, diagnosis, allergies, chronic, family, social, vaccination,
                    clinicalResult, doctorNote, treatmentPlan, labRequestDrafts);
            return;
        }

        String notes = buildMedicalRecordNote(allergies, chronic, family, social, vaccination, clinicalResult, doctorNote, treatmentPlan, labRequestInstruction);

        if ("savePrescription".equalsIgnoreCase(action)) {
            if (doctorDAO.hasIncompleteLabRequests(appointmentId)) {
                response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&tab=prescription&error=incompleteLabResults");
                return;
            }
            
            List<PrescriptionItem> prescriptionItems = parsePrescriptionItems(request);
            if (prescriptionItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&tab=prescription&error=emptyPrescription");
                return;
            }

            String prescriptionNote = cleanText(request.getParameter("prescriptionNote"));
            boolean prescriptionSaved = doctorDAO.savePrescription(appointmentId, doctor.getDoctorId(), prescriptionNote, prescriptionItems);
            if (!prescriptionSaved) {
                response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&tab=prescription&error=savePrescriptionFailed");
                return;
            }

            HttpSession sessionForLog = request.getSession(false);
            Integer logUserId = sessionForLog != null ? ((User) sessionForLog.getAttribute("account") != null ? ((User) sessionForLog.getAttribute("account")).getUserId() : null) : null;
            SystemLogService.log(logUserId, "PRESCRIPTION_SAVED",
                    "Lưu đơn thuốc: appointmentId=" + appointmentId + ", doctorId=" + doctor.getDoctorId());
            response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&tab=prescription&success=prescriptionSaved");
            return;
        }

        if ("finish".equalsIgnoreCase(action)) {
            boolean finished = doctorDAO.saveMedicalRecordAndFinishExamination(appointmentId, symptoms, diagnosis, notes);
             if (!finished) {
                response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&error=saveFailed");
                return;
            }
            SystemLogService.log(doctor.getUserId(), "EXAM_FINISHED",
                    "Hoàn tất khám bệnh: appointmentId=" + appointmentId + ", diagnosis=" + diagnosis);
            NotificationDAO notificationDAO = new NotificationDAO();
            String patientName = examData.getPatientName() == null ? "Bệnh nhân" : examData.getPatientName().trim();
            notificationDAO.createNotificationForAppointment(
                    appointmentId,
                    "Khám bệnh đã hoàn tất",
                    "Đã khám xong cho bệnh nhân " + patientName + ". Nhấn vào thông báo để xem hồ sơ bệnh án vừa hoàn tất.",
                    "examination_completed",
                    "appointment:" + appointmentId + ":exam_done"
            );
            DoctorQueueItem nextWaiting = doctorDAO.getNextWaitingQueueItem(doctor.getDoctorId());
            if (nextWaiting != null) {
                response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + nextWaiting.getAppointmentId() + "&success=examFinished");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/doctorDashboard?success=examFinished");
            return;
        }

        if ("createLabRequest".equalsIgnoreCase(action)) {
            if ("done".equalsIgnoreCase(examData.getStatus())) {
                forwardExamWithDraftData(request, response, doctorDAO, examData, "lab", "labRequestNotAllowed",
                        symptoms, diagnosis, allergies, chronic, family, social, vaccination,
                        clinicalResult, doctorNote, treatmentPlan, labRequestDrafts);
                return;
            }

            int createdCount = doctorDAO.saveMedicalRecordAndCreateLabRequests(
                    appointmentId,
                    doctor.getDoctorId(),
                    symptoms,
                    diagnosis,
                    notes,
                    labRequestDrafts.size());
            if (createdCount > 0) { 
                SystemLogService.log(doctor.getUserId(), "LAB_REQUEST_CREATED",
                        "Tạo yêu cầu xét nghiệm: appointmentId=" + appointmentId + ", quantity=" + createdCount);
                response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&tab=lab&success=labRequestedMultiple");
                return;
            }
            forwardExamWithDraftData(request, response, doctorDAO, examData, "lab", "labRequestFailed",
                    symptoms, diagnosis, allergies, chronic, family, social, vaccination,
                    clinicalResult, doctorNote, treatmentPlan, labRequestDrafts);
            return;
        }

        boolean saved = doctorDAO.upsertMedicalRecord(appointmentId, symptoms, diagnosis, notes);
        if (!saved) {
            response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&error=saveFailed");
            return;
        }

        SystemLogService.log(doctor.getUserId(), "MEDICAL_RECORD_SAVED",
                "Lưu hồ sơ bệnh án: appointmentId=" + appointmentId);
        response.sendRedirect(request.getContextPath() + "/doctor/exam?appointmentId=" + appointmentId + "&success=saved");
    }

    private void populateExamPageAttributes(HttpServletRequest request, DoctorDAO doctorDAO, DoctorQueueItem examData) {
        long appointmentId = examData.getAppointmentId();
        request.setAttribute("examData", examData);
        List<ExamLabItem> labResults = doctorDAO.getLabResultsByAppointment(appointmentId);
        request.setAttribute("labResults", labResults);
        request.setAttribute("canSavePrescription", canSavePrescriptionByLabStatus(labResults));
        MedicalRecord medicalRecord = doctorDAO.getMedicalRecordByAppointment(appointmentId);
        request.setAttribute("medicalRecord", medicalRecord);

        String notes = medicalRecord != null ? medicalRecord.getNotes() : null;
        request.setAttribute("historyAllergies", extractHistoryLine(notes, "Dị ứng"));
        request.setAttribute("historyChronic", extractHistoryLine(notes, "Bệnh mạn tính"));
        request.setAttribute("historyFamily", extractHistoryLine(notes, "Tiền sử gia đình"));
        request.setAttribute("historySocial", extractHistoryLine(notes, "Tiền sử xã hội"));
        request.setAttribute("historyVaccination", extractHistoryLine(notes, "Lịch sử tiêm chủng"));
        request.setAttribute("clinicalResult", extractSection(notes, SECTION_CLINICAL_RESULT));
        request.setAttribute("doctorNote", extractSection(notes, SECTION_DOCTOR_NOTE));
        request.setAttribute("treatmentPlan", extractSection(notes, SECTION_TREATMENT_PLAN));
        request.setAttribute("labRequestInstruction", extractSection(notes, SECTION_LAB_REQUEST));

        List<PrescriptionItem> prescriptionItems = doctorDAO.getPrescriptionItemsByAppointment(appointmentId);
        request.setAttribute("prescriptionItems", prescriptionItems);
        List<Medicine> medicineList = doctorDAO.getAllMedicines();
        request.setAttribute("medicineList", medicineList);
        List<ExaminationHistoryItem> examinationHistory
                = doctorDAO.getExaminationHistoryByAppointment(appointmentId);
        request.setAttribute("historyList", examinationHistory);
    }

    private void forwardExamWithDraftData(HttpServletRequest request, HttpServletResponse response,
            DoctorDAO doctorDAO, DoctorQueueItem examData, String activeTab, String errorCode,
            String symptoms, String diagnosis, String allergies, String chronic, String family, String social,
            String vaccination, String clinicalResult, String doctorNote, String treatmentPlan,
            List<LabRequestDraft> labRequestDrafts) throws ServletException, IOException {
        populateExamPageAttributes(request, doctorDAO, examData);
        request.setAttribute("activeTab", activeTab);
        request.setAttribute("error", errorCode);
        request.setAttribute("formSymptoms", symptoms);
        request.setAttribute("formDiagnosis", diagnosis);
        request.setAttribute("historyAllergies", allergies);
        request.setAttribute("historyChronic", chronic);
        request.setAttribute("historyFamily", family);
        request.setAttribute("historySocial", social);
        request.setAttribute("historyVaccination", vaccination);
        request.setAttribute("clinicalResult", clinicalResult);
        request.setAttribute("doctorNote", doctorNote);
        request.setAttribute("treatmentPlan", treatmentPlan);
        request.setAttribute("labRequestDrafts", labRequestDrafts);
        request.getRequestDispatcher("/pages/examination/exam.jsp").forward(request, response);
    }
    
    private List<PrescriptionItem> parsePrescriptionItems(HttpServletRequest request) {
        List<PrescriptionItem> items = new java.util.ArrayList<>();

        String[] medicineIds = request.getParameterValues("medicineId");
        String[] medicineNames = request.getParameterValues("medicineName");
        String[] dosages = request.getParameterValues("dosage");
        String[] frequencies = request.getParameterValues("frequency");
        String[] durations = request.getParameterValues("durationDays");

        if (medicineIds == null && medicineNames == null) {
            return items;
        }

        int rowCount = medicineIds != null ? medicineIds.length : medicineNames.length;
        for (int i = 0; i < rowCount; i++) {
            String medicineIdRaw = getArrayValue(medicineIds, i);
            String medicineNameRaw = getArrayValue(medicineNames, i);

            PrescriptionItem item = new PrescriptionItem();
            item.setMedicineName(medicineNameRaw);

            if (!medicineIdRaw.isEmpty()) {
                try {
                    item.setMedicineId(Integer.parseInt(medicineIdRaw));
                } catch (NumberFormatException ex) {
                    item.setMedicineId(0);
                }
            }

            if (item.getMedicineId() <= 0 && medicineNameRaw.isEmpty()) {
                continue;
            }

            item.setDosage(getArrayValue(dosages, i));
            item.setFrequency(getArrayValue(frequencies, i));
            item.setDurationDays(getArrayValue(durations, i));
            items.add(item);
        }

        return items;
    }

    private String getArrayValue(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) {
            return "";
        }
        return cleanText(values[index]);
    }

    private List<LabRequestDraft> parseLabRequestDrafts(HttpServletRequest request) {
        List<LabRequestDraft> drafts = new ArrayList<>();

        String[] testTypes = request.getParameterValues("labTestType");
        String[] priorities = request.getParameterValues("labPriority");
        String[] collectionMethods = request.getParameterValues("labCollectionMethod");
        String[] notes = request.getParameterValues("labRequestItemNote");

        if (testTypes == null && priorities == null && collectionMethods == null && notes == null) {
            return drafts;
        }

        int rowCount = maxLength(testTypes, priorities, collectionMethods, notes);
        for (int i = 0; i < rowCount; i++) {
            String testType = getArrayValue(testTypes, i);
            String priority = getArrayValue(priorities, i);
            String collectionMethod = getArrayValue(collectionMethods, i);
            String note = getArrayValue(notes, i);

            if (testType.isEmpty() && priority.isEmpty() && collectionMethod.isEmpty() && note.isEmpty()) {
                continue;
            }
            drafts.add(new LabRequestDraft(testType, priority, collectionMethod, note));
        }

        return drafts;
    }

    private int maxLength(String[]... arrays) {
        int max = 0;
        if (arrays == null) {
            return max;
        }

        for (String[] array : arrays) {
            if (array != null && array.length > max) {
                max = array.length;
            }
        }
        return max;
    }
    
    // Chuẩn hóa dữ liệu nhập cho form khám.
    // Giải quyết null và khoảng trắng dư để tránh lỗi validate/ghi DB.
    // null -> "", còn lại trim().
    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // Ngăn submit khi thiếu thông tin.
    // Kiểm tra các field bắt buộc.
    private String validateRequiredFields(String action,
            String diagnosis,
            String clinicalResult,
            String treatmentPlan,
            List<LabRequestDraft> labRequestDrafts) {
        if ("finish".equalsIgnoreCase(action)) {
            if (diagnosis.isEmpty() || clinicalResult.isEmpty() || treatmentPlan.isEmpty()) {
                return "missingRequiredFinishFields";
            }
        }

        if ("createLabRequest".equalsIgnoreCase(action)) {
            if (diagnosis.isEmpty() || clinicalResult.isEmpty() || labRequestDrafts.isEmpty()) {
                return "missingRequiredLabFields";
            }
            
            for (LabRequestDraft draft : labRequestDrafts) {
                if (draft.testType.isEmpty() || draft.priority.isEmpty() || draft.collectionMethod.isEmpty()) {
                    return "missingRequiredLabFields";
                }
            }
        }

        return "";
    }

    private String buildLabRequestInstruction(List<LabRequestDraft> labRequestDrafts) {
        if (labRequestDrafts == null || labRequestDrafts.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labRequestDrafts.size(); i++) {
            LabRequestDraft draft = labRequestDrafts.get(i);
            builder.append("[").append(i + 1).append("] ").append(draft.testType).append("\n");
            builder.append("- Mức độ ưu tiên: ").append(draft.priority).append("\n");
            builder.append("- Hình thức lấy mẫu: ").append(draft.collectionMethod).append("\n");
            builder.append("- Trạng thái kết quả: Chưa có kết quả");
            if (!draft.note.isEmpty()) {
                builder.append("\n- Ghi chú chỉ định: ").append(draft.note);
            }
            if (i < labRequestDrafts.size() - 1) {
                builder.append("\n\n");
            }
        }

        return builder.toString().trim();
    }

    private boolean canSavePrescriptionByLabStatus(List<ExamLabItem> labResults) {
        if (labResults == null || labResults.isEmpty()) {
            return true;
        }

        for (ExamLabItem lab : labResults) {
            if (lab == null) {
                continue;
            }
            String status = cleanText(lab.getStatus());
            if (!"completed".equalsIgnoreCase(status)) {
                return false;
            }
        }

        return true;
    }
    
    // Gộp (tiền sử, kết quả khám, ghi chú...) thành 1 field "notes.
    // Dùng section header dạng [SECTION] + nội dung tương ứng, tách ra để parse ngược khi tải lại. 
    private String buildMedicalRecordNote(
            String allergies,
            String chronic,
            String family,
            String social,
            String vaccination,
            String clinicalResult,
            String doctorNote,
            String treatmentPlan,
            String labRequestInstruction
    ) {
        StringBuilder sb = new StringBuilder();

        if (!allergies.isEmpty() || !chronic.isEmpty() || !family.isEmpty() || !social.isEmpty() || !vaccination.isEmpty()) {
            sb.append("[").append(SECTION_HISTORY).append("]\n");
            if (!allergies.isEmpty()) {
                sb.append("- Dị ứng: ").append(allergies).append("\n");
            }
            if (!chronic.isEmpty()) {
                sb.append("- Bệnh mãn tính: ").append(chronic).append("\n");
            }
            if (!family.isEmpty()) {
                sb.append("- Tiền sử gia đình: ").append(family).append("\n");
            }
            if (!social.isEmpty()) {
                sb.append("- Tiền sử xã hội: ").append(social).append("\n");
            }
            if (!vaccination.isEmpty()) {
                sb.append("- Lịch sử tiêm chủng: ").append(vaccination).append("\n");
            }
            sb.append("\n");
        }

        appendSection(sb, SECTION_CLINICAL_RESULT, clinicalResult);
        appendSection(sb, SECTION_DOCTOR_NOTE, doctorNote);
        appendSection(sb, SECTION_TREATMENT_PLAN, treatmentPlan);
        appendSection(sb, SECTION_LAB_REQUEST, labRequestInstruction);

        return sb.toString().trim();
    }

    // Chỉ thêm section khi có nội dung, tránh sinh section rỗng
    private void appendSection(StringBuilder sb, String title, String value) {
        if (value.isEmpty()) {
            return;
        }

        sb.append("[").append(title).append("]\n");
        sb.append(value).append("\n\n");
    }

    // Tách nội dung của một section cụ thể theo marker [SECTION].
    // Tìm vị trí marker hiện tại và marker kế tiếp -> substring phần nội dung.
    private String extractSection(String notes, String sectionTitle) {
        if (notes == null || notes.isBlank()) {
            return "";
        }

        String marker = "[" + sectionTitle + "]";
        int start = notes.indexOf(marker);
        if (start < 0) {
            return "";
        }

        int contentStart = start + marker.length();
        while (contentStart < notes.length() && (notes.charAt(contentStart) == '\n' || notes.charAt(contentStart) == '\r')) {
            contentStart++;
        }

        int end = notes.length();
        int nextMarker = notes.indexOf("[", contentStart);
        while (nextMarker >= 0) {
            int close = notes.indexOf("]", nextMarker);
            if (close > nextMarker) {
                end = nextMarker;
                break;
            }
            nextMarker = notes.indexOf("[", nextMarker + 1);
        }

        return notes.substring(contentStart, end).trim();
    }

    // Giải quyết khả năng notes có nhiều định dạng (trong section TIỀN SỬ hoặc text cũ).
    // Uu tiên parse trong section TIỀN SỬ, fallback parse toàn notes.
    private String extractHistoryLine(String notes, String label) {
        if (notes == null || notes.isBlank()) {
            return "";
        }

        String targetLabel = normalizeHistoryLabel(label);
        String historySection = extractSection(notes, SECTION_HISTORY);
        String value = extractHistoryLineFromBlock(historySection, targetLabel);
        if (!value.isEmpty()) {
            return value;
        }

        value = extractHistoryLineFromBlock(notes, targetLabel);
        if (!value.isEmpty()) {
            return value;
        }

        if (!notes.contains("[") && !notes.contains(":") && !notes.contains("=")) {
            return notes.trim();
        }

        return "";
    }

    // Các biến thể định dạng dòng như "- label: value", "label=value", ký tự ':' fullwidth.
    // Tách dòng, loại bullet, tìm separator và so khớp nhãn đã normalize.
    private String extractHistoryLineFromBlock(String block, String targetLabel) {
        if (block == null || block.isBlank()) {
            return "";
        }

        String[] lines = block.split("\\R");
        for (String line : lines) {
            String normalized = line.trim();
            if (normalized.isEmpty()) {
                continue;
            }

            while (normalized.startsWith("-") || normalized.startsWith("•") || normalized.startsWith("*")) {
                normalized = normalized.substring(1).trim();
            }

            int separator = normalized.indexOf(':');
            if (separator < 0) {
                separator = normalized.indexOf('=');
            }
            if (separator < 0) {
                separator = normalized.indexOf('：');
            }
            if (separator < 0) {
                continue;
            }

            String currentLabel = normalized.substring(0, separator).trim();
            if (normalizeHistoryLabel(currentLabel).equals(targetLabel)) {
                return normalized.substring(separator + 1).trim();
            }
        }
        return "";
    }

    // Giải quyết khác biệt có/không dấu, hoa/thường.
    // Unicode normalize, bỏ dấu, lowercase, trim.
    private String normalizeHistoryLabel(String label) {
        if (label == null) {
            return "";
        }

        String withoutAccent = Normalizer.normalize(label, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccent.toLowerCase().trim();
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
