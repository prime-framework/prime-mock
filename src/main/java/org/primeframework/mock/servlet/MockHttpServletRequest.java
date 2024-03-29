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
 */
package org.primeframework.mock.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * This class is a mock servlet request.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MockHttpServletRequest implements HttpServletRequest {
  protected final Map<String, Object> attributes = new HashMap<>();

  protected final MockContainer container;

  protected final MockServletContext context;

  protected final Map<String, FileInfo> files = new LinkedHashMap<>();

  // Support multi-threading requests in a test.
  protected final Map<String, List<String>> headers = Collections.synchronizedMap(new LinkedHashMap<>());

  protected final Map<String, List<String>> parameters = new LinkedHashMap<>();

  protected String contentType = null;

  protected String contextPath = "";

  protected List<Cookie> cookies = new ArrayList<>();

  protected MockRequestDispatcher dispatcher;

  protected String encoding;

  protected ServletInputStream inputStream;

  protected boolean inputStreamRetrieved;

  protected String localName = "localhost";

  protected Vector<Locale> locales = new Vector<>(singletonList(Locale.getDefault()));

  protected Method method;

  protected String pathInfo = "";

  protected String pathTranslated;

  protected BufferedReader reader;

  protected boolean readerRetrieved;

  protected String remoteAddr = "127.0.0.1";

  protected String remoteHost;

  protected int remotePort = 10000;

  protected String remoteUser;

  protected String scheme;

  protected String serverName;

  protected int serverPort;

  protected String servletPath = "";

  protected MockHttpSession session;

  protected String uri;

  private String overrideMethod;

  protected MockHttpServletRequest(MockContainer container) {
    this(null, container);
  }

  protected MockHttpServletRequest(String uri, MockContainer container) {
    this(uri, null, false, null, container);
  }

  protected MockHttpServletRequest(String uri, Locale locale, boolean post, String encoding, MockContainer container) {
    this(Collections.emptyMap(), uri, encoding, locale, post, container);
  }


  protected MockHttpServletRequest(Map<String, List<String>> parameters, String uri, String encoding,
                                   Locale locale, boolean post, MockContainer container) {
    this.parameters.putAll(parameters);
    this.uri = uri;
    this.encoding = encoding;
    this.locales.add(locale);
    this.method = post ? Method.POST : Method.GET;
    this.container = container;
    this.context = container.getContext();

    // These also set headers
    setScheme("HTTP");
    setServerName("localhost");
    setServerPort(10_000);

    if (post) {
      setContentType("application/x-www-form-urlencoded");
    }

    // User agent
    if (headers.keySet().stream().noneMatch(name -> name.equalsIgnoreCase("User-Agent"))) {
      // The value we add to the headers map must be mutable.
      List<String> values = new ArrayList<>();
      values.add("Prime-Mock");
      headers.put("User-Agent", values);
    }
  }

  /**
   * Adds a file to the HTTP request body. This must be called if the content type is not set and the InputStream hasn't
   * been set or retrieved.
   *
   * @param key         The name of the form field.
   * @param file        The file to add.
   * @param contentType The content type of the file.
   */
  public void addFile(String key, File file, String contentType) {
    if (contentType == null || file == null) {
      throw new IllegalArgumentException("The FileInfo must have a file and a contentType");
    }
    if (inputStreamRetrieved) {
      throw new IllegalStateException("InputStream retrieved already. Can't add a file to the HTTP request");
    }
    if (readerRetrieved) {
      throw new IllegalStateException("Reader retrieved already. Can't add a file to the HTTP request");
    }
    if (this.contentType != null) {
      throw new IllegalStateException("Content-Type set already. Can't add a file to the HTTP request");
    }

    this.contentType = "multipart/form-data, boundary=primeframeworkmultipartuploadLKAlskld09309djoid";
    this.files.put(key, new FileInfo(file, key, contentType));
  }

  //-------------------------------------------------------------------------
  //  javax.servlet.ServletRequest methods
  //-------------------------------------------------------------------------

  /**
   * Allows a header to be added.
   *
   * @param name  The header name.
   * @param value The header value.
   */
  public void addHeader(String name, String value) {
    List<String> values = headers.computeIfAbsent(name, key -> new ArrayList<>());

    // Special cases
    if (name.equalsIgnoreCase("user-agent")) {
      values.clear();
    }

    values.add(value);
  }

  /**
   * Adds a request locale.
   *
   * @param locale The locale.
   */
  public void addLocale(Locale locale) {
    this.locales.add(locale);
  }

  @Override
  public boolean authenticate(HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String changeSessionId() {
    throw new UnsupportedOperationException();
  }

  /**
   * Clears all the attributes
   */
  public void clearAttributes() {
    attributes.clear();
  }

  /**
   * Clears all the cookies.
   */
  public void clearCookies() {
    this.cookies.clear();
  }

  /**
   * Clears all the request locales.
   */
  public void clearLocales() {
    locales.clear();
  }

  /**
   * Clears all the parameters
   */
  public void clearParameters() {
    parameters.clear();
  }

  public void copyCookiesFromUserAgent() {
    cookies.clear();
    cookies.addAll(container.getUserAgent().getCookies(this));
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new UnsupportedOperationException();
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  /**
   * The attribute names.
   */
  public Enumeration<String> getAttributeNames() {
    return new Vector<>(attributes.keySet()).elements();
  }

  /**
   * Local clients don't authenticate
   */
  public String getAuthType() {
    return null;
  }

  /**
   * Returns the encoding which defaults to null unless it is set
   */
  public String getCharacterEncoding() {
    return encoding;
  }

  /**
   * This should set a new character encoding
   */
  public void setCharacterEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * @return If the input stream or the reader are setup, this will return the length of those using the available
   * method.
   */
  public int getContentLength() {
    if (inputStream != null) {
      try {
        return inputStream.available();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }

    return -1;
  }

  @Override
  public long getContentLengthLong() {
    return 0;
  }

  /**
   * @return The content type.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the content type of the request.
   *
   * @param contentType The new content type.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
    headers.computeIfAbsent("Content-Type", key -> new ArrayList<>()).add(contentType);
  }

  /**
   * @return The context path, which defaults to empty String.
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * Sets the context path.
   *
   * @param contextPath The context path.
   */
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  /**
   * @return Any cookies setup.
   */
  public Cookie[] getCookies() {
    List<Cookie> cookies = container.getUserAgent().getCookies(this);
    // Return null when no cookies are present to be spec compliant. This ensures we will hit a NPE in tests just like we would at runtime.
    if (cookies.isEmpty()) {
      return null;
    }

    return cookies.toArray(new Cookie[]{});
  }

  /**
   * @return The list of cookies.
   */
  public List<Cookie> getCookiesList() {
    return container.getUserAgent().getCookies(this);
  }

  /**
   * @param name The name of the header.
   * @return The date header (if it exists), or -1.
   */
  public long getDateHeader(String name) {
    String value = getHeader(name);
    if (value == null) {
      return -1;
    }

    return Long.parseLong(value);
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new UnsupportedOperationException();
  }

  /**
   * @param name The name of the header.
   * @return The header or null.
   */
  public String getHeader(String name) {
    if (name == null) {
      return null;
    }

    for (String key : headers.keySet()) {
      if (key.equalsIgnoreCase(name)) {
        return headers.get(key).get(0);
      }
    }

    return null;
  }

  /**
   * @return The header names.
   */
  public Enumeration<String> getHeaderNames() {
    return new Vector<>(headers.keySet()).elements();
  }

  /**
   * @param name The name of the headers.
   * @return The headers, never null.
   */
  public Enumeration<String> getHeaders(String name) {
    for (String key : headers.keySet()) {
      if (key.equalsIgnoreCase(name)) {
        return new Vector<>(headers.get(key)).elements();
      }
    }

    return new Vector<String>().elements();
  }

  /**
   * @return The input stream.
   * @throws IOException If the reader was already retrieved.
   */
  public ServletInputStream getInputStream() throws IOException {
    if (readerRetrieved) {
      throw new IOException("Reader has already been retrieved.");
    }

    if (files.size() > 0 && inputStream == null) {
      inputStream = new MultipartInputStream(parameters, files);
    } else if (inputStream == null) {
      inputStream = new MockServletInputStream();
    }

    inputStreamRetrieved = true;
    return inputStream;
  }

  /**
   * Sets the input stream.
   *
   * @param inputStream The input stream.
   */
  public void setInputStream(ServletInputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * @param name The name of the header.
   * @return The header or -1.
   */
  public int getIntHeader(String name) {
    String value = getHeader(name);
    if (value == null) {
      return -1;
    }

    return Integer.parseInt(value);
  }

  public String getLocalAddr() {
    return null;
  }

  /**
   * @return The local name.
   */
  public String getLocalName() {
    return localName;
  }

  /**
   * Sets the local name.
   *
   * @param localName The local name.
   */
  public void setLocalName(String localName) {
    this.localName = localName;
  }

  public int getLocalPort() {
    return 0;
  }

  /**
   * @return The system default locale if nothing was added or setup in the constructor or using the setter methods. If
   * there are multiple locales setup, this returns the first one.
   */
  public Locale getLocale() {
    if (locales.isEmpty()) {
      return Locale.getDefault();
    }

    return locales.get(0);
  }

  /**
   * @return The request locales.
   */
  public Enumeration<Locale> getLocales() {
    return locales.elements();
  }

  /**
   * @return The request locales vector (changes effect the internal Vector).
   */
  public Vector<Locale> getLocalesVector() {
    return locales;
  }


  //-------------------------------------------------------------------------
  //  javax.servlet.http.HttpServletRequest methods
  //-------------------------------------------------------------------------

  /**
   * @return GET or POST, depending on the constructor or post flag setup.
   */
  public String getMethod() {
    if (overrideMethod == null) {
      return method.toString();
    }

    return overrideMethod;
  }

  /**
   * Sets the method of the request.
   *
   * @param method The method.
   */
  public void setMethod(Method method) {
    this.method = method;
    if (method == Method.POST && contentType == null) {
      contentType = "application/x-www-form-urlencoded";
    }
  }

  /**
   * @return The parameter or null.
   */
  public String getParameter(String name) {
    if (doesNotHaveParameters()) {
      return null;
    }

    List<String> list = parameters.get(name);
    if (list != null && list.size() > 0) {
      return list.get(0);
    }

    return null;
  }

  public Map<String, String[]> getParameterMap() {
    if (doesNotHaveParameters()) {
      return emptyMap();
    }

    Map<String, String[]> params = new LinkedHashMap<>();
    for (String key : parameters.keySet()) {
      params.put(key, parameters.get(key).toArray(new String[0]));
    }

    return params;
  }

  public Enumeration<String> getParameterNames() {
    if (doesNotHaveParameters()) {
      return new Vector<String>().elements();
    }

    return new Vector<>(parameters.keySet()).elements();
  }

  public String[] getParameterValues(String name) {
    if (doesNotHaveParameters()) {
      return null;
    }

    List<String> list = parameters.get(name);
    if (list != null) {
      return list.toArray(new String[0]);
    }

    return null;
  }

  /**
   * @return The parameter map.
   */
  public Map<String, List<String>> getParameters() {
    return parameters;
  }

  @Override
  public Part getPart(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Part> getParts() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The path info.
   */
  public String getPathInfo() {
    return pathInfo;
  }

  /**
   * Sets the path info.
   *
   * @param pathInfo The path info.
   */
  public void setPathInfo(String pathInfo) {
    this.pathInfo = pathInfo;
  }

  /**
   * @return The path translated.
   */
  public String getPathTranslated() {
    return pathTranslated;
  }

  /**
   * Sets the path translated.
   *
   * @param pathTranslated The path translated.
   */
  public void setPathTranslated(String pathTranslated) {
    this.pathTranslated = pathTranslated;
  }

  /**
   * @return Always HTTP/1.0
   */
  public String getProtocol() {
    return "HTTP/1.0";
  }

  /**
   * @return The query string.
   */
  public String getQueryString() {
    StringBuilder build = new StringBuilder();
    for (String key : parameters.keySet()) {
      List<String> list = parameters.get(key);
      for (String value : list) {
        if (build.length() > 0) {
          build.append("&");
        }

        try {
          build.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return build.toString();
  }

  /**
   * @return The reader.
   * @throws IOException If the input stream was already retrieved.
   */
  public BufferedReader getReader() throws IOException {
    if (inputStreamRetrieved) {
      throw new IOException("InputStream already retrieved.");
    }

    if (inputStream == null) {
      inputStream = new MockServletInputStream();
    }

    if (reader == null) {
      if (encoding != null) {
        reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
      } else {
        reader = new BufferedReader(new InputStreamReader(inputStream));
      }
    }

    readerRetrieved = true;
    return reader;
  }

  /**
   * Sets the reader.
   *
   * @param reader The reader.
   */
  public void setReader(BufferedReader reader) {
    this.reader = reader;
  }

  /**
   * @deprecated
   */
  public String getRealPath(String url) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The remote address.
   */
  public String getRemoteAddr() {
    return remoteAddr;
  }

  /**
   * Sets the remote address.
   *
   * @param remoteAddr The remote address.
   */
  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  /**
   * @return The remote host.
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * Sets the remote host.
   *
   * @param remoteHost The remote host.
   */
  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  /**
   * @return The remote port.
   */
  public int getRemotePort() {
    return remotePort;
  }

  /**
   * Sets the remote port.
   *
   * @param remotePort The remote port.
   */
  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  /**
   * @return The remote user.
   */
  public String getRemoteUser() {
    return remoteUser;
  }

  /**
   * Sets the remote user.
   *
   * @param remoteUser The remote user.
   */
  public void setRemoteUser(String remoteUser) {
    this.remoteUser = remoteUser;
  }

  /**
   * The mock request dispatcher for the given path.
   *
   * @param thePath The path.
   * @return The request dispatcher.
   */
  public RequestDispatcher getRequestDispatcher(String thePath) {
    if (thePath == null) {
      return null;
    }

    String fullPath;

    // The spec says that the path can be relative, in which case it will
    // be relative to the request. So for relative paths, we need to take
    // into account the simulated URL (ServletURL).
    if (thePath.startsWith("/")) {

      fullPath = thePath;

    } else {

      String pI = getPathInfo();
      if (pI == null) {
        fullPath = catPath(getServletPath(), thePath);
      } else {
        fullPath = catPath(getServletPath() + pI, thePath);
      }

      if (fullPath == null) {
        return null;
      }
    }

    dispatcher = new MockRequestDispatcher(fullPath);
    return dispatcher;
  }

  /**
   * @return The RequestDispatcher if one was created from this Request
   */
  public MockRequestDispatcher getRequestDispatcher() {
    return dispatcher;
  }

  /**
   * @return The request URI.
   */
  public String getRequestURI() {
    return uri;
  }

  public StringBuffer getRequestURL() {
    return new StringBuffer(getBaseURL() + contextPath + uri);
  }

  /**
   * @return Nothing, not implemented
   */
  public String getRequestedSessionId() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The scheme, which defaults to HTTP.
   */
  public String getScheme() {
    return scheme;
  }

  //-------------------------------------------------------------------------
  //                            Helper methods
  //-------------------------------------------------------------------------

  /**
   * Sets the scheme, which defaults to HTTP.
   *
   * @param scheme The scheme.
   */
  public void setScheme(String scheme) {
    this.scheme = scheme;
    updateCommonHeaders();
  }


  //-------------------------------------------------------------------------
  //                          Modification Methods
  //-------------------------------------------------------------------------

  /**
   * @return The server name, which defaults to localhost.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Sets the server name, which defaults to localhost.
   *
   * @param serverName The server name.
   */
  public void setServerName(String serverName) {
    this.serverName = serverName;
    updateCommonHeaders();
  }

  /**
   * @return The server port, which defaults to 80.
   */
  public int getServerPort() {
    return serverPort;
  }

  /**
   * Sets the server port.
   *
   * @param serverPort The server port.
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
    updateCommonHeaders();
  }

  @Override
  public ServletContext getServletContext() {
    return context;
  }

  /**
   * @return The servlet path, which defaults to the empty string.
   */
  public String getServletPath() {
    return servletPath;
  }

  /**
   * Sets the servlet path.
   *
   * @param servletPath The servlet path.
   */
  public void setServletPath(String servletPath) {
    this.servletPath = servletPath;
  }

  /**
   * @return The session.
   */
  public HttpSession getSession() {
    return container.getSession();
  }

  /**
   * Sets a new session.
   *
   * @param session The new session.
   */
  public void setSession(MockHttpSession session) {
    this.session = session;
  }

  /**
   * @return The session.
   */
  public HttpSession getSession(boolean create) {
    return container.getSession(create);
  }

  /**
   * @return Nothing, this isn't implemented.
   */
  public Principal getUserPrincipal() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAsyncStarted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAsyncSupported() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Nothing, this isn't implemented.
   */
  public boolean isRequestedSessionIdFromCookie() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Nothing, this isn't implemented.
   */
  public boolean isRequestedSessionIdFromURL() {
    throw new UnsupportedOperationException();
  }

  /**
   * @deprecated
   */
  public boolean isRequestedSessionIdFromUrl() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Nothing, this isn't implemented.
   */
  public boolean isRequestedSessionIdValid() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return True if the scheme is HTTPS, false otherwise.
   */
  public boolean isSecure() {
    return scheme.equals("HTTPS");
  }

  /**
   * @return Nothing, this isn't implemented.
   */
  public boolean isUserInRole(String role) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void login(String username, String password) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void logout() {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes the attribute with the name given.
   *
   * @param name The name of the attribute.
   */
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  /**
   * Removes all of the headers with the given name.
   *
   * @param name The name.
   */
  public void removeHeader(String name) {
    headers.remove(name);
  }

  /**
   * Removes all the values of the request parameter with the given name
   *
   * @param name The name of the parameter.
   */
  public void removeParameter(String name) {
    parameters.remove(name);
  }

  /**
   * Sets the attribute given.
   *
   * @param name  The name of the attribute.
   * @param value The attribute value.
   */
  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  /**
   * Modifies the encoding.
   *
   * @param encoding The encoding.
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setOverrideMethod(String method) {
    overrideMethod = method;
  }

  /**
   * Sets the request parameter with the given name to the given value
   *
   * @param name  The name of the parameter.
   * @param value The value of the parameter.
   */
  public void setParameter(String name, String value) {
    List<String> list = parameters.computeIfAbsent(name, k -> new ArrayList<>());
    list.add(value);
  }

  /**
   * Sets the request parameter with the given name to the given values
   *
   * @param name   The name of the parameter.
   * @param values The values of the parameter.
   */
  public void setParameters(String name, String... values) {
    parameters.put(name, asList(values));
  }

  /**
   * Sets the request to a POST method.
   *
   * @param post True for a POST.
   */
  public void setPost(boolean post) {
    this.method = post ? Method.POST : Method.GET;

    if (post && contentType == null) {
      contentType = "application/x-www-form-urlencoded";
    }
  }

  /**
   * Modifies the request URI.
   *
   * @param uri The request URI.
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
    throw new UnsupportedOperationException();
  }

  protected String getBaseURL() {
    return scheme.toLowerCase() + "://" + serverName + (serverPort != 80 ? ":" + serverPort : "");
  }

  /**
   * Will concatenate 2 paths, normalising it. For example : ( /a/b/c + d = /a/b/d, /a/b/c + ../d = /a/d ). Code
   * borrowed from Tomcat 3.2.2 !
   *
   * @param theLookupPath the first part of the path
   * @param thePath       the part to add to the lookup path
   * @return the concatenated thePath or null if an error occurs
   */
  String catPath(String theLookupPath, String thePath) {
    // Cut off the last slash and everything beyond
    int index = theLookupPath.lastIndexOf("/");
    if (index == -1) {
      return thePath;
    }

    theLookupPath = theLookupPath.substring(0, index);

    // Deal with .. by chopping dirs off the lookup thePath
    while (thePath.startsWith("../")) {
      index = theLookupPath.lastIndexOf("/");
      if (theLookupPath.length() > 0) {
        theLookupPath = theLookupPath.substring(0, index);
      } else {
        // More ..'s than dirs, return null
        return null;
      }

      index = thePath.indexOf("../") + 3;
      thePath = thePath.substring(index);
    }

    return theLookupPath + "/" + thePath;
  }

  /**
   * Simulated how the container drains the InputStream when parameters are retrieved and the content-type is form
   * encoded.
   *
   * @return True if the parameters are still good, false if there is an input stream to be used.
   */
  private boolean doesNotHaveParameters() {
    if (method == Method.POST && contentType != null && contentType.equals("application/x-www-form-urlencoded")) {
      inputStream = new MockServletInputStream(new byte[0]);
      return false;
    }

    return !files.isEmpty();
  }

  private void updateCommonHeaders() {
    headers.remove("Origin");
    headers.computeIfAbsent("Origin", key -> new ArrayList<>()).add(getBaseURL());
    headers.remove("Referer");
    headers.computeIfAbsent("Referer", key -> new ArrayList<>()).add(getBaseURL() + "/referral-path");
  }

  public enum Method {
    GET,
    POST,
    PUT,
    HEAD,
    OPTIONS,
    DELETE,
    TRACE,
    CONNECT,
    PATCH
  }
}
