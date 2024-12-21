package com.pmi.tpd.euceg.core.refs;

import java.util.Map;

import org.eu.ceg.PackageType;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum PackageTypeEnum {

    /** */
    FLIP_TOP_BOX_SQUARE_CORNER("FLIP_TOP_BOX_SQUARE_CORNER", org.eu.ceg.PackageTypeEnum.FLIP_TOP_BOX_SQUARE_CORNER, "Flip top box, square corner"),

    /** */
    FLIP_TOP_BOX_BEVEL_CORNER_OCTAGONAL("FLIP_TOP_BOX_BEVEL_CORNER_OCTAGONAL", org.eu.ceg.PackageTypeEnum.FLIP_TOP_BOX_BEVEL_CORNER, "Flip top box, bevel corner/octagonal"),

    /** */
    FLIP_TOP_BOX_ROUNDED_CORNER("FLIP_TOP_BOX_ROUNDED_CORNER", org.eu.ceg.PackageTypeEnum.FLIP_TOP_BOX_ROUNDED_CORNER, "Flip top box, rounded corner"),

    /** */
    SHOULDER_HINGED_BOX("SHOULDER_HINGED_BOX", org.eu.ceg.PackageTypeEnum.SHOULDER_HINGED_BOX, "Shoulder hinged box"),

    /** */
    SOFT_PACK("SOFT_PACK", org.eu.ceg.PackageTypeEnum.SOFT_PACK, "Soft pack"),

    /** */
    POUCH_WITH_FLAP("POUCH_WITH_FLAP", org.eu.ceg.PackageTypeEnum.POUCH_WITH_FLAP, "Pouch with flap"),

    /** */
    BUCKET("BUCKET", org.eu.ceg.PackageTypeEnum.BUCKET, "Bucket (Cylindrical or cuboid)"),

    /** */
    CUBOID_CAN("CUBOID_CAN", org.eu.ceg.PackageTypeEnum.CUBOID_CAN, "Cuboid can"),

    /** */
    BLOCK_FOIL_PACK("BLOCK_FOIL_PACK", org.eu.ceg.PackageTypeEnum.BLOCK_PACK, "Block/Foil pack"),

    /** */
    CYLINDER_CARD_CAN("CYLINDER_CARD_CAN", org.eu.ceg.PackageTypeEnum.CYLINDER_CARD, "Cylinder card/can"),

    /** */
    STANDING_POUCH("STANDING_POUCH", org.eu.ceg.PackageTypeEnum.STANDING_POUCH, "Standing pouch"),

    /** */
    FOLDING_BOX("FOLDING_BOX", org.eu.ceg.PackageTypeEnum.FOLDING_BOX, "Folding box"),

    /** */
    CARTON_BOX("CARTON_BOX", org.eu.ceg.PackageTypeEnum.CARTON_BOX, "Carton box"),

    /** */
    HINGED_BOX("HINGED_BOX", org.eu.ceg.PackageTypeEnum.HINGED_BOX, "Hinged box"),

    /** */
    HINGED_TIN("HINGED_TIN", org.eu.ceg.PackageTypeEnum.HINGED_TIN, "Hinged tin"),

    /** */
    FLIP_TOP_PACK("FLIP_TOP_PACK", org.eu.ceg.PackageTypeEnum.FLIP_TOP_PACK, "Flip top pack"),

    /** */
    SINGLE_TUBE_TIN("SINGLE_TUBE_TIN", org.eu.ceg.PackageTypeEnum.SINGLE_TUBE_TIN, "Single tube tin"),

    /** */
    BUNDLE("BUNDLE", org.eu.ceg.PackageTypeEnum.BUNDLE, "Bundle"),

    /** */
    MULTI_CIGAR_TUBE("MULTI_CIGAR_TUBE", org.eu.ceg.PackageTypeEnum.MULTI_CIGAR_TUBE, "Multi cigar tube"),

    /** */
    CYLINDER_TIN("CYLINDER_TIN", org.eu.ceg.PackageTypeEnum.CYLINDER_TIN, "Cylinder tin"),

    /** */
    ROUND_TIN("ROUND_TIN", org.eu.ceg.PackageTypeEnum.ROUND_TIN, "Round tin"),

    /** */
    STANDING_POUCH_ROLL_FOLD("STANDING_POUCH_ROLL_FOLD", org.eu.ceg.PackageTypeEnum.STANDING_POUCH_ROLL_FOLD, "Standing pouch roll-fold"),

    /** */
    SLIDE_LID_BOX("SLIDE_LID_BOX", org.eu.ceg.PackageTypeEnum.SLIDE_LID_BOX, "Slide lid box"),

    /** */
    FLOW_WRAP("FLOW_WRAP", org.eu.ceg.PackageTypeEnum.FLOW_WRAP, "Flow wrap"),

    /** */
    FOLDING_POUCH("FOLDING_POUCH", org.eu.ceg.PackageTypeEnum.FOLDING_POUCH, "Folding pouch"),

    /** */
    SHELL_HULL_AND_SLIDE_BOX("SHELL_HULL_AND_SLIDE_BOX", org.eu.ceg.PackageTypeEnum.SLIDE_BOX, "Shell/Hull & Slide box"),

    /** */
    MULTI_PACK_DISPLAY("MULTI_PACK_DISPLAY", org.eu.ceg.PackageTypeEnum.MULTI_PACK, "Multi-pack display"),

    /** */
    PLASTIC_CONTAINER("PLASTIC_CONTAINER", org.eu.ceg.PackageTypeEnum.PLASTIC_CONTAINER, "Plastic Container");

    /** */
    private String name;

    /** */
    private final org.eu.ceg.PackageTypeEnum value;

    /** */
    private final String description;

    /**
     * @param name
     * @param value
     * @param description
     */
    PackageTypeEnum(final String name, final org.eu.ceg.PackageTypeEnum value, final String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    /**
     * @return
     */
    @JsonValue
    public int getValue() {
        return value.value();
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
    public PackageType toPackageType() {
        return new PackageType().withValue(value).withConfidential(false);
    }

    /**
     * @return
     */
    public static Map<Integer, String> toKeyMap() {
        final Map<Integer, String> map = Maps.newLinkedHashMap();
        for (final PackageTypeEnum e : PackageTypeEnum.values()) {
            map.put(e.getValue(), e.getDescription());
        }
        return map;
    }

}
