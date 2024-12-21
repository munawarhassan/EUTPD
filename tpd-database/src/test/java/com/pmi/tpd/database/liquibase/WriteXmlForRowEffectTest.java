package com.pmi.tpd.database.liquibase;

import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests the operation of the {@link WriteXmlForRowConsumer} class.
 */
public class WriteXmlForRowEffectTest extends MockitoTestCase {

    @Mock
    ILiquibaseXmlWriter writer;

    @Mock
    I18nService i18nService;

    @Mock
    ILiquibaseBackupMonitor monitor;

    /**
     * Tests that we write out a multi-column row.
     */
    @Test
    public void testMultipleColumns() throws XMLStreamException {
        final Map<String, Object> row = ImmutableMap.<String, Object> of("one", 1, "two", "pickle");
        final WriteXmlForRowConsumer effect = new WriteXmlForRowConsumer(writer, "test",
                Lists.newArrayList("one", "two"), monitor);
        effect.accept(row);

        final InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeStartElement("insert");
        inOrder.verify(writer).writeAttribute("tableName", "test");
        inOrder.verify(writer).writeColumn("one", 1);
        inOrder.verify(writer).writeColumn("two", "pickle");
        inOrder.verify(writer).writeEndElement();
    }

    /**
     * Tests that we can pass a capitalised column name to the effect, and it will find that column in the row even if
     * the name of the column is in lower case in the row.
     */
    @Test
    public void testColumnNamesInUpperCaseAndRowColumnsInLowerCase() throws XMLStreamException {
        final WriteXmlForRowConsumer effect = new WriteXmlForRowConsumer(writer, "test", Lists.newArrayList("ONE"),
                monitor);
        effect.accept(ImmutableMap.<String, Object> of("one", 1));
        verify(writer).writeColumn("one", 1);
    }

    @Test
    public void testColumnNamesInLowerCaseAndRowColumnsInUpperCase() throws XMLStreamException {
        final WriteXmlForRowConsumer effect = new WriteXmlForRowConsumer(writer, "test", Lists.newArrayList("one"),
                monitor);
        effect.accept(ImmutableMap.<String, Object> of("ONE", 1));
        verify(writer).writeColumn("one", 1);
    }

    @Test
    public void testThrowsException() throws Exception {
        assertThrows(LiquibaseDataAccessException.class, () -> {
            doThrow(XMLStreamException.class).when(writer).writeEndElement();
            final WriteXmlForRowConsumer effect = new WriteXmlForRowConsumer(writer, "test", Lists.newArrayList("one"),
                    monitor);
            try {
                effect.accept(ImmutableMap.<String, Object> of("ONE", 1));
                fail("Should have thrown a LiquibaseSystemException");
            } catch (final LiquibaseDataAccessException e) {
                assertEquals(XMLStreamException.class, e.getCause().getClass());
                throw e;
            }
        });
    }

    @Test
    public void testProgressReported() {
        final WriteXmlForRowConsumer effect = new WriteXmlForRowConsumer(writer, "test", Lists.newArrayList("one"),
                monitor);
        effect.accept(ImmutableMap.<String, Object> of("ONE", 1));
        verify(monitor).rowWritten();
    }
}
