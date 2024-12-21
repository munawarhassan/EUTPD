package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.io.Reader;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eu.ceg.PackageTypeEnum;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.Throwables;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.Parsers;
import com.pmi.tpd.euceg.core.exporter.BaseExcelExporter;
import com.pmi.tpd.euceg.core.exporter.Formats;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter()
@ToString
@Accessors(fluent = true)
public class RequestNovelTobaccoSubmission extends BaseRequestExportSubmission {

    // Novel Section
    //

    /** */
    private String productWeight;

    /** */
    private String productTobaccoWeight;

    /** */
    private String detailsDescriptionFile;

    /** */
    private String useInstructionsFile;

    // End Novel Section
    //

    private List<Presentation> presentations;

    @Getter
    @Setter()
    @ToString
    @Accessors(fluent = true)
    public static class Presentation {

        /** */
        private String brandName;

        /** */
        private String brandSubtype;

        /** */
        private String launchDate;

        /** */
        private String withdrawalDate;

        /** */
        private String productSubmitterNumber;

        /** */
        private String nationalMarket;

        /** */
        private String packageType;

        /** */
        private String packageUnits;

        /** */
        private String packageNetWeight;
    }

    @Override
    protected void parseSubmission(final BaseExcelExporter<?> context, @Nonnull final Reader xmlProduct)
            throws Exception {
        Assert.checkNotNull(xmlProduct, "xmlProduct");

        // Instance of the class which helps on reading tags
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        // Initializing the handler to access the tags in the XML file
        final XMLEventReader eventReader = factory.createXMLEventReader(xmlProduct);

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();
                final String localPart = startElement.getName().getLocalPart();
                switch (localPart) {
                    case "Product": {
                        parseProduct(context, eventReader, this);
                        break;
                    }
                }
            }
        }
    }

    public static void parseProduct(final BaseExcelExporter<?> context,
        @Nonnull final XMLEventReader eventReader,
        @Nonnull final RequestNovelTobaccoSubmission builder) throws Exception {
        Assert.checkNotNull(builder, "builder");

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Presentations": {
                        parsePresentations(eventReader, builder);
                        break;
                    }
                    case "NovelSpecific": {
                        parseNovelSpecific(context, eventReader, builder);
                        break;
                    }
                    case "ProductType": {
                        final int productType = Integer.valueOf(XmlHelper.getValue(eventReader));
                        builder.productType(Formats.fromEnumToString(TobaccoProductTypeEnum.fromValue(productType)));
                        break;
                    }
                    case "PreviousProductID": {
                        final String previousProductId = XmlHelper.getValue(eventReader);
                        builder.previousProductId(previousProductId);
                        break;
                    }
                    case "ProductID": {
                        builder.productId(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Weight": {
                        final String val = XmlHelper.getValue(eventReader);
                        builder.productWeight(val);
                        break;
                    }
                    case "TobaccoWeight": {
                        final String val = XmlHelper.getValue(eventReader);
                        builder.productTobaccoWeight(val);
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if ("Product".equals(endElement.getName().getLocalPart())) {
                    break;
                }
            }
        }

    }

    private static void parsePresentations(final XMLEventReader eventReader,
        final RequestNovelTobaccoSubmission builder) throws XMLStreamException {
        final List<Presentation> presentations = Lists.newArrayList();
        builder.presentations(presentations);

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Presentation": {
                        final Presentation presentation = new Presentation();
                        parsePresentation(eventReader, presentation);
                        presentations.add(presentation);
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if ("Presentations".equals(endElement.getName().getLocalPart())) {
                    break;
                }
            }
        }
    }

    private static void parsePresentation(final XMLEventReader eventReader, final Presentation presantation)
            throws XMLStreamException {

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "NationalMarket": {
                        final String countryCode = XmlHelper.getValue(eventReader);
                        if (!Strings.isNullOrEmpty(countryCode)) {
                            presantation.nationalMarket(countryCode);
                        }
                        break;
                    }
                    case "WithdrawalDate": {
                        final String date = XmlHelper.getValue(eventReader);
                        if (!Strings.isNullOrEmpty(date)) {
                            final LocalDate d = Parsers.parseLocalDate(date);
                            presantation.withdrawalDate(Eucegs.printLocalDate(d));
                        }
                        break;
                    }
                    case "LaunchDate": {
                        final String date = XmlHelper.getValue(eventReader);
                        if (!Strings.isNullOrEmpty(date)) {
                            final LocalDate d = Parsers.parseLocalDate(date);
                            presantation.launchDate(Eucegs.printLocalDate(d));
                        }
                        break;
                    }
                    case "BrandName": {
                        final String val = XmlHelper.getValue(eventReader);
                        presantation.brandName(Formats.trim(val));
                        break;
                    }
                    case "BrandSubtypeName": {
                        final String val = XmlHelper.getValue(eventReader);
                        presantation.brandSubtype(Formats.trim(val));
                        break;
                    }
                    case "ProductNumber": {
                        final String val = XmlHelper.getValue(eventReader);
                        presantation.productSubmitterNumber(Formats.trim(val));
                        break;
                    }
                    case "PackageType": {
                        try {
                            final Integer val = Integer.valueOf(XmlHelper.getValue(eventReader));
                            presantation.packageType(Formats.fromEnumToString(PackageTypeEnum.fromValue(val)));
                        } catch (final Exception e) {
                            Throwables.throwUnchecked(e);
                        }
                        break;
                    }
                    case "PackageUnits": {
                        final String val = XmlHelper.getValue(eventReader);
                        presantation.packageUnits(val);
                        break;
                    }
                    case "PackageNetWeight": {
                        final String val = XmlHelper.getValue(eventReader);
                        presantation.packageNetWeight(val);
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                final String localPart = endElement.getName().getLocalPart();
                if ("Presentation".equals(localPart)) {
                    break;
                }
            }
        }
    }

    private static void parseNovelSpecific(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final RequestNovelTobaccoSubmission builder) throws XMLStreamException {

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "DetailsDescriptionFile": {
                        builder.detailsDescriptionFile(Formats.att(context, XmlHelper.getAttachmentID(startElement)));
                        break;
                    }
                    case "UseInstructionsFile": {
                        builder.useInstructionsFile(Formats.att(context, XmlHelper.getAttachmentID(startElement)));
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if ("NovelSpecific".equals(endElement.getName().getLocalPart())) {
                    break;
                }
            }
        }
    }

}
