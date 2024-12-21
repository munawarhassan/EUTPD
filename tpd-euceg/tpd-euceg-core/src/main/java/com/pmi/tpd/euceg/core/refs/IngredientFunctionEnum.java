package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.IngredientFunction;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum IngredientFunctionEnum {

    /** */
    ADDICTIVE_ENHANCER(org.eu.ceg.IngredientFunctionEnum.ADDICTIVE_ENHANCER, "Addictive Enhancer"),
    /** */
    ADHESIVE(org.eu.ceg.IngredientFunctionEnum.ADHESIVE, "Adhesive"),
    /** */
    BINDER(org.eu.ceg.IngredientFunctionEnum.BINDER, "Binder"),
    /** */
    CARRIER(org.eu.ceg.IngredientFunctionEnum.CARRIER, "Carrier"),
    /** */
    COLOUR(org.eu.ceg.IngredientFunctionEnum.COLOUR, "Colour"),
    /** */
    COMBUSTION_MODIFIER(org.eu.ceg.IngredientFunctionEnum.COMBUSTION_MODIFIER, "Combustion Modifier"),
    /** */
    CASING(org.eu.ceg.IngredientFunctionEnum.CASING, "Casing"),
    /** */
    FIBRE(org.eu.ceg.IngredientFunctionEnum.FIBRE, "Fibre"),
    /** */
    FILLER(org.eu.ceg.IngredientFunctionEnum.FILLER, "Filler"),
    /** */
    FILTER_COMPONENT(org.eu.ceg.IngredientFunctionEnum.FILTER_COMPONENT, "Filter Component"),
    /** */
    FILTRATION_MATERIAL(org.eu.ceg.IngredientFunctionEnum.FILTRATION_MATERIAL, "Filtration Material"),
    /** */
    FLAVOUR_TASTE_ENHANCER(org.eu.ceg.IngredientFunctionEnum.FLAVOUR_ENHANCER, "Flavour and/or Taste Enhancer"),
    /** */
    HUMECTANT(org.eu.ceg.IngredientFunctionEnum.HUMECTANT, "Humectant"),
    /** */
    PH_MODIFIER(org.eu.ceg.IngredientFunctionEnum.PH_MODIFIER, "pH Modifier"),
    /** */
    PLASTICISER(org.eu.ceg.IngredientFunctionEnum.PLASTICIER, "Plasticiser"),
    /** */
    PRESERVATIVE(org.eu.ceg.IngredientFunctionEnum.PRESERVATIVE, "Preservative"),
    /** */
    SOLVENT_PROCESSING_AID(org.eu.ceg.IngredientFunctionEnum.SOLVANT, "Solvent - Processing Aid"),
    /** */
    REDUCED_IGNITION_PROPENSITY_AGENT(org.eu.ceg.IngredientFunctionEnum.REDUCE_IGNITION_PROPENSITY_AGENT, //
            "Reduced Ignition Propensity Agent"),
    /** */
    SIZING_AGENT(org.eu.ceg.IngredientFunctionEnum.SIZING_AGENT, "Sizing Agent"),
    /** */
    SMOKE_ENHANCER(org.eu.ceg.IngredientFunctionEnum.SMOKE_ENHANCER, "Smoke Enhancer"),
    /** */
    SMOKE_COLOUR_MODIFIER(org.eu.ceg.IngredientFunctionEnum.SMOKE_COLOUR_MODIFIER, "Smoke Colour Modifier"),
    /** */
    SMOKE_ODOUR_MODIFIER(org.eu.ceg.IngredientFunctionEnum.SMOKE_ODOUR_MODIFIER, "Smoke Odour Modifier"),
    /** */
    WRAPPER(org.eu.ceg.IngredientFunctionEnum.WRAPPER, "Wrapper"),
    /** */
    WATER_WETTING_AGENTS(org.eu.ceg.IngredientFunctionEnum.WATER_WETTING_AGENTS, "Water-Wetting Agents"),
    /** */
    VISCOSITY_MODIFIER(org.eu.ceg.IngredientFunctionEnum.VISCOSITY_MODIFIER, "Viscosity Modifier"),
    /** */
    OTHER(org.eu.ceg.IngredientFunctionEnum.OTHER, "Other");

    /** */
    private String name;

    /** */
    private org.eu.ceg.IngredientFunctionEnum value;

    /** */
    private IngredientFunctionEnum(final org.eu.ceg.IngredientFunctionEnum value, final String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * @return
     */
    public org.eu.ceg.IngredientFunctionEnum getValue() {
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
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public IngredientFunction toIngredientFunction() {
        return new IngredientFunction().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final IngredientFunctionEnum e : IngredientFunctionEnum.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }

}
