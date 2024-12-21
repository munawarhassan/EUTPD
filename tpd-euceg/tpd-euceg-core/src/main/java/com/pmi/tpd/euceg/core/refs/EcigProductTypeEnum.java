package com.pmi.tpd.euceg.core.refs;

import java.util.Map;
import java.util.Optional;

import org.eu.ceg.EcigProductType;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public enum EcigProductTypeEnum {

    /** */
    ECIG_DISPOSABLE("Electronic cigarette – Disposable", "Electronic cigarette – Disposable", org.eu.ceg.EcigProductTypeEnum.DISPOSABLE),

    /** */
    ECIG_RECHARGEABLE("Electronic cigarette – Rechargeable, placed on the market with one type of e-liquid (fixed combination). "
            + "Any rechargeable which can also be used as a refillable should be reported "
            + "under the refillable category", "Electronic cigarette – Rechargeable combination with closed tank.", org.eu.ceg.EcigProductTypeEnum.RECHARGEABLE),

    /** */
    ECIG_RECHARGEABLE_DEVICE_ONLY("Electronic cigarette – Rechargeable, device only.  Any rechargeable which can also be "
            + "used as a refillable should be reported under the refillable category ", "Electronic cigarette – Rechargeable, device only", org.eu.ceg.EcigProductTypeEnum.RECHARGEABLE_DEVICE_ONLY),

    /** */
    ECIG_REFILLABLE("Electronic cigarette – Refillable, placed on the market with one type of e-liquid (fixed combination).", "Electronic cigarette – Refillable combination with open tank.", org.eu.ceg.EcigProductTypeEnum.REFILLABLE),

    /** */
    ECIG_REFILLABLE_DEVICE_ONLY("Electronic cigarette – Refillable, device only", "Electronic cigarette – Refillable, device only", org.eu.ceg.EcigProductTypeEnum.REFILLABLE_DEVICE_ONLY),

    /** */
    KIT("Kit – Pack containing more than one different e-cigarette "
            + "device and/or more than one different refill container/cartridge", "Kit – Pack with different device and/or refill cartridge", org.eu.ceg.EcigProductTypeEnum.KIT),

    /** */
    REFILL_CONTAINER_CARTRIDGE("Refill container/cartridge containing e-liquid", "Refill container/cartridge containing e-liquid", org.eu.ceg.EcigProductTypeEnum.REFILL_CONTAINER_CARTRIDGE),

    /** */
    INDIVIDUAL_PART("Individual part of electronic cigarette capable of containing e-liquid", "Individual part of electronic cigarette capable of containing e-liquid", org.eu.ceg.EcigProductTypeEnum.INDIVIDUAL_PART),

    /** */
    OTHER("Other", "Other", org.eu.ceg.EcigProductTypeEnum.OTHER);

    /** */
    private String name;

    private String shortDescription;

    /** */
    private final org.eu.ceg.EcigProductTypeEnum value;

    /**
     * @param name
     * @param value
     */
    EcigProductTypeEnum(final String name, final String shortDescription, final org.eu.ceg.EcigProductTypeEnum value) {
        this.name = name;
        this.value = value;
        this.shortDescription = shortDescription;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public static Optional<EcigProductTypeEnum> fromValue(final int v) {
        final org.eu.ceg.EcigProductTypeEnum enumVal = org.eu.ceg.EcigProductTypeEnum.fromValue(v);
        return fromValue(enumVal);
    }

    public static Optional<EcigProductTypeEnum> fromValue(final org.eu.ceg.EcigProductTypeEnum enumVal) {
        for (final EcigProductTypeEnum e : EcigProductTypeEnum.values()) {
            if (e.value == enumVal) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    @JsonValue
    public int getValue() {
        return value.value();
    }

    public EcigProductType toEcigProductType() {
        return new EcigProductType().withValue(value).withConfidential(false);
    }

    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final EcigProductTypeEnum e : EcigProductTypeEnum.values()) {
            map.put(e.getValue(), e.getName());
        }
        return map;
    }

}
