package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.keystore.KeyStoreProperties;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = KeyStoreSetting.KeyStoreSettingBuilder.class)
@JsonSerialize
public class KeyStoreSetting {

    /** */
    @JsonProperty("enable")
    private final boolean enable;

    /** */
    @Pattern(
            regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
                    + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
            message = "{invalid.email}")
    @NotNull
    @JsonProperty("contact")
    private final String contact;

    public static KeyStoreSetting create(final KeyStoreProperties keyStoreProperties) {
        return KeyStoreSetting.builder()
                .enable(keyStoreProperties.getNotification().getExpiration().isEnable())
                .contact(keyStoreProperties.getNotification().getContact())
                .build();
    }

    public void save(final IApplicationProperties applicationProperties) {
        final KeyStoreProperties configuration = applicationProperties
                .getConfiguration(KeyStoreProperties.class);
        configuration.getNotification().setContact(contact);
        configuration.getNotification().getExpiration().setEnable(enable);
        applicationProperties.storeConfiguration(configuration);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class KeyStoreSettingBuilder {

    }
}
