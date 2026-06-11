<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Doctor Dashboard</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/examination/doctorDashboard.css">
    </head>
    <body>
        <div class="dashboard-container">
            <jsp:include page="/common/header.jsp" />

            <div class="summary-cards">
                <div class="card summary-card total">
                    <div class="summary-left">
                        <p>Tổng bệnh nhân</p>
                        <h3>${stats.total}</h3>
                    </div>
                    <div class="summary-icon blue">👥</div>
                </div>

                <div class="card summary-card waiting">
                    <div class="summary-left">
                        <p>Đang chờ</p>
                        <h3>${stats.waiting}</h3>
                    </div>
                    <div class="summary-icon yellow">⏰</div>
                </div>

                <div class="card summary-card done">
                    <div class="summary-left">
                        <p>Đã hoàn tất</p>
                        <h3>${stats.done}</h3>
                    </div>
                    <div class="summary-icon green">✅</div>
                </div>

                <div class="card summary-card done">
                    <div class="summary-left">
                        <p>Tỷ lệ hoàn thành</p>
                        <h3><fmt:formatNumber value="${stats.completionRate}" maxFractionDigits="1"/>%</h3>
                    </div>
                    <div class="summary-icon blue">📈</div>
                </div>
            </div>

            <div class="content">
                <div class="queue-section">
                    <div class="section-heading">
                        <h3>Danh sách bệnh nhân hôm nay</h3>
                        <div class="quick-start-wrap">
                            <c:choose>
                                <c:when test="${not empty startAppointmentId}">
                                    <a class="quick-start-btn"
                                       href="${pageContext.request.contextPath}/doctor/exam?appointmentId=${startAppointmentId}">
                                        ▶ Bắt đầu khám bệnh nhân #${startQueuePosition} - ${startPatientName}
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span class="quick-start-btn disabled">Không có bệnh nhân chờ khám</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="queue-filter">
                        <form method="get" id="queueFilterForm">
                            <input type="text" name="keyword"
                                   placeholder="Tìm kiếm bệnh nhân theo tên hoặc mã..."
                                   value="${keyword}"/>

                            <select name="status" onchange="document.getElementById('queueFilterForm').submit()">
                                <option value="active" ${selectedStatus eq 'active' ? 'selected' : ''}>Tất cả (đang chờ + đang khám)</option>
                                <option value="waiting" ${selectedStatus eq 'waiting' ? 'selected' : ''}>Đang chờ</option>
                                <option value="examining" ${selectedStatus eq 'examining' ? 'selected' : ''}>Đang khám</option>
                                <option value="done" ${selectedStatus eq 'done' ? 'selected' : ''}>Hoàn tất</option>
                            </select>
                            <input type="hidden" name="page" value="1"/>
                            <button type="submit">Lọc</button>
                            <a href="${pageContext.request.contextPath}/doctorDashboard?status=active" class="page-btn">Đặt lại</a>
                        </form>
                    </div>

                    <div class="queue-list">
                        <div class="queue-table-header" aria-hidden="true">
                            <span>STT</span>
                            <span>Họ tên</span>
                            <span>Giới tính</span>
                            <span>Ngày sinh</span>
                            <span>Triệu chứng</span>
                            <span>Trạng thái</span>
                        </div>

                        <c:forEach var="q" items="${queueList}">
                            <div class="queue-card queue-row"
                                data-appointment-id="${q.appointmentId}">

                                <span class="queue-col queue-position">#${q.queuePosition}</span>
                                <span class="queue-col queue-name">${q.patientName}</span>
                                <span class="queue-col queue-info">${q.gender}</span>

                                <span class="queue-col queue-dob">${q.dob}</span>
                                <span class="queue-col queue-symptom">${q.symptom}</span>
                                <span class="queue-col queue-state"><span class="queue-status ${q.status}">${q.status}</span></span>

                            </div>
                        </c:forEach>

                        <c:if test="${empty queueList}">
                            <p class="empty-state">Không có bệnh nhân chờ khám</p>
                        </c:if>
                    </div>
                    <c:if test="${totalRecords > 0}">
                        <div class="queue-pagination">
                            <span class="pagination-summary">Trang ${currentPage}/${totalPages} • ${totalRecords} bệnh nhân</span>
                            <div class="pagination-actions">
                                <c:url var="prevPageUrl" value="/doctorDashboard">
                                    <c:param name="status" value="${selectedStatus}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${currentPage - 1}" />
                                </c:url>
                                <c:url var="nextPageUrl" value="/doctorDashboard">
                                    <c:param name="status" value="${selectedStatus}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${currentPage + 1}" />
                                </c:url>

                                <c:choose>
                                    <c:when test="${currentPage > 1}">
                                        <a class="page-btn" href="${prevPageUrl}">← Trước</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-btn disabled">← Trước</span>
                                    </c:otherwise>
                                </c:choose>

                                <c:choose>
                                    <c:when test="${currentPage < totalPages}">
                                        <a class="page-btn" href="${nextPageUrl}">Sau →</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-btn disabled">Sau →</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <jsp:include page="/common/modal-alert.jsp" />
        
        

        <jsp:include page="/common/footer.jsp" />
    </body>
</html>