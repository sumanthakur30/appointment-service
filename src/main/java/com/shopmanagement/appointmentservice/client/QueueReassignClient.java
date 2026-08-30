package com.shopmanagement.appointmentservice.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.shopmanagement.ipdservice.filter.RequestIdFilter;
import com.shopmanagement.security.SecurityHeaderNames;

@Component
public class QueueReassignClient {
    private static final Logger log = LoggerFactory.getLogger(QueueReassignClient.class);

    private final RestTemplate restTemplate;
    private final String queueServiceBaseUrl;
    private final String internalApiKey;

    public QueueReassignClient(
            RestTemplate restTemplate,
            @Value("${services.queue.base-url:http://localhost:8098}") String queueServiceBaseUrl,
            @Value("${security.jwt.internal-api-key:}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.queueServiceBaseUrl = trimSlash(queueServiceBaseUrl);
        this.internalApiKey = internalApiKey == null ? "" : internalApiKey.trim();
    }

    public void reassignAppointmentDoctor(Long appointmentId, Long doctorId) {
        if (appointmentId == null || doctorId == null) {
            return;
        }
        String url = queueServiceBaseUrl + "/queue/tokens/by-appointment/" + appointmentId + "/doctor";
        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(Map.of("doctorId", doctorId), serviceHeaders()),
                    Void.class);
            log.info("Queue token moved for appointmentId={} doctorId={}", appointmentId, doctorId);
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND
                    || (ex.getStatusCode() == HttpStatus.BAD_REQUEST
                            && firstMessage(ex.getResponseBodyAsString(), "").toLowerCase().contains("not found"))) {
                log.info("No queue token for appointmentId={}", appointmentId);
                return;
            }
            log.warn(
                    "Queue move failed for appointmentId={} status={}: {}",
                    appointmentId,
                    ex.getStatusCode(),
                    firstMessage(ex.getResponseBodyAsString(), ex.getStatusText()));
        } catch (RuntimeException ex) {
            log.warn("Queue move skipped for appointmentId={}: {}", appointmentId, ex.getMessage());
        }
    }

    private HttpHeaders serviceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        Long tenantId = RequestIdFilter.getCurrentTenantId();
        if (tenantId != null) {
            headers.set(RequestIdFilter.TENANT_ID_HEADER, String.valueOf(tenantId));
        }
        String shopId = RequestIdFilter.getCurrentShopId();
        if (shopId != null && !shopId.isBlank()) {
            headers.set(RequestIdFilter.SHOP_ID_HEADER, shopId);
        }
        String role = RequestIdFilter.getCurrentRole();
        if (role != null) {
            headers.set(RequestIdFilter.AUTH_ROLE_HEADER, role);
        }
        String user = RequestIdFilter.getCurrentUser();
        if (user != null) {
            headers.set(RequestIdFilter.AUTH_USER_HEADER, user);
        }
        var permissions = RequestIdFilter.getCurrentPermissions();
        if (!permissions.isEmpty()) {
            headers.set(RequestIdFilter.AUTH_PERMISSIONS_HEADER, String.join(",", permissions));
        }
        String authorization = RequestIdFilter.getCurrentAuthorization();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (!internalApiKey.isBlank()) {
            headers.set(SecurityHeaderNames.INTERNAL_API_KEY, internalApiKey);
        }
        return headers;
    }

    private static String trimSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String firstMessage(String body, String fallback) {
        if (body == null || body.isBlank()) {
            return fallback;
        }
        int start = body.indexOf("\"message\"");
        if (start < 0) {
            return body.length() > 200 ? body.substring(0, 200) : body;
        }
        int colon = body.indexOf(':', start);
        int q1 = body.indexOf('"', colon + 1);
        int q2 = body.indexOf('"', q1 + 1);
        if (q1 >= 0 && q2 > q1) {
            return body.substring(q1 + 1, q2);
        }
        return body;
    }
}
