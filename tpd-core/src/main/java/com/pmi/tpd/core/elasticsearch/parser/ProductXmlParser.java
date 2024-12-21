package com.pmi.tpd.core.elasticsearch.parser;

import java.io.Reader;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.model.PresentationIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.euceg.api.Parsers;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.core.refs.EcigProductTypeEnum;
import com.pmi.tpd.euceg.core.refs.TobaccoProductTypeEnum;

import joptsimple.internal.Strings;

public class ProductXmlParser {

    public ProductXmlParser() {
    }

    public static ProductIndexed.ProductIndexedBuilder parse(@Nonnull final Reader xmlProduct, ProductType productType)
            throws XMLStreamException {
        return parse(xmlProduct, ProductIndexed.builder(), productType);
    }

    public static ProductIndexed.ProductIndexedBuilder parse(@Nonnull final Reader xmlProduct,
        @Nonnull final ProductIndexed.ProductIndexedBuilder indexedProduct,
        ProductType productType) throws XMLStreamException {
        Assert.checkNotNull(xmlProduct, "xmlProduct");
        Assert.checkNotNull(indexedProduct, "indexedProduct");

        // Instance of the class which helps on reading tags
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        // Initializing the handler to access the tags in the XML file
        final XMLEventReader eventReader = factory.createXMLEventReader(xmlProduct);
        boolean running = false;

        try {
            while (eventReader.hasNext()) {
                final XMLEvent xmlEvent = eventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    final StartElement startElement = xmlEvent.asStartElement();

                    if ("Product".equals(startElement.getName().getLocalPart())) {
                        running = true;
                    }
                    if (!running) {
                        continue;
                    }
                    switch (startElement.getName().getLocalPart()) {
                        case "Presentations":
                            parsePresentations(eventReader, indexedProduct);
                            break;
                        case "ProductType":
                            final Characters productTypeEvent = (Characters) eventReader.nextEvent();
                            final int type = Integer.valueOf(productTypeEvent.getData());
                            indexedProduct.type(type);
                            if (ProductType.TOBACCO.equals(productType)) {
                                indexedProduct.typeName(TobaccoProductTypeEnum.fromValue(type).getName());
                            } else {
                                indexedProduct.typeName(
                                    EcigProductTypeEnum.fromValue(type).map(EcigProductTypeEnum::getName).orElse(null));
                            }
                            break;
                        case "PreviousProductID":
                            final Characters previousProductIdEvent = (Characters) eventReader.nextEvent();
                            final String previousProductId = previousProductIdEvent.getData();
                            indexedProduct.previousProductId(previousProductId);
                            break;
                    }
                } else if (xmlEvent.isEndElement()) {
                    final EndElement endElement = xmlEvent.asEndElement();
                    if ("Product".equals(endElement.getName().getLocalPart())) {
                        break;
                    }
                }
            }
        } finally {
            if (eventReader != null) {
                eventReader.close();
            }
        }

        return indexedProduct;
    }

    private static void parsePresentations(final XMLEventReader eventReader,
        final ProductIndexed.ProductIndexedBuilder indexedProduct) throws XMLStreamException {
        final List<PresentationIndexed> presentations = Lists.newArrayList();

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "Presentation":
                        final PresentationIndexed presentation = new PresentationIndexed();
                        parsePresentation(eventReader, presentation);
                        presentations.add(presentation);
                        break;
                }
            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if ("Presentations".equals(endElement.getName().getLocalPart())) {
                    indexedProduct.presentations(presentations);
                    break;
                }
            }
        }
    }

    private static void parsePresentation(final XMLEventReader eventReader, final PresentationIndexed presantation)
            throws XMLStreamException {

        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();

                switch (startElement.getName().getLocalPart()) {
                    case "NationalMarket": {
                        final Characters value = (Characters) eventReader.nextEvent();
                        final String countryCode = value.getData();
                        if (!Strings.isNullOrEmpty(countryCode)) {
                            presantation.setNationalMarket(countryCode);
                            final Locale locale = new Locale("en", countryCode);
                            presantation.setNationalMarketName(locale.getDisplayCountry(Locale.ENGLISH));
                        }
                        break;
                    }
                    case "WithdrawalDate": {
                        final Characters value = (Characters) eventReader.nextEvent();
                        final String date = value.getData();
                        if (!Strings.isNullOrEmpty(date)) {
                            final LocalDate withdrawalDate = Parsers.parseLocalDate(date);
                            presantation.setWithdrawalDate(withdrawalDate);
                        }
                        break;
                    }
                    case "LaunchDate": {
                        final Characters value = (Characters) eventReader.nextEvent();
                        final String date = value.getData();
                        if (!Strings.isNullOrEmpty(date)) {
                            final LocalDate launchDate = Parsers.parseLocalDate(date);
                            presantation.setLaunchDate(launchDate);
                        }
                        break;
                    }
                    case "BrandName": {
                        final Characters value = (Characters) eventReader.nextEvent();
                        final String brandName = value.getData();
                        if (!Strings.isNullOrEmpty(brandName)) {
                            presantation.setBrandName(brandName.trim());
                        }
                        break;
                    }
                    case "BrandSubtypeName": {
                        final Characters value = (Characters) eventReader.nextEvent();
                        final String brandSubtypeName = value.getData();
                        if (!Strings.isNullOrEmpty(brandSubtypeName)) {
                            presantation.setBrandSubtype(brandSubtypeName);
                        }
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
