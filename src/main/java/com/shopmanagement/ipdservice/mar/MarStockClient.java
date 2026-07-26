package com.shopmanagement.ipdservice.mar;

import java.time.Duration;
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
public class MarStockClient {

    private static final Logger log = LoggerFactory.getLogger(MarStockClient.class);

    private final WebClient webClient;
    private final boolean enabled;

    public MarStockClient(
            WebClient.Builder builder,
            @Value("${stock.service.base-url:http://localhost:8082}") String baseUrl,
            @Value("${stock.mar.link-enabled:true}") boolean enabled) {
        this.webClient = builder.baseUrl(trimSlash(baseUrl)).build();
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void reserveAndCommit(Long productId, int quantity, String reservationKey, Long sourceLineId) {
        if (!enabled) {
            return;
        }
        if (productId == null || quantity <= 0) {
            throw new IllegalArgumentException("productId and positive quantity required for stock link");
        }
        Map<String, Object> line = Map.of(
                "sourceLineId", sourceLineId != null ? sourceLineId : 0L,
                "productId", productId,
                "quantity", quantity,
                "reservationKey", reservationKey);
        try {
            webClient.post()
                    .uri("/stock/reserve-fefo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::applyTenantHeaders)
                    .bodyValue(List.of(line))
                    .retrieve()
                    .bodyToMono(List.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();
            webClient.post()
                    .uri("/stock/reservations/{key}/commit", reservationKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::applyTenantHeaders)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("MAR stock link failed status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException(
                    "Pharmacy stock deduction failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            log.warn("MAR stock link error: {}", ex.getMessage());
            throw new IllegalStateException("Pharmacy stock deduction failed: " + ex.getMessage(), ex);
        }
    }

    private void applyTenantHeaders(org.springframework.http.HttpHeaders headers) {
        Long tenantId = RequestIdFilter.getCurrentTenantId();
        String shopId = RequestIdFilter.getCurrentShopId();
        headers.set(RequestIdFilter.TENANT_ID_HEADER, tenantId != null ? String.valueOf(tenantId) : "0");
        headers.set(RequestIdFilter.SHOP_ID_HEADER, shopId != null ? shopId : "default");
        String role = RequestIdFilter.getCurrentRole();
        String user = RequestIdFilter.getCurrentUser();
        headers.set(RequestIdFilter.AUTH_ROLE_HEADER, role != null ? role : "SHOP_OWNER");
        headers.set(RequestIdFilter.AUTH_USER_HEADER, user != null ? user : "ipd-service");
        headers.set(RequestIdFilter.AUTH_PERMISSIONS_HEADER, "MANAGE_STOCK,MANAGE_ORDERS");
    }

    private static String extractMessage(WebClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            return body.length() > 240 ? body.substring(0, 240) : body;
        }
        return ex.getStatusCode().toString();
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8082";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
