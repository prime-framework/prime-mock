/*
 * Copyright (c) 2001-2017, Inversoft, All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Mock container to manage the session and context references.
 *
 * @author Daniel DeGroff
 */
public class MockContainer {
  private final MockUserAgent userAgent;

  private MockServletContext context;

  private MockHttpServletRequest request;

  private MockHttpServletResponse response;

  private Set<String> savedContextKeys;

  private MockHttpSession session;

  public MockContainer() {
    this.context = new MockServletContext();
    request = new MockHttpServletRequest("", this);
    response = new MockHttpServletResponse(this);
    userAgent = new MockUserAgent();
  }

  /**
   * Clear the <code>ServletContext</code> attributes.
   */
  public void clearContextAttributes() {
    context.attributes.clear();
  }

  public MockContainer contextSavePoint() {
    savedContextKeys = new HashSet<>(context.attributes.keySet());
    return this;
  }

  public MockServletContext getContext() {
    return context;
  }

  public HttpServletRequestWrapper getHttpServletRequestWrapper() {
    return new HttpServletRequestWrapper(request);
  }

  public MockHttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(MockHttpServletRequest request) {
    this.request = request;
  }

  public MockHttpServletResponse getResponse() {
    return response;
  }

  /**
   * @see HttpServletRequest#getSession(boolean)
   */
  public MockHttpSession getSession(boolean create) {
    if (session == null && create) {
      session = new MockHttpSession(this);
    }

    return session;
  }

  /**
   * @see HttpServletRequest#getSession()
   */
  public MockHttpSession getSession() {
    return getSession(true);
  }

  public MockUserAgent getUserAgent() {
    return userAgent;
  }

  public MockServletContext newServletContext(File webDir) {
    context = new MockServletContext(webDir);
    return context;
  }

  public MockHttpServletRequest newServletRequest(Map<String, List<String>> parameters, String uri, String encoding,
                                                  Locale locale, boolean post) {
    request = new MockHttpServletRequest(parameters, uri, encoding, locale, post, this);
    return request;
  }

  public MockHttpServletRequest newServletRequest() {
    request = new MockHttpServletRequest(this);
    return request;
  }

  public MockHttpServletRequest newServletRequest(String uri) {
    request = new MockHttpServletRequest(uri, this);
    return request;
  }

  public MockHttpServletRequest newServletRequest(String uri, Locale locale, boolean post, String encoding) {
    request = new MockHttpServletRequest(uri, locale, post, encoding, this);
    return request;
  }

  public MockHttpServletResponse newServletResponse() {
    response = new MockHttpServletResponse(this);
    return response;
  }

  /**
   * Reset the servlet context to the initial state. This means the servlet context will be rebuilt using the same web
   * directory used when it was first initialized using {@link #newServletContext(File)}.
   */
  public void resetContext() {
    context = context.webDir == null ? new MockServletContext() : new MockServletContext(context.webDir);
  }

  public void resetRequest() {
    request = new MockHttpServletRequest("", this);
  }

  public void resetResponse() {
    response = new MockHttpServletResponse(this);
  }

  /**
   * Reset the session the default state. This means the session will be null until requested by calling {@link
   * HttpServletRequest#getSession()} or {@link HttpServletRequest#getSession(boolean)}.
   */
  public void resetSession() {
    session = null;
  }

  public void resetUserAgent() {
    userAgent.reset();
  }

  public MockContainer restoreContextToSavePoint(String... keys) {
    Set<String> keep = new HashSet<>(Arrays.asList(keys));
    context.attributes.keySet().removeIf(key -> !keep.contains(key) && !savedContextKeys.contains(key));
    return this;
  }
}
