<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Gửi Kết Quả Xét Nghiệm - Phòng Khám Đa Liễu</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
  <style>
    :root {
      --primary: #2563eb;
      --primary-hover: #1d4ed8;
      --success: #10b981;
      --success-hover: #059669;
      --danger: #ef4444;
      --warning: #f59e0b;
      --text-main: #1f2937;
      --text-sub: #6b7280;
      --border: #e5e7eb;
      --bg-light: #f9fafb;
      --sidebar-width: 260px;
    }

    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
      background: var(--bg-light);
      color: var(--text-main);
      padding-top: 60px;
    }

    .main-container {
      display: flex;
      min-height: calc(100vh - 60px);
    }

    .content-wrapper {
      flex: 1;
      padding: 24px;
      width: 100%;
      max-width: 100%;
    }

    .page-header {
      margin-bottom: 24px;
      max-width: 1200px;
      margin-left: auto;
      margin-right: auto;
    }

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-main);
      margin-bottom: 8px;
    }

    .page-subtitle {
      font-size: 14px;
      color: var(--text-sub);
    }

    .card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      max-width: 1200px;
      margin-left: auto;
      margin-right: auto;
    }

    .card-header {
      padding: 20px 24px;
      border-bottom: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-main);
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .card-body {
      padding: 24px;
    }

    .section {
      margin-bottom: 24px;
    }

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-main);
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 2px solid var(--border);
    }

    .detail-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 16px;
    }

    .detail-label {
      font-size: 12px;
      font-weight: 500;
      color: var(--text-sub);
      margin-bottom: 4px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .detail-value {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-main);
    }

    .status-pill {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
    }

    .status-pending { background: #fef3c7; color: #92400e; }
    .status-inprogress { background: #dbeafe; color: #1e40af; }
    .status-done { background: #d1fae5; color: #065f46; }

    .form-group {
      margin-bottom: 20px;
    }

    .form-label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 6px;
      color: var(--text-main);
    }

    .form-control {
      width: 100%;
      padding: 10px 12px;
      border: 1px solid var(--border);
      border-radius: 8px;
      font-size: 14px;
      font-family: inherit;
      transition: border-color 0.2s;
    }

    .form-control:focus {
      outline: none;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
    }

    textarea.form-control {
      min-height: 120px;
      resize: vertical;
    }

    .form-help {
      display: block;
      margin-top: 4px;
      font-size: 11px;
      color: var(--text-sub);
    }

    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }

    .btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-primary {
      background: var(--primary);
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background: var(--primary-hover);
    }

    .btn-success {
      background: var(--success);
      color: white;
    }

    .btn-success:hover:not(:disabled) {
      background: var(--success-hover);
    }

    .btn-outline {
      background: white;
      color: var(--text-main);
      border: 1px solid var(--border);
    }

    .btn-outline:hover:not(:disabled) {
      background: var(--bg-light);
    }

    .form-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      padding-top: 20px;
      border-top: 1px solid var(--border);
    }

    .alert {
      padding: 12px 16px;
      border-radius: 8px;
      margin-bottom: 20px;
      font-size: 14px;
    }

    .alert-warning {
      background: #fef3c7;
      color: #92400e;
      border: 1px solid #fde68a;
    }

    .alert-info {
      background: #dbeafe;
      color: #1e40af;
      border: 1px solid #bfdbfe;
    }

    @media (max-width: 768px) {
      .content-wrapper {
        margin-left: 0;
        padding: 16px;
      }

      .detail-row {
        grid-template-columns: 1fr;
        gap: 12px;
      }

      .form-actions {
        flex-direction: column;
      }

      .btn {
        width: 100%;
        justify-content: center;
      }
    }
  </style>
</head>
<body>
  <jsp:include page="../../common/header.jsp" />
  <jsp:include page="../../common/modal-alert.jsp" />

  <div class="main-container">
    <div class="content-wrapper">
      <div class="page-header">
        <h1 class="page-title">
          <i class="fas fa-paper-plane"></i> Gửi Kết Quả Xét Nghiệm
        </h1>
        <p class="page-subtitle">
          Hoàn tất xét nghiệm và gửi kết quả cho bác sĩ
        </p>
      </div>

      <c:if test="${empty labRequest}">
        <div class="alert alert-warning">
          <i class="fas fa-exclamation-triangle"></i>
          Không tìm thấy phiếu xét nghiệm. Vui lòng chọn phiếu từ danh sách.
        </div>
        <a href="${pageContext.request.contextPath}/lab-queue" class="btn btn-outline">
          <i class="fas fa-arrow-left"></i> Quay lại danh sách
        </a>
      </c:if>

      <c:if test="${not empty labRequest}">
        <!-- THÔNG TIN BỆNH NHÂN -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">
              <i class="fas fa-user"></i>
              <span>Thông tin bệnh nhân</span>
            </div>
            <div style="text-align: right;">
              <div style="font-size: 11px; color: var(--text-sub);">Mã phiếu</div>
              <strong style="color: var(--primary);">LAB-${labRequest.createdAt.year}-${String.format('%04d', labRequest.requestId)}</strong>
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
                <div class="detail-value">BN${String.format('%06d', labRequest.patient.patientId)}</div>
              </div>
            </div>

            <div class="detail-row">
              <div>
                <div class="detail-label">Tuổi / Giới tính</div>
                <c:set var="age" value="${labRequest.createdAt.year - labRequest.patient.dob.year}" />
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
                <div class="detail-label">Khoa gửi</div>
                <div class="detail-value">${labRequest.doctor.specialization}</div>
              </div>
              <div>
                <div class="detail-label">Bác sĩ chỉ định</div>
                <div class="detail-value">${labRequest.doctor.fullName}</div>
              </div>
            </div>

            <div style="width: 100%;">
              <div class="detail-label">Triệu chứng / Chỉ định xét nghiệm</div>
              <div class="detail-value" style="margin-top: 4px;">
                ${labRequest.appointment.symptom != null ? labRequest.appointment.symptom : 'Không có'}
              </div>
            </div>

            <div class="detail-row" style="margin-top: 16px;">
              <div>
                <div class="detail-label">Trạng thái hiện tại</div>
                <c:set var="statusText" value="${labRequest.status == 'pending' ? 'Chờ lấy mẫu' : (labRequest.status == 'processing' ? 'Đang xét nghiệm' : 'Đã có kết quả')}" />
                <c:set var="statusClass" value="${labRequest.status == 'pending' ? 'status-pending' : (labRequest.status == 'processing' ? 'status-inprogress' : 'status-done')}" />
                <div class="status-pill ${statusClass}">● ${statusText}</div>
              </div>
              <div>
                <div class="detail-label">Thời gian chỉ định</div>
                <div class="detail-value">
                  <fmt:formatDate value="${labRequest.createdAt}" pattern="HH:mm - dd/MM/yyyy" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- FORM GỬI KẾT QUẢ -->
        <div class="card" style="margin-top: 24px;">
          <div class="card-header">
            <div class="card-title">
              <i class="fas fa-file-medical"></i>
              <span>Kết quả xét nghiệm</span>
            </div>
          </div>

          <div class="card-body">
            <c:if test="${labRequest.status != 'processing'}">
              <div class="alert alert-warning">
                <i class="fas fa-exclamation-triangle"></i>
                Chỉ có thể gửi kết quả khi phiếu đang ở trạng thái "Đang xét nghiệm".
              </div>
            </c:if>

            <form id="sendResultForm" method="POST" action="${pageContext.request.contextPath}/lab-queue" enctype="multipart/form-data">
              <input type="hidden" name="action" value="sendResult" />
              <input type="hidden" name="requestId" value="${labRequest.requestId}" />

              <div class="form-group">
                <label class="form-label" for="resultFile">
                  <i class="fas fa-upload"></i> Tải lên file kết quả <span style="color: var(--danger);">*</span>
                </label>
                <input 
                  type="file" 
                  id="resultFile" 
                  name="resultFile" 
                  class="form-control"
                  accept=".pdf,.jpg,.jpeg,.png,.gif,.webp,.bmp,.doc,.docx,.xls,.xlsx"
                  required
                  ${labRequest.status != 'processing' ? 'disabled' : ''}
                />
                <small class="form-help">
                  <i class="fas fa-info-circle"></i> Định dạng cho phép: PDF, ảnh (JPG, PNG, GIF, WebP, BMP), tài liệu (DOC, DOCX, XLS, XLSX). <strong>Giới hạn dung lượng: 10MB</strong>.
                </small>
              </div>

              <div class="form-group">
                <label class="form-label" for="notes">
                  <i class="fas fa-comment-medical"></i> Ghi chú / Nhận xét của kỹ thuật viên
                </label>
                <textarea 
                  id="notes" 
                  name="notes" 
                  class="form-control"
                  placeholder="Ví dụ: Kết quả trong giới hạn bình thường, không phát hiện bất thường..."
                  ${labRequest.status != 'processing' ? 'disabled' : ''}
                >${labRequest.notes != null ? labRequest.notes : ''}</textarea>
                <small class="form-help">
                  <i class="fas fa-info-circle"></i> Ghi chú sẽ được gửi kèm kết quả cho bác sĩ
                </small>
              </div>

              <div class="alert alert-info">
                <i class="fas fa-info-circle"></i>
                <strong>Lưu ý:</strong> Sau khi gửi kết quả, bệnh nhân sẽ tự động được chuyển về danh sách chờ khám để bác sĩ xem kết quả và tư vấn.
              </div>

              <div class="form-actions">
                <a href="${pageContext.request.contextPath}/lab-queue" class="btn btn-outline">
                  <i class="fas fa-times"></i> Hủy
                </a>
                <button 
                  type="submit" 
                  class="btn btn-success"
                  ${labRequest.status != 'processing' ? 'disabled' : ''}
                >
                  <i class="fas fa-paper-plane"></i> Gửi kết quả xét nghiệm
                </button>
              </div>
            </form>
          </div>
        </div>
      </c:if>
    </div>
  </div>

  <jsp:include page="../../common/footer.jsp" />

  <script>
    (function() {
      var MAX_FILE_SIZE_MB = 10;
      var MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
      var ALLOWED_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'doc', 'docx', 'xls', 'xlsx'];

      function getFileExtension(fileName) {
        var i = fileName.lastIndexOf('.');
        return i >= 0 ? fileName.substring(i + 1).toLowerCase() : '';
      }

      function isAllowedFile(file) {
        var ext = getFileExtension(file.name);
        return ALLOWED_EXTENSIONS.indexOf(ext) !== -1;
      }

      document.getElementById('sendResultForm')?.addEventListener('submit', function(e) {
        e.preventDefault();

        var fileInput = document.getElementById('resultFile');
        if (!fileInput.files || fileInput.files.length === 0) {
          showAlert('Vui lòng chọn file kết quả xét nghiệm!', 'warning');
          return;
        }

        var file = fileInput.files[0];
        if (file.size > MAX_FILE_SIZE_BYTES) {
          showAlert('File quá lớn! Dung lượng tối đa là ' + MAX_FILE_SIZE_MB + 'MB. File của bạn: ' + (file.size / 1024 / 1024).toFixed(2) + 'MB.', 'warning');
          return;
        }
        if (!isAllowedFile(file)) {
          showAlert('Định dạng file không được phép. Chỉ chấp nhận: ' + ALLOWED_EXTENSIONS.join(', ') + '.', 'warning');
          return;
        }

      showConfirm(
        'Xác nhận gửi kết quả xét nghiệm?\n\nBệnh nhân sẽ được chuyển về danh sách chờ khám.',
        function() {
          const form = document.getElementById('sendResultForm');
          const formData = new FormData(form);
          const submitBtn = form.querySelector('button[type="submit"]');
          submitBtn.disabled = true;
          submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';

          fetch('${pageContext.request.contextPath}/lab-queue', {
            method: 'POST',
            body: formData
          })
          .then(res => res.json())
          .then(data => {
            if (data.success) {
              showAlert('Gửi kết quả thành công!', 'success', function() {
                window.location.href = '${pageContext.request.contextPath}/lab-queue';
              });
            } else {
              showAlert(data.message || 'Gửi kết quả thất bại.', 'error');
              submitBtn.disabled = false;
              submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Gửi kết quả xét nghiệm';
            }
          })
          .catch(err => {
            showAlert('Có lỗi xảy ra: ' + err.message, 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Gửi kết quả xét nghiệm';
          });
        }
      );
    });
    })();
  </script>
</body>
</html>
