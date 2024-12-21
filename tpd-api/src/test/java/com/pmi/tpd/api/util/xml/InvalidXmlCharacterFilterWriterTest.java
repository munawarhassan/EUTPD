package com.pmi.tpd.api.util.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class InvalidXmlCharacterFilterWriterTest extends TestCase {

  @Test
  public void testInvalidCharacter() throws IOException {
    String xml = "<element>\u0002</element>";
    Writer stringWriter = new StringWriter();
    Writer writer= new InvalidXmlCharacterFilterWriter(stringWriter);
    writer.append(xml);
    
    writer.close();

    assertEquals("<element>\uFFFD</element>", stringWriter.toString());

  }

}
