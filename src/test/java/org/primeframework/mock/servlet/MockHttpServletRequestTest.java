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
 */
package org.primeframework.mock.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the mocks.
 *
 * @author Brian Pontarelli
 */
public class MockHttpServletRequestTest {
  @Test
  public void addHeader() {
    MockHttpServletRequest request = new MockContainer().newServletRequest("/foo");

    // Add a single header
    request.addHeader("User-Agent", "Netscape Navigator");
    assertEquals(request.getHeader("User-Agent"), "Netscape Navigator");

    // Add it twice, this is a special case, and only one is added.
    request.addHeader("User-Agent", "Internet Explorer 3.0.1");
    assertEquals(request.getHeader("User-Agent"), "Internet Explorer 3.0.1");
  }

  @Test
  public void getHeaders() {
    MockHttpServletRequest request = new MockContainer().newServletRequest("/foo");

    // Case insensitive get string
    request.headers.put("Content-Type", new ArrayList<>(singletonList("application/json")));
    assertEquals(request.getHeader("content-Type"), "application/json");
    assertEquals(request.getHeader("content-type"), "application/json");
    assertEquals(request.getHeader("Content-typE"), "application/json");
    assertEquals(request.getHeader("Content-Type"), "application/json");

    // Case insensitive get list
    Enumeration<String> actual = request.getHeaders("Content-Type");
    List<String> actualList = new ArrayList<>();
    List<String> expected = new ArrayList<>(singletonList("application/json"));

    while (actual.hasMoreElements()) {
      actualList.add(actual.nextElement());
    }

    assertEquals(actualList.size(), expected.size());
    assertEquals(actualList, expected);

    // int helper
    request.headers.put("number", new ArrayList<>(singletonList(String.valueOf(42))));
    assertEquals(request.getIntHeader("number"), 42);

    // long helper
    long currentTime = System.currentTimeMillis();
    request.headers.put("date", new ArrayList<>(singletonList(String.valueOf(currentTime))));
    assertEquals(request.getDateHeader("date"), currentTime);
  }

  @Test
  public void multipart() throws Exception {
    MockHttpServletRequest request = new MockContainer().newServletRequest("/foo");
    request.addFile("file", new File("src/test/java/org/primeframework/mock/servlet/test-file.txt"), "text/plain");
    request.setParameter("test", "test");

    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    assertEquals(2, items.size());
    assertTrue(items.get(0).isFormField());
    assertEquals(items.get(0).getFieldName(), "test");
    assertEquals(items.get(0).getString(), "test");

    assertFalse(items.get(1).isFormField());
    assertEquals(items.get(1).getFieldName(), "file");
    assertEquals(items.get(1).getName(), "test-file.txt");
    assertEquals(items.get(1).getContentType(), "text/plain");

    File file = File.createTempFile("fileuploadtest", ".txt");
    items.get(1).write(file);

    String original = new String(Files.readAllBytes(Paths.get("src/test/java/org/primeframework/mock/servlet/test-file.txt")));
    assertEquals(original, new String(Files.readAllBytes(file.toPath())));
  }

  @Test
  public void multipartJARFile() throws Exception {
    test(new File("src/test/java/org/primeframework/mock/servlet/test.jar"), "application/java-archive");
    test(new File("src/test/java/org/primeframework/mock/servlet/test.gif"), "image/gif");
  }

  @Test
  public void sessionDefaults() throws Exception {
    MockHttpServletRequest request = new MockContainer().newServletRequest("/foo");
    assertNull(request.session);
    assertNull(request.getSession(false));
    assertNotNull(request.getSession());

    request = new MockContainer().newServletRequest("/foo", Locale.ENGLISH, true, "UTF-8");
    assertNull(request.session);
    assertNull(request.getSession(false));
    assertNotNull(request.getSession());

    request = new MockContainer().newServletRequest(Collections.emptyMap(), "/foo", "UTF-8", Locale.ENGLISH, true);
    assertNull(request.session);
    assertNull(request.getSession(false));
    assertNotNull(request.getSession());
  }

  private byte[] read(File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8194];
    int len;
    while ((len = fis.read(buf)) != -1) {
      baos.write(buf, 0, len);
    }
    return buf;
  }

  private void test(File file, String contentType) throws Exception {
    MockHttpServletRequest request = new MockContainer().newServletRequest("/foo");
    request.addFile("file", file, contentType);
    request.setParameter("test", "test");

    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    assertEquals(2, items.size());
    assertTrue(items.get(0).isFormField());
    assertEquals(items.get(0).getFieldName(), "test");
    assertEquals(items.get(0).getString(), "test");

    assertFalse(items.get(1).isFormField());
    assertEquals(items.get(1).getFieldName(), "file");
    assertEquals(items.get(1).getName(), file.getName());
    assertEquals(items.get(1).getContentType(), contentType);

    File tmp = File.createTempFile("fileuploadtest", "bin");
    items.get(1).write(tmp);

    byte[] orig = read(file);
    byte[] read = read(tmp);

    assertEquals(orig.length, read.length);
    for (int i = 0; i < orig.length; i++) {
      assertEquals(orig[i], read[i], "Byte at index " + i + " was invalid");
    }
  }
}
