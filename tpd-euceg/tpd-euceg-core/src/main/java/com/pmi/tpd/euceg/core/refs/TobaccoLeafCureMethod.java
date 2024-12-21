package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.LeafCureMethod;
import org.eu.ceg.LeafCureMethodEnum;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum TobaccoLeafCureMethod {
    /** */
    AIR(LeafCureMethodEnum.AIR, "Air"),
    /** */
    FIRE(LeafCureMethodEnum.FIRE, "Fire"),
    /** */
    STEAM(LeafCureMethodEnum.STEAM, "Steam"),
    /** */
    SUN(LeafCureMethodEnum.SUN, "Sun"),
    /** */
    FLUE(LeafCureMethodEnum.FLUE, "Flue"),
    /** */
    OTHER(LeafCureMethodEnum.OTHER, "Other");

    /** */
    private String name;

    /** */
    private LeafCureMethodEnum value;

    /**
     * @param value
     * @param name
     */
    private TobaccoLeafCureMethod(final LeafCureMethodEnum value, final String name) {
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
    public LeafCureMethodEnum getValue() {
        return value;
    }

    /**
     * @return
     */
    public LeafCureMethod toLeafCureMethod() {
        return new LeafCureMethod().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final TobaccoLeafCureMethod e : TobaccoLeafCureMethod.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }

}
