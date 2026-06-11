<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // 1. JAVA LOGIC: Tính toán số giây còn lại thực tế từ Session
    Long expiresAt = (Long) session.getAttribute("registerOtpExpires");
    long remainingSeconds = 0;
    
    if (expiresAt != null) {
        remainingSeconds = (expiresAt - System.currentTimeMillis()) / 1000;
        if (remainingSeconds < 0) {
            remainingSeconds = 0; 
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Xác thực Gmail - Phòng khám ABC</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        :root {
            --primary: #0061ff;
            --primary-hover: #0052d6;
            --bg: #f4f7fe;
            --text-main: #1f2937;
            --text-muted: #6b7280;
            --error-bg: #fee2e2;
            --error-text: #b91c1c;
            --warning-text: #d97706;
        }
        body {
            font-family: 'Segoe UI', sans-serif;
            margin: 0;
            background: var(--bg);
            /* Thay đổi để Header/Footer/Main xếp chồng theo chiều dọc */
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        
        /* Container chính chứa Verify Card để đẩy nội dung ra giữa */
        .main-content {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }

        .verify-card {
            background: #fff;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
            width: 100%;
            max-width: 420px;
            text-align: center;
            box-sizing: border-box;
        }
        .verify-card h2 {
            color: var(--primary);
            margin-top: 0;
            margin-bottom: 10px;
            font-size: 24px;
        }
        .verify-card p.subtitle {
            color: var(--text-muted);
            font-size: 14px;
            margin-bottom: 20px;
            line-height: 1.5;
        }
        .timer-display {
            font-weight: 600;
            color: var(--warning-text);
            margin-bottom: 20px;
            font-size: 15px;
        }
        .alert-error {
            background: var(--error-bg);
            color: var(--error-text);
            padding: 12px;
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
            text-align: left;
            border: 1px solid #fca5a5;
        }
        .alert-success {
            background: #dcfce7;
            color: #15803d;
            padding: 12px;
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
            text-align: left;
            border: 1px solid #86efac;
        }
        .form-group {
            text-align: left;
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            font-weight: 600;
            margin-bottom: 8px;
            font-size: 14px;
            color: var(--text-main);
        }
        .otp-input {
            width: 100%;
            padding: 14px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 18px;
            letter-spacing: 5px;
            text-align: center;
            box-sizing: border-box;
            outline: none;
            transition: border-color 0.2s;
        }
        .otp-input:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(0, 97, 255, 0.1);
        }
        .btn {
            width: 100%;
            padding: 14px;
            border-radius: 8px;
            font-weight: 600;
            font-size: 15px;
            cursor: pointer;
            transition: all 0.2s;
            border: none;
            margin-bottom: 12px;
        }
        .btn-primary {
            background: var(--primary);
            color: white;
        }
        .btn-primary:hover {
            background: var(--primary-hover);
        }
        .btn-secondary {
            background: white;
            color: var(--primary);
            border: 1px solid var(--primary);
        }
        .btn-secondary:hover:not(:disabled) {
            background: #f0f5ff;
        }
        .btn-secondary:disabled {
            border-color: #9ca3af;
            color: #9ca3af;
            cursor: not-allowed;
            background: #f9fafb;
        }
        .back-link-btn {
            background: none;
            border: none;
            cursor: pointer;
            padding: 0;
            color: var(--text-muted);
            font-size: 14px;
            margin-top: 10px;
        }
        .back-link-btn:hover {
            color: var(--text-main);
            text-decoration: underline;
        }
    </style>
</head>
<body>

<jsp:include page="/common/header.jsp" />

<div class="main-content">
    <div class="verify-card">
        <h2>Xác thực Gmail</h2>
        <p class="subtitle">Nhập mã OTP đã gửi về Gmail của bạn để hoàn tất đăng ký tài khoản.</p>

        <div class="timer-display" id="timerDisplay">Đang tải thời gian...</div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert-error">
                <i class="fas fa-exclamation-circle"></i> <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <% if (request.getAttribute("success") != null) { %>
            <div class="alert-success">
                <i class="fas fa-check-circle"></i> <%= request.getAttribute("success") %>
            </div>
        <% } %>

        <form action="${pageContext.request.contextPath}/verify-email" method="POST" id="verifyForm">
            <div class="form-group">
                <label>Mã OTP Gmail</label>
                <input type="text" name="otp" class="otp-input" placeholder="******" maxlength="6" required autocomplete="off" autofocus>
            </div>
            
            <button type="submit" class="btn btn-primary" id="submitBtn">Xác thực & tạo tài khoản</button>
        </form>

        <form action="${pageContext.request.contextPath}/verify-email" method="POST">
            <input type="hidden" name="action" value="resend">
            <button type="submit" class="btn btn-secondary" id="resendBtn" disabled>
                Gửi lại OTP mới
            </button>
        </form>

        <form action="${pageContext.request.contextPath}/verify-email" method="POST" style="margin-top: 10px;">
            <input type="hidden" name="action" value="cancel">
            <button type="submit" class="back-link-btn">← Hủy và trở lại đăng ký</button>
        </form>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />

<script>
    let timeLeft = <%= remainingSeconds %>;
    const timerDisplay = document.getElementById("timerDisplay");
    const resendBtn = document.getElementById("resendBtn");

    function updateUI() {
        if (timeLeft <= 0) {
            timerDisplay.innerText = "Mã OTP đã hết hạn";
            timerDisplay.style.color = "var(--error-text)";
            resendBtn.innerText = "Gửi lại OTP mới";
            resendBtn.disabled = false;
        } else {
            timerDisplay.innerText = "Mã OTP sẽ hết hạn sau: " + timeLeft + "s";
            resendBtn.innerText = "Gửi lại OTP mới (" + timeLeft + "s)";
            resendBtn.disabled = true;
        }
    }

    updateUI();

    if (timeLeft > 0) {
        const countdownTimer = setInterval(function() {
            timeLeft--;
            updateUI();
            if (timeLeft <= 0) {
                clearInterval(countdownTimer);
            }
        }, 1000);
    }
</script>

</body>
</html>