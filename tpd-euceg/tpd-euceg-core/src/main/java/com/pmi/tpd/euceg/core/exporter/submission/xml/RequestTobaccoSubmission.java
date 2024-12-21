package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.io.Reader;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
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
public class RequestTobaccoSubmission extends BaseRequestExportSubmission {

    // detail
    /** */
    private String productWeight;

    /** */
    private String productTobaccoWeight;

    /** */
    private String productLength;

    /** */
    private String productDiameter;

    /** */
    private String productFilter;

    /** */
    private String productFilterLength;

    /** */
    private String technicalFiles;

    /** */
    private String marketResearchFiles;

    // end section detail

    // tnco

    /** */
    private String tncoEmissionTar;

    /** */
    private String tncoEmissionNicotine;

    /** */
    private String tncoEmissionCo;

    // end section detail

    //
    //

    /** */
    private String generalComment;

    /** */
    private String sameCompositionOther;
    //
    //

    //
    // Cigarette
    /** */
    private String cigaretteCharacterisingFlavour;

    /** */
    private String cigaretteFilterVentilation;

    /** */
    private String cigaretteFilterDropPressureOpen;

    /** */
    private String cigaretteFilterDropPressureClosed;
    //
    //

    //
    // Ryo/Pipe
    //
    private String ryoPipeSpecificTotalNicotineContent;
    //
    //

    //
    // smokless
    //
    /** */
    private String smokelessTotalNicotineContent;

    /** */
    private String smokelessPh;
    //
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

        /** */
        private String nationalComment;

        /** */
        private String unitPacketPictureFiles;

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
                    case "GeneralComment": {
                        generalComment(Formats.trim(XmlHelper.getValue(eventReader)));
                        break;
                    }
                }
            }
        }
    }

    public static void parseProduct(final BaseExcelExporter<?> context,
        @Nonnull final XMLEventReader eventReader,
        @Nonnull final RequestTobaccoSubmission builder) throws Exception {
        Assert.checkNotNull(builder, "builder");

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Presentations": {
                        parsePresentations(context, eventReader, startElement, builder);
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
                        builder.productWeight(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "TobaccoWeight": {
                        builder.productTobaccoWeight(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Length": {
                        builder.productLength(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Diameter": {
                        builder.productDiameter(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Filter": {
                        builder.productFilter(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "FilterLength": {
                        builder.productFilterLength(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "TechnicalFiles": {
                        builder.technicalFiles(
                            Formats.joinAttattachment(context, XmlHelper.getAttachmentIDs(eventReader, startElement)));
                        break;
                    }
                    case "MarketResearchFiles": {
                        builder.marketResearchFiles(
                            Formats.joinAttattachment(context, XmlHelper.getAttachmentIDs(eventReader, startElement)));
                        break;
                    }
                    case "TncoEmission": {
                        parseTncoEmission(context, eventReader, startElement, builder);
                        break;
                    }
                    case "CigaretteSpecific": {
                        parseCigarette(context, eventReader, startElement, builder);
                        break;
                    }
                    case "RyoPipeSpecific": {
                        parseRyoPipeSpecific(context, eventReader, startElement, builder);
                        break;
                    }
                    case "SmokelessSpecific": {
                        parseSmokeless(context, eventReader, startElement, builder);
                        break;
                    }

                    case "SameCompositionProducts": {
                        while (eventReader.hasNext()) {
                            final XMLEvent ev = eventReader.nextTag();
                            if (ev.isStartElement()) {
                                final StartElement start = ev.asStartElement();
                                if ("ProductIdentification".equals(start.getName().getLocalPart())) {
                                    builder.sameCompositionOther(XmlHelper.getValue(eventReader));
                                    break;
                                }
                            } else if (ev.isEndElement()) {
                                final EndElement end = ev.asEndElement();
                                if ("SameCompositionProducts".equals(end.getName().getLocalPart())) {
                                    break;
                                }
                            }
                        }

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

    private static void parsePresentations(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final RequestTobaccoSubmission builder) throws XMLStreamException {
        final List<Presentation> presentations = Lists.newArrayList();
        builder.presentations(presentations);

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Presentation": {
                        final Presentation presentation = new Presentation();
                        parsePresentation(context, eventReader, startElement, presentation);
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

    private static void parsePresentation(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final Presentation presantation) throws XMLStreamException {

        final QName startName = element.getName();
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
                    case "NationalComment": {
                        presantation.nationalComment(Formats.trim(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "UnitPacketPictureFiles": {
                        presantation.unitPacketPictureFiles(
                            Formats.joinAttattachment(context, XmlHelper.getAttachmentIDs(eventReader, startElement)));
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if (startName.equals(endElement.getName())) {
                    break;
                }
            }
        }
    }

    private static void parseTncoEmission(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final RequestTobaccoSubmission builder) throws XMLStreamException {

        final QName startName = element.getName();
        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Tar": {
                        builder.tncoEmissionTar(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Nicotine": {
                        builder.tncoEmissionNicotine(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Co": {
                        builder.tncoEmissionCo(XmlHelper.getValue(eventReader));
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if (startName.equals(endElement.getName())) {
                    break;
                }
            }
        }
    }

    private static void parseCigarette(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final RequestTobaccoSubmission builder) throws XMLStreamException {

        final QName startName = element.getName();
        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "CharacterisingFlavour": {
                        builder.cigaretteCharacterisingFlavour(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "FilterVentilation": {
                        builder.cigaretteFilterVentilation(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "FilterDropPressureClosed": {
                        builder.cigaretteFilterDropPressureClosed(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "FilterDropPressureOpen": {
                        builder.cigaretteFilterDropPressureOpen(XmlHelper.getValue(eventReader));
                        break;
                    }
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if (startName.equals(endElement.getName())) {
                    break;
                }
            }
        }
    }

    private static void parseRyoPipeSpecific(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final RequestTobaccoSubmission builder) throws XMLStreamException {

        final QName startName = element.getName();
        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "TotalNicotineContent": {
                        builder.ryoPipeSpecificTotalNicotineContent(XmlHelper.getValue(eventReader));
                        break;
                    }

                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if (startName.equals(endElement.getName())) {
                    break;
                }
            }
        }
    }

    private static void parseSmokeless(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final RequestTobaccoSubmission builder) throws XMLStreamException {

        final QName startName = element.getName();
        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Ph": {
                        builder.smokelessPh(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "TotalNicotineContent": {
                        builder.smokelessTotalNicotineContent(XmlHelper.getValue(eventReader));
                        break;
                    }

                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if (startName.equals(endElement.getName())) {
                    break;
                }
            }
        }
    }

}
