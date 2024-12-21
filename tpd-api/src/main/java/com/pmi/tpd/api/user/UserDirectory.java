package com.pmi.tpd.api.user;

import static com.pmi.tpd.api.util.Assert.checkHasText;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum UserDirectory {
    /** user internal directory. */
    Internal("User Internal Directory", true),
    /** Internal with LDAP Authentication. */
    InternalLdap("Internal with LDAP Authentication", true),
    /** */
    InternalActiveDirectory("Internal with Active Directory Authentication", true),
    /** */
    ActiveDirectory("Microsoft Active Directory", false),
    /** */
    Ldap("LDAP", false);

    /** */
    private String description;

    /** */
    private boolean authenticationOnly;

    /**
     * @param description
     *            the description of directory.
     * @param authenticationOnly
     *            indicates if directory is use for authentication only.
     * @param persistentPasswordRequired
     */
    UserDirectory(final String description, final boolean authenticationOnly) {
        this.description = checkHasText(description, "description");
        this.authenticationOnly = authenticationOnly;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns {@code true} whether the directory use internal authorization (use for authentication only).
     */

    public boolean isAuthenticationOnly() {
        return authenticationOnly;
    }

    /**
     * @return Returns the default type.
     */
    public static UserDirectory defaultDirectory() {
        return Internal;
    }

}
