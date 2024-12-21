package com.pmi.tpd.euceg.backend.core.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.activation.DataHandler;

import org.eu.ceg.Attachment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.CharStreams;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class InputStreamDataSourceTest extends TestCase {

    boolean indent;

    @BeforeEach
    public void start() {
        indent = Eucegs.indentMarshalling(true);
    }

    @AfterEach
    public void stop() {
        Eucegs.indentMarshalling(indent);
    }

    @Test
    public void shouldIncludeInputStreamContent() throws IOException {
        final Attachment attachment = new Attachment().withContent(
            new DataHandler(new InputStreamDataSource(new ByteArrayInputStream("test text".getBytes()), "text/plain")));
        final String actual = Eucegs.marshal(attachment);
        approve(actual);
        try (Reader reader = new InputStreamReader(
                Eucegs.unmarshal(actual.getBytes(), Attachment.class).getContent().getInputStream())) {
            assertEquals("test text", CharStreams.toString(reader));
        }
    }
}
