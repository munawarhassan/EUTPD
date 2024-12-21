package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.VoltageWattageAdjustable;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum VoltageWattageAdjustableEnum {

    /** */
    YES_VOLTAGE_AND_WATTAGE_ADJUSTABLE(org.eu.ceg.VoltageWattageAdjustableEnum.VOLTAGE_WATTAGE_ADJUSTABLE, //
            "Yes, voltage and wattage adjustable"),
    /** */
    YES_ONLY_VOLTAGE_ADJUSTABLE(org.eu.ceg.VoltageWattageAdjustableEnum.ONLY_VOLTAGE_ADJUSTABLE, //
            "Yes, only voltage adjustable"),
    /** */
    YES_ONLY_WATTAGE_ADJUSTABLE(org.eu.ceg.VoltageWattageAdjustableEnum.ONLY_WATTAGE_ADJUSTABLE, //
            "Yes, only wattage adjustable"),
    /** */
    NO_UN_ADJUSTABLE(org.eu.ceg.VoltageWattageAdjustableEnum.UNADJUSTABLE, "No, un-adjustable");

    /** */
    private String name;

    /** */
    private org.eu.ceg.VoltageWattageAdjustableEnum value;

    /** */
    private VoltageWattageAdjustableEnum(final org.eu.ceg.VoltageWattageAdjustableEnum value, final String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * @return
     */
    public org.eu.ceg.VoltageWattageAdjustableEnum getValue() {
        return value;
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
    public VoltageWattageAdjustable toVoltageWattageAdjustable() {
        return new VoltageWattageAdjustable().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final VoltageWattageAdjustableEnum e : VoltageWattageAdjustableEnum.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }

}
