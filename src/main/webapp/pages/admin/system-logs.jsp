<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>System Logs</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                background: #f3f4f6; /* giống lab-queue.jsp (var(--bg)) */
                min-height: 100vh;
            }
            .container {
                padding: 30px 50px;
                max-width: 1400px;
                margin: 0 auto;
            }
            .page-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
            }
            .page-title {
                font-size: 26px;
                font-weight: 700;
                color: #1f2933;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .page-title span {
                font-size: 14px;
                font-weight: 500;
                color: #6b7280;
            }
            .back-link {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                color: #2563eb;
                text-decoration: none;
                font-weight: 500;
                background: rgba(37, 99, 235, 0.06);
                padding: 8px 12px;
                border-radius: 999px;
                border: 1px solid rgba(37, 99, 235, 0.15);
            }
            .back-link:hover {
                background: rgba(37, 99, 235, 0.12);
            }
            .filter-card {
                background: white;
                padding: 20px;
                border-radius: 12px;
                margin-bottom: 20px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
                display: grid;
                grid-template-columns: minmax(240px, 2fr) minmax(160px, 1.1fr) minmax(160px, 1.1fr) minmax(160px, 1.1fr) auto;
                gap: 14px;
                align-items: end;
            }
            .filter-group label {
                display: block;
                font-weight: 600;
                margin-bottom: 6px;
                font-size: 13px;
                color: #374151;
            }
            .filter-group input,
            .filter-group select {
                width: 100%;
                padding: 9px 11px;
                border-radius: 7px;
                border: 1px solid #d1d5db;
                font-size: 14px;
                outline: none;
                transition: all 0.2s ease;
                background: #f9fafb;
            }
            .filter-group input:focus,
            .filter-group select:focus {
                border-color: #2563eb;
                background: #fff;
                box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.18);
            }
            .filter-actions {
                display: flex;
                gap: 10px;
                justify-content: flex-end;
            }
            .btn {
                border-radius: 7px;
                border: none;
                padding: 10px 14px;
                font-size: 14px;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
                gap: 6px;
                cursor: pointer;
                transition: all 0.2s ease;
                white-space: nowrap;
            }
            .btn-primary {
                background: #2563eb;
                color: white;
                box-shadow: 0 8px 16px rgba(37, 99, 235, 0.28);
            }
            .btn-primary:hover {
                background: #1d4ed8;
                transform: translateY(-1px);
                box-shadow: 0 12px 20px rgba(37, 99, 235, 0.32);
            }
            .btn-secondary {
                background: #e5e7eb;
                color: #374151;
            }
            .btn-secondary:hover {
                background: #d1d5db;
            }
            .card {
                background: white;
                border-radius: 12px;
                padding: 18px 20px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
            }
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 12px;
            }
            .card-title {
                font-size: 17px;
                font-weight: 600;
                color: #111827;
                display: flex;
                align-items: center;
                gap: 8px;
            }
            .card-subtitle {
                font-size: 13px;
                color: #6b7280;
            }
            .badge {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                padding: 4px 10px;
                border-radius: 999px;
                font-size: 12px;
                font-weight: 500;
                background: #f3f4f6;
                color: #4b5563;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 8px;
            }
            thead {
                background: #f9fafb;
            }
            th, td {
                padding: 10px 10px;
                text-align: left;
                font-size: 13px;
                border-bottom: 1px solid #e5e7eb;
            }
            th {
                font-size: 12px;
                text-transform: uppercase;
                letter-spacing: 0.04em;
                color: #6b7280;
                font-weight: 600;
            }
            tbody tr:hover {
                background: #f9fafb;
            }
            .col-time {
                width: 200px;
                white-space: nowrap;
            }
            .col-user {
                width: 240px;
            }
            .col-action {
                width: 180px;
                white-space: nowrap;
            }
            .log-action {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 4px 9px;
                border-radius: 999px;
                font-size: 11px;
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 0.03em;
            }
            .log-action.login {
                background: rgba(16, 185, 129, 0.12);
                color: #047857;
            }
            .log-action.user {
                background: rgba(59, 130, 246, 0.12);
                color: #1d4ed8;
            }
            .log-action.lab {
                background: rgba(236, 72, 153, 0.12);
                color: #be185d;
            }
            .log-action.other {
                background: rgba(107, 114, 128, 0.12);
                color: #374151;
            }
            .log-description {
                font-size: 13px;
                color: #374151;
            }
            .log-meta {
                font-size: 12px;
                color: #6b7280;
            }
            .empty-state {
                padding: 24px 12px;
                text-align: center;
                color: #6b7280;
                font-size: 14px;
            }
            .pagination {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: 16px;
                font-size: 13px;
                color: #4b5563;
            }
            .pagination-buttons {
                display: flex;
                gap: 6px;
            }
            .page-btn {
                min-width: 32px;
                height: 32px;
                border-radius: 8px;
                border: 1px solid #d1d5db;
                background: white;
                font-size: 13px;
                cursor: pointer;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                transition: all 0.15s ease;
            }
            .page-btn:hover {
                background: #f3f4f6;
            }
            .page-btn.active {
                background: #2563eb;
                color: white;
                border-color: #2563eb;
                box-shadow: 0 3px 8px rgba(37, 99, 235, 0.3);
            }
            .page-btn.disabled {
                opacity: 0.45;
                cursor: default;
                background: #f3f4f6;
            }
        </style>
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />
        <div class="container">
            <div class="page-header">
                <div>
                    <div class="page-title">
                        <i class="fa-solid fa-clipboard-list"></i>
                        System Logs
                    </div>
                    <div class="card-subtitle" style="margin-top:4px;">
                        Theo dõi các hành động quan trọng trên hệ thống: đăng nhập, tạo tài khoản, tạo phiếu xét nghiệm...
                    </div>
                </div>
                
            </div>

            <form method="get" class="filter-card">
                <div class="filter-group">
                    <label for="keyword">Từ khóa</label>
                    <input type="text" id="keyword" name="keyword"
                           placeholder="Tìm theo mô tả, hành động, người dùng..."
                           value="${keyword}"/>
                </div>
                <div class="filter-group">
                    <label for="actionFilter">Loại hành động</label>
                    <select id="actionFilter" name="actionFilter">
                        <option value="">Tất cả</option>
                        <optgroup label="--- Xác thực & Tài khoản ---">
                            <option value="LOGIN_SUCCESS" ${"LOGIN_SUCCESS" == actionFilter ? "selected" : ""}>Đăng nhập</option>
                            <option value="LOGOUT" ${"LOGOUT" == actionFilter ? "selected" : ""}>Đăng xuất</option>
                            <option value="REGISTER_SUCCESS" ${"REGISTER_SUCCESS" == actionFilter ? "selected" : ""}>Đăng ký tài khoản</option>
                            <option value="PASSWORD_RESET" ${"PASSWORD_RESET" == actionFilter ? "selected" : ""}>Đặt lại mật khẩu</option>
                            <option value="PASSWORD_CHANGED" ${"PASSWORD_CHANGED" == actionFilter ? "selected" : ""}>Đổi mật khẩu</option>
                            <option value="PROFILE_UPDATED" ${"PROFILE_UPDATED" == actionFilter ? "selected" : ""}>Cập nhật hồ sơ cá nhân</option>
                        </optgroup>
                        <optgroup label="--- Quản lý nhân viên ---">
                            <option value="CREATE_USER" ${"CREATE_USER" == actionFilter ? "selected" : ""}>Tạo tài khoản nhân viên</option>
                            <option value="UPDATE_USER" ${"UPDATE_USER" == actionFilter ? "selected" : ""}>Cập nhật tài khoản nhân viên</option>
                            <option value="TOGGLE_USER_STATUS" ${"TOGGLE_USER_STATUS" == actionFilter ? "selected" : ""}>Khóa/Mở khóa tài khoản nhân viên</option>
                            <option value="RESEND_USER_PASSWORD" ${"RESEND_USER_PASSWORD" == actionFilter ? "selected" : ""}>Gửi lại mật khẩu nhân viên</option>
                            <option value="STAFF_ADDED" ${"STAFF_ADDED" == actionFilter ? "selected" : ""}>Thêm nhân viên</option>
                            <option value="STAFF_UPDATED" ${"STAFF_UPDATED" == actionFilter ? "selected" : ""}>Cập nhật nhân viên</option>
                            <option value="STAFF_RESEND_PASSWORD" ${"STAFF_RESEND_PASSWORD" == actionFilter ? "selected" : ""}>Gửi lại mật khẩu nhân viên (staff)</option>
                        </optgroup>
                        <optgroup label="--- Quản lý bệnh nhân ---">
                            <option value="CREATE_PATIENT_ACCOUNT" ${"CREATE_PATIENT_ACCOUNT" == actionFilter ? "selected" : ""}>Tạo tài khoản bệnh nhân</option>
                            <option value="UPDATE_PATIENT_ACCOUNT" ${"UPDATE_PATIENT_ACCOUNT" == actionFilter ? "selected" : ""}>Cập nhật tài khoản bệnh nhân</option>
                            <option value="TOGGLE_PATIENT_ACCOUNT_STATUS" ${"TOGGLE_PATIENT_ACCOUNT_STATUS" == actionFilter ? "selected" : ""}>Khóa/Mở khóa tài khoản bệnh nhân</option>
                            <option value="RESEND_PATIENT_ACCOUNT_PASSWORD" ${"RESEND_PATIENT_ACCOUNT_PASSWORD" == actionFilter ? "selected" : ""}>Gửi lại mật khẩu bệnh nhân</option>
                        </optgroup>
                        <optgroup label="--- Lịch làm việc ---">
                            <option value="SHIFT_ADDED" ${"SHIFT_ADDED" == actionFilter ? "selected" : ""}>Thêm ca làm việc</option>
                            <option value="SHIFT_UPDATED" ${"SHIFT_UPDATED" == actionFilter ? "selected" : ""}>Cập nhật ca làm việc</option>
                            <option value="SHIFT_DELETED" ${"SHIFT_DELETED" == actionFilter ? "selected" : ""}>Xóa ca làm việc</option>
                            <option value="DOCTOR_CREATE_SCHEDULE_CHANGE_REQUEST" ${"DOCTOR_CREATE_SCHEDULE_CHANGE_REQUEST" == actionFilter ? "selected" : ""}>Yêu cầu đổi ca (bác sĩ)</option>
                        </optgroup>
                        <optgroup label="--- Lịch hẹn ---">
                            <option value="APPOINTMENT_BOOKED" ${"APPOINTMENT_BOOKED" == actionFilter ? "selected" : ""}>Đặt lịch hẹn</option>
                            <option value="APPOINTMENT_PAYMENT_INIT" ${"APPOINTMENT_PAYMENT_INIT" == actionFilter ? "selected" : ""}>Khởi tạo thanh toán lịch hẹn</option>
                            <option value="APPOINTMENT_STATUS_UPDATED" ${"APPOINTMENT_STATUS_UPDATED" == actionFilter ? "selected" : ""}>Cập nhật trạng thái lịch hẹn</option>
                            <option value="CHECKIN_APPOINTMENT" ${"CHECKIN_APPOINTMENT" == actionFilter ? "selected" : ""}>Check-in lịch hẹn</option>
                            <option value="CANCEL_APPOINTMENT" ${"CANCEL_APPOINTMENT" == actionFilter ? "selected" : ""}>Hủy lịch hẹn</option>
                        </optgroup>
                        <optgroup label="--- Khám bệnh & Hồ sơ y tế ---">
                            <option value="EXAM_FINISHED" ${"EXAM_FINISHED" == actionFilter ? "selected" : ""}>Hoàn thành khám bệnh</option>
                            <option value="PRESCRIPTION_SAVED" ${"PRESCRIPTION_SAVED" == actionFilter ? "selected" : ""}>Lưu đơn thuốc</option>
                            <option value="MEDICAL_RECORD_SAVED" ${"MEDICAL_RECORD_SAVED" == actionFilter ? "selected" : ""}>Lưu hồ sơ bệnh án</option>
                            <option value="DOCTOR_REVIEWED" ${"DOCTOR_REVIEWED" == actionFilter ? "selected" : ""}>Đánh giá bác sĩ</option>
                        </optgroup>
                        <optgroup label="--- Xét nghiệm ---">
                            <option value="LAB_REQUEST_CREATED" ${"LAB_REQUEST_CREATED" == actionFilter ? "selected" : ""}>Tạo yêu cầu xét nghiệm (bác sĩ)</option>
                            <option value="CREATE_LAB_REQUEST" ${"CREATE_LAB_REQUEST" == actionFilter ? "selected" : ""}>Tạo phiếu xét nghiệm</option>
                            <option value="UPLOAD_LAB_RESULT" ${"UPLOAD_LAB_RESULT" == actionFilter ? "selected" : ""}>Tải kết quả xét nghiệm</option>
                            <option value="LAB_PAYMENT_CONFIRMED" ${"LAB_PAYMENT_CONFIRMED" == actionFilter ? "selected" : ""}>Xác nhận thanh toán xét nghiệm</option>
                        </optgroup>
                        <optgroup label="--- Dịch vụ ---">
                            <option value="SERVICE_ADDED" ${"SERVICE_ADDED" == actionFilter ? "selected" : ""}>Thêm dịch vụ</option>
                            <option value="SERVICE_UPDATED" ${"SERVICE_UPDATED" == actionFilter ? "selected" : ""}>Cập nhật dịch vụ</option>
                            <option value="SERVICE_DELETED" ${"SERVICE_DELETED" == actionFilter ? "selected" : ""}>Xóa dịch vụ</option>
                        </optgroup>
                        <optgroup label="--- Khác ---">
                            <option value="VIEW_TECH_DASHBOARD_STATS" ${"VIEW_TECH_DASHBOARD_STATS" == actionFilter ? "selected" : ""}>Xem thống kê kỹ thuật viên</option>
                            <option value="VIEW_DOCTOR_DASHBOARD" ${"VIEW_DOCTOR_DASHBOARD" == actionFilter ? "selected" : ""}>Xem dashboard bác sĩ</option>
                        </optgroup>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="fromDate">Từ ngày</label>
                    <input type="date" id="fromDate" name="fromDate" value="${fromDate}"/>
                </div>
                <div class="filter-group">
                    <label for="toDate">Đến ngày</label>
                    <input type="date" id="toDate" name="toDate" value="${toDate}"/>
                </div>
                <div class="filter-actions">
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        Lọc logs
                    </button>
                    <a href="${pageContext.request.contextPath}/admin-system-logs" class="btn btn-secondary">
                        <i class="fa-solid fa-rotate-right"></i>
                        Reset
                    </a>
                </div>
            </form>

            <div class="card">
                <div class="card-header">
                    <div class="card-title">
                        <i class="fa-solid fa-stream"></i>
                        Lịch sử hoạt động hệ thống
                    </div>
                    <div class="badge">
                        <i class="fa-regular fa-circle-dot"></i>
                        <span>${totalLogs} bản ghi</span>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th class="col-time">Thời gian</th>
                            <th class="col-user">Người thực hiện</th>
                            <th class="col-action">Hành động</th>
                            <th>Mô tả chi tiết</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty logs}">
                            <tr>
                                <td colspan="4" class="empty-state">
                                    <i class="fa-regular fa-face-smile-beam"></i>
                                    Chưa có log nào phù hợp với bộ lọc.
                                </td>
                            </tr>
                        </c:if>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td class="col-time">
                                    <div class="log-meta">
                                        <i class="fa-regular fa-clock"></i>
                                        <span>
                                            <fmt:formatDate value="${log.createdAt}"
                                                            pattern="dd/MM/yyyy HH:mm"
                                                            timeZone="Asia/Ho_Chi_Minh"/>
                                        </span>
                                    </div>
                                </td>
                                <td class="col-user">
                                    <div class="log-meta">
                                        <i class="fa-regular fa-user"></i>
                                        <c:choose>
                                            <c:when test="${log.userId != null}">
                                                <span>
                                                    <strong>${log.userFullName}</strong>
                                                    <c:if test="${not empty log.userRole}">
                                                        (<c:choose>
                                                            <c:when test="${log.userRole == 'admin'}">Admin</c:when>
                                                            <c:when test="${log.userRole == 'doctor'}">Bác sĩ</c:when>
                                                            <c:when test="${log.userRole == 'receptionist'}">Tiếp tân</c:when>
                                                            <c:when test="${log.userRole == 'technician'}">Kỹ thuật viên</c:when>
                                                            <c:when test="${log.userRole == 'patient'}">Bệnh nhân</c:when>
                                                            <c:otherwise>${log.userRole}</c:otherwise>
                                                        </c:choose>)
                                                    </c:if>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span>Hệ thống</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                                <td class="col-action">
                                    <c:set var="actionClass" value="other"/>
                                    <c:if test="${log.action == 'LOGIN_SUCCESS'}">
                                        <c:set var="actionClass" value="login"/>
                                    </c:if>
                                    <c:if test="${log.action == 'CREATE_USER' || log.action == 'UPDATE_USER' || log.action == 'TOGGLE_USER_STATUS'}">
                                        <c:set var="actionClass" value="user"/>
                                    </c:if>
                                    <c:if test="${log.action == 'CREATE_LAB_REQUEST'}">
                                        <c:set var="actionClass" value="lab"/>
                                    </c:if>
                                    <c:if test="${log.action == 'CANCEL_APPOINTMENT' || log.action == 'CHECKIN_APPOINTMENT' || log.action == 'UPDATE_APPOINTMENT_STATUS'}">
                                        <c:set var="actionClass" value="user"/>
                                    </c:if>
                                    <c:if test="${log.action == 'VIEW_TECH_DASHBOARD_STATS' || log.action == 'VIEW_DOCTOR_DASHBOARD'}">
                                        <c:set var="actionClass" value="lab"/>
                                    </c:if>
                                    <span class="log-action ${actionClass}">
                                        <i class="fa-solid fa-circle"></i>
                                        ${log.action}
                                    </span>
                                </td>
                                <td>
                                    <div class="log-description">
                                        ${log.description}
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <div class="pagination">
                    <div>
                        Trang ${currentPage} / ${totalPages}
                    </div>
                    <div class="pagination-buttons">
                        <c:set var="cp" value="${currentPage}"/>
                        <button type="button"
                                class="page-btn ${cp == 1 ? 'disabled' : ''}"
                                onclick="changePage(${cp - 1})"
                                ${cp == 1 ? 'disabled' : ''}>
                            <i class="fa-solid fa-angle-left"></i>
                        </button>
                        <c:forEach var="p" begin="1" end="${totalPages}">
                            <button type="button"
                                    class="page-btn ${p == cp ? 'active' : ''}"
                                    onclick="changePage(${p})">
                                ${p}
                            </button>
                        </c:forEach>
                        <button type="button"
                                class="page-btn ${cp == totalPages ? 'disabled' : ''}"
                                onclick="changePage(${cp + 1})"
                                ${cp == totalPages ? 'disabled' : ''}>
                            <i class="fa-solid fa-angle-right"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../../common/footer.jsp" />
                            
        <script>
            function changePage(page) {
                if (!page || page < 1) return;
                const url = new URL(window.location.href);
                url.searchParams.set('page', page);
                window.location.href = url.toString();
            }

            // Auto filter for report: thay đổi dropdown / date là lọc luôn
            (function () {
                const form = document.querySelector('.filter-card');
                if (!form) return;

                const actionSelect = document.getElementById('actionFilter');
                const fromInput = document.getElementById('fromDate');
                const toInput = document.getElementById('toDate');
                const keywordInput = document.getElementById('keyword');

                function submitFilter() {
                    form.submit();
                }

                if (actionSelect) {
                    actionSelect.addEventListener('change', submitFilter);
                }
                if (fromInput) {
                    fromInput.addEventListener('change', submitFilter);
                }
                if (toInput) {
                    toInput.addEventListener('change', submitFilter);
                }
                if (keywordInput) {
                    keywordInput.addEventListener('keypress', function (e) {
                        if (e.key === 'Enter') {
                            e.preventDefault();
                            submitFilter();
                        }
                    });
                }
            })();
        </script>
    </body>
    </html>

