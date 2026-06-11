<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Đánh giá bác sĩ</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/rating/ListRatingOfDoctor/ListOfRating.css">
    </head>

    <body>
        <jsp:include page="/common/header.jsp" />

        <div class="container">

            <!-- ===== LEFT ===== -->
            <div class="doctor-list">

                <h2 class="page-title">Đánh giá bác sĩ</h2>

                <!-- ===== RATING GRID ===== -->
                <div class="rating-grid">

                    <c:forEach var="q" items="${questions}">
                        <c:if test="${q.id != 5}">
                            <div class="rating-card">

                                <h3>${q.question_text}</h3>

                                <p class="stars">
                                    ⭐ <fmt:formatNumber value="${q.avgRating}" maxFractionDigits="1"/> / 5
                                </p>

                                <p class="desc">
                                    ${q.totalReviews} lượt đánh giá
                                </p>

                            </div>
                        </c:if>
                    </c:forEach>

                </div>

                <div class="note-section">

                    <h3 class="note-title">Nhận xét về bác sĩ</h3>

                    <c:choose>
                        <c:when test="${empty notes}">
                            <p class="desc">Chưa có nhận xét nào</p>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="n" items="${notes}">
                                <div class="note-item">

                                    <div class="note-avatar">
                                        ${n.userName.charAt(0)}
                                    </div>

                                    <div class="note-content">
                                        <strong>${n.userName}</strong>
                                        <p>${n.note}</p>
                                    </div>
                                    
                                    <c:if test="${user.userId == n.user_id || roleName == 'admin'}">
                                        <div class="note-actions">

                                            <c:if test="${user.userId == n.user_id}">
                                                <form method="get" action="${pageContext.request.contextPath}/ratingdoctorservlet">
                                                    <input type="hidden" name="appointmentId" value="${n.appointment_id}">
                                                    <input type="hidden" name="id" value="${doctor.doctorId}"> 
                                                    <button class="btn-edit">Sửa</button>
                                                </form>
                                            </c:if>

                                            <form method="post" action="${pageContext.request.contextPath}/listofrating"
                                                  onsubmit="return confirm('Bạn chắc chắn muốn xóa?')">
                                                <input type="hidden" name="appointmentId" value="${n.appointment_id}">
                                                <input type="hidden" name="doctorID" value="${doctor.doctorId}"> 
                                                <input type="hidden" name="action" value="delete">
                                                <button class="btn-delete">Xóa</button>
                                            </form>

                                        </div>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                </div>

            </div>

            <!-- ===== RIGHT ===== -->
            <div class="filter">

                <h3>Thông tin bác sĩ</h3>

                <div class="card">

                    <img src="${doctor.image}" alt="Doctor">

                    <h3>${doctor.fullName}</h3>

                    <p class="degree">${doctor.specialization}</p>

                    <div class="info">
                        <span>⏱ ${doctor.experience_years} năm</span>
                        <span>⭐ ${doctor.rating}</span>
                    </div>

                    <p class="price">
                        <fmt:formatNumber value="${doctor.price}" type="number"/>đ
                    </p>

                    <!-- Đặt dịch vụ -->
                    <form method="get" action="${pageContext.request.contextPath}/createpatientsservlet">
                        <button class="btn-primary" name="btnDoctorID" value="${doctor.doctorId}">
                            Đặt dịch vụ
                        </button>
                    </form>

                    <!-- Quay lại -->
                    <form method="get" action="${pageContext.request.contextPath}/listofdoctorservlet">
                        <button class="btn-primary">
                            Quay lại danh sách bác sĩ
                        </button>
                    </form>

                </div>

            </div>

        </div>
        <br>
        <br>
        <jsp:include page="/common/footer.jsp" />
    </body>
</html>