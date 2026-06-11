<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Hồ sơ sức khỏe</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/profile/patientHealthDashboard.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />

        <main class="health-page">
            <section class="page-hero">
                <div class="hero-badge">Patient Portal</div>
                <h1>Hồ sơ sức khỏe</h1>
                <p>Chọn bệnh nhân và cuộc hẹn để xem nhanh thông tin quan trọng.</p>
            </section>

            <section class="filter-card">
                <form method="get" action="${pageContext.request.contextPath}/patient-health-dashboard" class="filter-form">
                    <div class="field-group">
                        <label for="patientPickerTrigger">Bệnh nhân</label>
                        <c:set var="selectedPatientName" value="Chọn bệnh nhân" />
                        <c:forEach var="p" items="${patients}">
                            <c:if test="${selectedPatientId eq p.patientId}">
                                <c:set var="selectedPatientName" value="${p.fullName}" />
                            </c:if>
                        </c:forEach>
                        <div class="custom-select" id="patientPicker">
                            <input type="hidden" id="patientId" name="patientId" value="${selectedPatientId}" />
                            <button type="button" class="custom-select-trigger" id="patientPickerTrigger">
                                <span class="select-icon" aria-hidden="true">👤</span>
                                <span id="patientPickerLabel">${selectedPatientName}</span>
                                <span class="custom-select-caret" aria-hidden="true"></span>
                            </button>
                            <div class="custom-select-menu" id="patientPickerMenu">
                                <button type="button" class="custom-option ${empty selectedPatientId ? 'selected' : ''}" data-value="">Chọn bệnh nhân</button>
                                <c:forEach var="p" items="${patients}">
                                    <button type="button" class="custom-option ${selectedPatientId eq p.patientId ? 'selected' : ''}" data-value="${p.patientId}">${p.fullName}</button>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <div class="field-group">
                        <label for="appointmentId">Cuộc hẹn</label>
                        <div class="select-wrap">
                            <span class="select-icon" aria-hidden="true">📅</span>
                            <select id="appointmentId" name="appointmentId" onchange="this.form.submit()" ${empty selectedPatientId ? 'disabled' : ''}>
                                <option value="">Chọn cuộc hẹn</option>
                                <c:forEach var="a" items="${appointments}">
                                    <option value="${a.appointmentId}" ${selectedAppointmentId eq a.appointmentId ? 'selected' : ''}>
                                        #${a.appointmentId} - <fmt:formatDate value="${a.appointmentDate}" pattern="dd/MM/yyyy" /> ${a.appointmentTime}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <c:set var="isPrescriptionTab" value="${currentTab eq 'prescription' or param.tab eq 'prescription' or param.view eq 'prescription'}" />
                    <input type="hidden" name="tab" value="${isPrescriptionTab ? 'prescription' : 'record'}" />
                </form>
            </section>

            <section class="view-switch">
                <a class="view-btn ${not isPrescriptionTab ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/patient-health-dashboard?patientId=${selectedPatientId}&appointmentId=${selectedAppointmentId}&tab=record">
                    <span class="view-icon">📋</span> Hồ sơ bệnh án
                </a>
                <a class="view-btn ${isPrescriptionTab ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/patient-health-dashboard?patientId=${selectedPatientId}&appointmentId=${selectedAppointmentId}&tab=prescription">
                    <span class="view-icon">💊</span> Đơn thuốc
                </a>
            </section>

            <c:if test="${empty selectedPatientId}">
                <div class="empty-card">Vui lòng chọn bệnh nhân để tiếp tục.</div>
            </c:if>

            <c:if test="${not empty selectedPatientId and empty selectedAppointmentId}">
                <div class="empty-card">Vui lòng chọn cuộc hẹn để xem chi tiết hồ sơ bệnh án hoặc đơn thuốc.</div>
            </c:if>

            <c:if test="${not empty selectedAppointmentId}">
                <c:choose>
                    <c:when test="${isPrescriptionTab}">
                        <c:if test="${empty selectedPrescription}">
                            <div class="empty-card">Cuộc hẹn này chưa có đơn thuốc.</div>
                        </c:if>

                        <c:if test="${not empty selectedPrescription}">
                            <article class="rx-card">
                                <div class="card-head">
                                    <div>
                                        <span class="rx-id">Đơn #${selectedPrescription.prescriptionId}</span>
                                        <span class="patient-chip">Bệnh nhân: ${selectedPrescription.patientName}</span>
                                        <span class="doctor-chip">Bác sĩ: ${selectedPrescription.doctorName}</span>
                                    </div>
                                    <div class="record-date">
                                        <fmt:formatDate value="${selectedPrescription.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </div>
                                </div>

                                <section class="rx-overview">
                                    <div class="rx-detail-block">
                                        <div class="rx-field-label">Lịch khám</div>
                                        <div class="rx-field-value">#${selectedPrescription.appointmentId} · <fmt:formatDate value="${selectedPrescription.appointmentDate}" pattern="dd/MM/yyyy" /> ${selectedPrescription.appointmentTime}</div>
                                    </div>
                                    <div class="rx-detail-block">
                                        <div class="rx-field-label">Chẩn đoán</div>
                                        <div class="rx-field-value ${empty selectedPrescription.diagnosis ? 'muted' : ''}">${empty selectedPrescription.diagnosis ? 'Chưa cập nhật' : selectedPrescription.diagnosis}</div>
                                    </div>
                                    <div class="rx-detail-block full">
                                        <div class="rx-field-label">Ghi chú đơn thuốc</div>
                                        <div class="rx-field-value ${empty selectedPrescription.prescriptionNote ? 'muted' : ''}">${empty selectedPrescription.prescriptionNote ? 'Không có' : selectedPrescription.prescriptionNote}</div>
                                    </div>
                                </section>

                                <div class="rx-table-wrap">
                                    <table class="rx-table">
                                        <thead>
                                            <tr>
                                                <th>Thuốc</th>
                                                <th>Liều dùng</th>
                                                <th>Số lần/ngày</th>
                                                <th>Số ngày</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="item" items="${selectedPrescription.prescriptionItems}">
                                                <tr>
                                                    <td>${item.medicineName}${empty item.unit ? '' : ' ('}${empty item.unit ? '' : item.unit}${empty item.unit ? '' : ')'}</td>
                                                    <td>${empty item.dosage ? '---' : item.dosage}</td>
                                                    <td>${empty item.frequency ? '---' : item.frequency}</td>
                                                    <td>${empty item.durationDays ? '---' : item.durationDays}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </article>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${empty selectedRecord}">
                            <div class="empty-card">Cuộc hẹn này chưa có hồ sơ bệnh án.</div>
                        </c:if>

                        <c:if test="${not empty selectedRecord}">
                            <article class="record-card">
                                <div class="card-head">
                                    <div>
                                        <span class="record-id">#${selectedRecord.appointmentId}</span>
                                        <span class="patient-chip">Bệnh nhân: ${selectedRecord.patientName}</span>
                                        <span class="doctor-chip">Bác sĩ: ${selectedRecord.doctorName}</span>
                                    </div>
                                    <div class="record-date">
                                        <fmt:formatDate value="${selectedRecord.appointmentDate}" pattern="dd/MM/yyyy" /> · ${selectedRecord.appointmentTime}
                                    </div>
                                </div>

                                <div class="record-grid">
                                    <section class="section-box">
                                        <h3 class="section-title">Triệu chứng</h3>
                                        <p class="section-content ${empty selectedRecord.symptoms ? 'muted' : ''}">
                                            ${empty selectedRecord.symptoms ? 'Chưa có thông tin.' : selectedRecord.symptoms}
                                        </p>
                                    </section>

                                    <section class="section-box">
                                        <h3 class="section-title">Chẩn đoán</h3>
                                        <p class="section-content ${empty selectedRecord.diagnosis ? 'muted' : ''}">
                                            ${empty selectedRecord.diagnosis ? 'Chưa có thông tin.' : selectedRecord.diagnosis}
                                        </p>
                                    </section>

                                    <section class="section-box" style="grid-column: 1 / -1;">
                                        <h3 class="section-title">Tiền sử</h3>
                                        <div class="history-grid">
                                            <div class="history-item">
                                                <h4 class="history-item-title">Dị ứng</h4>
                                                <p class="history-item-value ${empty selectedRecord.historyAllergies ? 'muted' : ''}">${empty selectedRecord.historyAllergies ? 'Chưa có thông tin.' : selectedRecord.historyAllergies}</p>
                                            </div>
                                            <div class="history-item">
                                                <h4 class="history-item-title">Bệnh mãn tính</h4>
                                                <p class="history-item-value ${empty selectedRecord.historyChronic ? 'muted' : ''}">${empty selectedRecord.historyChronic ? 'Chưa có thông tin.' : selectedRecord.historyChronic}</p>
                                            </div>
                                            <div class="history-item">
                                                <h4 class="history-item-title">Tiền sử gia đình</h4>
                                                <p class="history-item-value ${empty selectedRecord.historyFamily ? 'muted' : ''}">${empty selectedRecord.historyFamily ? 'Chưa có thông tin.' : selectedRecord.historyFamily}</p>
                                            </div>
                                            <div class="history-item">
                                                <h4 class="history-item-title">Tiền sử xã hội</h4>
                                                <p class="history-item-value ${empty selectedRecord.historySocial ? 'muted' : ''}">${empty selectedRecord.historySocial ? 'Chưa có thông tin.' : selectedRecord.historySocial}</p>
                                            </div>
                                            <div class="history-item" style="grid-column: 1 / -1;">
                                                <h4 class="history-item-title">Lịch sử tiêm chủng</h4>
                                                <p class="history-item-value ${empty selectedRecord.historyVaccination ? 'muted' : ''}">${empty selectedRecord.historyVaccination ? 'Chưa có thông tin.' : selectedRecord.historyVaccination}</p>
                                            </div>
                                        </div>
                                    </section>

                                    <section class="section-box">
                                        <h3 class="section-title">Ghi chú bác sĩ</h3>
                                        <p class="section-content ${empty selectedRecord.doctorNote ? 'muted' : ''}">
                                            ${empty selectedRecord.doctorNote ? 'Chưa có ghi chú thêm.' : selectedRecord.doctorNote}
                                        </p>
                                    </section>

                                    <section class="section-box">
                                        <h3 class="section-title">Phương án điều trị</h3>
                                        <p class="section-content ${empty selectedRecord.treatmentPlan ? 'muted' : ''}">
                                            ${empty selectedRecord.treatmentPlan ? 'Chưa có phương án điều trị.' : selectedRecord.treatmentPlan}
                                        </p>
                                    </section>
                                </div>
                            </article>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </main>
        <script>
            (function () {
                var picker = document.getElementById('patientPicker');
                if (!picker) {
                    return;
                }

                var trigger = document.getElementById('patientPickerTrigger');
                var menu = document.getElementById('patientPickerMenu');
                var hiddenInput = document.getElementById('patientId');
                var label = document.getElementById('patientPickerLabel');
                var form = picker.closest('form');

                trigger.addEventListener('click', function () {
                    picker.classList.toggle('open');
                });

                menu.addEventListener('click', function (event) {
                    var option = event.target.closest('.custom-option');
                    if (!option) {
                        return;
                    }

                    hiddenInput.value = option.getAttribute('data-value');
                    label.textContent = option.textContent.trim();

                    menu.querySelectorAll('.custom-option').forEach(function (item) {
                        item.classList.remove('selected');
                    });
                    option.classList.add('selected');

                    picker.classList.remove('open');
                    form.submit();
                });

                document.addEventListener('click', function (event) {
                    if (!picker.contains(event.target)) {
                        picker.classList.remove('open');
                    }
                });
            })();
        </script>
    </body>
</html>
