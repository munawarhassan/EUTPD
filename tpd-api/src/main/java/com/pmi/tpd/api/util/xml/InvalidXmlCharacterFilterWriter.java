package com.pmi.tpd.api.util.xml;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class InvalidXmlCharacterFilterWriter extends FilterWriter {

  public InvalidXmlCharacterFilterWriter(Writer writer) {
    super(writer);
  }

  public final void write(int ch) throws IOException {
    if (!XMLChar.isInvalid(ch))
      out.write(ch);
    else {
      out.write(EscapingXMLStreamWriter.SUBSTITUTE);
    }
  }

  public final void write(char[] buf, int off, int len) throws IOException {
    for (int i = 0; i < len; i++)
      write(buf[off + i]);
  }

  public final void write(char[] buf) throws IOException {
    write(buf, 0, buf.length);
  }

  public final void write(String buf, int off, int len) throws IOException {
    write(buf.toCharArray(), off, len);
  }

  public final void write(String buf) throws IOException {
    write(buf.toCharArray(), 0, buf.length());
  }

}