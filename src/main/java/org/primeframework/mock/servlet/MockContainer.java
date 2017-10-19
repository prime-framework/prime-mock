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
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Mock container to manage the session and context references.
 *
 * @author Daniel DeGroff
 */
public class MockContainer {
  private MockServletContext context;

  private MockHttpSession session;

  public MockContainer() {
    this.context = new MockServletContext();
  }

  public MockServletContext getContext() {
    return context;
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

  public MockServletContext newServletContext(File webDir) {
    context = new MockServletContext(webDir);
    return context;
  }

  public MockHttpServletRequest newServletRequest(Map<String, List<String>> parameters, String uri, String encoding,
                                                  Locale locale, boolean post) {
    return new MockHttpServletRequest(parameters, uri, encoding, locale, post, this);
  }

  public MockHttpServletRequest newServletRequest() {
    return new MockHttpServletRequest(this);
  }

  public MockHttpServletRequest newServletRequest(String uri) {
    return new MockHttpServletRequest(uri, this);
  }

  public MockHttpServletRequest newServletRequest(String uri, Locale locale, boolean post, String encoding) {
    return new MockHttpServletRequest(uri, locale, post, encoding, this);
  }

  public MockHttpServletResponse newServletResponse() {
    return new MockHttpServletResponse();
  }

  /**
   * Reset the servlet context to the initial state. This means the servlet context will be rebuilt using the same web
   * directory used when it was first initialized using {@link #newServletContext(File)}.
   */
  public void resetContext() {
    context = context.webDir == null ? new MockServletContext() : new MockServletContext(context.webDir);
  }

  /**
   * Clear the <code>ServletContext</code> attributes.
   */
  public void resetContextAttributes() {
    context.attributes.clear();
  }

  /**
   * Reset the session the default state. This means the session will be null until requested by calling {@link
   * HttpServletRequest#getSession()} or {@link HttpServletRequest#getSession(boolean)}.
   */
  public void resetSession() {
    session = null;
  }
}
