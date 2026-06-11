package controller;

import dal.ServiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Role;
import model.ServicePrice;
import model.User;
import util.PagingHelper;
import util.SystemLogService;

public class AdminServiceServlet extends HttpServlet {

    private static final String VIEW_PATH = "/pages/admin/services.jsp";
    private static final String FORM_FLASH_KEY = "adminServiceFormFlash";
    private static final int PAGE_SIZE = 10;
    private static final int MAX_SERVICE_NAME_LENGTH = 100;
    private static final BigDecimal MAX_SERVICE_PRICE = new BigDecimal("1000000000");

    private final ServiceDAO serviceDAO = new ServiceDAO();

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("account") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("account");
        if (user.getRole() != Role.admin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới được truy cập");
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("add".equals(action)) {
                handleAdd(req);
            } else if ("update".equals(action)) {
                handleUpdate(req);
            } else if ("delete".equals(action)) {
                handleDelete(req);
            }

            if ("POST".equalsIgnoreCase(req.getMethod())) {
                redirectWithFlashState(req, resp);
                return;
            }

            loadPage(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Lỗi: " + e.getMessage());
            if ("POST".equalsIgnoreCase(req.getMethod())) {
                redirectWithFlashState(req, resp);
                return;
            }
            loadPage(req, resp);
        }
    }

    private void loadPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String search = trim(firstNonBlank(req.getParameter("filterSearch"), req.getParameter("search")));
        String category = trim(firstNonBlank(req.getParameter("filterCategory"), req.getParameter("category")));
        int page = PagingHelper.parsePage(req, "filterPage", PagingHelper.parsePage(req, "page", 1));

        consumeFormFlash(req);

        List<ServicePrice> services = serviceDAO.getAllServices();

        if (!search.isEmpty()) {
            String kw = search.toLowerCase();
            services = services.stream()
                    .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        if (!category.isEmpty() && !"all".equals(category)) {
            services = services.stream()
                    .filter(s -> category.equals(s.getServiceType()))
                    .collect(Collectors.toList());
        }

        req.setAttribute("services", services);
        applyPaging(req, services, page);

        req.setAttribute("searchKeyword", search);
        req.setAttribute("filterCategory", category.isEmpty() ? "all" : category);

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void handleAdd(HttpServletRequest req) {
        String name = normalizeServiceName(req.getParameter("name"));
        String serviceType = trim(req.getParameter("serviceType"));
        String priceRaw = trim(req.getParameter("price"));

        if (name.isEmpty() || serviceType.isEmpty() || priceRaw.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin");
            keepAddForm(req, name, serviceType, priceRaw);

            if (name.isEmpty()) {
                req.setAttribute("addNameError", "Tên dịch vụ không được để trống");
            }
            if (serviceType.isEmpty()) {
                req.setAttribute("addServiceTypeError", "Vui lòng chọn danh mục");
            }
            if (priceRaw.isEmpty()) {
                req.setAttribute("addPriceError", "Giá không được để trống");
            }
            return;
        }

        if (name.length() > MAX_SERVICE_NAME_LENGTH) {
            req.setAttribute("error", "Tên dịch vụ không được vượt quá " + MAX_SERVICE_NAME_LENGTH + " ký tự");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addNameError", "Tên dịch vụ tối đa " + MAX_SERVICE_NAME_LENGTH + " ký tự");
            return;
        }

        if (!isValidServiceType(serviceType)) {
            req.setAttribute("error", "Danh mục không hợp lệ");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addServiceTypeError", "Danh mục không hợp lệ");
            return;
        }

        if (serviceDAO.isServiceExistNormalized(name, serviceType, null)) {
            req.setAttribute("error", "Dịch vụ đã tồn tại");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addNameError", "Dịch vụ đã tồn tại trong danh mục này");
            return;
        }

        if (!priceRaw.matches("\\d+")) {
            req.setAttribute("error", "Giá phải là số nguyên không âm");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addPriceError", "Giá phải là số nguyên không âm");
            return;
        }

        BigDecimal price = parseAndValidatePrice(priceRaw, false);
        if (price == null) {
            req.setAttribute("error", "Giá không hợp lệ");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addPriceError", "Giá không hợp lệ");
            return;
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            req.setAttribute("error", "Giá phải lớn hơn hoặc bằng 0");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addPriceError", "Giá phải lớn hơn hoặc bằng 0");
            return;
        }

        if (price.compareTo(MAX_SERVICE_PRICE) > 0) {
            req.setAttribute("error", "Giá vượt quá giới hạn tối đa");
            keepAddForm(req, name, serviceType, priceRaw);
            req.setAttribute("addPriceError", "Giá tối đa là " + MAX_SERVICE_PRICE.toPlainString());
            return;
        }

        ServicePrice s = new ServicePrice();
        s.setName(name);
        s.setServiceType(serviceType);
        s.setPrice(price);

        int affectedRows = serviceDAO.addService(s);
        if (affectedRows > 0) {
            HttpSession sessionLog = req.getSession(false);
            User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
            SystemLogService.log(userLog != null ? userLog.getUserId() : null, "SERVICE_ADDED",
                    "Thêm dịch vụ: name=" + name + ", type=" + serviceType + ", price=" + price);
            req.setAttribute("success", "Thêm dịch vụ thành công");
            return;
        }

        req.setAttribute("error", "Không thể thêm dịch vụ");
        keepAddForm(req, name, serviceType, priceRaw);
    }

    private void handleUpdate(HttpServletRequest req) {
        String serviceIdRaw = trim(req.getParameter("serviceId"));
        String name = normalizeServiceName(req.getParameter("name"));
        String serviceType = trim(req.getParameter("serviceType"));
        String priceRaw = trim(req.getParameter("price"));

        if (serviceIdRaw.isEmpty()) {
            req.setAttribute("error", "Dịch vụ không hợp lệ");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            return;
        }

        if (name.isEmpty() || serviceType.isEmpty() || priceRaw.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);

            if (name.isEmpty()) {
                req.setAttribute("editNameError", "Tên dịch vụ không được để trống");
            }
            if (serviceType.isEmpty()) {
                req.setAttribute("editServiceTypeError", "Vui lòng chọn danh mục");
            }
            if (priceRaw.isEmpty()) {
                req.setAttribute("editPriceError", "Giá không được để trống");
            }
            return;
        }

        int serviceId;
        try {
            serviceId = Integer.parseInt(serviceIdRaw);
        } catch (Exception e) {
            req.setAttribute("error", "Dịch vụ không hợp lệ");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            return;
        }

        ServicePrice existingService = serviceDAO.getServiceById(serviceId);
        if (existingService == null) {
            req.setAttribute("error", "Dịch vụ không tồn tại");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            return;
        }

        if (name.length() > MAX_SERVICE_NAME_LENGTH) {
            req.setAttribute("error", "Tên dịch vụ không được vượt quá " + MAX_SERVICE_NAME_LENGTH + " ký tự");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editNameError", "Tên dịch vụ tối đa " + MAX_SERVICE_NAME_LENGTH + " ký tự");
            return;
        }

        if (!isValidServiceType(serviceType)) {
            req.setAttribute("error", "Danh mục không hợp lệ");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editServiceTypeError", "Danh mục không hợp lệ");
            return;
        }

        if (serviceDAO.isServiceExistNormalized(name, serviceType, serviceId)) {
            req.setAttribute("error", "Dịch vụ đã tồn tại");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editNameError", "Dịch vụ đã tồn tại trong danh mục này");
            return;
        }

        if (!priceRaw.matches("\\d+")) {
            req.setAttribute("error", "Giá phải là số nguyên không âm");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editPriceError", "Giá phải là số nguyên không âm");
            return;
        }

        BigDecimal price = parseAndValidatePrice(priceRaw, false);
        if (price == null) {
            req.setAttribute("error", "Giá không hợp lệ");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editPriceError", "Giá không hợp lệ");
            return;
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            req.setAttribute("error", "Giá phải lớn hơn hoặc bằng 0");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editPriceError", "Giá phải lớn hơn hoặc bằng 0");
            return;
        }

        if (price.compareTo(MAX_SERVICE_PRICE) > 0) {
            req.setAttribute("error", "Giá vượt quá giới hạn tối đa");
            keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
            req.setAttribute("editPriceError", "Giá tối đa là " + MAX_SERVICE_PRICE.toPlainString());
            return;
        }

        ServicePrice s = new ServicePrice();
        s.setServiceId(serviceId);
        s.setName(name);
        s.setServiceType(serviceType);
        s.setPrice(price);

        int affectedRows = serviceDAO.updateService(s);
        if (affectedRows > 0) {
            HttpSession sessionLog = req.getSession(false);
            User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
            SystemLogService.log(userLog != null ? userLog.getUserId() : null, "SERVICE_UPDATED",
                    "Cập nhật dịch vụ: serviceId=" + serviceId + ", name=" + name + ", price=" + price);
            req.setAttribute("success", "Cập nhật dịch vụ thành công");
            return;
        }

        req.setAttribute("error", "Không thể cập nhật dịch vụ");
        keepEditForm(req, serviceIdRaw, name, serviceType, priceRaw);
    }

    private void handleDelete(HttpServletRequest req) {
        String serviceIdRaw = trim(req.getParameter("serviceId"));

        if (serviceIdRaw.isEmpty()) {
            req.setAttribute("error", "Yêu cầu xóa không hợp lệ");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(serviceIdRaw);
        } catch (Exception e) {
            req.setAttribute("error", "Yêu cầu xóa không hợp lệ");
            return;
        }

        ServicePrice existingService = serviceDAO.getServiceById(id);
        if (existingService == null) {
            req.setAttribute("error", "Dịch vụ không tồn tại hoặc đã bị xóa");
            return;
        }

        int affectedRows = serviceDAO.deleteService(id);
        if (affectedRows > 0) {
            HttpSession sessionLog = req.getSession(false);
            User userLog = sessionLog != null ? (User) sessionLog.getAttribute("account") : null;
            SystemLogService.log(userLog != null ? userLog.getUserId() : null, "SERVICE_DELETED",
                    "Xóa dịch vụ: serviceId=" + id + ", name=" + existingService.getName());
            req.setAttribute("success", "Xóa dịch vụ thành công");
            return;
        }

        req.setAttribute("error", "Không thể xóa dịch vụ");
    }

    private void applyPaging(HttpServletRequest req, List<ServicePrice> fullList, int page) {
        List<ServicePrice> safe = fullList != null ? fullList : new ArrayList<>();
        int totalRecords = safe.size();
        PagingHelper.PagingMeta paging = PagingHelper.build(page, totalRecords, PAGE_SIZE, true);

        req.setAttribute("servicesPaged", paginate(safe, paging.getCurrentPage(), paging.getPageSize()));
        PagingHelper.expose(req, paging);
    }

    private <T> List<T> paginate(List<T> data, int page, int pageSize) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        int from = (page - 1) * pageSize;
        if (from < 0 || from >= data.size()) {
            return new ArrayList<>();
        }

        int to = Math.min(from + pageSize, data.size());
        return data.subList(from, to);
    }

    private BigDecimal parseAndValidatePrice(String priceRaw, boolean allowDecimal) {
        if (priceRaw == null || priceRaw.isBlank()) {
            return null;
        }

        String pattern = allowDecimal ? "\\d+(\\.\\d+)?" : "\\d+";
        if (!priceRaw.matches(pattern)) {
            return null;
        }

        try {
            return new BigDecimal(priceRaw);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidServiceType(String serviceType) {
        return "booking_fee".equals(serviceType) || "lab".equals(serviceType);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeServiceName(String s) {
        return trim(s).replaceAll("\\s+", " ");
    }

    private void keepAddForm(HttpServletRequest req, String name, String serviceType, String priceRaw) {
        req.setAttribute("addModalOpen", true);
        req.setAttribute("addName", name);
        req.setAttribute("addServiceType", serviceType);
        req.setAttribute("addPrice", priceRaw);
    }

    private void keepEditForm(HttpServletRequest req, String serviceIdRaw, String name, String serviceType, String priceRaw) {
        req.setAttribute("editModalOpen", true);
        req.setAttribute("editServiceId", serviceIdRaw);
        req.setAttribute("editName", name);
        req.setAttribute("editServiceType", serviceType);
        req.setAttribute("editPrice", priceRaw);
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b;
    }

    private void redirectWithFlashState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        flashCurrentFormState(req);
        resp.sendRedirect(req.getContextPath() + "/admin-services");
    }

    private void flashCurrentFormState(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Map<String, Object> flash = new LinkedHashMap<>();
        Enumeration<String> attributeNames = req.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (shouldFlashAttribute(name)) {
                flash.put(name, req.getAttribute(name));
            }
        }
        session.setAttribute(FORM_FLASH_KEY, flash);
    }

    private void consumeFormFlash(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object flash = session.getAttribute(FORM_FLASH_KEY);
        if (!(flash instanceof Map<?, ?>)) {
            return;
        }
        Map<?, ?> flashMap = (Map<?, ?>) flash;
        for (Map.Entry<?, ?> entry : flashMap.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                req.setAttribute((String) key, entry.getValue());
            }
        }
        session.removeAttribute(FORM_FLASH_KEY);
    }

    private boolean shouldFlashAttribute(String name) {
        String key = trim(name);
        return key.equals("error")
                || key.equals("success")
                || key.equals("addModalOpen")
                || key.equals("editModalOpen")
                || key.startsWith("add")
                || key.startsWith("edit");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    public String getServletInfo() {
        return "Admin Service Management Servlet";
    }
}
