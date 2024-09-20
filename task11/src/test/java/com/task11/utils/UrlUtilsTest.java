package com.task11.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilsTest {

    @Test
    void convertIdPath() {
       assertEquals("/tables/{tableId}", UrlUtils.convertIdPath("/tables/34343"));
       assertEquals("/tables/{tableId}", UrlUtils.convertIdPath("/tables/sdfdf"));
    }

    @Test
    void noConvertIdPath() {
        assertEquals("/tables", UrlUtils.convertIdPath("/tables/"));
        assertEquals("/tables", UrlUtils.convertIdPath("/tables"));
        assertEquals("/", UrlUtils.convertIdPath("/"));
        assertEquals("/", UrlUtils.convertIdPath(null));
    }
}