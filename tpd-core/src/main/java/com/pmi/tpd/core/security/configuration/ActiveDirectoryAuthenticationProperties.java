package com.pmi.tpd.core.security.configuration;

import com.pmi.tpd.api.user.UserDirectory;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@Setter
public class ActiveDirectoryAuthenticationProperties extends LdapAuthenticationProperties {

    /** */
    private String domain;

    @Override
    public UserDirectory getDirectoryType() {
        if (empty())
            return null;
        return isAuthenticationOnly() ? UserDirectory.InternalActiveDirectory : UserDirectory.ActiveDirectory;
    };
}
