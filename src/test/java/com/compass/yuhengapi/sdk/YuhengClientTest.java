package com.compass.yuhengapi.sdk;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class YuhengClientTest {

    private static WireMockServer wireMockServer;
    private YuhengClient client;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void teardownWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setupClient() {
        client = new YuhengClient(
                "http://localhost:" + wireMockServer.port(),
                "testClientId",
                "testSecret"
        );
    }

    @AfterEach
    void cleanupClient() {
        client.close();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testConstructorWithInvalidBaseUrl(String invalidBaseUrl) {
        assertThrows(IllegalArgumentException.class, () ->
                new YuhengClient(invalidBaseUrl, "clientId", "secret")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testConstructorWithInvalidClientId(String invalidClientId) {
        assertThrows(IllegalArgumentException.class, () ->
                new YuhengClient("http://localhost:8080", invalidClientId, "secret")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testConstructorWithInvalidSecret(String invalidSecret) {
        assertThrows(IllegalArgumentException.class, () ->
                new YuhengClient("http://localhost:8080", "clientId", invalidSecret)
        );
    }

    @Test
    void testConstructorWithTrailingSlash() {
        YuhengClient client = new YuhengClient("http://localhost:8080/api/", "clientId", "secret");
        assertNotNull(client);
        client.close();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testQueryDataWithInvalidUrl(String invalidUrl) {
        assertThrows(IllegalArgumentException.class, () ->
                client.queryData(invalidUrl)
        );
    }

    @Test
    void testQueryDataSuccess() {
        stubFor(get(urlEqualTo("/api/test"))
                .withHeader("clientId", equalTo("testClientId"))
                .withHeader("secret", equalTo("testSecret"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":{\"name\":\"test\"}}")));

        YuhengResponse response = client.queryData("/api/test");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testQueryDataWithParams() {
        stubFor(get(urlPathEqualTo("/api/test"))
                .withQueryParam("id", equalTo("123"))
                .withQueryParam("name", equalTo("test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":{}}")));

        Map<String, Object> params = new HashMap<>();
        params.put("id", "123");
        params.put("name", "test");

        YuhengResponse response = client.queryData("/api/test", params);

        assertTrue(response.isSuccess());
        verify(getRequestedFor(urlPathEqualTo("/api/test"))
                .withQueryParam("id", equalTo("123"))
                .withQueryParam("name", equalTo("test")));
    }

    @Test
    void testQueryDataHttpError() {
        stubFor(get(urlEqualTo("/api/error"))
                .willReturn(aResponse().withStatus(500)));

        YuhengResponse response = client.queryData("/api/error");

        assertFalse(response.isSuccess());
        assertEquals(-2, response.getCode());
        assertTrue(response.getMessage().contains("HTTP错误"));
    }

    @Test
    void testQueryDataEmptyResponse() {
        stubFor(get(urlEqualTo("/api/empty"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        YuhengResponse response = client.queryData("/api/empty");

        assertFalse(response.isSuccess());
        assertEquals(-1, response.getCode());
        assertTrue(response.getMessage().contains("响应体为空"));
    }

    @Test
    void testQueryPageSuccess() {
        stubFor(get(urlPathEqualTo("/api/page"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("pageSize", equalTo("20"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":[],\"pageInfo\":{\"currentPage\":1,\"rowsInPage\":0,\"rowsPerPage\":20,\"totalRows\":0,\"totalPages\":1}}")));

        YuhengResponse response = client.queryPage("/api/page", null, 1, 20);

        assertTrue(response.isSuccess());
        assertNotNull(response.getPageInfo());
    }

    @Test
    void testQueryPageWithDefaultPage() {
        stubFor(get(urlPathEqualTo("/api/page"))
                .withQueryParam("page", equalTo("20"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":[]}")));

        YuhengResponse response = client.queryPage("/api/page", null, null, null);

        assertTrue(response.isSuccess());
        verify(getRequestedFor(urlPathEqualTo("/api/page"))
                .withQueryParam("page", equalTo("20")));
    }

    @Test
    void testQueryAllPagesSinglePage() {
        stubFor(get(urlPathEqualTo("/api/all"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":[{\"id\":1},{\"id\":2}],\"pageInfo\":{\"currentPage\":1,\"rowsInPage\":2,\"rowsPerPage\":20,\"totalRows\":2,\"totalPages\":1}}")));

        YuhengResponse response = client.queryAllPages("/api/all", null);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertNotNull(response.getPageInfo());
    }

    @Test
    void testQueryAllPagesMultiplePages() {
        stubFor(get(urlPathEqualTo("/api/all"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":[{\"id\":1},{\"id\":2}],\"pageInfo\":{\"currentPage\":1,\"rowsInPage\":2,\"rowsPerPage\":2,\"totalRows\":4,\"totalPages\":2}}")));

        stubFor(get(urlPathEqualTo("/api/all"))
                .withQueryParam("page", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"message\":\"success\",\"data\":[{\"id\":3},{\"id\":4}],\"pageInfo\":{\"currentPage\":2,\"rowsInPage\":2,\"rowsPerPage\":2,\"totalRows\":4,\"totalPages\":2}}")));

        YuhengResponse response = client.queryAllPages("/api/all", null, 2);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getPageInfo().getTotalPages());
    }

    @Test
    void testQueryAllPagesWithError() {
        stubFor(get(urlPathEqualTo("/api/all"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":500,\"message\":\"server error\",\"data\":null}")));

        YuhengResponse response = client.queryAllPages("/api/all", null);

        assertFalse(response.isSuccess());
        assertEquals(500, response.getCode());
    }
}