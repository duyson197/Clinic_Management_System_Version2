<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Kiểm tra thanh toán xét nghiệm</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
  <style>
    :root {
      --primary: #2563eb;
      --primary-soft: #e0edff;
      --bg: #f3f4f6;
      --card: #ffffff;
      --text-main: #111827;
      --text-sub: #6b7280;
      --border: #e5e7eb;
      --danger: #ef4444;
      --success: #16a34a;
      --warning: #f59e0b;
      --radius-lg: 14px;
      --radius-md: 10px;
    }

    * {
      box-sizing: border-box;
      font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }

    body {
      margin: 0;
      padding-top: 60px;
      background: var(--bg);
      color: var(--text-main);
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .main-container {
      display: flex;
      flex: 1;
      margin-top: 0;
    }

    .content-wrapper {
      flex: 1;
      min-height: calc(100vh - 60px);
    }

    .page {
      padding: 20px 24px 32px;
    }

    .card {
      background: var(--card);
      border-radius: var(--radius-lg);
      padding: 16px 18px 18px;
      box-shadow: 0 12px 35px rgba(15, 23, 42, 0.05);
      border: 1px solid rgba(148, 163, 184, 0.18);
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
    }

    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
    }

    .card-title-icon {
      width: 26px;
      height: 26px;
      border-radius: 999px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      background: var(--primary-soft);
      color: var(--primary);
    }

    .card-subtitle {
      font-size: 12px;
      color: var(--text-sub);
    }

    .btn {
      border-radius: 999px;
      border: 1px solid transparent;
      padding: 8px 16px;
      font-size: 13px;
      font-weight: 500;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #fff;
      color: var(--text-main);
      text-decoration: none;
    }

    .btn-primary {
      background: var(--primary);
      color: #fff;
      box-shadow: 0 8px 18px rgba(37, 99, 235, 0.35);
    }

    .btn-outline {
      background: transparent;
      border-color: var(--border);
    }

    .btn-success {
      background: var(--success);
      color: #fff;
      border-color: transparent;
    }

    .btn-success:hover {
      filter: brightness(1.05);
    }

    .filters {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 12px;
    }

    .field-group {
      display: flex;
      flex-direction: column;
      gap: 4px;
      flex: 1 1 130px;
      min-width: 130px;
    }

    .field-label {
      font-size: 11px;
      color: var(--text-sub);
    }

    .select,
    .input {
      border-radius: var(--radius-md);
      border: 1px solid var(--border);
      padding: 7px 10px;
      font-size: 13px;
      outline: none;
      background: #f9fafb;
    }

    .select:focus,
    .input:focus {
      border-color: var(--primary);
      background: #fff;
      box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.12);
    }

    .queue-summary {
      display: flex;
      gap: 10px;
      margin-bottom: 8px;
      flex-wrap: wrap;
    }

    .chip {
      padding: 5px 9px;
      border-radius: 999px;
      font-size: 11px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #f9fafb;
      color: var(--text-sub);
    }

    .chip-dot {
      width: 8px;
      height: 8px;
      border-radius: 999px;
    }

    .dot-all {
      background: var(--primary);
    }

    .dot-pending {
      background: var(--warning);
    }

    .dot-paid {
      background: var(--success);
    }

    .table-wrapper {
      border-radius: 12px;
      border: 1px solid var(--border);
      overflow: hidden;
      background: #f9fafb;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;
    }

    thead {
      background: #eef2ff;
      color: #4b5563;
    }

    th,
    td {
      padding: 8px 10px;
      text-align: left;
      border-bottom: 1px solid #e5e7eb;
    }

    th {
      font-weight: 600;
      font-size: 12px;
      white-space: nowrap;
    }

    tbody tr:nth-child(even) {
      background: #fdfdfd;
    }

    tbody tr:hover {
      background: #e0edff;
    }

    .status-pill {
      padding: 4px 8px;
      border-radius: 999px;
      font-size: 11px;
      display: inline-flex;
      align-items: center;
      gap: 5px;
    }

    .status-pending {
      background: #fef3c7;
      color: #92400e;
    }

    .status-paid {
      background: #dcfce7;
      color: #166534;
    }

    .text-muted {
      color: var(--text-sub);
      font-size: 12px;
    }

    .text-right {
      text-align: right;
    }

    .amount {
      font-weight: 600;
      color: var(--primary);
    }

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 8px;
    }
  </style>
</head>
<body>
  <jsp:include page="../../common/header.jsp" />
  <jsp:include page="../../common/modal-alert.jsp" />

  <div class="main-container">
    <div class="content-wrapper">
      <div class="page" style="max-width: 1280px; margin: 0 auto;">

        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">
                <span class="card-title-icon"><i class="fas fa-cash-register"></i></span>
                <span>Danh sách chờ thanh toán xét nghiệm</span>
              </div>
              <div class="card-subtitle">
                Xác nhận thanh toán cho các phiếu xét nghiệm đang chờ xác nhận thanh toán
              </div>
            </div>
            <div class="text-right text-muted">
              Tổng: <strong id="totalCount">${totalRecords}</strong> phiếu
              <c:if test="${totalPages > 1}">
                <span style="margin-left: 10px;">(Trang ${currentPage}/${totalPages})</span>
              </c:if>
            </div>
          </div>


          <!-- TÌM KIẾM -->
          <form method="GET" action="${pageContext.request.contextPath}/lab-payment" id="filterForm">
            <input type="hidden" name="page" value="1" />
            <div class="filters">
              <div class="field-group" style="flex: 2 1 220px;">
                <label class="field-label" for="searchInput">Tìm theo tên BN / Mã BN / Mã phiếu</label>
                <input class="input" name="search" id="searchInput" placeholder="Nhập từ khóa tìm kiếm..." value="${searchTerm}" />
              </div>
              <div class="field-group" style="flex: 0 0 auto; align-self: flex-end;">
                <button type="button" id="clearFiltersBtn" class="btn btn-outline" style="white-space: nowrap;">
                  <i class="fas fa-times-circle"></i> Xóa bộ lọc
                </button>
              </div>
            </div>
          </form>

          <!-- TÓM TẮT -->
          <div class="queue-summary">
            <div class="chip">
              <span class="chip-dot dot-all"></span>
              <span><strong>${stats[0]}</strong> phiếu</span>
            </div>
            <div class="chip">
              <span class="chip-dot dot-pending"></span>
              <span>Chờ thanh toán: <strong>${stats[1]}</strong></span>
            </div>
            <div class="chip">
              <span class="chip-dot dot-paid"></span>
              <span>Đã thanh toán: <strong>${stats[2]}</strong></span>
            </div>
          </div>

          <!-- BẢNG DỮ LIỆU -->
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Mã phiếu</th>
                  <th>Bệnh nhân</th>
                  <th>Tuổi / Giới</th>
                  <th>Khoa gửi</th>
                  <th>Số tiền</th>
                  <th>Trạng thái</th>
                  <th>Thời gian</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody id="paymentTableBody">
                <c:choose>
                  <c:when test="${empty payments}">
                    <tr>
                      <td colspan="8" style="text-align: center; padding: 40px; color: var(--text-sub);">
                        Không có dữ liệu
                      </td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <c:forEach var="payment" items="${payments}">
                      <fmt:formatDate value="${payment.labRequest.createdAt}" pattern="yyyy" var="year" />
                      <c:set var="requestCode" value="LAB-${year}-${payment.labRequest.requestId}" />
                      <c:set var="patientCode" value="BN${payment.labRequest.patient.patientId}" />
                      <c:set var="age" value="-" />
                      <c:if test="${payment.labRequest.patient.dob != null}">
                        <jsp:useBean id="now" class="java.util.Date" />
                        <fmt:formatDate value="${payment.labRequest.patient.dob}" pattern="yyyy" var="birthYear" />
                        <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear" />
                        <c:set var="age" value="${currentYear - birthYear}" />
                      </c:if>
                      <c:set var="genderText" value="${payment.labRequest.patient.gender == 'male' ? 'Nam' : (payment.labRequest.patient.gender == 'female' ? 'Nữ' : 'Khác')}" />
                      <c:set var="statusText" value="${payment.status == 'pending' ? 'Chờ thanh toán' : 'Đã thanh toán'}" />
                      <c:set var="statusClass" value="${payment.status == 'pending' ? 'status-pending' : 'status-paid'}" />
                      <tr data-payment-id="${payment.paymentId}" data-request-id="${payment.labRequest.requestId}">
                        <td>${requestCode}</td>
                        <td>
                          ${payment.labRequest.patient.fullName}<br />
                          <span class="text-muted">${patientCode}</span>
                        </td>
                        <td>${age} / ${genderText}</td>
                        <td>${payment.labRequest.doctor.specialization}</td>
                        <td class="amount">
                          <fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VNĐ
                        </td>
                        <td>
                          <span class="status-pill ${statusClass}">
                            <c:choose>
                              <c:when test="${payment.status == 'pending'}">
                                <i class="fas fa-clock"></i>
                              </c:when>
                              <c:otherwise>
                                <i class="fas fa-check-circle"></i>
                              </c:otherwise>
                            </c:choose>
                            ${statusText}
                          </span>
                        </td>
                        <td>
                          <fmt:formatDate value="${payment.labRequest.createdAt}" pattern="HH:mm dd/MM/yyyy" timeZone="UTC" />
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${payment.status == 'pending'}">
                              <button class="btn btn-success" onclick="confirmPayment(${payment.paymentId}, '${payment.labRequest.patient.fullName}', ${payment.amount});" style="font-size: 12px; padding: 6px 12px;">
                                <i class="fas fa-check"></i> Xác nhận
                              </button>
                            </c:when>
                            <c:otherwise>
                              <span class="text-muted" style="font-size: 12px;">Đã hoàn thành</span>
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

          <!-- PHÂN TRANG -->
          <c:if test="${totalPages > 1}">
            <div class="pagination-wrapper">
              <c:set var="baseUrl" value="${pageContext.request.contextPath}/lab-payment" />
              <c:set var="queryParams" value="" />
              <c:if test="${not empty filterStatus}">
                <c:set var="queryParams" value="${queryParams}status=${filterStatus}&" />
              </c:if>
              <c:if test="${not empty searchTerm}">
                <c:set var="queryParams" value="${queryParams}search=${searchTerm}&" />
              </c:if>

              <!-- Nút Trước -->
              <c:if test="${currentPage > 1}">
                <a href="${baseUrl}?${queryParams}page=${currentPage - 1}" class="btn btn-outline" style="text-decoration: none;">
                  ‹ Trước
                </a>
              </c:if>
              <c:if test="${currentPage <= 1}">
                <span class="btn btn-outline" style="opacity: 0.5; cursor: not-allowed;">‹ Trước</span>
              </c:if>

              <!-- Số trang -->
              <c:choose>
                <c:when test="${totalPages <= 7}">
                  <c:forEach var="i" begin="1" end="${totalPages}">
                    <c:choose>
                      <c:when test="${i == currentPage}">
                        <span class="btn btn-primary" style="min-width: 36px;">${i}</span>
                      </c:when>
                      <c:otherwise>
                        <a href="${baseUrl}?${queryParams}page=${i}" class="btn btn-outline" style="text-decoration: none; min-width: 36px;">${i}</a>
                      </c:otherwise>
                    </c:choose>
                  </c:forEach>
                </c:when>
                <c:otherwise>
                  <c:if test="${currentPage > 3}">
                    <a href="${baseUrl}?${queryParams}page=1" class="btn btn-outline" style="text-decoration: none; min-width: 36px;">1</a>
                    <c:if test="${currentPage > 4}">
                      <span style="padding: 8px 4px;">...</span>
                    </c:if>
                  </c:if>

                  <c:forEach var="i" begin="${currentPage > 3 ? currentPage - 1 : 1}" end="${currentPage < totalPages - 2 ? currentPage + 1 : totalPages}">
                    <c:choose>
                      <c:when test="${i == currentPage}">
                        <span class="btn btn-primary" style="min-width: 36px;">${i}</span>
                      </c:when>
                      <c:otherwise>
                        <a href="${baseUrl}?${queryParams}page=${i}" class="btn btn-outline" style="text-decoration: none; min-width: 36px;">${i}</a>
                      </c:otherwise>
                    </c:choose>
                  </c:forEach>

                  <c:if test="${currentPage < totalPages - 2}">
                    <c:if test="${currentPage < totalPages - 3}">
                      <span style="padding: 8px 4px;">...</span>
                    </c:if>
                    <a href="${baseUrl}?${queryParams}page=${totalPages}" class="btn btn-outline" style="text-decoration: none; min-width: 36px;">${totalPages}</a>
                  </c:if>
                </c:otherwise>
              </c:choose>

              <!-- Nút Sau -->
              <c:if test="${currentPage < totalPages}">
                <a href="${baseUrl}?${queryParams}page=${currentPage + 1}" class="btn btn-outline" style="text-decoration: none;">
                  Sau ›
                </a>
              </c:if>
              <c:if test="${currentPage >= totalPages}">
                <span class="btn btn-outline" style="opacity: 0.5; cursor: not-allowed;">Sau ›</span>
              </c:if>
            </div>
          </c:if>
        </div>
      </div>
    </div>
  </div>

  <script>
    // Xác nhận thanh toán
    function confirmPayment(paymentId, patientName, amount) {
      var formattedAmount = new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
      showConfirm('Xác nhận thanh toán ' + formattedAmount + ' cho bệnh nhân ' + patientName + '?', function() {
        fetch('${pageContext.request.contextPath}/lab-payment', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: 'action=confirmPayment&paymentId=' + paymentId
        })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            showAlert('Đã xác nhận thanh toán thành công!', 'success', function() { location.reload(); });
          } else {
            showAlert('Xác nhận thất bại: ' + (data.message || 'Lỗi không xác định'), 'error');
          }
        })
        .catch(error => {
          console.error('Error:', error);
          showAlert('Đã xảy ra lỗi khi xác nhận thanh toán', 'error');
        });
      });
    }

    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        document.getElementById('filterForm').submit();
      }
    });

    searchInput.addEventListener('input', (e) => {
      if (!e.target.value.trim()) {
        document.getElementById('filterForm').submit();
      }
    });

    document.getElementById('clearFiltersBtn').addEventListener('click', () => {
      document.getElementById('searchInput').value = '';
      window.location.href = '${pageContext.request.contextPath}/lab-payment';
    });

  </script>

  <jsp:include page="../../common/footer.jsp" />
</body>
</html>
