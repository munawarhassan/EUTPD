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

import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.VoltageWattageAdjustableEnum;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
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
public class RequestEcigSubmission extends BaseRequestExportSubmission {

    //
    // detail
    //

    /** */
    private String productWeight;

    /** */
    private String productVolume;

    /** */
    private String clpClassification;

    /** */
    private String studySummaryFiles;

    /** */
    private String marketResearchFiles;

    private List<Presentation> presentations;

    //
    // Design section
    //
    /** */
    private String designDescription;

    /** */
    private String designIdentificationEcigDevice;

    /** */
    private String designLiquidVolumeCapacity;

    /** */
    private String designNicotineConcentration;

    /** */
    private String designBatteryType;

    /** */
    private String designBatteryCapacity;

    /** */
    private String designVoltageWattageAdjustable;

    /** */
    private String designVoltage;

    /** */
    private String designVoltageLowerRange;

    /** */
    private String designVoltageUpperRange;

    /** */
    private String designWattage;

    /** */
    private String designWattageLowerRange;

    /** */
    private String designWattageUpperRange;

    /** */
    private String designAirflowAdjustable;

    /** */
    private String designWickChangeable;

    /** */
    private String designMicroprocessor;

    /** */
    private String designCoilComposition;

    /** */
    private String designCoilResistance;

    /** */
    private String designNicotineDoseUptakeFile;

    /** */
    private String designChildTamperProof;

    /** */
    private String designProductionFile;

    /** */
    private String designProductionConformity;

    /** */
    private String designQualitySafety;

    /** */
    private String designHighPurity;

    /** */
    private String designNonRisk;

    /** */
    private String designConsistentDosing;

    /** */
    private String designConsistentDosingMethodsFile;

    /** */
    private String designOpeningRefillFile;

    /** */
    private String designLeafletFile;

    //
    // end design section
    //

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
        private String packageUnits;

        /** */
        private String nationalComment;

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
        @Nonnull final RequestEcigSubmission builder) throws Exception {
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
                    case "Design": {
                        parseDesign(context, eventReader, startElement, builder);
                        break;
                    }
                    case "ProductType": {
                        final int productType = Integer.valueOf(XmlHelper.getValue(eventReader));
                        builder.productType(Formats.fromEnumToString(EcigProductTypeEnum.fromValue(productType)));
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
                    case "Weight": {
                        builder.productWeight(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Volume": {
                        builder.productVolume(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "ClpClassification": {
                        builder.clpClassification(Formats.trim(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "StudySummaryFiles": {
                        builder.studySummaryFiles(
                            Formats.joinAttattachment(context, XmlHelper.getAttachmentIDs(eventReader, startElement)));
                        break;
                    }
                    case "MarketResearchFiles": {
                        builder.marketResearchFiles(
                            Formats.joinAttattachment(context, XmlHelper.getAttachmentIDs(eventReader, startElement)));
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
        final RequestEcigSubmission builder) throws XMLStreamException {
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
                    case "PackageUnits": {
                        final String val = XmlHelper.getValue(eventReader);
                        presantation.packageUnits(val);
                        break;
                    }
                    case "NationalComment": {
                        presantation.nationalComment(Formats.trim(XmlHelper.getValue(eventReader)));
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

    private static void parseDesign(final BaseExcelExporter<?> context,
        final XMLEventReader eventReader,
        final StartElement element,
        final RequestEcigSubmission builder) throws Exception {

        final QName startName = element.getName();
        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Description": {
                        builder.designDescription(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "IdentificationEcigDevice": {
                        builder.designIdentificationEcigDevice(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "LiquidVolumeCapacity": {
                        builder.designLiquidVolumeCapacity(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "NicotineConcentration": {
                        builder.designNicotineConcentration(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "BatteryType": {
                        builder.designBatteryType(Formats.trim(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "BatteryCapacity": {
                        builder.designBatteryCapacity(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "VoltageWattageAdjustable": {
                        builder.designVoltageWattageAdjustable(Formats.fromEnumToString(
                            VoltageWattageAdjustableEnum.fromValue(Integer.valueOf(XmlHelper.getValue(eventReader)))));
                        break;
                    }
                    case "Voltage": {
                        builder.designVoltage(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "VoltageLowerRange": {
                        builder.designVoltageLowerRange(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "VoltageUpperRange": {
                        builder.designVoltageUpperRange(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "Wattage": {
                        builder.designWattage(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "WattageLowerRange": {
                        builder.designWattageLowerRange(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "WattageUpperRange": {
                        builder.designWattageUpperRange(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "AirflowAdjustable": {
                        builder.designAirflowAdjustable(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "WickChangeable": {
                        builder.designWickChangeable(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "Microprocessor": {
                        builder.designMicroprocessor(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "CoilComposition": {
                        builder.designCoilComposition(Formats.trim(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "CoilResistance": {
                        builder.designCoilResistance(XmlHelper.getValue(eventReader));
                        break;
                    }
                    case "NicotineDoseUptakeFile": {
                        builder.designNicotineDoseUptakeFile(
                            Formats.att(context, XmlHelper.getAttachmentID(startElement)));
                        break;
                    }
                    case "ChildTamperProof": {
                        builder.designChildTamperProof(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "ProductionFile": {
                        builder.designProductionFile(Formats.att(context, XmlHelper.getAttachmentID(startElement)));
                        break;
                    }
                    case "ProductionConformity": {
                        builder.designProductionConformity(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "QualitySafety": {
                        builder.designQualitySafety(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "HighPurity": {
                        builder.designHighPurity(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "NonRisk": {
                        builder.designNonRisk(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "ConsistentDosing": {
                        builder.designConsistentDosing(Formats.bool(XmlHelper.getValue(eventReader)));
                        break;
                    }
                    case "ConsistentDosingMethodsFile": {
                        builder.designConsistentDosingMethodsFile(
                            Formats.att(context, XmlHelper.getAttachmentID(startElement)));
                        break;
                    }
                    case "OpeningRefillFile": {
                        builder.designOpeningRefillFile(Formats.att(context, XmlHelper.getAttachmentID(startElement)));
                        break;
                    }
                    case "LeafletFile": {
                        builder.designLeafletFile(Formats.att(context, XmlHelper.getAttachmentID(startElement)));
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
