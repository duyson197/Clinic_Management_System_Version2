<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Thanh toán thất bại</title>

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
                            <div class="circle">✕</div>
                            <p>Hoàn tất</p>
                        </div>

                    </div>

                    <!-- FAIL CARD -->
                    <div class="card-boxs fail-card">

                        <div class="fail-icon">
                            ✕
                        </div>

                        <h2>Thanh toán thất bại</h2>

                        <p class="fail-text">
                            Giao dịch của bạn không thành công. <br>
                            Vui lòng kiểm tra lại thông tin hoặc thử lại sau.
                        </p>
                      
                        <a class="actions"
                           href="${pageContext.request.contextPath}/appointmentservlet?doctor=${doctorID}&patientid=${patientID}">
                            Đăng kí lại 
                        </a>
                        
                        <a href="${pageContext.request.contextPath}/index.jsp"
                           class="btn-primary">
                            Về trang chủ
                        </a>
                    </div>

                </div>

            </div>

        </div>


        <jsp:include page="/common/footer.jsp" />

    </body>
</html>