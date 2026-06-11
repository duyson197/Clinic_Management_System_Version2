<%-- 
    Document   : appointmentSecond
    Created on : Feb 3, 2026, 2:00:08 PM
    Author     : Admin
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Check information</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/pages/appointments/appointment/appointment.css">
    </head>
    <body>

        <jsp:include page="/common/header.jsp" />

        <div class="page">
            <div class="content">

                <!-- LEFT -->
                <div class="main">

                    <!-- STEPS -->
                    <div class="steps-wrapper">

                        <div class="step active">
                            <div class="circle">✓</div>
                            <p>Xác nhận và chọn ca</p>
                        </div>

                        <div class="line"></div>

                        <div class="step">
                            <div class="circle">2</div>
                            <p>Thanh toán</p>
                        </div>

                        <div class="line"></div>

                        <div class="step">
                            <div class="circle">3</div>
                            <p>Hoàn tất</p>
                        </div>

                    </div>
                    <!-- THÔNG TIN XÁC NHẬN -->

                    <form method="post" action="${pageContext.request.contextPath}/appointmentservlet">

                        <div class="card-box">
                            <h3>Thông tin bệnh nhân</h3>

                            <div class="form-grid confirm-view">
                                <div>
                                    <label>Họ và tên</label>
                                    <p >${patient.getFullName()}</p>
                                </div>


                                <div>
                                    <label>Số điện thoại</label>
                                    <p>${ patient.phone}</p>
                                </div>

                                <div>
                                    <label>Email</label>
                                    <p>${patient.getEmail()}</p>
                                </div>

                                <div>
                                    <label>Ngày sinh</label>
                                    <p>
                                        <fmt:formatDate value="${patient.dob}" pattern="dd/MM/yyyy"/>
                                    </p>
                                </div>

                                <div>
                                    <label>Giới tính</label>
                                    <p>${patient.getGender()}</p>
                                </div>


                            </div>
                            <div>
                                <label>Triệu chứng bệnh *:</label>
                                <textarea name="note" class="note-box" placeholder="Nhập triệu chứng bệnh ..." required></textarea>

                            </div>
                            <c:if test="${not empty errorTime}">
                                <div class="error-time-box">
                                    <span class="error-time-icon">⏰</span>
                                    <span class="error-time-msg">${errorTime}</span>
                                </div>
                            </c:if> 
                            <!-- NGÀY + CA KHÁM -->
                            <div class="card-box">
                                <h3>Chọn ngày và ca khám</h3>

                                <label>Ngày khám *</label>

                                <div class="time-slots" id="dateRadios">

                                    <c:forEach items="${dates}" var="d" varStatus="s">

                                        <input type="radio" 
                                               name="appointment_date"
                                               id="date${s.index}"
                                               value="${d}"
                                               data-periods="${availablePeriodsByDate[d.toString()]}"
                                               ${s.index == 0 ? "checked" : ""}>

                                        <label for="date${s.index}" class="slot">
                                            <strong>${displayDates[d.toString()]}</strong>
                                        </label>

                                    </c:forEach>

                                </div>

                                <div class="time-slots">

                                    <input type="radio" name="time" id="morning" value="07:00" checked>
                                    <label for="morning" class="slot">
                                        <strong>Ca sáng</strong>
                                        <span>07:00 - 11:30</span>
                                    </label>

                                    <input type="radio" name="time" id="afternoon" value="13:00">
                                    <label for="afternoon" class="slot">
                                        <strong>Ca chiều</strong>
                                        <span>13:00 - 16:30</span>
                                    </label>

                                </div>

                            </div>


                            <input type="hidden" name="doctorID" value="${doctor.getDoctorId()}">
                            <input type="hidden" name="patientID" value="${patient.getPatientId()}">
                            <input type="hidden" name="userID" value="${sessionScope.account.userId}">
                            <input type="hidden" name="pricePay" value="${doctor.price}">
                            <input type="hidden" name="bookingStyle"
                                   value="${sessionScope.roleName == 'receptionist' ? 'walk_in' : 'online'}">
                            <!-- ACTION -->

                            <div class="actions">
                                <button type="button" class="btn-outline"
                                        onclick="location.href = '${pageContext.request.contextPath}/listofdoctorservlet'">
                                    Quay lại
                                </button>



                                <button type="submit"name="btnSubmit" value="thanhtoan"class="btn-primary"onclick="return confirmSubmit()">
                                    Xác nhận & Thanh toán
                                </button>

                            </div>
                        </div>
                    </form>


                </div>

                <!-- RIGHT  -->
                <div class="card">
                    <img src="${doctor.getImage()}" alt="Doctor">

                    <h3>${doctor.getFullName()}</h3>
                    <p class="degree">${doctor.getQualification()}</p>
                    <p class="desc">${doctor.getSpecialization()}</p>
                    <br>
                    <div class="info">
                        <span>⏱ ${doctor.getExperience_years()} năm</span>
                        <span>⭐ ${doctor.getRating()}</span>
                    </div>
                    <br>
                    <p class="price">
                        <fmt:formatNumber value="${doctor.price}" type="number"/>đ
                    </p>


                </div>


            </div>
        </div>
        <script>
            function confirmSubmit() {
                return confirm("Bạn đã chắc chắn đúng và đủ thông tin chưa?\nVui lòng kiểm tra kĩ các thông tin trước khi thanh toán.");
            }

            (function () {
                var morningInput = document.getElementById('morning');
                var afternoonInput = document.getElementById('afternoon');
                if (!morningInput || !afternoonInput) {
                    return;
                }

                function updateAvailablePeriods() {
                    var selectedDateInput = document.querySelector('input[name="appointment_date"]:checked');
                    var periodsRaw = selectedDateInput ? (selectedDateInput.getAttribute('data-periods') || '') : '';
                    var periods = periodsRaw.split(',').map(function (p) {
                        return p.trim();
                    }).filter(function (p) {
                        return p.length > 0;
                    });

                    var hasMorning = periods.indexOf('MORNING') >= 0;
                    var hasAfternoon = periods.indexOf('AFTERNOON') >= 0;

                    morningInput.disabled = !hasMorning;
                    afternoonInput.disabled = !hasAfternoon;

                    var morningLabel = document.querySelector('label[for="morning"]');
                    var afternoonLabel = document.querySelector('label[for="afternoon"]');
                    if (morningLabel) {
                        morningLabel.style.display = hasMorning ? '' : 'none';
                    }
                    if (afternoonLabel) {
                        afternoonLabel.style.display = hasAfternoon ? '' : 'none';
                    }

                    if (morningInput.checked && morningInput.disabled) {
                        morningInput.checked = false;
                    }
                    if (afternoonInput.checked && afternoonInput.disabled) {
                        afternoonInput.checked = false;
                    }
                    if (!morningInput.checked && !afternoonInput.checked) {
                        if (hasMorning) {
                            morningInput.checked = true;
                        } else if (hasAfternoon) {
                            afternoonInput.checked = true;
                        }
                    }
                }

                document.querySelectorAll('input[name="appointment_date"]').forEach(function (input) {
                    input.addEventListener('change', updateAvailablePeriods);
                });
                updateAvailablePeriods();
            })();
        </script>
        <jsp:include page="/common/footer.jsp" />

    </body>
</html>

