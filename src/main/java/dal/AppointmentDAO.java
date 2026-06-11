/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.Date;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Appointment;
import model.AppointmentDetail;
import model.Doctor;
import model.Patient;

/**
 *
 * @author Admin
 */
public class AppointmentDAO extends DBContext {

    private static final String SWAP_SHIFT_META_PREFIX = "[[SWAP_SHIFT_ID:";
    private static final String SWAP_SHIFT_META_SUFFIX = "]]";

    private static final class TemporaryEffectRow {

        private long requestId;
        private java.sql.Timestamp requestedAt;
        private int requesterId;
        private Integer counterpartId;
        private Integer targetShiftId;
        private Integer counterpartShiftId;
        private String actionType;
        private LocalDate workDate;
        private LocalDate oldDate;
        private String newPeriod;
        private String oldPeriod;
    }

    public boolean addAppointment(Appointment a) {
        return addAppointmentAndReturnId(a) > 0;
    }

    public long addAppointmentAndReturnId(Appointment a) {
        String sql = "INSERT INTO appointments "
                + "(patient_id, doctor_id, shift_id, booking_type, "
                + "appointment_date, appointment_time, status, symptom) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setLong(1, a.getPatientId());
            st.setInt(2, a.getDoctorId());
            st.setInt(3, a.getShiftId());
            st.setString(4, a.getBookingType());
            st.setDate(5, (Date) a.getAppointmentDate());
            st.setTime(6, a.getAppointmentTime());
            st.setString(7, a.getStatus());
            st.setString(8, a.getSymptom());

            int affected = st.executeUpdate();
            if (affected <= 0) {
                return -1;
            }

            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return -1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public long addPatient(Patient p) {
        String sql = "INSERT INTO patients (user_id, full_name, phone, dob, email, gender) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setInt(1, p.getUserId());
            st.setString(2, p.getFullName());
            st.setString(3, p.getPhone());
            st.setDate(4, new java.sql.Date(p.getDob().getTime()));
            st.setString(5, p.getEmail());
            st.setString(6, p.getGender());

            st.executeUpdate();

            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long getPatientID(Patient patient) {
        String sql = "SELECT patient_id FROM patients "
                + "WHERE full_name = ? AND phone = ? AND email = ?";

        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, patient.getFullName());
            st.setString(2, patient.getPhone());
            st.setString(3, patient.getEmail());

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return rs.getLong("patient_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<AppointmentDetail> getAppointmentsByPatientUserId(int userId) {

        List<AppointmentDetail> list = new ArrayList<>();

        String sql = "SELECT "
                + "a.appointment_id, "
                + "a.patient_id, "
                + "a.doctor_id, "
                + "a.shift_id, "
                + "a.booking_type, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status, "
                + "a.symptom, "
                + "d.specialization, "
                + "sp.academic_degree AS qualification, "
                + "d.price_booking, "
                + "du.user_id AS doctor_user_id, "
                + "du.full_name AS doctor_name, "
                + "du.image_url, "
                + "p.full_name, "
                + "p.phone, "
                + "p.email "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id "
                + "JOIN users du ON d.user_id = du.user_id "
                + "WHERE p.user_id = ? "
                + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {

                AppointmentDetail ad = new AppointmentDetail();

                // appointment
                ad.setAppointmentId(rs.getLong("appointment_id"));
                ad.setPatientId(rs.getLong("patient_id"));
                ad.setDoctorId(rs.getInt("doctor_id"));
                ad.setShiftId(rs.getInt("shift_id"));
                ad.setBookingType(rs.getString("booking_type"));
                ad.setAppointmentDate(rs.getDate("appointment_date"));
                ad.setAppointmentTime(rs.getTime("appointment_time"));
                ad.setStatus(rs.getString("status"));
                ad.setSymptom(rs.getString("symptom"));

                // doctor
                ad.setSpecialization(rs.getString("specialization"));
                ad.setQualification(rs.getString("qualification"));
                ad.setPrice(rs.getDouble("price_booking"));
                ad.setUserId(rs.getInt("doctor_user_id"));
                ad.setImage(rs.getString("image_url"));

                // patient
                ad.setFullName(rs.getString("full_name"));
                ad.setPhone(rs.getString("phone"));
                ad.setEmail(rs.getString("email"));
                ad.setDoctorName(rs.getString("doctor_name"));
                list.add(ad);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // lấy toàn bộ appointment
    public List<AppointmentDetail> getAllAppointments() {

        List<AppointmentDetail> list = new ArrayList<>();

        String sql = "SELECT "
                + "a.appointment_id, "
                + "a.patient_id, "
                + "a.doctor_id, "
                + "a.shift_id, "
                + "a.booking_type, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status, "
                + "a.symptom, "
                + "d.specialization, "
                + "sp.academic_degree AS qualification, "
                + "d.price_booking, "
                + "du.user_id AS doctor_user_id, "
                + "du.full_name AS doctor_name, "
                + "du.image_url, "
                + "p.full_name, "
                + "p.phone, "
                + "p.email "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "LEFT JOIN staff_profiles sp ON sp.user_id = d.user_id "
                + "JOIN users du ON d.user_id = du.user_id "
                + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            ResultSet rs = st.executeQuery();

            while (rs.next()) {

                AppointmentDetail ad = new AppointmentDetail();

                ad.setAppointmentId(rs.getLong("appointment_id"));
                ad.setPatientId(rs.getLong("patient_id"));
                ad.setDoctorId(rs.getInt("doctor_id"));
                ad.setShiftId(rs.getInt("shift_id"));
                ad.setBookingType(rs.getString("booking_type"));
                ad.setAppointmentDate(rs.getDate("appointment_date"));
                ad.setAppointmentTime(rs.getTime("appointment_time"));
                ad.setStatus(rs.getString("status"));
                ad.setSymptom(rs.getString("symptom"));

                // doctor
                ad.setSpecialization(rs.getString("specialization"));
                ad.setQualification(rs.getString("qualification"));
                ad.setPrice(rs.getDouble("price_booking"));
                ad.setUserId(rs.getInt("doctor_user_id"));
                ad.setImage(rs.getString("image_url"));
                ad.setDoctorName(rs.getString("doctor_name"));

                // patient
                ad.setFullName(rs.getString("full_name"));
                ad.setPhone(rs.getString("phone"));
                ad.setEmail(rs.getString("email"));

                list.add(ad);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateStatus(long appointmentId, String status) {

        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setString(1, status);
            st.setLong(2, appointmentId);

            st.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelPastBookedAppointments() {

        String sql = """
        UPDATE appointments
        SET status = 'cancelled'
        WHERE appointment_date < CURDATE()
        AND status = 'booked'
    """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getPatientIdByEmail(String email) {

        String sql = "SELECT patient_id FROM patients WHERE email = ?";

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setString(1, email);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return rs.getLong("patient_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<LocalDate> getAvailableDates(int doctorId) {
        List<LocalDate> list = new ArrayList<>();

        String sql = """
        SELECT ds.day_of_week, ds.max_patients
        FROM doctor_shifts ds
        JOIN doctors d ON d.doctor_id = ds.doctor_id
        JOIN users u ON u.user_id = d.user_id
        WHERE ds.doctor_id = ?
          AND ds.status = 'active'
          AND u.status = 'active'
    """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setInt(1, doctorId);
            ResultSet rs = st.executeQuery();

            Map<Integer, Integer> capacityByDay = new HashMap<>();
            while (rs.next()) {
                int day = rs.getInt("day_of_week");
                int maxPatients = rs.getInt("max_patients");
                capacityByDay.merge(day, maxPatients, Integer::sum);
            }

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            LocalTime cutoff = LocalTime.of(17, 0); 

            List<LocalDate> windowDates = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                windowDates.add(today.plusDays(i));
            }

            Map<LocalDate, Map<String, Integer>> countsByDate
                    = buildEffectivePeriodCountsByDate(doctorId, windowDates);

            for (LocalDate date : windowDates) {

                boolean validDate = now.isAfter(cutoff)
                        ? date.isAfter(today) 
                        : !date.isBefore(today);       

                if (!validDate) {
                    continue;
                }

                Map<String, Integer> periods
                        = countsByDate.getOrDefault(date, Map.of());

                int shiftCount = periods.getOrDefault("MORNING", 0)
                        + periods.getOrDefault("AFTERNOON", 0);

                if (shiftCount <= 0) {
                    continue;
                }

                int dayOfWeek = date.getDayOfWeek().getValue() % 7;

                int booked = countPatients(doctorId, Date.valueOf(date));
                int maxPatients = Math.max(1,
                        capacityByDay.getOrDefault(dayOfWeek, 20));

                if (booked < maxPatients) {
                    list.add(date);

                    if (list.size() >= 7) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Map<String, String> getAvailablePeriodCsvByDates(int doctorId, List<LocalDate> dates) {
        Map<String, String> result = new HashMap<>();
        if (dates == null || dates.isEmpty()) {
            return result;
        }
        Map<LocalDate, Map<String, Integer>> countsByDate = buildEffectivePeriodCountsByDate(doctorId, dates);
        for (LocalDate date : dates) {
            Map<String, Integer> periodCounts = countsByDate.getOrDefault(date, Map.of());
            List<String> periods = new ArrayList<>();
            if (periodCounts.getOrDefault("MORNING", 0) > 0) {
                periods.add("MORNING");
            }
            if (periodCounts.getOrDefault("AFTERNOON", 0) > 0) {
                periods.add("AFTERNOON");
            }
            result.put(date.toString(), String.join(",", periods));
        }
        return result;
    }

    private Map<LocalDate, Map<String, Integer>> buildEffectivePeriodCountsByDate(int doctorId, List<LocalDate> dates) {
        Map<LocalDate, Map<String, Integer>> countsByDate = new HashMap<>();
        if (dates == null || dates.isEmpty()) {
            return countsByDate;
        }

        Map<Integer, Map<String, Integer>> baseCountsByDay = new HashMap<>();
        String baseSql = """
            SELECT day_of_week, start_time
            FROM doctor_shifts
            WHERE doctor_id = ?
              AND status = 'active'
        """;
        try (PreparedStatement st = connection.prepareStatement(baseSql)) {
            st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int day = rs.getInt("day_of_week");
                    String period = toShiftPeriod(rs.getTime("start_time"));
                    if (period == null) {
                        continue;
                    }
                    baseCountsByDay.computeIfAbsent(day, k -> new HashMap<>())
                            .merge(period, 1, Integer::sum);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return countsByDate;
        }

        LocalDate minDate = dates.get(0);
        LocalDate maxDate = dates.get(0);
        for (LocalDate date : dates) {
            if (date.isBefore(minDate)) {
                minDate = date;
            }
            if (date.isAfter(maxDate)) {
                maxDate = date;
            }
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            Map<String, Integer> dayCounts = baseCountsByDay.getOrDefault(dayOfWeek, Map.of());
            Map<String, Integer> copy = new HashMap<>();
            copy.put("MORNING", dayCounts.getOrDefault("MORNING", 0));
            copy.put("AFTERNOON", dayCounts.getOrDefault("AFTERNOON", 0));
            countsByDate.put(date, copy);
        }

        applyApprovedTemporaryEffects(doctorId, countsByDate, Date.valueOf(minDate), Date.valueOf(maxDate));
        return countsByDate;
    }

    private void applyApprovedTemporaryEffects(
            int doctorId,
            Map<LocalDate, Map<String, Integer>> countsByDate,
            Date fromDate,
            Date toDate
    ) {
        if (countsByDate.isEmpty()) {
            return;
        }
        Map<Integer, Integer> doctorIdByShiftId = new HashMap<>();
        String sql = """
            SELECT r.request_id,
                   r.requested_at,
                   r.doctor_id AS requester_doctor_id,
                   r.reason AS request_reason,
                   i.action_type,
                   i.work_date,
                   i.day_of_week,
                   i.start_time,
                   i.end_time,
                   i.target_shift_id,
                   s_old.day_of_week AS old_day_of_week,
                   s_old.start_time AS old_start_time,
                   s_old.end_time AS old_end_time,
                   s_new.doctor_id AS counterpart_doctor_id
            FROM schedule_change_requests r
            JOIN schedule_change_request_items i ON r.request_id = i.request_id
            LEFT JOIN doctor_shifts s_old ON i.target_shift_id = s_old.shift_id
            LEFT JOIN doctor_shifts s_new ON s_new.shift_id = (
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
              AND (
                  i.work_date BETWEEN ? AND ?
                  OR (
                      s_old.day_of_week IS NOT NULL
                      AND i.day_of_week IS NOT NULL
                      AND DATE_ADD(
                          i.work_date,
                          INTERVAL (
                              (CASE WHEN s_old.day_of_week = 0 THEN 7 ELSE s_old.day_of_week END)
                              - (CASE WHEN i.day_of_week = 0 THEN 7 ELSE i.day_of_week END)
                          ) DAY
                      ) BETWEEN ? AND ?
                  )
              )
            ORDER BY r.requested_at ASC
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setDate(1, fromDate);
            st.setDate(2, toDate);
            st.setDate(3, fromDate);
            st.setDate(4, toDate);
            try (ResultSet rs = st.executeQuery()) {
                List<TemporaryEffectRow> rows = new ArrayList<>();
                Map<String, TemporaryEffectRow> latestRequesterRowByShiftDate = new HashMap<>();
                Map<String, TemporaryEffectRow> latestCounterpartRowByShiftDate = new HashMap<>();

                while (rs.next()) {
                    String requestReason = rs.getString("request_reason");
                    String actionType = rs.getString("action_type");
                    Date workDateSql = rs.getDate("work_date");
                    if (actionType == null || workDateSql == null) {
                        continue;
                    }

                    TemporaryEffectRow row = new TemporaryEffectRow();
                    row.requestId = rs.getLong("request_id");
                    row.requestedAt = rs.getTimestamp("requested_at");
                    row.requesterId = rs.getInt("requester_doctor_id");
                    row.targetShiftId = (Integer) rs.getObject("target_shift_id");
                    row.counterpartShiftId = extractCounterpartShiftId(requestReason);
                    row.counterpartId = resolveCounterpartDoctorId(rs, requestReason, doctorIdByShiftId);
                    row.actionType = actionType.trim().toUpperCase();
                    row.workDate = workDateSql.toLocalDate();
                    row.newPeriod = toShiftPeriod(rs.getTime("start_time"));
                    row.oldPeriod = toShiftPeriod(rs.getTime("old_start_time"));
                    row.oldDate = resolveOldDate(row.workDate, rs.getInt("day_of_week"), rs, "old_day_of_week");
                    if (row.oldDate == null) {
                        row.oldDate = row.workDate;
                    }

                    rows.add(row);

                    if ("UPDATE".equals(row.actionType)) {
                        if (row.targetShiftId != null) {
                            String requesterKey = row.targetShiftId + "|" + row.workDate;
                            TemporaryEffectRow existingRequester = latestRequesterRowByShiftDate.get(requesterKey);
                            if (isLaterTemporaryRow(row, existingRequester)) {
                                latestRequesterRowByShiftDate.put(requesterKey, row);
                            }
                        }
                        if (row.counterpartShiftId != null) {
                            String counterpartKey = row.counterpartShiftId + "|" + row.workDate;
                            TemporaryEffectRow existingCounterpart = latestCounterpartRowByShiftDate.get(counterpartKey);
                            if (isLaterTemporaryRow(row, existingCounterpart)) {
                                latestCounterpartRowByShiftDate.put(counterpartKey, row);
                            }
                        }
                    }
                }

                for (TemporaryEffectRow row : rows) {
                    switch (row.actionType) {
                        case "ADD":
                            if (row.requesterId == doctorId) {
                                applyPeriodDelta(countsByDate, row.workDate, row.newPeriod, +1);
                            }
                            break;
                        case "REMOVE":
                            if (row.requesterId == doctorId) {
                                String removePeriod = row.newPeriod != null ? row.newPeriod : row.oldPeriod;
                                applyPeriodDelta(countsByDate, row.workDate, removePeriod, -1);
                            }
                            break;
                        case "UPDATE":
                            if (row.requesterId == doctorId && row.targetShiftId != null) {
                                String requesterKey = row.targetShiftId + "|" + row.workDate;
                                if (row == latestRequesterRowByShiftDate.get(requesterKey)) {
                                    applyPeriodDelta(countsByDate, row.oldDate, row.oldPeriod, -1);
                                    applyPeriodDelta(countsByDate, row.workDate, row.newPeriod, +1);
                                }
                            }
                            if (row.counterpartId != null && row.counterpartId == doctorId && row.counterpartShiftId != null) {
                                String counterpartKey = row.counterpartShiftId + "|" + row.workDate;
                                if (row == latestCounterpartRowByShiftDate.get(counterpartKey)) {
                                    applyPeriodDelta(countsByDate, row.workDate, row.newPeriod, -1);
                                    applyPeriodDelta(countsByDate, row.oldDate, row.oldPeriod, +1);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer resolveCounterpartDoctorId(ResultSet rs, String requestReason, Map<Integer, Integer> doctorIdByShiftId)
            throws SQLException {
        Integer counterpartId = null;
        Integer counterpartShiftId = extractCounterpartShiftId(requestReason);
        if (counterpartShiftId != null) {
            counterpartId = doctorIdByShiftId.get(counterpartShiftId);
            if (counterpartId == null) {
                counterpartId = getDoctorIdByShiftId(counterpartShiftId);
                if (counterpartId != null) {
                    doctorIdByShiftId.put(counterpartShiftId, counterpartId);
                }
            }
        }
        // Do not fallback to inferred counterpart by day/time because it can be ambiguous
        // and create ghost shifts on wrong dates.
        return counterpartId;
    }

    private boolean isLaterTemporaryRow(TemporaryEffectRow candidate, TemporaryEffectRow existing) {
        if (candidate == null) {
            return false;
        }
        if (existing == null) {
            return true;
        }
        if (candidate.requestedAt != null && existing.requestedAt != null) {
            int compare = candidate.requestedAt.compareTo(existing.requestedAt);
            if (compare != 0) {
                return compare > 0;
            }
        } else if (candidate.requestedAt != null) {
            return true;
        } else if (existing.requestedAt != null) {
            return false;
        }
        return candidate.requestId > existing.requestId;
    }

    private void applyPeriodDelta(Map<LocalDate, Map<String, Integer>> countsByDate, LocalDate date, String period, int delta) {
        if (date == null || period == null || delta == 0) {
            return;
        }
        Map<String, Integer> periods = countsByDate.get(date);
        if (periods == null) {
            return;
        }
        int next = periods.getOrDefault(period, 0) + delta;
        periods.put(period, Math.max(0, next));
    }

    private LocalDate resolveOldDate(LocalDate workDate, int newDayOfWeek, ResultSet rs, String oldDayColumn) throws SQLException {
        int oldDay = rs.getInt(oldDayColumn);
        if (rs.wasNull() || workDate == null || newDayOfWeek < 0 || newDayOfWeek > 6) {
            return workDate;
        }
        int normalizedOldDay = oldDay == 0 ? 7 : oldDay;
        int normalizedNewDay = newDayOfWeek == 0 ? 7 : newDayOfWeek;
        int offset = normalizedOldDay - normalizedNewDay;
        return workDate.plusDays(offset);
    }

    private String toShiftPeriod(Time startTime) {
        if (startTime == null) {
            return null;
        }
        int hour = startTime.toLocalTime().getHour();
        return hour < 12 ? "MORNING" : "AFTERNOON";
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

    private Integer getDoctorIdByShiftId(int shiftId) {
        String sql = "SELECT doctor_id FROM doctor_shifts WHERE shift_id = ? LIMIT 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shiftId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("doctor_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countPatients(int doctorId, Date date) {

        String sql = """
            SELECT COUNT(*)
            FROM appointments
            WHERE doctor_id = ?
              AND appointment_date = ?
              AND status <> 'cancelled'
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setInt(1, doctorId);
            st.setDate(2, date);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getDoctorIdByAppointment(long appointmentId) {

        String sql = "SELECT doctor_id FROM appointments WHERE appointment_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, appointmentId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void addQueueWithPriority(long appointmentId, int doctorId) {
        String getAppointmentSql = """
            SELECT booking_type, appointment_date
            FROM appointments
            WHERE appointment_id = ?
            FOR UPDATE
        """;

        String findInsertPosOnlineSql = """
            SELECT COALESCE(MAX(q.queue_position), 0) + 1 AS insert_pos
            FROM exam_queue q
            JOIN appointments a ON a.appointment_id = q.appointment_id
            WHERE q.doctor_id = ?
              AND a.appointment_date = ?
              AND q.status IN ('waiting', 'waiting_return', 'in_lab', 'examining')
              AND a.booking_type = 'online'
        """;

        String findInsertPosWalkInSql = """
            SELECT COALESCE(MAX(q.queue_position), 0) + 1 AS insert_pos
            FROM exam_queue q
            JOIN appointments a ON a.appointment_id = q.appointment_id
            WHERE q.doctor_id = ?
              AND a.appointment_date = ?
              AND q.status IN ('waiting', 'waiting_return', 'in_lab', 'examining')
        """;

        String shiftQueueSql = """
            UPDATE exam_queue q
            JOIN appointments a ON a.appointment_id = q.appointment_id
            SET q.queue_position = q.queue_position + 1
            WHERE q.doctor_id = ?
              AND a.appointment_date = ?
              AND q.status IN ('waiting', 'waiting_return', 'in_lab', 'examining')
              AND q.queue_position >= ?
        """;

        String insertQueueSql = """
            INSERT INTO exam_queue (appointment_id, doctor_id, queue_position, status)
            VALUES (?, ?, ?, 'waiting')
        """;

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            String bookingType;
            Date appointmentDate;
            try (PreparedStatement getAppt = connection.prepareStatement(getAppointmentSql)) {
                getAppt.setLong(1, appointmentId);
                try (ResultSet rs = getAppt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return;
                    }
                    bookingType = rs.getString("booking_type");
                    appointmentDate = rs.getDate("appointment_date");
                }
            }

            int insertPos = 1;
            String findInsertPosSql = "online".equalsIgnoreCase(bookingType)
                    ? findInsertPosOnlineSql
                    : findInsertPosWalkInSql;

            try (PreparedStatement findPos = connection.prepareStatement(findInsertPosSql)) {
                findPos.setInt(1, doctorId);
                findPos.setDate(2, appointmentDate);
                try (ResultSet posRs = findPos.executeQuery()) {
                    if (posRs.next()) {
                        insertPos = posRs.getInt("insert_pos");
                    }
                }
            }

            try (PreparedStatement shift = connection.prepareStatement(shiftQueueSql)) {
                shift.setInt(1, doctorId);
                shift.setDate(2, appointmentDate);
                shift.setInt(3, insertPos);
                shift.executeUpdate();
            }

            try (PreparedStatement insert = connection.prepareStatement(insertQueueSql)) {
                insert.setLong(1, appointmentId);
                insert.setInt(2, doctorId);
                insert.setInt(3, insertPos);
                insert.executeUpdate();
            }

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
