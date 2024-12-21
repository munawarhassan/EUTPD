package com.pmi.tpd.api.util.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.google.common.io.CharStreams;
import com.pmi.tpd.testing.junit5.TestCase;

public class InvalidXmlCharacterFilterReaderTest extends TestCase {

  @Test
  public void testInvalidCharacter() throws IOException {
    String xml = "<element>\u0002</element>";
    Reader reader = new InvalidXmlCharacterFilterReader(new StringReader(xml));
    String result = CharStreams.toString(reader);

    assertEquals("<element>\uFFFD</element>", result);

  }

}
