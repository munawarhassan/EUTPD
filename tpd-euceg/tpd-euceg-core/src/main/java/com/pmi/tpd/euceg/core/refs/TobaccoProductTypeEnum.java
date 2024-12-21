package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.TobaccoProductType;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public enum TobaccoProductTypeEnum {

    /** */
    CIGARETTE("Cigarette", org.eu.ceg.TobaccoProductTypeEnum.CIGARETTE),

    /** */
    CIGAR("Cigar", org.eu.ceg.TobaccoProductTypeEnum.CIGAR),

    /** */
    CIGARILLO("Cigarillo", org.eu.ceg.TobaccoProductTypeEnum.CIGARILLO),
    /** */
    ROLL_TOBACCO("Roll your own tobacco", org.eu.ceg.TobaccoProductTypeEnum.ROLL_YOUR_OWN),

    /** */
    PIPE_TOBACCO("Pipe Tobacco", org.eu.ceg.TobaccoProductTypeEnum.PIPE_TOBACCO),

    /** */
    WATERPIPE_TOBACCO("Waterpipe tobacco", org.eu.ceg.TobaccoProductTypeEnum.WATERPIPE_TOBACCCO),

    /** */
    ORAL_TOBACCO("Oral tobacco", org.eu.ceg.TobaccoProductTypeEnum.ORAL_TOBACCO),

    /** */
    NASAL_TOBACCO("Nasal tobacco", org.eu.ceg.TobaccoProductTypeEnum.NASAL_TOBACCO),

    /** */
    CHEWING_TOBACCO("Chewing tobacco", org.eu.ceg.TobaccoProductTypeEnum.CHEWING_TOBACCO),

    /** */
    HERBAL_PRODUCT("Herbal product for smoking", org.eu.ceg.TobaccoProductTypeEnum.HERBAL_PRODUCT),

    /** */
    NOVEL_TOBACCO_PRODUCT("Novel tobacco product", org.eu.ceg.TobaccoProductTypeEnum.NOVEL_TOBACCO_PRODUCT),

    /** */
    OTHER_TOBACCO_PRODUCT("Other tobacco product", org.eu.ceg.TobaccoProductTypeEnum.OTHER);

    /** */
    private String name;

    /** */
    private final org.eu.ceg.TobaccoProductTypeEnum value;

    /**
     * @param name
     * @param value
     */
    TobaccoProductTypeEnum(final String name, final org.eu.ceg.TobaccoProductTypeEnum value) {
        this.name = name;
        this.value = value;
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
    public int getValue() {
        return value.value();
    }

    /**
     * @return
     */
    public TobaccoProductType toTobaccoProductType() {
        return new TobaccoProductType().withValue(value).withConfidential(false);
    }

    /**
     * @param v
     * @return
     */
    public static TobaccoProductTypeEnum fromValue(final int v) {
        final org.eu.ceg.TobaccoProductTypeEnum enumVal = org.eu.ceg.TobaccoProductTypeEnum.fromValue(v);
        for (final TobaccoProductTypeEnum e : TobaccoProductTypeEnum.values()) {
            if (e.value == enumVal) {
                return e;
            }
        }
        throw new IllegalArgumentException(v + " is not a valid " + TobaccoProductTypeEnum.class.getName() + " value.");
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final TobaccoProductTypeEnum e : TobaccoProductTypeEnum.values()) {
            map.put(e.getValue(), e.getName());
        }
        return map;
    }
}
