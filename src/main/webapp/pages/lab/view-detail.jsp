<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Chi tiết phiếu xét nghiệm - Phòng Khám Đa Liễu</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
  <style>
    :root {
      --primary: #2563eb;
      --success: #10b981;
      --danger: #ef4444;
      --warning: #f59e0b;
      --text-main: #1f2937;
      --text-sub: #6b7280;
      --border: #e5e7eb;
      --bg-light: #f9fafb;
      --sidebar-width: 260px;
    }
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: system-ui, sans-serif; background: var(--bg-light); color: var(--text-main); padding-top: 60px; }
    .main-container { display: flex; justify-content: center; min-height: calc(100vh - 60px); }
    .content-wrapper { flex: 1; padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header { margin-bottom: 24px; }
    .page-title { font-size: 24px; font-weight: 600; margin-bottom: 8px; }
    .page-subtitle { font-size: 14px; color: var(--text-sub); }
    .card { background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; margin-bottom: 24px; }
    .card-header { padding: 20px 24px; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }
    .card-title { font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
    .card-body { padding: 24px; }
    .detail-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 16px; }
    .detail-label { font-size: 12px; font-weight: 500; color: var(--text-sub); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px; }
    .detail-value { font-size: 14px; font-weight: 500; color: var(--text-main); }
    .status-pill { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500; }
    .status-pending { background: #fef3c7; color: #92400e; }
    .status-inprogress { background: #dbeafe; color: #1e40af; }
    .status-done { background: #d1fae5; color: #065f46; }
    .status-cancelled { background: #f3f4f6; color: #6b7280; }
    .btn { padding: 10px 20px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; text-decoration: none; }
    .btn-outline { background: white; color: var(--text-main); border: 1px solid var(--border); }
    .btn-outline:hover { background: var(--bg-light); }
    .result-file-link { display: inline-flex; align-items: center; gap: 8px; padding: 12px 16px; background: #ecfdf5; color: #065f46; border-radius: 8px; text-decoration: none; font-weight: 500; margin-top: 8px; }
    .result-file-link:hover { background: #d1fae5; }
    @media (max-width: 768px) { .content-wrapper { margin-left: 0; } .detail-row { grid-template-columns: 1fr; } }
  </style>
</head>
<body>
  <jsp:include page="../../common/header.jsp" />

  <div class="main-container">
    <div class="content-wrapper">
      <div class="page-header">
        <h1 class="page-title"><i class="fas fa-file-medical"></i> Chi tiết phiếu xét nghiệm</h1>
        <p class="page-subtitle">Thông tin phiếu và kết quả (nếu có)</p>
      </div>

      <c:if test="${empty labRequest}">
        <div class="card">
          <div class="card-body">
            <p style="color: var(--text-sub);">Không tìm thấy phiếu xét nghiệm.</p>
            <a href="${pageContext.request.contextPath}/lab-queue" class="btn btn-outline" style="margin-top: 16px;">
              <i class="fas fa-arrow-left"></i> Quay lại danh sách
            </a>
          </div>
        </div>
      </c:if>

      <c:if test="${not empty labRequest}">
        <div class="card">
          <div class="card-header">
            <div class="card-title"><i class="fas fa-user"></i> Thông tin bệnh nhân</div>
            <div style="text-align: right;">
              <div style="font-size: 11px; color: var(--text-sub);">Mã phiếu</div>
              <fmt:formatDate value="${labRequest.createdAt}" pattern="yyyy" var="labYear" />
            <fmt:formatNumber value="${labRequest.requestId}" pattern="0000" var="labIdFmt" />
            <strong style="color: var(--primary);">LAB-${labYear}-${labIdFmt}</strong>
            </div>
          </div>
          <div class="card-body">
            <div class="detail-row">
              <div>
                <div class="detail-label">Họ tên</div>
                <div class="detail-value">${labRequest.patient.fullName}</div>
              </div>
              <div>
                <div class="detail-label">Mã BN</div>
                <fmt:formatNumber value="${labRequest.patient.patientId}" pattern="000000" var="patientIdFmt" />
                <div class="detail-value">BN${patientIdFmt}</div>
              </div>
            </div>
            <div class="detail-row">
              <div>
                <div class="detail-label">Tuổi / Giới tính</div>
                <c:set var="age" value="${labRequest.patient.dob != null ? labRequest.createdAt.year - labRequest.patient.dob.year : '-'}" />
                <c:set var="genderText" value="${labRequest.patient.gender == 'male' ? 'Nam' : (labRequest.patient.gender == 'female' ? 'Nữ' : 'Khác')}" />
                <div class="detail-value">${age} tuổi, ${genderText}</div>
              </div>
              <div>
                <div class="detail-label">Số điện thoại</div>
                <div class="detail-value">${labRequest.patient.phone}</div>
              </div>
            </div>
            <div class="detail-row">
              <div>
                <div class="detail-label">Khoa gửi / Bác sĩ chỉ định</div>
                <div class="detail-value">${labRequest.doctor.specialization} - ${labRequest.doctor.fullName}</div>
              </div>
              <div>
                <div class="detail-label">Trạng thái</div>
                <c:set var="statusText" value="${labRequest.status == 'pending' ? 'Chờ lấy mẫu' : (labRequest.status == 'processing' ? 'Đang xét nghiệm' : (labRequest.status == 'cancelled' ? 'Đã hủy' : 'Đã có kết quả'))}" />
                <c:set var="statusClass" value="${labRequest.status == 'pending' ? 'status-pending' : (labRequest.status == 'processing' ? 'status-inprogress' : (labRequest.status == 'cancelled' ? 'status-cancelled' : 'status-done'))}" />
                <span class="status-pill ${statusClass}">● ${statusText}</span>
              </div>
            </div>
            <div style="width: 100%;">
              <div class="detail-label">Triệu chứng / Chỉ định xét nghiệm</div>
              <div class="detail-value" style="margin-top: 4px;">${labRequest.appointment.symptom != null ? labRequest.appointment.symptom : 'Không có'}</div>
            </div>
          </div>
        </div>

        <c:if test="${not empty labResult && not empty labResult.resultFile}">
          <div class="card">
            <div class="card-header">
              <div class="card-title"><i class="fas fa-file-pdf"></i> Kết quả xét nghiệm</div>
            </div>
            <div class="card-body">
              <div class="detail-row">
                <div>
                  <div class="detail-label">File kết quả</div>
                  <a href="${pageContext.request.contextPath}/${labResult.resultFile}" target="_blank" class="result-file-link">
                    <i class="fas fa-download"></i> Xem / Tải file kết quả
                  </a>
                </div>
                <c:if test="${labResult.completedAt != null}">
                  <div>
                    <div class="detail-label">Thời gian hoàn thành</div>
                    <div class="detail-value"><fmt:formatDate value="${labResult.completedAt}" pattern="HH:mm - dd/MM/yyyy" timeZone="Asia/Ho_Chi_Minh" /></div>
                  </div>
                </c:if>
              </div>
              <c:if test="${not empty labResult.notes}">
                <div style="margin-top: 16px;">
                  <div class="detail-label">Ghi chú kỹ thuật viên</div>
                  <div class="detail-value" style="margin-top: 4px;">${labResult.notes}</div>
                </div>
              </c:if>
            </div>
          </div>
        </c:if>

        <div style="display: flex; gap: 12px; flex-wrap: wrap;">
          <a href="${pageContext.request.contextPath}/lab-queue" class="btn btn-outline">
            <i class="fas fa-arrow-left"></i> Quay lại danh sách
          </a>
          <c:if test="${labRequest.status == 'processing'}">
            <a href="${pageContext.request.contextPath}/lab-queue?action=viewSendResult&requestId=${labRequest.requestId}" class="btn" style="background: var(--primary); color: white;">
              <i class="fas fa-paper-plane"></i> Gửi / Cập nhật kết quả
            </a>
          </c:if>
        </div>
      </c:if>
    </div>
  </div>

  <jsp:include page="../../common/footer.jsp" />
</body>
</html>
