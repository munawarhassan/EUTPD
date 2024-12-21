package com.pmi.tpd.core.liquibase.backup;

import static com.pmi.tpd.core.liquibase.backup.AbstractLiquibaseMigrationTest.BACKUP_PREFIX;
import static com.pmi.tpd.core.liquibase.backup.AbstractLiquibaseMigrationTest.BACKUP_SUFFIX;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.ENCODING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Compare the XML backups generated for each database.
 */
public class CompareLiquibaseBackupIT extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompareLiquibaseBackupIT.class);

    // location of the backup files (this should correspond to the destination
    // directory in the artifact dependencies in
    // the build plan)
    public static final String SOURCE_DIR = ".";

    // property to enable this test (by default the test is run in a separate stage
    // on BEAC)
    public static final String ENABLING_PROPERTY = "compare-migration-backups";

    // differences between the backups that should be ignored
    private static final Map<Pattern, String> FILTERS = ImmutableMap.<Pattern, String> builder()
            .put(
                Pattern.compile("<insert tableName=\"id_sequence\">.+</insert>",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                "")
            // remove IDs
            .put(Pattern.compile("<column name=\"id\" colType=\"numeric\">.+</column>", Pattern.CASE_INSENSITIVE),
                "<column name=\"id\" colType=\"numeric\">REMOVED_ID</column>")
            // remove unique keys
            .put(Pattern.compile("<column name=\"next_hi\" colType=\"numeric\">.+</column>", Pattern.CASE_INSENSITIVE),
                "<column name=\"next_hi\" colType=\"numeric\">REMOVED_UK</column>")
            // remove foreign keys to project IDs
            .put(
                Pattern.compile("<column name=\"project_id\" colType=\"numeric\">.+</column>",
                    Pattern.CASE_INSENSITIVE),
                "<column name=\"project_id\" colType=\"numeric\">REMOVED_FK</column>")
            // remove foreign keys to user IDs
            .put(Pattern.compile("<column name=\"owner_id\" colType=\"numeric\">.+</column>", Pattern.CASE_INSENSITIVE),
                "<column name=\"user_id\" colType=\"numeric\">REMOVED_FK</column>")
            .put(Pattern.compile("<column name=\"user_id\" colType=\"numeric\">.+</column>", Pattern.CASE_INSENSITIVE),
                "<column name=\"user_id\" colType=\"numeric\">REMOVED_FK</column>")
            .build();

    @Test
    public void testCompare() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean(ENABLING_PROPERTY));

        final File dir = new File(SOURCE_DIR).getAbsoluteFile();
        final List<File> backups = retrieveBackups(dir);
        assertFalse(backups.isEmpty(), "No backups were found in: " + dir);

        // remove any database specific data, such as generated IDs
        final List<File> filtered = filter(backups);

        final File first = filtered.get(0);
        for (final File other : filtered.subList(1, filtered.size())) {
            assertIdentical(first, other);
        }
    }

    private List<File> retrieveBackups(final File dir) {
        LOGGER.info("Looking for backups in {}", dir);
        final List<File> backups = ImmutableList.copyOf(dir.listFiles((FileFilter) file -> {
            final String name = file.getName();
            return file.isFile() && name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_SUFFIX);
        }));
        LOGGER.info("Found: {}", backups);
        return backups;
    }

    private List<File> filter(final List<File> backups) throws IOException {
        final List<File> list = new ArrayList<>(backups.size());
        for (final File backup : backups) {
            final File copy = new File(backup.getParent(), "filtered-" + backup.getName());
            Files.copy(backup, copy); // will overwrite the copy if already present
            list.add(filter(copy));
        }
        return list;
    }

    private File filter(final File file) throws IOException {
        String s = Files.asCharSource(file, Charset.forName(ENCODING)).read();
        for (final Map.Entry<Pattern, String> filter : FILTERS.entrySet()) {
            s = filter.getKey().matcher(s).replaceAll(filter.getValue());
        }
        Files.asCharSink(file, Charset.forName(ENCODING)).write(s);
        return file;
    }

    private void assertIdentical(final File backup1, final File backup2) throws IOException, SAXException {
        Reader first = null, second = null;
        try {
            first = new BufferedReader(new FileReader(backup1));
            second = new BufferedReader(new FileReader(backup2));

            final DetailedDiff diff = new DetailedDiff(new Diff(first, second));
            @SuppressWarnings("rawtypes")
            final List differences = diff.getAllDifferences();

            if (!differences.isEmpty()) {
                fail(String.format("The backups differ between %1$s and %2$s:\n" + differences + "\n\n"
                        + "To manually compare them, download the artefacts generated by the first stage of the build and run:\n"
                        + "diff %1$s %2$s\n" + " or on Mac OSX:\n"
                        + "/Developer/Applications/Utilities/FileMerge.app/Contents/MacOS/FileMerge -left %1$s -right %2$s\n",
                    backup1.getName(),
                    backup2.getName()));
            }
        } finally {
            Closeables.closeQuietly(first);
            Closeables.closeQuietly(second);
        }
    }

}
