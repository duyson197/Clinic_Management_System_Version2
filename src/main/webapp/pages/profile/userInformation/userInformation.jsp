<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Information</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/pages/profile/userInformation/userInformation.css?v=4.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/common/header.jsp" />

<div class="page ${param.edit == '1' ? 'edit-mode' : ''}">

    <h3>Thông tin tài khoản</h3>
    <p>Quản lý thông tin tài khoản và hồ sơ cá nhân</p>

    <c:if test="${not empty param.success}">
        <div class="notice success">
            <c:choose>
                <c:when test="${param.success == 'profileUpdated'}">Cập nhật toàn bộ thông tin thành công.</c:when>
                <c:when test="${param.success == 'passwordChanged'}">Đổi mật khẩu thành công.</c:when>
                <c:otherwise>Thao tác thành công.</c:otherwise>
            </c:choose>
        </div>
    </c:if>
    
    <c:if test="${not empty param.error}">
        <div class="notice error">
            <c:choose>
                <c:when test="${param.error == 'invalidName'}">Họ tên không hợp lệ. Chỉ chứa chữ cái.</c:when>
                <c:when test="${param.error == 'invalidPhone'}">Số điện thoại không hợp lệ (Đủ 10 số).</c:when>
                <c:when test="${param.error == 'invalidEmail'}">Email không đúng định dạng.</c:when>
                <c:when test="${param.error == 'phoneExists'}">Số điện thoại đã tồn tại.</c:when>
                <c:when test="${param.error == 'emailExists'}">Email đã tồn tại.</c:when>
                <c:when test="${param.error == 'otpRequired'}">Vui lòng nhập mã OTP.</c:when>
                <c:when test="${param.error == 'invalidOtp'}">Mã OTP không đúng hoặc đã hết hạn.</c:when>
                <c:when test="${param.error == 'oldPasswordIncorrect'}">Mật khẩu hiện tại không chính xác.</c:when>
                <c:when test="${param.error == 'passwordTooShort'}">Mật khẩu mới phải từ 6 ký tự.</c:when>
                <c:when test="${param.error == 'passwordMismatch'}">Mật khẩu xác nhận không trùng khớp.</c:when>
                <c:otherwise>Đã xảy ra lỗi. Vui lòng thử lại.</c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <div class="profile-top">
        <div class="profile-left">
            <div class="avatar">
                <c:choose>
                   <c:when test="${not empty user.imageUrl}">
                        <img src="${fn:startsWith(user.imageUrl, 'http') ? user.imageUrl : pageContext.request.contextPath.concat(user.imageUrl)}"
                             alt="Avatar"
                             onerror="this.src='https://i.pinimg.com/1200x/8f/1c/a2/8f1ca2029e2efceebd22fa05cca423d7.jpg'">
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/assets/default-avatar.svg" alt="Avatar" onerror="this.src='https://cdn-icons-png.flaticon.com/512/3774/3774299.png'">
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="profile-name">
                <h2>${user.fullName}</h2>
                <span class="profile-role">${user.role}</span>
            </div>
        </div>
        <div class="profile-right">
            <button type="button" id="btnToggleEdit" class="btn-edit-profile">✏ Chỉnh sửa</button>
        </div>
    </div>

    <div class="main">
        <div class="profile-view">
            <div class="info-box">
                <h4>Thông tin cá nhân</h4>
                <div><label>User ID</label><p>${user.userId}</p></div>
                <div><label>Họ và tên</label><p>${user.fullName}</p></div>
                <div><label>Số điện thoại</label><p>${user.phone}</p></div>
                <div><label>Email</label><p>${user.email}</p></div>
                <div><label>Vai trò</label><p>${user.role}</p></div>
            </div>
        </div>

        <div class="profile-edit">
            <form action="${pageContext.request.contextPath}/userinformationservlet" method="post" enctype="multipart/form-data" class="vi-form" novalidate>
                <input type="hidden" name="action" value="updateProfile">
                <input type="hidden" name="userID" value="${user.userId}">

                <div class="info-box">
                    <h4 style="margin-bottom: 25px;">Chỉnh sửa thông tin</h4>

                    <div class="form-group">
                        <label>Họ và tên</label>
                        <input type="text" name="txtName" value="${user.fullName}" required>
                    </div>

                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="text" name="txtPhone" value="${user.phone}" required maxlength="10" 
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')">
                    </div>

                    <c:choose>
                        <c:when test="${user.role.toString().equalsIgnoreCase('doctor')}">
                            <div class="form-group">
                                <label>Đổi Avatar (JPG/PNG - Tối đa 5MB)</label>
                                <input type="file" name="avatarFile" accept=".jpg,.jpeg,.png,.webp">
                            </div>
                        </c:when>
                    </c:choose>

                    <div style="margin-top: 25px; padding-top: 20px; border-top: 1px dashed #ccc;">
                        <label style="color: #d63031;">Cập nhật Email</label>
                        <p style="font-size: 13px; color: #666; margin-bottom: 15px;">Email sẽ không đổi trừ khi bạn xác thực OTP thành công.</p>
                        
                        <div class="email-input-group">
                            <input type="email" name="txtEmail" id="txtEmail" value="${not empty param.pendingEmail ? param.pendingEmail : user.email}" required>
                            <button type="button" id="btnSendOtp" class="btn-send-otp" style="display: none;">Gửi OTP</button>
                        </div>
                        <div id="otpSuccessBox" class="otp-success-box"></div>
                        <div id="otpInputSection" class="otp-input-section" style="display: ${not empty param.pendingEmail ? 'block' : 'none'};">
                            <label><i class="fas fa-shield-alt"></i> Nhập mã OTP Gmail *</label>
                            <input type="text" name="emailOtp" id="emailOtp" placeholder="* * * * * *" maxlength="6">
                        </div>
                    </div>

                    <button type="submit" class="btn-save-profile"><i class="fas fa-save"></i> Lưu tất cả thay đổi</button>
                </div>
            </form>
        </div>

        <div class="security">
            <form action="${pageContext.request.contextPath}/userinformationservlet" method="post" autocomplete="off">
                <input type="hidden" name="action" value="changePass">
                <h4>Đổi mật khẩu</h4>
                <div class="form-group">
                    <label>Mật khẩu hiện tại</label>
                    <input type="password" name="txtOldPass" required oninvalid="this.setCustomValidity('Vui lòng nhập mật khẩu hiện tại.')" oninput="this.setCustomValidity('')">
                </div>
                <div class="form-group">
                    <label>Mật khẩu mới <small>(Tối thiểu 6 ký tự)</small></label>
                    <input type="password" name="txtNewPass" required minlength="6" oninvalid="if(this.value.length === 0){this.setCustomValidity('Vui lòng nhập mật khẩu mới.')}else{this.setCustomValidity('Mật khẩu mới phải có ít nhất 6 ký tự.')}" oninput="this.setCustomValidity('')">
                </div>
                <div class="form-group">
                    <label>Xác nhận mật khẩu mới</label>
                    <input type="password" name="txtReNewPass" required minlength="6" oninvalid="if(this.value.length === 0){this.setCustomValidity('Vui lòng xác nhận mật khẩu.')}else{this.setCustomValidity('Mật khẩu xác nhận phải có ít nhất 6 ký tự.')}" oninput="this.setCustomValidity('')">
                </div>
                <button type="submit" class="btn-save-password">Đổi mật khẩu</button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />

<script>
    const btn = document.getElementById("btnToggleEdit");
    const page = document.querySelector(".page");
    const currentEmail = "${user.email}";
    const pendingEmailURL = "${param.pendingEmail}";
    const txtEmail = document.getElementById("txtEmail");
    const btnSendOtp = document.getElementById("btnSendOtp");
    const otpInputSection = document.getElementById("otpInputSection");
    const otpSuccessBox = document.getElementById("otpSuccessBox");
    let countdownInterval;
    let otpRequestedEmail = pendingEmailURL ? pendingEmailURL.trim().toLowerCase() : "";
    let otpFetchController = null;

    btn.addEventListener("click", () => {
        page.classList.toggle("edit-mode");
        btn.innerHTML = page.classList.contains("edit-mode") ? "❌ Hủy chỉnh sửa" : "✏ Chỉnh sửa";
    });

    function resetOtpState() {
        clearInterval(countdownInterval);
        otpInputSection.style.display = "none";
        otpSuccessBox.style.display = "none";
        otpSuccessBox.innerHTML = "";
        btnSendOtp.disabled = false;
        btnSendOtp.innerText = "Gửi OTP";
    }

    function toggleEmailState() {
        const email = txtEmail.value.trim().toLowerCase();
        if (otpRequestedEmail && email !== otpRequestedEmail) {
            otpRequestedEmail = "";
            resetOtpState();
        }

        if (email !== currentEmail.toLowerCase() && email.includes("@")) {
            btnSendOtp.style.display = "inline-flex";
            if (pendingEmailURL !== "") btnSendOtp.innerText = "Gửi lại OTP";
        } else {
            btnSendOtp.style.display = "none";
            resetOtpState();
        }
    }
    txtEmail.addEventListener("input", toggleEmailState);
    toggleEmailState();

    btnSendOtp.addEventListener("click", () => {
        const email = txtEmail.value.trim().toLowerCase();
        if (email === currentEmail.toLowerCase()) {
            alert("Email mới phải khác email hiện tại.");
            return;
        }

        if (otpFetchController) {
            otpFetchController.abort();
        }
        otpFetchController = new AbortController();
        btnSendOtp.disabled = true; btnSendOtp.innerText = "Đang gửi...";
        const params = new URLSearchParams(); params.append('action', 'ajaxSendOtp'); params.append('newEmail', email);

        fetch('${pageContext.request.contextPath}/userinformationservlet', {
            method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString(),
            signal: otpFetchController.signal
        }).then(res => res.json()).then(data => {
            if (data.success) {
                otpRequestedEmail = email;
                otpInputSection.style.display = "block";
                startCountdown(60);
            } else {
                alert(data.message);
                btnSendOtp.disabled = false;
                btnSendOtp.innerText = "Gửi lại OTP";
            }
        }).catch((error) => {
            if (error.name !== "AbortError") {
                alert("Lỗi kết nối");
                btnSendOtp.disabled = false;
                btnSendOtp.innerText = "Gửi lại OTP";
            }
        }).finally(() => {
            otpFetchController = null;
        });
    });

    function startCountdown(seconds) {
        let timeLeft = seconds;
        btnSendOtp.style.display = "none";
        otpSuccessBox.className = "otp-success-box";
        otpSuccessBox.innerHTML = '<i class="fas fa-check-circle"></i> <span id="otpStatusText">Mã OTP đã gửi, hết hạn sau: ' + timeLeft + 's</span>';
        otpSuccessBox.style.display = "flex";

        clearInterval(countdownInterval);
        countdownInterval = setInterval(() => {
            timeLeft--;
            document.getElementById("otpStatusText").innerText = "Mã OTP đã gửi, hết hạn sau: " + timeLeft + "s";
            if(timeLeft <= 0) {
                clearInterval(countdownInterval);
                otpSuccessBox.className = "otp-success-box expired";
                otpSuccessBox.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span id="otpStatusText">Mã OTP đã hết hạn! Vui lòng gửi lại.</span>';
                btnSendOtp.style.display = "inline-flex"; btnSendOtp.disabled = false; btnSendOtp.innerText = "Gửi lại OTP";
            }
        }, 1000);
    }
</script>
</body>
</html>