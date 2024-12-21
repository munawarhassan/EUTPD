package com.pmi.tpd.euceg.api;

import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eu.ceg.AcuteToxDermalCode;
import org.eu.ceg.AcuteToxInhalationCode;
import org.eu.ceg.AcuteToxOralCode;
import org.eu.ceg.AdditiveNumber;
import org.eu.ceg.AspirationToxCode;
import org.eu.ceg.AttachmentRef;
import org.eu.ceg.Boolean;
import org.eu.ceg.CarcinogenicityCode;
import org.eu.ceg.CasNumber;
import org.eu.ceg.Date;
import org.eu.ceg.EcNumber;
import org.eu.ceg.EcigProductType;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.EmissionName;
import org.eu.ceg.EyeDamageIrritationCode;
import org.eu.ceg.FemaNumber;
import org.eu.ceg.FlNumber;
import org.eu.ceg.IngredientCategory;
import org.eu.ceg.IngredientCategoryEnum;
import org.eu.ceg.IngredientFunction;
import org.eu.ceg.LeafCureMethod;
import org.eu.ceg.LeafCureMethodEnum;
import org.eu.ceg.LeafType;
import org.eu.ceg.LeafTypeEnum;
import org.eu.ceg.MutagenGenotoxCode;
import org.eu.ceg.NationalMarket;
import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.ObjectFactory;
import org.eu.ceg.PackageType;
import org.eu.ceg.PartType;
import org.eu.ceg.PartTypeEnum;
import org.eu.ceg.Percentage;
import org.eu.ceg.ProductIdentification;
import org.eu.ceg.ProductIdentificationType;
import org.eu.ceg.ProductNumber;
import org.eu.ceg.ReachRegistration;
import org.eu.ceg.ReachRegistrationEnum;
import org.eu.ceg.ReproductiveToxCode;
import org.eu.ceg.RespiratorySensitisationCode;
import org.eu.ceg.SkinCorrosiveIrritantCode;
import org.eu.ceg.SkinSensitisationCode;
import org.eu.ceg.StotCode;
import org.eu.ceg.String100;
import org.eu.ceg.String1000;
import org.eu.ceg.String300;
import org.eu.ceg.String40;
import org.eu.ceg.String500;
import org.eu.ceg.SubmissionType;
import org.eu.ceg.TobaccoProductType;
import org.eu.ceg.ToxicityStatus;
import org.eu.ceg.ToxicityStatusEnum;
import org.eu.ceg.ToxicologicalDataAvailable;
import org.eu.ceg.VoltageWattageAdjustable;
import org.eu.ceg.Year;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.xml.InvalidXmlCharacterFilterReader;
import com.pmi.tpd.api.util.xml.InvalidXmlCharacterFilterWriter;
import com.pmi.tpd.euceg.api.binding.BooleanNullable;

/**
 * Class facility helper containing all methods allow to create, manipulate, mashall {@link #marshal(Object)},
 * unmarshall {@link #marshal(Object)}, extract data {@link #extractFromXml(InputSource, String)}... all submission
 * objects contained in Euceg schema.
 * <p>
 * <b>See: </b> xsd files in resources/eucegs directory in this module for more information.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class Eucegs {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(Eucegs.class);

    /** */
    public static final String UNDEFINED_PRODUCT_ID = "00000-00-00000";

    /** */
    public static final String EUCEG_DATE_PATTERN = "dd/MM/yyyy";

    /** */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(EUCEG_DATE_PATTERN);

    /** */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** */
    private static ObjectFactory factory = new ObjectFactory();

    private static XMLInputFactory inputFactory;

    /** */
    private static JAXBContext jaxbContext;

    /** */
    @Deprecated(since = "2.4", forRemoval = true)
    private static boolean indent = false;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                ObjectFactory.class.getClassLoader());
            inputFactory = XMLInputFactory.newInstance();
        } catch (final JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private Eucegs() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Returns the default {@link Charset} used in un/marshalling.
     */
    @SuppressWarnings("null")
    @Nonnull
    public static Charset getDefaultCharset() {
        return Charsets.UTF_8;
    }

    @Nonnull
    public static Reader openReader(@Nonnull final InputStream in) throws IOException {
        final ByteSource byteSource = new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return in;
            }
        };
        return new InvalidXmlCharacterFilterReader(byteSource.asCharSource(Eucegs.getDefaultCharset()).openStream());
    }

    @SuppressWarnings("null")
    @Nonnull
    public static Reader openReader(@Nonnull final String xml) throws IOException {
        return openReader(xml.getBytes());
    }

    @Nonnull
    public static Reader openReader(@Nonnull final byte[] xml) throws IOException {
        final CharSource source = ByteSource.wrap(xml).asCharSource(Eucegs.getDefaultCharset());
        return new InvalidXmlCharacterFilterReader(source.openStream());
    }

    @Nonnull
    public static Reader openReader(@Nonnull final char[] xml) throws IOException {
        return new InvalidXmlCharacterFilterReader(new CharArrayReader(xml));
    }

    /**
     * Create a Marshaller object that can be used to convert a java content tree into XML data.
     *
     * @return a {@link Marshaller} object
     * @throws JAXBException
     *                       if an error was encountered while creating the Marshaller object.
     */
    @Nonnull
    public static Marshaller createMarshaller() throws JAXBException {
        return createMarshaller(indent);
    }

    /**
     * Create a Marshaller object that can be used to convert a java content tree into XML data.
     *
     * @param indentation
     *                    {@code true} to indent the xml output
     * @return a {@link Marshaller} object
     * @throws JAXBException
     *                       if an error was encountered while creating the Marshaller object.
     */
    @Nonnull
    public static Marshaller createMarshaller(final boolean indentation) throws JAXBException {
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, java.lang.Boolean.valueOf(indentation));
        marshaller.setProperty(Marshaller.JAXB_ENCODING, getDefaultCharset().name());
        return marshaller;
    }

    /**
     * Create an Unmarshaller object that can be used to convert XML data into a java content tree.
     *
     * @param xmlContent
     *                   the xml string to unmarshal XML data from
     * @return Returns the newly created root object of the java content tree
     * @param <T>
     *            the type of expected root object.
     */
    @Nonnull
    public static <T> T unmarshal(@Nonnull final String xmlContent) {
        try {
            return unmarshal(openReader(xmlContent));
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T unmarshal(final InputStream input) {
        return unmarshal(new InputStreamReader(input, getDefaultCharset()));
    }

    /**
     * Unmarshal XML data from the specified Reader and return the resulting content tree.
     *
     * @param reader
     *               the Reader to unmarshal XML data from
     * @return Returns the newly created root object of the java content tree
     * @param <T>
     *            the type of expected root object.
     */
    @SuppressWarnings({ "unchecked", "null" })
    @Nonnull
    protected static <T> T unmarshal(@Nonnull final Reader reader) {
        try {
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(reader);
        } catch (final JAXBException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Unmarshal XML data from the specified array of byte and return the resulting content tree.
     *
     * @param xmlContent
     *                   the array of byte to unmarshal XML data from
     * @return Returns the newly created root object of the java content tree
     * @param <T>
     *            the type of expected root object.
     * @throws IOException
     */
    public static <T> T unmarshal(@Nonnull final byte[] xmlContent) {
        try {
            return unmarshal(openReader(xmlContent));
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Unmarshal XML data from the specified array of byte and return the resulting content tree.
     *
     * @param xmlContent
     *                   the array of byte to unmarshal XML data from
     * @return Returns the newly created root object of the java content tree
     * @param <T>
     *            the type of expected root object.
     * @throws IOException
     */
    public static <T> T unmarshal(@Nonnull final char[] xmlContent) {
        try {
            return unmarshal(openReader(xmlContent));
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param xmlContent
     * @param cl
     * @return
     */
    public static <T> T unmarshal(final byte[] xmlContent, final Class<T> cl) {
        try {
            return unmarshal(ByteSource.wrap(xmlContent).asCharSource(getDefaultCharset()).openStream(), cl);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T unmarshal(final char[] xmlContent, final Class<T> cl) {
        return unmarshal(new CharArrayReader(xmlContent), cl);
    }

    public static <T> T unmarshal(final String xmlContent, final Class<T> cl) {
        return unmarshal(new StringReader(xmlContent), cl);
    }

    public static <T> T unmarshal(final Reader reader, final Class<T> cl) {
        try (reader) {
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final XMLStreamReader stream = inputFactory
                    .createXMLStreamReader(new InvalidXmlCharacterFilterReader(reader));
            final JAXBElement<T> root = unmarshaller.unmarshal(stream, cl);
            return root.getValue();

        } catch (final JAXBException | IOException | XMLStreamException | FactoryConfigurationError e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nullable
    public static String marshal(@Nullable final Object obj) {
        return marshal(obj, indent);
    }

    @Nullable
    public static String marshal(@Nullable final Object obj, final boolean indentation) {
        if (obj == null) {
            return null;
        }
        try (StringWriter writer = new StringWriter()) {
            marshal(obj, writer, indentation);
            return writer.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void marshal(@Nonnull final Object obj, @Nonnull final Writer writer) {
        marshal(obj, writer, indent);
    }

    public static void marshal(@Nonnull final Object obj, @Nonnull final Writer writer, final boolean indentation) {
        Preconditions.checkNotNull(obj, "obj");
        Preconditions.checkNotNull(writer, "writer");
        try {
            final Marshaller marshaller = createMarshaller(indentation);
            marshaller.marshal(obj, new InvalidXmlCharacterFilterWriter(writer));
        } catch (final JAXBException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void marshal(@Nonnull final Object obj, @Nonnull final OutputStream outputStream) {
        marshal(obj, outputStream, indent);
    }

    public static void marshal(@Nonnull final Object obj,
        @Nonnull final OutputStream outputStream,
        final boolean indentation) {
        marshal(obj, new OutputStreamWriter(outputStream, getDefaultCharset()), indentation);
    }

    /**
     * Marshall a payload in file stored in the specific {@code workingPath}
     *
     * @param payload
     *                    the payload to marshall
     * @param workingPath
     *                    the directory to use.
     * @return Returns the {@link File} representing the result of marshalling.
     * @throws IOException
     *                     if an I/O error occurs or {@code workingPath} does not exist.
     * @since 2.0
     */
    public static File marshallInFile(@Nonnull final Object payload, @Nonnull final Path workingPath)
            throws IOException {
        Preconditions.checkArgument(payload != null, "payload is required");
        Preconditions.checkArgument(workingPath != null, "workingPath is required");
        Preconditions.checkArgument(Files.isDirectory(workingPath), "workingpath must be exist and a directory");
        File tempFile = null;
        tempFile = workingPath.resolve(uuid() + ".xml").toFile();
        if (payload instanceof String) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                final String str = (String) payload;
                CharStreams.copy(new StringReader(str), writer);
            }
        } else if (payload instanceof byte[]) {
            try (OutputStream writer = new FileOutputStream(tempFile)) {
                try (InputStream in = ByteSource.wrap((byte[]) payload).openBufferedStream()) {
                    ByteStreams.copy(in, writer);
                }
            }
        } else if (payload instanceof char[]) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                final String str = new String((char[]) payload);
                CharStreams.copy(new StringReader(str), writer);
            }
        } else {
            OutputStream out = null;
            try {
                out = new FileOutputStream(tempFile);
                marshal(payload, out);
            } finally {
                Closeables.close(out, false);
            }
        }
        return tempFile;
    }

    /**
     * Extracts a list of {@link String} from {@code xmlStream} using {@code xpathString} a xpath (1.4) expression.
     *
     * @param reader
     *                    Reference the xml stream to use.
     * @param xpathString
     *                    The xpath expression to use.
     * @return Returns a {@link List} representing a list of values that is result of xpath evaluation.
     * @throws XPathExpressionException
     *                                      If the expression cannot be evaluated.
     * @throws SAXException
     *                                      If any parse errors occur.
     * @throws IOException
     *                                      If any IO errors occur.
     * @throws ParserConfigurationException
     *                                      if a DocumentBuilder cannot be created which satisfies the configuration
     *                                      requested.
     * @since 1.4
     */
    public static List<String> extractFromXml(@Nonnull final Reader reader, @Nonnull final String xpathString)
            throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
        final XPathFactory xPathfactory = XPathFactory.newInstance();
        final XPath xpath = xPathfactory.newXPath();
        final XPathExpression expression = Assert.checkNotNull(xpath.compile(xpathString), "xpathString");
        final InputSource inputSource = new InputSource(new InvalidXmlCharacterFilterReader(reader));
        inputSource.setEncoding(getDefaultCharset().name());
        return extractFromXml(inputSource, expression);
    }

    public static Set<String> extractAttachementID(final String xmlProduct) {
        try (Reader reader = new StringReader(xmlProduct)) {
            return extractAttachementID(reader);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Set<String> extractAttachementID(@Nonnull final Reader reader) {
        try {
            return Sets.newHashSet(
                Eucegs.extractFromXml(Assert.checkNotNull(reader, "reader"), "//@attachmentID[not(node())]"));
        } catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Extracts a list of {@link String} from {@code xmlStream} that is the result of evaluating the xpath expression
     * (1.4).
     *
     * @param xmlStream
     *                   Reference the xml stream to use.
     * @param expression
     *                   The xpath expression to use.
     * @return Returns a new {@link List} of {@link String} representing the result of evaluating the expression.
     * @throws XPathExpressionException
     *                                      If the expression cannot be evaluated.
     * @throws SAXException
     *                                      If any parse errors occur.
     * @throws IOException
     *                                      If any IO errors occur.
     * @throws ParserConfigurationException
     *                                      if a DocumentBuilder cannot be created which satisfies the configuration
     *                                      requested.
     * @since 1.4
     */
    private static List<String> extractFromXml(@Nonnull final InputSource xmlStream,
        @Nonnull final XPathExpression expression)
            throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        final NodeList nodeList = (NodeList) extractFromXml(xmlStream, expression, XPathConstants.NODESET);
        final List<String> l = Lists.newArrayListWithCapacity(nodeList.getLength());
        if (nodeList.getLength() == 0) {
            return ImmutableList.of();
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            final String value = nodeList.item(i).getNodeValue();
            if (!Strings.isNullOrEmpty(value)) {
                l.add(value);
            }
        }
        return l;

    }

    /**
     * Extracts the {@link Object} from {@code xmlStream} that is the result of evaluating the xpath expression (1.4)
     * and converting the result to {@code returnType}.
     *
     * @param xmlStream
     *                   Reference the xml stream to use.
     * @param expression
     *                   The xpath expression to use.
     * @param returnType
     *                   The desired return type.
     * @return Returns The {@link Object} that is the result of evaluating the expression and converting the result to
     *         {@code returnType}.
     * @throws XPathExpressionException
     *                                      If the expression cannot be evaluated.
     * @throws SAXException
     *                                      If any parse errors occur.
     * @throws IOException
     *                                      If any IO errors occur.
     * @throws ParserConfigurationException
     *                                      if a DocumentBuilder cannot be created which satisfies the configuration
     *                                      requested.
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    private static <T> T extractFromXml(@Nonnull final InputSource xmlStream,
        @Nonnull final XPathExpression expression,
        @Nonnull final QName returnType)
            throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(xmlStream);
        return (T) expression.evaluate(doc, returnType);

    }

    /**
     * Wrap the JAXB entity in {@link JAXBElement}.
     * <p>
     * Note: this entity must be annotated with {@link XmlType} annotation to find the appropriate {@link QName}.
     * </p>
     *
     * @param entity
     *               the JAXB entity to wrap
     * @return Returns a new instance of {@link JAXBElement} wrapping the entity.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> JAXBElement<T> wrap(@Nonnull final T entity) {
        final XmlType xmlType = Assert.checkNotNull(entity, "entity").getClass().getAnnotation(XmlType.class);
        final QName qName = new QName(xmlType.name());
        return new JAXBElement<>(qName, (Class<T>) entity.getClass(), entity);
    }

    /**
     * Wrap the JAXB entity in {@link JAXBElement} using specific inherrited entity class.
     * <p>
     * Note: this entity must be annotated with {@link XmlType} annotation to find the appropriate {@link QName}.
     * </p>
     *
     * @param entity
     *               the JAXB entity to wrap
     * @param cls
     *               the specific inherrited entity class.
     * @return Returns a new instance of {@link JAXBElement} wrapping the entity.
     */
    @Nonnull
    public static <T> JAXBElement<T> wrap(@Nonnull final T entity, @Nonnull final Class<T> cls) {
        Assert.checkNotNull(entity, "entity");
        final XmlType xmlType = Assert.checkNotNull(cls, "cls").getAnnotation(XmlType.class);
        final QName qName = new QName(xmlType.name());
        return new JAXBElement<>(qName, cls, entity);
    }

    /**
     * @param indent
     * @throws JAXBException
     */
    public static boolean indentMarshalling(final boolean indent) {
        final boolean old = Eucegs.indent;
        Eucegs.indent = indent;
        return old;
    }

    /**
     * @param value
     * @return
     */
    public static ProductNumber productNumber(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createProductNumber().withValue(value).withConfidential(confidential);
    }

    public static ProductNumber productNumber(final String value) {
        return productNumber(value, false);
    }

    /**
     * @param value
     * @return
     */
    public static SubmissionType submissionType(final org.eu.ceg.SubmissionTypeEnum value) {
        if (value == null) {
            return null;
        }
        return factory.createSubmissionType().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @return
     */
    public static TobaccoProductType tobaccoProductType(final org.eu.ceg.TobaccoProductTypeEnum value) {
        if (value == null) {
            return null;
        }
        return factory.createTobaccoProductType().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @return
     */
    public static NationalMarket nationalMarket(final NationalMarketValue value) {
        if (value == null) {
            return null;
        }
        return factory.createNationalMarket().withValue(value).withConfidential(false);
    }

    public static IngredientCategory ingredientCategorie(final IngredientCategoryEnum value,
        final boolean confidential) {
        if (value == null) {
            return null; // NOPMD by Christophe Friederich on 5/24/17 10:20 AM
        }
        return factory.createIngredientCategory().withValue(value).withConfidential(confidential);
    }

    public static IngredientCategory ingredientCategorie(final IngredientCategoryEnum value) {
        return ingredientCategorie(value, false);
    }

    /**
     * @param value
     * @return
     */
    public static IngredientFunction ingredientFunction(final org.eu.ceg.IngredientFunctionEnum value) {
        if (value == null) {
            return null;
        }
        return factory.createIngredientFunction().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static ReachRegistration reachRegistration(final ReachRegistrationEnum value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return factory.createReachRegistration().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static ReachRegistration reachRegistration(final ReachRegistrationEnum value) {
        return reachRegistration(value, false);
    }

    /**
     * @param value
     * @return
     */
    public static EcigProductType ecigProductType(final EcigProductTypeEnum value) {
        if (value == null) {
            return null;
        }
        return factory.createEcigProductType().withValue(value).withConfidential(false);
    }

    public static EmissionName emissionName(final org.eu.ceg.EmissionNameEnum value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return factory.createEmissionName().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static EmissionName emissionName(final org.eu.ceg.EmissionNameEnum value) {
        return emissionName(value, false);
    }

    /**
     * @param text
     * @return
     */
    public static CasNumber casNumber(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createCasNumber().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static CasNumber casNumber(final String value) {
        return casNumber(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static AcuteToxDermalCode acuteToxDermalCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new AcuteToxDermalCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static AcuteToxDermalCode acuteToxDermalCode(final String value) {
        return acuteToxDermalCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static AdditiveNumber additiveNumber(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new AdditiveNumber().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static AcuteToxInhalationCode acuteToxInhalationCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new AcuteToxInhalationCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static AcuteToxInhalationCode acuteToxInhalationCode(final String value) {
        return acuteToxInhalationCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static AcuteToxOralCode acuteToxOralCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new AcuteToxOralCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static AcuteToxOralCode acuteToxOralCode(final String value) {
        return acuteToxOralCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static AspirationToxCode aspirationToxCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new AspirationToxCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static AspirationToxCode aspirationToxCode(final String value) {
        return aspirationToxCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static CarcinogenicityCode carcinogenicityCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new CarcinogenicityCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static CarcinogenicityCode carcinogenicityCode(final String value) {
        return carcinogenicityCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static EyeDamageIrritationCode eyeDamageIrritationCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new EyeDamageIrritationCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static EyeDamageIrritationCode eyeDamageIrritationCode(final String value) {
        return eyeDamageIrritationCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static MutagenGenotoxCode mutagenGenotoxCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new MutagenGenotoxCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static MutagenGenotoxCode mutagenGenotoxCode(final String value) {
        return mutagenGenotoxCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static ReproductiveToxCode reproductiveToxCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new ReproductiveToxCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static ReproductiveToxCode reproductiveToxCode(final String value) {
        return reproductiveToxCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static RespiratorySensitisationCode respiratorySensitisationCode(final String value,
        final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new RespiratorySensitisationCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static RespiratorySensitisationCode respiratorySensitisationCode(final String value) {
        return respiratorySensitisationCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static SkinCorrosiveIrritantCode skinCorrosiveIrritantCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new SkinCorrosiveIrritantCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static SkinCorrosiveIrritantCode skinCorrosiveIrritantCode(final String value) {
        return skinCorrosiveIrritantCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static SkinSensitisationCode skinSensitisationCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new SkinSensitisationCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static SkinSensitisationCode skinSensitisationCode(final String value) {
        return skinSensitisationCode(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static StotCode stotCode(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new StotCode().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static StotCode stotCode(final String value) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new StotCode().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static EcNumber ecNumber(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new EcNumber().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static EcNumber ecNumber(final String value) {
        return ecNumber(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static FemaNumber femaNumber(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new FemaNumber().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static FemaNumber femaNumber(final String value) {
        return femaNumber(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static FlNumber flNumber(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new FlNumber().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static FlNumber flNumber(final String value) {
        return flNumber(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static ToxicityStatus toxicityStatus(final ToxicityStatusEnum value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return new ToxicityStatus().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static ToxicityStatus toxicityStatus(final ToxicityStatusEnum value) {
        return toxicityStatus(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static ToxicologicalDataAvailable toxicologicalDataAvailable(
        final org.eu.ceg.ToxicologicalDataAvailableEnum value,
        final boolean confidential) {
        if (value == null) {
            return null;
        }
        return new ToxicologicalDataAvailable().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static ToxicologicalDataAvailable toxicologicalDataAvailable(
        final org.eu.ceg.ToxicologicalDataAvailableEnum value) {
        return toxicologicalDataAvailable(value, false);
    }

    /**
     * @param value
     * @param productId
     * @return
     */
    public static ProductIdentification productIdentification(final String value,
        final ProductIdentificationType productId) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new ProductIdentification().withValue(value).withType(productId).withConfidential(false);
    }

    /**
     * @param value
     * @return
     */
    public static PackageType packageType(final org.eu.ceg.PackageTypeEnum value) {
        if (value == null) {
            return null;
        }
        return new PackageType().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @return
     */
    public static LeafCureMethod leafCureMethod(final LeafCureMethodEnum value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return new LeafCureMethod().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static LeafCureMethod leafCureMethod(final LeafCureMethodEnum value) {
        return leafCureMethod(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static LeafType leafType(final LeafTypeEnum value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return new LeafType().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static LeafType leafType(final LeafTypeEnum value) {
        return leafType(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static PartType partType(final PartTypeEnum value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return new PartType().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static PartType partType(final PartTypeEnum value) {
        return partType(value, false);
    }

    /**
     * @param value
     * @return
     */
    public static VoltageWattageAdjustable voltageWattageAdjustable(
        final org.eu.ceg.VoltageWattageAdjustableEnum value) {
        if (value == null) {
            return null;
        }
        return new VoltageWattageAdjustable().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @return
     */
    public static Boolean toBoolean(final java.lang.Boolean value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return factory.createBoolean().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static Boolean toBoolean(final java.lang.Boolean value) {
        return toBoolean(value, false);
    }

    public static BooleanNullable toBooleanNullable(final java.lang.Boolean value, final boolean confidential) {
        return new BooleanNullable().withValue(value).withConfidential(confidential);
        // conform if use xs:string for value, but this change has not impact in
        // marshalling/unmarcharling
        // return factory.createBooleanNullable()
        // .withValue(BooleanNullableValue.fromValue(value.toString()))
        // .withConfidential(confidential);
    }

    public static BooleanNullable toBooleanNullable(final java.lang.Boolean value) {
        return toBooleanNullable(value, false);
    }

    public static Percentage percentage(final java.lang.Integer value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return factory.createPercentage().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static Percentage percentage(final java.lang.Integer value) {
        return percentage(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static String40 string40(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createString40().withValue(value).withConfidential(confidential);
    }

    public static String40 string40(final String value) {
        return string40(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static String100 string100(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createString100().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static String100 string100(final String value) {
        return string100(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static String300 string300(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createString300().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static String300 string300(final String value) {
        return string300(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static String500 string500(final String value, final boolean confidential) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return new String500().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static String500 string500(final String value) {
        return string500(value, false);
    }

    /**
     * @param value
     * @return
     */
    public static String1000 string1000(final String value) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createString1000().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @return
     */
    public static AttachmentRef attachmentRef(final String value) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return factory.createAttachmentRef().withAttachmentID(value);
    }

    /**
     * @param value
     * @return
     */
    public static Date toDate(final String value) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        return toDate(parseLocalDate(value));
    }

    /**
     * @param value
     * @return
     */
    public static Date toDate(final LocalDate value) {
        if (value == null) {
            return null;
        }
        return factory.createDate().withValue(value).withConfidential(false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static org.eu.ceg.Double decimal(final BigDecimal value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return factory.createDouble().withValue(value).withConfidential(confidential);
    }

    public static org.eu.ceg.Double decimal(final BigDecimal value) {
        return decimal(value, false);
    }

    public static org.eu.ceg.Integer toInteger(final java.lang.Integer value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return factory.createInteger().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static org.eu.ceg.Integer toInteger(final java.lang.Integer value) {
        return toInteger(value, false);
    }

    /**
     * @param value
     * @param confidential
     * @return
     */
    public static Year year(final java.lang.Integer value, final boolean confidential) {
        if (value == null) {
            return null;
        }
        return new Year().withValue(value).withConfidential(confidential);
    }

    /**
     * @param value
     * @return
     */
    public static Year year(final java.lang.Integer value) {
        return year(value, false);
    }

    /**
     * @return
     */
    @Nonnull
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * <p>
     * Converts the string argument into a {@link DateTime} value.
     *
     * @param lexicalXSDDate
     *                       A string containing lexical representation of xsd:Date.
     * @return A Date value represented by the string argument.
     * @throws IllegalArgumentException
     *                                  if string parameter does not conform to lexical value space defined in XML
     *                                  Schema Part 2: Datatypes for xsd:datetime.
     */
    public static DateTime parseDateTime(final String lexicalXSDDate) {
        if (lexicalXSDDate != null) {
            return DATETIME_FORMATTER.parseDateTime(lexicalXSDDate);
        }
        return null;
    }

    /**
     * <p>
     * Converts a {@link DateTime} value into a string.
     *
     * @param val
     *            A {@link DateTime} value
     * @return A string containing a lexical representation of xsd:datetime
     * @throws IllegalArgumentException
     *                                  if <tt>val</tt> is null.
     */
    public static String printDateTime(final DateTime val) {
        if (val == null) {
            return null;
        }
        return DATETIME_FORMATTER.print(val);
    }

    /**
     * <p>
     * Converts the string argument into a {@link LocalDate} value.
     *
     * @param lexicalXSDDate
     *                       A string containing lexical representation of xsd:Date.
     * @return A Date value represented by the string argument.
     * @throws IllegalArgumentException
     *                                  if string parameter does not conform to lexical value space defined in XML
     *                                  Schema Part 2: Datatypes for xsd:Date.
     */
    public static LocalDate parseLocalDate(final String lexicalXSDDate) {
        if (lexicalXSDDate != null) {
            return DATE_FORMATTER.parseLocalDate(lexicalXSDDate);
        }
        return null;
    }

    /**
     * <p>
     * Converts a {@link LocalDate} value into a string.
     *
     * @param val
     *            A {@link LocalDate} value
     * @return A string containing a lexical representation of xsd:date
     * @throws IllegalArgumentException
     *                                  if <tt>val</tt> is null.
     */
    public static String printLocalDate(final LocalDate val) {
        if (val == null) {
            return null;
        }
        return DATE_FORMATTER.print(val);
    }

}
