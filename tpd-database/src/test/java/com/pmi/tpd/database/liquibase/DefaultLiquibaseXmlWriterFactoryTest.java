package com.pmi.tpd.database.liquibase;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.same;

import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests the operation of the {@link DefaultLiquibaseXmlWriterFactory} class.
 */
public class DefaultLiquibaseXmlWriterFactoryTest extends MockitoTestCase {

    private static final String AUTHOR = "test.user";

    @Mock
    private OutputStream stream;

    private DefaultLiquibaseXmlWriterFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        factory = new DefaultLiquibaseXmlWriterFactory();
    }

    @Test
    public void testCreate() {
        final DefaultLiquibaseXmlWriter liquibaseWriter = (DefaultLiquibaseXmlWriter) factory.create(stream, AUTHOR);

        assertNotNull(liquibaseWriter.getIdGenerator());
        assertEquals("test.user", liquibaseWriter.getChangeSetAuthor());
    }

    @Test
    public void testNewIdGeneratorForEveryNewWriter() throws Exception {

        final DefaultLiquibaseXmlWriter liquibaseWriter1 = (DefaultLiquibaseXmlWriter) factory.create(stream, AUTHOR);
        final DefaultLiquibaseXmlWriter liquibaseWriter2 = (DefaultLiquibaseXmlWriter) factory.create(stream, AUTHOR);

        assertNotSame(liquibaseWriter2.getIdGenerator(), liquibaseWriter1.getIdGenerator());

        assertThat(liquibaseWriter1.getIdGenerator().next("table"), endsWith("-1"));
        assertThat(liquibaseWriter2.getIdGenerator().next("table"), endsWith("-1"));
    }

    @Test
    public void testCreateWithException() throws Exception {
        assertThrows(LiquibaseDataAccessException.class, () -> {
            factory = spy(factory);
            doThrow(XMLStreamException.class).when(factory).newXmlWriter(same(stream));

            try {
                factory.create(stream, AUTHOR);
            } catch (final LiquibaseDataAccessException e) {
                assertSame(XMLStreamException.class, e.getCause().getClass());
                throw e;
            }
        });
    }
}
