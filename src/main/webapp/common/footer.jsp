<%@page contentType="text/html" pageEncoding="UTF-8"%>
<style>
    /* 1. ÉP TRANG WEB LUÔN CAO TỐI THIỂU BẰNG MÀN HÌNH */
    html {
        height: 100%;
    }
    
    body {
        margin: 0;
        padding: 0;
        min-height: 100vh !important; /* Đảm bảo chiều cao body bằng 100% màn hình (Viewport Height) */
        display: flex !important;
        flex-direction: column !important; /* Xếp các phần tử theo chiều dọc */
    }

    .site-footer,
    .site-footer * {
        box-sizing: border-box;
    }

    /* 2. ĐẨY FOOTER XUỐNG TẬN CÙNG ĐÁY MÀN HÌNH */
    .site-footer {
        margin-top: auto !important; /* Lực đẩy ma thuật: Dùng toàn bộ khoảng trắng thừa để đẩy footer xuống đáy */
        background: linear-gradient(135deg, #0f172a, #1e3a8a) !important;
        color: #dbeafe !important;
        width: 100% !important; 
        flex-shrink: 0; /* Đảm bảo footer luôn giữ nguyên độ cao, không bị bóp méo */
    }

    .footer-inner {
        max-width: 1200px;
        margin: 0 auto;
        padding: 40px 20px 30px;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        gap: 30px;
        box-sizing: border-box;
    }

    .footer-brand {
        flex: 1;
        min-width: 300px;
        max-width: 500px;
    }

    .footer-brand h4 {
        margin: 0 0 12px;
        color: #ffffff;
        font-size: 22px;
        font-weight: 700;
    }

    .footer-brand p {
        margin: 0;
        line-height: 1.6;
        font-size: 15px;
        color: #bfdbfe;
    }

    .footer-contact {
        flex: 1;
        min-width: 280px;
        text-align: right;
        font-size: 15px;
        line-height: 1.8;
        color: #dbeafe;
        word-break: break-word;
    }

    .footer-contact strong {
        color: #ffffff;
    }

    .footer-copy {
        border-top: 1px solid rgba(191, 219, 254, 0.25);
        text-align: center;
        padding: 15px 20px;
        font-size: 13px;
        color: #bfdbfe;
        letter-spacing: 0.5px;
    }
    
    @media (max-width: 992px) {
        .footer-inner {
            justify-content: flex-start;
        }
    }
    @media (max-width: 768px) {
        .footer-inner {
            padding: 28px 16px 24px;
            gap: 18px;
        }
        .footer-brand,
        .footer-contact {
            min-width: 100%;
        }
        .footer-contact {
            text-align: left;
        }
        .footer-copy {
            padding: 12px 16px;
        }
    }
</style>

<footer class="site-footer" id="globalFooter">
    <div class="footer-inner">
        <div class="footer-brand">
            <h4>Hệ thống Quản lý Phòng khám ABC</h4>
            <p>
                Nền tảng hỗ trợ quản lý lịch khám, bác sĩ và hồ sơ bệnh nhân nhanh chóng,
                an toàn, chuyên nghiệp. Phục vụ 24/7.
            </p>
        </div>
        <div class="footer-contact">
            <div><strong>Hotline:</strong> 1900 1234</div>
            <div><strong>Email:</strong> support@phongkhamabc.vn</div>
            <div><strong>Địa chỉ:</strong> 123 Đường Da Liễu, TP. Hà Nội</div>
        </div>
    </div>
    <div class="footer-copy">Copyright &copy; 2026 - Hệ thống Phòng Khám ABC</div>
</footer>

<script>
    // Javascript vẫn được giữ lại để chống lỗi thiếu thẻ <div> của JSP
    document.addEventListener("DOMContentLoaded", function() {
        var footer = document.getElementById("globalFooter");
        if (footer && footer.parentNode !== document.body) {
            document.body.appendChild(footer);
        }
    });
</script>