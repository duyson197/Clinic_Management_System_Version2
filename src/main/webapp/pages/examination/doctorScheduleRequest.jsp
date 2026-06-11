<%-- 
    Document   : doctorScheduleRequest
    Created on : 14 Mar 2026, 4:38:28 am
    Author     : anngu
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Yêu cầu đổi lịch làm việc</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/examination/doctorScheduleRequest.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />

        <div class="schedule-request-container">
            <div class="hero-card">
                <h2>Lịch làm việc của bạn</h2>
            </div>

            <c:if test="${not empty sessionScope.scheduleRequestSuccess}">
                <div class="alert success">${sessionScope.scheduleRequestSuccess}</div>
                <c:remove var="scheduleRequestSuccess" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.scheduleRequestError}">
                <div class="alert error">${sessionScope.scheduleRequestError}</div>
                <c:remove var="scheduleRequestError" scope="session"/>
            </c:if>

            <div class="content-grid">
                <section class="panel">
                    <h3>Lịch làm việc tuần hiện tại</h3>
                    <c:choose>
                        <c:when test="${empty weeklyShifts}">
                            <p class="empty">Bạn chưa có ca làm việc nào được cấu hình.</p>
                        </c:when>
                        <c:otherwise>
                            <table class="shift-table">
                                <thead>
                                    <tr>
                                        <th>Mã ca</th>
                                        <th>Thứ</th>
                                        <th>Giờ làm</th>
                                        <th>Số bệnh nhân tối đa</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="shift" items="${weeklyShifts}">
                                        <tr>
                                            <td>#${shift.shiftId}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${shift.dayOfWeek == 0}">Chủ nhật</c:when>
                                                    <c:when test="${shift.dayOfWeek == 1}">Thứ 2</c:when>
                                                    <c:when test="${shift.dayOfWeek == 2}">Thứ 3</c:when>
                                                    <c:when test="${shift.dayOfWeek == 3}">Thứ 4</c:when>
                                                    <c:when test="${shift.dayOfWeek == 4}">Thứ 5</c:when>
                                                    <c:when test="${shift.dayOfWeek == 5}">Thứ 6</c:when>
                                                    <c:when test="${shift.dayOfWeek == 6}">Thứ 7</c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${shift.startTime} - ${shift.endTime}</td>
                                            <td>${shift.maxPatients}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="panel">
                    <h3>Tạo đơn yêu cầu đổi lịch</h3>
                    <form method="post" action="${pageContext.request.contextPath}/doctor/schedule-request" class="request-form" id="scheduleRequestForm">
                        <div class="form-row">
                            <label>Loại yêu cầu
                                <select name="requestType" id="requestType" required>
                                    <option value="TEMPORARY">Đổi lịch làm việc tạm thời</option>
                                    <option value="PERMANENT">Đổi lịch làm việc dài hạn</option>
                                </select>
                            </label>
                            <input type="hidden" name="scopeType" id="scopeType" value="ONE_DATE">
                        </div>

                        <p class="form-hint" id="scopeHint"></p>

                        <div class="form-row two-col">
                            <label>Đơn yêu cầu
                                <select name="actionType" id="actionType" required>
                                    <option value="ADD">Thêm ca</option>
                                    <option value="UPDATE">Đổi ca</option>
                                    <option value="REMOVE">Hủy ca</option>
                                </select>
                            </label>
                        </div>

                        <div class="form-row" id="targetShiftGroup">
                            <label>Ca gốc cần sửa/hủy
                                <select name="targetShiftId" id="targetShiftId">
                                    <option value="">-- Chọn ca gốc --</option>
                                    <c:forEach var="shift" items="${weeklyShifts}">
                                        <option value="${shift.shiftId}">#${shift.shiftId} - 
                                            <c:choose>
                                                <c:when test="${shift.dayOfWeek == 0}">Chủ nhật</c:when>
                                                <c:when test="${shift.dayOfWeek == 1}">Thứ 2</c:when>
                                                <c:when test="${shift.dayOfWeek == 2}">Thứ 3</c:when>
                                                <c:when test="${shift.dayOfWeek == 3}">Thứ 4</c:when>
                                                <c:when test="${shift.dayOfWeek == 4}">Thứ 5</c:when>
                                                <c:when test="${shift.dayOfWeek == 5}">Thứ 6</c:when>
                                                <c:when test="${shift.dayOfWeek == 6}">Thứ 7</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                            (${shift.startTime} - ${shift.endTime})</option>
                                        </c:forEach>
                                </select>
                            </label>
                        </div>

                        <div class="form-row" id="oneDateGroup">
                            <label>Ngày áp dụng
                                <input type="date" name="workDate" id="workDate">
                            </label>
                        </div>

                        <div class="form-row" id="weeklyTemplateGroup">
                            <label>Thứ áp dụng
                                <select name="dayOfWeek" id="dayOfWeek">
                                    <option value="">-- Chọn thứ --</option>
                                    <option value="0">Chủ nhật</option>
                                    <option value="1">Thứ 2</option>
                                    <option value="2">Thứ 3</option>
                                    <option value="3">Thứ 4</option>
                                    <option value="4">Thứ 5</option>
                                    <option value="5">Thứ 6</option>
                                    <option value="6">Thứ 7</option>
                                </select>
                            </label>
                        </div>

                        <div class="form-row" id="swapShiftGroup">
                            <label>Ca bác sĩ muốn đổi
                                <select name="swapShiftId" id="swapShiftId">
                                    <option value="">-- Chọn ngày áp dụng để tải danh sách ca --</option>
                                </select>
                            </label>
                            <p class="field-hint">Chỉ hiển thị ca của bác sĩ khác theo đúng ngày/thứ bạn chọn.</p>
                        </div>


                        <div id="timeAndCapacityGroup">
                            <div class="form-row">
                                <label>Ca làm việc
                                    <select name="shiftPeriod" id="shiftPeriod">
                                        <option value="">-- Chọn ca --</option>
                                        <option value="MORNING">Ca sáng</option>
                                        <option value="AFTERNOON">Ca chiều</option>
                                    </select>
                                </label>
                            </div>
                            <div class="form-row">
                                <label>Số bệnh nhân tối đa
                                    <input type="number" min="1" name="maxPatients" id="maxPatients" placeholder="VD: 20">
                                </label>
                            </div>
                        </div>

                        <div class="form-row">
                            <label>Lý do gửi yêu cầu
                                <textarea name="reason" rows="4" required placeholder="Nhập lý do đổi lịch, bối cảnh và thời gian cần áp dụng..."></textarea>
                            </label>
                        </div>

                        <button type="submit" class="btn-submit">Gửi yêu cầu duyệt</button>
                    </form>
                </section>
            </div>

            <section class="panel">

                <h3>Lịch sử đơn đổi lịch gần đây</h3>
                <c:choose>
                    <c:when test="${empty recentRequests}">
                        <p class="empty">Chưa có yêu cầu nào được gửi.</p>
                    </c:when>
                    <c:otherwise>
                        <table class="request-table">
                            <thead>
                                <tr>
                                    <th>Mã đơn</th>
                                    <th>Loại</th>
                                    <th>Hành động</th>
                                    <th>Thời gian</th>
                                    <th>Chi tiết đơn</th>
                                    <th>Trạng thái</th>
                                    <th>Ghi chú admin</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${recentRequests}">
                                    <tr>
                                        <td>#${item.requestId}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.requestType == 'TEMPORARY'}">Tạm thời</c:when>
                                                <c:when test="${item.requestType == 'PERMANENT'}">Dài hạn</c:when>
                                                <c:otherwise>${item.requestType}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.actionType == 'ADD'}">Thêm ca</c:when>
                                                <c:when test="${item.actionType == 'UPDATE'}">Sửa ca</c:when>
                                                <c:when test="${item.actionType == 'REMOVE'}">Hủy ca</c:when>
                                                <c:otherwise>${item.actionType}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><fmt:formatDate value="${item.requestedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.actionType == 'UPDATE'}">
                                                    <c:choose>
                                                        <c:when test="${item.requestType == 'PERMANENT'}">
                                                            <c:choose>
                                                                <c:when test="${item.oldDayOfWeek == 0}">CN</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 1}">T2</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 2}">T3</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 3}">T4</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 4}">T5</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 5}">T6</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 6}">T7</c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                            ${item.oldStartTime} - ${item.oldEndTime}
                                                            <span class="detail-arrow">→</span>
                                                            <c:choose>
                                                                <c:when test="${item.dayOfWeek == 0}">CN</c:when>
                                                                <c:when test="${item.dayOfWeek == 1}">T2</c:when>
                                                                <c:when test="${item.dayOfWeek == 2}">T3</c:when>
                                                                <c:when test="${item.dayOfWeek == 3}">T4</c:when>
                                                                <c:when test="${item.dayOfWeek == 4}">T5</c:when>
                                                                <c:when test="${item.dayOfWeek == 5}">T6</c:when>
                                                                <c:when test="${item.dayOfWeek == 6}">T7</c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                            ${item.startTime} - ${item.endTime}
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:if test="${not empty item.oldWorkDate}">
                                                                <fmt:formatDate value="${item.oldWorkDate}" pattern="dd/MM" />
                                                            </c:if>
                                                            ${item.oldStartTime} - ${item.oldEndTime}
                                                            <span class="detail-arrow">→</span>
                                                            <fmt:formatDate value="${item.workDate}" pattern="dd/MM" />
                                                            ${item.startTime} - ${item.endTime}
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${not empty item.newDoctorName}">
                                                        <div class="subtle-note">Đổi với: ${item.newDoctorName}</div>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="displayStartTime" value="${item.startTime}" />
                                                    <c:set var="displayEndTime" value="${item.endTime}" />
                                                    <c:if test="${empty displayStartTime}">
                                                        <c:set var="displayStartTime" value="${item.oldStartTime}" />
                                                    </c:if>
                                                    <c:if test="${empty displayEndTime}">
                                                        <c:set var="displayEndTime" value="${item.oldEndTime}" />
                                                    </c:if>
                                                    <c:choose>
                                                        <c:when test="${not empty item.workDate}">
                                                            <fmt:formatDate value="${item.workDate}" pattern="dd/MM/yyyy" />
                                                        </c:when>
                                                        <c:when test="${not empty item.dayOfWeek}">
                                                            <c:choose>
                                                                <c:when test="${item.dayOfWeek == 0}">Chủ nhật</c:when>
                                                                <c:when test="${item.dayOfWeek == 1}">Thứ 2</c:when>
                                                                <c:when test="${item.dayOfWeek == 2}">Thứ 3</c:when>
                                                                <c:when test="${item.dayOfWeek == 3}">Thứ 4</c:when>
                                                                <c:when test="${item.dayOfWeek == 4}">Thứ 5</c:when>
                                                                <c:when test="${item.dayOfWeek == 5}">Thứ 6</c:when>
                                                                <c:otherwise>Thứ 7</c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:when test="${not empty item.oldDayOfWeek}">
                                                            <c:choose>
                                                                <c:when test="${item.oldDayOfWeek == 0}">Chủ nhật</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 1}">Thứ 2</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 2}">Thứ 3</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 3}">Thứ 4</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 4}">Thứ 5</c:when>
                                                                <c:when test="${item.oldDayOfWeek == 5}">Thứ 6</c:when>
                                                                <c:otherwise>Thứ 7</c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                    - ${displayStartTime} - ${displayEndTime}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="status ${item.status}">
                                                <c:choose>
                                                    <c:when test="${item.status == 'PENDING'}">Đang chờ duyệt</c:when>
                                                    <c:when test="${item.status == 'APPROVED'}">Đã duyệt</c:when>
                                                    <c:when test="${item.status == 'REJECTED'}">Từ chối</c:when>
                                                    <c:otherwise>${item.status}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td class="admin-note-cell">
                                            <c:choose>
                                                <c:when test="${empty item.adminNote}">-</c:when>
                                                <c:otherwise><c:out value="${item.adminNote}"/></c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </section>
        </div>

        <script>
            (function () {
                const requestType = document.getElementById('requestType');
                const scopeType = document.getElementById('scopeType');
                const actionType = document.getElementById('actionType');
                const scheduleRequestForm = document.getElementById('scheduleRequestForm');

                const scopeHint = document.getElementById('scopeHint');
                const oneDateGroup = document.getElementById('oneDateGroup');
                const weeklyTemplateGroup = document.getElementById('weeklyTemplateGroup');
                const targetShiftGroup = document.getElementById('targetShiftGroup');
                const timeAndCapacityGroup = document.getElementById('timeAndCapacityGroup');
                const swapShiftGroup = document.getElementById('swapShiftGroup');

                const oneDateInput = document.getElementById('workDate');
                const weeklyInput = document.getElementById('dayOfWeek');
                const targetShiftInput = document.getElementById('targetShiftId');
                const swapShiftInput = document.getElementById('swapShiftId');
                const shiftPeriodInput = document.getElementById('shiftPeriod');
                const maxPatientsInput = document.getElementById('maxPatients');

                function toDateInputValue(date) {
                    const tzOffset = date.getTimezoneOffset() * 60000;
                    return new Date(date.getTime() - tzOffset).toISOString().slice(0, 10);
                }

                const today = new Date();
                oneDateInput.min = toDateInputValue(today);

                function toggleGroup(group, input, visible) {
                    group.classList.toggle('hidden', !visible);
                    input.disabled = !visible;
                    if (!visible) {
                        input.value = '';
                    }
                }

                function toggleTimeCapacity(visible) {
                    timeAndCapacityGroup.classList.toggle('hidden', !visible);
                    shiftPeriodInput.disabled = !visible;
                    if (!visible) {
                        shiftPeriodInput.value = '';
                    }
                }

                function toggleMaxPatients(visible) {
                    const maxPatientsRow = maxPatientsInput.closest('.form-row');
                    if (maxPatientsRow) {
                        maxPatientsRow.classList.toggle('hidden', !visible);
                    }
                    maxPatientsInput.disabled = !visible;
                    if (!visible) {
                        maxPatientsInput.value = '';
                    }
                }

                function loadSwapShiftOptions() {
                    if (swapShiftInput.disabled) {
                        swapShiftInput.innerHTML = '<option value="">-- Chọn phạm vi áp dụng để tải danh sách ca --</option>';
                        return;
                    }

                    const isTemporary = scopeType.value === 'ONE_DATE';
                    const selectedDate = oneDateInput.value;
                    const selectedDay = weeklyInput.value;

                    if (isTemporary && !selectedDate) {
                        swapShiftInput.innerHTML = '<option value="">-- Chọn ngày áp dụng để tải danh sách ca --</option>';
                        return;
                    }

                    if (!isTemporary && !selectedDay) {
                        swapShiftInput.innerHTML = '<option value="">-- Chọn thứ áp dụng để tải danh sách ca --</option>';
                        return;
                    }

                    let url = scheduleRequestForm.action + '?mode=SWAP_OPTIONS';
                    if (isTemporary) {
                        url += '&workDate=' + encodeURIComponent(selectedDate);
                    } else {
                        url += '&dayOfWeek=' + encodeURIComponent(selectedDay);
                    }
                    fetch(url, {headers: {'Accept': 'application/json'}})
                            .then(function (response) {
                                if (!response.ok) {
                                    return [];
                                }
                                return response.json();
                            })
                            .then(function (items) {
                                if (!Array.isArray(items) || items.length === 0) {
                                    swapShiftInput.innerHTML = '<option value="">-- Không có ca phù hợp trong ngày này --</option>';
                                    return;
                                }
                                swapShiftInput.innerHTML = '<option value="">-- Chọn ca bác sĩ muốn đổi --</option>';
                                items.forEach(function (item) {
                                    const option = document.createElement('option');
                                    option.value = item.shiftId;
                                    option.textContent = item.label;
                                    swapShiftInput.appendChild(option);
                                });
                            })
                            .catch(function () {
                                swapShiftInput.innerHTML = '<option value="">-- Không thể tải danh sách ca --</option>';
                            });
                }

                function applyTypeScopeRules() {
                    if (requestType.value === 'PERMANENT') {
                        scopeType.value = 'WEEKLY_TEMPLATE';
                        scopeHint.textContent = actionType.value === 'REMOVE'
                                ? 'Hủy ca dài hạn: chọn ca gốc để hủy trong lịch tuần chuẩn.'
                                : (actionType.value === 'UPDATE'
                                        ? 'Đổi ca dài hạn: chọn thứ áp dụng và ca của bác sĩ khác để đổi theo lịch tuần chuẩn.'
                                        : 'Yêu cầu dài hạn sẽ thay đổi lịch tuần chuẩn.');
                    } else {
                        scopeType.value = 'ONE_DATE';
                        scopeHint.textContent = actionType.value === 'REMOVE'
                                ? 'Hủy ca tạm thời: chọn ngày áp dụng và ca sáng/chiều cần hủy.'
                                : (actionType.value === 'UPDATE'
                                        ? 'Đổi ca tạm thời: chọn 1 ngày cụ thể và chọn ca của bác sĩ khác để đổi.'
                                        : 'Yêu cầu tạm thời chỉ áp dụng cho 1 ngày cụ thể.');
                    }
                }

                function renderFormBySelection() {
                    applyTypeScopeRules();

                    const scope = scopeType.value;
                    const action = actionType.value;

                    const oneDateVisible = scope === 'ONE_DATE';
                    toggleGroup(oneDateGroup, oneDateInput, oneDateVisible);
                    toggleGroup(weeklyTemplateGroup, weeklyInput,
                            scope === 'WEEKLY_TEMPLATE' && (action === 'ADD' || action === 'UPDATE'));
                    const requireTargetShift = action === 'UPDATE' || (action === 'REMOVE' && scope === 'WEEKLY_TEMPLATE');
                    toggleGroup(targetShiftGroup, targetShiftInput, requireTargetShift);
                    toggleGroup(swapShiftGroup, swapShiftInput, action === 'UPDATE');
                    const showShiftPeriod = action === 'ADD' || (action === 'REMOVE' && scope === 'ONE_DATE');
                    toggleTimeCapacity(showShiftPeriod);
                    toggleMaxPatients(action === 'ADD');

                    loadSwapShiftOptions();
                }

                requestType.addEventListener('change', renderFormBySelection);
                actionType.addEventListener('change', renderFormBySelection);
                oneDateInput.addEventListener('change', loadSwapShiftOptions);
                weeklyInput.addEventListener('change', loadSwapShiftOptions);
                renderFormBySelection();
            })();
        </script>
        <jsp:include page="/common/footer.jsp" />
    </body>
</html>
