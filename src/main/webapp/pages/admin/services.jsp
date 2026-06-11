<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý Giá Dịch vụ</title>
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

            .alert {
                padding: 15px 20px;
                border-radius: 8px;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
                animation: slideIn 0.3s ease-out;
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
                    transform: translateY(-20px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            .table-container {
                background: white;
                padding: 25px;
                border-radius: 10px;
                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                overflow-x: auto;
            }

            .table-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
                padding-bottom: 4px;
            }

            .table-header h3 {
                font-size: 18px;
                color: #333;
            }

            .toolbar {
                background: white;
                padding: 20px;
                border-radius: 10px;
                margin-bottom: 20px;
                display: grid;
                gap: 12px;
                align-items: end;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            }

            .toolbar-service {
                grid-template-columns: minmax(320px, 2.2fr) minmax(240px, 1.6fr) minmax(260px, 1.2fr);
            }

            .search-box {
                min-width: 0;
            }

            .search-box label {
                display: block;
                font-weight: 600;
                margin-bottom: 8px;
                color: #333;
                font-size: 13px;
            }

            .search-box input {
                width: 100%;
                padding: 10px 15px;
                border: 1px solid #ddd;
                border-radius: 6px;
                font-size: 14px;
                transition: all 0.3s ease;
            }

            .search-box input:focus {
                outline: none;
                border-color: #0061ff;
                box-shadow: 0 0 0 3px rgba(0, 97, 255, 0.1);
            }

            .filter-box {
                min-width: 0;
            }

            .filter-box label {
                display: block;
                font-weight: 600;
                margin-bottom: 8px;
                color: #333;
                font-size: 13px;
            }

            .filter-box select {
                width: 100%;
                padding: 10px 15px;
                border: 1px solid #ddd;
                border-radius: 6px;
                font-size: 14px;
                cursor: pointer;
                background: white;
                transition: all 0.3s ease;
            }

            .filter-box select:focus {
                outline: none;
                border-color: #0061ff;
                box-shadow: 0 0 0 3px rgba(0, 97, 255, 0.1);
            }

            .toolbar-buttons {
                display: flex;
                gap: 10px;
                align-self: end;
                width: 100%;
            }

            .btn-search, .btn-reset, .btn-add {
                padding: 10px 16px;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                font-size: 14px;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 6px;
                text-decoration: none;
            }

            .toolbar-buttons .btn-search,
            .toolbar-buttons .btn-reset {
                flex: 1 1 0;
            }

            .btn-search {
                background: #0061ff;
                color: white;
            }

            .btn-search:hover {
                background: #0052cc;
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 97, 255, 0.3);
            }

            .btn-reset {
                background: #f0f0f0;
                color: #333;
            }

            .btn-reset:hover {
                background: #e0e0e0;
            }

            .btn-add {
                background: #4caf50;
                color: white;
            }

            .btn-add:hover {
                background: #45a049;
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
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

            @media (max-width: 768px) {
                .container {
                    padding: 20px;
                }

                .toolbar {
                    grid-template-columns: 1fr;
                }

                .search-box,
                .filter-box {
                    width: 100%;
                    min-width: unset;
                }

                .toolbar-buttons {
                    justify-content: stretch;
                    width: 100%;
                }

                .toolbar-buttons .btn-search,
                .toolbar-buttons .btn-reset,
                .toolbar-buttons .btn-add {
                    flex: 1;
                    justify-content: center;
                }
            }

            table {
                width: 100%;
                border-collapse: collapse;
            }

            th {
                background: linear-gradient(135deg, #f8f9fa 0%, #f0f0f0 100%);
                padding: 15px;
                text-align: left;
                font-weight: 600;
                color: #333;
                border-bottom: 2px solid #e0e0e0;
            }

            td {
                padding: 15px;
                border-bottom: 1px solid #f0f0f0;
                color: #555;
            }

            tr:hover {
                background: #f9f9f9;
            }

            .action-buttons {
                display: flex;
                flex-wrap: wrap;
            }

            .btn-action {
                border: none;
                background: none;
                cursor: pointer;
                font-size: 16px;
                padding: 6px 10px;
                border-radius: 4px;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                gap: 4px;
            }

            .btn-edit {
                color: #1976d2;
            }

            .btn-edit:hover {
                background: #e3f2fd;
            }

            .btn-delete {
                color: #d32f2f;
            }

            .btn-delete:hover {
                background: #ffebee;
            }

            .no-data {
                text-align: center;
                padding: 30px;
                color: #999;
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
                from {
                    opacity: 0;
                }
                to {
                    opacity: 1;
                }
            }

            .modal-content {
                background-color: white;
                margin: 5% auto;
                padding: 30px;
                border-radius: 10px;
                width: 90%;
                max-width: 550px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
                animation: slideUp 0.3s ease;
            }

            @keyframes slideUp {
                from {
                    transform: translateY(50px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            .modal-header {
                font-size: 20px;
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
                font-size: 24px;
                line-height: 1;
                background: none;
                border: none;
                color: #999;
                transition: all 0.3s ease;
            }

            .modal-close:hover {
                color: #333;
            }

            .form-group {
                margin-bottom: 18px;
            }

            .form-group label {
                display: block;
                font-weight: 600;
                margin-bottom: 8px;
                color: #333;
                font-size: 14px;
            }

            .form-group input,
            .form-group select {
                width: 100%;
                padding: 10px 15px;
                border: 1px solid #ddd;
                border-radius: 6px;
                font-size: 14px;
                font-family: inherit;
                transition: all 0.3s ease;
            }

            .form-group input:focus,
            .form-group select:focus {
                outline: none;
                border-color: #0061ff;
                box-shadow: 0 0 0 3px rgba(0, 97, 255, 0.1);
            }

            .field-error {
                margin-top: 6px;
                color: #dc3545;
                font-size: 13px;
                font-weight: 600;
            }

            .field-input-error {
                border-color: #dc3545 !important;
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

            .btn-submit {
                padding: 10px 20px;
                background: #0061ff;
                color: white;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                gap: 6px;
            }

            .btn-submit:hover {
                background: #0052cc;
            }
        </style>
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />

        <div class="container">
            <!-- Thông báo thành công -->
            <c:if test="${not empty success}">
                <div class="alert success">
                    <i class="fas fa-check-circle"></i>
                    ${success}
                </div>
            </c:if>

            <!-- Thông báo lỗi -->
            <c:if test="${not empty error and not addModalOpen and not editModalOpen}">
                <div class="alert error">
                    <i class="fas fa-exclamation-circle"></i>
                    ${error}
                </div>
            </c:if>

            <div class="table-container">
                <div class="table-header">
                    <h3><i class="fas fa-list"></i> Danh sách Dịch vụ</h3>
                    <button class="btn-add" onclick="openAddModal()">
                        <i class="fas fa-plus"></i> Thêm Dịch vụ
                    </button>
                </div>

                <!-- Search & Filter -->
                <div class="toolbar toolbar-service">
                    <form method="GET" action="${pageContext.request.contextPath}/admin-services" style="display: contents;">
                            <div class="search-box">
                                <label><i class="fas fa-search"></i> Tìm kiếm</label>
                                <input type="text" name="search" value="${searchKeyword}" placeholder="Tên dịch vụ..." onkeypress="if(event.keyCode==13) this.form.submit()">
                            </div>
                            <div class="filter-box">
                                <label><i class="fas fa-filter"></i> Danh mục</label>
                                <select name="category" onchange="this.form.submit()">
                                    <option value="all" ${filterCategory == 'all' ? 'selected' : ''}>-- Tất cả --</option>
                                    <option value="booking_fee" ${filterCategory == 'booking_fee' ? 'selected' : ''}>Khám & tư vấn</option>
                                    <option value="lab" ${filterCategory == 'lab' ? 'selected' : ''}>Kiểm tra chuyên sâu</option>
                                </select>
                            </div>
                            <input type="hidden" name="page" value="1">
                            <div class="toolbar-buttons">
                                    <button type="submit" class="btn-search">
                                        <i class="fas fa-search"></i> Tìm
                                    </button>
                                    <a href="${pageContext.request.contextPath}/admin-services" class="btn-reset">
                                        <i class="fas fa-redo"></i> Đặt lại
                                    </a>
                            </div>
                    </form>
                </div>

                <!-- Danh sách dịch vụ -->
                <c:choose>
                    <c:when test="${not empty services}">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 40%;">Tên Dịch vụ</th>
                                    <th style="width: 30%;">Danh mục</th>
                                    <th style="width: 20%;">Giá (VNĐ)</th>
                                    <th style="width: 10%;">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="service" items="${servicesPaged}">
                                    <c:set var="displayType" value="${service.serviceType}"/>
                                    <c:if test="${service.serviceType eq 'booking_fee'}">
                                        <c:set var="displayType" value="Khám & tư vấn"/>
                                    </c:if>
                                    <c:if test="${service.serviceType eq 'lab'}">
                                        <c:set var="displayType" value="Kiểm tra chuyên sâu"/>
                                    </c:if>
                                    <tr>
                                        <td><strong>${service.name}</strong></td>
                                        <td>${displayType}</td>
                                        <td><fmt:formatNumber value="${service.price}" type="number" maxFractionDigits="0"/> đ</td>
                                        <td>
                                            <div class="action-buttons">
                                                <button class="btn-action btn-edit" onclick="openEditModal(${service.serviceId}, &quot;${service.name}&quot;, &quot;${service.serviceType}&quot;, &quot;<fmt:formatNumber value='${service.price}' type='number' groupingUsed='false' maxFractionDigits='0'/>&quot;)" title="Chỉnh sửa">
                                                    <i class="fas fa-pen-to-square"></i>
                                                </button>
                                                <form method="POST" action="${pageContext.request.contextPath}/admin-services" style="display: inline;">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="serviceId" value="${service.serviceId}">
                                                    <input type="hidden" name="filterSearch" value="${searchKeyword}">
                                                    <input type="hidden" name="filterCategory" value="${filterCategory}">
                                                    <input type="hidden" name="filterPage" value="${currentPage}">
                                                    <button type="button" class="btn-action btn-delete" title="Xóa" onclick="confirmDeleteService(this)">
                                                        <i class="fas fa-trash"></i>
                                                    </button>
                                                </form>
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
                                <c:url var="prevUrl" value="/admin-services">
                                    <c:param name="search" value="${searchKeyword}" />
                                    <c:param name="category" value="${filterCategory}" />
                                    <c:param name="page" value="${currentPage - 1}" />
                                </c:url>
                                <c:url var="nextUrl" value="/admin-services">
                                    <c:param name="search" value="${searchKeyword}" />
                                    <c:param name="category" value="${filterCategory}" />
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
                                    <c:url var="pageUrl" value="/admin-services">
                                        <c:param name="search" value="${searchKeyword}" />
                                        <c:param name="category" value="${filterCategory}" />
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
                    </c:when>
                    <c:otherwise>
                        <div class="no-data">
                            <i class="fas fa-box"></i>
                            <p>Chưa có dịch vụ nào</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- MODAL THÊM DỊCH VỤ -->
        <div id="addModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <i class="fas fa-plus-circle"></i>
                    <span>Thêm Dịch vụ Mới</span>
                    <button class="modal-close" onclick="closeAddModal()" type="button">×</button>
                </div>

                <form method="POST" action="${pageContext.request.contextPath}/admin-services" novalidate>
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="filterSearch" value="${searchKeyword}">
                    <input type="hidden" name="filterCategory" value="${filterCategory}">
                    <input type="hidden" name="filterPage" value="${currentPage}">

                    <c:if test="${not empty error and addModalOpen}">
                        <div class="alert error" style="margin-bottom: 12px;">
                            <i class="fas fa-exclamation-circle"></i>
                            ${error}
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label>Tên dịch vụ <span style="color: red;">*</span></label>
                        <input type="text" name="name" class="${not empty addNameError ? 'field-input-error' : ''}" placeholder="Nhập tên dịch vụ" value="${addName}">
                        <c:if test="${not empty addNameError}">
                            <div class="field-error">${addNameError}</div>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label>Danh mục <span style="color: red;">*</span></label>
                        <select name="serviceType" class="${not empty addServiceTypeError ? 'field-input-error' : ''}">
                            <option value="" ${empty addServiceType ? 'selected' : ''}>-- Chọn danh mục --</option>
                            <option value="booking_fee" ${addServiceType == 'booking_fee' ? 'selected' : ''}>Khám & tư vấn</option>
                            <option value="lab" ${addServiceType == 'lab' ? 'selected' : ''}>Kiểm tra chuyên sâu</option>
                        </select>
                        <c:if test="${not empty addServiceTypeError}">
                            <div class="field-error">${addServiceTypeError}</div>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label>Giá (VNĐ) <span style="color: red;">*</span></label>
                        <input type="number" name="price" min="0" class="${not empty addPriceError ? 'field-input-error' : ''}" placeholder="0" value="${addPrice}">
                        <c:if test="${not empty addPriceError}">
                            <div class="field-error">${addPriceError}</div>
                        </c:if>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn-cancel" onclick="closeAddModal()">
                            <i class="fas fa-times"></i> Hủy
                        </button>
                        <button type="submit" class="btn-submit">
                            <i class="fas fa-save"></i> Thêm dịch vụ
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- MODAL CHỈNH SỬA DỊCH VỤ -->
        <div id="editModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <i class="fas fa-pen-to-square"></i>
                    <span>Chỉnh sửa Dịch vụ</span>
                    <button class="modal-close" onclick="closeEditModal()" type="button">×</button>
                </div>

                <form method="POST" action="${pageContext.request.contextPath}/admin-services" novalidate>
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="serviceId" id="editServiceId" value="${editServiceId}">
                    <input type="hidden" name="filterSearch" value="${searchKeyword}">
                    <input type="hidden" name="filterCategory" value="${filterCategory}">
                    <input type="hidden" name="filterPage" value="${currentPage}">

                    <c:if test="${not empty error and editModalOpen}">
                        <div class="alert error" style="margin-bottom: 12px;">
                            <i class="fas fa-exclamation-circle"></i>
                            ${error}
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label>Tên dịch vụ <span style="color: red;">*</span></label>
                        <input type="text" name="name" id="editServiceName" class="${not empty editNameError ? 'field-input-error' : ''}" placeholder="Nhập tên dịch vụ" value="${editName}">
                        <c:if test="${not empty editNameError}">
                            <div class="field-error">${editNameError}</div>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label>Danh mục <span style="color: red;">*</span></label>
                        <select name="serviceType" id="editServiceType" class="${not empty editServiceTypeError ? 'field-input-error' : ''}">
                            <option value="" ${empty editServiceType ? 'selected' : ''}>-- Chọn danh mục --</option>
                            <option value="booking_fee" ${editServiceType == 'booking_fee' ? 'selected' : ''}>Khám & tư vấn</option>
                            <option value="lab" ${editServiceType == 'lab' ? 'selected' : ''}>Kiểm tra chuyên sâu</option>
                        </select>
                        <c:if test="${not empty editServiceTypeError}">
                            <div class="field-error">${editServiceTypeError}</div>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label>Giá (VNĐ) <span style="color: red;">*</span></label>
                        <input type="number" name="price" id="editServicePrice" min="0" class="${not empty editPriceError ? 'field-input-error' : ''}" placeholder="0" value="${editPrice}">
                        <c:if test="${not empty editPriceError}">
                            <div class="field-error">${editPriceError}</div>
                        </c:if>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn-cancel" onclick="closeEditModal()">
                            <i class="fas fa-times"></i> Hủy
                        </button>
                        <button type="submit" class="btn-submit">
                            <i class="fas fa-save"></i> Lưu thay đổi
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="../../common/footer.jsp" />
        <jsp:include page="../../common/modal-alert.jsp" />
                
        <script>
            // Mở modal thêm
            function openAddModal() {
                document.getElementById('addModal').style.display = 'block';
            }

            function confirmDeleteService(button) {
                const form = button ? button.closest('form') : null;
                if (!form) return;
                const message = 'Bạn chắc chắn muốn xóa dịch vụ này?';
                if (typeof showConfirm === 'function') {
                    showConfirm(message, function() { form.submit(); });
                    return;
                }
                if (confirm(message)) {
                    form.submit();
                }
            }

            // Đóng modal thêm
            function closeAddModal() {
                document.getElementById('addModal').style.display = 'none';
            }

            // Mở modal chỉnh sửa
            function openEditModal(serviceId, name, serviceType, price) {
                document.getElementById('editServiceId').value = serviceId;
                document.getElementById('editServiceName').value = name;
                document.getElementById('editServiceType').value = serviceType;
                document.getElementById('editServicePrice').value = String(price).replace(/\.0+$/, '');
                document.getElementById('editModal').style.display = 'block';
            }

            // Đóng modal chỉnh sửa
            function closeEditModal() {
                document.getElementById('editModal').style.display = 'none';
            }

            // Đóng modal khi click bên ngoài
            window.onclick = function(event) {
                const addModal = document.getElementById('addModal');
                const editModal = document.getElementById('editModal');
                if (event.target === addModal) {
                    addModal.style.display = 'none';
                }
                if (event.target === editModal) {
                    editModal.style.display = 'none';
                }
            }

            // Tự động đóng thông báo sau 5 giây
            document.addEventListener('DOMContentLoaded', function() {
                const alerts = document.querySelectorAll('.alert');
                alerts.forEach(alert => {
                    setTimeout(() => {
                        alert.style.animation = 'slideIn 0.3s ease-out reverse';
                        setTimeout(() => alert.remove(), 300);
                    }, 5000);
                });

                const shouldOpenAddModal = '${addModalOpen}' === 'true';
                const shouldOpenEditModal = '${editModalOpen}' === 'true';
                if (shouldOpenAddModal) {
                    openAddModal();
                }
                if (shouldOpenEditModal) {
                    document.getElementById('editModal').style.display = 'block';
                }
            });
        </script>
    </body>
</html>



