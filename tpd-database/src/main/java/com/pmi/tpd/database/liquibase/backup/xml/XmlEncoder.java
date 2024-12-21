package com.pmi.tpd.database.liquibase.backup.xml;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface XmlEncoder {

  /**
   * Decodes strings that have been encoded as the {@link #encode(String)} method
   * would.
   *
   * @param string
   *               the string to be decoded
   * @return the decoded string
   * @see #encode(String)
   */
  @Nonnull
  String decode(@Nonnull String string);

  /**
   * Produces an encoding of the given string that could be safely used as the
   * value of an XML element.
   *
   * @param string
   *               the string to be encoded
   * @return the encoded string
   * @see #decode(String)
   */
  @Nonnull
  String encode(@Nonnull String string);

  /**
   * Produces an encoding of the given string that is suitable for use as the
   * value of a CDATA section.
   *
   * @param s
   *          the string to be encoded
   * @return the encoded string
   */
  @Nonnull
  String encodeForCdata(@Nonnull String s);

  /**
   * Tests if a given character is legal in XML.
   *
   * @param c
   *          any 16-bit UTF-8 Java character.
   * @return <code>true</code> if the given character can be used in and XML
   *         document without escaping,
   *         <code>false</code> otherwise.
   * @see <a href="http://www.w3.org/TR/REC-xml/#charsets">XML Charsets</a>
   */
  boolean isLegalInXml(char c);
}
