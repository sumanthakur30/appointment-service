package com.shopmanagement.ipdservice.clinical;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.shopmanagement.ipdservice.filter.RequestIdFilter;

@Component
public class IpdLabClient {

    private static final Logger log = LoggerFactory.getLogger(IpdLabClient.class);

    private final WebClient webClient;
    private final boolean enabled;

    public IpdLabClient(
            WebClient.Builder builder,
            @Value("${order.service.base-url:http://localhost:8083}") String baseUrl,
            @Value("${ipd.lab.link-enabled:true}") boolean enabled) {
        this.webClient = builder.baseUrl(trimSlash(baseUrl)).build();
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createLabOrder(Map<String, Object> body) {
        if (!enabled) {
            throw new IllegalStateException("IPD lab link is disabled");
        }
        try {
            Map<?, ?> response = webClient.post()
                    .uri("/sales-admin/lab-orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::applyHeaders)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(25))
                    .block();
            return response == null ? Map.of() : new LinkedHashMap<>((Map<String, Object>) response);
        } catch (WebClientResponseException ex) {
            log.warn("lab order create failed status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Lab order failed: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Lab order failed: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> patientClinicalSummary(Long patientId, Long tenantId) {
        if (!enabled || patientId == null || tenantId == null) {
            return Map.of("labOrders", List.of(), "labResults", List.of());
        }
        try {
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sales-admin/patients/{patientId}/clinical-summary")
                            .queryParam("tenantId", tenantId)
                            .queryParam("tenantWide", true)
                            .build(patientId))
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(this::applyHeaders)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(25))
                    .block();
            if (response == null) {
                return Map.of("labOrders", List.of(), "labResults", List.of());
            }
            Map<String, Object> out = new LinkedHashMap<>();
            Object orders = response.get("labOrders");
            Object results = response.get("labResults");
            out.put("labOrders", orders instanceof List<?> ? orders : new ArrayList<>());
            out.put("labResults", results instanceof List<?> ? results : new ArrayList<>());
            return out;
        } catch (Exception ex) {
            log.debug("clinical-summary unavailable: {}", ex.getMessage());
            return Map.of("labOrders", List.of(), "labResults", List.of());
        }
    }

    private void applyHeaders(org.springframework.http.HttpHeaders headers) {
        Long tenantId = RequestIdFilter.getCurrentTenantId();
        String shopId = RequestIdFilter.getCurrentShopId();
        headers.set(RequestIdFilter.TENANT_ID_HEADER, tenantId != null ? String.valueOf(tenantId) : "0");
        headers.set(RequestIdFilter.SHOP_ID_HEADER, shopId != null ? shopId : "default");
        String role = RequestIdFilter.getCurrentRole();
        String user = RequestIdFilter.getCurrentUser();
        headers.set(RequestIdFilter.AUTH_ROLE_HEADER, role != null ? role : "SHOP_OWNER");
        headers.set(RequestIdFilter.AUTH_USER_HEADER, user != null ? user : "ipd-service");
        headers.set(RequestIdFilter.AUTH_PERMISSIONS_HEADER, "MANAGE_ORDERS,MANAGE_APPOINTMENTS,MANAGE_LAB");
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8083";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
