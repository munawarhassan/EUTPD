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
public class LdapSchema {

    /** Root node in LDAP from which to search for users and groups. Example: cn=users,dc=example,dc=com. */
    private String baseDn;

    /** Prepended to the base DN to limit the scope when searching for users. */
    private String additionalUserDn;

    /** Prepended to the base DN to limit the scope when searching for groups. */
    private String additionalGroupDn;

}