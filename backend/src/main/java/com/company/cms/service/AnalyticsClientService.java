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
        try {
            return restClient.get()
                    .uri(analyticsServiceUrl + path)
                    .header("x-internal-key", internalApiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        } catch (Exception e) {
            log.warn("Analytics service unavailable for path: {}. Error: {}", path, e.getMessage());
            return Map.of(
                    "status", "UNAVAILABLE",
                    "message", "Analytics engine temporarily unavailable. Please start the Python analytics service on port 8000."
            );
        }
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
