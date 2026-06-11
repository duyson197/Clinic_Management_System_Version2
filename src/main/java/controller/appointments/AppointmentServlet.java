package controller.appointments;

import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import dal.AppointmentDAO;
import dal.DoctorDAO;
import dal.PatientPortalDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.User;
import util.SystemLogService;

public class AppointmentServlet extends HttpServlet {

    private static final String CLIENT_ID = "e76a6cbb-71b7-40a3-bd89-69c577698cb9";
    private static final String API_KEY = "512e43a7-c663-4519-ab90-6f183569a75d";
    private static final String CHECKSUM_KEY = "370d7efb2d9ce65c36e7b943087d5876090b8664cc64edb9ec7ba9a334ee56c1";
    private static final PayOS payOS = new PayOS(CLIENT_ID, API_KEY, CHECKSUM_KEY);
    private static final String BASE_URL = "http://localhost:8080/PhongKhamDaLieu";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        User user = (User) session.getAttribute("account");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        String doctorID = request.getParameter("doctor");
        String patientID = request.getParameter("patientid");

        if (doctorID == null || doctorID.isEmpty()) {
            doctorID = (String) request.getAttribute("doctor");
        }
        if (patientID == null || patientID.isEmpty()) {
            Object pid = request.getAttribute("patientid");
            patientID = pid != null ? pid.toString() : null;
        }

        int patientId = Integer.parseInt(patientID);
        int doctorId = Integer.parseInt(doctorID);

        AppointmentDAO dao = new AppointmentDAO();
        PatientPortalDAO daos = new PatientPortalDAO();
        DoctorDAO doctordao = new DoctorDAO();

        Patient p = daos.getPatientsByPatientID(patientId);
        Doctor doctor = doctordao.getDoctorById(doctorID);
        if (doctor == null) {
            response.sendRedirect(request.getContextPath() + "/listofdoctorservlet");
            return;
        }

        List<LocalDate> availableDates = dao.getAvailableDates(doctorId);
        Map<String, String> availablePeriodsByDate = dao.getAvailablePeriodCsvByDates(doctorId, availableDates);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Map<String, String> displayDates = new HashMap<>();
        for (LocalDate d : availableDates) {
            displayDates.put(d.toString(), d.format(formatter));
        }

        request.setAttribute("displayDates", displayDates);
        request.setAttribute("doctor", doctor);
        request.setAttribute("patient", p);
        request.setAttribute("dates", availableDates);
        request.setAttribute("availablePeriodsByDate", availablePeriodsByDate);

        request.getRequestDispatcher("/pages/appointments/appointment/appointmentCheck.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        User user = (User) session.getAttribute("account");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
            return;
        }
        String userID = request.getParameter("userID");
        int useId = Integer.parseInt(userID);

        String doctorID = request.getParameter("doctorID");
        int doctorId = Integer.parseInt(doctorID);
        DoctorDAO doctorDAO = new DoctorDAO();
        Doctor doctor = doctorDAO.getDoctorById(doctorID);
        if (doctor == null) {
            response.sendRedirect(request.getContextPath() + "/listofdoctorservlet");
            return;
        }
        String patientID = request.getParameter("patientID");
        int patientId = Integer.parseInt(patientID);
        String note = request.getParameter("note");
        String bookingStyle = request.getParameter("bookingStyle");
        String dateStr = request.getParameter("appointment_date");
        java.sql.Date sqlDate = java.sql.Date.valueOf(dateStr);
        String timeStr = request.getParameter("time");

        LocalTime localTime = LocalTime.parse(timeStr);
        java.sql.Time sqlTime = java.sql.Time.valueOf(localTime);
        String submit = request.getParameter("btnSubmit");

        LocalDate appointmentDate = sqlDate.toLocalDate();
        LocalDate today = LocalDate.now();

        if (appointmentDate.equals(today)) {
            LocalTime now = LocalTime.now();
            LocalTime endMorning = LocalTime.of(11, 30);
            LocalTime endAfternoon = LocalTime.of(16, 30);

            String errorTime = null;

            if (localTime.equals(LocalTime.of(7, 0)) && now.isAfter(endMorning)) {
                errorTime = "Ca sáng hôm nay đã kết thúc, vui lòng chọn ca chiều hoặc ngày khác.";
            } else if (localTime.equals(LocalTime.of(13, 0)) && now.isAfter(endAfternoon)) {
                errorTime = "Ca chiều hôm nay đã kết thúc, vui lòng chọn ngày khác.";
            }

            if (errorTime != null) {
                PatientPortalDAO daoPa = new PatientPortalDAO();
                AppointmentDAO daoApp = new AppointmentDAO();

                Patient patient = daoPa.getPatientsByPatientID(patientId);
                List<LocalDate> availableDates = daoApp.getAvailableDates(doctorId);
                Map<String, String> availablePeriodsByDate
                        = daoApp.getAvailablePeriodCsvByDates(doctorId, availableDates);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                Map<String, String> displayDates = new HashMap<>();
                for (LocalDate d : availableDates) {
                    displayDates.put(d.toString(), d.format(formatter));
                }

                request.setAttribute("errorTime", errorTime);
                request.setAttribute("doctor", doctor);
                request.setAttribute("patient", patient);
                request.setAttribute("note", note);
                request.setAttribute("dates", availableDates);
                request.setAttribute("availablePeriodsByDate", availablePeriodsByDate);
                request.setAttribute("displayDates", displayDates);

                request.getRequestDispatcher("/pages/appointments/appointment/appointmentCheck.jsp")
                        .forward(request, response);
                return;
            }
        }

        PatientPortalDAO daos = new PatientPortalDAO();
        Patient patient = daos.getPatientsByPatientID(patientId);

        Appointment appointment;
        if (user.getRole().toString().equals("receptionist")) {
            appointment = new Appointment(patientId, doctorId, 1, "walk_in", sqlDate, sqlTime, "booked", note);

        } else {
            appointment = new Appointment(patientId, doctorId, 1, bookingStyle, sqlDate, sqlTime, "booked", note);
        }
        if (submit != null && submit.equalsIgnoreCase("thanhtoan")) {

            try {
                String priceStr = request.getParameter("pricePay");

                long amount;
                try {
                    String cleaned = priceStr.trim().replace(",", "");
                    amount = (long) Double.parseDouble(cleaned);
                } catch (NumberFormatException e) {
                    throw new Exception("Giá tiền không đúng định dạng: " + priceStr);
                }
                session.removeAttribute("pendingAppointment");
                session.removeAttribute("pendingPatient");
                session.setAttribute("pendingPatient", patient);
                session.setAttribute("pendingAppointment", appointment);
                SystemLogService.log(user.getUserId(), "APPOINTMENT_PAYMENT_INIT",
                        "Khởi tạo thanh toán đặt lịch: patientId=" + patientId + ", doctorId=" + doctorId + ", date=" + dateStr + ", time=" + timeStr);
                long orderCode = (System.currentTimeMillis() % 100000000L) * 1000
                        + (long) (Math.random() * 1000);

                CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount(amount)
                        .description(patient.getFullName() + "")
                        .returnUrl("http://localhost:8080/PhongKhamDaLieu/appointmentpaymentservlet")
                        .cancelUrl(BASE_URL + "/appointmentpaymentservlet?code=cancel&status=CANCELLED")
                        .build();

                CreatePaymentLinkResponse result = payOS.paymentRequests().create(paymentRequest);

                String checkoutUrl = result.getCheckoutUrl();
                response.sendRedirect(checkoutUrl);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorPay", "Lỗi thanh toán: " + e.getMessage());
                request.getRequestDispatcher("/pages/appointments/appointment/appointmentFailPayment.jsp")
                        .forward(request, response);
                return;
            }
        }

    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
