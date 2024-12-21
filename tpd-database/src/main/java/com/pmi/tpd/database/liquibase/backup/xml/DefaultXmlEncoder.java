package com.pmi.tpd.database.liquibase.backup.xml;

import java.util.Arrays;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Chars;

//CHECKSTYLE:OFF
/**
 * An XML encoder that encodes XML-illegal characters, carriage returns, and
 * line feeds by prefixing their Java hex
 * encodings with backslash; and by encoding backslashes as double-backslashes.
 * Some examples:
 * <ul>
 * <li>{@literal '\u0006'} becomes {@literal "\\u0006"}</li>
 * <li>{@literal '\r'} becomes {@literal "\\u000D"}</li>
 * <li>{@literal '\\'} becomes {@literal "\\\\"}</li>
 * </ul>
 * Embedded CDATA sections are split into two, so
 * {@code "<![CDATA[<sender>John Smith</sender>]]>"} becomes {@code
 * "<![CDATA[<sender>John Smith</sender>]]]]><![CDATA[>"}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
// CHECKSTYLE:ON
public class DefaultXmlEncoder implements XmlEncoder {

  /** */
  private static final char BACKSLASH = '\\';

  /** */
  private static final char CARRIAGE_RETURN = '\r';

  /** */
  private static final char LINE_FEED = '\n';

  /**
   * Characters that we have to encode because they are not valid characters in
   * XML.
   */
  private static final char[] ILLEGAL_CHARS = new char[] { '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
      '\u0006', '\u0007', '\u0008', '\u000B', '\u000C', '\u000E', '\u000F', '\u0010', '\u0011', '\u0012',
      '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C',
      '\u001D', '\u001E', '\u001F', '\uFFFE', '\uFFFF' };

  /**
   * Characters that we want to encode in addition to the illegal characters
   * above.
   */
  private static final char[] EXTRAS = new char[] { BACKSLASH, CARRIAGE_RETURN, LINE_FEED };

  /**
   * All of the characters that will be encoded.
   */
  private static final char[] MAPPED_CHARS = Chars.concat(ILLEGAL_CHARS, EXTRAS);

  /**
   * The encodings corresponding to the characters in MAPPED_CHARS array.
   * <p>
   * Yes, yes, we could use a map for this, but that would be much slower because
   * Java doesn't have maps with
   * primitive keys. To avoid boxing and un-boxing, we are doing the mapping
   * ourselves using arrays.
   */
  private static final String[] ENCODINGS;

  static {
    // sort the mapped characters array so that we can do a binary search on it
    // later
    Arrays.sort(MAPPED_CHARS);

    // Set up encodings for each of the mapped characters.
    // Each encoding is at the same position in the ENCODINGS array
    // as it is in the MAPPED_CHARS array.
    ENCODINGS = ObjectArrays.newArray(String.class, MAPPED_CHARS.length);
    for (int i = 0; i < MAPPED_CHARS.length; i++) {

      // We use the standard Java Unicode character encoding, with a
      // double backslash to prevent parsers from treated it as an actual character.
      ENCODINGS[i] = String.format("\\u%04X", (int) MAPPED_CHARS[i]);
    }

    // We encode the backslash as a double backslash.
    final int backslashIndex = Arrays.binarySearch(MAPPED_CHARS, BACKSLASH);
    ENCODINGS[backslashIndex] = "" + BACKSLASH + BACKSLASH;
  }

  /**
   * This implementation is a modified version of
   * {@code com.cenqua.fisheye.util.XmlUtils#unicodeDecode(String)}.
   */
  @Override
  @Nonnull
  public String decode(@Nonnull final String string) {
    // We'll only need this buffer if we encounter a backslash,
    // meaning that the source string contains some encoding.
    StringBuilder buffer = null;

    final int len = string.length();

    // i holds the current position within the source string
    // Note that the value of i is modified by code within the loop
    for (int i = 0; i < len; i++) {
      final char c = string.charAt(i);

      if (c == BACKSLASH) {
        if (buffer == null) {
          buffer = new StringBuilder(string.substring(0, i));
        }
        if (i > len - 2) {
          throw new RuntimeException(
              "Failed to decode this: \"" + string + "\". Encountered backslash at end of string.");
        }
        if (string.charAt(i + 1) == BACKSLASH) {
          // Since the current character is followed by a backslash,
          // we know that we're looking at an encoded backslash.
          buffer.append(BACKSLASH);
          i += 1;
        } else {
          if (i > len - 6) {
            throw new RuntimeException(
                "Failed to decode this: \"" + string + "\". Malformed Unicode encoding.");
          }

          // We're looking at an encoded Unicode character

          // Grab the four digits of the Unicode encoding
          final String codePointString = string.substring(i + 2, i + 6);

          // Decode the Unicode character and add it to the buffer
          int codePoint = 0;
          try {
            codePoint = Integer.parseInt(codePointString, 16);
          } catch (final NumberFormatException e) {
            throw new RuntimeException("Failed to decode this: \"" + string + "\". Invalid code point.");
          }

          buffer.append((char) codePoint);

          // Move past our encoded Unicode character
          i += 5;
        }
      } else if (buffer != null) {
        buffer.append(c);
      }
    }
    return buffer == null ? string : buffer.toString();
  }

  /**
   * This implementation is a modified version of
   * {@code com.cenqua.fisheye.util.XmlUtils#unicodeEncode(String)}.
   */
  @Override
  @Nonnull
  public String encode(@Nonnull final String string) {
    StringBuilder buffer = null;
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);

      // Go looking to see if the current character has to be encoded
      final int index = Arrays.binarySearch(MAPPED_CHARS, c);

      // If the current character is one that needs to be encoded,
      // then we need to start using a buffer
      if (index >= 0 && buffer == null) {
        // Put all the characters traversed so far into the new buffer
        buffer = new StringBuilder(string.substring(0, i));
      }

      // If we're buffering, then continue to buffer
      if (buffer != null) {
        // If index < 0 we know that the current character doesn't need encoding;
        // otherwise look up the encoding for the current character.
        buffer.append(index < 0 ? c : ENCODINGS[index]);
      }
    }
    return buffer == null ? string : buffer.toString();
  }

  @Override
  @Nonnull
  public String encodeForCdata(@Nonnull final String s) {
    Preconditions.checkNotNull(s);
    final StringBuilder buffer = new StringBuilder();
    int index;
    int oldIndex = 0;
    while ((index = s.indexOf("]]>", oldIndex)) > -1) {
      final String chunk = s.substring(oldIndex, index);
      buffer.append(encode(chunk));
      oldIndex = index + 3;
      buffer.append("]]]]><![CDATA[>");
    }
    final String chunk = s.substring(oldIndex);
    buffer.append(encode(chunk));
    return buffer.toString();
  }

  @Override
  public boolean isLegalInXml(final char c) {
    return Arrays.binarySearch(ILLEGAL_CHARS, c) < 0;
  }
}
