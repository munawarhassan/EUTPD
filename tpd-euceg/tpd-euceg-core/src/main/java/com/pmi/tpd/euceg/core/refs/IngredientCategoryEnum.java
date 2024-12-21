package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.IngredientCategory;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum IngredientCategoryEnum {
    /** */
    TOBACCO_BURNT(org.eu.ceg.IngredientCategoryEnum.TOBACCO_BURNT, "Tobacco (burnt)"),
    /** */
    TOBACCO_UNBURNT(org.eu.ceg.IngredientCategoryEnum.TOBACCO_UNBURNT, "Tobacco (unburnt)"),
    /** */
    PAPER_BURNT(org.eu.ceg.IngredientCategoryEnum.PAPER_BURNT, "Paper (burnt)"),
    /** */
    SIDE_SEAM_ADHESIVE_BURNT(org.eu.ceg.IngredientCategoryEnum.SIDE_SEAM_ADHESIVE, "Side seam adhesive (burnt )"),
    /** */
    INKS_USED_ON_CIGARETTE_PAPER_BURNT(org.eu.ceg.IngredientCategoryEnum.INKS, "Inks used on cigarette paper (burnt)"),
    /** */
    FILTRATION_MATERIAL_UNBURNT(org.eu.ceg.IngredientCategoryEnum.FILTRATION_MATERIAL, "Filtration material (unburnt)"),
    /** */
    FILTER_OVERWRAP_UNBURNT(org.eu.ceg.IngredientCategoryEnum.FILTER_OVERWRAP, "Filter overwrap (unburnt)"),
    /** */
    FILTER_ADHESIVE_UNBURNT(org.eu.ceg.IngredientCategoryEnum.FILTER_ADHESIVE, "Filter adhesive (unburnt)"),
    /** */
    TIPPING_PAPER_AND_TIPPING_PAPER_INKS_UNBURNT(org.eu.ceg.IngredientCategoryEnum.TIPPING_PAPER, //
            "Tipping paper and tipping paper inks (unburnt)"),
    /** */
    ADHESIVE_UNBURNT(org.eu.ceg.IngredientCategoryEnum.ADHESIVE_UNBURNT, "Adhesive (unburnt)"),
    /** */
    ADHESIVE_BURNT(org.eu.ceg.IngredientCategoryEnum.ADHESIVE_BURNT, "Adhesive (burnt)"),
    /** */
    TIPS_UNBURNT(org.eu.ceg.IngredientCategoryEnum.TIPS, "Tips (unburnt)"),
    /** */
    POUCH_MATERIAL_UNBURNT(org.eu.ceg.IngredientCategoryEnum.POUCH_MATERIAL, "Pouch material (unburnt)"),
    /** */
    PAPER_UNBURNT(org.eu.ceg.IngredientCategoryEnum.PAPER_UNBURNT, "Paper (unburnt)"),
    /** */
    OTHER_UNBURNT(org.eu.ceg.IngredientCategoryEnum.OTHER, "Other (unburnt)");

    /** */
    private String name;

    /** */
    private org.eu.ceg.IngredientCategoryEnum value;

    /** */
    private IngredientCategoryEnum(final org.eu.ceg.IngredientCategoryEnum value, final String name) {
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
    public org.eu.ceg.IngredientCategoryEnum getValue() {
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
    public IngredientCategory toIngredientCategory() {
        return new IngredientCategory().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final IngredientCategoryEnum e : IngredientCategoryEnum.values()) {
            map.put(e.getValue().value(), e.getName());
        }
        return map;
    }
}
