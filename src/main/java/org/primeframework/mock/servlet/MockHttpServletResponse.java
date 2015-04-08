/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class is a mock servlet response.
 *
 * @author Brian Pontarelli
 */
public class MockHttpServletResponse implements HttpServletResponse {
  protected int code;

  protected boolean committed;

  protected String contentType;

  protected List<Cookie> cookies = new ArrayList<Cookie>();

  protected String encoding;

  protected boolean flushed;

  protected Map<String, List<String>> headers = new HashMap<>();

  protected long length;

  protected Locale locale;

  protected String message;

  protected String redirect;

  protected boolean reset;

  protected int size;

  protected MockServletOutputStream stream = new MockServletOutputStream();

  public void addCookie(Cookie cookie) {
    cookies.add(cookie);
  }

  public void addDateHeader(String name, long date) {
    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(Long.toString(date));
  }

  public void addHeader(String name, String value) {
    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(value);
  }

  public void addIntHeader(String name, int value) {
    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(Integer.toString(value));
  }

  public boolean containsHeader(String name) {
    return headers.containsKey(name);
  }

  public String encodeRedirectURL(String s) {
    throw new UnsupportedOperationException("Not used in this MVC");
  }

  public String encodeRedirectUrl(String s) {
    throw new UnsupportedOperationException("Not used in this MVC");
  }

  public String encodeURL(String s) {
    throw new UnsupportedOperationException("Not used in this MVC");
  }

  public String encodeUrl(String s) {
    throw new UnsupportedOperationException("Not used in this MVC");
  }

  public void flushBuffer() throws IOException {
    this.flushed = true;
  }

  public int getBufferSize() {
    return size;
  }

  public void setBufferSize(int size) {
    this.size = size;
  }

  public String getCharacterEncoding() {
    return encoding;
  }

  public void setCharacterEncoding(String encoding) {
    this.encoding = encoding;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public List<Cookie> getCookies() {
    return cookies;
  }

  public void setCookies(List<Cookie> cookies) {
    this.cookies = cookies;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @Override
  public String getHeader(String name) {
    List<String> list = headers.get(name);
    if (list == null || list.isEmpty()) {
      return null;
    }

    return list.get(0);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return headers.keySet();
  }

  @Override
  public Collection<String> getHeaders(String name) {
    List<String> list = headers.get(name);
    if (list == null || list.isEmpty()) {
      return null;
    }

    return list;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public int getLength() {
    return (int) length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return stream;
  }

  public String getRedirect() {
    return redirect;
  }

  public void setRedirect(String redirect) {
    this.redirect = redirect;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public int getStatus() {
    return code;
  }

  public void setStatus(int code) {
    this.code = code;
  }

  public MockServletOutputStream getStream() {
    return stream;
  }

  public void setStream(MockServletOutputStream stream) {
    this.stream = stream;
  }

  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(stream);
  }

  public boolean isCommitted() {
    return committed;
  }

  public void setCommitted(boolean committed) {
    this.committed = committed;
  }

  public boolean isFlushed() {
    return flushed;
  }

  public void setFlushed(boolean flushed) {
    this.flushed = flushed;
  }

  public boolean isReset() {
    return reset;
  }

  public void setReset(boolean reset) {
    this.reset = reset;
  }

  public void reset() {
    this.reset = true;
  }

  public void resetBuffer() {
    this.reset = true;
  }

  public void sendError(int code, String message) throws IOException {
    this.code = code;
    this.message = message;
  }

  public void sendError(int code) throws IOException {
    this.code = code;
  }

  public void sendRedirect(String url) throws IOException {
    this.redirect = url;
    this.code = HttpServletResponse.SC_FOUND;
  }

  public void setContentLength(int length) {
    this.length = length;
  }

  @Override
  public void setContentLengthLong(long len) {
    this.length = len;
  }

  public void setDateHeader(String name, long date) {
    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(Long.toString(date));
  }

  public void setHeader(String name, String value) {
    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(value);
  }

  public void setIntHeader(String name, int value) {
    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(Integer.toString(value));
  }

  public void setStatus(int code, String message) {
    this.code = code;
    this.message = message;
  }
}