package com.compass.yuhengapi.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageInfoTest {

    @Test
    void testDefaultConstructor() {
        PageInfo pageInfo = new PageInfo();
        assertNotNull(pageInfo);
        assertNull(pageInfo.getCurrentPage());
        assertNull(pageInfo.getRowsInPage());
        assertNull(pageInfo.getRowsPerPage());
        assertNull(pageInfo.getTotalRows());
        assertNull(pageInfo.getTotalPages());
    }

    @Test
    void testSettersAndGetters() {
        PageInfo pageInfo = new PageInfo();
        
        pageInfo.setCurrentPage(1);
        pageInfo.setRowsInPage(10);
        pageInfo.setRowsPerPage(20);
        pageInfo.setTotalRows(100L);
        pageInfo.setTotalPages(5);

        assertEquals(1, pageInfo.getCurrentPage());
        assertEquals(10, pageInfo.getRowsInPage());
        assertEquals(20, pageInfo.getRowsPerPage());
        assertEquals(100L, pageInfo.getTotalRows());
        assertEquals(5, pageInfo.getTotalPages());
    }
}