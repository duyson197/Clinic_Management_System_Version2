package dal;

import model.LabPayment;
import model.LabRequest;
import model.Patient;
import model.Doctor;
import model.Appointment;
import model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Lab Payment - handles payment waiting list for lab tests Uses
 * existing 'payments' table linked via appointment_id
 */
public class LabPaymentDAO extends DBContext {

    /**
     * Get payment waiting list with filters and pagination Only shows payments
     * waiting receptionist confirmation before lab processing
     */
    public List<LabPayment> getPaymentWaitingListWithFilter(String paymentStatus, String searchTerm, int page, int pageSize) {
        List<LabPayment> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                pay.payment_id,
                pay.appointment_id,
                pay.amount,
                pay.method,
                pay.status AS payment_status,
                pay.created_at AS payment_created_at,
                lr.request_id,
                lr.doctor_id,
                lr.status AS lab_status,
                lr.created_at AS lab_created_at,
                p.patient_id,
                p.user_id,
                p.full_name AS patient_name,
                p.phone AS patient_phone,
                p.dob,
                p.email AS patient_email,
                p.gender,
                d.doctor_id,
                u.full_name AS doctor_name,
                a.symptom,
                a.status AS appointment_status
            FROM payments pay
            JOIN lab_requests lr ON lr.request_id = pay.lab_request_id
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE lr.status = 'pending'
              AND pay.status = 'pending'
        """);

        List<Object> params = new ArrayList<>();

        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            sql.append(" AND pay.status = ?");
            params.add(paymentStatus);
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            Integer patientIdSearch = null;
            Integer requestIdSearch = null;
            String upperTerm = searchTerm.trim().toUpperCase();
            if (upperTerm.matches("BN\\d+")) {
                try { patientIdSearch = Integer.parseInt(upperTerm.substring(2)); } catch (NumberFormatException ignored) {}
            }
            java.util.regex.Matcher labMatcher = java.util.regex.Pattern.compile("(?:LAB-\\d+-)(\\d+)").matcher(upperTerm);
            if (labMatcher.find()) {
                try { requestIdSearch = Integer.parseInt(labMatcher.group(1)); } catch (NumberFormatException ignored) {}
            }
            StringBuilder cond = new StringBuilder(" AND (p.full_name LIKE ? OR p.phone LIKE ? OR CONCAT('LAB-', YEAR(lr.created_at), '-', LPAD(lr.request_id, 4, '0')) LIKE ? OR CONCAT('BN', LPAD(p.patient_id, 6, '0')) LIKE ?");
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            if (patientIdSearch != null) {
                cond.append(" OR p.patient_id = ?");
                params.add(patientIdSearch);
            }
            if (requestIdSearch != null) {
                cond.append(" OR lr.request_id = ?");
                params.add(requestIdSearch);
            }
            cond.append(")");
            sql.append(cond);
        }

        sql.append(" ORDER BY pay.status ASC, pay.created_at DESC");
        sql.append(" LIMIT ? OFFSET ?");

        int offset = (page - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                LabPayment payment = mapResultSetToLabPayment(rs);
                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Count total payments with filter
     */
    public int countPaymentsWithFilter(String paymentStatus, String searchTerm) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) as total
            FROM payments pay
            JOIN lab_requests lr ON lr.request_id = pay.lab_request_id
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE lr.status = 'pending'
              AND pay.status = 'pending'
        """);

        List<Object> params = new ArrayList<>();

        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            sql.append(" AND pay.status = ?");
            params.add(paymentStatus);
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            Integer patientIdSearch = null;
            Integer requestIdSearch = null;
            String upperTerm = searchTerm.trim().toUpperCase();
            if (upperTerm.matches("BN\\d+")) {
                try { patientIdSearch = Integer.parseInt(upperTerm.substring(2)); } catch (NumberFormatException ignored) {}
            }
            java.util.regex.Matcher labMatcher = java.util.regex.Pattern.compile("(?:LAB-\\d+-)(\\d+)").matcher(upperTerm);
            if (labMatcher.find()) {
                try { requestIdSearch = Integer.parseInt(labMatcher.group(1)); } catch (NumberFormatException ignored) {}
            }
            StringBuilder cond = new StringBuilder(" AND (p.full_name LIKE ? OR p.phone LIKE ? OR CONCAT('LAB-', YEAR(lr.created_at), '-', LPAD(lr.request_id, 4, '0')) LIKE ? OR CONCAT('BN', LPAD(p.patient_id, 6, '0')) LIKE ?");
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            if (patientIdSearch != null) {
                cond.append(" OR p.patient_id = ?");
                params.add(patientIdSearch);
            }
            if (requestIdSearch != null) {
                cond.append(" OR lr.request_id = ?");
                params.add(requestIdSearch);
            }
            cond.append(")");
            sql.append(cond);
        }

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get payment statistics
     *
     * @return [total, pending, paid]
     */
    public int[] getPaymentStatistics(String searchTerm) {
        int[] stats = new int[3]; // [total, pending, paid]

        StringBuilder sql = new StringBuilder("""
            SELECT
                COUNT(*) as total,
                COALESCE(SUM(CASE WHEN pay.status = 'pending' THEN 1 ELSE 0 END), 0) as pending,
                COALESCE(SUM(CASE WHEN pay.status = 'paid' THEN 1 ELSE 0 END), 0) as paid
            FROM payments pay
            JOIN lab_requests lr ON lr.request_id = pay.lab_request_id
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE lr.status = 'pending'
              AND pay.status = 'pending'
        """);

        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            Integer patientIdSearch = null;
            Integer requestIdSearch = null;
            String upperTerm = searchTerm.trim().toUpperCase();
            if (upperTerm.matches("BN\\d+")) {
                try { patientIdSearch = Integer.parseInt(upperTerm.substring(2)); } catch (NumberFormatException ignored) {}
            }
            java.util.regex.Matcher labMatcher = java.util.regex.Pattern.compile("(?:LAB-\\d+-)(\\d+)").matcher(upperTerm);
            if (labMatcher.find()) {
                try { requestIdSearch = Integer.parseInt(labMatcher.group(1)); } catch (NumberFormatException ignored) {}
            }
            StringBuilder cond = new StringBuilder(" AND (p.full_name LIKE ? OR p.phone LIKE ? OR CONCAT('LAB-', YEAR(lr.created_at), '-', LPAD(lr.request_id, 4, '0')) LIKE ? OR CONCAT('BN', LPAD(p.patient_id, 6, '0')) LIKE ?");
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            if (patientIdSearch != null) {
                cond.append(" OR p.patient_id = ?");
                params.add(patientIdSearch);
            }
            if (requestIdSearch != null) {
                cond.append(" OR lr.request_id = ?");
                params.add(requestIdSearch);
            }
            cond.append(")");
            sql.append(cond);
        }

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("pending");
                stats[2] = rs.getInt("paid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Get payment by ID
     */
    public LabPayment getPaymentById(long paymentId) {
        String sql = """
            SELECT
                pay.payment_id,
                pay.appointment_id,
                pay.amount,
                pay.method,
                pay.status AS payment_status,
                pay.created_at AS payment_created_at,
                lr.request_id,
                lr.doctor_id,
                lr.status AS lab_status,
                lr.created_at AS lab_created_at,
                p.patient_id,
                p.user_id,
                p.full_name AS patient_name,
                p.phone AS patient_phone,
                p.dob,
                p.email AS patient_email,
                p.gender,
                d.doctor_id,
                u.full_name AS doctor_name,
                a.symptom,
                a.status AS appointment_status
            FROM payments pay
            JOIN lab_requests lr ON lr.request_id = pay.lab_request_id
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE pay.payment_id = ?
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, paymentId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return mapResultSetToLabPayment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Confirm payment: mark as paid. Lab queue visibility is controlled by
     * paid-payment filter at LabRequestDAO queries.
     */
    public boolean confirmPayment(long paymentId) {
        String updatePaymentSql = "UPDATE payments SET status = 'paid' WHERE payment_id = ?";

        try {
            int rowsAffected;
            try (PreparedStatement st = connection.prepareStatement(updatePaymentSql)) {
                st.setLong(1, paymentId);
                rowsAffected = st.executeUpdate();
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a payment record for a lab request.
     * Uses lab_request_id to link payment directly to the lab request (not appointment).
     */
    public boolean createLabPayment(long appointmentId, int labRequestId, BigDecimal amount, String method) {
        // Check if payment already exists for this lab request
        String checkSql = "SELECT payment_id FROM payments WHERE lab_request_id = ?";
        try (PreparedStatement checkSt = connection.prepareStatement(checkSql)) {
            checkSt.setInt(1, labRequestId);
            ResultSet rs = checkSt.executeQuery();
            if (rs.next()) {
                return true; // already created
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String sql = "INSERT INTO payments (appointment_id, lab_request_id, amount, method, status, created_at) VALUES (?, ?, ?, ?, 'pending', NOW())";

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, appointmentId);
            st.setInt(2, labRequestId);
            st.setBigDecimal(3, amount);
            st.setString(4, method != null ? method : "cash");
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get default lab test price from service_prices table
     */
    public BigDecimal getLabTestPrice() {
        String sql = "SELECT price FROM service_prices WHERE service_type = 'lab' LIMIT 1";

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Default price if not found in database
        return new BigDecimal("150000");
    }

    /**
     * Map ResultSet to LabPayment object
     */
    private LabPayment mapResultSetToLabPayment(ResultSet rs) throws SQLException {
        LabPayment payment = new LabPayment();
        payment.setPaymentId(rs.getLong("payment_id"));
        payment.setAppointmentId(rs.getLong("appointment_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setMethod(rs.getString("method"));
        payment.setStatus(rs.getString("payment_status"));
        payment.setCreatedAt(rs.getTimestamp("payment_created_at"));

        // Map LabRequest
        LabRequest labRequest = new LabRequest();
        labRequest.setRequestId(rs.getInt("request_id"));
        labRequest.setAppointmentId(rs.getLong("appointment_id"));
        labRequest.setDoctorId(rs.getInt("doctor_id"));
        labRequest.setStatus(rs.getString("lab_status"));
        labRequest.setCreatedAt(rs.getTimestamp("lab_created_at"));

        // Map Patient
        Patient patient = new Patient();
        patient.setPatientId(rs.getLong("patient_id"));
        patient.setUserId(rs.getInt("user_id"));
        patient.setFullName(rs.getString("patient_name"));
        patient.setPhone(rs.getString("patient_phone"));
        patient.setDob(rs.getDate("dob"));
        patient.setEmail(rs.getString("patient_email"));
        patient.setGender(rs.getString("gender"));
        labRequest.setPatient(patient);

        // Map Doctor
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getInt("doctor_id"));
        doctor.setFullName(rs.getString("doctor_name"));
        labRequest.setDoctor(doctor);

        // Map Appointment
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getLong("appointment_id"));
        appointment.setSymptom(rs.getString("symptom"));
        appointment.setStatus(rs.getString("appointment_status"));
        labRequest.setAppointment(appointment);

        payment.setLabRequest(labRequest);

        return payment;
    }
}
