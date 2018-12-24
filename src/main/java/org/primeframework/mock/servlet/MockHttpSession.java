/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

  private ZonedDateTime created;

  protected MockHttpSession(MockContainer container) {
    this.container = container;
    created = ZonedDateTime.now(ZoneOffset.UTC);
  }

  public void clear() {
    attributes.clear();
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  public Enumeration getAttributeNames() {
    return new Vector(attributes.keySet()).elements();
  }

  public long getCreationTime() {
    if (created == null) {
      return 0;
    }

    return created.toInstant().toEpochMilli();
  }

  public String getId() {
    return "1";
  }

  /**
   * This is not currently updated correctly. It will be equal to the created time.
   *
   * @return the last accessed time in epoch millis
   */
  public long getLastAccessedTime() {
    if (created == null) {
      return 0;
    }

    return created.toInstant().toEpochMilli();
  }

  public int getMaxInactiveInterval() {
    return 0;
  }

  public void setMaxInactiveInterval(int i) {
  }

  public ServletContext getServletContext() {
    return container.getContext();
  }

  public HttpSessionContext getSessionContext() {
    return null;
  }

  public Object getValue(String name) {
    return attributes.get(name);
  }

  public String[] getValueNames() {
    return new String[0];
  }

  public void invalidate() {
    attributes.clear();
    container.resetSession();
    created = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
  }

  public boolean isNew() {
    return false;
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

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }
}
