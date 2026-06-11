<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đăng nhập - Phòng khám ABC</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

        <style>
            :root {
                --primary: #0061ff;
                --bg: #f4f7fe;
            }
            body {
                font-family: 'Segoe UI', sans-serif;
                margin: 0;
                background: var(--bg);
            }
            .container {
                display: flex;
                height: 100vh;
            }

            .auth-form-side {
                flex: 1;
                padding: 0 100px;
                background: white;
                display: flex;
                flex-direction: column;
                justify-content: center;
                position: relative; 
            }
            .logo-area {
                text-align: center;
                margin-bottom: 30px;
            }
            .logo-area i {
                font-size: 40px;
                color: var(--primary);
                margin-bottom: 10px;
            }

            .role-switch {
                display: flex;
                background: #f0f2f5;
                padding: 4px;
                border-radius: 8px;
                margin-bottom: 25px;
            }

            .role-switch button {
                flex: 1;
                padding: 10px;
                border: none;
                background: transparent;
                cursor: pointer;
                border-radius: 6px;
                font-weight: 600;
                color: #666;
                transition: all 0.3s ease;
            }

            .role-switch button.active {
                background: white;
                color: var(--primary);
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }

            .form-group {
                margin-bottom: 15px;
            }
            .form-group label {
                display: block;
                font-weight: 600;
                margin-bottom: 8px;
                font-size: 14px;
                color: #333;
            }
            .form-group input {
                width: 100%;
                padding: 12px;
                border: 1px solid #ddd;
                border-radius: 8px;
                box-sizing: border-box;
                outline: none;
                transition: 0.3s;
            }
            .form-group input:focus {
                border-color: var(--primary);
            }

            .options {
                display: flex;
                justify-content: space-between;
                font-size: 13px;
                margin-bottom: 25px;
                color: #555;
            }
            .options a {
                color: var(--primary);
                text-decoration: none;
            }

            .btn-submit {
                width: 100%;
                padding: 12px;
                background: var(--primary);
                color: white;
                border: none;
                border-radius: 8px;
                font-weight: bold;
                cursor: pointer;
                font-size: 16px;
            }
            .btn-submit:hover {
                background: #0052d6;
            }

            .footer-link {
                text-align: center;
                margin-top: 20px;
                font-size: 14px;
            }
            .footer-link a {
                color: var(--primary);
                text-decoration: none;
                font-weight: bold;
            }

            .auth-banner-side {
                flex: 1;
                background: linear-gradient(135deg, #e0f2ff 0%, #ffffff 100%);
                display: flex;
                align-items: center;
                justify-content: center;
                text-align: center;
                padding: 50px;
            }
            .banner-content img {
                width: 100%;
                max-width: 400px;
                border-radius: 20px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.1);
                margin-bottom: 20px;
            }
            .banner-content h3 {
                color: #333;
                margin-bottom: 10px;
            }
            .banner-content p {
                color: #666;
                line-height: 1.5;
            }
            .btn-back-home {
                position: absolute;
                top: 25px;          
                left: 30px;         
                text-decoration: none;
                color: #666;
                font-size: 14px;
                font-weight: 500;
                display: flex;
                align-items: center;
                gap: 8px; 
                transition: 0.3s;
            }
            
            .btn-back-home:hover {
                color: var(--primary); 
                transform: translateX(-3px); 
            }
       </style>
    </head>
    <body>
        <div class="container">
            <div class="auth-form-side">
                
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn-back-home">
                    <i class="fas fa-arrow-left"></i> Quay lại trang chủ
                </a>
                <div class="logo-area">
                    <i class="fas fa-heartbeat"></i>
                    <h2>Phòng Khám ABC</h2>
                    <p style="color: #666; font-size: 14px;">Hệ thống quản lý phòng khám</p>
                </div>

                <form action="${pageContext.request.contextPath}/login" method="POST">
                    <div class="role-switch">
                        <button type="button" id="btn-patient" class="active" onclick="selectRole('patient')">Bệnh nhân</button>
                        <button type="button" id="btn-staff" onclick="selectRole('staff')">Nhân viên / Bác sĩ</button>
                    </div>

                    <input type="hidden" name="role" id="selected-role" value="patient">

                    <%-- 1. HIỆN THÔNG BÁO XANH KHI ĐỔI MẬT KHẨU THÀNH CÔNG --%>
                    <c:if test="${param.reset == 'true'}">
                        <p style="color: #0f5132; text-align: center; font-size: 14px; background: #d1e7dd; padding: 10px; border-radius: 5px; border: 1px solid #badbcc;">
                            <i class="fas fa-check-circle"></i> Đổi mật khẩu thành công! Vui lòng đăng nhập lại.
                        </p>
                    </c:if>

                    <c:if test="${param.registered == 'true'}">
                        <p style="color: #0f5132; text-align: center; font-size: 14px; background: #d1e7dd; padding: 10px; border-radius: 5px;">
                            <i class="fas fa-check-circle"></i> Đăng ký thành công! Vui lòng đăng nhập bằng Gmail đã xác thực.
                        </p>
                    </c:if>

                    <% if(request.getAttribute("error") != null) { %>
                    <p style="color: red; text-align: center; font-size: 14px; background: #ffe6e6; padding: 10px; border-radius: 5px;">
                        <i class="fas fa-exclamation-circle"></i> <%= request.getAttribute("error") %>
                    </p>
                    <% } %>

                    <div class="form-group">
                        <label>Gmail</label>
                        <%-- 2. TỰ ĐỘNG LẤY EMAIL TỪ QUÊN MẬT KHẨU SANG --%>
                        <input type="email" name="email" placeholder="Nhập Gmail" 
                               value="${not empty param.email ? param.email : (email != null ? email : '')}" required autofocus>
                    </div>
                    <div class="form-group">
                        <label>Mật khẩu</label>
                        <input type="password" name="password" placeholder="Nhập mật khẩu" required>
                    </div>

                    <div class="options">
                        <label style="display: flex; align-items: center; gap: 5px; font-weight: normal;">
                            <input type="checkbox"> Ghi nhớ đăng nhập
                        </label>
                        <%-- 3. ĐẢM BẢO LINK QUÊN MẬT KHẨU CHUẨN --%>
                        <a href="${pageContext.request.contextPath}/forgot-password" class="forgot-link">Quên mật khẩu?</a>
                    </div>

                    <button type="submit" class="btn-submit">Đăng nhập</button>
                </form>
                <div class="footer-link">
                    Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
                </div>
            </div>

            <div class="auth-banner-side">
                <div class="banner-content">
                    <img src="https://img.freepik.com/free-photo/doctor-nurses-special-equipment_23-2148980721.jpg" alt="Doctor Banner"> 
                    <h3>Chào mừng đến với Phòng Khám ABC</h3>
                    <p>Hệ thống quản lý hiện đại, giúp tối ưu hóa quy trình làm việc và nâng cao chất lượng chăm sóc sức khỏe.</p>
                </div>
            </div>
        </div>

        <script>
            function selectRole(role) {
                var btnStaff = document.getElementById("btn-staff");
                var btnPatient = document.getElementById("btn-patient");
                var inputRole = document.getElementById("selected-role");

                if (role === 'staff') {
                    btnStaff.classList.add("active");
                    btnPatient.classList.remove("active");
                    inputRole.value = "staff";
                } else {
                    btnPatient.classList.add("active");
                    btnStaff.classList.remove("active");
                    inputRole.value = "patient";
                }
            }
        </script>
    </body>
</html>