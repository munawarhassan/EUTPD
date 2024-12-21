package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.PresentationNumberTypeEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum ProductNumberTypeEnum {

    /** */
    SUBMITTER(PresentationNumberTypeEnum.SUBMITTER, "Submitter identifier number"),

    /** */
    UPC(PresentationNumberTypeEnum.UPC, "Universal Product Code"),

    /** */
    EAN(PresentationNumberTypeEnum.EAN, "European Article Number"),

    /** */
    GTIN(PresentationNumberTypeEnum.GTIN, "Global Trade Identification Number"),

    /** */
    SKU(PresentationNumberTypeEnum.SKU, "Stock Keeping Unit");

    /** */
    private String name;

    /** */
    private final String description;

    /**
     * @param value
     * @param description
     */
    ProductNumberTypeEnum(final PresentationNumberTypeEnum value, final String description) {
        this.name = value.name();
        this.description = description;
    }

    /**
     * @return
     */
    @JsonValue
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return
     */
    public static Map<String, String> toMap() {
        final Map<String, String> map = Maps.newLinkedHashMap();
        for (final ProductNumberTypeEnum e : ProductNumberTypeEnum.values()) {
            map.put(e.getName(), e.getDescription());
        }
        return map;
    }

    /**
     * @return
     */
    public PresentationNumberTypeEnum toPresentationNumberType() {
        return PresentationNumberTypeEnum.fromValue(getName());
    }

}
