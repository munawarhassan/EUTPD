package com.pmi.tpd.euceg.api.entity;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author christophe friederich
 * @since 2.5
 */
public enum SubmitterStatus {

    /** */
    DRAFT("Draft"),
    /** */
    IMPORTED("Imported"),
    /** */
    VALID("Valid"),
    /** */
    SENT("Sent");

    private String name;

    private SubmitterStatus(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Map<String, String> toMap() {
        final Map<String, String> map = Maps.newLinkedHashMap();
        for (final SubmitterStatus e : SubmitterStatus.values()) {
            map.put(e.toString(), e.getName());
        }
        return map;
    }
}
