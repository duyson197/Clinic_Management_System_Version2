package dal;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportDAO extends DBContext {

    private void syncDoctorRowsForAllDoctorUsers() {
        String sql = """
            INSERT INTO doctors (user_id, specialization)
            SELECT u.user_id, 'Chua cap nhat'
            FROM users u
            LEFT JOIN doctors d ON d.user_id = u.user_id
            WHERE u.role = 'doctor' AND d.doctor_id IS NULL
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int[] getAppointmentStatusStats() {
        return getAppointmentStatusStats(0);
    }

    public int[] getAppointmentStatusStats(int doctorId) {
        int[] stats = new int[6]; // [total, booked, checked_in, waiting, completed, cancelled]
        String sql = doctorId > 0
            ? """
                SELECT
                    COUNT(*) AS total,
                    COALESCE(SUM(CASE WHEN status = 'booked' THEN 1 ELSE 0 END), 0) AS booked,
                    COALESCE(SUM(CASE WHEN status = 'checked_in' THEN 1 ELSE 0 END), 0) AS checked_in,
                    COALESCE(SUM(CASE WHEN status = 'waiting' THEN 1 ELSE 0 END), 0) AS waiting,
                    COALESCE(SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END), 0) AS completed,
                    COALESCE(SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END), 0) AS cancelled
                FROM appointments
                WHERE doctor_id = ?
              """
            : """
                SELECT
                    COUNT(*) AS total,
                    COALESCE(SUM(CASE WHEN status = 'booked' THEN 1 ELSE 0 END), 0) AS booked,
                    COALESCE(SUM(CASE WHEN status = 'checked_in' THEN 1 ELSE 0 END), 0) AS checked_in,
                    COALESCE(SUM(CASE WHEN status = 'waiting' THEN 1 ELSE 0 END), 0) AS waiting,
                    COALESCE(SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END), 0) AS completed,
                    COALESCE(SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END), 0) AS cancelled
                FROM appointments
              """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (doctorId > 0) st.setInt(1, doctorId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("booked");
                stats[2] = rs.getInt("checked_in");
                stats[3] = rs.getInt("waiting");
                stats[4] = rs.getInt("completed");
                stats[5] = rs.getInt("cancelled");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public int[] getLabRequestStatusStats() {
        return getLabRequestStatusStats(0);
    }

    public int[] getLabRequestStatusStats(int doctorId) {
        int[] stats = new int[5]; // [total, pending, processing, completed, cancelled]
        String sql = doctorId > 0
            ? """
                SELECT
                    COUNT(*) AS total,
                    COALESCE(SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END), 0) AS pending,
                    COALESCE(SUM(CASE WHEN status = 'processing' THEN 1 ELSE 0 END), 0) AS processing,
                    COALESCE(SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END), 0) AS completed,
                    COALESCE(SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END), 0) AS cancelled
                FROM lab_requests
                WHERE doctor_id = ?
              """
            : """
                SELECT
                    COUNT(*) AS total,
                    COALESCE(SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END), 0) AS pending,
                    COALESCE(SUM(CASE WHEN status = 'processing' THEN 1 ELSE 0 END), 0) AS processing,
                    COALESCE(SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END), 0) AS completed,
                    COALESCE(SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END), 0) AS cancelled
                FROM lab_requests
              """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (doctorId > 0) st.setInt(1, doctorId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("pending");
                stats[2] = rs.getInt("processing");
                stats[3] = rs.getInt("completed");
                stats[4] = rs.getInt("cancelled");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public BigDecimal[] getPaymentSummary() {
        return getPaymentSummary(0);
    }

    public BigDecimal[] getPaymentSummary(int doctorId) {
        BigDecimal[] summary = new BigDecimal[3]; // [totalAmount, paidAmount, pendingAmount]
        String sql = doctorId > 0
            ? """
                SELECT
                    COALESCE(SUM(p.amount), 0) AS totalAmount,
                    COALESCE(SUM(CASE WHEN p.status = 'paid' THEN p.amount ELSE 0 END), 0) AS paidAmount,
                    COALESCE(SUM(CASE WHEN p.status = 'pending' THEN p.amount ELSE 0 END), 0) AS pendingAmount
                FROM payments p
                JOIN appointments a ON p.appointment_id = a.appointment_id
                WHERE a.doctor_id = ?
              """
            : """
                SELECT
                    COALESCE(SUM(amount), 0) AS totalAmount,
                    COALESCE(SUM(CASE WHEN status = 'paid' THEN amount ELSE 0 END), 0) AS paidAmount,
                    COALESCE(SUM(CASE WHEN status = 'pending' THEN amount ELSE 0 END), 0) AS pendingAmount
                FROM payments
              """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (doctorId > 0) st.setInt(1, doctorId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                summary[0] = rs.getBigDecimal("totalAmount");
                summary[1] = rs.getBigDecimal("paidAmount");
                summary[2] = rs.getBigDecimal("pendingAmount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    public java.util.List<model.DoctorProductivity> getDoctorProductivity() {
        return getDoctorProductivity(0);
    }

    public java.util.List<model.DoctorProductivity> getDoctorProductivity(int doctorId) {
        syncDoctorRowsForAllDoctorUsers();
        java.util.List<model.DoctorProductivity> list = new java.util.ArrayList<>();
        String sql;
        if (doctorId > 0) {
            sql = """
                SELECT d.doctor_id, u.full_name,
                       COUNT(*) AS completed_count
                FROM appointments a
                JOIN doctors d ON a.doctor_id = d.doctor_id
                JOIN users u ON d.user_id = u.user_id
                WHERE a.status = 'completed' AND d.doctor_id = ?
                GROUP BY d.doctor_id, u.full_name
                ORDER BY completed_count DESC
            """;
        } else {
            sql = """
                SELECT d.doctor_id, u.full_name,
                       COUNT(*) AS completed_count
                FROM appointments a
                JOIN doctors d ON a.doctor_id = d.doctor_id
                JOIN users u ON d.user_id = u.user_id
                WHERE a.status = 'completed'
                GROUP BY d.doctor_id, u.full_name
                ORDER BY completed_count DESC
                LIMIT 10
            """;
        }
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (doctorId > 0) st.setInt(1, doctorId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                model.DoctorProductivity dp = new model.DoctorProductivity();
                dp.setDoctorId(rs.getInt("doctor_id"));
                dp.setDoctorName(rs.getString("full_name"));
                dp.setTotalCompletedAppointments(rs.getInt("completed_count"));
                list.add(dp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.List<model.DoctorProductivity> getAllDoctors() {
        syncDoctorRowsForAllDoctorUsers();
        java.util.List<model.DoctorProductivity> list = new java.util.ArrayList<>();
        String sql = """
            SELECT d.doctor_id, u.full_name
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            WHERE u.role = 'doctor'
            ORDER BY u.full_name
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                model.DoctorProductivity dp = new model.DoctorProductivity();
                dp.setDoctorId(rs.getInt("doctor_id"));
                dp.setDoctorName(rs.getString("full_name"));
                list.add(dp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

