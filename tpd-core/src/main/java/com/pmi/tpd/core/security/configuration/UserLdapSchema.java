package com.pmi.tpd.core.security.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLdapSchema {

    /** The LDAP user object class type to use when loading users. */
    private String userObjectClass;

    /** The filter to use when searching user objects. */
    private String userObjectFilter;

    /** The attribute field to use on the user object. Examples: cn, sAMAccountName. */
    private String userNameAttribute;

    /** The RDN to use when loading the user username.Example: cn. */
    private String userNameRdnAttribute;

    /** The attribute field to use when loading the user first name. */
    private String userFirstNameAttribute;

    /** The attribute field to use when loading the user last name. */
    private String userLastNameAttribute;

    /** The attribute field to use when loading the user full name. */
    private String userDisplayNameAttribute;

    /** The attribute field to use when loading the user email. */
    private String userEmailAttribute;

    /** The attribute field to use when manipulating a user password. */
    private String userPasswordAttribute;

    /** The attribute field to use for tracking user identity across user renames. */
    private String userUniqueIDAttribute;

    public void override(final UserLdapSchema schema) {
        if (userObjectClass == null) {
            userObjectClass = schema.userObjectClass;
        }
        if (userObjectFilter == null) {
            userObjectFilter = schema.userObjectFilter;
        }
        if (userNameAttribute == null) {
            userNameAttribute = schema.userNameAttribute;
        }
        if (userNameRdnAttribute == null) {
            userNameRdnAttribute = schema.userNameRdnAttribute;
        }
        if (userFirstNameAttribute == null) {
            userFirstNameAttribute = schema.userFirstNameAttribute;
        }
        if (userLastNameAttribute == null) {
            userLastNameAttribute = schema.userLastNameAttribute;
        }
        if (userDisplayNameAttribute == null) {
            userDisplayNameAttribute = schema.userDisplayNameAttribute;
        }
        if (userEmailAttribute == null) {
            userEmailAttribute = schema.userEmailAttribute;
        }
        if (userPasswordAttribute == null) {
            userPasswordAttribute = schema.userPasswordAttribute;
        }
        if (userUniqueIDAttribute == null) {
            userUniqueIDAttribute = schema.userUniqueIDAttribute;
        }
    }
}
