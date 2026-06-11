<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page import="dal.DoctorDAO"%>

<%
    
    DoctorDAO dao = new DoctorDAO();
    request.setAttribute("homeDoctors", dao.getTopRatedDoctors(3));
%>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Phòng Khám ABC - Hệ Thống Y Tế Hàng Đầu</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
        <style>
            :root {
                --primary: #0061ff;
                --dark: #1e293b;
                --bg: #f8fafc;
            }
            body {
                font-family: 'Inter', 'Segoe UI', sans-serif;
                background-color: var(--bg);
                margin: 0;
                color: var(--dark);
            }
            .container {
                max-width: 1100px;
                margin: 0 auto;
                padding: 0 20px;
            }

            /* HERO SECTION */
            .hero {
                background: linear-gradient(135deg, #ffffff 0%, #e0f2fe 100%);
                padding: 100px 0;
                display: flex;
                align-items: center;
            }
            .hero-flex {
                display: flex;
                align-items: center;
                gap: 60px;
            }
            .hero-text {
                flex: 1.2;
            }
            .hero-title {
                font-size: 56px;
                font-weight: 800;
                line-height: 1.1;
                margin-bottom: 24px;
                color: var(--primary);
            }
            .hero-title span {
                color: var(--dark);
            }
            .hero-sub {
                font-size: 19px;
                color: #475569;
                margin-bottom: 40px;
            }

            .btn-main {
                background: var(--primary);
                color: white;
                padding: 18px 36px;
                border-radius: 50px;
                font-weight: 700;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                gap: 12px;
                box-shadow: 0 10px 25px rgba(0, 97, 255, 0.3);
                transition: 0.4s;
            }
            .btn-main:hover {
                transform: translateY(-3px);
            }

            /* DOCTOR SECTION */
            .doctor-section {
                padding: 80px 0;
            }
            .section-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 50px;
            }

            .doctor-card {
                background: white;
                border-radius: 24px;
                padding: 30px;
                margin-bottom: 30px;
                display: flex;
                gap: 40px;
                align-items: center;
                transition: 0.4s;
                border: 1px solid #f1f5f9;
                box-shadow: 0 4px 6px rgba(0,0,0,0.05);
            }
            .doctor-card:hover {
                transform: scale(1.02);
                border-color: var(--primary);
            }

            .doc-img {
                width: 160px;
                height: 160px;
                border-radius: 24px;
                object-fit: cover;
            }
            .doc-info {
                flex: 1;
            }
            .doc-name {
                font-size: 24px;
                font-weight: 800;
                margin: 0;
            }
            .doc-spec {
                color: var(--primary);
                font-weight: 700;
                display: block;
                margin-bottom: 10px;
            }
            .doc-desc {
                color: #64748b;
                font-size: 14px;
                margin-bottom: 15px;
            }

            .btn-book {
                background: #f1f5f9;
                color: var(--dark);
                padding: 12px 24px;
                border-radius: 12px;
                text-decoration: none;
                font-weight: 700;
            }
            .btn-book:hover {
                background: var(--primary);
                color: white;
            }
        </style>
    </head>
    <body>
        <jsp:include page="common/header.jsp" />

        <section class="hero">
            <div class="container hero-flex">
                <div class="hero-text">
                    <h1 class="hero-title"><span>Sức Khỏe Của Bạn,</span><br>Trách Nhiệm Của Chúng Tôi.</h1>
                    <p class="hero-sub">Trải nghiệm dịch vụ y tế đẳng cấp với đội ngũ bác sĩ hàng đầu.</p>
                    <a href="${pageContext.request.contextPath}/listofdoctorservlet" class="btn-main">Đặt lịch ngay</a>
                </div>
                <div style="flex: 1;">
                    <img src="https://img.freepik.com/free-photo/doctor-working-laptop-medical-office_23-2148980721.jpg" 
                         style="width: 100%; border-radius: 40px;" alt="Medical Team">
                </div>
            </div>
        </section>

        <section class="doctor-section">
            <div class="container">
                <div class="section-header">
                    <h2>Chuyên Gia Ưu Tú</h2>
                    <a href="${pageContext.request.contextPath}/listofdoctorservlet" style="color: var(--primary); text-decoration: none; font-weight: 700;">Xem tất cả ></a>
                </div>

               <c:forEach items="${homeDoctors}" var="doctor">
    <div class="doctor-card">
        <div class="doc-avatar-wrap">
            <img src="${fn:startsWith(doctor.image, 'http') ? doctor.image : pageContext.request.contextPath.concat(doctor.image)}" 
                 class="doc-img" onerror="this.src='https://cdn-icons-png.flaticon.com/512/3774/3774299.png'">
            <div class="badge-exp">${doctor.exp}+ Năm KN</div>
        </div>

        <div class="doc-info">
            <span class="doc-spec">${doctor.specialization}</span>
            <h3 class="doc-name">${doctor.fullName}</h3>
            <p class="doc-desc">
                Trình độ chuyên môn: <strong>${doctor.qualification}</strong>. 
                Với kinh nghiệm dày dặn, bác sĩ luôn được bệnh nhân đánh giá cao về phác đồ điều trị và thái độ chăm sóc tận tình.
            </p>

            <div class="doc-meta">
                <div class="meta-item">
                    <i class="fas fa-star" style="color: #f59e0b;"></i> ${doctor.rating} / 5.0
                </div>
            </div>
        </div>

        <div>
            <a href="${pageContext.request.contextPath}/${empty sessionScope.account ? 'pages/auth/login.jsp' : 'listofdoctorservlet'}" class="btn-book">Hẹn gặp bác sĩ</a>
        </div>
    </div>
</c:forEach>
            </div>
        </section>

        <jsp:include page="common/footer.jsp" />
    </body>
</html>