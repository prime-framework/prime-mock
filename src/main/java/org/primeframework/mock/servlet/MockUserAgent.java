/*
 * Copyright (c) 2001-2020, Inversoft, All Rights Reserved
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
 */
package org.primeframework.mock.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Daniel DeGroff
 */
public class MockUserAgent {
  // Cookies keyed by host and path
  public Map<String, List<Cookie>> cookies = new HashMap<>();

  public void addCookie(MockHttpServletRequest request, Cookie cookie) {
    URI uri = URI.create(request.getBaseURL());
    addCookie(cookie, uri);
  }

  public List<Cookie> getCookies(HttpServletRequest request) {
    URI uri = URI.create(request.getRequestURL().toString());
    String path = uri.getPath().equals("") ? "/" : uri.getPath();

    String domain = uri.getHost() + (uri.getPath().equals("/") ? "" : uri.getPath());
    return cookies.keySet()
                  .stream()
                  .filter(k -> domain.indexOf(k) == 0)
                  .map(k -> cookies.get(k))
                  .flatMap(Collection::stream)
                  // If the cookie has a path, the incoming URI path must be equal to or begin with this same path
                  .filter(c -> c.getPath() == null || path.startsWith(c.getPath()))
                  .collect(Collectors.toList());
  }

  public void reset() {
    cookies.clear();
  }

  private void addCookie(Cookie cookie, URI uri) {
    // Not sure this is 100% accurate according to the spec.
    // - As I understand it there is a difference between omitting the domain on the cookie and
    //  setting it. The result looks the same, but the browser will handle sub-domains differently.
    //  I think this is adequate for our use at the moment.
    //
    // See https://en.wikipedia.org/wiki/HTTP_cookie#Domain_and_path
    //  > If a cookie's Domain and Path attributes are not specified by the server, they default to the domain
    //    and path of the resource that was requested.[43] However, in most browsers there is a difference
    //    between a cookie set from foo.com without a domain, and a cookie set with the foo.com domain. In
    //    the former case, the cookie will only be sent for requests to foo.com, also known as a host-only
    //    cookie. In the latter case, all sub domains are also included (for example, docs.foo.com)
    String host = uri.getHost();
    String domain = cookie.getDomain();
    if (domain == null) {
      domain = host;
    }

    List<Cookie> existing = cookies.computeIfAbsent(domain, k -> new ArrayList<>());
    existing.removeIf(c -> c.getName().equals(cookie.getName()));
    // A Max-Age of 0 is a delete.
    if (cookie.getMaxAge() != 0) {
      existing.add(cookie);
    }
  }
}
