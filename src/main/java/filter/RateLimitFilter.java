package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebFilter("/*")
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static final long MAX_TOKENS = 50;
    private static final double REFILL_RATE = 10.0;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        if (path.matches(".*\\.(css|js|jpg|jpeg|png|gif|ico|woff|woff2)$")) {
            chain.doFilter(request, response);
            return;
        }

        String ipAddress = getClientIp(req);
        TokenBucket bucket = buckets.computeIfAbsent(ipAddress, k -> new TokenBucket(MAX_TOKENS, REFILL_RATE));

        if (bucket.tryConsume()) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(
                    "{\"error\": \"Too Many Requests\", \"message\": \"Bạn đang gửi quá nhiều yêu cầu. Hệ thống tạm thời chặn IP của bạn để bảo vệ hệ thống. Vui lòng thử lại sau.\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public void destroy() {
        buckets.clear();
    }
}
