package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@JsonSerialize
public class UpdatePasswordRequest {

    /** */
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty("currentPassword")
    private final String currentPassword;

    /** */
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty("newPassword")
    private final String newPassword;

    @Builder
    public UpdatePasswordRequest(@JsonProperty(value = "currentPassword", required = true) final String currentPassword,
            @JsonProperty(value = "newPassword", required = true) final String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}