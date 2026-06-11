<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tài khoản bệnh nhân</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); min-height: 100vh; }
        .container { padding: 30px 50px; max-width: 1400px; margin: 0 auto; }
        .toolbar { background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 100%; padding: 20px; margin-bottom: 20px; display: grid; grid-template-columns: minmax(460px, 2.5fr) minmax(260px, 1.15fr) auto; gap: 12px; align-items: end; }
        .search-box, .filter-box { min-width: 0; }
        .search-box label, .filter-box label, .form-group label { display: block; font-weight: 600; margin-bottom: 8px; color: #333; font-size: 13px; }
        .search-box input, .filter-box select, .form-group input { width: 100%; padding: 10px 15px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
        .toolbar-buttons { display: flex; gap: 10px; align-self: end; justify-self: end; }
        .btn-search, .btn-reset, .btn-submit, .btn-cancel, .btn-back, .btn-add { padding: 10px 16px; border: none; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 14px; display: inline-flex; align-items: center; gap: 6px; cursor: pointer; }
        .btn-reset, .btn-cancel { background: #f0f0f0; color: #334155; }
        .btn-back { background: #f0f0f0; color: #334155; font-weight: 700; }
        .btn-search, .btn-submit { background: #0061ff; color: white; }
        .btn-add { background: #4caf50; color: white; }
        .btn-add:hover { background: #45a049; }
        .btn-inline { border: none; border-radius: 6px; padding: 0 14px; font-size: 13px; font-weight: 600; background: #f59e0b; color: white; cursor: pointer; white-space: nowrap; display: inline-flex; align-items: center; justify-content: center; align-self: stretch; min-height: 42px; }
        .btn-inline:hover { background: #d97706; }
        .alert { padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; animation: slideIn 0.3s ease-out; }
        .alert.success { background: #e8f5e9; color: #2e7d32; border-left: 4px solid #4caf50; }
        .alert.error { background: #ffebee; color: #c62828; border-left: 4px solid #f44336; }
        @keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
        .table-container { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); overflow-x: auto; width: 100%; }
        .table-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; gap:12px; }
        .table-header h3 { font-size:18px; color:#333; display:flex; align-items:center; gap:8px; }
        table { width: 100%; min-width: 100%; border-collapse: collapse; }
        th { background: linear-gradient(135deg, #f8f9fa 0%, #f0f0f0 100%); padding: 15px; text-align: left; font-weight: 600; color: #333; border-bottom: 2px solid #e0e0e0; font-size: 14px; }
        td { padding: 15px; border-bottom: 1px solid #f0f0f0; color: #555; font-size: 14px; }
        tr:hover { background: #f9f9f9; }
        .badge { display: inline-block; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-patient { background: #e1f5fe; color: #0277bd; }
        .badge-active { background: #e8f5e9; color: #388e3c; }
        .badge-inactive { background: #ffebee; color: #d32f2f; }
        .btn-action { border: none; background: none; cursor: pointer; font-size: 16px; padding: 6px 10px; border-radius: 4px; transition: all 0.3s ease; display: flex; align-items: center; gap: 4px; }
        .action-buttons { justify-content: center; display: flex; flex-wrap: wrap; }
        .btn-edit { color: #fb923c; }
        .btn-edit:hover { background: #FFEDD5; }
        .btn-toggle { color: #7b1fa2; }
        .btn-toggle:hover { background: #f3e5f5; }
        .no-data { text-align: center; padding: 40px; color: #999; }
        .pagination-wrapper { margin-top: 16px; display: flex; justify-content: center; gap: 8px; flex-wrap: wrap; }
        .page-link { min-width: 34px; padding: 8px 12px; border: 1px solid #dcdcdc; border-radius: 6px; text-decoration: none; color: #333; background: #fff; text-align: center; }
        .page-link.active { background: #0061ff; color: #fff; border-color: #0061ff; pointer-events: none; }
        .page-link.disabled { opacity: .5; pointer-events: none; }
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); overflow-y: auto; }
        .modal-content { background-color: white; margin: 5% auto; padding: 30px; border-radius: 10px; width: 90%; max-width: 550px; box-shadow: 0 10px 40px rgba(0,0,0,0.3); }
        .modal-header { font-size: 20px; font-weight: 600; color: #0061ff; margin-bottom: 25px; display: flex; align-items: center; gap: 10px; border-bottom: 2px solid #f0f0f0; padding-bottom: 15px; }
        .modal-close { margin-left: auto; cursor: pointer; font-size: 24px; background: none; border: none; color: #999; }
        .form-group { margin-bottom: 18px; }
        .field-error { color: #d32f2f; font-size: 12px; margin-top: 6px; font-weight: 500; }
        .input-action-row { display: flex; gap: 10px; align-items: stretch; }
        .input-action-row input { flex: 1; }
        .modal-footer { display: flex; gap: 10px; justify-content: flex-end; margin-top: 25px; padding-top: 15px; border-top: 1px solid #f0f0f0; }
        @media (max-width: 768px) {
            .container { padding: 20px; }
            .toolbar { grid-template-columns: 1fr; }
            .toolbar-buttons { width: 100%; justify-content: stretch; }
            .toolbar-buttons .btn-search, .toolbar-buttons .btn-reset, .toolbar-buttons .btn-submit, .toolbar-buttons .btn-back, .toolbar-buttons .btn-add { flex: 1; justify-content: center; }
            .input-action-row { flex-direction: column; }
            .btn-inline { padding: 10px 14px; }
        }
    </style>
</head>
<body>
    <jsp:include page="/common/header.jsp" />
    <div class="container">
        <c:if test="${not empty success}">
            <div class="alert success"><i class="fas fa-check-circle"></i>${success}</div>
        </c:if>
        <c:if test="${not empty error and not addModalOpen and not editModalOpen}">
            <div class="alert error"><i class="fas fa-exclamation-circle"></i>${error}</div>
        </c:if>

        <div class="toolbar">
            <div class="search-box">
                <label><i class="fas fa-search"></i> Tìm kiếm</label>
                <input type="text" id="userSearch" placeholder="Nhập tên, số điện thoại hoặc email..." value="${searchKeyword}">
            </div>
            <div class="filter-box">
                <label><i class="fas fa-filter"></i> Trạng thái</label>
                <select id="userStatusFilter">
                    <option value="all" ${filterStatus == 'all' ? 'selected' : ''}>-- Tất cả --</option>
                    <option value="active" ${filterStatus == 'active' ? 'selected' : ''}>Hoạt động</option>
                    <option value="inactive" ${filterStatus == 'inactive' ? 'selected' : ''}>Khóa</option>
                </select>
            </div>
            <div class="toolbar-buttons">
                <button class="btn-search" onclick="searchUsers()"><i class="fas fa-search"></i> Tìm</button>
                <button class="btn-reset" onclick="resetUserFilter()"><i class="fas fa-redo"></i> Đặt lại</button>
                <c:if test="${canManagePatients}">
                    <button class="btn-add" type="button" onclick="openAddModal()"><i class="fas fa-user-plus"></i> Thêm tài khoản</button>
                </c:if>
                <c:if test="${patientAccountViewerRole == 'admin'}">
                    <a class="btn-back" href="${pageContext.request.contextPath}/users"><i class="fas fa-arrow-left"></i> Quản lý tài khoản</a>
                </c:if>
            </div>
        </div>

        <div class="table-container">
            <div class="table-header">
                <h3><i class="fas fa-users"></i> Danh sách tài khoản bệnh nhân</h3>
            </div>

            <c:choose>
                <c:when test="${not empty users}">
                    <table>
                        <thead>
                            <tr>
                                <th>Tên</th>
                                <th>Số điện thoại</th>
                                <th>Email</th>
                                <th>Vai trò</th>
                                <th>Trạng thái</th>
                                <th style="width: 140px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${users}" var="user">
                                <tr>
                                    <td><strong>${user.fullName}</strong></td>
                                    <td>${user.phone}</td>
                                    <td>${not empty user.email ? user.email : '<em>Chưa cập nhật</em>'}</td>
                                    <td><span class="badge badge-patient">Bệnh nhân</span></td>
                                    <td><span class="badge ${user.status.toString() == 'active' ? 'badge-active' : 'badge-inactive'}">${user.status.toString() == 'active' ? 'Hoạt động' : 'Khóa'}</span></td>
                                    <td>
                                        <div class="action-buttons">
                                            <c:if test="${canManagePatients}">
                                                <button class="btn-action btn-edit" onclick="openEditModal('${user.userId}', '${user.fullName}', '${user.phone}', '${user.email}', '${pendingResendMap[user.userId] ? 'true' : 'false'}')" title="Chỉnh sửa"><i class="fas fa-pen-to-square"></i></button>
                                                <button class="btn-action btn-toggle" onclick="toggleStatus(${user.userId}, '${user.fullName}')" title="Đổi trạng thái"><i class="fas fa-toggle-on"></i></button>
                                            </c:if>
                                            <c:if test="${not canManagePatients}">-</c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="no-data"><i class="fas fa-inbox"></i><p>Chưa có tài khoản bệnh nhân nào</p></div>
                </c:otherwise>
            </c:choose>
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
                <c:url var="prevUrl" value="patient-accounts">
                    <c:param name="action" value="${currentAction}" />
                    <c:param name="keyword" value="${searchKeyword}" />
                    <c:param name="status" value="${filterStatus}" />
                    <c:param name="page" value="${currentPage - 1}" />
                </c:url>
                <c:url var="nextUrl" value="patient-accounts">
                    <c:param name="action" value="${currentAction}" />
                    <c:param name="keyword" value="${searchKeyword}" />
                    <c:param name="status" value="${filterStatus}" />
                    <c:param name="page" value="${currentPage + 1}" />
                </c:url>
                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a class="page-link" href="${prevUrl}">‹ Trước</a>
                    </c:when>
                    <c:otherwise><span class="page-link disabled">‹ Trước</span></c:otherwise>
                </c:choose>

                <c:if test="${startPage > 1}">
                    <span class="page-link disabled">...</span>
                </c:if>

                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                    <c:url var="pageUrl" value="patient-accounts">
                        <c:param name="action" value="${currentAction}" />
                        <c:param name="keyword" value="${searchKeyword}" />
                        <c:param name="status" value="${filterStatus}" />
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
                    <c:otherwise><span class="page-link disabled">Sau ›</span></c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>

    <c:if test="${canManagePatients}">
        <div id="addAccountModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <i class="fas fa-user-plus"></i>
                    <span>Thêm tài khoản bệnh nhân</span>
                    <button class="modal-close" onclick="closeModal('addAccountModal')">×</button>
                </div>
                <form action="patient-accounts" method="POST" id="addAccountForm">
                    <input type="hidden" name="action" value="add">

                    <c:if test="${not empty error and addModalOpen}">
                        <div class="alert error"><i class="fas fa-exclamation-circle"></i>${error}</div>
                    </c:if>

                    <div class="form-group">
                        <label>Họ tên <span style="color: red;">*</span></label>
                        <input type="text" name="fullname" id="addFullName" required maxlength="100" value="${addFullName}">
                        <c:if test="${not empty addFullNameError}"><div class="field-error">${addFullNameError}</div></c:if>
                    </div>
                    <div class="form-group">
                        <label>Số điện thoại <span style="color: red;">*</span></label>
                        <input type="tel" name="phone" id="addPhone" required maxlength="10" pattern="0[0-9]{9}" value="${addPhone}">
                        <c:if test="${not empty addPhoneError}"><div class="field-error">${addPhoneError}</div></c:if>
                    </div>
                    <div class="form-group">
                        <label>Email <span style="color: red;">*</span></label>
                        <input type="email" name="email" id="addEmail" required maxlength="100" value="${addEmail}">
                        <c:if test="${not empty addEmailError}"><div class="field-error">${addEmailError}</div></c:if>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn-cancel" onclick="closeModal('addAccountModal')"><i class="fas fa-times"></i> Hủy</button>
                        <button type="submit" class="btn-submit"><i class="fas fa-save"></i> Tạo tài khoản</button>
                    </div>
                </form>
            </div>
        </div>

        <div id="editAccountModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <i class="fas fa-pen-to-square"></i>
                    <span>Chỉnh sửa tài khoản bệnh nhân</span>
                    <button class="modal-close" onclick="closeModal('editAccountModal')">×</button>
                </div>
                <form action="patient-accounts" method="POST" id="editAccountForm">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" name="userId" id="editUserId" value="${editUserId}">

                    <c:if test="${not empty error and editModalOpen}">
                        <div class="alert error"><i class="fas fa-exclamation-circle"></i>${error}</div>
                    </c:if>

                    <div class="form-group">
                        <label>Họ tên <span style="color: red;">*</span></label>
                        <input type="text" name="fullname" id="editFullName" required maxlength="100" value="${editFullName}">
                        <c:if test="${not empty editFullNameError}"><div class="field-error">${editFullNameError}</div></c:if>
                    </div>
                    <div class="form-group">
                        <label>Số điện thoại <span style="color: red;">*</span></label>
                        <input type="tel" name="phone" id="editPhone" required maxlength="10" pattern="0[0-9]{9}" value="${editPhone}">
                        <c:if test="${not empty editPhoneError}"><div class="field-error">${editPhoneError}</div></c:if>
                    </div>
                    <div class="form-group">
                        <label>Email <span style="color: red;">*</span></label>
                        <div class="input-action-row">
                            <input type="email" name="email" id="editEmail" required maxlength="100" value="${editEmail}">
                            <button type="button" class="btn-inline" id="editResendButton" onclick="resendPasswordFromEditModal()" style="display: none;">Gửi lại email</button>
                        </div>
                        <c:if test="${not empty editEmailError}"><div class="field-error">${editEmailError}</div></c:if>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn-cancel" onclick="closeModal('editAccountModal')"><i class="fas fa-times"></i> Hủy</button>
                        <button type="submit" class="btn-submit"><i class="fas fa-save"></i> Lưu thay đổi</button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <jsp:include page="../../common/footer.jsp" />
    <jsp:include page="../../common/modal-alert.jsp" />
    <script>
        const canManagePatients = '${canManagePatients}' === 'true';

        function toggleEditResendButton(show) {
            const button = document.getElementById('editResendButton');
            if (button) {
                button.style.display = show ? 'inline-flex' : 'none';
            }
        }

        function openAddModal() {
            if (!canManagePatients) return;
            const form = document.getElementById('addAccountForm');
            if (form) {
                form.reset();
            }
            openModal('addAccountModal');
        }

        function openEditModal(userId, fullName, phone, email, pendingResend) {
            if (!canManagePatients) return;
            document.getElementById('editUserId').value = userId || '';
            document.getElementById('editFullName').value = fullName || '';
            document.getElementById('editPhone').value = phone || '';
            document.getElementById('editEmail').value = email || '';
            toggleEditResendButton(pendingResend === true || pendingResend === 'true');
            openModal('editAccountModal');
        }

        function trimAddFormInputs() {
            ['addFullName', 'addPhone', 'addEmail'].forEach(id => {
                const node = document.getElementById(id);
                if (node && typeof node.value === 'string') {
                    node.value = node.value.trim();
                }
            });
        }

        function trimEditFormInputs() {
            ['editFullName', 'editPhone', 'editEmail'].forEach(id => {
                const node = document.getElementById(id);
                if (node && typeof node.value === 'string') {
                    node.value = node.value.trim();
                }
            });
        }

        function resendPasswordFromEditModal() {
            const userId = document.getElementById('editUserId').value;
            if (!userId) return;
            const message = 'Gửi lại mật khẩu tạm qua email cho tài khoản này?';
            const submitResendForm = function() {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = 'patient-accounts';
                form.innerHTML = '<input type="hidden" name="action" value="resendPassword"><input type="hidden" name="userId" value="' + userId + '">';
                document.body.appendChild(form);
                form.submit();
            };

            if (typeof showConfirm === 'function') {
                showConfirm(message, submitResendForm);
                return;
            }
            if (!confirm(message)) return;
            submitResendForm();
        }

        function toggleStatus(userId, name) {
            if (!canManagePatients) return;
            const message = 'Thay đổi trạng thái của ' + name + '?';
            const submitToggleForm = function() {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = 'patient-accounts';
                form.innerHTML = '<input type="hidden" name="action" value="toggleStatus"><input type="hidden" name="userId" value="' + userId + '">';
                document.body.appendChild(form);
                form.submit();
            };

            if (typeof showConfirm === 'function') {
                showConfirm(message, submitToggleForm);
                return;
            }
            if (!confirm(message)) return;
            submitToggleForm();
        }

        function openModal(modalId) { document.getElementById(modalId).style.display = 'block'; }
        function closeModal(modalId) { document.getElementById(modalId).style.display = 'none'; }
        function filterUsers() {
            const status = userStatusFilter.value;
            const keyword = userSearch.value.trim();
            let url = 'patient-accounts?action=filter&page=1&status=' + status;
            if (keyword) url += '&keyword=' + encodeURIComponent(keyword);
            window.location.href = url;
        }
        function searchUsers() {
            const status = userStatusFilter.value;
            const keyword = userSearch.value.trim();
            if (!keyword) { filterUsers(); return; }
            let url = 'patient-accounts?action=search&page=1&keyword=' + encodeURIComponent(keyword);
            if (status !== 'all') url += '&status=' + status;
            window.location.href = url;
        }
        function resetUserFilter() {
            userSearch.value = '';
            userStatusFilter.value = 'all';
            window.location.href = 'patient-accounts';
        }

        window.onclick = function(event) {
            document.querySelectorAll('.modal').forEach(modal => {
                if (event.target === modal) modal.style.display = 'none';
            });
        };

        document.addEventListener('DOMContentLoaded', function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                setTimeout(() => {
                    alert.style.animation = 'slideIn 0.3s ease-out reverse';
                    setTimeout(() => alert.remove(), 300);
                }, 5000);
            });

            userSearch.addEventListener('keypress', function(e) { if (e.key === 'Enter') searchUsers(); });
            userStatusFilter.addEventListener('change', filterUsers);
            if (canManagePatients) {
                document.getElementById('addAccountForm').addEventListener('submit', trimAddFormInputs);
                document.getElementById('editAccountForm').addEventListener('submit', trimEditFormInputs);
            }
            if ('${addModalOpen}' === 'true' && canManagePatients) {
                openModal('addAccountModal');
            }
            if ('${editModalOpen}' === 'true' && canManagePatients) {
                openEditModal('${editUserId}', '${editFullName}', '${editPhone}', '${editEmail}', '${editResendAvailable}');
            }
        });
    </script>
</body>
</html>
