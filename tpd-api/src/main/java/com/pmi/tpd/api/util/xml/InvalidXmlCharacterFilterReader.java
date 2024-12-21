package com.pmi.tpd.api.util.xml;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;


public class InvalidXmlCharacterFilterReader extends FilterReader {

  public InvalidXmlCharacterFilterReader(final Reader in) {
    super(in);
  }

  @Override
  public int read(final char[] cbuf, final int off, final int len) throws IOException {
    final int read = super.read(cbuf, off, len);
    if (read == -1) {
      return read;
    }

    for (int i = off; i < off + read; i++) {
      if (!XMLChar.isValid(cbuf[i])) {
        cbuf[i] = EscapingXMLStreamWriter.SUBSTITUTE;
      }
    }
    return read;
  }
}