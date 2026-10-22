package com.company.cms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long TIME_WINDOW_MS = 60_000; // 1 minute

    private static class Attempt {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }

    private final Map<String, Attempt> ipAttempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/api/auth/login".equalsIgnoreCase(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String clientIp = getClientIp(request);
            Attempt attempt = ipAttempts.compute(clientIp, (ip, att) -> {
                long now = System.currentTimeMillis();
                if (att == null || (now - att.windowStart > TIME_WINDOW_MS)) {
                    Attempt newAtt = new Attempt();
                    return newAtt;
                }
                return att;
            });

            if (attempt.count.get() >= MAX_FAILED_ATTEMPTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many failed login attempts. Please wait 1 minute.\"}");
                return;
            }

            filterChain.doFilter(request, response);

            if (response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
                attempt.count.incrementAndGet();
            } else if (response.getStatus() == HttpServletResponse.SC_OK) {
                ipAttempts.remove(clientIp);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        // Only trust X-Forwarded-For if request comes from local reverse proxy
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            String xf = request.getHeader("X-Forwarded-For");
            if (xf != null && !xf.isBlank()) {
                return xf.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }
}
