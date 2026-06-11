package dal;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Doctor;
import model.DoctorShift;
import model.ScheduleChangeRequest;
import model.ScheduleSwapShiftOption;

public class DoctorScheduleDAO extends DBContext {

    private static final String SWAP_SHIFT_META_PREFIX = "[[SWAP_SHIFT_ID:";
    private static final String SWAP_SHIFT_META_SUFFIX = "]]";
    private String lastReviewError;

    public String getLastReviewError() {
        return lastReviewError;
    }

    // Lay danh sach ca lam viec dang active cua mot bac si.
    public List<DoctorShift> getDoctorShifts(int doctorId) {
        List<DoctorShift> list = new ArrayList<>();


        String sql = """
            SELECT shift_id, doctor_id, day_of_week,
                   start_time, end_time, max_patients
            FROM doctor_shifts
            WHERE doctor_id = ?
              AND status = 'active'
            ORDER BY day_of_week, start_time
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                list.add(mapDoctorShift(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lay danh sach bac si dang active de lap lich.
    public List<Doctor> getActiveDoctorsForSchedule() {
        List<Doctor> list = new ArrayList<>();
        syncDoctorRowsForAllDoctorUsers();
        String sql = """
            SELECT d.doctor_id, d.user_id, d.specialization, u.full_name
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            WHERE u.role = 'doctor' AND u.status = 'active'
            ORDER BY u.full_name
        """;

        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Doctor d = new Doctor();
                d.setDoctorId(rs.getInt("doctor_id"));
                d.setUserId(rs.getInt("user_id"));
                d.setSpecialization(rs.getString("specialization"));
                d.setFullName(rs.getString("full_name"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Dong bo bang doctors tu danh sach user co role doctor.
    public void syncDoctorRowsForAllDoctorUsers() {
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

    // Them ca moi hoac kich hoat lai ca cu neu trung khung gio.
    public void addDoctorShift(int doctorId, int dayOfWeek, LocalTime startTime, LocalTime endTime, int maxPatients) throws SQLException {
        Integer inactiveShiftId = findInactiveExactShiftId(doctorId, dayOfWeek, startTime, endTime);
        if (inactiveShiftId != null) {
            reactivateDoctorShift(inactiveShiftId, maxPatients);
            return;
        }

        String sql = """
            INSERT INTO doctor_shifts (doctor_id, day_of_week, start_time, end_time, max_patients, status)
            VALUES (?, ?, ?, ?, ?, 'active')
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            st.setInt(2, dayOfWeek);
            st.setTime(3, Time.valueOf(startTime));
            st.setTime(4, Time.valueOf(endTime));
            st.setInt(5, maxPatients);
            st.executeUpdate();
        }
    }

    // Kiem tra doctor_id co ton tai hay khong.
    public boolean doctorExists(int doctorId) {
        String sql = "SELECT 1 FROM doctors WHERE doctor_id = ? LIMIT 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiem tra bac si co dang o trang thai active hay khong.
    public boolean isDoctorActive(int doctorId) {
        String sql = """
            SELECT 1
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            WHERE d.doctor_id = ? AND u.role = 'doctor' AND u.status = 'active'
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lay thong tin chi tiet cua mot ca lam viec theo shift id.
    public DoctorShift getDoctorShiftById(int shiftId) {
        String sql = """
            SELECT shift_id, doctor_id, day_of_week, start_time, end_time, max_patients
            FROM doctor_shifts
            WHERE shift_id = ?
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapDoctorShift(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Kiem tra shift id co ton tai hay khong.
    public boolean shiftExists(int shiftId) {
        String sql = "SELECT 1 FROM doctor_shifts WHERE shift_id = ? LIMIT 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiem tra ca co thuoc ve dung bac si duoc chi dinh hay khong.
    public boolean isShiftOwnedByDoctor(int shiftId, int doctorId) {
        String sql = """
            SELECT 1
            FROM doctor_shifts
            WHERE shift_id = ? AND doctor_id = ?
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            st.setInt(2, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiem tra ca con lich hen hien tai hoac tuong lai hay khong.
    public boolean hasUpcomingAppointmentsForShift(int shiftId) {
        String sql = """
            SELECT 1
            FROM appointments
            WHERE shift_id = ?
              AND (
                  appointment_date > CURRENT_DATE
                  OR (appointment_date = CURRENT_DATE AND appointment_time >= CURRENT_TIME)
              )
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiem tra ca da tung duoc dat lich hen hay chua.
    public boolean hasAnyAppointmentsForShift(int shiftId) {
        String sql = "SELECT 1 FROM appointments WHERE shift_id = ? LIMIT 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiem tra ca co lich hen trong mot ngay cu the hay khong.
    public boolean hasAppointmentsForShiftOnDate(int shiftId, Date workDate) {
        String sql = "SELECT 1 FROM appointments WHERE shift_id = ? AND appointment_date = ? LIMIT 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            st.setDate(2, workDate);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lay thong tin ca theo hieu luc tam thoi (neu co) tai mot ngay cu the.
    public EffectiveShiftState getEffectiveShiftStateForDate(int shiftId, Date workDate) {
        DoctorShift baseShift = getDoctorShiftById(shiftId);
        if (baseShift == null || workDate == null) {
            return null;
        }

        String sql = """
            SELECT effective_day_of_week, effective_start_time, effective_end_time
            FROM (
                SELECT r.requested_at AS req_at,
                       i.work_date AS effective_work_date,
                       i.day_of_week AS effective_day_of_week,
                       i.start_time AS effective_start_time,
                       i.end_time AS effective_end_time
                FROM schedule_change_requests r
                JOIN schedule_change_request_items i ON r.request_id = i.request_id
                WHERE r.status = 'APPROVED'
                  AND r.request_type = 'TEMPORARY'
                  AND r.scope_type = 'ONE_DATE'
                  AND i.action_type = 'UPDATE'
                  AND i.target_shift_id = ?

                UNION ALL

                SELECT r.requested_at AS req_at,
                       DATE_ADD(
                           i.work_date,
                           INTERVAL (
                               (CASE WHEN s_old.day_of_week = 0 THEN 7 ELSE s_old.day_of_week END)
                               - (CASE WHEN i.day_of_week = 0 THEN 7 ELSE i.day_of_week END)
                           ) DAY
                       ) AS effective_work_date,
                       s_old.day_of_week AS effective_day_of_week,
                       s_old.start_time AS effective_start_time,
                       s_old.end_time AS effective_end_time
                FROM schedule_change_requests r
                JOIN schedule_change_request_items i ON r.request_id = i.request_id
                JOIN doctor_shifts s_old ON i.target_shift_id = s_old.shift_id
                JOIN doctor_shifts s_new ON s_new.shift_id = (
                    SELECT s2.shift_id
                    FROM doctor_shifts s2
                    WHERE s2.day_of_week = i.day_of_week
                      AND s2.start_time = i.start_time
                      AND s2.end_time = i.end_time
                      AND s2.doctor_id <> r.doctor_id
                    ORDER BY s2.shift_id
                    LIMIT 1
                )
                WHERE r.status = 'APPROVED'
                  AND r.request_type = 'TEMPORARY'
                  AND r.scope_type = 'ONE_DATE'
                  AND i.action_type = 'UPDATE'
                  AND s_new.shift_id = ?
            ) x
            WHERE effective_work_date = ?
            ORDER BY req_at DESC
            LIMIT 1
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            st.setInt(2, shiftId);
            st.setDate(3, workDate);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new EffectiveShiftState(
                            rs.getInt("effective_day_of_week"),
                            rs.getTime("effective_start_time").toLocalTime(),
                            rs.getTime("effective_end_time").toLocalTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new EffectiveShiftState(
                baseShift.getDayOfWeek(),
                baseShift.getStartTime(),
                baseShift.getEndTime()
        );
    }

    // Tim shift active theo bac si + thu + khung gio.
    public Integer findActiveShiftIdByDoctorDayAndTime(int doctorId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        String sql = """
            SELECT shift_id
            FROM doctor_shifts
            WHERE doctor_id = ?
              AND day_of_week = ?
              AND start_time = ?
              AND end_time = ?
              AND status = 'active'
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            st.setInt(2, dayOfWeek);
            st.setTime(3, Time.valueOf(startTime));
            st.setTime(4, Time.valueOf(endTime));
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shift_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Kiem tra bac si co lich hen trong khung gio cu the cua mot ngay hay khong.
    public boolean hasAppointmentsForDoctorInTimeWindow(int doctorId, Date workDate, LocalTime startTime, LocalTime endTime) {
        if (workDate == null || startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            return false;
        }
        String sql = """
            SELECT 1
            FROM appointments
            WHERE doctor_id = ?
              AND appointment_date = ?
              AND appointment_time >= ?
              AND appointment_time < ?
              AND status <> 'cancelled'
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            st.setDate(2, workDate);
            st.setTime(3, Time.valueOf(startTime));
            st.setTime(4, Time.valueOf(endTime));
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cap nhat lai thong tin thu va khung gio cua ca lam viec.
    public void updateDoctorShift(int shiftId, int dayOfWeek, LocalTime startTime, LocalTime endTime, int maxPatients) throws SQLException {
        String sql = """
            UPDATE doctor_shifts
            SET day_of_week = ?, start_time = ?, end_time = ?, max_patients = ?
            WHERE shift_id = ?
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, dayOfWeek);
            st.setTime(2, Time.valueOf(startTime));
            st.setTime(3, Time.valueOf(endTime));
            st.setInt(4, maxPatients);
            st.setInt(5, shiftId);
            st.executeUpdate();
        }
    }

    // Xoa mem ca lam viec bang cach doi status sang inactive.
    public void deleteDoctorShift(int shiftId) throws SQLException {
        String sql = "UPDATE doctor_shifts SET status = 'inactive' WHERE shift_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            st.executeUpdate();
        }
    }

    // Kiem tra khung gio moi co bi trung voi ca active khac hay khong.
    public boolean hasShiftConflict(int doctorId, int dayOfWeek, LocalTime startTime, LocalTime endTime, Integer excludeShiftId) {
        StringBuilder sql = new StringBuilder("""
            SELECT 1
            FROM doctor_shifts
            WHERE doctor_id = ?
              AND day_of_week = ?
              AND status = 'active'
              AND start_time < ?
              AND end_time > ?
        """);
        if (excludeShiftId != null) {
            sql.append(" AND shift_id <> ? ");
        }
        sql.append(" LIMIT 1 ");

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            int index = 1;
            st.setInt(index++, doctorId);
            st.setInt(index++, dayOfWeek);
            st.setTime(index++, Time.valueOf(endTime));
            st.setTime(index++, Time.valueOf(startTime));
            if (excludeShiftId != null) {
                st.setInt(index, excludeShiftId);
            }

            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lay cac ca active cua bac si trong mot thu cu the.
    public List<DoctorShift> getShiftsByDoctorAndDay(int doctorId, int dayOfWeek) {
        List<DoctorShift> list = new ArrayList<>();

        String sql = """
        SELECT shift_id, start_time, end_time, max_patients
        FROM doctor_shifts
        WHERE doctor_id = ? AND day_of_week = ?
          AND status = 'active'
    """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            st.setInt(2, dayOfWeek);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                DoctorShift s = new DoctorShift();
                s.setShiftId(rs.getInt("shift_id"));
                s.setStartTime(rs.getTime("start_time").toLocalTime());
                s.setEndTime(rs.getTime("end_time").toLocalTime());
                s.setMaxPatients(rs.getInt("max_patients"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lay danh sach ca co the doi cua bac si khac trong cung thu.
    public List<ScheduleSwapShiftOption> getSwapShiftOptionsByDate(int requesterDoctorId, int dayOfWeek) {
        List<ScheduleSwapShiftOption> list = new ArrayList<>();
        String sql = """
            SELECT s.shift_id, s.doctor_id, s.day_of_week, s.start_time, s.end_time,
                   u.full_name AS doctor_name
            FROM doctor_shifts s
            JOIN doctors d ON d.doctor_id = s.doctor_id
            JOIN users u ON u.user_id = d.user_id
            WHERE s.day_of_week = ?
              AND s.doctor_id <> ?
              AND s.status = 'active'
              AND u.role = 'doctor'
              AND u.status = 'active'
            ORDER BY u.full_name, s.start_time
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, dayOfWeek);
            st.setInt(2, requesterDoctorId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                ScheduleSwapShiftOption option = new ScheduleSwapShiftOption();
                option.setShiftId(rs.getInt("shift_id"));
                option.setDoctorId(rs.getInt("doctor_id"));
                option.setDoctorName(rs.getString("doctor_name"));
                option.setDayOfWeek(rs.getInt("day_of_week"));
                option.setStartTime(rs.getTime("start_time").toLocalTime());
                option.setEndTime(rs.getTime("end_time").toLocalTime());
                list.add(option);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lay lich su yeu cau doi lich cua mot bac si.
    public List<ScheduleChangeRequest> getScheduleChangeRequestsByDoctor(int doctorId, int limit) {
        List<ScheduleChangeRequest> list = new ArrayList<>();
        String sql = """
            SELECT r.request_id, r.doctor_id, r.request_type, r.scope_type,
                   r.reason, r.status, r.requested_at, r.admin_note,
                   i.action_type, i.target_shift_id, i.work_date, i.day_of_week,
                   i.start_time, i.end_time, i.max_patients,
                               s_old.day_of_week AS old_day_of_week,
                               s_old.start_time AS old_start_time,
                               s_old.end_time AS old_end_time,
                               u_new.full_name AS new_doctor_name,
                               CASE
                                   WHEN i.work_date IS NOT NULL AND s_old.day_of_week IS NOT NULL AND i.day_of_week IS NOT NULL
                                   THEN DATE_ADD(
                                       i.work_date,
                                       INTERVAL (
                                           (CASE WHEN s_old.day_of_week = 0 THEN 7 ELSE s_old.day_of_week END)
                                           - (CASE WHEN i.day_of_week = 0 THEN 7 ELSE i.day_of_week END)
                                       ) DAY
                                   )
                                   ELSE NULL
                               END AS old_work_date
                        FROM schedule_change_requests r
                        LEFT JOIN schedule_change_request_items i ON r.request_id = i.request_id
                        LEFT JOIN doctor_shifts s_old ON i.target_shift_id = s_old.shift_id
                        LEFT JOIN doctor_shifts s_new ON s_new.shift_id = (
                            SELECT s2.shift_id
                            FROM doctor_shifts s2
                            WHERE i.action_type = 'UPDATE'
                              AND s2.day_of_week = i.day_of_week
                              AND s2.start_time = i.start_time
                              AND s2.end_time = i.end_time
                              AND s2.doctor_id <> r.doctor_id
                            ORDER BY s2.shift_id
                            LIMIT 1
                        )
                        LEFT JOIN doctors d_new ON s_new.doctor_id = d_new.doctor_id
                        LEFT JOIN users u_new ON d_new.user_id = u_new.user_id
            WHERE r.doctor_id = ?
            ORDER BY r.requested_at DESC
            LIMIT ?
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            st.setInt(2, limit);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                ScheduleChangeRequest request = mapScheduleChangeRequestBasic(rs);

                int oldDayOfWeek = rs.getInt("old_day_of_week");
                request.setOldDayOfWeek(rs.wasNull() ? null : oldDayOfWeek);

                Time oldStartTime = rs.getTime("old_start_time");
                if (oldStartTime != null) {
                    request.setOldStartTime(oldStartTime.toLocalTime());
                }

                Time oldEndTime = rs.getTime("old_end_time");
                if (oldEndTime != null) {
                    request.setOldEndTime(oldEndTime.toLocalTime());
                }

                request.setNewDoctorName(rs.getString("new_doctor_name"));
                Integer counterpartShiftId = extractCounterpartShiftId(rs.getString("reason"));
                request.setCounterpartShiftId(counterpartShiftId);
                if (counterpartShiftId != null) {
                    DoctorShift counterpartShift = getDoctorShiftById(counterpartShiftId);
                    if (counterpartShift != null) {
                        request.setCounterpartDoctorId(counterpartShift.getDoctorId());
                    }
                    String preferredDoctorName = getDoctorNameByShiftId(counterpartShiftId);
                    if (preferredDoctorName != null && !preferredDoctorName.isBlank()) {
                        request.setNewDoctorName(preferredDoctorName);
                    }
                }
                request.setOldWorkDate(rs.getDate("old_work_date"));

                list.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lay danh sach request cho admin voi bo loc mac dinh.
    public List<ScheduleChangeRequest> getScheduleChangeRequestsForAdmin(String statusFilter) {
        return getScheduleChangeRequestsForAdmin(statusFilter, "ALL", "ALL", "");
    }

    // Lay danh sach request cho admin theo nhieu tieu chi loc.
    public List<ScheduleChangeRequest> getScheduleChangeRequestsForAdmin(
            String statusFilter,
            String requestTypeFilter,
            String actionTypeFilter,
            String keyword
    ) {
        List<ScheduleChangeRequest> list = new ArrayList<>();
        boolean hasStatusFilter = statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter);
        boolean hasRequestTypeFilter = requestTypeFilter != null && !requestTypeFilter.isBlank() && !"ALL".equalsIgnoreCase(requestTypeFilter);
        boolean hasActionTypeFilter = actionTypeFilter != null && !actionTypeFilter.isBlank() && !"ALL".equalsIgnoreCase(actionTypeFilter);
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        StringBuilder sql = new StringBuilder("""
            SELECT r.request_id, r.doctor_id, r.request_type, r.scope_type,
                   r.reason, r.status, r.requested_at, r.admin_note,
                   i.action_type, i.target_shift_id, i.work_date, i.day_of_week,
                   i.start_time, i.end_time, i.max_patients,
                   u.full_name AS doctor_name,
                   u_old.full_name AS old_doctor_name,
                   s_old.day_of_week AS old_day_of_week,
                   s_old.start_time AS old_start_time,
                   s_old.end_time AS old_end_time,
                   u_new.full_name AS new_doctor_name,
                   CASE
                       WHEN i.work_date IS NOT NULL AND s_old.day_of_week IS NOT NULL AND i.day_of_week IS NOT NULL
                       THEN DATE_ADD(
                           i.work_date,
                           INTERVAL (
                               (CASE WHEN s_old.day_of_week = 0 THEN 7 ELSE s_old.day_of_week END)
                               - (CASE WHEN i.day_of_week = 0 THEN 7 ELSE i.day_of_week END)
                           ) DAY
                       )
                       ELSE NULL
                   END AS old_work_date
            FROM schedule_change_requests r
            JOIN doctors d ON r.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            LEFT JOIN schedule_change_request_items i ON r.request_id = i.request_id
            LEFT JOIN doctor_shifts s_old ON i.target_shift_id = s_old.shift_id
            LEFT JOIN doctors d_old ON s_old.doctor_id = d_old.doctor_id
            LEFT JOIN users u_old ON d_old.user_id = u_old.user_id
            LEFT JOIN doctor_shifts s_new ON s_new.shift_id = (
                SELECT s2.shift_id
                FROM doctor_shifts s2
                WHERE i.action_type = 'UPDATE'
                  AND s2.day_of_week = i.day_of_week
                  AND s2.start_time = i.start_time
                  AND s2.end_time = i.end_time
                  AND s2.doctor_id <> r.doctor_id
                ORDER BY s2.shift_id
                LIMIT 1
            )
            LEFT JOIN doctors d_new ON s_new.doctor_id = d_new.doctor_id
            LEFT JOIN users u_new ON d_new.user_id = u_new.user_id
        """);

        sql.append(" WHERE 1=1 ");
        if (hasStatusFilter) {
            sql.append(" AND r.status = ? ");
        }
        if (hasRequestTypeFilter) {
            sql.append(" AND r.request_type = ? ");
        }
        if (hasActionTypeFilter) {
            sql.append(" AND i.action_type = ? ");
        }
        if (hasKeyword) {
            sql.append(" AND (u.full_name LIKE ? OR r.reason LIKE ?) ");
        }
        sql.append(" ORDER BY r.requested_at DESC ");

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (hasStatusFilter) {
                st.setString(index++, statusFilter.trim().toUpperCase());
            }
            if (hasRequestTypeFilter) {
                st.setString(index++, requestTypeFilter.trim().toUpperCase());
            }
            if (hasActionTypeFilter) {
                st.setString(index++, actionTypeFilter.trim().toUpperCase());
            }
            if (hasKeyword) {
                String keywordLike = "%" + keyword.trim() + "%";
                st.setString(index++, keywordLike);
                st.setString(index++, keywordLike);
            }
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                ScheduleChangeRequest request = mapScheduleChangeRequestBasic(rs);
                request.setDoctorName(rs.getString("doctor_name"));
                request.setOldDoctorName(rs.getString("old_doctor_name"));

                int oldDayOfWeek = rs.getInt("old_day_of_week");
                request.setOldDayOfWeek(rs.wasNull() ? null : oldDayOfWeek);

                Time oldStartTime = rs.getTime("old_start_time");
                if (oldStartTime != null) {
                    request.setOldStartTime(oldStartTime.toLocalTime());
                }

                Time oldEndTime = rs.getTime("old_end_time");
                if (oldEndTime != null) {
                    request.setOldEndTime(oldEndTime.toLocalTime());
                }

                request.setNewDoctorName(rs.getString("new_doctor_name"));
                Integer counterpartShiftId = extractCounterpartShiftId(rs.getString("reason"));
                request.setCounterpartShiftId(counterpartShiftId);
                if (counterpartShiftId != null) {
                    DoctorShift counterpartShift = getDoctorShiftById(counterpartShiftId);
                    if (counterpartShift != null) {
                        request.setCounterpartDoctorId(counterpartShift.getDoctorId());
                    }
                    String preferredDoctorName = getDoctorNameByShiftId(counterpartShiftId);
                    if (preferredDoctorName != null && !preferredDoctorName.isBlank()) {
                        request.setNewDoctorName(preferredDoctorName);
                    }
                }
                request.setOldWorkDate(rs.getDate("old_work_date"));
                list.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Dem so request dang cho duyet.
    public int countPendingScheduleChangeRequests() {
        String sql = "SELECT COUNT(*) FROM schedule_change_requests WHERE status = 'PENDING'";
        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Duyet hoac tu choi request va ap dung thay doi neu can.
    public boolean reviewScheduleChangeRequest(int requestId, String decision, String adminNote) {
        lastReviewError = null;
        String normalizedDecision = decision == null ? "" : decision.trim().toUpperCase();
        boolean shouldApply = "APPROVED".equals(normalizedDecision);
        String reviewSql = """
            UPDATE schedule_change_requests
            SET status = ?, admin_note = ?
            WHERE request_id = ? AND status = 'PENDING'
        """;

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            PendingScheduleReview request = getPendingScheduleReviewForUpdate(requestId);
            if (request == null) {
                connection.rollback();
                lastReviewError = "Đơn đã được xử lý trước đó";
                return false;
            }

            if (shouldApply && isRequestSuperseded(request)) {
                String note = "Ca #" + request.targetShiftId + " đã bị thay đổi bởi yêu cầu khác đã được duyệt.";
                applyAutoRejectWithNote(request.requestId, note);
                connection.commit();
                lastReviewError = note;
                return false;
            }

            if (shouldApply && !applyApprovedScheduleRequest(request)) {
                connection.rollback();
                lastReviewError = "Không thể duyệt đơn vì dữ liệu ca làm việc đã thay đổi";
                return false;
            }

            try (PreparedStatement st = connection.prepareStatement(reviewSql)) {
                st.setString(1, normalizedDecision);
                if (adminNote == null || adminNote.isBlank()) {
                    st.setNull(2, Types.VARCHAR);
                } else {
                    st.setString(2, adminNote.trim());
                }
                st.setInt(3, requestId);
                if (st.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            lastReviewError = "Lỗi khi xử lý duyệt đơn.";
            return false;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isRequestSuperseded(PendingScheduleReview request) throws SQLException {
        if (request == null || request.targetShiftId == null || request.requestedAt == null) {
            return false;
        }
        String sql = """
            SELECT 1
            FROM schedule_change_requests r2
            JOIN schedule_change_request_items i2 ON r2.request_id = i2.request_id
            WHERE r2.status = 'APPROVED'
              AND i2.target_shift_id = ?
              AND r2.requested_at > ?
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, request.targetShiftId);
            st.setTimestamp(2, request.requestedAt);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void applyAutoRejectWithNote(int requestId, String note) throws SQLException {
        String sql = """
            UPDATE schedule_change_requests
            SET status = 'REJECTED',
                admin_note = ?
            WHERE request_id = ?
              AND status = 'PENDING'
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, note);
            st.setInt(2, requestId);
            st.executeUpdate();
        }
    }

    // Tao yeu cau doi lich moi va luu item chi tiet di kem.
    public boolean createScheduleChangeRequest(
            int doctorId,
            String requestType,
            String scopeType,
            String reason,
            String actionType,
            Integer targetShiftId,
            Integer counterpartShiftId,
            Date workDate,
            Integer dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxPatients
    ) {
        String requestReason = appendCounterpartShiftMeta(reason, actionType, counterpartShiftId);
        String insertRequestSql = """
            INSERT INTO schedule_change_requests
            (doctor_id, request_type, scope_type, reason, status, requested_at)
            VALUES (?, ?, ?, ?, 'PENDING', NOW())
        """;

        String insertItemSql = """
            INSERT INTO schedule_change_request_items
            (request_id, action_type, target_shift_id, work_date, day_of_week, start_time, end_time, max_patients)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            int requestId;
            try (PreparedStatement insertRequest = connection.prepareStatement(insertRequestSql, Statement.RETURN_GENERATED_KEYS)) {
                insertRequest.setInt(1, doctorId);
                insertRequest.setString(2, requestType);
                insertRequest.setString(3, scopeType);
                insertRequest.setString(4, requestReason);
                if (insertRequest.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet keys = insertRequest.getGeneratedKeys()) {
                    if (!keys.next()) {
                        connection.rollback();
                        return false;
                    }
                    requestId = keys.getInt(1);
                }
            }

            try (PreparedStatement insertItem = connection.prepareStatement(insertItemSql)) {
                insertItem.setInt(1, requestId);
                insertItem.setString(2, actionType);

                if (targetShiftId == null) {
                    insertItem.setNull(3, Types.INTEGER);
                } else {
                    insertItem.setInt(3, targetShiftId);
                }

                int index = 4;
                if (workDate == null) {
                    insertItem.setNull(index++, Types.DATE);
                } else {
                    insertItem.setDate(index++, workDate);
                }

                if (dayOfWeek == null) {
                    insertItem.setNull(index++, Types.TINYINT);
                } else {
                    insertItem.setInt(index++, dayOfWeek);
                }

                if (startTime == null) {
                    insertItem.setNull(index++, Types.TIME);
                } else {
                    insertItem.setTime(index++, Time.valueOf(startTime));
                }

                if (endTime == null) {
                    insertItem.setNull(index++, Types.TIME);
                } else {
                    insertItem.setTime(index++, Time.valueOf(endTime));
                }

                if (maxPatients == null) {
                    insertItem.setNull(index++, Types.INTEGER);
                } else {
                    insertItem.setInt(index++, maxPatients);
                }

                if (insertItem.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasApprovedTemporaryParticipationForShiftOnDate(int shiftId, Date workDate) {
        if (shiftId <= 0 || workDate == null) {
            return false;
        }
        String sql = """
            SELECT 1
            FROM (
                SELECT i.target_shift_id AS requester_shift_id,
                       (
                           SELECT s2.shift_id
                           FROM doctor_shifts s2
                           WHERE s2.day_of_week = i.day_of_week
                             AND s2.start_time = i.start_time
                             AND s2.end_time = i.end_time
                             AND s2.doctor_id <> r.doctor_id
                           ORDER BY s2.shift_id
                           LIMIT 1
                       ) AS counterpart_shift_id,
                       i.work_date AS new_work_date,
                       CASE
                           WHEN s_old.day_of_week IS NOT NULL AND i.day_of_week IS NOT NULL
                           THEN DATE_ADD(
                               i.work_date,
                               INTERVAL (
                                   (CASE WHEN s_old.day_of_week = 0 THEN 7 ELSE s_old.day_of_week END)
                                   - (CASE WHEN i.day_of_week = 0 THEN 7 ELSE i.day_of_week END)
                               ) DAY
                           )
                           ELSE NULL
                       END AS old_work_date
                FROM schedule_change_requests r
                JOIN schedule_change_request_items i ON r.request_id = i.request_id
                LEFT JOIN doctor_shifts s_old ON i.target_shift_id = s_old.shift_id
                WHERE i.action_type = 'UPDATE'
                  AND r.scope_type = 'ONE_DATE'
                  AND r.request_type = 'TEMPORARY'
                  AND r.status = 'APPROVED'
            ) x
            WHERE (x.requester_shift_id = ? OR x.counterpart_shift_id = ?)
              AND (? = x.new_work_date OR ? = x.old_work_date)
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            st.setInt(2, shiftId);
            st.setDate(3, workDate);
            st.setDate(4, workDate);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Chuyen du lieu mot dong result set thanh DoctorShift.
    private DoctorShift mapDoctorShift(ResultSet rs) throws SQLException {
        DoctorShift s = new DoctorShift();
        s.setShiftId(rs.getInt("shift_id"));
        s.setDoctorId(rs.getInt("doctor_id"));
        s.setDayOfWeek(rs.getInt("day_of_week"));
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setMaxPatients(rs.getInt("max_patients"));
        return s;
    }

    // Tim ca inactive co khung gio giong het de tai su dung.
    private Integer findInactiveExactShiftId(int doctorId, int dayOfWeek, LocalTime startTime, LocalTime endTime) throws SQLException {
        String sql = """
            SELECT shift_id
            FROM doctor_shifts
            WHERE doctor_id = ?
              AND day_of_week = ?
              AND start_time = ?
              AND end_time = ?
              AND status = 'inactive'
            ORDER BY shift_id
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, doctorId);
            st.setInt(2, dayOfWeek);
            st.setTime(3, Time.valueOf(startTime));
            st.setTime(4, Time.valueOf(endTime));
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shift_id");
                }
            }
        }
        return null;
    }

    // Kich hoat lai ca cu va cap nhat lai so benh nhan toi da.
    private void reactivateDoctorShift(int shiftId, int maxPatients) throws SQLException {
        String sql = """
            UPDATE doctor_shifts
            SET status = 'active',
                max_patients = ?
            WHERE shift_id = ?
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, maxPatients);
            st.setInt(2, shiftId);
            st.executeUpdate();
        }
    }

    // Map du lieu co ban cua request tu result set.
    private ScheduleChangeRequest mapScheduleChangeRequestBasic(ResultSet rs) throws SQLException {
        ScheduleChangeRequest request = new ScheduleChangeRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setDoctorId(rs.getInt("doctor_id"));
        request.setRequestType(rs.getString("request_type"));
        request.setScopeType(rs.getString("scope_type"));
        request.setReason(stripCounterpartShiftMeta(rs.getString("reason")));
        request.setStatus(rs.getString("status"));
        String requestedAtRaw = rs.getString("requested_at");
        if (requestedAtRaw != null && !requestedAtRaw.isBlank()) {
            request.setRequestedAt(Timestamp.valueOf(requestedAtRaw));
        }
        request.setAdminNote(rs.getString("admin_note"));
        request.setActionType(rs.getString("action_type"));

        int targetShiftId = rs.getInt("target_shift_id");
        request.setTargetShiftId(rs.wasNull() ? null : targetShiftId);

        request.setWorkDate(rs.getDate("work_date"));
        int dayOfWeek = rs.getInt("day_of_week");
        request.setDayOfWeek(rs.wasNull() ? null : dayOfWeek);

        Time startTime = rs.getTime("start_time");
        if (startTime != null) {
            request.setStartTime(startTime.toLocalTime());
        }

        Time endTime = rs.getTime("end_time");
        if (endTime != null) {
            request.setEndTime(endTime.toLocalTime());
        }

        int maxPatients = rs.getInt("max_patients");
        request.setMaxPatients(rs.wasNull() ? null : maxPatients);
        return request;
    }

    // Lay request pending va khoa ban ghi de xu ly an toan.
    private PendingScheduleReview getPendingScheduleReviewForUpdate(int requestId) throws SQLException {
        String sql = """
            SELECT r.request_id, r.doctor_id, r.request_type, r.scope_type,
                   r.reason, r.requested_at,
                   i.action_type, i.target_shift_id, i.work_date, i.day_of_week,
                   i.start_time, i.end_time, i.max_patients
            FROM schedule_change_requests r
            LEFT JOIN schedule_change_request_items i ON r.request_id = i.request_id
            WHERE r.request_id = ? AND r.status = 'PENDING'
            LIMIT 1
            FOR UPDATE
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, requestId);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                PendingScheduleReview review = new PendingScheduleReview();
                review.requestId = rs.getInt("request_id");
                review.doctorId = rs.getInt("doctor_id");
                review.requestType = rs.getString("request_type");
                review.scopeType = rs.getString("scope_type");
                review.reason = rs.getString("reason");
                review.requestedAt = rs.getTimestamp("requested_at");
                review.actionType = rs.getString("action_type");

                int targetShiftId = rs.getInt("target_shift_id");
                review.targetShiftId = rs.wasNull() ? null : targetShiftId;

                review.workDate = rs.getDate("work_date");
                int dayOfWeek = rs.getInt("day_of_week");
                review.dayOfWeek = rs.wasNull() ? null : dayOfWeek;

                Time startTime = rs.getTime("start_time");
                review.startTime = startTime == null ? null : startTime.toLocalTime();

                Time endTime = rs.getTime("end_time");
                review.endTime = endTime == null ? null : endTime.toLocalTime();

                int maxPatients = rs.getInt("max_patients");
                review.maxPatients = rs.wasNull() ? null : maxPatients;
                return review;
            }
        }
    }

    // Dieu phoi xu ly theo tung loai hanh dong sau khi duyet.
    private boolean applyApprovedScheduleRequest(PendingScheduleReview request) throws SQLException {
        if (request.actionType == null || request.actionType.isBlank()) {
            return true;
        }

        if ("TEMPORARY".equalsIgnoreCase(request.requestType)
                || "ONE_DATE".equalsIgnoreCase(request.scopeType)) {
            return true;
        }

        String actionType = request.actionType.trim().toUpperCase();
        return switch (actionType) {
            case "ADD" -> applyApprovedAddRequest(request);
            case "REMOVE" -> applyApprovedRemoveRequest(request);
            case "UPDATE" -> applyApprovedUpdateRequest(request);
            default -> true;
        };
    }

    // Xu ly request them ca sau khi da duoc duyet.
    private boolean applyApprovedAddRequest(PendingScheduleReview request) throws SQLException {
        if (request.dayOfWeek == null || request.startTime == null || request.endTime == null || request.maxPatients == null) {
            return false;
        }

        if (hasShiftConflict(request.doctorId, request.dayOfWeek, request.startTime, request.endTime, null)) {
            return false;
        }

        addDoctorShift(request.doctorId, request.dayOfWeek, request.startTime, request.endTime, request.maxPatients);
        return true;
    }

    // Xu ly request xoa ca sau khi da duoc duyet.
    private boolean applyApprovedRemoveRequest(PendingScheduleReview request) throws SQLException {
        if (request.targetShiftId == null) {
            return true;
        }

        DoctorShift shift = getDoctorShiftByIdForUpdate(request.targetShiftId);
        if (shift == null || shift.getDoctorId() != request.doctorId) {
            return false;
        }

        String deleteSql = "UPDATE doctor_shifts SET status = 'inactive' WHERE shift_id = ?";
        try (PreparedStatement st = connection.prepareStatement(deleteSql)) {
            st.setInt(1, request.targetShiftId);
            return st.executeUpdate() > 0;
        }
    }

    // Xu ly request doi ca sau khi da duoc duyet.
    private boolean applyApprovedUpdateRequest(PendingScheduleReview request) throws SQLException {
        if (request.targetShiftId == null || request.dayOfWeek == null || request.startTime == null || request.endTime == null) {
            return false;
        }

        DoctorShift requesterShift = getDoctorShiftByIdForUpdate(request.targetShiftId);
        if (requesterShift == null || requesterShift.getDoctorId() != request.doctorId) {
            return false;
        }

        Integer preferredCounterpartShiftId = extractCounterpartShiftId(request.reason);
        DoctorShift counterpart;
        if (preferredCounterpartShiftId != null) {
            counterpart = getDoctorShiftByIdForUpdate(preferredCounterpartShiftId);
            if (counterpart == null || counterpart.getDoctorId() == request.doctorId) {
                return false;
            }
            if (counterpart.getDayOfWeek() != request.dayOfWeek
                    || !counterpart.getStartTime().equals(request.startTime)
                    || !counterpart.getEndTime().equals(request.endTime)) {
                return false;
            }
        } else {
            counterpart = findCounterpartShiftForSwap(
                    request.doctorId, request.targetShiftId, request.dayOfWeek, request.startTime, request.endTime
            );
        }

        int requesterMaxPatients = request.maxPatients != null ? request.maxPatients : requesterShift.getMaxPatients();
        if (!updateShiftById(
                requesterShift.getShiftId(),
                request.dayOfWeek,
                request.startTime,
                request.endTime,
                requesterMaxPatients
        )) {
            return false;
        }

        if (counterpart != null) {
            return updateShiftById(
                    counterpart.getShiftId(),
                    requesterShift.getDayOfWeek(),
                    requesterShift.getStartTime(),
                    requesterShift.getEndTime(),
                    counterpart.getMaxPatients()
            );
        }
        return true;
    }

    // Lay ca active theo shift id va khoa dong de cap nhat.
    private DoctorShift getDoctorShiftByIdForUpdate(int shiftId) throws SQLException {
        String sql = """
            SELECT shift_id, doctor_id, day_of_week, start_time, end_time, max_patients
            FROM doctor_shifts
            WHERE shift_id = ?
              AND status = 'active'
            LIMIT 1
            FOR UPDATE
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapDoctorShift(rs);
            }
        }
    }

    // Tim ca doi ung cua bac si khac khi xu ly doi ca.
    private DoctorShift findCounterpartShiftForSwap(int requesterDoctorId, int requesterShiftId, int dayOfWeek,
            LocalTime startTime, LocalTime endTime) throws SQLException {
        String sql = """
            SELECT shift_id, doctor_id, day_of_week, start_time, end_time, max_patients
            FROM doctor_shifts
            WHERE day_of_week = ?
              AND start_time = ?
              AND end_time = ?
              AND status = 'active'
              AND doctor_id <> ?
              AND shift_id <> ?
            ORDER BY shift_id
            LIMIT 1
            FOR UPDATE
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, dayOfWeek);
            st.setTime(2, Time.valueOf(startTime));
            st.setTime(3, Time.valueOf(endTime));
            st.setInt(4, requesterDoctorId);
            st.setInt(5, requesterShiftId);

            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapDoctorShift(rs);
            }
        }
    }

    // Cap nhat truc tiep mot shift trong transaction hien tai.
    private boolean updateShiftById(int shiftId, int dayOfWeek, LocalTime startTime, LocalTime endTime, int maxPatients)
            throws SQLException {
        String sql = """
            UPDATE doctor_shifts
            SET day_of_week = ?, start_time = ?, end_time = ?, max_patients = ?
            WHERE shift_id = ?
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, dayOfWeek);
            st.setTime(2, Time.valueOf(startTime));
            st.setTime(3, Time.valueOf(endTime));
            st.setInt(4, maxPatients);
            st.setInt(5, shiftId);
            return st.executeUpdate() > 0;
        }
    }

    private static final class PendingScheduleReview {

        private int requestId;
        private int doctorId;
        private String requestType;
        private String scopeType;
        private String reason;
        private java.sql.Timestamp requestedAt;
        private String actionType;
        private Integer targetShiftId;
        private Date workDate;
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer maxPatients;
    }

    private String appendCounterpartShiftMeta(String reason, String actionType, Integer counterpartShiftId) {
        String cleanedReason = stripCounterpartShiftMeta(reason);
        if (!"UPDATE".equalsIgnoreCase(actionType) || counterpartShiftId == null) {
            return cleanedReason;
        }
        String meta = SWAP_SHIFT_META_PREFIX + counterpartShiftId + SWAP_SHIFT_META_SUFFIX;
        if (cleanedReason == null || cleanedReason.isBlank()) {
            return meta;
        }
        return cleanedReason + "\n" + meta;
    }

    private Integer extractCounterpartShiftId(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        int start = reason.lastIndexOf(SWAP_SHIFT_META_PREFIX);
        if (start < 0) {
            return null;
        }
        int valueStart = start + SWAP_SHIFT_META_PREFIX.length();
        int end = reason.indexOf(SWAP_SHIFT_META_SUFFIX, valueStart);
        if (end < 0) {
            return null;
        }
        String value = reason.substring(valueStart, end).trim();
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stripCounterpartShiftMeta(String reason) {
        if (reason == null || reason.isBlank()) {
            return reason;
        }
        int start = reason.lastIndexOf(SWAP_SHIFT_META_PREFIX);
        if (start < 0) {
            return reason;
        }
        int end = reason.indexOf(SWAP_SHIFT_META_SUFFIX, start + SWAP_SHIFT_META_PREFIX.length());
        if (end < 0) {
            return reason;
        }
        String cleaned = (reason.substring(0, start) + reason.substring(end + SWAP_SHIFT_META_SUFFIX.length())).trim();
        return cleaned;
    }

    private String getDoctorNameByShiftId(int shiftId) {
        String sql = """
            SELECT u.full_name
            FROM doctor_shifts s
            JOIN doctors d ON s.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE s.shift_id = ?
            LIMIT 1
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class EffectiveShiftState {

        private final int dayOfWeek;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public EffectiveShiftState(int dayOfWeek, LocalTime startTime, LocalTime endTime) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public int getDayOfWeek() {
            return dayOfWeek;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }
    }
}
