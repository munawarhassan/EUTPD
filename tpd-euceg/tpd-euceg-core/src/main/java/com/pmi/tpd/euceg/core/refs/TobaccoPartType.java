package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.PartType;
import org.eu.ceg.PartTypeEnum;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum TobaccoPartType {
    /** */
    TOBACCO_LEAF(PartTypeEnum.TOBACCO_LEAF, "Tobacco leaf"),
    /** */
    MANUFACTURED_CUT_STEMS(PartTypeEnum.CUT_STEMS, "Manufactured – Cut stems"),
    /** */
    MANUFACTURED_RECONSTITUTED_TOBACCO(PartTypeEnum.RECONSTITUTED_TOBACCO, "Manufactured - Reconstituted tobacco"),
    /** */
    MANUFACTURED_EXPANDED_TOBACCO(PartTypeEnum.EXPANDED_TOBACCO, "Manufactured - Expanded tobacco"),
    /** */
    OTHER_UNSPECIFIED(PartTypeEnum.OTHER, "Other/Unspecified");

    /** */
    private String name;

    /** */
    private PartTypeEnum value;

    /**
     * @param value
     * @param name
     */
    private TobaccoPartType(final PartTypeEnum value, final String name) {
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
    public PartTypeEnum getValue() {
        return value;
    }

    /**
     * @return
     */
    public PartType toPartType() {
        return new PartType().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final TobaccoPartType e : TobaccoPartType.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }

}
