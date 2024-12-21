package com.pmi.tpd.euceg.core.refs;

import java.util.Optional;

import org.eu.ceg.SubmissionType;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum SubmissionTypeEnum {

    /** */
    NEW_PRODUCT("NEW_PRODUCT", org.eu.ceg.SubmissionTypeEnum.NEW, "Reporting of information on a new product", "New Product Launch"),

    /** */
    MODIFIED("MODIFIED", org.eu.ceg.SubmissionTypeEnum.MODIFICATION_NEW, //
            "Modification of information on a previously reported product leading to a new TP-ID number", "Update – New TP-ID"),

    /** */
    UPDATED("UPDATED", org.eu.ceg.SubmissionTypeEnum.UPDATE_ADDITION, //
            "Update of existing product – addition of information", "Update – Addition of Presentation"),

    /** */
    WITHDRAWAL_UPDATED("WITHDRAWAL_UPDATED", org.eu.ceg.SubmissionTypeEnum.UPDATE_WITHDRAW, //
            "Update of existing product – withdrawal of information", "Update - Withdrawal"),

    /** */
    OTHER_UPDATED("OTHER_UPDATED", org.eu.ceg.SubmissionTypeEnum.UPDATE_OTHER, //
            "Update of existing product – other information", "Update – Other"),

    /** */
    REGULAR_UPDATED("REGULAR_UPDATED", org.eu.ceg.SubmissionTypeEnum.UPDATE_ANNUAL_DATA, //
            "Updates to information to be submitted in regular intervals/annually such as sales "
                    + "data or actual quantities of ingredients", "Annual Report"),

    /** */
    CORRECTION("CORRECTION", org.eu.ceg.SubmissionTypeEnum.CORRECTION, "Correction", "Correction");

    /** */
    private String name;

    /** */
    private String shortDescription;

    /** */
    private final org.eu.ceg.SubmissionTypeEnum value;

    /** */
    private final String description;

    /**
     * @param name
     * @param value
     * @param description
     */
    SubmissionTypeEnum(final String name, final org.eu.ceg.SubmissionTypeEnum value, final String description,
            final String shortDescription) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.shortDescription = shortDescription;
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
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @return
     */
    public org.eu.ceg.SubmissionTypeEnum getEnum() {
        return value;
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
    public String getDescription() {
        return description;
    }

    /**
     * @param v
     * @return
     */
    public static Optional<SubmissionTypeEnum> fromValue(final int v) {
        try {
            final org.eu.ceg.SubmissionTypeEnum enumVal = org.eu.ceg.SubmissionTypeEnum.fromValue(v);
            return fromValue(enumVal);
        } catch (final IllegalArgumentException ex) {
            // do nothing
        }
        return Optional.empty();
    }

    public static Optional<SubmissionTypeEnum> fromValue(final org.eu.ceg.SubmissionTypeEnum enumVal) {
        try {
            final SubmissionTypeEnum[] l = SubmissionTypeEnum.values();
            for (final SubmissionTypeEnum e : l) {
                if (e.value == enumVal) {
                    return Optional.of(e);
                }
            }
        } catch (final IllegalArgumentException ex) {
            // do nothing
        }
        return Optional.empty();
    }

    /**
     * @return
     */
    public SubmissionType toSubmissionType() {
        return new SubmissionType().withValue(value).withConfidential(false);
    }
}
