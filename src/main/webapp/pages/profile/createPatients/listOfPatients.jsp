<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Patient List</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/pages/profile/createPatients/listOfPatients.css">

    </head>

    <body>

        <jsp:include page="/common/header.jsp" />

        <div class="patient-page">

            <div class="patient-container">

                <!-- header -->
                <div class="patient-header">

                    <h2 class="patient-title">
                        Danh sách bệnh nhân của ${sessionScope.account.fullName}
                    </h2>


                    <a href="${pageContext.request.contextPath}/createpatientsservlet?DoctorID=${DoctorID}&action=create"
                       class="btn-add-patiaent">
                        +
                    </a>
                </div>
                <div class="patient-filter">
                    <input type="text" id="searchPatient"
                           placeholder="Tìm theo tên bệnh nhân...">
                </div>

                <!-- bảng danh sách -->
                <div class="patient-table-wrapper">

                    <table class="patient-table">

                        <thead>
                            <tr>
                                <th class="col-name">Họ tên</th>
                                <th class="col-dob">Ngày sinh</th>
                                <th class="col-gender">Giới tính</th>
                                <th class="col-phone">Điện thoại</th>
                                <th></th>
                                <th></th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:forEach items="${patientList}" var="p">

                                <tr class="patient-row">

                                    <td class="patient-name">
                                        ${p.fullName}
                                    </td>

                                    <td class="patient-dob">
                                        <fmt:formatDate value="${p.dob}" pattern="dd/MM/yyyy"/>
                                    </td>

                                    <td class="patient-gender">
                                        ${p.gender}
                                    </td>

                                    <td class="patient-phone">
                                        ${p.phone}
                                    </td>

                                    <td class="patient-action">

                                        <a class="btn-edit"
                                           href="${pageContext.request.contextPath}/createpatientsservlet?DoctorID=${DoctorID}&action=edit&id=${p.patientId}">
                                            Sửa
                                        </a>

                                    </td>
                                    <c:if test="${DoctorID != null}">
                                        <td class="patient-action">

                                            <a class="btn-edit"
                                               href="${pageContext.request.contextPath}/appointmentservlet?doctor=${DoctorID}&patientid=${p.patientId}">
                                                Chọn
                                            </a>

                                        </td>
                                    </c:if>



                                </tr>

                            </c:forEach>

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
        <script>
            const searchInput = document.getElementById("searchPatient");

            searchInput.addEventListener("keyup", function () {
                let keyword = this.value.toLowerCase();

                let rows = document.querySelectorAll(".patient-row");

                rows.forEach(row => {
                    let name = row.querySelector(".patient-name").innerText.toLowerCase();

                    if (name.includes(keyword)) {
                        row.style.display = "";
                    } else {
                        row.style.display = "none";
                    }
                });
            });
        </script>
        <jsp:include page="/common/footer.jsp" />

    </body>
</html>