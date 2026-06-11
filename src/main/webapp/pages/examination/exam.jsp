<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Khám bệnh</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/examination/exam.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />
        <div class="exam-container">
            <c:if test="${not empty pageError}">
                <div class="alert-error">${pageError}</div>
                <div class="actions">
                    <button class="btn-outline" type="button" onclick="location.href = '${pageContext.request.contextPath}/doctorDashboard'">← Quay lại danh sách</button>
                </div>
            </c:if>

            <c:if test="${empty pageError}">
                <form id="examForm" method="post" action="${pageContext.request.contextPath}/doctor/exam">
                    <input type="hidden" name="appointmentId" value="${examData.appointmentId}">

                    <div class="exam-header">
                        <div>
                            <p class="kicker">Hồ sơ khám</p>
                            <h2>Phiên khám bệnh #${examData.queuePosition} - ${examData.patientName}</h2>
                        </div>
                        <div class="actions">
                            <button class="btn-outline" type="button" onclick="location.href = '${pageContext.request.contextPath}/doctorDashboard'">← Quay lại</button>
                            <button class="btn-primary" type="submit" name="action" value="save">💾 Lưu tạm</button>
                            <button id="finishExamBtn" class="btn-success" type="submit" name="action" value="finish">✔ Hoàn thành</button>
                        </div>
                    </div>

                    <c:if test="${success == 'saved'}">
                        <div class="alert-success">Đã lưu thông tin bệnh án.</div>
                    </c:if>
                    <c:if test="${success == 'examFinished'}">
                        <div class="alert-success">Đã hoàn tất bệnh nhân trước đó. Hệ thống đã chuyển sang bệnh nhân kế tiếp trong hàng đợi.</div>
                    </c:if>

                    <c:if test="${error == 'saveFailed'}">
                        <div class="alert-error">Không thể lưu hồ sơ khám. Vui lòng thử lại.</div>
                    </c:if>
                    <c:if test="${error == 'missingRequiredFinishFields'}">
                        <div class="alert-error">Cần nhập đầy đủ Chẩn đoán, Kết quả khám lâm sàng và Phương án điều trị trước khi hoàn thành phiên khám.</div>
                    </c:if>
                    <c:if test="${error == 'missingRequiredLabFields'}">
                        <div class="alert-error">Cần nhập Chẩn đoán, Kết quả khám lâm sàng và đầy đủ thông tin chỉ định xét nghiệm trước khi tạo yêu cầu.</div>
                    </c:if>
                    <c:if test="${success == 'labRequestedMultiple'}">
                        <div class="alert-success">Đã gửi các yêu cầu xét nghiệm và chuyển bệnh nhân sang hàng đợi xét nghiệm.</div>
                    </c:if>
                    <c:if test="${error == 'labRequestFailed'}">
                        <div class="alert-error">Không thể tạo yêu cầu xét nghiệm. Có thể lịch khám này đã có phiếu xét nghiệm.</div>
                    </c:if>
                    <c:if test="${error == 'labRequestNotAllowed'}">
                        <div class="alert-error">Phiên khám đã hoàn tất, không thể tạo thêm yêu cầu xét nghiệm.</div>
                    </c:if>                     
                    <c:if test="${success == 'prescriptionSaved'}">
                        <div class="alert-success">Đã lưu đơn thuốc thành công.</div>
                    </c:if>
                    <c:if test="${error == 'emptyPrescription'}">
                        <div class="alert-error">Vui lòng chọn ít nhất một thuốc để lưu đơn.</div>
                    </c:if>
                    <c:if test="${error == 'savePrescriptionFailed'}">
                        <div class="alert-error">Không thể lưu đơn thuốc. Vui lòng thử lại.</div>
                    </c:if>
                    <c:if test="${error == 'incompleteLabResults'}">
                        <div class="alert-error">Chỉ có thể lưu đơn thuốc khi tất cả yêu cầu xét nghiệm đã có kết quả.</div>
                    </c:if>

                    <div class="tabs">
                        <button class="tab ${activeTab == 'info' || empty activeTab ? 'active' : ''}" data-target="info" type="button" onclick="showTab('info')">Thông tin</button>
                        <button class="tab ${activeTab == 'lab' ? 'active' : ''}" data-target="lab" type="button" onclick="showTab('lab')">Kết quả XN</button>
                        <button class="tab ${activeTab == 'prescription' ? 'active' : ''}" data-target="prescription" type="button" onclick="showTab('prescription')">Đơn thuốc</button>
                        <button class="tab ${activeTab == 'history' ? 'active' : ''}" data-target="history" type="button" onclick="showTab('history')">Lịch sử</button>
                    </div>

                    <div class="tab-content-shell">
                        <div class="tab-content ${activeTab == 'info' || empty activeTab ? 'active' : ''}" id="info">
                            <div class="card-grid">
                                <section class="card">
                                    <h3>Thông tin bệnh nhân</h3>
                                    <div class="grid">
                                        <div>
                                            <label>Họ tên</label>
                                            <input value="${examData.patientName}" readonly>
                                        </div>
                                        <div>
                                            <label>Giới tính</label>
                                            <input value="${examData.gender}" readonly>
                                        </div>
                                        <div>
                                            <label>Ngày sinh</label>
                                            <input value="${examData.dob}" readonly>
                                        </div>
                                        <div>
                                            <label>Trạng thái khám</label>
                                            <input value="${examData.status}" readonly>
                                        </div>
                                    </div>

                                    <label>Triệu chứng ban đầu (đặt lịch)</label>
                                    <textarea rows="3" readonly>${examData.symptom}</textarea>

                                    <label>Triệu chứng hiện tại</label>
                                    <textarea rows="3" name="symptoms" placeholder="Mô tả triệu chứng hiện tại của bệnh nhân..."><c:out value="${not empty formSymptoms ? formSymptoms : (medicalRecord != null ? medicalRecord.symptoms : examData.symptom)}"/></textarea>

                                    <label>Chẩn đoán</label>
                                    <textarea rows="3" name="diagnosis" placeholder="Nhập chẩn đoán lâm sàng..."><c:out value="${not empty formDiagnosis ? formDiagnosis : (medicalRecord != null ? medicalRecord.diagnosis : '')}"/></textarea>
                                </section>

                                <section class="card">
                                    <h3>Tiền sử bệnh</h3>
                                    <label>Dị ứng</label>
                                    <textarea rows="2" name="historyAllergies" placeholder="Liệt kê các dị ứng đã biết"><c:out value="${historyAllergies}"/></textarea>

                                    <label>Bệnh mãn tính</label>
                                    <textarea rows="2" name="historyChronic" placeholder="Liệt kê bệnh mãn tính"><c:out value="${historyChronic}"/></textarea>

                                    <label>Tiền sử gia đình</label>
                                    <textarea rows="2" name="historyFamily" placeholder="Tiền sử bệnh gia đình liên quan"><c:out value="${historyFamily}"/></textarea>

                                    <label>Tiền sử xã hội</label>
                                    <textarea rows="2" name="historySocial" placeholder="Thói quen sinh hoạt, hút thuốc, rượu bia..."><c:out value="${historySocial}"/></textarea>

                                    <label>Lịch sử tiêm chủng</label>
                                    <textarea rows="2" name="historyVaccination" placeholder="Các mũi tiêm và thời điểm"><c:out value="${historyVaccination}"/></textarea>
                                </section>
                            </div>

                            <section class="card section-spacing">
                                <h3>Kết quả khám lâm sàng</h3>
                                <label>Kết quả khám</label>
                                <textarea rows="4" name="clinicalResult" placeholder="Ghi chép quan sát và kết quả khám lâm sàng..."><c:out value="${clinicalResult}"/></textarea>

                                <label>Ghi chú của bác sĩ</label>
                                <textarea rows="4" name="doctorNote" placeholder="Ghi chú và dặn dò bổ sung..."><c:out value="${doctorNote}"/></textarea>

                                <label>Phương án điều trị</label>
                                <textarea rows="4" name="treatmentPlan" placeholder="Mô tả phương án điều trị đề xuất..."><c:out value="${treatmentPlan}"/></textarea>
                            </section>
                        </div>

                        <div class="tab-content ${activeTab == 'lab' ? 'active' : ''}" id="lab">
                            <c:if test="${examData.status != 'done'}">
                                <div class="card">
                                    <h3>Tạo yêu cầu xét nghiệm</h3>
                                    <div id="labRequestList" class="rx-list">
                                        <c:choose>
                                            <c:when test="${not empty labRequestDrafts}">
                                                <c:forEach var="draft" items="${labRequestDrafts}" varStatus="status">
                                                    <div class="rx-row lab-row">
                                                        <select name="labTestType">
                                                            <option value="">Chọn loại xét nghiệm</option>
                                                            <option value="Công thức máu" ${draft.testType == 'Công thức máu' ? 'selected' : ''}>Công thức máu</option>
                                                            <option value="Đường huyết" ${draft.testType == 'Đường huyết' ? 'selected' : ''}>Đường huyết</option>
                                                            <option value="Sinh hóa máu" ${draft.testType == 'Sinh hóa máu' ? 'selected' : ''}>Sinh hóa máu</option>
                                                            <option value="Nước tiểu" ${draft.testType == 'Nước tiểu' ? 'selected' : ''}>Nước tiểu</option>
                                                            <option value="X-quang" ${draft.testType == 'X-quang' ? 'selected' : ''}>X-quang</option>
                                                        </select>
                                                        <select name="labPriority">
                                                            <option value="Bình thường" ${draft.priority == 'Bình thường' ? 'selected' : ''}>Bình thường</option>
                                                            <option value="Khẩn" ${draft.priority == 'Khẩn' ? 'selected' : ''}>Khẩn</option>
                                                        </select>
                                                        <select name="labCollectionMethod">
                                                            <option value="Lấy mẫu tại chỗ" ${draft.collectionMethod == 'Lấy mẫu tại chỗ' ? 'selected' : ''}>Lấy mẫu tại chỗ</option>
                                                            <option value="Nhịn ăn trước xét nghiệm" ${draft.collectionMethod == 'Nhịn ăn trước xét nghiệm' ? 'selected' : ''}>Nhịn ăn trước xét nghiệm</option>
                                                            <option value="Theo hướng dẫn bác sĩ" ${draft.collectionMethod == 'Theo hướng dẫn bác sĩ' ? 'selected' : ''}>Theo hướng dẫn bác sĩ</option>
                                                        </select>
                                                        <input name="labRequestItemNote" placeholder="Ghi chú từng yêu cầu (tuỳ chọn)" value="${fn:escapeXml(draft.note)}">
                                                        <button type="button" class="btn-outline btn-remove-lab" onclick="removeLabRow(this)" ${fn:length(labRequestDrafts) == 1 ? 'disabled' : ''}>Xoá</button>
                                                    </div>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="rx-row lab-row">
                                                    <select name="labTestType">
                                                        <option value="">Chọn loại xét nghiệm</option>
                                                        <option value="Công thức máu">Công thức máu</option>
                                                        <option value="Đường huyết">Đường huyết</option>
                                                        <option value="Sinh hóa máu">Sinh hóa máu</option>
                                                        <option value="Nước tiểu">Nước tiểu</option>
                                                        <option value="X-quang">X-quang</option>
                                                    </select>
                                                    <select name="labPriority">
                                                        <option value="Bình thường">Bình thường</option>
                                                        <option value="Khẩn">Khẩn</option>
                                                    </select>
                                                    <select name="labCollectionMethod">
                                                        <option value="Lấy mẫu tại chỗ">Lấy mẫu tại chỗ</option>
                                                        <option value="Nhịn ăn trước xét nghiệm">Nhịn ăn trước xét nghiệm</option>
                                                        <option value="Theo hướng dẫn bác sĩ">Theo hướng dẫn bác sĩ</option>
                                                    </select>
                                                    <input name="labRequestItemNote" placeholder="Ghi chú từng yêu cầu (tuỳ chọn)">
                                                    <button type="button" class="btn-outline btn-remove-lab" onclick="removeLabRow(this)" disabled>Xoá</button>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="actions">
                                        <button type="button" class="btn-outline" onclick="addLabRow()">+ Thêm yêu cầu xét nghiệm</button>
                                        <button id="createLabRequestBtn" type="submit" class="btn-primary" name="action" value="createLabRequest">🧪 Gửi yêu cầu xét nghiệm</button>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${examData.status == 'done'}">
                                <div class="empty-state">Phiên khám đã hoàn tất, không thể tạo yêu cầu xét nghiệm.</div>
                            </c:if>

                            <div class="section-spacing"></div>

                            <c:if test="${empty labResults}">
                                <p>Chưa có kết quả xét nghiệm cho lịch khám này.</p>
                            </c:if>

                            <c:forEach var="lab" items="${labResults}">
                                <div class="card lab-item">
                                    <div class="lab-head">
                                        <h4>Phiếu xét nghiệm #${lab.requestId}</h4>
                                        <c:set var="labStatusText" value="${lab.status == 'pending' ? 'Chờ xử lý' : (lab.status == 'processing' ? 'Đang xét nghiệm' : (lab.status == 'completed' ? 'Hoàn tất' : lab.status))}" />
                                        <span class="status-pill ${lab.status}">${labStatusText}</span>
                                    </div>
                                    <div class="lab-meta-grid">
                                        <div class="meta-item">
                                            <span class="meta-label">Thời gian chỉ định</span>
                                            <span class="meta-value">${empty lab.requestedAt ? '---' : lab.requestedAt}</span>
                                        </div>
                                        <div class="meta-item">
                                            <span class="meta-label">Hoàn tất</span>
                                            <span class="meta-value">${empty lab.completedAt ? '---' : lab.completedAt}</span>
                                        </div>
                                        <div class="meta-item meta-item-full">
                                            <span class="meta-label">Ghi chú kỹ thuật</span>
                                            <p class="meta-note">${empty lab.notes ? 'Không có ghi chú.' : lab.notes}</p>
                                        </div>
                                        <div class="meta-item meta-item-full">
                                            <span class="meta-label">File kết quả</span>
                                            <c:choose>
                                                <c:when test="${not empty lab.resultFile}">
                                                    <a class="lab-link" href="${pageContext.request.contextPath}/${lab.resultFile}" target="_blank">📄 Xem file kết quả</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="meta-value">Chưa có file</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="tab-content ${activeTab == 'prescription' ? 'active' : ''}" id="prescription">
                            <div class="card">
                                <h3>Đơn thuốc</h3>
                                <c:if test="${not canSavePrescription}">
                                    <div class="alert-error">Đang có yêu cầu xét nghiệm chưa hoàn tất. Vui lòng đợi có đủ kết quả trước khi lưu đơn thuốc.</div>
                                </c:if>
                                <!--                            <label>Ghi chú đơn thuốc</label>
                                                            <textarea rows="2" name="prescriptionNote" placeholder="Lưu ý chung cho bệnh nhân khi dùng thuốc..."></textarea>-->

                                <div id="rxList" class="rx-list">
                                    <c:if test="${not empty prescriptionItems}">
                                        <c:forEach var="item" items="${prescriptionItems}">
                                            <div class="rx-row">
                                                <select name="medicineId" onchange="syncMedicineName(this)">
                                                    <option value="">Chọn thuốc</option>
                                                    <c:if test="${item.medicineId <= 0 && not empty item.medicineName}">
                                                        <option value="" selected>${item.medicineName} (không còn trong danh mục)</option>
                                                    </c:if>
                                                    <c:forEach var="m" items="${medicineList}">
                                                        <option value="${m.medicineId}" ${m.medicineId == item.medicineId ? 'selected' : ''}>${m.medicineName}${empty m.unit ? '' : ' ('}${empty m.unit ? '' : m.unit}${empty m.unit ? '' : ')'}</option>
                                                    </c:forEach>
                                                </select>
                                                <input type="hidden" name="medicineName" value="${item.medicineName}">
                                                <input name="dosage" placeholder="Liều dùng" value="${item.dosage}">
                                                <input name="frequency" placeholder="Số lần/ngày" value="${item.frequency}">
                                                <input name="durationDays" placeholder="Số ngày" value="${item.durationDays}">
                                            </div>
                                        </c:forEach>
                                    </c:if>
                                </div>

                                <c:if test="${empty prescriptionItems}">
                                    <p id="rxEmptyHint" class="muted">Chưa có thuốc trong đơn. Bấm <strong>+ Thêm thuốc</strong> để bắt đầu kê toa.</p>
                                </c:if>
                                <c:if test="${not empty prescriptionItems}">
                                    <p id="rxEmptyHint" class="muted" style="display:none;">Chưa có thuốc trong đơn. Bấm <strong>+ Thêm thuốc</strong> để bắt đầu kê toa.</p>
                                </c:if>
                                <div class="actions">
                                    <button type="button" class="btn-outline" onclick="addRxRow()">+ Thêm thuốc</button>
                                    <button type="submit" class="btn-primary" name="action" value="savePrescription" ${canSavePrescription ? '' : 'disabled'}>💊 Lưu đơn thuốc</button>
                                </div>
                            </div>
                        </div>

                        <div class="tab-content ${activeTab == 'history' ? 'active' : ''}" id="history">
                            <c:if test="${empty historyList}">
                                <p>Chưa có lịch sử khám trước đó của bệnh nhân.</p>
                            </c:if>

                            <c:if test="${not empty historyList}">
                                <div class="history-table-wrap">
                                    <table class="history-table">
                                        <thead>
                                            <tr>
                                                <th>Mã lịch khám</th>
                                                <th>Ngày khám</th>
                                                <th>Giờ khám</th>
                                                <th>Triệu chứng</th>
                                                <th>Chẩn đoán</th>
                                                <th>Ghi chú</th>
                                                <th>Trạng thái lịch</th>
                                                <th>Trạng thái khám</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="h" items="${historyList}">
                                                <tr>
                                                    <td><span class="history-id">#${h.appointmentId}</span></td>
                                                    <td><fmt:formatDate value="${h.appointmentDate}" pattern="dd/MM/yyyy" /></td>
                                                    <td>${h.appointmentTime}</td>
                                                    <td><div class="history-cell-text">${empty h.symptom ? '---' : h.symptom}</div></td>
                                                    <td><div class="history-cell-text">${empty h.diagnosis ? '---' : h.diagnosis}</div></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${empty h.notes}">
                                                                <div class="history-note history-note-empty">Không có ghi chú.</div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="history-note-wrapper">
                                                                    <button
                                                                        class="history-note-toggle"
                                                                        type="button"
                                                                        aria-expanded="false"
                                                                        onclick="toggleHistoryNote(this)">
                                                                        <span class="history-note-toggle-label">Xem đầy đủ</span>
                                                                    </button>
                                                                    <div class="history-note" title="${h.notes}">
                                                                        ${h.notes}
                                                                    </div>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <span class="status-pill history-status ${h.appointmentStatus}">
                                                            ${empty h.appointmentStatus ? '---' : h.appointmentStatus}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <span class="status-pill history-status ${h.queueStatus}">
                                                            ${empty h.queueStatus ? 'N/A' : h.queueStatus}
                                                        </span>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </form>
            </c:if>
        </div>

        <template id="rxTemplate">
            <select name="medicineId" onchange="syncMedicineName(this)">
                <option value="">Chọn thuốc</option>
                <c:forEach var="m" items="${medicineList}">
                    <option value="${m.medicineId}">${m.medicineName}${empty m.unit ? '' : ' ('}${empty m.unit ? '' : m.unit}${empty m.unit ? '' : ')'}</option>
                </c:forEach>
            </select>
            <input type="hidden" name="medicineName" value="">
            <input name="dosage" placeholder="Liều dùng">
            <input name="frequency" placeholder="Số lần/ngày">
            <input name="durationDays" placeholder="Số ngày">
        </template>
        <template id="labTemplate">
            <select name="labTestType">
                <option value="">Chọn loại xét nghiệm</option>
                <option value="Công thức máu">Công thức máu</option>
                <option value="Đường huyết">Đường huyết</option>
                <option value="Sinh hóa máu">Sinh hóa máu</option>
                <option value="Nước tiểu">Nước tiểu</option>
                <option value="X-quang">X-quang</option>
            </select>
            <select name="labPriority">
                <option value="Bình thường">Bình thường</option>
                <option value="Khẩn">Khẩn</option>
            </select>
            <select name="labCollectionMethod">
                <option value="Lấy mẫu tại chỗ">Lấy mẫu tại chỗ</option>
                <option value="Nhịn ăn trước xét nghiệm">Nhịn ăn trước xét nghiệm</option>
                <option value="Theo hướng dẫn bác sĩ">Theo hướng dẫn bác sĩ</option>
            </select>
            <input name="labRequestItemNote" placeholder="Ghi chú từng yêu cầu (tuỳ chọn)">
            <button type="button" class="btn-outline btn-remove-lab" onclick="removeLabRow(this)">Xoá</button>
        </template>

        <jsp:include page="/common/modal-alert.jsp" />
        
        <script>
            function showTab(id) {
                document.querySelectorAll('.tab').forEach(t => {
                    t.classList.toggle('active', t.dataset.target === id);
                });
                document.querySelectorAll('.tab-content').forEach(c => {
                    c.classList.toggle('active', c.id === id);
                });
            }

            function toggleHistoryNote(button) {
                const noteWrapper = button.closest('.history-note-wrapper');
                if (!noteWrapper) {
                    return;
                }

                const expanded = noteWrapper.classList.toggle('expanded');
                button.setAttribute('aria-expanded', expanded ? 'true' : 'false');

                const label = button.querySelector('.history-note-toggle-label');
                if (label) {
                    label.textContent = expanded ? 'Thu gọn' : 'Xem đầy đủ';
                }
            }

            function syncMedicineName(selectElement) {
                const row = selectElement.closest('.rx-row');
                if (!row) {
                    return;
                }
                const hidden = row.querySelector('input[name="medicineName"]');
                if (!hidden) {
                    return;
                }

                const selectedOption = selectElement.options[selectElement.selectedIndex];
                if (selectedOption && selectElement.value) {
                    hidden.value = selectedOption.textContent.trim();
                    return;
                }

                if (!hidden.value) {
                    hidden.value = '';
                }
            }

            function addRxRow() {
                const wrap = document.getElementById('rxList');
                const row = document.createElement('div');
                row.className = 'rx-row';
                row.innerHTML = document.getElementById('rxTemplate').innerHTML;
                wrap.appendChild(row);
                const emptyHint = document.getElementById('rxEmptyHint');
                if (emptyHint) {
                    emptyHint.style.display = 'none';
                }
            }

            function addLabRow() {
                const wrap = document.getElementById('labRequestList');
                if (!wrap) {
                    return;
                }
                const row = document.createElement('div');
                row.className = 'rx-row lab-row';
                row.innerHTML = document.getElementById('labTemplate').innerHTML;
                wrap.appendChild(row);
                syncLabRemoveButtons();
            }

            function removeLabRow(button) {
                const wrap = document.getElementById('labRequestList');
                const row = button ? button.closest('.lab-row') : null;
                if (!wrap || !row) {
                    return;
                }
                row.remove();
                syncLabRemoveButtons();
            }

            function syncLabRemoveButtons() {
                const rows = document.querySelectorAll('#labRequestList .lab-row');
                rows.forEach((row, index) => {
                    const btn = row.querySelector('.btn-remove-lab');
                    if (!btn) {
                        return;
                    }
                    btn.disabled = rows.length <= 1;
                    if (rows.length <= 1 && index === 0) {
                        btn.setAttribute('title', 'Cần ít nhất 1 yêu cầu');
                    } else {
                        btn.removeAttribute('title');
                    }
                });
            }

            document.addEventListener('DOMContentLoaded', () => {
                document.querySelectorAll('#rxList select[name="medicineId"]').forEach(syncMedicineName);
                syncLabRemoveButtons();
                
                const examForm = document.getElementById('examForm');
                const createLabRequestBtn = document.getElementById('createLabRequestBtn');
                const finishExamBtn = document.getElementById('finishExamBtn');

                if (examForm) {
                    examForm.addEventListener('submit', function (event) {
                        const submitter = event.submitter;
                        if (!submitter || (submitter.id !== 'createLabRequestBtn' && submitter.id !== 'finishExamBtn')) {
                            return;
                        }

                        if (submitter.dataset.confirmed === 'true') {
                            delete submitter.dataset.confirmed;
                            return;
                        }

                        event.preventDefault();
                        if (typeof showConfirm !== 'function') {
                            const fallbackAccepted = confirm(
                                    submitter.id === 'createLabRequestBtn'
                                    ? 'Xác nhận gửi yêu cầu xét nghiệm cho bệnh nhân này?'
                                    : 'Xác nhận hoàn tất phiên khám này?'
                                    );
                            if (fallbackAccepted) {
                                submitter.dataset.confirmed = 'true';
                                submitter.click();
                            }
                            return;
                        }

                        const message = submitter.id === 'createLabRequestBtn'
                                ? 'Xác nhận gửi yêu cầu xét nghiệm cho bệnh nhân này?'
                                : 'Xác nhận hoàn tất phiên khám này?';
                        showConfirm(message, function () {
                            submitter.dataset.confirmed = 'true';
                            submitter.click();
                        });
                    });
                }
            });
        </script>
        <jsp:include page="/common/footer.jsp" />
    </body>
</html>