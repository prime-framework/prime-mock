/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */
package org.primeframework.mock.servlet;

import javax.servlet.http.Cookie;
import java.util.List;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel DeGroff
 */
public class MockUserAgentTest {

  @Test
  public void cookies() {
    MockContainer container = new MockContainer();
    MockUserAgent userAgent = container.getUserAgent();

    // Reference to the first request
    MockHttpServletRequest firstRequest = container.newServletRequest("/foo");

    // No cookies
    assertEquals(userAgent.getCookies(firstRequest).size(), 0);

    // One cookie
    container.getResponse().addCookie(new Cookie("token", "secret"));

    List<Cookie> actual = userAgent.getCookies(firstRequest);
    assertEquals(actual.size(), 1);
    assertEquals(actual.get(0).getName(), "token");
    assertEquals(actual.get(0).getValue(), "secret");

    // Add a second cookie for a different domain
    container.resetResponse();
    MockHttpServletRequest secondRequest = container.newServletRequest("/admin");
    secondRequest.setLocalName("fusionauth.io");
    secondRequest.setServerName("fusionauth.io");

    container.getResponse().addCookie(new Cookie("JSESSIONID", "12345"));

    // Should only get the single cookie for my original request
    actual = userAgent.getCookies(firstRequest);
    assertEquals(actual.size(), 1);
    assertEquals(actual.get(0).getName(), "token");
    assertEquals(actual.get(0).getValue(), "secret");

    // Should only get the second cookie for my second request
    actual = userAgent.getCookies(secondRequest);
    assertEquals(actual.size(), 1);
    assertEquals(actual.get(0).getName(), "JSESSIONID");
    assertEquals(actual.get(0).getValue(), "12345");

    // Reset response, third request is to fusionauth.io/login
    container.resetResponse();
    MockHttpServletRequest thirdRequest = container.newServletRequest("/login");
    thirdRequest.setLocalName("fusionauth.io");
    thirdRequest.setServerName("fusionauth.io");

    // Add a third cookie to fusionauth.io but scoped to /admin
    Cookie cookie = new Cookie("preferences", "42");
    cookie.setPath("/admin");
    container.getResponse().addCookie(cookie);

    // Expect JSESSIONID but not preferences when path is /login
    actual = userAgent.getCookies(thirdRequest);
    assertEquals(actual.size(), 1);
    assertEquals(actual.get(0).getName(), "JSESSIONID");
    assertEquals(actual.get(0).getValue(), "12345");

    // Re-execute the second request, expect two tokens when path is /admin
    actual = userAgent.getCookies(secondRequest);
    assertEquals(actual.size(), 2);
    assertEquals(actual.get(0).getName(), "JSESSIONID");
    assertEquals(actual.get(0).getValue(), "12345");
    assertEquals(actual.get(1).getName(), "preferences");
    assertEquals(actual.get(1).getValue(), "42");
  }
}