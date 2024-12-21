package com.pmi.tpd.core.user;

import java.security.Principal;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;

@JsonSerialize
@Getter
public class UserUpdate implements Principal {

    /** */
    @Pattern(regexp = "^[a-z0-9]*$")
    @NotNull
    @Size(min = 1, max = 20)
    private final String name;

    /** */
    @NotNull
    @Size(max = 250)
    private final String displayName;

    /** */
    @NotNull
    @Email
    @Size(min = 1, max = 255)
    private final String email;

    protected UserUpdate() {
        this(null, null, null);
    }

    public UserUpdate(final String name, final String displayName, final String email) {
        this.name = name;
        this.displayName = displayName;
        this.email = email;
    }

}