<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Danh sách bác sĩ</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/pages/appointments/listOfDoctors.css">
    </head>
    <body>

        <jsp:include page="/common/header.jsp" />

        <div class="container">
            <!-- ===== LEFT ===== -->
            <div class="doctor-list">
                <h2 class="page-title">Đội ngũ bác sĩ da liễu</h2>
                <div class="cards">
                    <c:forEach items="${doctors}" var="d">
                        <div class="card">
                            <img src="${d.image}" alt="Doctor">

                            <h3>${d.fullName}</h3>
                            <p class="degree">${d.qualification}</p>
                            <p class="desc">${d.specialization}</p>


                            <div class="info">
                                <span>⏱ ${d.experience_years} năm</span>
                                <span>⭐ ${d.rating}</span>
                            </div>

                            <p class="price">
                                <fmt:formatNumber value="${d.price}" type="number"/>đ
                            </p>
                            <div class="action-buttons">
                                <form method="get" action="${pageContext.request.contextPath}/listofrating">
                                    <button class="btn btn-view" name="btnDoctorID" value="${d.doctorId}">
                                        Xem đánh giá
                                    </button>
                                </form>

                                <c:if test="${sessionScope.account != null }">
                                    <c:if test="${user.getRole() eq 'receptionist'}">
                                        <form method="post" action="${pageContext.request.contextPath}/listofdoctorservlet">
                                            <button class="btn btn-book" name="doctorID" value="${d.doctorId}">
                                                Đặt dịch vụ
                                            </button>
                                        </form>
                                    </c:if>

                                    <c:if test="${user.getRole() != 'receptionist'}">
                                        <form method="get" action="${pageContext.request.contextPath}/createpatientsservlet">
                                            <button class="btn btn-book" name="btnDoctorID" value="${d.doctorId}">
                                                Đặt dịch vụ
                                            </button>
                                        </form>
                                    </c:if>
                                </c:if>
                            </div>

                        </div>
                    </c:forEach>
                </div>
                </form>
            </div>

            <form method="get" action="${pageContext.request.contextPath}/listofdoctorservlet">
                <div class="filter">
                    <h3>Tìm kiếm & Lọc</h3>

                    <label>Tìm theo tên bác sĩ</label>
                    <input type="text" name="doctorName" placeholder="Nhập tên bác sĩ" value="${doctorName}">

                    <label>Khoảng giá</label>
                    <div class="price-range">
                        <input type="number" name="priceFrom" placeholder="Từ" value="${priceFrom}">
                        <input type="number" name="priceTo" placeholder="Đến" value="${priceTo}">
                    </div>

                    <c:if test="${not empty error}">
                        <p class="error-msg">${error}</p>
                    </c:if>

                    <label>Năm kinh nghiệm</label>
                    <select name="experience">
                        <option value="">Tất cả</option>
                        <option value="5">5+ năm</option>
                        <option value="10">10+ năm</option>
                    </select>

                    <label>Sắp xếp theo</label>
                    <select name="sort">
                        <option value="">Bác sĩ nổi bật</option>
                        <option value="priceAsc">Giá thấp → cao</option>
                        <option value="priceDesc">Giá cao → thấp</option>
                    </select>

                    <button type="submit" class="filter-btn">🔍 Lọc kết quả</button>
                </div>
            </form>
        </div>
        <br>
        <jsp:include page="/common/footer.jsp" />

    </body>
</html>
