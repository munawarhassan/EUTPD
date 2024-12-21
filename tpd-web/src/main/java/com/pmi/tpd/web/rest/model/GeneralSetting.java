package com.pmi.tpd.web.rest.model;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.IApplicationProperties;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@Getter
@Builder
@JsonDeserialize(builder = GeneralSetting.GeneralSettingBuilder.class)
@JsonSerialize
public class GeneralSetting {

    /** */
    private final String language;

    /**
     * Create and initialise a new instance of {@link GeneralSetting} with given application properties.
     *
     * @param props
     *            a application properties
     * @return Returns new initialised instance of {@link GeneralSetting}.
     */
    @Nonnull
    public static GeneralSetting create(@Nonnull final IApplicationProperties props) {
        final GeneralSetting general = GeneralSetting.builder()
                .language(props.getDefaultBackedString(ApplicationConstants.PropertyKeys.LANGUAGE).orElse(null))
                .build();
        return general;
    }

    /**
     * @param applicationProperties
     */
    public void save(final IApplicationProperties applicationProperties) {
        applicationProperties.setString(ApplicationConstants.PropertyKeys.LANGUAGE, language);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class GeneralSettingBuilder {

    }

}