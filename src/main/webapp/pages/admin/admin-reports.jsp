<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Admin Reports</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                background: #f3f4f6; /* đồng bộ với lab-queue.jsp và system-logs.jsp */
                min-height: 100vh;
                margin: 0;
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
                margin-bottom: 24px;
            }
            .page-title {
                font-size: 26px;
                font-weight: 700;
                color: #111827;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .page-subtitle {
                font-size: 13px;
                color: #6b7280;
                margin-top: 4px;
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
            .grid {
                display: grid;
                grid-template-columns: repeat(3, minmax(0, 1fr));
                gap: 16px;
                margin-bottom: 24px;
            }
            .card {
                background: white;
                border-radius: 14px;
                padding: 16px 18px;
                box-shadow: 0 2px 10px rgba(15, 23, 42, 0.08);
            }
            .card-title {
                font-size: 14px;
                font-weight: 600;
                color: #4b5563;
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 8px;
            }
            .metric-main {
                font-size: 22px;
                font-weight: 700;
                color: #111827;
            }
            .metric-sub {
                font-size: 12px;
                color: #6b7280;
                margin-top: 4px;
            }
            .metric-tags {
                display: flex;
                flex-wrap: wrap;
                gap: 6px;
                margin-top: 10px;
            }
            .tag {
                font-size: 11px;
                padding: 4px 8px;
                border-radius: 999px;
                background: #f3f4f6;
                color: #374151;
            }
            .layout-2 {
                display: grid;
                grid-template-columns: 2fr 1.3fr;
                gap: 18px;
                margin-bottom: 24px;
            }
            .filter-bar {
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 20px;
                background: white;
                border-radius: 12px;
                padding: 14px 18px;
                box-shadow: 0 2px 10px rgba(15, 23, 42, 0.08);
            }
            .filter-bar label {
                font-size: 13px;
                font-weight: 600;
                color: #374151;
                white-space: nowrap;
            }
            .filter-bar select {
                font-size: 13px;
                padding: 7px 10px;
                border: 1px solid #d1d5db;
                border-radius: 8px;
                background: #f9fafb;
                color: #111827;
                outline: none;
                min-width: 220px;
            }
            .filter-bar select:focus {
                border-color: #2563eb;
                background: white;
            }
            .filter-bar button {
                font-size: 13px;
                padding: 7px 16px;
                background: #2563eb;
                color: white;
                border: none;
                border-radius: 8px;
                cursor: pointer;
                font-weight: 500;
            }
            .filter-bar button:hover {
                background: #1d4ed8;
            }
            .filter-bar a.reset-btn {
                font-size: 13px;
                padding: 7px 12px;
                color: #6b7280;
                text-decoration: none;
                border: 1px solid #d1d5db;
                border-radius: 8px;
                background: white;
            }
            .filter-bar a.reset-btn:hover {
                background: #f3f4f6;
            }
            .table-wrapper {
                max-height: 360px;
                overflow: auto;
                border-radius: 12px;
                border: 1px solid #e5e7eb;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                padding: 9px 10px;
                font-size: 13px;
                border-bottom: 1px solid #e5e7eb;
                text-align: left;
            }
            th {
                background: #f9fafb;
                font-size: 12px;
                text-transform: uppercase;
                letter-spacing: 0.04em;
                color: #6b7280;
            }
            tbody tr:hover {
                background: #f9fafb;
            }
        </style>
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />
        <div class="container">
            <div class="page-header">
                <div>
                    <div class="page-title">
                        <i class="fa-solid fa-chart-line"></i>
                        Báo cáo hoạt động phòng khám
                    </div>
                    <div class="page-subtitle">
                        Tổng hợp nhanh lịch hẹn, phiếu xét nghiệm, doanh thu và năng suất làm việc của bác sĩ.
                    </div>
                </div>
               
            </div>

            <div class="grid">
                <div class="card">
                    <div class="card-title">
                        <i class="fa-regular fa-calendar-check"></i>
                        Lịch hẹn
                    </div>
                    <div class="metric-main">
                        <c:out value="${apptTotal}" /> lịch hẹn
                    </div>
                    <div class="metric-sub">
                        Phân bổ theo trạng thái: đặt mới, chờ khám, đang khám, đã khám, đã hủy...
                    </div>
                    <div class="metric-tags">
                        <span class="tag">Đặt lịch: <c:out value="${apptBooked}" /></span>
                        <span class="tag">Check-in: <c:out value="${apptCheckedIn}" /></span>
                        <span class="tag">Chờ khám: <c:out value="${apptWaiting}" /></span>
                        <span class="tag">Hoàn thành: <c:out value="${apptCompleted}" /></span>
                        <span class="tag">Đã hủy: <c:out value="${apptCancelled}" /></span>
                    </div>
                </div>

                <div class="card">
                    <div class="card-title">
                        <i class="fa-solid fa-vials"></i>
                        Phiếu xét nghiệm
                    </div>
                    <div class="metric-main">
                        <c:out value="${labStats[0]}" /> phiếu
                    </div>
                    <div class="metric-sub">
                        Theo dõi số lượng pending / đang xử lý / hoàn thành / hủy.
                    </div>
                    <div class="metric-tags">
                        <span class="tag">Pending: <c:out value="${labStats[1]}" /></span>
                        <span class="tag">Đang xử lý: <c:out value="${labStats[2]}" /></span>
                        <span class="tag">Hoàn thành: <c:out value="${labStats[3]}" /></span>
                        <span class="tag">Đã hủy: <c:out value="${labStats[4]}" /></span>
                    </div>
                </div>

                <div class="card">
                    <div class="card-title">
                        <i class="fa-solid fa-receipt"></i>
                        Thanh toán
                    </div>
                    <div class="metric-main">
                        <c:out value="${paymentSummary[1]}" /> đ đã thu
                    </div>
                    <div class="metric-sub">
                        Tổng số tiền đã thanh toán, đang chờ xử lý.
                    </div>
                    <div class="metric-tags">
                        <span class="tag">Tổng tạo: <c:out value="${paymentSummary[0]}" /> đ</span>
                        <span class="tag">Đã thanh toán: <c:out value="${paymentSummary[1]}" /> đ</span>
                        <span class="tag">Đang chờ: <c:out value="${paymentSummary[2]}" /> đ</span>
                    </div>
                </div>
            </div>

            <form method="get" action="${pageContext.request.contextPath}/admin-reports" class="filter-bar">
                <label for="doctorFilter"><i class="fa-solid fa-user-md"></i> Lọc theo bác sĩ:</label>
                <select id="doctorFilter" name="doctorId">
                    <option value="">-- Tất cả bác sĩ --</option>
                    <c:forEach var="doc" items="${allDoctors}">
                        <option value="${doc.doctorId}" ${selectedDoctorId == doc.doctorId ? 'selected' : ''}>${doc.doctorName}</option>
                    </c:forEach>
                </select>
                <button type="submit"><i class="fa-solid fa-filter"></i> Lọc</button>
                <c:if test="${selectedDoctorId > 0}">
                    <a href="${pageContext.request.contextPath}/admin-reports" class="reset-btn"><i class="fa-solid fa-xmark"></i> Bỏ lọc</a>
                </c:if>
            </form>

            <div class="layout-2">
                <div class="card">
                    <div class="card-title">
                        <i class="fa-solid fa-chart-pie"></i>
                        Biểu đồ trạng thái lịch hẹn
                    </div>
                    <canvas id="appointmentsChart" height="200"></canvas>
                </div>

                <div class="card">
                    <div class="card-title">
                        <i class="fa-solid fa-user-md"></i>
                        <c:choose>
                            <c:when test="${selectedDoctorId > 0}">Năng suất của bác sĩ được chọn</c:when>
                            <c:otherwise>Năng suất làm việc của bác sĩ</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Bác sĩ</th>
                                    <th>Số ca khám hoàn thành</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:if test="${empty doctorProductivity}">
                                    <tr>
                                        <td colspan="2" style="text-align:center; color:#6b7280; padding:16px;">
                                            Chưa có dữ liệu thống kê.
                                        </td>
                                    </tr>
                                </c:if>
                                <c:forEach var="dp" items="${doctorProductivity}">
                                    <tr>
                                        <td>${dp.doctorName}</td>
                                        <td>${dp.totalCompletedAppointments}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <jsp:include page="../../common/footer.jsp" />
        <script>
            const apptData = {
                labels: ['Đặt lịch', 'Check-in', 'Chờ khám', 'Hoàn thành', 'Đã hủy'],
                datasets: [{
                        label: 'Số lịch hẹn',
                        data: [
                            ${apptBooked}, ${apptCheckedIn}, ${apptWaiting}, ${apptCompleted}, ${apptCancelled}
                        ],
                        backgroundColor: [
                            'rgba(59, 130, 246, 0.7)',
                            'rgba(16, 185, 129, 0.7)',
                            'rgba(245, 158, 11, 0.7)',
                            'rgba(34, 197, 94, 0.7)',
                            'rgba(248, 113, 113, 0.7)'
                        ],
                        borderColor: [
                            'rgba(37, 99, 235, 1)',
                            'rgba(5, 150, 105, 1)',
                            'rgba(217, 119, 6, 1)',
                            'rgba(22, 163, 74, 1)',
                            'rgba(239, 68, 68, 1)'
                        ],
                        borderWidth: 1
                    }]
            };

            const ctx = document.getElementById('appointmentsChart').getContext('2d');
            new Chart(ctx, {
                type: 'bar',
                data: apptData,
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: function (context) {
                                    return context.parsed.y + ' lịch';
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
        </script>
    </body>
    </html>

