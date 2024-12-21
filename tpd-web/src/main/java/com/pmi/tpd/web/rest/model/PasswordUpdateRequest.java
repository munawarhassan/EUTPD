package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * POJO to update user password from JSON or xml representation.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
public class PasswordUpdateRequest {

    /** */
    @NotNull
    @Size(min = 5, max = 100)
    @JsonProperty(value = "password", required = true)
    private final String password;

    /** */
    @NotNull
    @Size(min = 5, max = 100)
    @JsonProperty(value = "passwordConfirm", required = true)
    private final String passwordConfirm;

}