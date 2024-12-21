package com.pmi.tpd.web.rest.model;

import java.security.Principal;

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
public class UserRenameRequest implements Principal {

    /** */
    @NotNull
    @Size(max = 255)
    private final String name;

    /** */
    @NotNull
    @Size(max = 255)
    private final String newName;

    /**
     * @param name
     *            the current name of user.
     * @param newName
     *            the new name of user.
     */
    @Builder
    public UserRenameRequest(@JsonProperty(value = "name", required = true) final String name,
            @JsonProperty(value = "newName", required = true) final String newName) {
        this.name = name;
        this.newName = newName;
    }

}