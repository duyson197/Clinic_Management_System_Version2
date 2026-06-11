<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý nhân viên</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; background: linear-gradient(135deg,#f5f7fa 0%,#c3cfe2 100%); min-height: 100vh; }
        .container { padding: 30px 50px; max-width: 1400px; margin: 0 auto; }
        .alert { padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; animation: slideIn .3s ease-out; }
        .alert.fade-out { animation: slideIn .3s ease-out reverse forwards; }
        .alert.success { background:#e8f5e9; color:#2e7d32; border-left:4px solid #4caf50; }
        .alert.error { background:#ffebee; color:#c62828; border-left:4px solid #f44336; }
        .table-container { background: #fff; padding: 25px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,.1); overflow-x: auto; }
        .table-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
        .toolbar { background:#fff; padding:20px; border-radius:10px; margin-bottom:20px; display:grid; grid-template-columns:minmax(340px,1.9fr) minmax(220px,1fr) minmax(220px,1fr) auto; gap:12px; align-items:end; box-shadow:0 2px 10px rgba(0,0,0,.1); }
        .search-box label, .filter-box label, .form-group label { display:block; font-weight:600; margin-bottom:8px; color:#333; font-size:13px; }
        .search-box input, .filter-box select, .form-group input, .form-group select { width:100%; padding:10px 15px; border:1px solid #ddd; border-radius:6px; font-size:14px; }
        .toolbar-buttons, .action-buttons, .modal-footer, .input-action-row { display:flex; gap:10px; }
        .modal-footer { justify-content:flex-end; margin-top:24px; padding-top:16px; border-top:1px solid #eef1f5; }
        .btn-search,.btn-reset,.btn-add,.btn-cancel,.btn-submit,.btn-inline { padding:10px 16px; border:none; border-radius:6px; cursor:pointer; font-weight:600; font-size:14px; display:inline-flex; align-items:center; gap:6px; text-decoration:none; }
        .btn-search,.btn-submit { background:#0061ff; color:#fff; }
        .btn-reset,.btn-cancel { background:#f0f0f0; color:#333; }
        .btn-add { background:#4caf50; color:#fff; }
        .btn-inline { background:#f59e0b; color:#fff; min-height:42px; }
        table { width:100%; border-collapse:collapse; }
        th { background: linear-gradient(135deg,#f8f9fa 0%,#f0f0f0 100%); padding:15px; text-align:left; font-weight:600; color:#333; border-bottom:2px solid #e0e0e0; }
        td { padding:15px; border-bottom:1px solid #f0f0f0; color:#555; vertical-align:top; }
        .stack { display:flex; flex-direction:column; gap:4px; }
        .muted { color:#6b7280; font-size:13px; }
        .btn-action { border:none; background:none; cursor:pointer; font-size:14px; padding:6px 10px; border-radius:4px; display:inline-flex; align-items:center; text-decoration:none; }
        .btn-edit { color:#FB923C; }
        .btn-calendar { color:#5b21b6; }
        .no-data { text-align:center; padding:30px; color:#999; }
        .modal { display:none; position:fixed; z-index:1000; inset:0; background:rgba(0,0,0,.5); overflow-y:auto; }
        .modal-content { background:#fff; margin:5% auto; padding:30px; border-radius:10px; width:90%; max-width:760px; box-shadow:0 10px 40px rgba(0,0,0,.3); }
        .modal-header { font-size:20px; font-weight:600; color:#0061ff; margin-bottom:20px; display:flex; align-items:center; gap:10px; border-bottom:2px solid #f0f0f0; padding-bottom:14px; }
        .modal-close { margin-left:auto; cursor:pointer; font-size:24px; border:none; background:none; color:#999; }
        .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:14px; }
        .form-full { grid-column:1/-1; }
        .readonly-field { background:#f3f4f6; color:#6b7280; cursor:not-allowed; }
        .field-error { margin-top:6px; color:#dc3545; font-size:13px; font-weight:600; }
        .field-input-error { border-color:#dc3545 !important; }
        .doctor-only { display:none; }
        .pagination-wrapper { margin-top:16px; display:flex; justify-self:center; gap:8px; flex-wrap:wrap; }
        .page-link { min-width:34px; padding:8px 12px; border:1px solid #dcdcdc; border-radius:6px; background:#fff; color:#333; text-decoration:none; font-weight:600; text-align:center; }
        .page-link:hover { background:#f5f5f5; }
        .page-link.active { background:#0061ff; color:#fff; border-color:#0061ff; pointer-events:none; }
        .page-link.disabled { opacity:.5; pointer-events:none; }
        @keyframes slideIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        @media (max-width: 992px) { .container{padding:20px;} .toolbar,.form-grid{grid-template-columns:1fr;} .input-action-row{flex-direction:column;} }
    </style>
</head>
<body>
<jsp:include page="/common/header.jsp" />
<div class="container">
    <c:if test="${not empty success}"><div class="alert success"><i class="fas fa-check-circle"></i>${success}<c:if test="${not empty flashResendUserId}"><button type="button" class="btn-inline" style="margin-left:auto; min-height:36px;" onclick="resendPassword(${flashResendUserId})">Gửi lại email</button></c:if></div></c:if>
    <c:if test="${not empty notice}"><div class="alert success"><i class="fas fa-circle-info"></i>${notice}</div></c:if>
    <c:if test="${not empty error and not addModalOpen and not editModalOpen}"><div class="alert error"><i class="fas fa-exclamation-circle"></i>${error}</div></c:if>

    <div class="table-container">
        <div class="table-header">
            <h3><i class="fas fa-id-badge"></i> Danh sách nhân viên</h3>
            <button class="btn-add" type="button" onclick="openAddModal()"><i class="fas fa-plus"></i> Thêm nhân viên</button>
        </div>

        <form method="GET" action="${pageContext.request.contextPath}/admin-staffs" class="toolbar">
            <div class="search-box">
                <label>Tìm kiếm</label>
                <input type="text" name="keyword" value="${keyword}" placeholder="Nhập tên, số điện thoại hoặc email...">
            </div>
            <div class="filter-box">
                <label>Vai trò</label>
                <select name="role" onchange="this.form.submit()">
                    <option value="">-- Tất cả --</option>
                    <c:forEach var="role" items="${roleOptions}">
                        <option value="${role}" ${selectedRole == role ? 'selected' : ''}>${role == 'doctor' ? 'Bác sĩ' : role == 'receptionist' ? 'Tiếp tân' : role == 'technician' ? 'Kỹ thuật viên' : 'Quản lý bệnh nhân'}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="filter-box">
                <label>Bằng cấp</label>
                <select name="qualification" onchange="this.form.submit()">
                    <option value="">-- Tất cả --</option>
                    <c:forEach var="q" items="${qualificationOptions}">
                        <option value="${q}" ${selectedQualification == q ? 'selected' : ''}>${q == 'bachelor' ? 'Cử nhân' : q == 'master' ? 'Thạc sĩ' : 'Tiến sĩ'}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="toolbar-buttons">
                <button class="btn-search" type="submit"><i class="fas fa-search"></i> Tìm</button>
                <a class="btn-reset" href="${pageContext.request.contextPath}/admin-staffs"><i class="fas fa-undo"></i> Đặt lại</a>
            </div>
        </form>

        <c:choose>
            <c:when test="${not empty staffs}">
                <table>
                    <thead>
                        <tr>
                            <th>Họ tên</th>
                            <th>Vai trò</th>
                            <th>Liên hệ</th>
                            <th>Bằng cấp</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="d" items="${staffsPaged}">
                            <c:url var="doctorScheduleUrl" value="/admin-doctor-schedules"><c:param name="keyword" value="${d.fullName}" /></c:url>
                            <tr>
                                <td>${d.fullName}</td>
                                <td>${d.role == 'doctor' ? 'Bác sĩ' : d.role == 'receptionist' ? 'Tiếp tân' : d.role == 'technician' ? 'Kỹ thuật viên' : 'Quản lý bệnh nhân'}</td>
                                <td><div class="stack"><span>${d.phone}</span><span class="muted">${d.email}</span></div></td>
                                <td>${empty d.academicDegree ? '-' : (d.academicDegree == 'bachelor' ? 'Cử nhân' : d.academicDegree == 'master' ? 'Thạc sĩ' : 'Tiến sĩ')}</td>
                                <td>
                                    <div class="action-buttons">
                                        <button type="button" class="btn-action btn-edit"
                                                data-user-id="${d.userId}"
                                                data-full-name="${fn:escapeXml(d.fullName)}"
                                                data-phone="${fn:escapeXml(d.phone)}"
                                                data-email="${fn:escapeXml(d.email)}"
                                                data-status="${fn:escapeXml(d.status)}"
                                                data-role="${fn:escapeXml(d.role)}"
                                                data-qualification="${fn:escapeXml(d.academicDegree)}"
                                                data-gender="${fn:escapeXml(d.gender)}"
                                                data-dob="${d.dob}"
                                                data-specialization="${fn:escapeXml(d.specialization)}"
                                                data-academic-title="${fn:escapeXml(d.academicTitle)}"
                                                data-professional-qualification="${fn:escapeXml(d.professionalQualification)}"
                                                data-experience="${d.experience_years}"
                                                data-rating="${d.rating}"
                                                data-price-booking="${d.price}"
                                                data-pending-resend="${pendingResendMap[d.userId] ? 'true' : 'false'}"
                                                onclick="openEditModal(this)"><i class="fas fa-pen-to-square"></i></button>
                                        <c:if test="${d.role == 'doctor'}"><a class="btn-action btn-calendar" href="${doctorScheduleUrl}"><i class="fas fa-calendar-days"></i></a></c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
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

                        <c:choose>
                            <c:when test="${currentPage > 1}">
                                <c:url var="prevPageUrl" value="/admin-staffs">
                                    <c:param name="page" value="${currentPage - 1}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="role" value="${selectedRole}" />
                                    <c:param name="qualification" value="${selectedQualification}" />
                                </c:url>
                                <a class="page-link" href="${prevPageUrl}">‹ Trước</a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link disabled">‹ Trước</span>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${startPage > 1}">
                            <span class="page-link disabled">...</span>
                        </c:if>

                        <c:forEach begin="${startPage}" end="${endPage}" var="pageNumber">
                            <c:url var="pageUrl" value="/admin-staffs">
                                <c:param name="page" value="${pageNumber}" />
                                <c:param name="keyword" value="${keyword}" />
                                <c:param name="role" value="${selectedRole}" />
                                <c:param name="qualification" value="${selectedQualification}" />
                            </c:url>
                            <a class="page-link ${pageNumber == currentPage ? 'active' : ''}" href="${pageUrl}">${pageNumber}</a>
                        </c:forEach>

                        <c:if test="${endPage < totalPages}">
                            <span class="page-link disabled">...</span>
                        </c:if>

                        <c:choose>
                            <c:when test="${currentPage < totalPages}">
                                <c:url var="nextPageUrl" value="/admin-staffs">
                                    <c:param name="page" value="${currentPage + 1}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="role" value="${selectedRole}" />
                                    <c:param name="qualification" value="${selectedQualification}" />
                                </c:url>
                                <a class="page-link" href="${nextPageUrl}">Sau ›</a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link disabled">Sau ›</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise><div class="no-data">Chưa có nhân viên nào</div></c:otherwise>
        </c:choose>
    </div>
</div>

<div id="addStaffModal" class="modal">
    <div class="modal-content">
        <div class="modal-header"><span>Thêm nhân viên</span><button class="modal-close" type="button" onclick="closeAddModal()">×</button></div>
        <form id="addStaffForm" method="POST" action="${pageContext.request.contextPath}/admin-staffs" novalidate onsubmit="return submitStaffForm('add')">
            <c:if test="${not empty error and addModalOpen}">
                <div class="alert error" style="margin-bottom: 12px;">
                    <i class="fas fa-exclamation-circle"></i>
                    ${error}
                </div>
            </c:if>
            <input type="hidden" name="action" value="add">
            <div class="form-grid">
                <div class="form-group form-full"><label>Họ tên *</label><input type="text" name="fullName" class="${not empty addFullNameError ? 'field-input-error' : ''}" value="${addFullName}"><c:if test="${not empty addFullNameError}"><div class="field-error">${addFullNameError}</div></c:if></div>
                <div class="form-group"><label>Giới tính *</label><select name="gender" class="${not empty addGenderError ? 'field-input-error' : ''}"><option value="">-- Chọn giới tính --</option><c:forEach var="g" items="${genderOptions}"><option value="${g}" ${addGender == g ? 'selected' : ''}>${g == 'male' ? 'Nam' : g == 'female' ? 'Nữ' : 'Khác'}</option></c:forEach></select><c:if test="${not empty addGenderError}"><div class="field-error">${addGenderError}</div></c:if></div>
                <div class="form-group"><label>Ngày sinh *</label><input type="date" name="dob" class="${not empty addDobError ? 'field-input-error' : ''}" value="${addDob}"><c:if test="${not empty addDobError}"><div class="field-error">${addDobError}</div></c:if></div>
                <div class="form-group"><label>Số điện thoại *</label><input type="tel" name="phone" class="${not empty addPhoneError ? 'field-input-error' : ''}" value="${addPhone}"><c:if test="${not empty addPhoneError}"><div class="field-error">${addPhoneError}</div></c:if></div>
                <div class="form-group"><label>Email *</label><input type="email" name="email" class="${not empty addEmailError ? 'field-input-error' : ''}" value="${addEmail}"><c:if test="${not empty addEmailError}"><div class="field-error">${addEmailError}</div></c:if></div>
                <div class="form-group"><label>Vai trò *</label><select id="addRole" name="role" class="${not empty addRoleError ? 'field-input-error' : ''}" onchange="toggleDoctorOnlyFields('add')"><option value="">-- Chọn vai trò --</option><c:forEach var="role" items="${roleOptions}"><option value="${role}" ${addRole == role ? 'selected' : ''}>${role == 'doctor' ? 'Bác sĩ' : role == 'receptionist' ? 'Tiếp tân' : role == 'technician' ? 'Kỹ thuật viên' : 'Quản lý bệnh nhân'}</option></c:forEach></select><c:if test="${not empty addRoleError}"><div class="field-error">${addRoleError}</div></c:if></div>
                <div class="form-group"><label>Bằng cấp *</label><select id="addQualification" name="qualification" class="${not empty addQualificationError ? 'field-input-error' : ''}"><option value="">-- Chọn bằng cấp --</option><c:forEach var="q" items="${qualificationOptions}"><option value="${q}" ${addQualification == q ? 'selected' : ''}>${q == 'bachelor' ? 'Cử nhân' : q == 'master' ? 'Thạc sĩ' : 'Tiến sĩ'}</option></c:forEach></select><c:if test="${not empty addQualificationError}"><div class="field-error">${addQualificationError}</div></c:if></div>
                <div class="form-group form-full doctor-only" id="addSpecializationGroup"><label>Chuyên môn *</label><select id="addSpecialization" name="specialization" class="${not empty addSpecializationError ? 'field-input-error' : ''}"><option value="">-- Chọn chuyên môn --</option><c:forEach var="item" items="${expertiseOptions}"><option value="${item}" ${addSpecialization == item ? 'selected' : ''}>${item}</option></c:forEach></select><c:if test="${not empty addSpecializationError}"><div class="field-error">${addSpecializationError}</div></c:if></div>
                <div class="form-group doctor-only" id="addAcademicTitleGroup"><label>Học hàm</label><select id="addAcademicTitle" name="academicTitle" class="${not empty addAcademicTitleError ? 'field-input-error' : ''}"><option value="">-- Không chọn --</option><c:forEach var="title" items="${academicTitleOptions}"><option value="${title}" ${addAcademicTitle == title ? 'selected' : ''}>${title == 'professor' ? 'Giáo sư' : 'Phó giáo sư'}</option></c:forEach></select><c:if test="${not empty addAcademicTitleError}"><div class="field-error">${addAcademicTitleError}</div></c:if></div>
                <div class="form-group doctor-only" id="addProfessionalQualificationGroup"><label>Trình độ hành nghề</label><select id="addProfessionalQualification" name="professionalQualification" class="${not empty addProfessionalQualificationError ? 'field-input-error' : ''}"><option value="">-- Không chọn --</option><c:forEach var="pq" items="${professionalQualificationOptions}"><option value="${pq}" ${addProfessionalQualification == pq ? 'selected' : ''}>${pq == 'resident_doctor' ? 'Bác sĩ nội trú' : pq == 'specialist_level_1' ? 'Chuyên khoa I' : 'Chuyên khoa II'}</option></c:forEach></select><c:if test="${not empty addProfessionalQualificationError}"><div class="field-error">${addProfessionalQualificationError}</div></c:if></div>
                <div class="form-group doctor-only" id="addExperienceGroup"><label>Kinh nghiệm *</label><input type="number" id="addExperienceYears" name="experienceYears" min="0" max="50" class="${not empty addExperienceError ? 'field-input-error' : ''}" value="${addExperience}"><c:if test="${not empty addExperienceError}"><div class="field-error">${addExperienceError}</div></c:if></div>
                <div class="form-group form-full doctor-only" id="addPriceGroup"><label>Giá khám *</label><input id="addPriceBooking" type="number" name="priceBooking" min="0" max="10000000" class="${not empty addPriceError ? 'field-input-error' : ''}" value="${addPrice}"><c:if test="${not empty addPriceError}"><div class="field-error">${addPriceError}</div></c:if></div>
            </div>
            <div class="modal-footer"><button type="button" class="btn-cancel" onclick="closeAddModal()">Hủy</button><button type="submit" class="btn-submit">Lưu</button></div>
        </form>
    </div>
</div>

<div id="editStaffModal" class="modal">
    <div class="modal-content">
        <div class="modal-header"><span>Chỉnh sửa nhân viên</span><button class="modal-close" type="button" onclick="closeEditModal()">×</button></div>
        <form id="editStaffForm" method="POST" action="${pageContext.request.contextPath}/admin-staffs" novalidate onsubmit="return submitStaffForm('edit')">
            <c:if test="${not empty error and editModalOpen}">
                <div class="alert error" style="margin-bottom: 12px;">
                    <i class="fas fa-exclamation-circle"></i>
                    ${error}
                </div>
            </c:if>
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="userId" id="editUserId" value="${editUserId}">
            <div class="form-grid">
                <div class="form-group form-full"><label>Họ tên *</label><input type="text" id="editFullName" name="fullName" class="${not empty editFullNameError ? 'field-input-error' : ''}" value="${editFullName}"><c:if test="${not empty editFullNameError}"><div class="field-error">${editFullNameError}</div></c:if></div>
                <div class="form-group"><label>Giới tính *</label><select id="editGender" name="gender" class="${not empty editGenderError ? 'field-input-error' : ''}"><option value="">-- Chọn giới tính --</option><c:forEach var="g" items="${genderOptions}"><option value="${g}" ${editGender == g ? 'selected' : ''}>${g == 'male' ? 'Nam' : g == 'female' ? 'Nữ' : 'Khác'}</option></c:forEach></select><c:if test="${not empty editGenderError}"><div class="field-error">${editGenderError}</div></c:if></div>
                <div class="form-group"><label>Ngày sinh *</label><input type="date" id="editDob" name="dob" class="${not empty editDobError ? 'field-input-error' : ''}" value="${editDob}"><c:if test="${not empty editDobError}"><div class="field-error">${editDobError}</div></c:if></div>
                <div class="form-group"><label>Số điện thoại *</label><input type="tel" id="editPhone" name="phone" class="${not empty editPhoneError ? 'field-input-error' : ''}" value="${editPhone}"><c:if test="${not empty editPhoneError}"><div class="field-error">${editPhoneError}</div></c:if></div>
                <div class="form-group"><label>Email *</label><div class="input-action-row"><input type="email" id="editEmail" name="email" class="${not empty editEmailError ? 'field-input-error' : ''}" value="${editEmail}"><button type="button" class="btn-inline" id="editResendButton" onclick="resendPasswordFromEditModal()" style="display:none;">Gửi lại email</button></div><c:if test="${not empty editEmailError}"><div class="field-error">${editEmailError}</div></c:if></div>
                <div class="form-group"><label>Trạng thái</label><input class="readonly-field" type="text" id="editStatus" value="${editStatus}" readonly></div>
                <div class="form-group"><label>Vai trò *</label><select id="editRole" name="role" class="${not empty editRoleError ? 'field-input-error' : ''}" onchange="toggleDoctorOnlyFields('edit')"><option value="">-- Chọn vai trò --</option><c:forEach var="role" items="${roleOptions}"><option value="${role}" ${editRole == role ? 'selected' : ''}>${role == 'doctor' ? 'Bác sĩ' : role == 'receptionist' ? 'Tiếp tân' : role == 'technician' ? 'Kỹ thuật viên' : 'Quản lý bệnh nhân'}</option></c:forEach></select><c:if test="${not empty editRoleError}"><div class="field-error">${editRoleError}</div></c:if></div>
                <div class="form-group"><label>Bằng cấp *</label><select id="editQualification" name="qualification" class="${not empty editQualificationError ? 'field-input-error' : ''}"><option value="">-- Chọn bằng cấp --</option><c:forEach var="q" items="${qualificationOptions}"><option value="${q}" ${editQualification == q ? 'selected' : ''}>${q == 'bachelor' ? 'Cử nhân' : q == 'master' ? 'Thạc sĩ' : 'Tiến sĩ'}</option></c:forEach></select><c:if test="${not empty editQualificationError}"><div class="field-error">${editQualificationError}</div></c:if></div>
                <div class="form-group form-full doctor-only" id="editSpecializationGroup"><label>Chuyên môn *</label><select id="editSpecialization" name="specialization" class="${not empty editSpecializationError ? 'field-input-error' : ''}"><option value="">-- Chọn chuyên môn --</option><c:forEach var="item" items="${expertiseOptions}"><option value="${item}" ${editSpecialization == item ? 'selected' : ''}>${item}</option></c:forEach></select><c:if test="${not empty editSpecializationError}"><div class="field-error">${editSpecializationError}</div></c:if></div>
                <div class="form-group doctor-only" id="editAcademicTitleGroup"><label>Học hàm</label><select id="editAcademicTitle" name="academicTitle" class="${not empty editAcademicTitleError ? 'field-input-error' : ''}"><option value="">-- Không chọn --</option><c:forEach var="title" items="${academicTitleOptions}"><option value="${title}" ${editAcademicTitle == title ? 'selected' : ''}>${title == 'professor' ? 'Giáo sư' : 'Phó giáo sư'}</option></c:forEach></select><c:if test="${not empty editAcademicTitleError}"><div class="field-error">${editAcademicTitleError}</div></c:if></div>
                <div class="form-group doctor-only" id="editProfessionalQualificationGroup"><label>Trình độ hành nghề</label><select id="editProfessionalQualification" name="professionalQualification" class="${not empty editProfessionalQualificationError ? 'field-input-error' : ''}"><option value="">-- Không chọn --</option><c:forEach var="pq" items="${professionalQualificationOptions}"><option value="${pq}" ${editProfessionalQualification == pq ? 'selected' : ''}>${pq == 'resident_doctor' ? 'Bác sĩ nội trú' : pq == 'specialist_level_1' ? 'Chuyên khoa I' : 'Chuyên khoa II'}</option></c:forEach></select><c:if test="${not empty editProfessionalQualificationError}"><div class="field-error">${editProfessionalQualificationError}</div></c:if></div>
                <div class="form-group doctor-only" id="editExperienceGroup"><label>Kinh nghiệm *</label><input type="number" id="editExperienceYears" name="experienceYears" min="0" max="50" class="${not empty editExperienceError ? 'field-input-error' : ''}" value="${editExperience}"><c:if test="${not empty editExperienceError}"><div class="field-error">${editExperienceError}</div></c:if></div>
                <div class="form-group doctor-only" id="editRatingGroup"><label>Đánh giá</label><input class="readonly-field" type="text" id="editRating" value="${editRating}" readonly></div>
                <div class="form-group doctor-only" id="editPriceGroup"><label>Giá khám *</label><input type="number" id="editPriceBooking" name="priceBooking" min="0" max="10000000" class="${not empty editPriceError ? 'field-input-error' : ''}" value="${editPrice}"><c:if test="${not empty editPriceError}"><div class="field-error">${editPriceError}</div></c:if></div>
            </div>
            <div class="modal-footer"><button type="button" class="btn-cancel" onclick="closeEditModal()">Hủy</button><button type="submit" class="btn-submit">Lưu</button></div>
        </form>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />
<jsp:include page="../../common/modal-alert.jsp" />
        <script>
            function formatStatusDisplay(v) {
                return (v || '').toLowerCase() === 'active' ? 'Hoạt động' : 'Khóa'
            }
            function isDoctorRole(v) {
                return (v || '').trim() === 'doctor'
            }
            function ensureClientFieldError(prefix, fieldName, message) {
                const input = document.getElementById(prefix + fieldName);
                if (!input)
                    return;
                input.classList.add('field-input-error');
                const group = input.closest('.form-group');
                if (!group)
                    return;
                let error = group.querySelector('.field-error.client-error[data-error-for="' + fieldName + '"]');
                if (!error) {
                    error = document.createElement('div');
                    error.className = 'field-error client-error';
                    error.dataset.errorFor = fieldName;
                    group.appendChild(error)
                }
                error.textContent = message
            }
            function clearClientFieldError(prefix, fieldName) {
                const input = document.getElementById(prefix + fieldName);
                if (input)
                    input.classList.remove('field-input-error');
                const group = input ? input.closest('.form-group') : null;
                if (!group)
                    return;
                group.querySelectorAll('.field-error.client-error[data-error-for="' + fieldName + '"]').forEach(function (el) {
                    el.remove()
                })
            }
            function clearRenderedFieldError(prefix, fieldName) {
                const input = document.getElementById(prefix + fieldName);
                if (input)
                    input.classList.remove('field-input-error');
                const group = input ? input.closest('.form-group') : null;
                if (!group)
                    return;
                group.querySelectorAll('.field-error').forEach(function (el) {
                    el.remove()
                })
            }
            function clearAllRenderedErrors(prefix) {
                const modal = document.getElementById(prefix === 'add' ? 'addStaffModal' : 'editStaffModal');
                if (!modal)
                    return;
                modal.querySelectorAll('.field-error').forEach(function (el) {
                    el.remove()
                });
                modal.querySelectorAll('.field-input-error').forEach(function (el) {
                    el.classList.remove('field-input-error')
                })
            }
            function validateProfessionalQualificationRule(prefix, showError) {
                const role = (document.getElementById(prefix + 'Role')?.value || '').trim();
                const qualification = (document.getElementById(prefix + 'Qualification')?.value || '').trim();
                const professionalQualification = (document.getElementById(prefix + 'ProfessionalQualification')?.value || '').trim();
                const invalid = isDoctorRole(role) && qualification === 'bachelor' && !professionalQualification;
                if (invalid) {
                    if (showError) {
                        ensureClientFieldError(prefix, 'ProfessionalQualification', 'Bác sĩ có bằng cấp cử nhân bắt buộc phải có trình độ hành nghề')
                    } else {
                        clearClientFieldError(prefix, 'ProfessionalQualification')
                    }
                    return false
                }
                clearClientFieldError(prefix, 'ProfessionalQualification');
                return true
            }
            function validateAcademicTitleRule(prefix, showError) {
                const role = (document.getElementById(prefix + 'Role')?.value || '').trim();
                const qualification = (document.getElementById(prefix + 'Qualification')?.value || '').trim();
                const academicTitle = (document.getElementById(prefix + 'AcademicTitle')?.value || '').trim();
                const invalid = isDoctorRole(role) && !!academicTitle && qualification !== 'doctorate';
                if (invalid) {
                    if (showError) {
                        ensureClientFieldError(prefix, 'AcademicTitle', 'Bác sĩ có học hàm bắt buộc phải có bằng tiến sĩ')
                    } else {
                        clearClientFieldError(prefix, 'AcademicTitle')
                    }
                    return false
                }
                clearClientFieldError(prefix, 'AcademicTitle');
                return true
            }
            function getSuggestedPrice(prefix) {
                const qualification = (document.getElementById(prefix + 'Qualification')?.value || '').trim();
                const academicTitle = (document.getElementById(prefix + 'AcademicTitle')?.value || '').trim();
                const professionalQualification = (document.getElementById(prefix + 'ProfessionalQualification')?.value || '').trim();
                if (academicTitle === 'professor' || academicTitle === 'associate_professor')
                    return '400000';
                if (qualification === 'doctorate' || professionalQualification === 'specialist_level_2')
                    return '300000';
                if (qualification === 'master' || professionalQualification === 'specialist_level_1' || professionalQualification === 'resident_doctor')
                    return '200000';
                return ''
            }
            function applySuggestedPrice(prefix, force) {
                const roleInput = document.getElementById(prefix + 'Role');
                const priceInput = document.getElementById(prefix + 'PriceBooking');
                if (!roleInput || !priceInput || !isDoctorRole(roleInput.value))
                    return;
                const suggested = getSuggestedPrice(prefix);
                if (!suggested)
                    return;
                if (force || !(priceInput.value || '').trim())
                    priceInput.value = suggested
            }
            function bindPriceRules(prefix) {
                ['Qualification', 'AcademicTitle', 'ProfessionalQualification'].forEach(function (suffix) {
                    const el = document.getElementById(prefix + suffix);
                    if (el)
                        el.addEventListener('change', function () {
                            clearRenderedFieldError(prefix, suffix);
                            applySuggestedPrice(prefix, true);
                            validateProfessionalQualificationRule(prefix, suffix === 'ProfessionalQualification');
                            validateAcademicTitleRule(prefix, false)
                        })
                })
            }
            function bindFieldErrorReset(prefix) {
                ['FullName', 'Gender', 'Dob', 'Phone', 'Email', 'Role', 'Qualification', 'Specialization', 'AcademicTitle', 'ProfessionalQualification', 'ExperienceYears', 'PriceBooking'].forEach(function (suffix) {
                    const el = document.getElementById(prefix + suffix);
                    if (!el)
                        return;
                    const eventName = el.tagName === 'SELECT' ? 'change' : 'input';
                    el.addEventListener(eventName, function () {
                        clearRenderedFieldError(prefix, suffix)
                    })
                })
            }
            function toggleDoctorOnlyFields(prefix) {
                const show = isDoctorRole(document.getElementById(prefix + 'Role').value);
                ['SpecializationGroup', 'AcademicTitleGroup', 'ProfessionalQualificationGroup', 'ExperienceGroup', 'RatingGroup', 'PriceGroup'].forEach(s => {
                    const el = document.getElementById(prefix + s);
                    if (el)
                        el.style.display = show ? 'block' : 'none'
                });
                if (show)
                    applySuggestedPrice(prefix, false);
                validateProfessionalQualificationRule(prefix, false);
                validateAcademicTitleRule(prefix, false)
            }
            function clearFieldErrors(modalId) {
                const modal = document.getElementById(modalId);
                if (!modal)
                    return;
                modal.querySelectorAll('.field-error.client-error').forEach(el => el.remove());
                modal.querySelectorAll('.field-input-error').forEach(function (el) {
                    if (el.closest('.form-group')?.querySelector('.field-error'))
                        return;
                    el.classList.remove('field-input-error')
                })
            }
            function toggleEditResendButton(show) {
                const button = document.getElementById('editResendButton');
                if (button)
                    button.style.display = show ? 'inline-flex' : 'none'
            }
            function submitStaffForm(prefix) {
                clearAllRenderedErrors(prefix);
                validateProfessionalQualificationRule(prefix, true);
                validateAcademicTitleRule(prefix, true);
                return true
            }
            function setFormFieldValue(form, name, value) {
                if (!form || !form.elements || !form.elements[name])
                    return;
                form.elements[name].value = value
            }
            function resetAddModal() {
                const form = document.getElementById('addStaffForm');
                if (form) {
                    setFormFieldValue(form, 'fullName', '');
                    setFormFieldValue(form, 'gender', '');
                    setFormFieldValue(form, 'dob', '');
                    setFormFieldValue(form, 'phone', '');
                    setFormFieldValue(form, 'email', '');
                    setFormFieldValue(form, 'role', '');
                    setFormFieldValue(form, 'qualification', '');
                    setFormFieldValue(form, 'specialization', '');
                    setFormFieldValue(form, 'academicTitle', '');
                    setFormFieldValue(form, 'professionalQualification', '');
                    setFormFieldValue(form, 'experienceYears', '');
                    setFormFieldValue(form, 'priceBooking', '')
                }
                clearAllRenderedErrors('add');
                const modal = document.getElementById('addStaffModal');
                if (modal)
                    modal.querySelectorAll('.alert').forEach(function (el) {
                        el.remove()
                    });
                toggleDoctorOnlyFields('add')
            }
            function openAddModal(resetForm) {
                if (resetForm !== false)
                    resetAddModal();
                document.getElementById('addStaffModal').style.display = 'block';
                toggleDoctorOnlyFields('add')
            }
            function closeAddModal() {
                resetAddModal();
                document.getElementById('addStaffModal').style.display = 'none'
            }
            function closeEditModal() {
                document.getElementById('editStaffModal').style.display = 'none'
            }
            function resendPassword(userId) {
                if (!userId)
                    return;
                const message = 'Gửi lại mật khẩu tạm qua email cho nhân viên này?';
                const submitResendForm = function () {
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = '${pageContext.request.contextPath}/admin-staffs';
                    form.innerHTML = '<input type="hidden" name="action" value="resendPassword"><input type="hidden" name="userId" value="' + userId + '">';
                    document.body.appendChild(form);
                    form.submit()
                };
                if (typeof showConfirm === 'function') {
                    showConfirm(message, submitResendForm);
                    return;
                }
                if (!confirm(message))
                    return;
                submitResendForm()
            }
            function resendPasswordFromEditModal() {
                const userId = document.getElementById('editUserId').value;
                if (!userId)
                    return;
                resendPassword(userId)
            }
            function openEditModal(btn) {
                clearFieldErrors('editStaffModal');
                document.getElementById('editUserId').value = btn.dataset.userId || '';
                document.getElementById('editFullName').value = btn.dataset.fullName || '';
                document.getElementById('editPhone').value = btn.dataset.phone || '';
                document.getElementById('editEmail').value = btn.dataset.email || '';
                document.getElementById('editStatus').value = formatStatusDisplay(btn.dataset.status);
                document.getElementById('editRole').value = btn.dataset.role || '';
                document.getElementById('editQualification').value = btn.dataset.qualification || '';
                document.getElementById('editGender').value = btn.dataset.gender || '';
                document.getElementById('editDob').value = btn.dataset.dob || '';
                document.getElementById('editSpecialization').value = btn.dataset.specialization || '';
                document.getElementById('editAcademicTitle').value = btn.dataset.academicTitle || '';
                document.getElementById('editProfessionalQualification').value = btn.dataset.professionalQualification || '';
                document.getElementById('editExperienceYears').value = (btn.dataset.experience && btn.dataset.experience !== '0') ? btn.dataset.experience : '';
                document.getElementById('editRating').value = btn.dataset.rating || '0.0';
                document.getElementById('editPriceBooking').value = parseInt(btn.dataset.priceBooking || '0', 10) || '';
                toggleEditResendButton(btn.dataset.pendingResend === true || btn.dataset.pendingResend === 'true');
                document.getElementById('editStaffModal').style.display = 'block';
                toggleDoctorOnlyFields('edit')
            }
            window.onclick = function (event) {
                if (event.target === document.getElementById('addStaffModal'))
                    closeAddModal();
                if (event.target === document.getElementById('editStaffModal'))
                    closeEditModal()
            }
            document.addEventListener('DOMContentLoaded', function () {
                const alerts = document.querySelectorAll('.alert');
                alerts.forEach(function (alert) {
                    setTimeout(function () {
                        alert.classList.add('fade-out');
                        setTimeout(function () {
                            if (alert && alert.parentNode)
                                alert.parentNode.removeChild(alert)
                        }, 300)
                    }, 5000)
                });
                const addRole = document.getElementById('addRole');
                const editRole = document.getElementById('editRole');
                if (addRole)
                    addRole.addEventListener('change', function () {
                        toggleDoctorOnlyFields('add')
                    });
                if (editRole)
                    editRole.addEventListener('change', function () {
                        toggleDoctorOnlyFields('edit')
                    });
                bindFieldErrorReset('add');
                bindFieldErrorReset('edit');
                bindPriceRules('add');
                bindPriceRules('edit');
                toggleDoctorOnlyFields('add');
                toggleDoctorOnlyFields('edit');
                toggleEditResendButton(false);<c:if test="${addModalOpen}">openAddModal(false);</c:if><c:if test="${editModalOpen}">document.getElementById('editStaffModal').style.display = 'block';
                document.getElementById('editUserId').value = '${fn:escapeXml(editUserId)}';
                document.getElementById('editFullName').value = '${fn:escapeXml(editFullName)}';
                document.getElementById('editPhone').value = '${fn:escapeXml(editPhone)}';
                document.getElementById('editEmail').value = '${fn:escapeXml(editEmail)}';
                document.getElementById('editStatus').value = formatStatusDisplay('${fn:escapeXml(editStatus)}');
                document.getElementById('editRole').value = '${fn:escapeXml(editRole)}';
                document.getElementById('editQualification').value = '${fn:escapeXml(editQualification)}';
                document.getElementById('editGender').value = '${fn:escapeXml(editGender)}';
                document.getElementById('editDob').value = '${fn:escapeXml(editDob)}';
                document.getElementById('editSpecialization').value = '${fn:escapeXml(editSpecialization)}';
                document.getElementById('editAcademicTitle').value = '${fn:escapeXml(editAcademicTitle)}';
                document.getElementById('editProfessionalQualification').value = '${fn:escapeXml(editProfessionalQualification)}';
                document.getElementById('editExperienceYears').value = ('${fn:escapeXml(editExperience)}' && '${fn:escapeXml(editExperience)}' !== '0') ? '${fn:escapeXml(editExperience)}' : '';
                document.getElementById('editRating').value = '${fn:escapeXml(editRating)}';
                document.getElementById('editPriceBooking').value = parseInt('${fn:escapeXml(editPrice)}' || '0', 10) || '';
                toggleEditResendButton('${editResendAvailable}' === 'true');
                toggleDoctorOnlyFields('edit');
                document.querySelector('#editStaffModal .modal-content')?.scrollTo({top: 0, behavior: 'instant'});</c:if>
});
        </script>
</body>
</html>




