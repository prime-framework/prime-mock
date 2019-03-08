/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.primeframework.mock.lang.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a mock servlet context.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class MockServletContext implements ServletContext {
  public static final String WEB_INF_LIB = "/WEB-INF/lib";

  private final static Logger logger = LoggerFactory.getLogger(MockServletContext.class);

  public final Map<String, Object> attributes = new HashMap<>();

  public ClassPath classPath;

  public String contextPath = "";

  public File webDir;

  protected MockServletContext() {
    logger.debug("Built MockServletContext without webDir");
    try {
      classPath = ClassPath.getCurrentClassPath();
    } catch (IOException e) {
      throw new RuntimeException("Unable to determine current classpath");
    }
  }

  protected MockServletContext(File webDir) {
    this();
    logger.debug("Built MockServletContext with webDir " + webDir.getAbsolutePath());
    this.webDir = webDir;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, String className) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addListener(String className) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Dynamic addServlet(String servletName, String className) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Dynamic addServlet(String servletName, Servlet servlet) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void declareRoles(String... roleNames) {
    throw new UnsupportedOperationException();
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  public Enumeration getAttributeNames() {
    return new Vector(attributes.keySet()).elements();
  }

  @Override
  public ClassLoader getClassLoader() {
    throw new UnsupportedOperationException();
  }

  public ServletContext getContext(String s) {
    return null;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getEffectiveMajorVersion() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getEffectiveMinorVersion() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    throw new UnsupportedOperationException();
  }

  public String getInitParameter(String s) {
    return null;
  }

  public Enumeration getInitParameterNames() {
    return null;
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    throw new UnsupportedOperationException();
  }

  public int getMajorVersion() {
    return 0;
  }

  public String getMimeType(String s) {
    return null;
  }

  public int getMinorVersion() {
    return 0;
  }

  public RequestDispatcher getNamedDispatcher(String s) {
    return null;
  }

  public String getRealPath(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    File f = new File(webDir, path);
    if (f.exists()) {
      return f.getAbsolutePath();
    }

    return null;
  }

  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  public URL getResource(String path) throws MalformedURLException {
    if (path.startsWith(WEB_INF_LIB)) {
      String jarFile = path.substring(WEB_INF_LIB.length());
      List<String> entries = classPath.getNames();
      for (String entry : entries) {
        if (entry.endsWith(jarFile)) {
          return new File(entry).toURI().toURL();
        }
      }

      return null;
    } else {
      if (path.startsWith("/")) {
        path = path.substring(1);
      }

      File f = new File(webDir, path);
      if (f.isFile()) {
        return f.toURI().toURL();
      }

      return null;
    }
  }

  public InputStream getResourceAsStream(String path) {
    try {
      URL url = getResource(path);
      if (url != null) {
        return url.openStream();
      }
    } catch (Exception ignore) {
    }

    return null;
  }

  public Set getResourcePaths(String path) {
    if (path.equals(WEB_INF_LIB)) {
      Set<String> finalPaths = new HashSet<>();
      Set<String> urls = new HashSet(classPath.getNames());
      for (String url : urls) {
        int index = url.lastIndexOf("/");
        if (index >= 0 && index != url.length() - 1) {
          finalPaths.add(WEB_INF_LIB + "/" + url.substring(index + 1));
        } else if (index != url.length() - 1) { // Only if it is a file not a directory
          finalPaths.add(WEB_INF_LIB + "/" + url);
        }
      }

      return finalPaths;
    } else {
      if (path.startsWith("/")) {
        path = path.substring(1);
      }

      Set<String> urls = new HashSet<>();
      File f = new File(webDir, path);
      if (f.isDirectory()) {
        File[] files = f.listFiles();
        for (File file : files) {
          try {
            urls.add(file.toURI().toURL().toExternalForm());
          } catch (MalformedURLException e) {
            // Ignore
          }
        }
      }

      return urls;
    }
  }

  public String getServerInfo() {
    return null;
  }

  public Servlet getServlet(String s) {
    return null;
  }

  public String getServletContextName() {
    return null;
  }

  public Enumeration getServletNames() {
    return null;
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    throw new UnsupportedOperationException();
  }

  public Enumeration getServlets() {
    return null;
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getVirtualServerName() {
    throw new UnsupportedOperationException();
  }

  public void log(String s) {
  }

  public void log(Exception e, String s) {
  }

  public void log(String s, Throwable throwable) {
  }

  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    throw new UnsupportedOperationException();
  }
}