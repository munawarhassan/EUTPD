package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.ToxicologicalDataAvailable;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum ToxicologicalDataAvailableEnum {
    /** No toxicological data available. */
    NO_TOXICOLOGICAL_AVAILABLE(org.eu.ceg.ToxicologicalDataAvailableEnum.NO_DATA_AVAILABLE, //
            "No toxicological data available"),
    /** Toxicological data is available but not new. */
    TOXICOLOGICAL_DATA_AVAILABLE(org.eu.ceg.ToxicologicalDataAvailableEnum.DATA_AVAILABLE, //
            "Toxicological data is available but not new"),
    /** New toxicological data has been obtained since the last reporting period. */
    NEW_TOXICOLOGICAL_DATA(org.eu.ceg.ToxicologicalDataAvailableEnum.NEW_DATA_AVAILABLE, //
            "New toxicological data has been obtained since the last reporting period");

    /** */
    private String name;

    /** */
    private org.eu.ceg.ToxicologicalDataAvailableEnum value;

    /**
     * @param value
     * @param name
     */
    private ToxicologicalDataAvailableEnum(final org.eu.ceg.ToxicologicalDataAvailableEnum value, final String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public org.eu.ceg.ToxicologicalDataAvailableEnum getValue() {
        return value;
    }

    /**
     * @return
     */
    public ToxicologicalDataAvailable toToxicologicalDataAvailable() {
        return new ToxicologicalDataAvailable().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final ToxicologicalDataAvailableEnum e : ToxicologicalDataAvailableEnum.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }

}
