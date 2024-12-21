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

import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.Parsers;
import com.pmi.tpd.euceg.api.ProductType;
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
public class RequestOverviewSubmission extends BaseRequestExportSubmission {

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
                        parseProduct(eventReader, this);
                        break;
                    }
                }
            }
        }
    }

    public static void parseProduct(@Nonnull final XMLEventReader eventReader,
        @Nonnull final RequestOverviewSubmission builder) throws Exception {
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
                    case "ProductType": {
                        final int val = Integer.valueOf(XmlHelper.getValue(eventReader));
                        final ProductType type = builder.productCategory();
                        if (ProductType.TOBACCO.equals(type)) {
                            builder.productType(Formats.fromEnumToString(TobaccoProductTypeEnum.fromValue(val)));
                        } else if (ProductType.ECIGARETTE.equals(type)) {
                            builder.productType(Formats.fromEnumToString(EcigProductTypeEnum.fromValue(val)));
                        }
                        break;
                    }
                    case "PreviousProductID": {
                        builder.previousProductId(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "ProductID": {
                        builder.productId(XmlHelper.getValue(eventReader));
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

    private static void parsePresentations(final XMLEventReader eventReader, final RequestOverviewSubmission builder)
            throws XMLStreamException {
        final List<Presentation> presentations = Lists.newArrayList();
        builder.presentations(presentations);

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Presentation":
                        final RequestOverviewSubmission.Presentation presentation = new Presentation();
                        parsePresentation(eventReader, presentation);
                        presentations.add(presentation);
                        break;
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if ("Presentations".equals(endElement.getName().getLocalPart())) {
                    break;
                }
            }
        }
    }

    private static void parsePresentation(final XMLEventReader eventReader,
        final RequestOverviewSubmission.Presentation presantation) throws XMLStreamException {

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

}
