package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.EmissionName;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum EmissionNameEnum {
    /** */
    NICOTINE(org.eu.ceg.EmissionNameEnum.NICOTINE, "Nicotine"),
    /** */
    ETHYLENE_GLYCOL(org.eu.ceg.EmissionNameEnum.ETHYLENE_GLYCOL, "Ethylene glycol"),
    /** */
    DIETHYLENE_GLYCOL(org.eu.ceg.EmissionNameEnum.DIETHYLENE_GLYCOL, "Diethylene glycol"),
    /** */
    FORMALDEHYDE(org.eu.ceg.EmissionNameEnum.FORMALDEHYDE, "Formaldehyde"),
    /** */
    ACETALDEHYDE(org.eu.ceg.EmissionNameEnum.ACETALDEHYDE, "Acetaldehyde"),
    /** */
    ACROLEIN(org.eu.ceg.EmissionNameEnum.ACROLEIN, "Acrolein"),
    /** */
    CROTONALDEHYDE(org.eu.ceg.EmissionNameEnum.CROTONALDEHYDE, "Crotonaldehyde"),
    /** */
    TSNA_NNN(org.eu.ceg.EmissionNameEnum.TSNA_NNN, "SNA: NNN"),
    /** */
    TSNA_NNK(org.eu.ceg.EmissionNameEnum.TSNA_NNK, "TSNA: NNK"),
    /** */
    CADMIUM(org.eu.ceg.EmissionNameEnum.CADMIUM, "Cadmium"),
    /** */
    CHROMIUM(org.eu.ceg.EmissionNameEnum.CHROMIUM, "Chromium"),
    /** */
    COPPER(org.eu.ceg.EmissionNameEnum.COPPER, "Copper"),
    /** */
    LEAD(org.eu.ceg.EmissionNameEnum.LEAD, "Lead"),
    /** */
    NICKEL(org.eu.ceg.EmissionNameEnum.NICKEL, "Nickel"),
    /** */
    ARSENIC(org.eu.ceg.EmissionNameEnum.ARSENIC, "Arsenic"),
    /** */
    TOLUENE(org.eu.ceg.EmissionNameEnum.TOLUENE, "Toluene"),
    /** */
    BENZENE(org.eu.ceg.EmissionNameEnum.BENZENE, "Benzene"),
    /** */
    BUTADIENE(org.eu.ceg.EmissionNameEnum.BUTADIENE, "1,3-Butadiene"),
    /** */
    ISOPRENE(org.eu.ceg.EmissionNameEnum.ISOPRENE, "Isoprene"),
    /** */
    DIACETYL(org.eu.ceg.EmissionNameEnum.DIACETYL, "Diacetyl"),
    /** */
    ACETYL_PROPIONYL(org.eu.ceg.EmissionNameEnum.ACETYL_PROPIONYL, "Acetyl Propionyl"),
    /** */
    OTHER(org.eu.ceg.EmissionNameEnum.OTHER, "Other");

    /** */
    private String name;

    /** */
    private org.eu.ceg.EmissionNameEnum value;

    /**
     * @param value
     * @param name
     */
    private EmissionNameEnum(final org.eu.ceg.EmissionNameEnum value, final String name) {
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
    public org.eu.ceg.EmissionNameEnum getValue() {
        return value;
    }

    /**
     * @return
     */
    @JsonValue
    public int getJsonValue() {
        return value.value();
    }

    /**
     * @return
     */
    public EmissionName toEmissionName() {
        return new EmissionName().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final EmissionNameEnum e : EmissionNameEnum.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }
}
