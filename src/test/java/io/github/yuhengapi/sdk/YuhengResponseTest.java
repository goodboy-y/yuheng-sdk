package io.github.yuhengapi.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YuhengResponseTest {

    @Test
    void testDefaultConstructor() {
        YuhengResponse response = new YuhengResponse();
        assertNotNull(response);
        assertNull(response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertNull(response.getPageInfo());
        assertNull(response.getTimestamp());
        assertFalse(response.isSuccess());
    }

    @Test
    void testConstructorWithCodeMessageData() {
        YuhengResponse response = new YuhengResponse(200, "success", "testData");
        
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("testData", response.getData());
        assertTrue(response.isSuccess());
    }

    @Test
    void testIsSuccessWithSuccessCode() {
        YuhengResponse response = new YuhengResponse();
        response.setCode(200);
        assertTrue(response.isSuccess());
    }

    @Test
    void testIsSuccessWithNonSuccessCode() {
        YuhengResponse response = new YuhengResponse();
        response.setCode(500);
        assertFalse(response.isSuccess());
    }

    @Test
    void testIsSuccessWithNullCode() {
        YuhengResponse response = new YuhengResponse();
        response.setCode(null);
        assertFalse(response.isSuccess());
    }

    @Test
    void testSettersAndGetters() {
        YuhengResponse response = new YuhengResponse();
        
        response.setCode(200);
        response.setMessage("success");
        response.setData("testData");
        response.setTimestamp(1234567890L);
        
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(1);
        response.setPageInfo(pageInfo);

        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("testData", response.getData());
        assertEquals(1234567890L, response.getTimestamp());
        assertNotNull(response.getPageInfo());
        assertEquals(1, response.getPageInfo().getCurrentPage());
    }
}