<%-- 
    Document   : appointmentSecond
    Created on : Feb 3, 2026, 2:00:08 PM
    Author     : Admin
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>List of appointment</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/pages/appointments/listOfAppointment/listOfAppointment.css">
    </head>
    <body>

        <jsp:include page="/common/header.jsp" />

        <div class="container">

            <div class="header">

                <div>
                    <h2>Danh sách cuộc hẹn</h2>
                    <p>Quản lý trạng thái các cuộc hẹn</p>
                </div>

                <div class="total">
                    Tổng: ${list.size()} cuộc hẹn
                </div>

            </div>


            <div class="filter-bar">

                <select id="statusFilter">

                    <option value="all">Tất cả</option>
                    <option value="booked">Booked</option>
                    <option value="checked_in">Checked in</option>
                    <option value="waiting">Waiting</option>
                    <option value="completed">Completed</option>
                    <option value="cancelled">Cancelled</option>

                </select>

                <select id="dateFilter">
                    <option value="today">Hôm nay</option>
                    <option value="all">Tất cả thời gian</option>
                    <option value="month">Tháng này</option>
                    <option value="year">Năm nay</option>
                </select>
                <input type="text" id="searchBox"
                       placeholder="Tìm theo tên bệnh nhân / mã hẹn">

                <button onclick="clearFilter()" class="btn-clear">
                    Xóa bộ lọc
                </button>

            </div>


            <div class="stats">

                <span class="badge blue" id="appointmentCount">${list.size()} cuộc</span>
            </div>


            <table id="appointmentTable">

                <thead>
                    <tr>

                        <th>Mã hẹn</th>
                        <th>Bệnh nhân</th>
                        <th>Bác sĩ</th>
                        <th>Booking</th>
                        <th>Ngày</th>
                        <th>Giờ</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>

                    </tr>
                </thead>

                <tbody>

                    <c:forEach items="${list}" var="a">

                        <tr data-status="${a.status}">

                            <td>AP-${a.appointmentId}</td>

                            <td>
                                ${a.fullName}
                                <br>
                                <span class="sub">${a.phone}</span>
                            </td>

                            <td>${a.doctorName}</td>

                            <td>
                                <span class="booking ${a.bookingType}">
                                    ${a.bookingType}
                                </span>
                            </td>

                            <td>${a.appointmentDate}</td>

                            <td>${a.appointmentTime}</td>

                            <td>

                                <span class="status ${a.status}">
                                    ${a.status}
                                </span>

                            </td>

                            <td>
                                <jsp:useBean id="now" class="java.util.Date" />
                                <fmt:formatDate value="${now}" pattern="yyyy-MM-dd" var="today"/>
                                <fmt:formatDate value="${now}" pattern="HH:mm" var="currentTime"/>

                                <c:if test="${a.status eq 'booked' and a.appointmentDate eq today}">
                                    <c:if test="${(a.appointmentTime eq '07:00:00' and currentTime ge '07:00' and currentTime le '11:30')
                                                  or (a.appointmentTime eq '13:00:00' and currentTime ge '13:00' and currentTime le '16:30')}">
                                          <form action="listofappointment" method="post">
                                              <input type="hidden" name="id"     value="${a.appointmentId}">
                                              <input type="hidden" name="status" value="checked_in">
                                              <button class="btn-update">Check in</button>
                                          </form>
                                    </c:if>
                                </c:if>
                            </td>

                        </tr>

                    </c:forEach>

                </tbody>

            </table>

        </div>

        <script>
            const dateFilter = document.getElementById("dateFilter");
            const statusFilter = document.getElementById("statusFilter");
            const searchBox = document.getElementById("searchBox");

            window.onload = function () {
                statusFilter.value = "booked";
                dateFilter.value = "today";
                filterTable();
            };

            dateFilter.addEventListener("change", filterTable);
            statusFilter.addEventListener("change", filterTable);
            searchBox.addEventListener("keyup", filterTable);

            function filterTable() {

                let status = statusFilter.value;
                let keyword = searchBox.value.toLowerCase();
                let dateType = dateFilter.value;

                let today = new Date();

                let rows = document.querySelectorAll("#appointmentTable tbody tr");

                rows.forEach(row => {

                    let rowStatus = row.getAttribute("data-status");
                    let text = row.innerText.toLowerCase();

                    let dateCell = row.children[4].innerText;
                    let rowDate = new Date(dateCell);

                    let show = true;

                    if (status !== "all" && rowStatus !== status) {
                        show = false;
                    }

                    if (!text.includes(keyword)) {
                        show = false;
                    }

                    if (dateType === "today") {
                        if (rowDate.toDateString() !== today.toDateString()) {
                            show = false;
                        }
                    }

                    if (dateType === "month") {
                        if (rowDate.getMonth() !== today.getMonth() ||
                                rowDate.getFullYear() !== today.getFullYear()) {
                            show = false;
                        }
                    }

                    if (dateType === "year") {
                        if (rowDate.getFullYear() !== today.getFullYear()) {
                            show = false;
                        }
                    }

                    row.style.display = show ? "" : "none";

                });
                updateCount();

            }

            function clearFilter() {
                statusFilter.value = "booked";
                dateFilter.value = "today";
                searchBox.value = "";

                filterTable();
            }
            function updateCount() {
                let rows = document.querySelectorAll("#appointmentTable tbody tr");
                let count = 0;

                rows.forEach(row => {
                    if (row.style.display !== "none") {
                        count++;
                    }
                });

                document.getElementById("appointmentCount").innerText = count + " cuộc";
            }
        </script>
        <jsp:include page="/common/footer.jsp" />

    </body>
</html>