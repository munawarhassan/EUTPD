package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.ENCODING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;

public class XmlRoundTripTest extends MockitoTestCase {

    private static final String NASTY_STRING = "      <?xml version=\"1.0\"?><note>\n"
            + "function matchwo(a,b)\n{     \nif (a < b && a < 0) then\n  {\n"
            + "  return 1; \\    \\u0043  \\\\ \\\\usdfgsfg  \n  }\r\n\telse\n"
            + "  {\n  return 0;\\n  }\n}&#40;\n</note>    ";

    @Mock
    private ICancelState cancelState;

    @Mock
    private ILiquibaseBackupMonitor backupMonitor;

    @Mock
    private ILiquibaseAccessor dao;

    @Captor
    private ArgumentCaptor<InsertDataChange> insertCaptor;

    private Map<String, Object> originalRow;

    @Mock
    private ILiquibaseRestoreMonitor restoreMonitor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        final Answer<Void> applyEffect = invocation -> {
            final Consumer<ILiquibaseAccessor> effect = (Consumer<ILiquibaseAccessor>) invocation.getArguments()[0];
            effect.accept(dao);
            return null;
        };
        doAnswer(applyEffect).when(dao).withLock(any(Consumer.class));
        doReturn("unknown").when(dao).getDatabaseType();
    }

    @Test
    public void testRoundTrip() throws XMLStreamException, UnsupportedEncodingException {
        // Insert some values
        final ImmutableMap.Builder<String, Object> rowBuilder = ImmutableMap.builder();
        rowBuilder.put("empty", "");
        rowBuilder.put("integer", "1234567890");
        rowBuilder.put("text", "Just some normal text");
        rowBuilder.put("complex", NASTY_STRING);
        rowBuilder.put("cdata", "<node>text</node>");
        rowBuilder.put("cdata2", "\\]\\]\\> qwe");
        rowBuilder.put("cdata3", "[[[]]] qwe");
        rowBuilder.put("html-encoding", "%5D%5D%3E qwe"); // ]]> but in HTML encoding
        rowBuilder.put("html-encoding2", "%5D%5D&gt; qwe"); // ]]> but in HTML encoding
        rowBuilder.put("international", "hellø костя");
        rowBuilder.put("special-characters", "~`!@#$%^&*()_+-={}|[]\\:\";'<>?,./");
        rowBuilder.put("large-string", StringUtils.repeat("text", 4000));
        rowBuilder.put("utf-encoded", "\\u005d\\u005d\\u003e qwe");
        rowBuilder.put("ascii-encoded", "&#093;&#093;&#062; qwe"); // ]]> in ASCII

        originalRow = rowBuilder.build();

        // Do the round trip
        deserialize(serializeRow(), dao);

        // Get row after round trip
        verify(dao).insert(insertCaptor.capture());
        final InsertDataChange insertion = insertCaptor.getValue();
        final List<ColumnConfig> actualRow = insertion.getColumns();

        // Assert that whatever we inserted remain intact after round trip
        final Iterator<Map.Entry<String, Object>> originalIterator = originalRow.entrySet().iterator();
        final Iterator<ColumnConfig> transformedIterator = actualRow.iterator();
        while (originalIterator.hasNext() && transformedIterator.hasNext()) {
            final Map.Entry<String, Object> original = originalIterator.next();
            assertEquals(original.getValue(),
                transformedIterator.next().getValue(),
                "Round trip failed for column:" + original.getKey());
        }
    }

    // ------------------------------------------------------------------------------------------------------------------
    // Helper methods used by the test
    // ------------------------------------------------------------------------------------------------------------------

    private void deserialize(final String changeLog, final ILiquibaseAccessor dao) throws UnsupportedEncodingException {
        final InputStream in = new ByteArrayInputStream(changeLog.getBytes(ENCODING));
        new DefaultLiquibaseMigrationDao(mock(ILiquibaseXmlWriterFactory.class))
                .restore(dao, in, null, restoreMonitor, cancelState);
    }

    private String serializeRow() throws XMLStreamException, UnsupportedEncodingException {
        final DefaultLiquibaseXmlWriterFactory writerFactory = new DefaultLiquibaseXmlWriterFactory();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ILiquibaseXmlWriter writer = writerFactory.create(buffer, "test.user");

        writer.writeStartDocument(ENCODING, "1.0");
        writer.writeDatabaseChangeLogStartElement();
        writer.writeChangeSetStartElement("test");
        new WriteXmlForRowConsumer(writer, "test", originalRow.keySet(), backupMonitor).accept(originalRow);
        writer.writeEndDocument(); // Closes any start tags and writes corresponding end tags

        writer.flush();
        writer.close();

        return buffer.toString(ENCODING);
    }
}
