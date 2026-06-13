package io.github.yuhengapi.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YuhengClient {

    private final String baseUrl;
    private final String clientId;
    private final String secret;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YuhengClient(String baseUrl, String clientId, String secret) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("baseUrl不能为空");
        }
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId不能为空");
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("secret不能为空");
        }

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.clientId = clientId;
        this.secret = secret;
        this.objectMapper = new ObjectMapper();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public YuhengResponse queryData(String relativeUrl) {
        return queryData(relativeUrl, null);
    }

    public YuhengResponse queryData(String relativeUrl, Map<String, Object> params) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("relativeUrl不能为空");
        }

        String url = buildUrl(relativeUrl, params);
        return executeGetRequest(url);
    }

    public YuhengResponse queryPage(String relativeUrl, Map<String, Object> params, Integer page, Integer pageSize) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("relativeUrl不能为空");
        }

        if (page == null) {
            page = 20;
        }

        Map<String, Object> pageParams = params != null ? new java.util.HashMap<>(params) : new java.util.HashMap<>();
        pageParams.put("page", page);
        if (pageSize != null) {
            pageParams.put("pageSize", pageSize);
        }

        String url = buildUrl(relativeUrl, pageParams);
        return executeGetRequest(url);
    }

    public YuhengResponse queryAllPages(String relativeUrl, Map<String, Object> params) {
        return queryAllPages(relativeUrl, params, 20);
    }

    public YuhengResponse queryAllPages(String relativeUrl, Map<String, Object> params, Integer pageSize) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("relativeUrl不能为空");
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }

        List<Object> allData = new ArrayList<>();
        int currentPage = 1;
        long totalRows = 0;
        int totalPages = 1;

        while (currentPage <= totalPages) {
            YuhengResponse response = queryPage(relativeUrl, params, currentPage, pageSize);

            if (!response.isSuccess()) {
                return response;
            }

            PageInfo pageInfo = response.getPageInfo();
            if (pageInfo != null) {
                totalPages = pageInfo.getTotalPages() != null ? pageInfo.getTotalPages() : 1;
                totalRows = pageInfo.getTotalRows() != null ? pageInfo.getTotalRows() : 0;
            }

            Object data = response.getData();
            if (data instanceof List) {
                allData.addAll((List<?>) data);
            }

            currentPage++;
        }

        YuhengResponse result = new YuhengResponse(200, "操作成功", allData);
        PageInfo resultPageInfo = new PageInfo();
        resultPageInfo.setCurrentPage(1);
        resultPageInfo.setRowsInPage(allData.size());
        resultPageInfo.setRowsPerPage(allData.size());
        resultPageInfo.setTotalRows(totalRows);
        resultPageInfo.setTotalPages(1);
        result.setPageInfo(resultPageInfo);
        result.setTimestamp(System.currentTimeMillis());

        return result;
    }

    private String buildUrl(String relativeUrl, Map<String, Object> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append(relativeUrl);

        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                try {
                    if (entry.getValue() != null) {
                        if (!first) {
                            urlBuilder.append("&");
                        }
                        urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        urlBuilder.append("=");
                        urlBuilder.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
                        first = false;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return urlBuilder.toString();
    }

    private YuhengResponse executeGetRequest(String url) {
        HttpGet request = new HttpGet(url);
        request.setHeader("clientId", clientId);
        request.setHeader("secret", secret);
        request.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            int statusCode = response.getCode();
            if (statusCode != 200) {
                return new YuhengResponse(-2, "HTTP错误: " + statusCode, null);
            }

            HttpEntity entity = response.getEntity();
            String bodyString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            if (bodyString == null || bodyString.isEmpty()) {
                return new YuhengResponse(-1, "响应体为空", null);
            }

            return objectMapper.readValue(bodyString, YuhengResponse.class);

        } catch (IOException | ParseException e) {
            return new YuhengResponse(-1, "网络错误: " + e.getMessage(), null);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
            }
        }
    }
}
