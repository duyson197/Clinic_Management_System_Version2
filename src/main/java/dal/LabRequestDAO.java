package dal;

import model.LabRequest;
import model.Patient;
import model.Doctor;
import model.Appointment;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class LabRequestDAO extends DBContext {

    /**
     * Lấy danh sách tất cả lab requests với thông tin đầy đủ
     */
    public List<LabRequest> getAllLabRequests() {
        List<LabRequest> list = new ArrayList<>();
        
        String sql = """
            SELECT 
                lr.request_id,
                lr.appointment_id,
                lr.doctor_id,
                lr.status,
                lr.created_at,
                p.patient_id,
                p.user_id,
                p.full_name AS patient_name,
                p.phone AS patient_phone,
                p.dob,
                p.email AS patient_email,
                p.gender,
                d.doctor_id,
                u.full_name AS doctor_name,
                u.phone AS doctor_phone,
                u.email AS doctor_email,
                a.symptom,
                a.status AS appointment_status
            FROM lab_requests lr
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            ORDER BY lr.created_at DESC
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            ResultSet rs = st.executeQuery();
            
            while (rs.next()) {
                LabRequest lr = mapResultSetToLabRequest(rs);
                list.add(lr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Đếm tổng số lab requests với filter (cho pagination)
     */
    public int countLabRequestsWithFilter(String status, String department, String priority, String searchTerm) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) as total
            FROM lab_requests lr
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE 1=1
            AND EXISTS (
                SELECT 1
                FROM payments pay
                WHERE pay.lab_request_id = lr.request_id
                AND pay.status = 'paid'
             )
        """);
        
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND lr.status = ?");
            params.add(status);
        } else {
            sql.append(" AND lr.status != 'cancelled'");
        }

        // department filter removed: specialization column no longer exists in doctors table

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            // Tách số từ BN<số> hoặc LAB-<năm>-<số>
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
     * Lấy lab requests với filter và pagination
     */
    public List<LabRequest> getLabRequestsWithFilterAndPagination(String status, String department, String priority, String searchTerm, int page, int pageSize) {
        List<LabRequest> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                lr.request_id,
                lr.appointment_id,
                lr.doctor_id,
                lr.status,
                lr.created_at,
                p.patient_id,
                p.user_id,
                p.full_name AS patient_name,
                p.phone AS patient_phone,
                p.dob,
                p.email AS patient_email,
                p.gender,
                d.doctor_id,
                u.full_name AS doctor_name,
                u.phone AS doctor_phone,
                u.email AS doctor_email,
                a.symptom,
                a.status AS appointment_status
            FROM lab_requests lr
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE 1=1
            AND EXISTS (
            SELECT 1
            FROM payments pay
            WHERE pay.lab_request_id = lr.request_id
               AND pay.status = 'paid'
            )
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append(" AND lr.status = ?");
            params.add(status);
        } else {
            sql.append(" AND lr.status != 'cancelled'");
        }

        // department filter removed: specialization column no longer exists in doctors table

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            // Tách số từ BN<số> hoặc LAB-<năm>-<số>
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

        sql.append(" ORDER BY CASE lr.status WHEN 'processing' THEN 0 WHEN 'pending' THEN 1 ELSE 2 END ASC, CASE WHEN lr.status IN ('pending','processing') THEN lr.created_at END ASC, CASE WHEN lr.status NOT IN ('pending','processing') THEN lr.created_at END DESC");
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
                LabRequest lr = mapResultSetToLabRequest(rs);
                list.add(lr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Lấy lab requests với filter
     */
    public List<LabRequest> getLabRequestsWithFilter(String status, String department, String priority, String searchTerm) {
        List<LabRequest> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                lr.request_id,
                lr.appointment_id,
                lr.doctor_id,
                lr.status,
                lr.created_at,
                p.patient_id,
                p.user_id,
                p.full_name AS patient_name,
                p.phone AS patient_phone,
                p.dob,
                p.email AS patient_email,
                p.gender,
                d.doctor_id,
                u.full_name AS doctor_name,
                u.phone AS doctor_phone,
                u.email AS doctor_email,
                a.symptom,
                a.status AS appointment_status
            FROM lab_requests lr
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE 1=1
            AND EXISTS (
            SELECT 1
            FROM payments pay
              WHERE pay.lab_request_id = lr.request_id
               AND pay.status = 'paid'
            )
        """);

        List<Object> params = new ArrayList<>();
        int paramIndex = 1;

        if (status != null && !status.isEmpty()) {
            sql.append(" AND lr.status = ?");
            params.add(status);
        }
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            // Tách số từ BN<số> hoặc LAB-<năm>-<số>
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
        
        sql.append(" ORDER BY lr.created_at DESC");

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = st.executeQuery();
            
            while (rs.next()) {
                LabRequest lr = mapResultSetToLabRequest(rs);
                
                // Filter by priority if needed (assuming priority is stored in notes or separate field)
                // For now, we'll skip priority filter as it's not in the database schema
                // You may need to add a priority field to lab_requests table
                
                list.add(lr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Lấy lab request theo ID
     */
    public LabRequest getLabRequestById(int requestId) {
        String sql = """
            SELECT 
                lr.request_id,
                lr.appointment_id,
                lr.doctor_id,
                lr.status,
                lr.created_at,
                p.patient_id,
                p.user_id,
                p.full_name AS patient_name,
                p.phone AS patient_phone,
                p.dob,
                p.email AS patient_email,
                p.gender,
                d.doctor_id,
                u.full_name AS doctor_name,
                u.phone AS doctor_phone,
                u.email AS doctor_email,
                a.symptom,
                a.status AS appointment_status
            FROM lab_requests lr
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE lr.request_id = ?
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, requestId);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLabRequest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Tạo phiếu chỉ định xét nghiệm (bác sĩ). Trả về requestId nếu thành công, 0 nếu thất bại.
     * Cho phép nhiều phiếu xét nghiệm cho cùng một appointment (ví dụ: bệnh khác, chỉ định thêm).
     * Chuyển trạng thái bệnh nhân trong exam_queue sang in_lab để tạm rời hàng chờ khám.
     */
    public int insertLabRequest(long appointmentId, int doctorId) {
        String insertSql = "INSERT INTO lab_requests (appointment_id, doctor_id, status, created_at) VALUES (?, ?, 'pending', ?)";
        try (PreparedStatement st = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp vnNow = Timestamp.valueOf(
                ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
            );
            st.setLong(1, appointmentId);
            st.setInt(2, doctorId);
            st.setTimestamp(3, vnNow);
            st.executeUpdate();
            ResultSet keys = st.getGeneratedKeys();
            if (keys.next()) {
                int requestId = keys.getInt(1);
                // Chuyển bệnh nhân sang trạng thái đang đi xét nghiệm
                String updateQueue = "UPDATE exam_queue SET status = 'in_lab' WHERE appointment_id = ?";
                try (PreparedStatement upQueue = connection.prepareStatement(updateQueue)) {
                    upQueue.setLong(1, appointmentId);
                    upQueue.executeUpdate();
                }
                return requestId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Hủy phiếu xét nghiệm (chỉ khi pending hoặc processing). Đưa bệnh nhân trở lại danh sách chờ khám (INSERT lại exam_queue).
     */
    public boolean cancelLabRequest(int requestId) {
        String getSql = "SELECT appointment_id, doctor_id, status FROM lab_requests WHERE request_id = ?";
        try (PreparedStatement st = connection.prepareStatement(getSql)) {
            st.setInt(1, requestId);
            ResultSet rs = st.executeQuery();
            if (!rs.next()) return false;
            long appointmentId = rs.getLong("appointment_id");
            int doctorId = rs.getInt("doctor_id");
            String status = rs.getString("status");
            if ("completed".equals(status)) return false; // Không hủy khi đã hoàn thành
            connection.setAutoCommit(false);
            try {
                // Đánh dấu phiếu xét nghiệm đã hủy
                try (PreparedStatement up = connection.prepareStatement("UPDATE lab_requests SET status = 'cancelled' WHERE request_id = ?")) {
                    up.setInt(1, requestId);
                    up.executeUpdate();
                }

                // Cập nhật trạng thái lịch hẹn về 'waiting' để quay lại hàng đợi khám
                try (PreparedStatement upAp = connection.prepareStatement("UPDATE appointments SET status = 'waiting' WHERE appointment_id = ?")) {
                    upAp.setLong(1, appointmentId);
                    upAp.executeUpdate();
                }

                // Đưa bệnh nhân trở lại exam_queue với status = 'waiting'
                // Nếu đã tồn tại bản ghi trong exam_queue thì chỉ cần UPDATE status,
                // nếu chưa có thì INSERT mới với queue_position kế tiếp.
                String checkQueueSql = "SELECT queue_id, queue_position FROM exam_queue WHERE appointment_id = ?";
                boolean existsInQueue = false;
                try (PreparedStatement checkSt = connection.prepareStatement(checkQueueSql)) {
                    checkSt.setLong(1, appointmentId);
                    ResultSet qrs = checkSt.executeQuery();
                    existsInQueue = qrs.next();
                }

                if (existsInQueue) {
                    try (PreparedStatement upQueue = connection.prepareStatement("UPDATE exam_queue SET status = 'waiting' WHERE appointment_id = ?")) {
                        upQueue.setLong(1, appointmentId);
                        upQueue.executeUpdate();
                    }
                } else {
                    int nextPos = 1;
                    String maxSql = "SELECT COALESCE(MAX(queue_position), 0) + 1 AS np FROM exam_queue WHERE doctor_id = ?";
                    try (PreparedStatement maxSt = connection.prepareStatement(maxSql)) {
                        maxSt.setInt(1, doctorId);
                        ResultSet maxRs = maxSt.executeQuery();
                        if (maxRs.next()) nextPos = maxRs.getInt("np");
                    }
                    try (PreparedStatement ins = connection.prepareStatement("INSERT INTO exam_queue (appointment_id, doctor_id, queue_position, status) VALUES (?, ?, ?, 'waiting')")) {
                        ins.setLong(1, appointmentId);
                        ins.setInt(2, doctorId);
                        ins.setInt(3, nextPos);
                        ins.executeUpdate();
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                return false;
            } finally {
                try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy kết quả xét nghiệm theo request_id (để hiển thị file đã upload).
     */
    public model.LabResult getLabResultByRequestId(int requestId) {
        String sql = "SELECT result_id, request_id, technician_id, result_file, notes, completed_at FROM lab_results WHERE request_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, requestId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                model.LabResult r = new model.LabResult();
                r.setResultId(rs.getInt("result_id"));
                r.setRequestId(rs.getInt("request_id"));
                r.setTechnicianId(rs.getObject("technician_id", Integer.class));
                r.setResultFile(rs.getString("result_file"));
                r.setNotes(rs.getString("notes"));
                r.setCompletedAt(rs.getTimestamp("completed_at"));
                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật trạng thái lab request
     */
    public boolean updateLabRequestStatus(int requestId, String status) {
        String sql = "UPDATE lab_requests SET status = ? WHERE request_id = ?";
        
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, status);
            st.setInt(2, requestId);
            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật ghi chú cho lab request
     */
    public boolean updateLabRequestNotes(int requestId, String notes) {
        // Check if lab_result exists
        String checkSql = "SELECT result_id FROM lab_results WHERE request_id = ?";
        
        try (PreparedStatement checkSt = connection.prepareStatement(checkSql)) {
            checkSt.setInt(1, requestId);
            ResultSet rs = checkSt.executeQuery();
            
            if (rs.next()) {
                // Update existing
                String updateSql = "UPDATE lab_results SET notes = ? WHERE request_id = ?";
                try (PreparedStatement updateSt = connection.prepareStatement(updateSql)) {
                    updateSt.setString(1, notes);
                    updateSt.setInt(2, requestId);
                    updateSt.executeUpdate();
                }
            } else {
                // Insert new
                String insertSql = "INSERT INTO lab_results (request_id, notes) VALUES (?, ?)";
                try (PreparedStatement insertSt = connection.prepareStatement(insertSql)) {
                    insertSt.setInt(1, requestId);
                    insertSt.setString(2, notes);
                    insertSt.executeUpdate();
                }
            }
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gửi kết quả xét nghiệm: lưu lab_result, đổi trạng thái lab_request -> completed,
     * đổi trạng thái appointment -> waiting, đưa bệnh nhân trở lại hàng chờ ưu tiên (waiting_return).
     */
    public boolean sendLabResult(int requestId, Integer technicianId, String resultFile, String notes) {
        try {
            connection.setAutoCommit(false);

            // 1) Lấy appointment_id, doctor_id từ lab_requests
            long appointmentId;
            int doctorId;
            String getSql = "SELECT appointment_id, doctor_id FROM lab_requests WHERE request_id = ?";
            try (PreparedStatement st = connection.prepareStatement(getSql)) {
                st.setInt(1, requestId);
                ResultSet rs = st.executeQuery();
                if (!rs.next()) {
                    connection.rollback();
                    return false;
                }
                appointmentId = rs.getLong("appointment_id");
                doctorId = rs.getInt("doctor_id");
            }

            // 2) Insert hoặc update lab_results
            String checkResult = "SELECT result_id FROM lab_results WHERE request_id = ?";
            try (PreparedStatement checkSt = connection.prepareStatement(checkResult)) {
                checkSt.setInt(1, requestId);
                ResultSet rs = checkSt.executeQuery();
                if (rs.next()) {
                    String updateResult = "UPDATE lab_results SET technician_id = ?, result_file = ?, notes = ?, completed_at = ? WHERE request_id = ?";
                    try (PreparedStatement up = connection.prepareStatement(updateResult)) {
                        Timestamp vnNow = Timestamp.valueOf(
                            ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
                        );
                        up.setObject(1, technicianId);
                        up.setString(2, resultFile != null ? resultFile : "");
                        up.setString(3, notes != null ? notes : "");
                        up.setTimestamp(4, vnNow);
                        up.setInt(5, requestId);
                        up.executeUpdate();
                    }
                } else {
                    String insertResult = "INSERT INTO lab_results (request_id, technician_id, result_file, notes, completed_at) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ins = connection.prepareStatement(insertResult)) {
                        Timestamp vnNow = Timestamp.valueOf(
                            ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
                        );
                        ins.setInt(1, requestId);
                        ins.setObject(2, technicianId);
                        ins.setString(3, resultFile != null ? resultFile : "");
                        ins.setString(4, notes != null ? notes : "");
                        ins.setTimestamp(5, vnNow);
                        ins.executeUpdate();
                    }
                }
            }

            // 3) Cập nhật lab_requests.status = 'completed'
            try (PreparedStatement st = connection.prepareStatement("UPDATE lab_requests SET status = 'completed' WHERE request_id = ?")) {
                st.setInt(1, requestId);
                st.executeUpdate();
            }

            // 4) Cập nhật appointments.status = 'waiting' (trả về danh sách chờ khám)
            try (PreparedStatement st = connection.prepareStatement("UPDATE appointments SET status = 'waiting' WHERE appointment_id = ?")) {
                st.setLong(1, appointmentId);
                st.executeUpdate();
            }

            // Đưa vào exam_queue: nếu đã tồn tại thì đổi sang waiting_return (ưu tiên quay lại), nếu chưa có thì INSERT waiting_return
            String checkQueue = "SELECT queue_id FROM exam_queue WHERE appointment_id = ?";
            try (PreparedStatement checkSt = connection.prepareStatement(checkQueue)) {
                checkSt.setLong(1, appointmentId);
                ResultSet rs = checkSt.executeQuery();
                if (rs.next()) {
                    try (PreparedStatement up = connection.prepareStatement("UPDATE exam_queue SET status = 'waiting_return' WHERE appointment_id = ?")) {
                        up.setLong(1, appointmentId);
                        up.executeUpdate();
                    }
                } else {
                    // Lấy queue_position tiếp theo cho doctor
                    int nextPos = 1;
                    String maxSql = "SELECT COALESCE(MAX(queue_position), 0) + 1 AS np FROM exam_queue WHERE doctor_id = ?";
                    try (PreparedStatement maxSt = connection.prepareStatement(maxSql)) {
                        maxSt.setInt(1, doctorId);
                        ResultSet maxRs = maxSt.executeQuery();
                        if (maxRs.next()) nextPos = maxRs.getInt("np");
                    }
                    try (PreparedStatement ins = connection.prepareStatement("INSERT INTO exam_queue (appointment_id, doctor_id, queue_position, status) VALUES (?, ?, ?, 'waiting_return')")) {
                        ins.setLong(1, appointmentId);
                        ins.setInt(2, doctorId);
                        ins.setInt(3, nextPos);
                        ins.executeUpdate();
                    }
                }
            }

            // Payment được tạo từ lúc bác sĩ chỉ định xét nghiệm (pending + payment pending -> payment paid -> vào lab queue),
            // nên không tạo payment ở bước gửi kết quả để tránh lệch luồng nghiệp vụ.

            connection.commit();
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Lấy danh sách các khoa/phòng (specializations)
     */
    public int getActiveRequestId() {
        // Only returns a request currently being processed (processing status).
        // Returns -1 when no request is being processed (all pending requests are free to start).
        String sql = """
            SELECT lr.request_id
            FROM lab_requests lr
            WHERE lr.status = 'processing'
            AND EXISTS (
                SELECT 1 FROM payments pay
                WHERE pay.lab_request_id = lr.request_id AND pay.status = 'paid'
            )
            ORDER BY lr.created_at ASC
            LIMIT 1
            """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getInt("request_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<String> getAllSpecializations() {
        // specialization column removed from doctors table in new schema
        return new ArrayList<>();
    }

    /**
     * Lấy thống kê lab requests
     */
    public int[] getLabRequestStatistics() {
        return getLabRequestStatisticsWithFilter(null, null, null);
    }

    /**
     * Lấy thống kê lab requests theo bộ lọc (không giới hạn theo ngày)
     * @return stats [total, pending, processing, completed]
     */
    public int[] getLabRequestStatisticsWithFilter(String status, String department, String searchTerm) {
        int[] stats = new int[5]; // [total, pending, processing, completed, cancelled]

        StringBuilder sql = new StringBuilder("""
            SELECT
                COUNT(*) as total,
                COALESCE(SUM(CASE WHEN lr.status = 'pending' THEN 1 ELSE 0 END), 0) as pending,
                COALESCE(SUM(CASE WHEN lr.status = 'processing' THEN 1 ELSE 0 END), 0) as processing,
                COALESCE(SUM(CASE WHEN lr.status = 'completed' THEN 1 ELSE 0 END), 0) as completed,
                COALESCE(SUM(CASE WHEN lr.status = 'cancelled' THEN 1 ELSE 0 END), 0) as cancelled
            FROM lab_requests lr
            JOIN appointments a ON lr.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON lr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE 1=1
            AND EXISTS (
            SELECT 1
             FROM payments pay
             WHERE pay.lab_request_id = lr.request_id
             AND pay.status = 'paid'
             )
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append(" AND lr.status = ?");
            params.add(status);
        }

        // department filter removed: specialization column no longer exists in doctors table

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchPattern = "%" + searchTerm + "%";
            // Tách số từ BN<số> hoặc LAB-<năm>-<số>
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
                stats[2] = rs.getInt("processing");
                stats[3] = rs.getInt("completed");
                stats[4] = rs.getInt("cancelled");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Map ResultSet to LabRequest object
     */
    private LabRequest mapResultSetToLabRequest(ResultSet rs) throws SQLException {
        LabRequest lr = new LabRequest();
        lr.setRequestId(rs.getInt("request_id"));
        lr.setAppointmentId(rs.getLong("appointment_id"));
        lr.setDoctorId(rs.getInt("doctor_id"));
        lr.setStatus(rs.getString("status"));
        lr.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Map Patient
        Patient patient = new Patient();
        patient.setPatientId(rs.getLong("patient_id"));
        patient.setUserId(rs.getInt("user_id"));
        patient.setFullName(rs.getString("patient_name"));
        patient.setPhone(rs.getString("patient_phone"));
        patient.setDob(rs.getDate("dob"));
        patient.setEmail(rs.getString("patient_email"));
        patient.setGender(rs.getString("gender"));
        lr.setPatient(patient);
        
        // Map Doctor
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getInt("doctor_id"));
        doctor.setFullName(rs.getString("doctor_name"));
        doctor.setPhone(rs.getString("doctor_phone"));
        doctor.setEmail(rs.getString("doctor_email"));
        lr.setDoctor(doctor);
        
        // Map Appointment
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getLong("appointment_id"));
        appointment.setSymptom(rs.getString("symptom"));
        appointment.setStatus(rs.getString("appointment_status"));
        lr.setAppointment(appointment);
        
        // Get notes from lab_results if exists
        String notesSql = "SELECT notes FROM lab_results WHERE request_id = ?";
        try (PreparedStatement notesSt = connection.prepareStatement(notesSql)) {
            notesSt.setInt(1, lr.getRequestId());
            ResultSet notesRs = notesSt.executeQuery();
            if (notesRs.next()) {
                lr.setNotes(notesRs.getString("notes"));
            }
        } catch (SQLException e) {
            // Notes not found, that's okay
        }
        
        return lr;
    }
}

