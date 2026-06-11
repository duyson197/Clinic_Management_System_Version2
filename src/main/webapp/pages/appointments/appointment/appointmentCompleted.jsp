<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/pages/appointments/appointment/appointment.css">
    </head>
    <body>

        <jsp:include page="/common/header.jsp" />

        <div class="page">
            <div class="content">

                <div class="main">

                    <!-- STEPS -->
                    <div class="steps-wrapper">

                        <div class="step active">
                            <div class="circle">✓</div>
                            <p>Xác nhận và chọn ca</p>
                        </div>

                        <div class="line"></div>

                        <div class="step active">
                            <div class="circle">✓</div>
                            <p>Thanh toán</p>
                        </div>

                        <div class="line"></div>

                        <div class="step active">
                            <div class="circle">✓</div>
                            <p>Hoàn tất</p>
                        </div>

                    </div>
                    <div class="card-boxs">
                        <h3>Cảm ơn đã đặt lịch hẹn</h3>
                         <c:set var="displayPatientName" value="${not empty requestScope.bookedPatientName ? requestScope.bookedPatientName : param.patientName}" />

                        <div class="success-wrapper">
                            <div class="success-icon-box">
                                <img src="https://tse1.mm.bing.net/th/id/OIP.dSGCRzF6aLogIpu-UJt7gAHaF4?pid=Api&h=220&P=0"
                                     alt="Thành công"
                                     class="success-icon">
                            </div>
                        </div>

                        <label style="color: red">Cuộc hẹn của bạn đã được đặt thành công !!!</label>
                        <br>
                        <c:if test="${not empty displayPatientName}">
                            <label style="color: red">Bệnh nhân: ${displayPatientName}</label>
                            <br>
                        </c:if>
                        <label style="color: red">Vui lòng đến cơ sở khám đúng thời gian</label>

                        <div class="actions">
                            <a href="${pageContext.request.contextPath}/index.jsp" class="btn-primary">
                                Về trang chủ
                            </a>
                        </div>
                    </div>

                </div>



            </div>

            <jsp:include page="/common/footer.jsp" />

    </body>
</html>