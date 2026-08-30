package com.shopmanagement.ipdservice.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String SHOP_ID_HEADER = "X-Shop-Id";
    public static final String AUTH_ROLE_HEADER = "X-Auth-Role";
    public static final String AUTH_USER_HEADER = "X-Auth-User";
    public static final String AUTH_PERMISSIONS_HEADER = "X-Auth-Permissions";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentShopId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> currentPermissions = new ThreadLocal<>();
    private static final ThreadLocal<String> currentAuthorization = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            if (!"OPTIONS".equalsIgnoreCase(request.getMethod())
                    && !request.getRequestURI().startsWith("/actuator")
                    && !isPublicFamilyPath(request.getRequestURI())) {
                String tenantId = request.getHeader(TENANT_ID_HEADER);
                String shopId = request.getHeader(SHOP_ID_HEADER);
                if (tenantId == null || tenantId.isBlank() || shopId == null || shopId.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Missing tenant context headers\"}");
                    return;
                }
                try {
                    currentTenantId.set(Long.valueOf(tenantId.trim()));
                } catch (NumberFormatException ex) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Invalid X-Tenant-Id header\"}");
                    return;
                }
                currentShopId.set(shopId.trim());
                currentRole.set(normalize(request.getHeader(AUTH_ROLE_HEADER)));
                currentUser.set(normalize(request.getHeader(AUTH_USER_HEADER)));
                currentPermissions.set(parsePermissions(request.getHeader(AUTH_PERMISSIONS_HEADER)));
                currentAuthorization.set(normalizeAuth(request.getHeader("Authorization")));
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY);
            currentTenantId.remove();
            currentShopId.remove();
            currentRole.remove();
            currentUser.remove();
            currentPermissions.remove();
        }
    }

    public static Long getCurrentTenantId() {
        return currentTenantId.get();
    }

    public static String getCurrentShopId() {
        return currentShopId.get();
    }

    public static String getCurrentRole() {
        return currentRole.get();
    }

    public static String getCurrentUser() {
        return currentUser.get();
    }

    public static List<String> getCurrentPermissions() {
        List<String> permissions = currentPermissions.get();
        return permissions == null ? List.of() : permissions;
    }

    /** Visitor pass portal ΓÇö pass code is the credential; no tenant headers. */
    private static boolean isPublicFamilyPath(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.contains("/ipd/family/public/");
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private static List<String> parsePermissions(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }
        return Arrays.stream(headerValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .toList();
    }
}
