package com.company.cms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AnalyticsClientService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsClientService.class);

    private final RestClient restClient;

    @Value("${app.analytics.url:http://localhost:8000}")
    private String analyticsServiceUrl;

    @Value("${app.analytics.internal-key:cms-internal-analytics-key-2026}")
    private String internalApiKey;

    public AnalyticsClientService(RestClient restClient) {
        this.restClient = restClient;
    }

    public Object triggerEtl() {
        return post("/etl/run");
    }

    public Object getEtlStatus() {
        return get("/etl/status");
    }

    public Object getKpiMetrics(String region, String fromDate, String toDate) {
        String uri = "/analytics/kpis?region=" + (region != null ? region : "ALL");
        return get(uri);
    }

    public Object getSalesTrend(String region, String category) {
        String uri = "/analytics/sales/trend?region=" + (region != null ? region : "ALL") +
                "&category=" + (category != null ? category : "ALL");
        return get(uri);
    }

    public Object getRegionalComparison(String category) {
        String uri = "/analytics/regional?category=" + (category != null ? category : "ALL");
        return get(uri);
    }

    public Object getSalesFunnel(String region) {
        String uri = "/analytics/funnel?region=" + (region != null ? region : "ALL");
        return get(uri);
    }

    public Object getForecast(int horizon, String region) {
        String uri = "/analytics/forecast?horizon=" + horizon + "&region=" + (region != null ? region : "ALL");
        return get(uri);
    }

    public Object getSegments(int k) {
        String uri = "/analytics/segments?k=" + k;
        return get(uri);
    }

    public Object getSentiment(Long productId) {
        String uri = productId != null ? "/analytics/sentiment?productId=" + productId : "/analytics/sentiment";
        return get(uri);
    }

    public Object getUnifiedReport(String department) {
        String uri = "/analytics/unified-report" + (department != null ? "?department=" + department : "");
        return get(uri);
    }

    public Object getAdvancedPlots() {
        return get("/analytics/advanced-plots");
    }

    private Object get(String path) {
        int maxRetries = 2;
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return restClient.get()
                        .uri(analyticsServiceUrl + path)
                        .header("x-internal-key", internalApiKey)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(Object.class);
            } catch (Exception e) {
                lastException = e;
                log.warn("Analytics GET attempt {}/{} failed for path: {}. Error: {}",
                        attempt + 1, maxRetries, path, e.getMessage());
                if (attempt < maxRetries - 1) {
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        log.error("Analytics service unavailable after {} retries for path: {}. Last error: {}",
                maxRetries, path, lastException != null ? lastException.getMessage() : "unknown");
        return Map.of(
                "status", "UNAVAILABLE",
                "message", "Analytics engine temporarily unavailable. The service may be starting up — please retry in 30 seconds."
        );
    }

    private Object post(String path) {
        try {
            return restClient.post()
                    .uri(analyticsServiceUrl + path)
                    .header("x-internal-key", internalApiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        } catch (Exception e) {
            log.warn("Analytics ETL trigger failed. Error: {}", e.getMessage());
            return Map.of(
                    "status", "UNAVAILABLE",
                    "message", "Unable to trigger ETL. Ensure Python analytics service is active."
            );
        }
    }
}
