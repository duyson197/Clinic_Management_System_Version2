<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Lấy các trạng thái từ Session
    Boolean isVerified = (Boolean) session.getAttribute("forgotPasswordVerified");
    if (isVerified == null) isVerified = false;

    Long expiresAt = (Long) session.getAttribute("forgotOtpExpires");
    long remainingSeconds = 0;
    
    // Nếu chưa xác thực và có thời gian thì tính đếm ngược
    if (!isVerified && expiresAt != null) {
        remainingSeconds = (expiresAt - System.currentTimeMillis()) / 1000;
        if (remainingSeconds < 0) remainingSeconds = 0;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quên mật khẩu - Phòng khám ABC</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        :root { --primary: #0061ff; --primary-hover: #0052d6; --bg: #f4f7fe; --text-main: #1f2937; --text-muted: #6b7280; --error-bg: #fee2e2; --error-text: #b91c1c; }
        body { font-family: 'Segoe UI', sans-serif; margin: 0; background: var(--bg); display: flex; flex-direction: column; min-height: 100vh; }
        .main-content { flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px 20px; }
        .forgot-card { background: #fff; padding: 40px; border-radius: 16px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05); width: 100%; max-width: 450px; box-sizing: border-box; }
        .forgot-card h2 { color: var(--primary); margin-top: 0; margin-bottom: 10px; font-size: 26px; }
        .forgot-card p.subtitle { color: var(--text-muted); font-size: 15px; margin-bottom: 20px; line-height: 1.5; }
        .timer-display { font-weight: 600; color: #d97706; margin-bottom: 20px; font-size: 15px; }
        .alert-error { background: var(--error-bg); color: var(--error-text); padding: 14px; border-radius: 8px; font-size: 14px; margin-bottom: 20px; border: 1px solid #fca5a5; }
        .alert-success { background: #dcfce7; color: #15803d; padding: 14px; border-radius: 8px; font-size: 14px; margin-bottom: 20px; border: 1px solid #86efac; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; font-weight: 700; margin-bottom: 8px; font-size: 14px; color: var(--text-main); }
        .form-control { width: 100%; padding: 14px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 15px; box-sizing: border-box; outline: none; transition: 0.2s; }
        .form-control:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(0, 97, 255, 0.1); }
        .btn { width: 100%; padding: 14px; border-radius: 8px; font-weight: 700; font-size: 15px; cursor: pointer; transition: 0.2s; border: none; margin-bottom: 10px;}
        .btn-primary { background: var(--primary); color: white; }
        .btn-primary:hover { background: var(--primary-hover); }
        .btn-outline { background: white; color: var(--primary); border: 1px solid var(--primary); }
        .btn-outline:hover:not(:disabled) { background: #f0f5ff; }
        .btn-outline:disabled { border-color: #9ca3af; color: #9ca3af; cursor: not-allowed; background: #f9fafb; }
        .back-link { color: #5b21b6; text-decoration: none; font-size: 15px; font-weight: 500; display: inline-block; margin-top: 15px;}
        .back-link:hover { color: var(--primary); text-decoration: none; }
    </style>
</head>
<body>

<jsp:include page="/common/header.jsp" />

<div class="main-content">
    <div class="forgot-card">
        <h2>Quên mật khẩu</h2>
        <p class="subtitle">
            <% if (isVerified) { %>
                Vui lòng nhập mật khẩu mới cho tài khoản của bạn.
            <% } else { %>
                Nhập Gmail để nhận OTP và đặt lại mật khẩu.
            <% } %>
        </p>

        <% if (request.getAttribute("showVerifyForm") != null && !isVerified) { %>
            <div class="timer-display" id="timerDisplay">Đang tải thời gian...</div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= request.getAttribute("error") %></div>
        <% } %>
        <% if (request.getAttribute("success") != null) { %>
            <div class="alert-success"><i class="fas fa-check-circle"></i> <%= request.getAttribute("success") %></div>
        <% } %>

        <%-- ĐIỀU HƯỚNG MÀN HÌNH THEO TRẠNG THÁI --%>

        <% if (isVerified) { %>
            <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
                <div class="form-group">
                    <label>Mật khẩu mới</label>
                    <input type="password" name="newPassword" class="form-control" placeholder="Từ 6 ký tự trở lên" required minlength="6">
                </div>
                <div class="form-group">
                    <label>Xác nhận mật khẩu mới</label>
                    <input type="password" name="confirmPassword" class="form-control" placeholder="Nhập lại mật khẩu mới" required minlength="6">
                </div>
                <button type="submit" name="action" value="resetPassword" class="btn btn-primary">Xác nhận & Đổi mật khẩu</button>
            </form>

        <% } else if (request.getAttribute("showVerifyForm") != null) { %>
            <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
                <div class="form-group">
                    <label>Gmail</label>
                    <input type="email" class="form-control" value="${sessionScope.forgotPasswordEmail}" readonly style="background-color: #f9fafb; color: #6b7280;">
                </div>
                <div class="form-group">
                    <label>Mã OTP (6 số)</label>
                    <input type="text" name="otp" class="form-control" placeholder="Nhập mã OTP" required autocomplete="off" maxlength="6" autofocus>
                </div>
                
                <button type="submit" name="action" value="verifyOtp" class="btn btn-primary">Xác thực OTP</button>
            </form>

            <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
                <input type="hidden" name="email" value="${sessionScope.forgotPasswordEmail}">
                <button type="submit" name="action" value="resend" class="btn btn-outline" id="resendBtn" disabled>Gửi lại OTP</button>
            </form>

        <% } else { %>
            <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
                <div class="form-group">
                    <label>Gmail đã đăng ký</label>
                    <input type="email" name="email" class="form-control" value="${email}" placeholder="example@gmail.com" required>
                </div>
                <button type="submit" name="action" value="sendOtp" class="btn btn-primary">Gửi OTP</button>
            </form>
        <% } %>

        <div style="text-align: center;">
            <a href="${pageContext.request.contextPath}/login" class="back-link">← Quay lại đăng nhập</a>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />

<%-- SCRIPT TÍNH 60S GIẢM DẦN CHO TRẠNG THÁI 2 --%>
<% if (request.getAttribute("showVerifyForm") != null && !isVerified) { %>
<script>
    let timeLeft = <%= remainingSeconds %>;
    const timerDisplay = document.getElementById("timerDisplay");
    const resendBtn = document.getElementById("resendBtn");

    function updateUI() {
        if (timeLeft <= 0) {
            timerDisplay.innerText = "Mã OTP đã hết hạn!";
            timerDisplay.style.color = "var(--error-text)";
            resendBtn.innerText = "Gửi lại OTP";
            resendBtn.disabled = false;
        } else {
            timerDisplay.innerText = "Mã OTP sẽ hết hạn sau: " + timeLeft + "s";
            resendBtn.innerText = "Gửi lại OTP (" + timeLeft + "s)";
            resendBtn.disabled = true;
        }
    }

    updateUI();
    if (timeLeft > 0) {
        const countdownTimer = setInterval(function() {
            timeLeft--;
            updateUI();
            if (timeLeft <= 0) clearInterval(countdownTimer);
        }, 1000);
    }
</script>
<% } %>

</body>
</html>