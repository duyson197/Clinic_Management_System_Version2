package dal;

import model.Doctor;
import model.DoctorQueueItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.DoctorDashboardStats;
import model.ExamLabItem;
import model.ExaminationHistoryItem;
import model.MedicalRecord;
import model.Medicine;
import model.PrescriptionItem;

public class DoctorDAO {

    /* get doctor by id*/
    public Doctor getDoctorByUserId(int userId) {
        String sql = """
            SELECT d.doctor_id, d.user_id, d.specialization,
                   u.full_name, u.phone, u.email
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            WHERE d.user_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Doctor d = new Doctor();
                    d.setDoctorId(rs.getInt("doctor_id"));
                    d.setUserId(rs.getInt("user_id"));
                    d.setSpecialization(rs.getString("specialization"));
                    d.setFullName(rs.getString("full_name"));
                    d.setPhone(rs.getString("phone"));
                    d.setEmail(rs.getString("email"));
                    return d;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDoctorByUserId: " + e.getMessage());
        }
        return null;
    }

    public void syncDoctorRowsForAllDoctorUsers() {
        String sql = """
            INSERT INTO doctors (user_id, specialization)
            SELECT u.user_id, 'Chua cap nhat'
            FROM users u
            LEFT JOIN doctors d ON d.user_id = u.user_id
            WHERE u.role = 'doctor' AND d.doctor_id IS NULL
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi syncDoctorRowsForAllDoctorUsers: " + e.getMessage());
        }
    }

    public void syncDoctorProfilesForAllDoctorUsers() {
        String sql = """
            INSERT INTO doctors (user_id, specialization)
            SELECT u.user_id, 'Chưa cập nhật'
            FROM users u
            LEFT JOIN doctors d ON d.user_id = u.user_id
            WHERE u.role = 'doctor' AND d.doctor_id IS NULL
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.executeUpdate();
        } catch (SQLException e) {
            // Keep schedule page working even if sync fails on existing schemas.
            System.err.println("Lỗi DB khi syncDoctorProfilesForAllDoctorUsers (non-fatal): " + e.getMessage());
        }
    }

    public boolean hasFutureUnfinishedAppointmentsByUserId(int userId) {
        String sql = """
            SELECT 1
            FROM doctors d
            JOIN appointments a ON a.doctor_id = d.doctor_id
            WHERE d.user_id = ?
              AND a.status NOT IN ('completed', 'cancelled')
              AND (
                  a.appointment_date > CURRENT_DATE
                  OR (a.appointment_date = CURRENT_DATE AND a.appointment_time >= CURRENT_TIME)
              )
            LIMIT 1
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi hasFutureUnfinishedAppointmentsByUserId: " + e.getMessage());
        }
        return false;
    }

    public void upsertDoctorProfileByUserId(int userId, String academicDegree, Date dob, String gender,
            String specialization, int experienceYears, String academicTitle,
            String professionalQualification, int priceBooking) throws SQLException {
        syncDoctorRowsForAllDoctorUsers();

        String updateDoctorSql = """
            UPDATE doctors
            SET specialization = ?, experience_years = ?, academic_title = ?,
                professional_qualification = ?, price_booking = ?
            WHERE user_id = ?
        """;

        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement updateDoctor = conn.prepareStatement(updateDoctorSql)) {
                updateDoctor.setString(1, specialization);
                updateDoctor.setInt(2, experienceYears);
                setNullableString(updateDoctor, 3, academicTitle);
                setNullableString(updateDoctor, 4, professionalQualification);
                updateDoctor.setInt(5, priceBooking);
                updateDoctor.setInt(6, userId);
                if (updateDoctor.executeUpdate() == 0) {
                    try (PreparedStatement insertDoctor = conn.prepareStatement(
                            """
                            INSERT INTO doctors (user_id, specialization, experience_years, academic_title,
                                                 professional_qualification, price_booking)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """)) {
                        insertDoctor.setInt(1, userId);
                        insertDoctor.setString(2, specialization);
                        insertDoctor.setInt(3, experienceYears);
                        setNullableString(insertDoctor, 4, academicTitle);
                        setNullableString(insertDoctor, 5, professionalQualification);
                        insertDoctor.setInt(6, priceBooking);
                        insertDoctor.executeUpdate();
                    }
                }
            }

            String updateStaffSql = """
                UPDATE staff_profiles
                SET academic_degree = ?, dob = ?, gender = ?
                WHERE user_id = ?
            """;
            try (PreparedStatement updateStaff = conn.prepareStatement(updateStaffSql)) {
                updateStaff.setString(1, academicDegree);
                updateStaff.setDate(2, dob);
                updateStaff.setString(3, gender);
                updateStaff.setInt(4, userId);
                if (updateStaff.executeUpdate() == 0) {
                    try (PreparedStatement insertStaff = conn.prepareStatement(
                            """
                            INSERT INTO staff_profiles (user_id, academic_degree, dob, gender)
                            VALUES (?, ?, ?, ?)
                            """)) {
                        insertStaff.setInt(1, userId);
                        insertStaff.setString(2, academicDegree);
                        insertStaff.setDate(3, dob);
                        insertStaff.setString(4, gender);
                        insertStaff.executeUpdate();
                    }
                }
            }
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
            return;
        }
        statement.setString(index, value);
    }

    public List<Doctor> getDoctorsForAdmin(String keyword, String specializationFilter, String qualificationFilter) {
        syncDoctorRowsForAllDoctorUsers();
        List<Doctor> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT d.doctor_id, d.user_id, d.specialization, sp.academic_degree, sp.dob, sp.gender,
                   d.experience_years, d.academic_title, d.professional_qualification, d.price_booking, d.rating,
                   u.full_name, u.phone, u.email, u.status AS user_status
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id
            WHERE u.role = 'doctor'
        """);

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (specializationFilter != null && !specializationFilter.isBlank()) {
            sql.append(" AND d.specialization = ?");
            params.add(specializationFilter.trim());
        }
        if (qualificationFilter != null && !qualificationFilter.isBlank()) {
            sql.append(" AND sp.academic_degree = ?");
            params.add(qualificationFilter.trim());
        }
        sql.append(" ORDER BY u.full_name");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object param : params) {
                st.setObject(idx++, param);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Doctor d = new Doctor();
                    d.setDoctorId(rs.getInt("doctor_id"));
                    d.setUserId(rs.getInt("user_id"));
                    d.setSpecialization(rs.getString("specialization"));
                    d.setAcademicDegree(rs.getString("academic_degree"));
                    d.setDob(rs.getDate("dob"));
                    d.setGender(rs.getString("gender"));
                    d.setExperience_years(rs.getInt("experience_years"));
                    d.setAcademicTitle(rs.getString("academic_title"));
                    d.setProfessionalQualification(rs.getString("professional_qualification"));
                    d.setPrice(rs.getDouble("price_booking"));
                    d.setRating(rs.getDouble("rating"));
                    d.setFullName(rs.getString("full_name"));
                    d.setPhone(rs.getString("phone"));
                    d.setEmail(rs.getString("email"));
                    d.setStatus(rs.getString("user_status"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDoctorsForAdmin: " + e.getMessage());
        }
        return list;
    }

    public List<Doctor> getStaffsForAdmin(String keyword, String roleFilter, String qualificationFilter) {
        syncDoctorRowsForAllDoctorUsers();
        List<Doctor> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT d.doctor_id, u.user_id, u.role, sp.academic_degree, sp.dob, sp.gender,
                   d.specialization, d.experience_years, d.academic_title, d.professional_qualification,
                   d.price_booking, d.rating, u.full_name, u.phone, u.email, u.status AS user_status
            FROM users u
            LEFT JOIN staff_profiles sp ON sp.user_id = u.user_id
            LEFT JOIN doctors d ON d.user_id = u.user_id
            WHERE u.role IN ('doctor', 'receptionist', 'technician', 'patient_manager')
        """);

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (roleFilter != null && !roleFilter.isBlank()) {
            sql.append(" AND u.role = ?");
            params.add(roleFilter.trim());
        }
        if (qualificationFilter != null && !qualificationFilter.isBlank()) {
            sql.append(" AND sp.academic_degree = ?");
            params.add(qualificationFilter.trim());
        }
        sql.append(" ORDER BY u.full_name");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object param : params) {
                st.setObject(idx++, param);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAdminStaff(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getStaffsForAdmin: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDistinctDoctorSpecializations() {
        List<String> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT specialization
            FROM doctors
            WHERE specialization IS NOT NULL AND specialization <> ''
            ORDER BY specialization
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("specialization"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDistinctDoctorSpecializations: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDistinctDoctorQualifications() {
        List<String> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT academic_degree
            FROM staff_profiles
            WHERE academic_degree IS NOT NULL AND academic_degree <> ''
            ORDER BY academic_degree
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("academic_degree"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDistinctDoctorQualifications: " + e.getMessage());
        }
        return list;
    }

    public Doctor getDoctorByIdForAdmin(int doctorId) {
        String sql = """
            SELECT d.doctor_id, d.user_id, d.specialization, sp.academic_degree, sp.dob, sp.gender,
                   d.experience_years, d.academic_title, d.professional_qualification, d.price_booking, d.rating,
                   u.full_name, u.phone, u.email, u.status AS user_status
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id
            WHERE d.doctor_id = ? AND u.role = 'doctor'
            LIMIT 1
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Doctor d = new Doctor();
                    d.setDoctorId(rs.getInt("doctor_id"));
                    d.setUserId(rs.getInt("user_id"));
                    d.setSpecialization(rs.getString("specialization"));
                    d.setAcademicDegree(rs.getString("academic_degree"));
                    d.setDob(rs.getDate("dob"));
                    d.setGender(rs.getString("gender"));
                    d.setExperience_years(rs.getInt("experience_years"));
                    d.setAcademicTitle(rs.getString("academic_title"));
                    d.setProfessionalQualification(rs.getString("professional_qualification"));
                    d.setPrice(rs.getDouble("price_booking"));
                    d.setRating(rs.getDouble("rating"));
                    d.setFullName(rs.getString("full_name"));
                    d.setPhone(rs.getString("phone"));
                    d.setEmail(rs.getString("email"));
                    d.setStatus(rs.getString("user_status"));
                    return d;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDoctorByIdForAdmin: " + e.getMessage());
        }
        return null;
    }

    public Doctor getStaffByUserIdForAdmin(int userId) {
        syncDoctorRowsForAllDoctorUsers();
        String sql = """
            SELECT d.doctor_id, u.user_id, u.role, sp.academic_degree, sp.dob, sp.gender,
                   d.specialization, d.experience_years, d.academic_title, d.professional_qualification,
                   d.price_booking, d.rating, u.full_name, u.phone, u.email, u.status AS user_status
            FROM users u
            LEFT JOIN staff_profiles sp ON sp.user_id = u.user_id
            LEFT JOIN doctors d ON d.user_id = u.user_id
            WHERE u.user_id = ?
              AND u.role IN ('doctor', 'receptionist', 'technician', 'patient_manager')
            LIMIT 1
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapAdminStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getStaffByUserIdForAdmin: " + e.getMessage());
        }
        return null;
    }

    public void upsertStaffProfileByUserId(int userId, String academicDegree, Date dob, String gender) throws SQLException {
        String updateStaffSql = """
            UPDATE staff_profiles
            SET academic_degree = ?, dob = ?, gender = ?
            WHERE user_id = ?
        """;
        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement updateStaff = conn.prepareStatement(updateStaffSql)) {
                updateStaff.setString(1, academicDegree);
                updateStaff.setDate(2, dob);
                updateStaff.setString(3, gender);
                updateStaff.setInt(4, userId);
                if (updateStaff.executeUpdate() == 0) {
                    try (PreparedStatement insertStaff = conn.prepareStatement(
                            """
                            INSERT INTO staff_profiles (user_id, academic_degree, dob, gender)
                            VALUES (?, ?, ?, ?)
                            """)) {
                        insertStaff.setInt(1, userId);
                        insertStaff.setString(2, academicDegree);
                        insertStaff.setDate(3, dob);
                        insertStaff.setString(4, gender);
                        insertStaff.executeUpdate();
                    }
                }
            }
        }
    }

    public void ensureDoctorRowByUserId(int userId, int priceBooking) throws SQLException {
        syncDoctorRowsForAllDoctorUsers();
        String sql = """
            UPDATE doctors
            SET price_booking = ?
            WHERE user_id = ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, priceBooking);
            st.setInt(2, userId);
            if (st.executeUpdate() == 0) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO doctors (user_id, price_booking) VALUES (?, ?)")) {
                    insert.setInt(1, userId);
                    insert.setInt(2, priceBooking);
                    insert.executeUpdate();
                }
            }
        }
    }

    public void updateDoctorPriceByUserId(int userId, int priceBooking) throws SQLException {
        String sql = "UPDATE doctors SET price_booking = ? WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, priceBooking);
            st.setInt(2, userId);
            st.executeUpdate();
        }
    }

    public int createDoctorWithUser(String fullName, String phone, String email, String password,
            String specialization, String qualification, int experienceYears, int priceBooking) throws SQLException {
        String sqlUser = """
            INSERT INTO users (full_name, phone, email, password_hash, role, status)
            VALUES (?, ?, ?, ?, 'doctor', 'active')
        """;
        String sqlDoctor = """
            INSERT INTO doctors (user_id, specialization, experience_years, price_booking)
            VALUES (?, ?, ?, ?)
        """;
        String sqlStaff = """
            INSERT INTO staff_profiles (user_id, academic_degree)
            VALUES (?, ?)
        """;

        try (Connection conn = DBContext.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                int userId;

                try (PreparedStatement userSt = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    userSt.setString(1, fullName);
                    userSt.setString(2, phone);
                    userSt.setString(3, email);
                    userSt.setString(4, password);
                    int affected = userSt.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        return 0;
                    }
                    try (ResultSet keys = userSt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return 0;
                        }
                        userId = keys.getInt(1);
                    }
                }

                try (PreparedStatement doctorSt = conn.prepareStatement(sqlDoctor)) {
                    doctorSt.setInt(1, userId);
                    doctorSt.setString(2, specialization);
                    doctorSt.setInt(3, experienceYears);
                    doctorSt.setInt(4, priceBooking);
                    int affected = doctorSt.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        return 0;
                    }
                }

                try (PreparedStatement staffSt = conn.prepareStatement(sqlStaff)) {
                    staffSt.setInt(1, userId);
                    staffSt.setString(2, qualification);
                    int affected = staffSt.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        return 0;
                    }
                }

                conn.commit();
                return userId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public boolean updateDoctorAndUser(int doctorId, String fullName, String phone, String email,
            String specialization, String qualification, int experienceYears, int priceBooking) throws SQLException {
        String sqlGet = "SELECT user_id FROM doctors WHERE doctor_id = ? LIMIT 1";
        String sqlUser = "UPDATE users SET full_name = ?, phone = ?, email = ? WHERE user_id = ? AND role = 'doctor'";
        String sqlDoctor = """
            UPDATE doctors
            SET specialization = ?, experience_years = ?, price_booking = ?
            WHERE doctor_id = ?
        """;
        String sqlStaff = """
            UPDATE staff_profiles
            SET academic_degree = ?
            WHERE user_id = ?
        """;
        String sqlInsertStaff = """
            INSERT INTO staff_profiles (user_id, academic_degree)
            VALUES (?, ?)
        """;

        try (Connection conn = DBContext.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                Integer userId = null;
                try (PreparedStatement st = conn.prepareStatement(sqlGet)) {
                    st.setInt(1, doctorId);
                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("user_id");
                        }
                    }
                }
                if (userId == null) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement userSt = conn.prepareStatement(sqlUser)) {
                    userSt.setString(1, fullName);
                    userSt.setString(2, phone);
                    userSt.setString(3, email);
                    userSt.setInt(4, userId);
                    if (userSt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement doctorSt = conn.prepareStatement(sqlDoctor)) {
                    doctorSt.setString(1, specialization);
                    doctorSt.setInt(2, experienceYears);
                    doctorSt.setInt(3, priceBooking);
                    doctorSt.setInt(4, doctorId);
                    if (doctorSt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement staffSt = conn.prepareStatement(sqlStaff)) {
                    staffSt.setString(1, qualification);
                    staffSt.setInt(2, userId);
                    if (staffSt.executeUpdate() == 0) {
                        try (PreparedStatement insertStaffSt = conn.prepareStatement(sqlInsertStaff)) {
                            insertStaffSt.setInt(1, userId);
                            insertStaffSt.setString(2, qualification);
                            if (insertStaffSt.executeUpdate() == 0) {
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public void upsertDoctorProfileByUserId(int userId, String specialization, String qualification,
            int experienceYears, int priceBooking) throws SQLException {
        upsertDoctorProfileByUserId(userId, qualification, null, null, specialization,
                experienceYears, null, null, priceBooking);
    }

    public void upsertStaffProfileByUserId(int userId, String qualification, int experienceYears) throws SQLException {
        upsertStaffProfileByUserId(userId, qualification, null, null);
    }

    private Doctor mapAdminStaff(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setUserId(rs.getInt("user_id"));
        d.setRole(rs.getString("role"));
        d.setSpecialization(rs.getString("specialization"));
        d.setAcademicDegree(rs.getString("academic_degree"));
        d.setDob(rs.getDate("dob"));
        d.setGender(rs.getString("gender"));
        d.setExperience_years(rs.getInt("experience_years"));
        d.setAcademicTitle(rs.getString("academic_title"));
        d.setProfessionalQualification(rs.getString("professional_qualification"));
        d.setPrice(rs.getDouble("price_booking"));
        d.setRating(rs.getDouble("rating"));
        d.setFullName(rs.getString("full_name"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setStatus(rs.getString("user_status"));
        return d;
    }

    public void updateQueueStatus(long appointmentId, String status) {
        String sql = """
            UPDATE exam_queue
            SET status = ?
            WHERE appointment_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, status);
            st.setLong(2, appointmentId);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi updateQueueStatus: " + e.getMessage());
        }
    }

    public void startExamination(long appointmentId) {
        String sql = """
            UPDATE exam_queue
            SET status = 'examining'
            WHERE appointment_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, appointmentId);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi startExamination: " + e.getMessage());
        }
    }

    public DoctorQueueItem getCurrentExaminingQueueItem(int doctorId) {
        String sql = """
            SELECT
                q.queue_position,
                q.appointment_id,
                p.patient_id,
                p.full_name AS patient_name,
                p.gender,
                p.dob,
                a.symptom,
                q.status
            FROM exam_queue q
            JOIN appointments a ON q.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE q.doctor_id = ?
               AND q.status = 'examining'
               AND a.appointment_date = CURRENT_DATE
            ORDER BY q.queue_position
            LIMIT 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQueueItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getCurrentExaminingQueueItem: " + e.getMessage());
        }

        return null;
    }

    public DoctorQueueItem getNextWaitingQueueItem(int doctorId) {
        String sql = """
            SELECT
                q.queue_position,
                q.appointment_id,
                p.patient_id,
                p.full_name AS patient_name,
                p.gender,
                p.dob,
                a.symptom,
                q.status
            FROM exam_queue q
            JOIN appointments a ON q.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE q.doctor_id = ?
               AND q.status IN ('waiting_return', 'waiting')
               AND a.appointment_date = CURRENT_DATE
            ORDER BY CASE WHEN q.status = 'waiting_return' THEN 0 ELSE 1 END, q.queue_position
            LIMIT 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQueueItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getNextWaitingQueueItem: " + e.getMessage());
        }

        return null;
    }

    public void finishExamination(long appointmentId) {
        String sql = """
            UPDATE exam_queue
            SET status = 'done'
            WHERE appointment_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, appointmentId);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi finishExamination: " + e.getMessage());
        }
    }

    public List<DoctorQueueItem> getTodayQueueByDoctor(int doctorId) {
        List<DoctorQueueItem> list = new ArrayList<>();

        String sql = """
        SELECT
            q.queue_position,
            p.full_name AS patient_name,
            p.gender,
            p.dob,
            a.symptom,
            q.status
        FROM exam_queue q
        JOIN appointments a ON q.appointment_id = a.appointment_id
        JOIN patients p ON a.patient_id = p.patient_id
        WHERE q.doctor_id = ?
        ORDER BY
        CASE
            WHEN q.status = 'waiting_return' THEN 0
            WHEN q.status = 'examining' THEN 1
            WHEN q.status = 'waiting' THEN 2
            WHEN q.status = 'in_lab' THEN 3
            ELSE 4
        END,
        q.queue_position
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoctorQueueItem item = new DoctorQueueItem();
                    item.setQueuePosition(rs.getInt("queue_position"));
                    item.setPatientName(rs.getString("patient_name"));
                    item.setGender(rs.getString("gender"));
                    item.setDob(rs.getDate("dob"));
                    item.setSymptom(rs.getString("symptom"));
                    item.setStatus(rs.getString("status"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getTodayQueueByDoctor: " + e.getMessage());
        }
        return list;
    }

    public DoctorDashboardStats getDashboardStats(int doctorId) {
        DoctorDashboardStats stats = new DoctorDashboardStats();
        String sql = """
        SELECT
            COUNT(*) AS total,
            SUM(CASE WHEN q.status IN ('waiting', 'waiting_return') THEN 1 ELSE 0 END) AS waiting,
            SUM(CASE WHEN q.status = 'examining' THEN 1 ELSE 0 END) AS examining,
            SUM(CASE WHEN q.status = 'done' THEN 1 ELSE 0 END) AS done
            FROM exam_queue q
            JOIN appointments a ON q.appointment_id = a.appointment_id
            WHERE q.doctor_id = ?
                AND a.appointment_date = CURRENT_DATE
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int done = rs.getInt("done");
                    stats.setTotal(total);
                    stats.setWaiting(rs.getInt("waiting"));
                    stats.setExamining(rs.getInt("examining"));
                    stats.setDone(done);
                    stats.setCompletionRate(total == 0 ? 0 : (done * 100.0) / total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDashboardStats: " + e.getMessage());
        }
        return stats;
    }

    // lọc theo keyword , trạng thái
    public List<DoctorQueueItem> getQueueByDoctorWithFilter(
            int doctorId,
            String status,
            String keyword
    ) {
        return getQueueByDoctorWithFilterPaging(doctorId, status, keyword, 1, Integer.MAX_VALUE);
    }

    // tính tổng số bản ghi theo bộ lọc để controller tính totalPages cho phân trang.
    public int countQueueByDoctorWithFilter(int doctorId, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM exam_queue q
        JOIN appointments a ON q.appointment_id = a.appointment_id
        JOIN patients p ON a.patient_id = p.patient_id
        WHERE q.doctor_id = ?
           AND a.appointment_date = CURRENT_DATE
    """);

        boolean hasActiveFilter = status == null
                || status.isBlank()
                || "active".equalsIgnoreCase(status)
                || "all".equalsIgnoreCase(status);
        boolean hasStatusFilter = !hasActiveFilter;
        boolean hasKeywordFilter = keyword != null && !keyword.isBlank();

        if (hasActiveFilter) {
            sql.append(" AND LOWER(q.status) IN ('waiting', 'waiting_return', 'in_lab', 'examining') ");
        } else if ("done".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
            sql.append(" AND LOWER(q.status) IN ('done', 'completed') ");
        } else if (hasStatusFilter) {
            sql.append(" AND LOWER(q.status) = ? ");
        }

        if (hasKeywordFilter) {
            sql.append(" AND p.full_name LIKE ? ");
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, doctorId);

            if (hasStatusFilter && !"done".equalsIgnoreCase(status) && !"completed".equalsIgnoreCase(status)) {
                ps.setString(index++, status.trim().toLowerCase());
            }

            if (hasKeywordFilter) {
                ps.setString(index, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi countQueueByDoctorWithFilter: " + e.getMessage());
        }

        return 0;
    }

    // Pagination
    public List<DoctorQueueItem> getQueueByDoctorWithFilterPaging(
            int doctorId,
            String status,
            String keyword,
            int page,
            int pageSize
    ) {
        List<DoctorQueueItem> list = new ArrayList<>();

        int safePage = Math.max(page, 1);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int offset = (safePage - 1) * safePageSize;

        StringBuilder sql = new StringBuilder("""
        SELECT
            q.queue_position,
            q.appointment_id,
            p.patient_id,
            p.full_name AS patient_name,
            p.gender,
            p.dob,
            a.symptom,
            q.status
        FROM exam_queue q
        JOIN appointments a ON q.appointment_id = a.appointment_id
        JOIN patients p ON a.patient_id = p.patient_id
        WHERE q.doctor_id = ?
           AND a.appointment_date = CURRENT_DATE
    """);

        boolean hasActiveFilter = status == null
                || status.isBlank()
                || "active".equalsIgnoreCase(status)
                || "all".equalsIgnoreCase(status);
        boolean hasStatusFilter = !hasActiveFilter;
        boolean hasKeywordFilter = keyword != null && !keyword.isBlank();

        if (hasActiveFilter) {
            sql.append(" AND LOWER(q.status) IN ('waiting', 'waiting_return', 'in_lab', 'examining') ");
        } else if ("done".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
            sql.append(" AND LOWER(q.status) IN ('done', 'completed') ");
        } else if (hasStatusFilter) {
            sql.append(" AND LOWER(q.status) = ? ");
        }

        if (hasKeywordFilter) {
            sql.append(" AND p.full_name LIKE ? ");
        }

        sql.append("""
            ORDER BY
                CASE
                    WHEN q.status = 'waiting_return' THEN 0
                    WHEN q.status = 'examining' THEN 1
                    WHEN q.status = 'waiting' THEN 2
                    WHEN q.status = 'in_lab' THEN 3
                    ELSE 4
                END,
                q.queue_position
            LIMIT ? OFFSET ?
        """);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, doctorId);

            if (hasStatusFilter && !"done".equalsIgnoreCase(status) && !"completed".equalsIgnoreCase(status)) {
                ps.setString(index++, status.trim().toLowerCase());
            }

            if (hasKeywordFilter) {
                ps.setString(index++, "%" + keyword + "%");
            }

            ps.setInt(index++, safePageSize);
            ps.setInt(index, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapQueueItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getQueueByDoctorWithFilterPaging: " + e.getMessage());
        }
        return list;
    }

    public DoctorQueueItem getQueueItemByAppointment(int doctorId, long appointmentId) {
        String sql = """
            SELECT
                q.queue_position,
                q.appointment_id,
                p.patient_id,
                p.full_name AS patient_name,
                p.gender,
                p.dob,
                a.symptom,
                q.status
            FROM exam_queue q
            JOIN appointments a ON q.appointment_id = a.appointment_id
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE q.doctor_id = ? AND q.appointment_id = ?
            LIMIT 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setLong(2, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQueueItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getQueueItemByAppointment: " + e.getMessage());
        }

        return null;
    }

    // Nạp timeline các phiếu xét nghiệm và kết quả theo appointment hiện tại.
    public List<ExamLabItem> getLabResultsByAppointment(long appointmentId) {
        List<ExamLabItem> list = new ArrayList<>();
        String sql = """
            SELECT
                lr.request_id,
                lr.status,
                lr.created_at,
                res.result_file,
                res.notes,
                res.completed_at
            FROM lab_requests lr
            LEFT JOIN lab_results res ON lr.request_id = res.request_id
            WHERE lr.appointment_id = ?
            ORDER BY lr.created_at DESC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamLabItem item = new ExamLabItem();
                    item.setRequestId(rs.getInt("request_id"));
                    item.setStatus(rs.getString("status"));
                    item.setRequestedAt(rs.getTimestamp("created_at"));
                    item.setResultFile(rs.getString("result_file"));
                    item.setNotes(rs.getString("notes"));
                    item.setCompletedAt(rs.getTimestamp("completed_at"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getLabResultsByAppointment: " + e.getMessage());
        }

        return list;
    }

    public List<ExaminationHistoryItem> getExaminationHistoryByAppointment(long appointmentId) {
        List<ExaminationHistoryItem> list = new ArrayList<>();

        String sql = """
            SELECT
                a.appointment_id,
                a.appointment_date,
                a.appointment_time,
                COALESCE(mr.symptoms, a.symptom) AS symptom,
                a.status AS appointment_status,
                COALESCE(eq.status, 'N/A') AS queue_status,
                    mr.diagnosis,
                    mr.notes,
                    mr.updated_at AS record_updated_at
            FROM appointments current_ap
            JOIN appointments a ON a.patient_id = current_ap.patient_id
            LEFT JOIN exam_queue eq ON eq.appointment_id = a.appointment_id
            JOIN medical_records mr ON mr.appointment_id = a.appointment_id
            WHERE current_ap.appointment_id = ?
            AND a.appointment_id <> current_ap.appointment_id
            ORDER BY a.appointment_date DESC, a.appointment_time DESC, mr.updated_at DESC
            LIMIT 10
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExaminationHistoryItem item = new ExaminationHistoryItem();
                    item.setAppointmentId(rs.getLong("appointment_id"));
                    item.setAppointmentDate(rs.getDate("appointment_date"));
                    item.setAppointmentTime(rs.getTime("appointment_time"));
                    item.setSymptom(rs.getString("symptom"));
                    item.setAppointmentStatus(rs.getString("appointment_status"));
                    item.setQueueStatus(rs.getString("queue_status"));
                    item.setDiagnosis(rs.getString("diagnosis"));
                    item.setNotes(rs.getString("notes"));
                    item.setRecordUpdatedAt(rs.getTimestamp("record_updated_at"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getExaminationHistoryByAppointment: " + e.getMessage());
        }

        return list;
    }

    public MedicalRecord getMedicalRecordByAppointment(long appointmentId) {
        String sql = """
            SELECT appointment_id, symptoms, diagnosis, notes, updated_at
            FROM medical_records
            WHERE appointment_id = ?
            LIMIT 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setAppointmentId(rs.getLong("appointment_id"));
                    record.setSymptoms(rs.getString("symptoms"));
                    record.setDiagnosis(rs.getString("diagnosis"));
                    record.setNotes(rs.getString("notes"));
                    record.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return record;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getMedicalRecordByAppointment: " + e.getMessage());
        }

        return null;
    }

    public boolean upsertMedicalRecord(long appointmentId, String symptoms, String diagnosis, String notes) {
        try (Connection conn = DBContext.getConnection()) {
            return upsertMedicalRecordTx(conn, appointmentId, symptoms, diagnosis, notes);
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi upsertMedicalRecord: " + e.getMessage());
            return false;
        }
    }

    public boolean saveMedicalRecordAndFinishExamination(long appointmentId, String symptoms, String diagnosis, String notes) {
        String finishSql = """
            UPDATE exam_queue
            SET status = 'done'
            WHERE appointment_id = ?
        """;

        try (Connection conn = DBContext.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                if (!upsertMedicalRecordTx(conn, appointmentId, symptoms, diagnosis, notes)) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement finish = conn.prepareStatement(finishSql)) {
                    finish.setLong(1, appointmentId);
                    if (finish.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Lỗi DB khi saveMedicalRecordAndFinishExamination: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi lấy connection cho saveMedicalRecordAndFinishExamination: " + e.getMessage());
            return false;
        }
    }

    public int saveMedicalRecordAndCreateLabRequests(long appointmentId, int doctorId, String symptoms, String diagnosis, String notes, int requestCount) {
        String insertLabSql = "INSERT INTO lab_requests (appointment_id, doctor_id, status, created_at) VALUES (?, ?, 'pending', NOW())";
        String moveToLabQueueSql = "UPDATE exam_queue SET status = 'in_lab' WHERE appointment_id = ?";
        int safeRequestCount = Math.max(1, requestCount);

        try (Connection conn = DBContext.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                if (!upsertMedicalRecordTx(conn, appointmentId, symptoms, diagnosis, notes)) {
                    conn.rollback();
                    return 0;
                }

                int createdCount = 0;
                for (int i = 0; i < safeRequestCount; i++) {
                    int requestId = insertLabRequestTx(conn, appointmentId, doctorId, insertLabSql);
                    if (requestId <= 0) {
                        conn.rollback();
                        return 0;
                    }
                    if (!ensureLabPaymentPendingTx(conn, appointmentId, requestId)) {
                        conn.rollback();
                        return 0;
                    }
                    createdCount++;
                }

                moveAppointmentToLabQueueTx(conn, appointmentId, moveToLabQueueSql);

                conn.commit();
                return createdCount;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Lỗi DB khi saveMedicalRecordAndCreateLabRequests: " + e.getMessage());
                return 0;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi lấy connection cho saveMedicalRecordAndCreateLabRequests: " + e.getMessage());
            return 0;
        }
    }

    public boolean hasIncompleteLabRequests(long appointmentId) {
        String sql = """
            SELECT 1
            FROM lab_requests
            WHERE appointment_id = ?
              AND status <> 'completed'
            LIMIT 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi hasIncompleteLabRequests: " + e.getMessage());
        }

        return true;
    }

    // === Private Transaction Helpers (nhận Connection làm tham số) ===

    private boolean upsertMedicalRecordTx(Connection conn, long appointmentId, String symptoms, String diagnosis, String notes) throws SQLException {
        String checkSql = "SELECT 1 FROM medical_records WHERE appointment_id = ? LIMIT 1";
        String updateRecordSql = """
            UPDATE medical_records
            SET symptoms = ?, diagnosis = ?, notes = ?, updated_at = NOW()
            WHERE appointment_id = ?
        """;
        String insertRecordSql = """
            INSERT INTO medical_records (appointment_id, doctor_id, symptoms, diagnosis, notes, updated_at)
            VALUES (?, (SELECT doctor_id FROM appointments WHERE appointment_id = ?), ?, ?, ?, NOW())
        """;

        boolean exists;
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setLong(1, appointmentId);
            try (ResultSet rs = check.executeQuery()) {
                exists = rs.next();
            }
        }

        if (exists) {
            try (PreparedStatement update = conn.prepareStatement(updateRecordSql)) {
                update.setString(1, symptoms);
                update.setString(2, diagnosis);
                update.setString(3, notes);
                update.setLong(4, appointmentId);
                return update.executeUpdate() > 0;
            }
        }

        try (PreparedStatement insert = conn.prepareStatement(insertRecordSql)) {
            insert.setLong(1, appointmentId);
            insert.setLong(2, appointmentId);
            insert.setString(3, symptoms);
            insert.setString(4, diagnosis);
            insert.setString(5, notes);
            return insert.executeUpdate() > 0;
        }
    }

    private int insertLabRequestTx(Connection conn, long appointmentId, int doctorId, String insertLabSql) throws SQLException {
        try (PreparedStatement insertLab = conn.prepareStatement(insertLabSql, Statement.RETURN_GENERATED_KEYS)) {
            insertLab.setLong(1, appointmentId);
            insertLab.setInt(2, doctorId);
            if (insertLab.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet keys = insertLab.getGeneratedKeys()) {
                if (!keys.next()) {
                    return 0;
                }
                return keys.getInt(1);
            }
        }
    }

    private void moveAppointmentToLabQueueTx(Connection conn, long appointmentId, String moveToLabQueueSql) throws SQLException {
        try (PreparedStatement moveQueue = conn.prepareStatement(moveToLabQueueSql)) {
            moveQueue.setLong(1, appointmentId);
            if (moveQueue.executeUpdate() == 0) {
                throw new SQLException("Appointment is not in exam_queue");
            }
        }
    }

    /**
     * Tạo payment pending cho xét nghiệm ngay khi bác sĩ chỉ định.
     * Nếu payment đã tồn tại cho appointment này thì giữ nguyên và coi như thành công.
     */
    private boolean ensureLabPaymentPendingTx(Connection conn, long appointmentId, int labRequestId) throws SQLException {
        String checkPaymentSql = "SELECT payment_id FROM payments WHERE lab_request_id = ? LIMIT 1";
        try (PreparedStatement check = conn.prepareStatement(checkPaymentSql)) {
            check.setInt(1, labRequestId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        java.math.BigDecimal labPrice = new java.math.BigDecimal("150000");
        String priceSql = "SELECT price FROM service_prices WHERE service_type = 'lab' LIMIT 1";
        try (PreparedStatement priceSt = conn.prepareStatement(priceSql)) {
            try (ResultSet priceRs = priceSt.executeQuery()) {
                if (priceRs.next() && priceRs.getBigDecimal("price") != null) {
                    labPrice = priceRs.getBigDecimal("price");
                }
            }
        }

        String insertPaymentSql = "INSERT INTO payments (appointment_id, lab_request_id, amount, method, status, created_at) VALUES (?, ?, ?, 'cash', 'pending', NOW())";
        try (PreparedStatement ins = conn.prepareStatement(insertPaymentSql)) {
            ins.setLong(1, appointmentId);
            ins.setInt(2, labRequestId);
            ins.setBigDecimal(3, labPrice);
            return ins.executeUpdate() > 0;
        }
    }

    public void updateDoctor(int doctorId, String qualification, int experience, String specialization) throws SQLException {
        String sqlGetUser = "SELECT user_id FROM doctors WHERE doctor_id = ?";
        String sqlUpdateDoctor = """
            UPDATE doctors
            SET specialization = ?, experience_years = ?
            WHERE doctor_id = ?
        """;
        String sqlUpdateStaff = """
            UPDATE staff_profiles
            SET academic_degree = ?
            WHERE user_id = ?
        """;
        String sqlInsertStaff = """
            INSERT INTO staff_profiles (user_id, academic_degree)
            VALUES (?, ?)
        """;

        try (Connection conn = DBContext.getConnection()) {
            int userId;
            try (PreparedStatement getUser = conn.prepareStatement(sqlGetUser)) {
                getUser.setInt(1, doctorId);
                try (ResultSet userRs = getUser.executeQuery()) {
                    if (!userRs.next()) {
                        return;
                    }
                    userId = userRs.getInt("user_id");
                }
            }

            try (PreparedStatement updateDoctorSt = conn.prepareStatement(sqlUpdateDoctor)) {
                updateDoctorSt.setString(1, specialization);
                updateDoctorSt.setInt(2, experience);
                updateDoctorSt.setInt(3, doctorId);
                updateDoctorSt.executeUpdate();
            }

            try (PreparedStatement st = conn.prepareStatement(sqlUpdateStaff)) {
                st.setString(1, qualification);
                st.setInt(2, userId);

                if (st.executeUpdate() == 0) {
                    try (PreparedStatement insert = conn.prepareStatement(sqlInsertStaff)) {
                        insert.setInt(1, userId);
                        insert.setString(2, qualification);
                        insert.executeUpdate();
                    }
                }
            }
        }
    }

    public boolean savePrescription(long appointmentId, int doctorId, String prescriptionNote, List<PrescriptionItem> prescriptionItems) {
        if (prescriptionItems == null || prescriptionItems.isEmpty()) {
            return false;
        }

        try (Connection conn = DBContext.getConnection()) {
            return savePrescriptionByRecordSchema(conn, appointmentId, doctorId, prescriptionItems);
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi savePrescription: " + e.getMessage());
            return false;
        }
    }

    private boolean savePrescriptionByRecordSchema(Connection conn, long appointmentId, int doctorId, List<PrescriptionItem> prescriptionItems) throws SQLException {
        long recordId = ensureMedicalRecordForAppointment(conn, appointmentId, doctorId);
        if (recordId <= 0) {
            return false;
        }

        String findPrescriptionSql = "SELECT prescription_id FROM prescriptions WHERE record_id = ? LIMIT 1";
        String insertPrescriptionSql = "INSERT INTO prescriptions (record_id, doctor_id, created_at) VALUES (?, ?, NOW())";
        String updatePrescriptionSql = "UPDATE prescriptions SET doctor_id = ?, created_at = NOW() WHERE prescription_id = ?";
        return savePrescriptionTransactional(conn, recordId, doctorId, prescriptionItems, findPrescriptionSql, insertPrescriptionSql, updatePrescriptionSql);
    }

    private long ensureMedicalRecordForAppointment(Connection conn, long appointmentId, int doctorId) throws SQLException {
        String findRecordSql = "SELECT record_id FROM medical_records WHERE appointment_id = ? LIMIT 1";
        try (PreparedStatement findRecord = conn.prepareStatement(findRecordSql)) {
            findRecord.setLong(1, appointmentId);
            try (ResultSet rs = findRecord.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("record_id");
                }
            }
        }

        String createRecordSql = """
            INSERT INTO medical_records (appointment_id, doctor_id, symptoms, diagnosis, notes, updated_at)
            VALUES (?, ?, '', '', '', NOW())
        """;
        try (PreparedStatement createRecord = conn.prepareStatement(createRecordSql, Statement.RETURN_GENERATED_KEYS)) {
            createRecord.setLong(1, appointmentId);
            createRecord.setInt(2, doctorId);
            if (createRecord.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet keys = createRecord.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        return 0;
    }

    private boolean savePrescriptionTransactional(Connection conn, long recordId,
            int doctorId,
            List<PrescriptionItem> prescriptionItems,
            String findPrescriptionSql,
            String insertPrescriptionSql,
            String updatePrescriptionSql) throws SQLException {
        String deleteItemsSql = "DELETE FROM prescription_items WHERE prescription_id = ?";
        String insertItemSql = """
            INSERT INTO prescription_items
                (prescription_id, medicine_name, dosage, frequency, duration)
            VALUES (?, ?, ?, ?, ?)
        """;

        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            int prescriptionId;
            try (PreparedStatement find = conn.prepareStatement(findPrescriptionSql)) {
                find.setLong(1, recordId);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) {
                        prescriptionId = rs.getInt("prescription_id");
                    } else {
                        try (PreparedStatement insertPrescription = conn.prepareStatement(insertPrescriptionSql, Statement.RETURN_GENERATED_KEYS)) {
                            insertPrescription.setLong(1, recordId);
                            insertPrescription.setInt(2, doctorId);
                            if (insertPrescription.executeUpdate() == 0) {
                                conn.rollback();
                                return false;
                            }
                            try (ResultSet keys = insertPrescription.getGeneratedKeys()) {
                                if (!keys.next()) {
                                    conn.rollback();
                                    return false;
                                }
                                prescriptionId = keys.getInt(1);
                            }
                        }
                    }
                }
            }

            try (PreparedStatement updatePrescription = conn.prepareStatement(updatePrescriptionSql)) {
                updatePrescription.setInt(1, doctorId);
                updatePrescription.setInt(2, prescriptionId);
                updatePrescription.executeUpdate();
            }

            try (PreparedStatement deleteItems = conn.prepareStatement(deleteItemsSql)) {
                deleteItems.setInt(1, prescriptionId);
                deleteItems.executeUpdate();
            }

            try (PreparedStatement insertItem = conn.prepareStatement(insertItemSql)) {
                for (PrescriptionItem item : prescriptionItems) {
                    String medicineName = item.getMedicineName();
                    if (medicineName == null || medicineName.isBlank()) {
                        medicineName = item.getMedicineId() > 0
                                ? "Medicine #" + item.getMedicineId()
                                : "Chưa cập nhật";
                    }
                    insertItem.setInt(1, prescriptionId);
                    insertItem.setString(2, medicineName);
                    insertItem.setString(3, item.getDosage());
                    insertItem.setString(4, item.getFrequency());
                    insertItem.setString(5, item.getDurationDays());
                    insertItem.addBatch();
                }
                insertItem.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public List<PrescriptionItem> getPrescriptionItemsByAppointment(long appointmentId) {
        List<PrescriptionItem> list = new ArrayList<>();

        String sql = """
            SELECT
                pi.item_id,
                pi.prescription_id,
                pi.medicine_name,
                pi.dosage,
                pi.frequency,
                pi.`duration` AS duration_value
            FROM prescriptions p
            JOIN medical_records mr ON mr.record_id = p.record_id
            JOIN prescription_items pi ON pi.prescription_id = p.prescription_id
            WHERE mr.appointment_id = ?
            ORDER BY pi.item_id
        """;

        try (Connection conn = DBContext.getConnection()) {
            loadPrescriptionItems(conn, list, sql, appointmentId);
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getPrescriptionItemsByAppointment: " + e.getMessage());
        }

        return list;
    }

    private void loadPrescriptionItems(Connection conn, List<PrescriptionItem> list, String sql, long appointmentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
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
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = """
            SELECT medicine_id, medicine_name, unit, default_dosage
            FROM medicines
            ORDER BY medicine_name
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Medicine medicine = new Medicine();
                    medicine.setMedicineId(rs.getInt("medicine_id"));
                    medicine.setMedicineName(rs.getString("medicine_name"));
                    medicine.setUnit(rs.getString("unit"));
                    medicine.setDefaultDosage(rs.getString("default_dosage"));
                    list.add(medicine);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getAllMedicines: " + e.getMessage());
        }

        return list;
    }

    public Doctor getDoctorById(String doctorID) {

        String sql = """
        SELECT
            d.doctor_id,
            u.full_name,
            d.specialization,
            sp.academic_degree AS qualification,
            d.experience_years,
            d.rating,
            d.price_booking,
            u.image_url,
            u.status AS user_status
        FROM doctors d
        JOIN users u ON d.user_id = u.user_id
        LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id
        WHERE d.doctor_id = ?
          AND u.status = 'active'
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, Integer.parseInt(doctorID));

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Doctor d = new Doctor();
                    d.setDoctorId(rs.getInt("doctor_id"));
                    d.setFullName(rs.getString("full_name"));
                    d.setSpecialization(rs.getString("specialization"));
                    d.setQualification(rs.getString("qualification"));
                    d.setExperience_years(rs.getInt("experience_years"));
                    d.setRating(rs.getDouble("rating"));
                    d.setPrice(rs.getDouble("price_booking"));
                    d.setImage(rs.getString("image_url"));
                    d.setStatus(rs.getString("user_status"));
                    return d;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getDoctorById: " + e.getMessage());
        }

        return null;
    }

    public List<Doctor> getAllDoctors() {

        List<Doctor> list = new ArrayList<>();

        String sql = """
        SELECT
            d.doctor_id,
            u.full_name,
            d.specialization,
            sp.academic_degree AS qualification,
            d.experience_years,
            d.rating,
            d.price_booking,
            u.image_url
        FROM doctors d
        JOIN users u ON d.user_id = u.user_id
        LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id
        WHERE u.status = 'active'
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Doctor d = new Doctor();
                d.setDoctorId(rs.getInt("doctor_id"));
                d.setFullName(rs.getString("full_name"));
                d.setSpecialization(rs.getString("specialization"));
                d.setQualification(rs.getString("qualification"));
                d.setExperience_years(rs.getInt("experience_years"));
                d.setRating(rs.getDouble("rating"));
                d.setPrice(rs.getDouble("price_booking"));
                d.setImage(rs.getString("image_url"));
                list.add(d);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getAllDoctors: " + e.getMessage());
        }

        return list;
    }


    public List<Doctor> filterDoctors(
            String name,
            String priceFrom,
            String priceTo,
            String experience,
            String sort) {

        List<Doctor> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT
            d.doctor_id,
            u.full_name,
            d.specialization,
            sp.academic_degree AS qualification,
            d.experience_years,
            d.rating,
            d.price_booking,
            u.image_url
        FROM doctors d
        JOIN users u ON d.user_id = u.user_id
        LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id
        WHERE u.status = 'active'
    """);

        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND u.full_name LIKE ? ");
        }

        if (priceFrom != null && !priceFrom.isEmpty()) {
            sql.append(" AND d.price_booking >= ? ");
        }

        if (priceTo != null && !priceTo.isEmpty()) {
            sql.append(" AND d.price_booking <= ? ");
        }

        if (experience != null && !experience.isEmpty()) {
            sql.append(" AND d.experience_years >= ? ");
        }

        if ("priceAsc".equals(sort)) {
            sql.append(" ORDER BY d.price_booking ASC ");
        } else if ("priceDesc".equals(sort)) {
            sql.append(" ORDER BY d.price_booking DESC ");
        } else if ("rating".equals(sort)) {
            sql.append(" ORDER BY d.rating DESC ");
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (name != null && !name.trim().isEmpty()) {
                st.setString(index++, "%" + name + "%");
            }

            if (priceFrom != null && !priceFrom.isEmpty()) {
                st.setDouble(index++, Double.parseDouble(priceFrom));
            }

            if (priceTo != null && !priceTo.isEmpty()) {
                st.setDouble(index++, Double.parseDouble(priceTo));
            }

            if (experience != null && !experience.isEmpty()) {
                st.setInt(index++, Integer.parseInt(experience));
            }

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Doctor d = new Doctor();
                    d.setDoctorId(rs.getInt("doctor_id"));
                    d.setFullName(rs.getString("full_name"));
                    d.setSpecialization(rs.getString("specialization"));
                    d.setQualification(rs.getString("qualification"));
                    d.setExperience_years(rs.getInt("experience_years"));
                    d.setRating(rs.getDouble("rating"));
                    d.setPrice(rs.getDouble("price_booking"));
                    d.setImage(rs.getString("image_url"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi filterDoctors: " + e.getMessage());
        }

        return list;
    }

    public void toggleUserStatusById(int userId) throws SQLException {
        String sql = "UPDATE users SET status = CASE WHEN status = 'active' THEN 'inactive' ELSE 'active' END WHERE user_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.executeUpdate();
        }
    }

    public java.util.List<java.util.Map<String, Object>> getTopRatedDoctors(int limit) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        String sql = "SELECT d.doctor_id, u.full_name, d.specialization, " +
                     "sp.academic_degree AS qualification, d.experience_years, d.rating, u.image_url " +
                     "FROM doctors d JOIN users u ON d.user_id = u.user_id " +
                     "LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id " +
                     "WHERE u.status = 'active' ORDER BY d.rating DESC LIMIT ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> doc = new java.util.HashMap<>();
                    doc.put("doctorId", rs.getInt("doctor_id"));
                    doc.put("fullName", rs.getString("full_name"));
                    doc.put("specialization", rs.getString("specialization"));
                    doc.put("qualification", rs.getString("qualification"));
                    doc.put("exp", rs.getInt("experience_years"));
                    doc.put("rating", rs.getDouble("rating"));
                    doc.put("image", rs.getString("image_url"));
                    list.add(doc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DB khi getTopRatedDoctors: " + e.getMessage());
        }
        return list;
    }

    // === Private Helper: Map ResultSet -> DoctorQueueItem ===
    private DoctorQueueItem mapQueueItem(ResultSet rs) throws SQLException {
        DoctorQueueItem item = new DoctorQueueItem();
        item.setQueuePosition(rs.getInt("queue_position"));
        item.setAppointmentId(rs.getLong("appointment_id"));
        item.setPatientId(rs.getLong("patient_id"));
        item.setPatientName(rs.getString("patient_name"));
        item.setGender(rs.getString("gender"));
        item.setDob(rs.getDate("dob"));
        item.setSymptom(rs.getString("symptom"));
        item.setStatus(rs.getString("status"));
        return item;
    }
}
