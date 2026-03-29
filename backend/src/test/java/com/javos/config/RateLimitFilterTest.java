/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RateLimitFilter}.
 */
class RateLimitFilterTest {

    private RateLimitProperties props;
    private RateLimitFilter filter;

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        props = new RateLimitProperties();
        props.setEnabled(true);
        props.setApiCapacity(5);
        props.setApiRefillTokens(5);
        props.setApiRefillDurationSeconds(60);
        props.setAuthCapacity(3);
        props.setAuthRefillTokens(3);
        props.setAuthRefillDurationSeconds(60);
        filter = new RateLimitFilter(props);
    }

    @Test
    void request_nonApiPath_isNotRateLimited() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/some-static-file.js");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void request_apiPath_withinLimit_isAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void request_apiPath_exceedsLimit_returns429() throws Exception {
        String ip = "10.0.0.3";
        // Exhaust the bucket (capacity = 5)
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/clients");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }

        // 6th request should be rate-limited
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("GET", "/api/v1/clients");
        blockedReq.setRemoteAddr(ip);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilter(blockedReq, blockedRes, chain);

        assertThat(blockedRes.getStatus()).isEqualTo(429);
        verify(chain, times(5)).doFilter(any(), any());
    }

    @Test
    void request_authPath_exceedsAuthLimit_returns429() throws Exception {
        String ip = "10.0.0.4";
        // Exhaust the auth bucket (capacity = 3)
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }

        // 4th request should be rate-limited
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        blockedReq.setRemoteAddr(ip);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilter(blockedReq, blockedRes, chain);

        assertThat(blockedRes.getStatus()).isEqualTo(429);
    }

    @Test
    void request_differentIps_haveSeparateBuckets() throws Exception {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        // Exhaust ip1's bucket
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/clients");
            req.setRemoteAddr(ip1);
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        }

        // ip2 should still be allowed (separate bucket)
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/v1/clients");
        req2.setRemoteAddr(ip2);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, chain);

        assertThat(res2.getStatus()).isEqualTo(200);
    }

    @Test
    void request_withXForwardedFor_usesFirstIp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 10.0.0.1, 10.0.0.2");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void whenDisabled_allRequestsAreAllowed() throws Exception {
        props.setEnabled(false);

        // Simulate many requests (more than the capacity)
        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr("10.5.5.5");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            assertThat(res.getStatus()).isEqualTo(200);
        }

        verify(chain, times(20)).doFilter(any(), any());
    }
}
