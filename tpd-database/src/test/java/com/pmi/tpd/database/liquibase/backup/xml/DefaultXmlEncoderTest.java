package com.pmi.tpd.database.liquibase.backup.xml;

import com.pmi.tpd.testing.junit5.TestCase;

import org.junit.jupiter.api.Test;

public class DefaultXmlEncoderTest extends TestCase {

    /** An instance of the class under test */
    private static final DefaultXmlEncoder encoder = new DefaultXmlEncoder();

    // ==================================================================================================================
    // Tab character
    // ==================================================================================================================

    @Test
    public void testTabLegalInXml() {
        assertTrue(encoder.isLegalInXml('\t'));
    }

    @Test
    public void testEncodeTabForCdata() {
        assertEquals("\t", encoder.encodeForCdata("\t"));
    }

    @Test
    public void testDecodeTab() {
        assertEquals("\t", encoder.decode("\t"));
    }

    // ==================================================================================================================
    // Carriage return character
    // ==================================================================================================================

    @Test
    public void testCarriageReturnLegalInXml() {
        assertTrue(encoder.isLegalInXml('\r'));
    }

    /**
     * We have to encode the carriage return because stupid Java stuff strips \r when it precedes \n.
     */
    @Test
    public void testEncodeCarriageReturnForCdata() {
        assertEquals("\\u000D", encoder.encodeForCdata("\r"));
    }

    @Test
    public void testDecodeCarriageReturn() {
        assertEquals("\r", encoder.decode("\\u000D"));
    }

    // ==================================================================================================================
    // Line feed character
    // ==================================================================================================================

    @Test
    public void testLineFeedLegalInXml() {
        assertTrue(encoder.isLegalInXml('\n'));
    }

    @Test
    /**
     * We're encoding line feeds in order to avoid any potential - we don't know of any, but have a health paranoia -
     * problems across operating systems.
     */
    public void testEncodeLineFeedForCdata() {
        assertEquals("\\u000A", encoder.encodeForCdata("\n"));
    }

    @Test
    public void testDecodeLineFeed() {
        assertEquals("\n", encoder.decode("\\u000A"));
    }

    // ==================================================================================================================
    // Double-quote character
    // ==================================================================================================================

    @Test
    public void testQuoteLegalInXml() {
        assertTrue(encoder.isLegalInXml('\"'));
    }

    @Test
    public void testEncodeQuoteForCdata() {
        assertEquals("\"", encoder.encodeForCdata("\""));
    }

    @Test
    public void testDecodeQuote() {
        assertEquals("\"", encoder.decode("\""));
    }

    // ==================================================================================================================
    // Characters that are not valid in XML
    // ==================================================================================================================

    @Test
    public void testFormFeedIllegalInXml() {
        assertFalse(encoder.isLegalInXml('\f'));
    }

    @Test
    public void testEncodeFormFeedForCdata() {
        assertEquals("\\u000C", encoder.encodeForCdata("\f"));
    }

    @Test
    public void testDecodeFormFeed() {
        assertEquals("\f", encoder.decode("\\u000C"));
    }

    // ==================================================================================================================
    // Decoding invalid strings
    // (which should never happen, unless the customer fiddles with the backup file)
    // ==================================================================================================================

    @Test
    public void testDecodeBackslashAtEnd() {
        final Throwable exception = assertThrows(RuntimeException.class, () -> encoder.decode("\\"));
        assertEquals("Failed to decode this: \"\\\". Encountered backslash at end of string.", exception.getMessage());
    }

    @Test
    public void testDecodeMalformedUnicode() {
        final Throwable exception = assertThrows(RuntimeException.class, () -> encoder.decode("\\u003"));
        assertEquals("Failed to decode this: \"\\u003\". Malformed Unicode encoding.", exception.getMessage());
    }

    @Test
    public void testDecodeNonIntegerCodePoint() {
        final Throwable exception = assertThrows(RuntimeException.class, () -> encoder.decode("\\u0xyz"));
        assertEquals("Failed to decode this: \"\\u0xyz\". Invalid code point.", exception.getMessage());
    }

    // ==================================================================================================================
    // Tricky strings and edge cases
    // ==================================================================================================================

    @Test
    public void testEncodeEmptyStringForCdata() {
        assertEquals("", encoder.encodeForCdata(""));
    }

    @Test
    public void testDecodeEmptyString() {
        assertEquals("", encoder.decode(""));
    }

    @Test
    public void testEncodeBlankStringForCdata() {
        assertEquals(" ", encoder.encodeForCdata(" "));
    }

    @Test
    public void testDecodeBlankString() {
        assertEquals(" ", encoder.decode(" "));
    }

    @Test
    public void testEncodeEmbeddedCdataForCdata() {
        assertEquals("<![CDATA[<sender>John Smith</sender>]]]]><![CDATA[>",
            encoder.encodeForCdata("<![CDATA[<sender>John Smith</sender>]]>"));
    }

    @Test
    public void testEncodeEscapedUnicodeForCdata() {
        assertEquals("\\\\u0018", encoder.encodeForCdata("\\u0018"));
    }

    @Test
    public void testDecodeEscapedUnicode() {
        assertEquals("\\u0018", encoder.decode("\\\\u0018"));
    }

    @Test
    public void testEncodeNumericCharacterReferenceForCdata() {
        assertEquals("&#13;", encoder.encodeForCdata("&#13;"));
    }

    @Test
    public void testDecodeNumericCharacterReference() {
        assertEquals("&#13;", encoder.decode("&#13;"));
    }

    @Test
    public void testEncodeCarriageReturnLineFeed() {
        assertEquals("\\u000D\\u000A", encoder.encodeForCdata("\r\n"));
    }

    @Test
    public void testEncodeLeadingSpacesForCdata() {
        assertEquals("   abc", encoder.encodeForCdata("   abc"));
    }

    @Test
    public void testDecodeLeadingSpaces() {
        assertEquals("   abc", encoder.decode("   abc"));
    }

    @Test
    public void testEncodeTrailingSpacesForCdata() {
        assertEquals("abc   ", encoder.encodeForCdata("abc   "));
    }

    @Test
    public void testDecodeTrailingSpaces() {
        assertEquals("abc   ", encoder.decode("abc   "));
    }

    // ==================================================================================================================
    // Bi-directional encoding/decoding of complex strings
    // ==================================================================================================================

    /**
     * Sample string has:
     * <ul>
     * <li>Leading spaces</li>
     * <li>Trailing spaces</li>
     * <li>Interstitial spaces</li>
     * <li>Control characters that are valid in XML</li>
     * <li>Characters that are invalid in XML</li>
     * <li>Escaped Unicode character (\\u0018)</li>
     * </ul>
     * This test does not include CDATA because there is no decoding provided (it is done by XML parsers).
     */
    @Test
    public void testComplexEncodeDecode() {
        final String sample = "    \t  \r\n \fa\fb\fc  \n<What the?> \\u0018&#13;  \\\\u0018     ";
        assertEquals(sample, encoder.decode(encoder.encode(sample)));
    }
}
