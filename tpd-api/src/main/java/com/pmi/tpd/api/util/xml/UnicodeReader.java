package com.pmi.tpd.api.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class UnicodeReader extends Reader {
  private static final int BOM_SIZE = 4;
  private final InputStreamReader reader;

  /**
   * Construct UnicodeReader
   * 
   * @param in              Input stream.
   * @param defaultEncoding Default encoding to be used if BOM is not found,
   *                        or <code>null</code> to use system default encoding.
   * @throws IOException If an I/O error occurs.
   */
  public UnicodeReader(final InputStream in, final String defaultEncoding) throws IOException {
    final byte bom[] = new byte[BOM_SIZE];
    String encoding;
    int unread;
    final PushbackInputStream pushbackStream = new PushbackInputStream(in, BOM_SIZE);
    final int n = pushbackStream.read(bom, 0, bom.length);

    // Read ahead four bytes and check for BOM marks.
    if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
      encoding = "UTF-8";
      unread = n - 3;
    } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
      encoding = "UTF-16BE";
      unread = n - 2;
    } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
      encoding = "UTF-16LE";
      unread = n - 2;
    } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE)
        && (bom[3] == (byte) 0xFF)) {
      encoding = "UTF-32BE";
      unread = n - 4;
    } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00)
        && (bom[3] == (byte) 0x00)) {
      encoding = "UTF-32LE";
      unread = n - 4;
    } else {
      encoding = defaultEncoding;
      unread = n;
    }

    // Unread bytes if necessary and skip BOM marks.
    if (unread > 0) {
      pushbackStream.unread(bom, (n - unread), unread);
    } else if (unread < -1) {
      pushbackStream.unread(bom, 0, 0);
    }

    // Use given encoding.
    if (encoding == null) {
      reader = new InputStreamReader(pushbackStream);
    } else {
      reader = new InputStreamReader(pushbackStream, encoding);
    }
  }

  public String getEncoding() {
    return reader.getEncoding();
  }

  public int read(final char[] cbuf, final int off, final int len) throws IOException {
    return reader.read(cbuf, off, len);
  }

  public void close() throws IOException {
    reader.close();
  }
}