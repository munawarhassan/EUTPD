package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.common.collect.Lists;

class XmlHelper {

    private static final QName QNAME_ATTACHMENT_ID = QName.valueOf("attachmentID");

    public static String getAttachmentID(final StartElement startElement) {
        return startElement.getAttributeByName(QNAME_ATTACHMENT_ID).getValue();
    }

    public static List<String> getAttachmentIDs(final XMLEventReader eventReader, final StartElement startElement)
            throws XMLStreamException {
        final List<String> attrs = Lists.newArrayList();
        final String localElement = startElement.getName().getLocalPart();
        while (eventReader.hasNext()) {
            final XMLEvent xmlEvent = eventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String name = element.getName().getLocalPart();
                if ("Attachment".equals(name)) {
                    attrs.add(getAttachmentID(element));
                }

            } else if (xmlEvent.isEndElement()) {
                final EndElement endElement = xmlEvent.asEndElement();
                if (localElement.equals(endElement.getName().getLocalPart())) {
                    break;
                }
            }
        }
        return attrs;
    }

    public static String getValue(final XMLEventReader eventReader) throws XMLStreamException {
        if (eventReader.hasNext() && eventReader.peek().isCharacters()) {
            final Characters value = (Characters) eventReader.nextEvent();
            final String str = value.getData();
            return str != null ? str.trim() : str;
        }
        return null;
    }

}
