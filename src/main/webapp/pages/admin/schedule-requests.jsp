<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Duyệt yêu cầu đổi lịch bác sĩ</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
                min-height: 100vh;
            }

            .container {
                padding: 30px 50px;
                max-width: 1400px;
                margin: 0 auto;
            }

            .panel {
                background: white;
                padding: 20px;
                border-radius: 10px;
                margin-bottom: 20px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            }

            .head {
                display: flex;
                justify-content: space-between;
                align-items: center;
                gap: 10px;
                margin-bottom: 8px;
            }

            .head h2 {
                margin: 0;
                font-size: 22px;
                color: #1f2937;
            }

            .sub {
                color: #64748b;
                font-size: 14px;
            }

            .alert {
                padding: 15px 20px;
                border-radius: 8px;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
                animation: slideIn 0.3s ease-out;
            }

            .alert.fade-out {
                animation: slideIn 0.3s ease-out reverse forwards;
            }

            .alert.success {
                background: #e8f5e9;
                color: #2e7d32;
                border-left: 4px solid #4caf50;
            }

            .alert.error {
                background: #ffebee;
                color: #c62828;
                border-left: 4px solid #f44336;
            }

            @keyframes slideIn {
                from {
                    opacity: 0;
                    transform: translateY(-10px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .toolbar {
                background: white;
                padding: 20px;
                border-radius: 10px;
                margin-bottom: 20px;
                display: grid;
                grid-template-columns: minmax(340px, 1.9fr) minmax(170px, 0.85fr) minmax(170px, 0.85fr) minmax(170px, 0.85fr) auto;
                gap: 12px;
                align-items: end;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            }

            .search-box,
            .filter-box {
                min-width: 0;
            }

            .search-box label,
            .filter-box label {
                display: block;
                font-weight: 600;
                margin-bottom: 8px;
                color: #333;
                font-size: 13px;
            }

            .search-box input,
            .filter-box select {
                width: 100%;
                padding: 10px 15px;
                border: 1px solid #ddd;
                border-radius: 6px;
                font-size: 14px;
                transition: all 0.3s ease;
            }

            .search-box input:focus,
            .filter-box select:focus {
                outline: none;
                border-color: #0061ff;
                box-shadow: 0 0 0 3px rgba(0, 97, 255, 0.1);
            }

            .toolbar-buttons {
                display: flex;
                gap: 10px;
                align-self: end;
            }

            .btn-search {
                padding: 10px 16px;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                font-size: 14px;
                display: inline-flex;
                align-items: center;
                gap: 6px;
                background: #0061ff;
                color: white;
            }

            .btn-search:hover {
                background: #0052cc;
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 97, 255, 0.3);
            }

            .btn-reset {
                padding: 10px 16px;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                font-size: 14px;
                display: inline-flex;
                align-items: center;
                gap: 6px;
                text-decoration: none;
                background: #f0f0f0;
                color: #333;
            }

            .btn-reset:hover {
                background: #e0e0e0;
            }

            .btn-muted {
                padding: 10px 16px;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                font-size: 14px;
                display: inline-flex;
                align-items: center;
                gap: 6px;
                text-decoration: none;
                background: #f1f5f9;
                color: #0f172a;
            }

            .table-container {
                background: white;
                padding: 25px;
                border-radius: 10px;
                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                overflow-x: auto;
            }

            table {
                width: 100%;
                border-collapse: collapse;
                min-width: 1000px;
            }

            th {
                background: linear-gradient(135deg, #f8f9fa 0%, #f0f0f0 100%);
                padding: 15px;
                text-align: left;
                font-weight: 600;
                color: #333;
                border-bottom: 2px solid #e0e0e0;
                font-size: 14px;
            }

            td {
                padding: 15px;
                border-bottom: 1px solid #f0f0f0;
                color: #555;
                font-size: 14px;
                vertical-align: top;
            }

            tr:hover {
                background: #f9f9f9;
            }

            /* Column balance only: keep current layout, tune widths */
            th:nth-child(1), td:nth-child(1) { width: 18%; } /* Bác sĩ */
            th:nth-child(2), td:nth-child(2) { width: 10%; } /* Loại yêu cầu */
            th:nth-child(3), td:nth-child(3) { width: 13%; } /* Phạm vi */
            th:nth-child(4), td:nth-child(4) { width: 30%; } /* Chi tiết thay đổi */
            th:nth-child(5), td:nth-child(5) { width: 11%; } /* Lý do */
            th:nth-child(6), td:nth-child(6) { width: 8%; }  /* Trạng thái */
            th:nth-child(7), td:nth-child(7) { width: 10%; } /* Thao tác */

            .status {
                display: inline-block;
                padding: 6px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
                white-space: nowrap;
            }

            .status.PENDING {
                background: #fff3cd;
                color: #856404;
            }

            .status.APPROVED {
                background: #e8f5e9;
                color: #388e3c;
            }

            .status.REJECTED {
                background: #ffebee;
                color: #d32f2f;
            }

            .action-col {
                width: 10%;
            }

            .action-stack {
                display: flex;
                flex-direction: column;
                gap: 8px;
            }

            .action-row {
                display: flex;
                gap: 6px;
                align-items: center;
            }

            .action-stack textarea {
                border: 1px solid #ddd;
                border-radius: 6px;
                padding: 8px 10px;
                font-size: 13px;
                resize: vertical;
                min-height: 64px;
            }

            .btn-action {
                border: none;
                background: none;
                cursor: pointer;
                font-size: 16px;
                padding: 6px 10px;
                border-radius: 4px;
                transition: all 0.3s ease;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                text-decoration: none;
            }

            .btn-view {
                color: #FB923C;
            }

            .btn-view:hover {
                background: #FFEDD5;
            }

            .btn-approve {
                color: #16a34a;
            }

            .btn-approve:hover {
                background: #dcfce7;
            }

            .btn-reject {
                color: #dc2626;
            }

            .btn-reject:hover {
                background: #fee2e2;
            }

            .details-panel summary {
                list-style: none;
            }

            .details-panel summary::-webkit-details-marker {
                display: none;
            }

            .empty {
                text-align: center;
                padding: 40px;
                color: #999;
            }

            .pagination-wrapper {
                margin-top: 16px;
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 8px;
                flex-wrap: wrap;
            }

            .page-link {
                min-width: 34px;
                padding: 8px 12px;
                border: 1px solid #dcdcdc;
                border-radius: 6px;
                background: #fff;
                color: #333;
                text-decoration: none;
                font-weight: 600;
                text-align: center;
                display: inline-flex;
                align-items: center;
                justify-content: center;
            }

            .page-link:hover {
                background: #f5f5f5;
            }

            .page-link.active {
                background: #0061ff;
                color: #fff;
                border-color: #0061ff;
                pointer-events: none;
            }

            .page-link.disabled {
                opacity: .5;
                cursor: not-allowed;
                pointer-events: none;
            }

            .modal {
                display: none;
                position: fixed;
                z-index: 1000;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                animation: fadeIn 0.3s ease;
                overflow-y: auto;
            }

            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }

            .modal-content {
                background-color: white;
                margin: 5% auto;
                padding: 30px;
                border-radius: 10px;
                width: 90%;
                max-width: 680px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
                animation: slideUp 0.3s ease;
            }

            @keyframes slideUp {
                from { transform: translateY(50px); opacity: 0; }
                to { transform: translateY(0); opacity: 1; }
            }

            .modal-header {
                font-size: 30px;
                font-weight: 600;
                color: #0061ff;
                margin-bottom: 25px;
                display: flex;
                align-items: center;
                gap: 10px;
                border-bottom: 2px solid #f0f0f0;
                padding-bottom: 15px;
            }

            .modal-close {
                margin-left: auto;
                cursor: pointer;
                font-size: 30px;
                line-height: 1;
                background: none;
                border: none;
                color: #999;
                transition: all 0.3s ease;
            }

            .modal-close:hover {
                color: #333;
            }

            .form-info {
                background: #f5f7fa;
                padding: 15px;
                border-radius: 6px;
                margin-bottom: 15px;
                font-size: 14px;
                color: #555;
                line-height: 1.6;
            }

            .form-info-item {
                display: flex;
                justify-content: space-between;
                gap: 12px;
                padding: 8px 0;
                border-bottom: 1px solid #e0e0e0;
            }

            .form-info-item:last-child {
                border-bottom: none;
            }

            .form-info-item strong {
                color: #333;
            }

            .group-title {
                margin: 10px 0 6px;
                font-size: 13px;
                font-weight: 700;
                color: #0f172a;
                text-transform: uppercase;
                letter-spacing: 0.02em;
            }

            .group-box {
                border: 1px solid #dbe4f3;
                border-radius: 8px;
                background: #f8fbff;
                padding: 8px 12px;
                margin-bottom: 10px;
            }

            .group-box .form-info-item {
                padding: 7px 0;
            }

            .modal-footer {
                display: flex;
                gap: 10px;
                justify-content: flex-end;
                margin-top: 25px;
                padding-top: 15px;
                border-top: 1px solid #f0f0f0;
            }

            .btn-cancel {
                padding: 10px 20px;
                background: #f0f0f0;
                color: #333;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                gap: 6px;
            }

            .btn-cancel:hover {
                background: #e0e0e0;
            }

            @media (max-width: 980px) {
                .container {
                    padding: 20px;
                }

                .toolbar {
                    grid-template-columns: 1fr;
                }

                .toolbar-buttons {
                    width: 100%;
                }

                .toolbar-buttons .btn-reset {
                    width: 100%;
                    justify-content: center;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />

        <div class="container">
            <div class="panel">
                <div class="head">
                    <div>
                        <h2>Duyệt yêu cầu đổi lịch bác sĩ</h2>
                        <div class="sub">Đơn chờ duyệt hiện tại: <strong>${pendingCount}</strong></div>
                    </div>
                    <a class="btn-muted" href="${pageContext.request.contextPath}/admin-doctor-schedules">
                        <i class="fas fa-arrow-left"></i> Quay lại lịch bác sĩ
                    </a>
                </div>

                <c:if test="${not empty sessionScope.scheduleReviewSuccess}">
                    <div class="alert success">${sessionScope.scheduleReviewSuccess}</div>
                    <c:remove var="scheduleReviewSuccess" scope="session"/>
                </c:if>
                <c:if test="${not empty sessionScope.scheduleReviewError}">
                    <div class="alert error">${sessionScope.scheduleReviewError}</div>
                    <c:remove var="scheduleReviewError" scope="session"/>
                </c:if>
            </div>

            <form id="filterForm" method="GET" action="${pageContext.request.contextPath}/admin-schedule-requests" class="toolbar">
                <div class="search-box">
                    <label>Tìm kiếm</label>
                    <input type="text" id="keywordInput" name="keyword" value="${keyword}" placeholder="Tên bác sĩ, lý do...">
                </div>
                <div class="filter-box">
                    <label>Phạm vi</label>
                    <select name="requestType" onchange="this.form.submit()">
                        <option value="ALL" ${requestTypeFilter == 'ALL' ? 'selected' : ''}>Tất cả</option>
                        <option value="TEMPORARY" ${requestTypeFilter == 'TEMPORARY' ? 'selected' : ''}>Tạm thời</option>
                        <option value="PERMANENT" ${requestTypeFilter == 'PERMANENT' ? 'selected' : ''}>Dài hạn</option>
                    </select>
                </div>
                <div class="filter-box">
                    <label>Loại yêu cầu</label>
                    <select name="actionType" onchange="this.form.submit()">
                        <option value="ALL" ${actionTypeFilter == 'ALL' ? 'selected' : ''}>Tất cả</option>
                        <option value="ADD" ${actionTypeFilter == 'ADD' ? 'selected' : ''}>Thêm ca</option>
                        <option value="UPDATE" ${actionTypeFilter == 'UPDATE' ? 'selected' : ''}>Đổi ca</option>
                        <option value="REMOVE" ${actionTypeFilter == 'REMOVE' ? 'selected' : ''}>Xóa ca</option>
                    </select>
                </div>
                <div class="filter-box">
                    <label>Trạng thái</label>
                    <select name="status" onchange="this.form.submit()">
                        <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>Tất cả</option>
                        <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
                        <option value="APPROVED" ${statusFilter == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                        <option value="REJECTED" ${statusFilter == 'REJECTED' ? 'selected' : ''}>Đã từ chối</option>
                    </select>
                </div>
                <div class="toolbar-buttons">
                    <button class="btn-search" type="submit"><i class="fas fa-search"></i> Tìm</button>
                    <a class="btn-reset" href="${pageContext.request.contextPath}/admin-schedule-requests"><i class="fas fa-redo"></i> Đặt lại</a>
                </div>
            </form>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Bác sĩ</th>
                            <th>Loại yêu cầu</th>
                            <th>Phạm vi</th>
                            <th>Chi tiết thay đổi</th>
                            <th>Lý do</th>
                            <th>Trạng thái</th>
                            <th class="action-col">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty requests}">
                                <tr>
                                    <td colspan="7" class="empty">Không có đơn phù hợp bộ lọc hiện tại.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${requests}">
                                    <tr>
                                        <td>${item.doctorName}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.actionType == 'REMOVE'}">Xóa ca</c:when>
                                                <c:when test="${item.actionType == 'UPDATE'}">Đổi ca</c:when>
                                                <c:otherwise>Thêm ca</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.requestType == 'TEMPORARY'}">Tạm thời</c:when>
                                                <c:otherwise>Dài hạn</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.actionType == 'UPDATE'}">
                                                    <c:set var="oldDateText" value="-" />
                                                    <c:set var="newDateText" value="-" />
                                                    <c:set var="oldShiftText" value="-" />
                                                    <c:set var="newShiftText" value="-" />
                                                    <c:set var="newDoctorText" value="${empty item.newDoctorName ? '-' : item.newDoctorName}" />

                                                    <c:choose>
                                                        <c:when test="${item.requestType == 'PERMANENT'}">
                                                            <c:choose>
                                                                <c:when test="${item.oldDayOfWeek == 0}"><c:set var="oldDateText" value="Chủ nhật" /></c:when>
                                                                <c:when test="${item.oldDayOfWeek == 1}"><c:set var="oldDateText" value="Thứ 2" /></c:when>
                                                                <c:when test="${item.oldDayOfWeek == 2}"><c:set var="oldDateText" value="Thứ 3" /></c:when>
                                                                <c:when test="${item.oldDayOfWeek == 3}"><c:set var="oldDateText" value="Thứ 4" /></c:when>
                                                                <c:when test="${item.oldDayOfWeek == 4}"><c:set var="oldDateText" value="Thứ 5" /></c:when>
                                                                <c:when test="${item.oldDayOfWeek == 5}"><c:set var="oldDateText" value="Thứ 6" /></c:when>
                                                                <c:when test="${item.oldDayOfWeek == 6}"><c:set var="oldDateText" value="Thứ 7" /></c:when>
                                                            </c:choose>
                                                            <c:choose>
                                                                <c:when test="${item.dayOfWeek == 0}"><c:set var="newDateText" value="Chủ nhật" /></c:when>
                                                                <c:when test="${item.dayOfWeek == 1}"><c:set var="newDateText" value="Thứ 2" /></c:when>
                                                                <c:when test="${item.dayOfWeek == 2}"><c:set var="newDateText" value="Thứ 3" /></c:when>
                                                                <c:when test="${item.dayOfWeek == 3}"><c:set var="newDateText" value="Thứ 4" /></c:when>
                                                                <c:when test="${item.dayOfWeek == 4}"><c:set var="newDateText" value="Thứ 5" /></c:when>
                                                                <c:when test="${item.dayOfWeek == 5}"><c:set var="newDateText" value="Thứ 6" /></c:when>
                                                                <c:when test="${item.dayOfWeek == 6}"><c:set var="newDateText" value="Thứ 7" /></c:when>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:if test="${not empty item.oldWorkDate}">
                                                                <fmt:formatDate value="${item.oldWorkDate}" pattern="dd/MM/yyyy" var="oldDateFmt" />
                                                                <c:set var="oldDateText" value="${oldDateFmt}" />
                                                            </c:if>
                                                            <c:if test="${not empty item.workDate}">
                                                                <fmt:formatDate value="${item.workDate}" pattern="dd/MM/yyyy" var="newDateFmt" />
                                                                <c:set var="newDateText" value="${newDateFmt}" />
                                                            </c:if>
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <c:choose>
                                                        <c:when test="${not empty item.oldStartTime and fn:startsWith(item.oldStartTime, '0')}">
                                                            <c:set var="oldShiftText" value="Sáng" />
                                                        </c:when>
                                                        <c:when test="${not empty item.oldStartTime}">
                                                            <c:set var="oldShiftText" value="Chiều" />
                                                        </c:when>
                                                    </c:choose>
                                                    <c:choose>
                                                        <c:when test="${not empty item.startTime and fn:startsWith(item.startTime, '0')}">
                                                            <c:set var="newShiftText" value="Sáng" />
                                                        </c:when>
                                                        <c:when test="${not empty item.startTime}">
                                                            <c:set var="newShiftText" value="Chiều" />
                                                        </c:when>
                                                    </c:choose>

                                                    <c:choose>
                                                        <c:when test="${oldDateText == '-' and oldShiftText == '-'}">
                                                            Không rõ ca cũ - ${newDateText} ${newShiftText} (${newDoctorText})
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${oldDateText} ${oldShiftText} - ${newDateText} ${newShiftText} (${newDoctorText})
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${not empty item.workDate}">
                                                            <fmt:formatDate value="${item.workDate}" pattern="dd/MM/yyyy" />
                                                        </c:when>
                                                        <c:when test="${not empty item.oldWorkDate}">
                                                            <fmt:formatDate value="${item.oldWorkDate}" pattern="dd/MM/yyyy" />
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
                                                    -
                                                    <c:choose>
                                                        <c:when test="${item.actionType == 'REMOVE'}">
                                                            <c:choose>
                                                                <c:when test="${not empty item.oldStartTime and fn:startsWith(item.oldStartTime, '0')}">Sáng</c:when>
                                                                <c:when test="${not empty item.oldStartTime}">Chiều</c:when>
                                                                <c:when test="${not empty item.startTime and fn:startsWith(item.startTime, '0')}">Sáng</c:when>
                                                                <c:when test="${not empty item.startTime}">Chiều</c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:choose>
                                                                <c:when test="${not empty item.startTime and fn:startsWith(item.startTime, '0')}">Sáng</c:when>
                                                                <c:when test="${not empty item.startTime}">Chiều</c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${empty item.reason ? '-' : item.reason}</td>
                                        <td>
                                            <span class="status ${item.status}">
                                                <c:choose>
                                                    <c:when test="${item.status == 'PENDING'}">Chờ duyệt</c:when>
                                                    <c:when test="${item.status == 'APPROVED'}">Đã duyệt</c:when>
                                                    <c:otherwise>Từ chối</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td class="action-col">
                                            <c:choose>
                                                <c:when test="${item.status == 'PENDING'}">
                                                    <form method="POST" action="${pageContext.request.contextPath}/admin-schedule-requests" class="action-stack">
                                                        <input type="hidden" name="action" value="review">
                                                        <input type="hidden" name="requestId" value="${item.requestId}">
                                                        <input type="hidden" name="status" value="${statusFilter}">
                                                        <input type="hidden" name="requestType" value="${requestTypeFilter}">
                                                        <input type="hidden" name="actionType" value="${actionTypeFilter}">
                                                        <input type="hidden" name="keyword" value="${keyword}">
                                                        <input type="hidden" name="page" value="${currentPage}">
                                                        <div class="action-row">
                                                            <button
                                                                class="btn-action btn-view"
                                                                type="button"
                                                                title="Xem chi tiết"
                                                                data-request-id="${item.requestId}"
                                                                data-doctor-name="${fn:escapeXml(item.doctorName)}"
                                                                data-request-type="${item.requestType}"
                                                                data-action-type="${item.actionType}"
                                                                data-scope-type="${item.scopeType}"
                                                                data-work-date="${item.workDate}"
                                                                data-day-of-week="${item.dayOfWeek}"
                                                                data-requested-at="<fmt:formatDate value='${item.requestedAt}' pattern='dd/MM/yyyy HH:mm' />"
                                                                data-status="${item.status}"
                                                                data-reason="${fn:escapeXml(item.reason)}"
                                                                data-target-shift-id="${item.targetShiftId}"
                                                                data-start-time="${item.startTime}"
                                                                data-end-time="${item.endTime}"
                                                                data-max-patients="${item.maxPatients}"
                                                                data-old-doctor-name="${fn:escapeXml(item.oldDoctorName)}"
                                                                data-old-day-of-week="${item.oldDayOfWeek}"
                                                                data-old-start-time="${item.oldStartTime}"
                                                                data-old-end-time="${item.oldEndTime}"
                                                                data-old-work-date="${item.oldWorkDate}"
                                                                data-new-doctor-name="${fn:escapeXml(item.newDoctorName)}"
                                                                data-admin-note="${fn:escapeXml(item.adminNote)}"
                                                                onclick="viewRequestDetail(this)">
                                                                <i class="fas fa-eye"></i>
                                                            </button>
                                                            <button class="btn-action btn-approve" type="submit" name="decision" value="APPROVED" title="Đồng ý">
                                                                <i class="fas fa-check"></i>
                                                            </button>
                                                            <button class="btn-action btn-reject" type="submit" name="decision" value="REJECTED" title="Từ chối">
                                                                <i class="fas fa-xmark"></i>
                                                            </button>
                                                        </div>
                                                        <textarea name="adminNote" placeholder="Ghi chú xử lý (tùy chọn)"></textarea>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <button
                                                        class="btn-action btn-view"
                                                        type="button"
                                                        title="Xem chi tiết"
                                                        data-request-id="${item.requestId}"
                                                        data-doctor-name="${fn:escapeXml(item.doctorName)}"
                                                        data-request-type="${item.requestType}"
                                                        data-action-type="${item.actionType}"
                                                        data-scope-type="${item.scopeType}"
                                                        data-work-date="${item.workDate}"
                                                        data-day-of-week="${item.dayOfWeek}"
                                                        data-requested-at="<fmt:formatDate value='${item.requestedAt}' pattern='dd/MM/yyyy HH:mm' />"
                                                        data-status="${item.status}"
                                                        data-reason="${fn:escapeXml(item.reason)}"
                                                        data-target-shift-id="${item.targetShiftId}"
                                                        data-start-time="${item.startTime}"
                                                        data-end-time="${item.endTime}"
                                                        data-max-patients="${item.maxPatients}"
                                                        data-old-doctor-name="${fn:escapeXml(item.oldDoctorName)}"
                                                        data-old-day-of-week="${item.oldDayOfWeek}"
                                                        data-old-start-time="${item.oldStartTime}"
                                                        data-old-end-time="${item.oldEndTime}"
                                                        data-old-work-date="${item.oldWorkDate}"
                                                        data-new-doctor-name="${fn:escapeXml(item.newDoctorName)}"
                                                        data-admin-note="${fn:escapeXml(item.adminNote)}"
                                                        onclick="viewRequestDetail(this)">
                                                        <i class="fas fa-eye"></i>
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <c:if test="${totalPages > 1}">
                <div class="pagination-wrapper">
                    <c:set var="maxVisiblePages" value="6" />
                    <c:set var="startPage" value="1" />
                    <c:set var="endPage" value="${totalPages}" />
                    <c:if test="${totalPages > maxVisiblePages}">
                        <c:set var="startPage" value="${currentPage - 2}" />
                        <c:set var="endPage" value="${startPage + maxVisiblePages - 1}" />
                        <c:if test="${startPage < 1}">
                            <c:set var="startPage" value="1" />
                            <c:set var="endPage" value="${maxVisiblePages}" />
                        </c:if>
                        <c:if test="${endPage > totalPages}">
                            <c:set var="endPage" value="${totalPages}" />
                            <c:set var="startPage" value="${totalPages - maxVisiblePages + 1}" />
                        </c:if>
                    </c:if>

                    <c:url var="prevUrl" value="/admin-schedule-requests">
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="requestType" value="${requestTypeFilter}" />
                        <c:param name="actionType" value="${actionTypeFilter}" />
                        <c:param name="status" value="${statusFilter}" />
                        <c:param name="page" value="${currentPage - 1}" />
                    </c:url>
                    <c:url var="nextUrl" value="/admin-schedule-requests">
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="requestType" value="${requestTypeFilter}" />
                        <c:param name="actionType" value="${actionTypeFilter}" />
                        <c:param name="status" value="${statusFilter}" />
                        <c:param name="page" value="${currentPage + 1}" />
                    </c:url>

                    <c:choose>
                        <c:when test="${currentPage > 1}">
                            <a class="page-link" href="${prevUrl}">‹ Trước</a>
                        </c:when>
                        <c:otherwise>
                            <span class="page-link disabled">‹ Trước</span>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${startPage > 1}">
                        <span class="page-link disabled">...</span>
                    </c:if>

                    <c:forEach var="i" begin="${startPage}" end="${endPage}">
                        <c:url var="pageUrl" value="/admin-schedule-requests">
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="requestType" value="${requestTypeFilter}" />
                            <c:param name="actionType" value="${actionTypeFilter}" />
                            <c:param name="status" value="${statusFilter}" />
                            <c:param name="page" value="${i}" />
                        </c:url>
                        <c:choose>
                            <c:when test="${i == currentPage}">
                                <span class="page-link active">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a class="page-link" href="${pageUrl}">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <c:if test="${endPage < totalPages}">
                        <span class="page-link disabled">...</span>
                    </c:if>

                    <c:choose>
                        <c:when test="${currentPage < totalPages}">
                            <a class="page-link" href="${nextUrl}">Sau ›</a>
                        </c:when>
                        <c:otherwise>
                            <span class="page-link disabled">Sau ›</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <div id="viewRequestModal" class="modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <i class="fas fa-file-circle-check"></i>
                        <span>Chi tiết đơn đổi lịch</span>
                        <button class="modal-close" type="button" onclick="closeRequestModal()">×</button>
                    </div>

                    <div class="form-info">
                        <div class="form-info-item"><strong>Bác sĩ gửi yêu cầu:</strong><span id="mDoctorName"></span></div>
                        <div class="form-info-item"><strong>Loại yêu cầu:</strong><span id="mRequestType"></span></div>
                        <div class="form-info-item"><strong>Phạm vi:</strong><span id="mScopeType"></span></div>

                        <div id="mAddSection">
                            <div class="form-info-item"><strong>Ngày áp dụng:</strong><span id="mAddApplyDate"></span></div>
                            <div class="form-info-item"><strong>Ca làm việc:</strong><span id="mAddShift"></span></div>
                            <div class="form-info-item"><strong>Số bệnh nhân tối đa:</strong><span id="mAddMaxPatients"></span></div>
                        </div>

                        <div id="mUpdateSection">
                            <div class="group-title">Ca làm việc gốc</div>
                            <div class="group-box">
                                <div class="form-info-item"><strong>Ngày áp dụng:</strong><span id="mOldApplyDate"></span></div>
                                <div class="form-info-item"><strong>Ca làm việc:</strong><span id="mOldShift"></span></div>
                            </div>

                            <div class="group-title">Ca làm việc muốn đổi</div>
                            <div class="group-box">
                                <div class="form-info-item"><strong>Bác sĩ:</strong><span id="mNewDoctor"></span></div>
                                <div class="form-info-item"><strong>Ngày áp dụng:</strong><span id="mNewApplyDate"></span></div>
                                <div class="form-info-item"><strong>Ca làm việc:</strong><span id="mNewShift"></span></div>
                            </div>
                        </div>

                        <div id="mRemoveSection">
                            <div class="form-info-item"><strong>Ngày áp dụng:</strong><span id="mRemoveApplyDate"></span></div>
                            <div class="form-info-item"><strong>Ca làm việc:</strong><span id="mRemoveShift"></span></div>
                        </div>

                        <div class="form-info-item"><strong>Lý do:</strong><span id="mReason"></span></div>
                        <div class="form-info-item"><strong>Ngày gửi:</strong><span id="mRequestedAt"></span></div>
                        <div class="form-info-item"><strong>Trạng thái:</strong><span id="mStatus"></span></div>
                        <div class="form-info-item"><strong>Ghi chú admin:</strong><span id="mAdminNote"></span></div>
                    </div>

                    <div class="modal-footer">
                        <button class="btn-cancel" type="button" onclick="closeRequestModal()">
                            <i class="fas fa-times"></i> Đóng
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <script>
            function mapRequestType(value) {
                if (value === 'REMOVE') return 'Xóa ca';
                if (value === 'UPDATE') return 'Đổi ca';
                return 'Thêm ca';
            }

            function mapActionType(value) {
                return value === 'TEMPORARY' ? 'Tạm thời' : 'Dài hạn';
            }

            function mapScopeType(value) {
                return value === 'TEMPORARY' ? 'Tạm thời' : 'Dài hạn';
            }

            function mapDayOfWeek(value) {
                const map = {
                    '0': 'Chủ nhật',
                    '1': 'Thứ 2',
                    '2': 'Thứ 3',
                    '3': 'Thứ 4',
                    '4': 'Thứ 5',
                    '5': 'Thứ 6',
                    '6': 'Thứ 7'
                };
                return map[String(value)] || '-';
            }

            function mapStatusText(value) {
                if (value === 'PENDING') return 'Chờ duyệt';
                if (value === 'APPROVED') return 'Đã duyệt';
                return 'Từ chối';
            }

            function mapApplyDate(workDate, dayOfWeek) {
                if (workDate && workDate !== 'null') {
                    const parts = workDate.split('-');
                    if (parts.length === 3) {
                        return parts[2] + '/' + parts[1] + '/' + parts[0];
                    }
                    return workDate;
                }
                if (dayOfWeek && dayOfWeek !== 'null') {
                    return mapDayOfWeek(dayOfWeek);
                }
                return '-';
            }

            function parseIsoDate(isoDate) {
                if (!isoDate || isoDate === 'null') return null;
                const parts = isoDate.split('-');
                if (parts.length !== 3) return null;
                const y = parseInt(parts[0], 10);
                const m = parseInt(parts[1], 10) - 1;
                const d = parseInt(parts[2], 10);
                const date = new Date(y, m, d);
                return isNaN(date.getTime()) ? null : date;
            }

            function formatDateDdMmYyyy(date) {
                const dd = String(date.getDate()).padStart(2, '0');
                const mm = String(date.getMonth() + 1).padStart(2, '0');
                const yyyy = date.getFullYear();
                return dd + '/' + mm + '/' + yyyy;
            }

            function deriveOldApplyDate(workDate, newDayOfWeek, oldDayOfWeek) {
                const base = parseIsoDate(workDate);
                const newDay = parseInt(newDayOfWeek, 10);
                const oldDay = parseInt(oldDayOfWeek, 10);
                if (!base || isNaN(newDay) || isNaN(oldDay)) {
                    return mapApplyDate('', oldDayOfWeek);
                }
                const diff = (oldDay - newDay + 7) % 7;
                const oldDate = new Date(base);
                oldDate.setDate(oldDate.getDate() + diff);
                return formatDateDdMmYyyy(oldDate);
            }

            function normalizeTime(timeValue) {
                if (!timeValue || timeValue === 'null') return '';
                return String(timeValue).length >= 5 ? String(timeValue).substring(0, 5) : String(timeValue);
            }

            function buildShiftText(startTime, endTime, targetShiftId) {
                const start = normalizeTime(startTime);
                const end = normalizeTime(endTime);
                if (start && end) {
                    const hour = parseInt(start.split(':')[0], 10);
                    if (!isNaN(hour)) {
                        return hour < 12 ? 'Sáng' : 'Chiều';
                    }
                    return start + ' - ' + end;
                }
                return '-';
            }

            function viewRequestDetail(btn) {
                const d = btn.dataset;
                const actionType = d.actionType || 'ADD';
                const applyDate = mapApplyDate(d.workDate, d.dayOfWeek);
                const newShiftText = buildShiftText(d.startTime, d.endTime, d.targetShiftId);
                const oldShiftText = buildShiftText(d.oldStartTime, d.oldEndTime, d.targetShiftId);
                const oldApplyDate = mapApplyDate('', d.oldDayOfWeek);

                document.getElementById('mDoctorName').textContent = d.doctorName || '-';
                document.getElementById('mRequestType').textContent = mapRequestType(actionType);
                document.getElementById('mScopeType').textContent = mapScopeType(d.requestType);
                document.getElementById('mRequestedAt').textContent = d.requestedAt || '-';

                const statusNode = document.getElementById('mStatus');
                statusNode.innerHTML = '<span class="status ' + (d.status || 'PENDING') + '">' + mapStatusText(d.status) + '</span>';

                document.getElementById('mReason').textContent = d.reason || '-';
                document.getElementById('mAdminNote').textContent = (d.adminNote && d.adminNote !== 'null') ? d.adminNote : '-';

                const addSection = document.getElementById('mAddSection');
                const updateSection = document.getElementById('mUpdateSection');
                const removeSection = document.getElementById('mRemoveSection');
                addSection.style.display = 'none';
                updateSection.style.display = 'none';
                removeSection.style.display = 'none';

                if (actionType === 'ADD') {
                    addSection.style.display = 'block';
                    document.getElementById('mAddApplyDate').textContent = applyDate;
                    document.getElementById('mAddShift').textContent = newShiftText;
                    document.getElementById('mAddMaxPatients').textContent = (d.maxPatients && d.maxPatients !== 'null') ? d.maxPatients : '-';
                } else if (actionType === 'UPDATE') {
                    updateSection.style.display = 'block';
                    const oldDateView = mapApplyDate(d.oldWorkDate, d.oldDayOfWeek) !== '-'
                        ? mapApplyDate(d.oldWorkDate, d.oldDayOfWeek)
                        : deriveOldApplyDate(d.workDate, d.dayOfWeek, d.oldDayOfWeek);
                    const hasOldShift = oldShiftText && oldShiftText !== '-';
                    const hasOldDate = oldDateView && oldDateView !== '-';

                    document.getElementById('mOldApplyDate').textContent = hasOldDate ? oldDateView : 'Không rõ ngày cũ';
                    document.getElementById('mOldShift').textContent = hasOldShift ? oldShiftText : 'Không rõ ca cũ';
                    document.getElementById('mNewDoctor').textContent = (d.newDoctorName && d.newDoctorName !== 'null') ? d.newDoctorName : (d.doctorName || '-');
                    document.getElementById('mNewApplyDate').textContent = applyDate;
                    document.getElementById('mNewShift').textContent = newShiftText;
                } else {
                    removeSection.style.display = 'block';
                    document.getElementById('mRemoveApplyDate').textContent = applyDate;
                    const removeShiftText = buildShiftText(
                        d.oldStartTime || d.startTime,
                        d.oldEndTime || d.endTime,
                        d.targetShiftId
                    );
                    document.getElementById('mRemoveShift').textContent = removeShiftText;
                }

                document.getElementById('viewRequestModal').style.display = 'block';
            }

            function closeRequestModal() {
                document.getElementById('viewRequestModal').style.display = 'none';
            }

            window.addEventListener('click', function (event) {
                const modal = document.getElementById('viewRequestModal');
                if (event.target === modal) {
                    closeRequestModal();
                }
            });

            document.addEventListener('DOMContentLoaded', function () {
                const alerts = document.querySelectorAll('.alert');
                alerts.forEach(function (alert) {
                    setTimeout(function () {
                        alert.classList.add('fade-out');
                        setTimeout(function () {
                            if (alert && alert.parentNode) {
                                alert.parentNode.removeChild(alert);
                            }
                        }, 300);
                    }, 5000);
                });
            });
        </script>
        <jsp:include page="/common/footer.jsp" />
    </body>
</html>
