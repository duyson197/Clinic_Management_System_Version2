<%-- 
    Document   : HistoryOfAppointment
    Created on : Mar 3, 2026, 11:55:52 PM
    Author     : Admin
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Các cuộc hẹn</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/pages/profile/historyOfAppointment/historyOfAppointment.css">
        <style>
            .appointment-card.highlight-appointment {
                border: 2px solid #2563eb;
                box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.15);
            }
        </style>
    </head>
    <body>

        <jsp:include page="/common/header.jsp" />

        <div class="page">

            <h3>Lịch sử các cuộc hẹn</h3>
            <p>Chi tiết các cuộc hẹn</p>
            <div>
                <div class="status-filter">

                    <a href="#" onclick="filterStatus('booked')">Đã đặt</a>
                    <a href="#" onclick="filterStatus('checked_in')">Đã check-in</a>
                    <a href="#" onclick="filterStatus('waiting')">Đang chờ</a>
                    <a href="#" onclick="filterStatus('completed')">Hoàn thành</a>
                    <a href="#" onclick="filterStatus('cancelled')">Đã hủy</a>
                    <a href="#" onclick="filterStatus('all')">Tất cả</a>
                </div>

                <div class="name-filter">
                    <select id="nameSelect" onchange="filterByName()">
                        <option value="all">Tất cả bệnh nhân</option>

                        <c:forEach var="a" items="${appointmentList}">
                            <option value="${a.fullName}">
                                ${a.fullName}
                            </option>
                        </c:forEach>

                    </select>
                </div>
            </div>
            <br>
            <!-- ================= PROFILE HEADER ================= -->
            <div class="appointment-list">

                <c:forEach var="a" items="${appointmentList}">

                    <div class="appointment-card"
                         onclick="openModal(this)"
                         data-appointment-id="${a.appointmentId}"
                         data-name="${a.fullName}"
                         data-phone="${a.phone}"
                         data-email="${a.email}"
                         data-date="${a.appointmentDate}"
                         data-time="${a.appointmentTime}"
                         data-status="${a.status}"
                         data-symptom="${a.symptom}"
                         data-price="${a.price}"
                         data-doctor="${a.specialization}"
                         data-qualification="${a.qualification}"
                         >

                        <div class="doctor-info">
                            <img src="${a.image}" class="doctor-img">

                            <div>
                                <h3>Bác sĩ: ${a.doctorName}</h3>
                                <p>Bằng cấp: ${a.qualification}</p>
                                <p class="price">${a.price} đ</p>
                            </div>
                        </div>

                        <div class="appointment-meta">
                            <p><strong>Ngày:</strong> ${a.appointmentDate}</p>
                            <p><strong>Giờ:</strong> ${a.appointmentTime}</p>
                        </div>

                        <div class="card-actions">

                            <button class="status-btn">${a.status}</button>

                            <c:if test="${a.status == 'booked'}">
                                <form action="historyofappointmentservlet" method="post">
                                    <input type="hidden" name="id" value="${a.appointmentId}">
                                    <button class="cancel-btn" 
                                            name="status" 
                                            value="cancelled">
                                        Hủy lịch
                                    </button>

                                </form>
                            </c:if>
                            <c:if test="${a.status == 'completed'}">
                                <form action="ratingdoctorservlet" >
                                    <input type="hidden" name="id" value="${a.doctorId}">
                                    <input type="hidden" name="appointmentId" value="${a.appointmentId}">

                                    <button class="cancel-btn" >

                                        Đánh giá
                                    </button>

                                </form>
                            </c:if>


                        </div>

                    </div>

                </c:forEach>

            </div>

            <div class="modal-overlay" id="modal">

                <div class="modal">

                    <div class="modal-header">
                        <h2>Chi tiết cuộc hẹn</h2>
                        <span class="close" onclick="closeModal()">✕</span>
                    </div>

                    <div class="modal-body">

                        <h3>Thông tin bệnh nhân</h3>
                        <p><strong>Họ tên:</strong> <span id="mName"></span></p>
                        <p><strong>SĐT:</strong> <span id="mPhone"></span></p>
                        <p><strong>Email:</strong> <span id="mEmail"></span></p>
                        
                        
                        <hr>

                        <h3>Thông tin cuộc hẹn</h3>
                        <p><strong>Ngày:</strong> <span id="mDate"></span></p>
                        <p><strong>Giờ:</strong> <span id="mTime"></span></p>
                        <p><strong>Dịch vụ:</strong> <span id="mDoctor"></span></p>
                        <p><strong>Ghi chú:</strong> <span id="mSymptom"></span></p>

                        <hr>

                        <h3>Tổng chi phí</h3>
                        <p class="total-price"><span id="mPrice"></span> đ</p>

                    </div>

                </div>

            </div>
        </div>
        <jsp:include page="/common/footer.jsp" />


        <script>
    let currentStatus = "booked";
    let currentName = "all";

    function openModal(card) {
        document.getElementById("modal").style.display = "flex";

        document.getElementById("mName").innerText = card.dataset.name;
        document.getElementById("mPhone").innerText = card.dataset.phone;
        document.getElementById("mEmail").innerText = card.dataset.email;
        document.getElementById("mDate").innerText = card.dataset.date;
        document.getElementById("mTime").innerText = card.dataset.time;
        document.getElementById("mDoctor").innerText = card.dataset.doctor;
        document.getElementById("mSymptom").innerText = card.dataset.symptom;
        document.getElementById("mPrice").innerText = card.dataset.price;
    }

    function closeModal() {
        document.getElementById("modal").style.display = "none";
    }

    function filterStatus(status) {
        currentStatus = status;
        applyFilter();
    }

    function filterByName() {
        currentName = document.getElementById("nameSelect").value;
        applyFilter();
    }

    function applyFilter() {
        let cards = document.querySelectorAll(".appointment-card");

        cards.forEach(card => {
            let cardStatus = card.dataset.status;
            let cardName = card.dataset.name;

            let matchStatus = (currentStatus === "all" || cardStatus === currentStatus);
            let matchName = (currentName === "all" || cardName === currentName);

            if (matchStatus && matchName) {
                card.style.display = "flex";
            } else {
                card.style.display = "none";
            }
        });
    }

    window.onload = function () {
        const highlightedId = '${highlightedAppointmentId}';

        if (highlightedId && highlightedId !== 'null') {
            let found = false;

            document.querySelectorAll(".appointment-card").forEach(card => {
                if (card.dataset.appointmentId === highlightedId) {
                    found = true;

                    currentStatus = "all";
                    currentName = "all";

                    card.style.display = "flex";
                    card.classList.add("highlight-appointment");

                    card.scrollIntoView({behavior: "smooth", block: "center"});
                    openModal(card);
                } else {
                    card.style.display = "none";
                    card.classList.remove("highlight-appointment");
                }
            });

            if (!found) {
                applyFilter();
            }
        } else {
            applyFilter(); 
        }
    };
</script>

    </body>
</html>
