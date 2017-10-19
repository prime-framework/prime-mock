/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This is a mock session.
 *
 * @author Brian Pontarelli
 */
public class MockHttpSession implements HttpSession {
  protected final Map<String, Object> attributes = new HashMap<>();

  private MockContainer container;

  protected MockHttpSession(MockContainer container) {
    this.container = container;
  }

  public long getCreationTime() {
    return 0;
  }

  public String getId() {
    return "1";
  }

  public long getLastAccessedTime() {
    return 0;
  }

  public ServletContext getServletContext() {
    return container.getContext();
  }

  public void setMaxInactiveInterval(int i) {
  }

  public int getMaxInactiveInterval() {
    return 0;
  }

  public HttpSessionContext getSessionContext() {
    return null;
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  public Object getValue(String name) {
    return attributes.get(name);
  }

  public Enumeration getAttributeNames() {
    return new Vector(attributes.keySet()).elements();
  }

  public String[] getValueNames() {
    return new String[0];
  }

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  public void putValue(String name, Object value) {
    attributes.put(name, value);
  }

  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  public void removeValue(String name) {
    attributes.remove(name);
  }

  public void invalidate() {
    attributes.clear();
  }

  public boolean isNew() {
    return false;
  }

  public void clear() {
    attributes.clear();
  }
}
