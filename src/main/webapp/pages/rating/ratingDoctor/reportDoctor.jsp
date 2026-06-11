<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/rating/ratingDoctor/reportDoctor.css">
        <title>Đánh giá bác sĩ</title>
    </head>
    <body>
        <jsp:include page="/common/header.jsp" />

        <div class="page">
            <div class="content">
                <div class="main">
                    <form method="post" action="${pageContext.request.contextPath}/ratingdoctorservlet">

                        <div class="card-box">
                            <h3>Đánh giá bác sĩ</h3>

                            <input type="hidden" name="doctorID" value="${doctor.doctorId}">
                            <input type="hidden" name="appointmentId" value="${appointmentID}">

                            <c:forEach var="q" items="${list}">
                                <c:if test="${q.id != 5}">
                                    <div class="question">
                                        <p>${q.question_text}</p>
                                        <select name="rating_${q.id}" required>
                                            <option value="">Chọn đánh giá</option>
                                            <option value="1">1 ⭐</option>
                                            <option value="2">2 ⭐</option>
                                            <option value="3">3 ⭐</option>
                                            <option value="4">4 ⭐</option>
                                            <option value="5">5 ⭐</option>
                                        </select>
                                    </div>
                                </c:if>
                            </c:forEach>

                            <c:forEach var="q" items="${list}">
                                <c:if test="${q.id == 5}">
                                    <div class="question">
                                        <p>${q.question_text}</p>
                                        <textarea name="note_${q.id}" placeholder="Nhập ghi chú..." required></textarea>
                                    </div>
                                </c:if>
                           </c:forEach>
                        </div>

                        <div class="actions">
                            <button type="button" class="btn-outline"
                                    onclick="location.href = '${pageContext.request.contextPath}/historyofappointment'">
                                Quay lại
                            </button>
                            <button type="submit" class="btn-primary">
                                Gửi đánh giá
                            </button>
                        </div>

                    </form>
                </div>
                <div class="card">
                    <img src="${doctor.getImage()}" alt="Doctor">

                    <h3>${doctor.getFullName()}</h3>
                    <p class="degree">${doctor.getQualification()}</p>
                    <p class="desc">${doctor.getSpecialization()}</p>
                    <p class="desc">${doctor.getClinic_address()}</p>
                    <br>
                    <div class="info">
                        <span>⏱ ${doctor.getExperience_years()} năm</span>
                        <span>⭐ ${doctor.getRating()}</span>
                    </div>
                    <br>



                </div>

            </div>
        </div>

        <jsp:include page="/common/footer.jsp" />
    </body>
</html>