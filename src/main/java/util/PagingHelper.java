/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author anngu
 */
public final class PagingHelper {

    private PagingHelper() {
    }

    public static int parsePage(HttpServletRequest request, String paramName, int defaultPage) {
        int safeDefault = Math.max(1, defaultPage);
        if (request == null || paramName == null || paramName.isBlank()) {
            return safeDefault;
        }

        String raw = request.getParameter(paramName);
        if (raw == null || raw.isBlank()) {
            return safeDefault;
        }

        try {
            int page = Integer.parseInt(raw);
            return Math.max(1, page);
        } catch (NumberFormatException e) {
            return safeDefault;
        }
    }

    public static PagingMeta build(int requestedPage, int totalRecords, int pageSize, boolean emptyAsOnePage) {
        int safeRecords = Math.max(0, totalRecords);
        int safePageSize = pageSize > 0 ? pageSize : 10;

        int totalPages = (int) Math.ceil((double) safeRecords / safePageSize);
        if (emptyAsOnePage && totalPages == 0) {
            totalPages = 1;
        }

        int currentPage = Math.max(1, requestedPage);
        if (totalPages > 0 && currentPage > totalPages) {
            currentPage = totalPages;
        }

        int offset = (currentPage - 1) * safePageSize;
        return new PagingMeta(currentPage, totalPages, safeRecords, safePageSize, offset);
    }

    public static void expose(HttpServletRequest request, PagingMeta meta) {
        request.setAttribute("currentPage", meta.getCurrentPage());
        request.setAttribute("totalPages", meta.getTotalPages());
        request.setAttribute("totalRecords", meta.getTotalRecords());
        request.setAttribute("pageSize", meta.getPageSize());
    }

    public static final class PagingMeta {

        private final int currentPage;
        private final int totalPages;
        private final int totalRecords;
        private final int pageSize;
        private final int offset;

        public PagingMeta(int currentPage, int totalPages, int totalRecords, int pageSize, int offset) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalRecords = totalRecords;
            this.pageSize = pageSize;
            this.offset = offset;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getOffset() {
            return offset;
        }
    }
}
