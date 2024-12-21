package com.pmi.tpd.database.liquibase;

import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Lists;

/**
 * A SAX content handler that scans a changeset log to determine the number of changesets and the number of individual
 * changes to apply.
 *
 * @see ChangeSetReader
 * @author Christophe Friederich
 * @since 1.3
 */
public class ChangeSetScanner extends DefaultHandler {

    /** The number of changes per change set. */
    private final List<Long> changeCounts = Lists.newLinkedList();

    /** The number of changes in the current change set. */
    private long changeCount;

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if ("changeSet".equals(qName)) {
            changeCounts.add(changeCount);
            changeCount = 0;
        } else if ("insert".equals(qName) || "delete".equals(qName)) {
            changeCount++;
        }
    }

    public Iterable<Long> getChangeCounts() {
        return changeCounts;
    }
}
