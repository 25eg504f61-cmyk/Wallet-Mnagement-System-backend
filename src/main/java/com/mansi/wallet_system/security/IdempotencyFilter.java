package com.mansi.wallet_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final Map<String, String> idempotencyCache = new ConcurrentHashMap<>();

    public IdempotencyFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().contains("/wallets/transfer")) {
            String idempotencyKey = request.getHeader("Idempotency-Key");

            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                String cachedResponse = idempotencyCache.get(idempotencyKey);

                if (cachedResponse != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write(cachedResponse);
                    return;
                }

                ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
                
                filterChain.doFilter(request, responseWrapper);

                if (responseWrapper.getStatus() >= 200 && responseWrapper.getStatus() < 300) {
                    byte[] responseArray = responseWrapper.getContentAsByteArray();
                    String responseStr = new String(responseArray, responseWrapper.getCharacterEncoding());
                    idempotencyCache.put(idempotencyKey, responseStr);
                }

                responseWrapper.copyBodyToResponse();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
