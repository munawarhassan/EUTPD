package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.LeafType;
import org.eu.ceg.LeafTypeEnum;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum TobaccoLeafType {
    /** */
    VIRGINIA(LeafTypeEnum.VIRGINIA, "Virginia"),
    /** */
    BURLEY(LeafTypeEnum.BURLEY, "Burley"),
    /** */
    ORIENTAL(LeafTypeEnum.ORIENTAL, "Oriental"),
    /** */
    MARYLAND(LeafTypeEnum.MARYLAND, "Maryland"),
    /** */
    KENTUCKY(LeafTypeEnum.KENTUCKY, "Kentucky"),
    /** */
    DARK(LeafTypeEnum.DARK, "Dark"),
    /** */
    OTHER(LeafTypeEnum.OTHER, "Other"),
    /** */
    UNSPECIFIED(LeafTypeEnum.UNSPECIFIED, "Unspecified");

    /** */
    private String name;

    /** */
    private LeafTypeEnum value;

    /**
     * @param value
     * @param name
     */
    private TobaccoLeafType(final LeafTypeEnum value, final String name) {
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
    public LeafTypeEnum getValue() {
        return value;
    }

    /**
     * @return
     */
    public LeafType toLeafType() {
        return new LeafType().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final TobaccoLeafType e : TobaccoLeafType.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }

}
