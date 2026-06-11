package controller.medicalRecord;

import dal.PatientPortalDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.MedicalRecord;
import model.Patient;
import model.User;

public class PatientHealthDashboardServlet extends HttpServlet {

    private static final String VIEW_RECORD = "record";
    private static final String VIEW_PRESCRIPTION = "prescription";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User account = (User) session.getAttribute("account");
        if (account == null || account.getRole() == null || !"patient".equalsIgnoreCase(account.getRole().name())) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        PatientPortalDAO patientPortalDAO = new PatientPortalDAO();
        List<Patient> patients = patientPortalDAO.getPatientsByUserId(account.getUserId());

        Long selectedPatientId = parseId(request.getParameter("patientId"));
        if (selectedPatientId != null && !isPatientOwnedByAccount(patients, selectedPatientId)) {
            selectedPatientId = null;
        }

        List<MedicalRecord> records = patientPortalDAO.getMedicalRecordsByUserId(account.getUserId(), selectedPatientId);
        List<MedicalRecord> prescriptions = patientPortalDAO.getPrescriptionsByUserId(account.getUserId(), selectedPatientId);

        Map<Long, MedicalRecord> appointmentMap = new LinkedHashMap<>();
        collectAppointmentMap(records, appointmentMap);
        collectAppointmentMap(prescriptions, appointmentMap);
        List<MedicalRecord> appointments = new ArrayList<>(appointmentMap.values());

        Long selectedAppointmentId = parseId(request.getParameter("appointmentId"));
        if (selectedAppointmentId != null && !appointmentMap.containsKey(selectedAppointmentId)) {
            selectedAppointmentId = null;
        }

        MedicalRecord selectedRecord = findByAppointmentId(records, selectedAppointmentId);
        MedicalRecord selectedPrescription = findByAppointmentId(prescriptions, selectedAppointmentId);

        String selectedView = normalizeView(request.getParameter("view"));

        request.setAttribute("patients", patients);
        request.setAttribute("appointments", appointments);
        request.setAttribute("selectedPatientId", selectedPatientId);
        request.setAttribute("selectedAppointmentId", selectedAppointmentId);
        request.setAttribute("selectedRecord", selectedRecord);
        request.setAttribute("selectedPrescription", selectedPrescription);
        request.setAttribute("selectedView", selectedView);

        request.getRequestDispatcher("/pages/profile/patientHealthDashboard.jsp").forward(request, response);
    }

    private String normalizeView(String rawView) {
        if (VIEW_PRESCRIPTION.equalsIgnoreCase(rawView)) {
            return VIEW_PRESCRIPTION;
        }
        return VIEW_RECORD;
    }

    private void collectAppointmentMap(List<MedicalRecord> source, Map<Long, MedicalRecord> appointmentMap) {
        for (MedicalRecord item : source) {
            if (!appointmentMap.containsKey(item.getAppointmentId())) {
                appointmentMap.put(item.getAppointmentId(), item);
            }
        }
    }

    private MedicalRecord findByAppointmentId(List<MedicalRecord> source, Long appointmentId) {
        if (appointmentId == null) {
            return null;
        }

        for (MedicalRecord item : source) {
            if (item.getAppointmentId() == appointmentId) {
                return item;
            }
        }

        return null;
    }

    private Long parseId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }

        try {
            long value = Long.parseLong(rawId);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isPatientOwnedByAccount(List<Patient> patients, long selectedPatientId) {
        for (Patient patient : patients) {
            if (patient.getPatientId() == selectedPatientId) {
                return true;
            }
        }
        return false;
    }
}