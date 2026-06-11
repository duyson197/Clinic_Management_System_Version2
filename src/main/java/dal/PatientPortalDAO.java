package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MedicalRecord;
import model.Patient;
import model.PrescriptionItem;

public class PatientPortalDAO extends DBContext {

    private static final String SECTION_HISTORY = "TIỀN SỬ";
    private static final String SECTION_DOCTOR_NOTE = "GHI CHÚ BÁC SĨ";
    private static final String SECTION_TREATMENT_PLAN = "PHƯƠNG ÁN ĐIỀU TRỊ";

    public List<Patient> getPatientsByUserId(int userId) {
        List<Patient> patients = new ArrayList<>();

        String sql = """
                     SELECT patient_id, full_name, phone, dob, email, gender
                                 FROM patients
                                 WHERE user_id = ?
                                 ORDER BY full_name
                             """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getLong("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patient.setPhone(rs.getString("phone"));
                patient.setDob(rs.getDate("dob"));
                patient.setEmail(rs.getString("email"));
                patient.setGender(rs.getString("gender"));
                patients.add(patient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patients;
    }

    public List<MedicalRecord> getMedicalRecordsByUserId(int userId, Long patientId) {
        List<MedicalRecord> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                p.patient_id,
                p.full_name AS patient_name,
                a.appointment_id,
                a.appointment_date,
                a.appointment_time,
                COALESCE(du.full_name, 'Chưa cập nhật') AS doctor_name,
                COALESCE(mr.symptoms, a.symptom) AS symptoms,
                mr.diagnosis,
                mr.notes,
                mr.updated_at
            FROM patients p
            JOIN appointments a ON a.patient_id = p.patient_id
            LEFT JOIN doctors d ON d.doctor_id = a.doctor_id
            LEFT JOIN users du ON du.user_id = d.user_id
            JOIN medical_records mr ON mr.appointment_id = a.appointment_id
            WHERE p.user_id = ?
            """);

        if (patientId != null) {
            sql.append(" AND p.patient_id = ? ");
        }

        sql.append(" ORDER BY a.appointment_date DESC, a.appointment_time DESC, mr.updated_at DESC ");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            if (patientId != null) {
                ps.setLong(2, patientId);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalRecord item = new MedicalRecord();
                String notes = rs.getString("notes");
                String history = extractSection(notes, SECTION_HISTORY);

                item.setPatientId(rs.getLong("patient_id"));
                item.setPatientName(rs.getString("patient_name"));
                item.setAppointmentId(rs.getLong("appointment_id"));
                item.setAppointmentDate(rs.getDate("appointment_date"));
                item.setAppointmentTime(rs.getTime("appointment_time"));
                item.setDoctorName(rs.getString("doctor_name"));
                item.setSymptoms(rs.getString("symptoms"));
                item.setDiagnosis(rs.getString("diagnosis"));
                item.setNotes(notes);
                item.setHistory(history);
                item.setHistoryAllergies(extractHistoryLine(history, "Dị ứng"));
                item.setHistoryChronic(extractHistoryLine(history, "Bệnh mãn tính"));
                item.setHistoryFamily(extractHistoryLine(history, "Tiền sử gia đình"));
                item.setHistorySocial(extractHistoryLine(history, "Tiền sử xã hội"));
                item.setHistoryVaccination(extractHistoryLine(history, "Lịch sử tiêm chủng"));
                item.setDoctorNote(extractSection(notes, SECTION_DOCTOR_NOTE));
                item.setTreatmentPlan(extractSection(notes, SECTION_TREATMENT_PLAN));
                item.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

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

    private String extractHistoryLine(String historySection, String label) {
        if (historySection == null || historySection.isBlank()) {
            return "";
        }

        String[] lines = historySection.split("\\R");
        String prefix = label + ":";

        for (String line : lines) {
            String normalized = line.trim();
            if (normalized.startsWith("-")) {
                normalized = normalized.substring(1).trim();
            }

            if (normalized.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return normalized.substring(prefix.length()).trim();
            }
        }

        return "";
    }

    public List<MedicalRecord> getPrescriptionsByUserId(int userId, Long patientId) {
        List<MedicalRecord> list = new ArrayList<>();

        try {
            loadPrescriptionsByRecordSchema(list, userId, patientId);
        } catch (SQLException ignored) {
            list.clear();
        }

        if (!list.isEmpty()) {
            return list;
        }

        try {
            loadPrescriptionsByAppointmentSchema(list, userId, patientId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void loadPrescriptionsByRecordSchema(List<MedicalRecord> list, int userId, Long patientId) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT
                pt.patient_id,
                pt.full_name AS patient_name,
                p.prescription_id,
                '' AS prescription_note,
                p.created_at,
                a.appointment_id,
                a.appointment_date,
                a.appointment_time,
                COALESCE(du.full_name, 'Chưa cập nhật') AS doctor_name,
                COALESCE(mr.diagnosis, '') AS diagnosis
            FROM patients pt
            JOIN appointments a ON a.patient_id = pt.patient_id
            LEFT JOIN medical_records mr ON mr.appointment_id = a.appointment_id
            JOIN prescriptions p ON p.record_id = mr.record_id
            LEFT JOIN doctors d ON d.doctor_id = p.doctor_id
            LEFT JOIN users du ON du.user_id = d.user_id
            WHERE pt.user_id = ?
            """);

        if (patientId != null) {
            sql.append(" AND pt.patient_id = ? ");
        }

        sql.append(" ORDER BY p.created_at DESC ");
        
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindPrescriptionParams(ps, userId, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPrescriptionRecord(rs));
            }
        }
    }

    private void loadPrescriptionsByAppointmentSchema(List<MedicalRecord> list, int userId, Long patientId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT
                pt.patient_id,
                pt.full_name AS patient_name,
                p.prescription_id,
                COALESCE(p.notes, '') AS prescription_note,
                p.created_at,
                a.appointment_id,
                a.appointment_date,
                a.appointment_time,
                COALESCE(du.full_name, 'Chưa cập nhật') AS doctor_name,
                COALESCE(mr.diagnosis, '') AS diagnosis
            FROM patients pt
            JOIN appointments a ON a.patient_id = pt.patient_id
            JOIN prescriptions p ON p.appointment_id = a.appointment_id
            LEFT JOIN medical_records mr ON mr.appointment_id = a.appointment_id
            LEFT JOIN doctors d ON d.doctor_id = p.doctor_id
            LEFT JOIN users du ON du.user_id = d.user_id
            WHERE pt.user_id = ?
        """);

        if (patientId != null) {
            sql.append(" AND pt.patient_id = ? ");
        }

        sql.append(" ORDER BY p.created_at DESC ");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindPrescriptionParams(ps, userId, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPrescriptionRecord(rs));
            }
        }
    }

    private void bindPrescriptionParams(PreparedStatement ps, int userId, Long patientId) throws SQLException {
        ps.setInt(1, userId);
        if (patientId != null) {
            ps.setLong(2, patientId);
        }
    }

    private MedicalRecord mapPrescriptionRecord(ResultSet rs) throws SQLException {
        MedicalRecord item = new MedicalRecord();
        item.setPatientId(rs.getLong("patient_id"));
        item.setPatientName(rs.getString("patient_name"));
        item.setPrescriptionId(rs.getInt("prescription_id"));
        item.setPrescriptionNote(rs.getString("prescription_note"));
        item.setAppointmentId(rs.getLong("appointment_id"));
        item.setAppointmentDate(rs.getDate("appointment_date"));
        item.setAppointmentTime(rs.getTime("appointment_time"));
        item.setDoctorName(rs.getString("doctor_name"));
        item.setDiagnosis(rs.getString("diagnosis"));
        item.setUpdatedAt(rs.getTimestamp("created_at"));
        item.setPrescriptionItems(getPrescriptionItemsByPrescriptionId(item.getPrescriptionId()));
        return item;
    }

    private List<PrescriptionItem> getPrescriptionItemsByPrescriptionId(int prescriptionId) {
        List<PrescriptionItem> list = new ArrayList<>();
        try {
            loadPrescriptionItemsSimpleSchema(list, prescriptionId);
            return list;
        } catch (SQLException ignored) {
            list.clear();
        }

        try {
            loadPrescriptionItemsMedicineSchema(list, prescriptionId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void loadPrescriptionItemsSimpleSchema(List<PrescriptionItem> list, int prescriptionId) throws SQLException {
        String sql = """
            SELECT
                pi.item_id,
                pi.prescription_id,
                pi.medicine_name,
                pi.dosage,
                pi.frequency,
                pi.`duration` AS duration_value
            FROM prescription_items pi
            WHERE pi.prescription_id = ?
            ORDER BY pi.item_id
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setItemId(rs.getInt("item_id"));
                item.setPrescriptionId(rs.getInt("prescription_id"));
                item.setMedicineName(rs.getString("medicine_name"));
                item.setDosage(rs.getString("dosage"));
                item.setFrequency(rs.getString("frequency"));
                item.setDurationDays(rs.getString("duration_value"));
                list.add(item);
            }
        }
    }

    private void loadPrescriptionItemsMedicineSchema(List<PrescriptionItem> list, int prescriptionId) throws SQLException {
        String sql = """
            SELECT
                pi.item_id,
                pi.prescription_id,
                pi.medicine_name,
                pi.dosage,
                pi.frequency,
                pi.duration
            FROM prescription_items pi
            WHERE pi.prescription_id = ?
            ORDER BY pi.item_id
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setItemId(rs.getInt("item_id"));
                item.setPrescriptionId(rs.getInt("prescription_id"));
                item.setMedicineName(rs.getString("medicine_name"));
                item.setDosage(rs.getString("dosage"));
                item.setDurationDays(rs.getString("duration"));
                list.add(item);
            }
        }
    }

    public void editPatient(String patientID, Patient patient) {

        String sql = "UPDATE patients SET full_name=?, phone=?, dob=?, email=?, gender=? WHERE patient_id=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, patient.getFullName());

            if (patient.getPhone() == null || patient.getPhone().isEmpty()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, patient.getPhone());
            }

            ps.setDate(3, patient.getDob());

            if (patient.getEmail() == null || patient.getEmail().isEmpty()) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, patient.getEmail());
            }

            ps.setString(5, patient.getGender());
            ps.setLong(6, Long.parseLong(patientID));

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long addPatient(Patient patient) {

        String sql = "INSERT INTO patients (user_id, full_name, phone, dob, email, gender) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setInt(1, patient.getUserId());
            ps.setString(2, patient.getFullName());

            if (patient.getPhone() == null || patient.getPhone().isEmpty()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, patient.getPhone());
            }

            ps.setDate(4, patient.getDob());

            if (patient.getEmail() == null || patient.getEmail().isEmpty()) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, patient.getEmail());
            }

            ps.setString(6, patient.getGender());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Patient getPatientsByPatientID(long patientID) {

        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, patientID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Patient p = new Patient();

                p.setPatientId(rs.getLong("patient_id"));
                p.setUserId(rs.getInt("user_id"));
                p.setFullName(rs.getString("full_name"));
                p.setPhone(rs.getString("phone"));
                p.setDob(rs.getDate("dob"));
                p.setEmail(rs.getString("email"));
                p.setGender(rs.getString("gender"));

                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    
}
