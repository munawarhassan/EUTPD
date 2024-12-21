package com.pmi.tpd.euceg.core;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eu.ceg.Attachment;
import org.eu.ceg.AttachmentAction;
import org.eu.ceg.Product;
import org.eu.ceg.ProductNumber;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductType;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.testing.junit5.TestCase;

public class EucegsTest extends TestCase implements TestWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EucegsTest.class);

    long start;

    boolean indent;

    public static Path targetFolder;

    @Override
    public void testSuccessful(@SuppressWarnings("exports") final ExtensionContext context) {
        LOGGER.info("Elapse time: {} for test '{}'", System.currentTimeMillis() - start, getMethodName());
    }

    @Override
    public void testFailed(@SuppressWarnings("exports") final ExtensionContext context, final Throwable cause) {
        LOGGER.info("Elapse time: {} for test '{}'", System.currentTimeMillis() - start, getMethodName());
    }

    @Override
    public void testDisabled(@SuppressWarnings("exports") final ExtensionContext context,
        final Optional<String> reason) {

    }

    @Override
    public void testAborted(@SuppressWarnings("exports") final ExtensionContext context, final Throwable cause) {
        LOGGER.info("Elapse time: {} for test '{}'", System.currentTimeMillis() - start, getMethodName());
    }

    @BeforeAll
    public static void start(@TempDir final Path tempDir) {
        targetFolder = tempDir.resolve("target");
    }

    @BeforeEach
    public void start() {
        start = System.currentTimeMillis();
        indent = Eucegs.indentMarshalling(true);
        LOGGER.info("Starting test: {}", getMethodName());
    }

    @AfterEach
    public void stop() {
        Eucegs.indentMarshalling(indent);
    }

    @Test
    public void shouldExtractAllAttachmentId() throws Exception {
        try (Reader reader = getXmlReader("submission.xml")) {
            final List<String> l = Eucegs.extractFromXml(reader, "//@attachmentID");
            assertEquals(13, l.size(), "should contains 13 attachement ID");
            assertThat(l, hasItem(not(is(emptyOrNullString()))));
        }
    }

    @Test
    public void shouldExtractNotEmptyOrNullAttachmentId() throws Exception {
        try (Reader reader = getXmlReader("submission.xml")) {
            final Set<String> l = Sets.newHashSet(Eucegs.extractFromXml(reader, "//@attachmentID[not(node())]"));
            assertEquals(12, l.size(), "should contains 12 attachement ID");
            assertThat(l, hasItem(not(is(emptyOrNullString()))));
        }
    }

    @Test
    public void shouldExtractDistinctAttachmentId() throws Exception {
        try (Reader reader = getXmlReader("submission.xml")) {
            final List<String> l = Eucegs.extractFromXml(reader,
                "//@attachmentID[not(. = preceding::*/@attachmentID)]");
            assertEquals(12, l.size(), "should contains 12 attachement ID");
            // just verify with set collection.
            assertEquals(12, Sets.newHashSet(l).size(), "should contains 12 attachement ID");
            assertThat(l, hasItem(not(is(emptyOrNullString()))));
        }
    }

    /**
     * @throws IOException
     * @since 2.0
     */
    @Test
    public void shouldWrapInherritedEntity() throws IOException {
        final Product product = new TobaccoProduct()
                .withProductType(new TobaccoProductType().withValue(TobaccoProductTypeEnum.CIGAR))
                .withProductID(new ProductNumber().withValue("pom.00001"));

        approve("product", Eucegs.marshal(Eucegs.wrap(product, Product.class)));

        approve("sinmple-wrap", Eucegs.marshal(Eucegs.wrap(product)));

        approve("tobaccor-product", Eucegs.marshal(Eucegs.wrap((TobaccoProduct) product, TobaccoProduct.class)));
    }

    /**
     * @throws IOException
     * @since 2.0
     */
    @Test
    public void shouldProvideFileFromMarshalling() throws IOException {
        final Attachment attachment = new Attachment().withAction(AttachmentAction.CREATE)
                .withAttachmentID("a318fb18-ce1e-4de9-b2a9-c8e5ea2e1b9e")
                .withConfidential(false);
        final File actualFile = Eucegs.marshallInFile(attachment, newFolder("works").toPath());
        assertEquals(true, actualFile.exists(), "file should exists");
        final String actual = Files.asCharSource(actualFile, Eucegs.getDefaultCharset()).read();
        approve(actual);
    }

    private Reader getXmlReader(final String xmlFile) throws Exception {
        return new InputStreamReader(getResourceAsStream(this.getClass(), xmlFile), Eucegs.getDefaultCharset());

    }

    private File newFolder(final String path) throws IOException {
        return newFolder(new String[] { path });
    }

    /**
     * Returns a new fresh folder with the given paths under the temporary folder. For example, if you pass in the
     * strings {@code "parent"} and {@code "child"} then a directory named {@code "parent"} will be created under the
     * temporary folder and a directory named {@code "child"} will be created under the newly-created {@code "parent"}
     * directory.
     */
    private File newFolder(final String... paths) throws IOException {
        if (paths.length == 0) {
            throw new IllegalArgumentException("must pass at least one path");
        }

        /*
         * Before checking if the paths are absolute paths, check if create() was ever called, and if it wasn't, throw
         * IllegalStateException.
         */
        final File root = targetFolder.toFile();
        for (final String path : paths) {
            if (new File(path).isAbsolute()) {
                throw new IOException("folder path \'" + path + "\' is not a relative path");
            }
        }

        File relativePath = null;
        File file = root;
        boolean lastMkdirsCallSuccessful = true;
        for (final String path : paths) {
            relativePath = new File(relativePath, path);
            file = new File(root, relativePath.getPath());

            lastMkdirsCallSuccessful = file.mkdirs();
            if (!lastMkdirsCallSuccessful && !file.isDirectory()) {
                if (file.exists()) {
                    throw new IOException("a file with the path \'" + relativePath.getPath() + "\' exists");
                } else {
                    throw new IOException("could not create a folder with the path \'" + relativePath.getPath() + "\'");
                }
            }
        }
        if (!lastMkdirsCallSuccessful) {
            throw new IOException("a folder with the path \'" + relativePath.getPath() + "\' already exists");
        }
        return file;
    }
}
