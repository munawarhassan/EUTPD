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
public class UserPasswordUpdateRequest extends PasswordUpdateRequest {

    /** */
    @NotNull
    @Size(min = 5, max = 100)
    private final String oldPassword;

    /**
     * @param oldPassword
     * @param password
     * @param passwordConfirm
     */
    @Builder
    public UserPasswordUpdateRequest(@JsonProperty(value = "oldPassword", required = true) final String oldPassword,
            @JsonProperty(value = "password", required = true) final String password,
            @JsonProperty(value = "passwordConfirm", required = true) final String passwordConfirm) {
        super(password, passwordConfirm);
        this.oldPassword = oldPassword;
    }
}
