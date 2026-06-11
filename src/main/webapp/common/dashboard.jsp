    <%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%
    // Check authentication
    if (session.getAttribute("account") == null) {
        response.sendRedirect(request.getContextPath() + "/pages/auth/login.jsp");
        return;
    }
    
    model.User account = (model.User) session.getAttribute("account");
    Object roleObj = account.getRole();
    String userRole = roleObj != null ? roleObj.toString().toLowerCase() : "";
    request.setAttribute("userRole", userRole);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        :root {
            --primary: #2563eb;
            --primary-soft: #e0edff;
            --bg: #f3f4f6;
            --card: #ffffff;
            --text-main: #111827;
            --text-sub: #6b7280;
            --border: #e5e7eb;
            --sidebar-width: 260px;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            padding-top: 60px; /* Space for fixed header */
            background: var(--bg);
            color: var(--text-main);
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        /* Main Layout */
        .main-container {
            display: flex;
            flex: 1;
            margin-top: 0;
        }

        /* Content Area */
        .content-area {
            flex: 1;
            padding: 20px;
            min-height: calc(100vh - 60px);
            overflow-y: auto;
        }

        .dashboard-header {
            margin-bottom: 24px;
        }

        .dashboard-header h1 {
            font-size: 24px;
            font-weight: 600;
            color: var(--text-main);
            margin-bottom: 8px;
        }

        .dashboard-header p {
            color: var(--text-sub);
            font-size: 14px;
        }

        .dashboard-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }

        .stat-card {
            background: var(--card);
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            border: 1px solid var(--border);
        }

        .stat-card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 12px;
        }

        .stat-card-title {
            font-size: 14px;
            color: var(--text-sub);
            font-weight: 500;
        }

        .stat-card-icon {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
        }

        .stat-card-icon.blue {
            background: #dbeafe;
            color: #1d4ed8;
        }

        .stat-card-icon.orange {
            background: #fef3c7;
            color: #92400e;
        }

        .stat-card-icon.green {
            background: #dcfce7;
            color: #166534;
        }

        .stat-card-value {
            font-size: 28px;
            font-weight: 700;
            color: var(--text-main);
        }

        .welcome-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 16px;
            padding: 30px;
            color: white;
            margin-bottom: 24px;
        }

        .welcome-card h2 {
            font-size: 24px;
            margin-bottom: 8px;
        }

        .welcome-card p {
            opacity: 0.9;
            font-size: 14px;
        }

        /* Ensure content scrolls properly */
        html {
            overflow-x: hidden;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp" />

    <div class="main-container">
        <!-- Content Area -->
        <main class="content-area">

        </main>
    </div>

    <jsp:include page="footer.jsp" />

    <c:if test="${userRole == 'technician'}">
        <script>
            // Load statistics
            async function loadStatistics() {
                try {
                    const response = await fetch('${pageContext.request.contextPath}/technician-dashboard?action=stats');
                    const data = await response.json();
                    
                    if (data.success) {
                        document.getElementById('totalRequests').textContent = data.total || 0;
                        document.getElementById('pendingRequests').textContent = data.pending || 0;
                        document.getElementById('processingRequests').textContent = data.processing || 0;
                        document.getElementById('completedRequests').textContent = data.completed || 0;
                    }
                } catch (error) {
                    console.error('Error loading statistics:', error);
                    // Set default values
                    document.getElementById('totalRequests').textContent = '0';
                    document.getElementById('pendingRequests').textContent = '0';
                    document.getElementById('processingRequests').textContent = '0';
                    document.getElementById('completedRequests').textContent = '0';
                }
            }

            // Load stats on page load
            document.addEventListener('DOMContentLoaded', loadStatistics);
        </script>
    </c:if>
</body>
</html>
