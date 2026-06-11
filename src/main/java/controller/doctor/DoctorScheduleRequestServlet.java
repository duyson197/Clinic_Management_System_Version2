package controller.doctor;

import dal.DoctorDAO;
import dal.DoctorScheduleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Doctor;
import model.DoctorShift;
import model.ScheduleChangeRequest;
import model.ScheduleSwapShiftOption;
import model.User;
import util.SystemLogService;

public class DoctorScheduleRequestServlet extends HttpServlet {

    private static final Map<String, LocalTime[]> SHIFT_TIME_BY_PERIOD = Map.of(
            "MORNING", new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(12, 0)},
            "AFTERNOON", new LocalTime[]{LocalTime.of(13, 0), LocalTime.of(17, 0)}
    );
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User account = (User) session.getAttribute("account");
        if (account == null || account.getRole() == null || !"doctor".equalsIgnoreCase(account.getRole().name())) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
        Doctor doctor = doctorDAO.getDoctorByUserId(account.getUserId());
        if (doctor == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        String mode = safeUpper(request.getParameter("mode"));
        if ("SWAP_OPTIONS".equals(mode)) {
            writeSwapOptionsResponse(request, response, doctor.getDoctorId());
            return;
        }

        int doctorId = doctor.getDoctorId();
        List<DoctorShift> weeklyShifts = scheduleDAO.getDoctorShifts(doctorId);
        DoctorScheduleDAO requestDAO = new DoctorScheduleDAO();
        List<ScheduleChangeRequest> recentRequests = requestDAO.getScheduleChangeRequestsByDoctor(doctorId, 20);

        request.setAttribute("weeklyShifts", weeklyShifts);
        request.setAttribute("recentRequests", recentRequests);

        request.getRequestDispatcher("/pages/examination/doctorScheduleRequest.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User account = (User) session.getAttribute("account");
        if (account == null || account.getRole() == null || !"doctor".equalsIgnoreCase(account.getRole().name())) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        DoctorDAO doctorDAO = new DoctorDAO();
        DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
        Doctor doctor = doctorDAO.getDoctorByUserId(account.getUserId());
        if (doctor == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        String requestType = safeUpper(request.getParameter("requestType"));
        String actionType = safeUpper(request.getParameter("actionType"));
        String reason = trimOrEmpty(request.getParameter("reason"));
        String scopeType = deriveScopeType(requestType, actionType);

        String error = validateInput(requestType, actionType, reason);

        Integer targetShiftId = parseInteger(request.getParameter("targetShiftId"));
        Integer dayOfWeek = parseInteger(request.getParameter("dayOfWeek"));
        Integer maxPatients = parseInteger(request.getParameter("maxPatients"));
        Integer swapShiftId = parseInteger(request.getParameter("swapShiftId"));
        Date workDate = parseDate(request.getParameter("workDate"));
        String shiftPeriod = safeUpper(request.getParameter("shiftPeriod"));
        Integer removeShiftId = null;

        LocalTime startTime = null;
        LocalTime endTime = null;
        if ("ADD".equals(actionType) || ("REMOVE".equals(actionType) && "ONE_DATE".equals(scopeType))) {
            LocalTime[] shiftTime = SHIFT_TIME_BY_PERIOD.get(shiftPeriod);
            if (shiftTime != null) {
                startTime = shiftTime[0];
                endTime = shiftTime[1];
            }
        }

        if (error == null && "ONE_DATE".equals(scopeType) && workDate == null) {
            error = "Vui lòng chọn ngày áp dụng cho yêu cầu tạm thời.";
        }
        if (error == null && "ONE_DATE".equals(scopeType) && workDate != null
                && workDate.toLocalDate().isBefore(LocalDate.now())) {
            error = "Chỉ áp dụng từ ngày hôm nay trở đi.";
        }
        if (error == null && "ONE_DATE".equals(scopeType) && "UPDATE".equals(actionType) && workDate != null
                && workDate.toLocalDate().isAfter(LocalDate.now().plusWeeks(2))) {
            error = "Chỉ đổi được ca trong tối đa 2 tuần tới.";
        }

        if (error == null && "ADD".equals(actionType)) {
            if (!SHIFT_TIME_BY_PERIOD.containsKey(shiftPeriod)) {
                error = "Vui lòng chọn ca làm việc (sáng/chiều).";
            }
            if (maxPatients == null || maxPatients <= 0) {
                error = "Số bệnh nhân tối đa phải lớn hơn 0.";
            }
        }

        if (error == null && "REMOVE".equals(actionType) && "ONE_DATE".equals(scopeType)) {
            if (!SHIFT_TIME_BY_PERIOD.containsKey(shiftPeriod)) {
                error = "Vui lòng chọn ca cần hủy (sáng/chiều).";
            } else if (workDate != null) {
                dayOfWeek = normalizeDayOfWeek(workDate.toLocalDate().getDayOfWeek());
                removeShiftId = scheduleDAO.findActiveShiftIdByDoctorDayAndTime(
                        doctor.getDoctorId(),
                        dayOfWeek,
                        startTime,
                        endTime
                );
                if (removeShiftId == null) {
                    error = "Bạn không có ca làm việc vào ngày áp dụng đã chọn.";
                } else if (scheduleDAO.hasAppointmentsForShiftOnDate(removeShiftId, workDate)) {
                    error = "Không thể hủy ca đã có lịch hẹn.";
                }
            }
        }

        if (error == null && "UPDATE".equals(actionType)) {
            if ("ONE_DATE".equals(scopeType) && workDate == null) {
                error = "Vui lòng chọn ngày để tìm ca bác sĩ muốn đổi.";
            } else if ("WEEKLY_TEMPLATE".equals(scopeType) && dayOfWeek == null) {
                error = "Vui lòng chọn thứ áp dụng để tìm ca bác sĩ muốn đổi.";
            } else if (swapShiftId == null) {
                error = "Vui lòng chọn ca của bác sĩ khác để đổi.";
            }
        }

        if (error == null && ("UPDATE".equals(actionType)
                || ("REMOVE".equals(actionType) && "WEEKLY_TEMPLATE".equals(scopeType)))
                && targetShiftId == null) {
            error = "Vui lòng chọn ca gốc cần cập nhật hoặc hủy.";
        }

        DoctorShift currentShift = null;
        if (error == null && targetShiftId != null) {
            currentShift = scheduleDAO.getDoctorShiftById(targetShiftId);
            if (currentShift == null || currentShift.getDoctorId() != doctor.getDoctorId()) {
                error = "Ca gốc không thuộc lịch làm việc của bạn.";
            }
        }

        if (error == null && "REMOVE".equals(actionType)
                && "WEEKLY_TEMPLATE".equals(scopeType) && targetShiftId != null) {
            if (currentShift != null) {
                dayOfWeek = currentShift.getDayOfWeek();
                if (scheduleDAO.hasAnyAppointmentsForShift(targetShiftId)) {
                    error = "Không thể hủy ca đã có lịch hẹn.";
                }
            }
        }

        if (error == null && "ONE_DATE".equals(scopeType) && workDate != null) {
            if ("UPDATE".equals(actionType) && targetShiftId != null
                    && scheduleDAO.hasApprovedTemporaryParticipationForShiftOnDate(targetShiftId, workDate)) {
                error = "Ca gốc đã có đơn đổi lịch tạm thời được duyệt, không thể gửi thêm yêu cầu cho ca này.";
            } else if ("REMOVE".equals(actionType) && removeShiftId != null
                    && scheduleDAO.hasApprovedTemporaryParticipationForShiftOnDate(removeShiftId, workDate)) {
                error = "Ca này đã có đơn đổi lịch tạm thời được duyệt, không thể gửi thêm yêu cầu cho ca này.";
            }
        }

        if (error == null && "ONE_DATE".equals(scopeType) && workDate != null
                && currentShift != null && ("UPDATE".equals(actionType) || "REMOVE".equals(actionType))) {
            int workDateDay = normalizeDayOfWeek(workDate.toLocalDate().getDayOfWeek());
            DoctorScheduleDAO.EffectiveShiftState currentEffective = scheduleDAO.getEffectiveShiftStateForDate(
                    currentShift.getShiftId(),
                    workDate
            );
            int currentEffectiveDay = currentEffective != null ? currentEffective.getDayOfWeek() : currentShift.getDayOfWeek();
            LocalTime currentEffectiveStart = currentEffective != null ? currentEffective.getStartTime() : currentShift.getStartTime();
            LocalDate sourceShiftDate = resolveShiftDateForOneDateRequest(
                    workDate.toLocalDate(),
                    workDateDay,
                    currentEffectiveDay
            );
            if (error == null && "UPDATE".equals(actionType)
                    && sourceShiftDate.isAfter(LocalDate.now().plusWeeks(2))) {
                error = "Chỉ được đổi ca trong tối đa 2 tuần tới.";
            }
            if (error == null && "UPDATE".equals(actionType)
                    && scheduleDAO.hasApprovedTemporaryParticipationForShiftOnDate(
                            currentShift.getShiftId(),
                            Date.valueOf(sourceShiftDate))) {
                error = "Ca gốc đã có đơn đổi lịch tạm thời được duyệt, không thể gửi thêm yêu cầu cho ca này.";
            }
            if (hasShiftStarted(sourceShiftDate, currentEffectiveStart)) {
                error = "Chỉ được đổi/hủy ca trước giờ bắt đầu ca gốc.";
            }
        }

        if (error == null && "ONE_DATE".equals(scopeType) && workDate != null
                && "REMOVE".equals(actionType) && startTime != null
                && hasShiftStarted(workDate.toLocalDate(), startTime)) {
            error = "Chỉ được đổi/hủy ca trước giờ bắt đầu ca gốc.";
        }

        if (error == null && "UPDATE".equals(actionType)) {
            DoctorShift swapShift = scheduleDAO.getDoctorShiftById(swapShiftId);
            if (swapShift == null) {
                error = "Không tìm thấy ca bác sĩ muốn đổi.";
            } else {
                int workDateDay = workDate == null ? -1 : normalizeDayOfWeek(workDate.toLocalDate().getDayOfWeek());
                if (swapShift.getDoctorId() == doctor.getDoctorId()) {
                    error = "Bạn chỉ có thể chọn ca của bác sĩ khác.";
                } else if ("ONE_DATE".equals(scopeType)) {
                    DoctorScheduleDAO.EffectiveShiftState swapEffective = scheduleDAO.getEffectiveShiftStateForDate(
                            swapShift.getShiftId(),
                            workDate
                    );
                    int swapEffectiveDay = swapEffective != null ? swapEffective.getDayOfWeek() : swapShift.getDayOfWeek();
                    LocalTime swapEffectiveStart = swapEffective != null ? swapEffective.getStartTime() : swapShift.getStartTime();
                    if (swapEffectiveDay != workDateDay) {
                        error = "Ca được chọn không nằm trong ngày áp dụng.";
                    } else if (hasShiftStarted(workDate.toLocalDate(), swapEffectiveStart)) {
                        error = "Chỉ được đổi ca trước giờ bắt đầu ca.";
                    } else if (scheduleDAO.hasApprovedTemporaryParticipationForShiftOnDate(swapShift.getShiftId(), workDate)) {
                        error = "Ca muốn đổi đã có đơn đổi lịch tạm thời được duyệt, không thể gửi thêm yêu cầu cho ca này.";
                    } else {
                        DoctorScheduleDAO.EffectiveShiftState currentEffectiveForSwap = (currentShift == null || workDate == null)
                                ? null
                                : scheduleDAO.getEffectiveShiftStateForDate(currentShift.getShiftId(), workDate);
                        LocalDate sourceShiftDate = resolveShiftDateForOneDateRequest(
                                workDate.toLocalDate(),
                                workDateDay,
                                currentEffectiveForSwap != null ? currentEffectiveForSwap.getDayOfWeek()
                                        : (currentShift != null ? currentShift.getDayOfWeek() : workDateDay)
                        );
                        boolean sourceHasAppointment = targetShiftId != null
                                && scheduleDAO.hasAppointmentsForShiftOnDate(targetShiftId, Date.valueOf(sourceShiftDate));
                        boolean swapHasAppointment = scheduleDAO.hasAppointmentsForShiftOnDate(swapShiftId, workDate);
                        if (sourceHasAppointment || swapHasAppointment) {
                            error = "Không thể đổi ca tạm thời nếu 1 trong 2 ca đã có lịch hẹn";
                        }
                    }
                } else if ("WEEKLY_TEMPLATE".equals(scopeType)) {
                    if (swapShift.getDayOfWeek() != dayOfWeek) {
                        error = "Ca được chọn không nằm trong thứ áp dụng.";
                    }
                } else {
                    error = "Phạm vi đổi ca không hợp lệ.";
                }

                if (error == null) {
                    startTime = swapShift.getStartTime();
                    endTime = swapShift.getEndTime();
                    if ("ONE_DATE".equals(scopeType)) {
                        dayOfWeek = workDateDay;
                    }
                    if (targetShiftId != null) {
                        if (currentShift != null) {
                            maxPatients = currentShift.getMaxPatients();
                        }
                    }
                }
            }
        }

        if (error != null) {
            request.getSession().setAttribute("scheduleRequestError", error);
            response.sendRedirect(request.getContextPath() + "/doctor/schedule-request");
            return;
        }

        DoctorScheduleDAO requestDAO = new DoctorScheduleDAO();
        boolean created = requestDAO.createScheduleChangeRequest(
                doctor.getDoctorId(),
                requestType,
                scopeType,
                reason,
                actionType,
                targetShiftId,
                swapShiftId,
                workDate,
                dayOfWeek,
                startTime,
                endTime,
                maxPatients
        );

        if (created) {
            SystemLogService.logWithSession(session, "DOCTOR_CREATE_SCHEDULE_CHANGE_REQUEST",
                    "Bác sĩ " + doctor.getFullName() + " gửi yêu cầu đổi lịch loại "
                    + requestType + " - " + actionType + ".");
            request.getSession().setAttribute("scheduleRequestSuccess",
                    "Đã gửi yêu cầu đổi lịch thành công. Vui lòng chờ quản trị viên duyệt.");
        } else {
            request.getSession().setAttribute("scheduleRequestError",
                    "Không thể tạo yêu cầu lúc này. Vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/doctor/schedule-request");
    }

    private String validateInput(String requestType, String actionType, String reason) {
        if (!("TEMPORARY".equals(requestType) || "PERMANENT".equals(requestType))) {
            return "Loại yêu cầu không hợp lệ.";
        }
        if (!("ADD".equals(actionType) || "UPDATE".equals(actionType) || "REMOVE".equals(actionType))) {
            return "Hành động thay đổi ca không hợp lệ.";
        }
        if (reason.isBlank()) {
            return "Vui lòng nhập lý do gửi đơn.";
        }
        return null;
    }

    private String deriveScopeType(String requestType, String actionType) {
        if ("PERMANENT".equals(requestType)) {
            return "WEEKLY_TEMPLATE";
        }
        return "ONE_DATE";
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Date parseDate(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Date.valueOf(LocalDate.parse(value.trim()));
        } catch (Exception ex) {
            return null;
        }
    }

    private void writeSwapOptionsResponse(HttpServletRequest request, HttpServletResponse response,
            int requesterDoctorId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Date workDate = parseDate(request.getParameter("workDate"));
        Integer dayOfWeek = parseInteger(request.getParameter("dayOfWeek"));
        if (dayOfWeek == null) {
            if (workDate != null) {
                dayOfWeek = normalizeDayOfWeek(workDate.toLocalDate().getDayOfWeek());
            }
        }

        if (dayOfWeek == null || dayOfWeek < 0 || dayOfWeek > 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("[]");
            return;
        }

        DoctorScheduleDAO requestDAO = new DoctorScheduleDAO();
        List<ScheduleSwapShiftOption> options;
        if (workDate != null) {
            Map<Integer, ScheduleSwapShiftOption> optionByShiftId = new LinkedHashMap<>();
            for (int d = 0; d <= 6; d++) {
                List<ScheduleSwapShiftOption> dayOptions = requestDAO.getSwapShiftOptionsByDate(requesterDoctorId, d);
                for (ScheduleSwapShiftOption option : dayOptions) {
                    DoctorScheduleDAO.EffectiveShiftState state = requestDAO.getEffectiveShiftStateForDate(option.getShiftId(), workDate);
                    if (state != null && state.getDayOfWeek() == dayOfWeek) {
                        option.setDayOfWeek(state.getDayOfWeek());
                        option.setStartTime(state.getStartTime());
                        option.setEndTime(state.getEndTime());
                        optionByShiftId.putIfAbsent(option.getShiftId(), option);
                    }
                }
            }
            options = List.copyOf(optionByShiftId.values());
        } else {
            options = requestDAO.getSwapShiftOptionsByDate(requesterDoctorId, dayOfWeek);
        }

        try (PrintWriter out = response.getWriter()) {
            out.write("[");
            for (int i = 0; i < options.size(); i++) {
                ScheduleSwapShiftOption option = options.get(i);
                if (i > 0) {
                    out.write(",");
                }
                String label = option.getDoctorName() + " - " + getDayLabel(option.getDayOfWeek())
                        + " (" + option.getStartTime().format(TIME_FMT) + " - " + option.getEndTime().format(TIME_FMT) + ")";
                out.write("{\"shiftId\":" + option.getShiftId()
                        + ",\"doctorId\":" + option.getDoctorId()
                        + ",\"doctorName\":\"" + escapeJson(option.getDoctorName()) + "\""
                        + ",\"label\":\"" + escapeJson(label) + "\"}");
            }
            out.write("]");
        }
    }

    private int normalizeDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }

    private boolean hasShiftStarted(LocalDate workDate, LocalTime shiftStartTime) {
        if (workDate == null || shiftStartTime == null) {
            return false;
        }
        LocalDateTime shiftStart = LocalDateTime.of(workDate, shiftStartTime);
        return !LocalDateTime.now().isBefore(shiftStart);
    }

    private LocalDate resolveShiftDateForOneDateRequest(LocalDate selectedDate, int selectedDayOfWeek, int shiftDayOfWeek) {
        int normalizedSelectedDay = selectedDayOfWeek == 0 ? 7 : selectedDayOfWeek;
        int normalizedShiftDay = shiftDayOfWeek == 0 ? 7 : shiftDayOfWeek;
        int dayOffset = normalizedShiftDay - normalizedSelectedDay;
        return selectedDate.plusDays(dayOffset);
    }

    private String getDayLabel(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 0 ->
                "Chủ nhật";
            case 1 ->
                "Thứ 2";
            case 2 ->
                "Thứ 3";
            case 3 ->
                "Thứ 4";
            case 4 ->
                "Thứ 5";
            case 5 ->
                "Thứ 6";
            case 6 ->
                "Thứ 7";
            default ->
                "Không xác định";
        };
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}






