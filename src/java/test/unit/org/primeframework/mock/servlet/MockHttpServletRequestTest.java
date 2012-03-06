/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class tests the mocks.
 *
 * @author Brian Pontarelli
 */
public class MockHttpServletRequestTest {
  @Test
  public void multipart() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("/foo", new MockServletContext());
    request.addFile("file", new File("src/java/test/unit/org/primeframework/mock/servlet/test-file.txt"), "text/plain");
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

    String original = FileUtils.readFileToString(new File("src/java/test/unit/org/primeframework/mock/servlet/test-file.txt"));
    assertEquals(original, FileUtils.readFileToString(file));
  }

  @Test
  public void multipartJARFile() throws Exception {
    test(new File("src/java/test/unit/org/primeframework/mock/servlet/test.jar"), "application/java-archive");
    test(new File("src/java/test/unit/org/primeframework/mock/servlet/test.gif"), "image/gif");
  }

  private void test(File file, String contentType) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("/foo", new MockServletContext());
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
}