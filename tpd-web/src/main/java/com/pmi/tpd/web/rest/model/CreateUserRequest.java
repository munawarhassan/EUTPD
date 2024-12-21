package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.pmi.tpd.api.user.UserDirectory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * POJO to create a new user from JSON or xml representation.
 *
 * @author Christophe Friederich
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "CreateUser", description = "Contains information to create a new user account")
public class CreateUserRequest {

    /** */
    @Pattern(regexp = "^[a-zA-Z0-9]*$")
    @NotNull
    @Size(min = 1, max = 20)
    private String username;

    /** */
    @Size(min = 5, max = 100)
    private String password;

    /** */
    @NotNull
    @Size(max = 250)
    private String displayName;

    /** */
    @NotNull
    @Email
    @Size(min = 1, max = 255)
    private String emailAddress;

    /** */
    @NotNull
    private UserDirectory directory;

    /**
     * <code>true</code> to add the user to the default group, which can be used to grant them a set of initial
     * permissions; otherwise, <code>false</code> to not add them to a group.
     */
    private boolean addToDefaultGroup;

    /**
     * if present and not <code>false</code> instead of requiring a password, the create user will be notified via email
     * their account has been created and requires a password to be reset. This option can only be used if a mail server
     * has been configured.
     */
    private boolean notify;

}
