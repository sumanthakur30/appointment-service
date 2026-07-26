package com.shopmanagement.ipdservice.billing;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class OrderBillingClient {

    private static final Logger log = LoggerFactory.getLogger(OrderBillingClient.class);

    private final WebClient webClient;
    private final boolean enabled;

    public OrderBillingClient(
            WebClient.Builder builder,
            @Value("${order.service.base-url:http://localhost:8083}") String baseUrl,
            @Value("${order.billing.sync-enabled:true}") boolean enabled) {
        this.webClient = builder.baseUrl(trimSlash(baseUrl)).build();
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Posts IPD daily charges to order-service. Returns order id, or empty if disabled/failed.
     */
    public Long postIpdCharges(IpdChargeSyncRequest request, TenantHeaders headers) {
        if (!enabled) {
            return null;
        }
        try {
            Map<?, ?> response = webClient.post()
                    .uri("/sales-admin/healthcare/ipd-charges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(RequestIdFilter.TENANT_ID_HEADER, String.valueOf(headers.tenantId()))
                    .header(RequestIdFilter.SHOP_ID_HEADER, headers.shopId())
                    .header(RequestIdFilter.AUTH_ROLE_HEADER,
                            headers.role() != null ? headers.role() : "SHOP_OWNER")
                    .header(RequestIdFilter.AUTH_USER_HEADER,
                            headers.user() != null ? headers.user() : "ipd-service")
                    .header(RequestIdFilter.AUTH_PERMISSIONS_HEADER, "MANAGE_ORDERS,MANAGE_APPOINTMENTS")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();
            if (response == null) {
                return null;
            }
            Object id = response.get("id");
            if (id instanceof Number n) {
                return n.longValue();
            }
            return null;
        } catch (WebClientResponseException ex) {
            log.warn("order-service IPD charge sync failed status={} body={}",
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Billing sync failed: " + ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            log.warn("order-service IPD charge sync error: {}", ex.getMessage());
            throw new IllegalStateException("Billing sync failed: " + ex.getMessage(), ex);
        }
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8083";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public record TenantHeaders(Long tenantId, String shopId, String role, String user) {}

    public static class IpdChargeSyncRequest {
        public Long customerId;
        public Long encounterId;
        public Long admissionId;
        public String admissionNo;
        public LocalDate chargeDate;
        public String paymentStatus = "PENDING";
        public String paymentMethod = "CASH";
        public String idempotencyKey;
        public List<Line> lines = new ArrayList<>();

        public static class Line {
            public String chargeCode;
            public String productName;
            public Double quantity = 1.0;
            public BigDecimal amount;
        }
    }
}
