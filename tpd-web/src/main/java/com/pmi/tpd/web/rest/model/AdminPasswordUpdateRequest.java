package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;

/**
 * POJO to update user password by administrator from JSON or xml representation.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
@Getter
@JsonSerialize
public class AdminPasswordUpdateRequest extends PasswordUpdateRequest {

    /** */
    @NotNull
    @Size(min = 1, max = 255)
    private final String name;

    @Builder
    public AdminPasswordUpdateRequest(@JsonProperty(value = "password", required = true) final String password,
            @JsonProperty(value = "passwordConfirm", required = true) final String passwordConfirm,
            @JsonProperty(value = "name", required = true) final String name) {
        super(password, passwordConfirm);
        this.name = name;
    }

}