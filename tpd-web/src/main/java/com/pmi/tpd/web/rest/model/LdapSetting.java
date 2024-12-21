package com.pmi.tpd.web.rest.model;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.ActiveDirectoryAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.GroupLdapSchema;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.LdapSchema;
import com.pmi.tpd.core.security.configuration.MembershipLdapSchema;
import com.pmi.tpd.core.security.configuration.SecurityProperties;
import com.pmi.tpd.core.security.configuration.UserLdapSchema;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 */
@Getter
@Jacksonized
@Builder(toBuilder = true)
public class LdapSetting {

    /** */
    private final String name;

    /** */
    private final UserDirectory directoryType;

    /** */
    private final boolean authenticationOnly;

    /** */
    private final String domain;

    /** */
    private final String hostname;

    /** */
    private final Integer port;

    /** */
    private final String username;

    /** */
    private final String password;

    /**
     * Ldap schema
     */
    /**  */
    private final String baseDn;

    /**  */
    private final String additionalUserDn;

    /**  */
    private final String additionalGroupDn;

    /**
     * User Schema
     */

    /** The LDAP user object class type to use when loading users. */
    private final String userObjectClass;

    /** The filter to use when searching user objects. */
    private final String userObjectFilter;

    /** The attribute field to use on the user object. Examples: cn, sAMAccountName. */
    private final String userNameAttribute;

    /** The RDN to use when loading the user username.Example: cn. */
    private final String userNameRdnAttribute;

    /** The attribute field to use when loading the user first name. */
    private final String userFirstNameAttribute;

    /** The attribute field to use when loading the user last name. */
    private final String userLastNameAttribute;

    /** The attribute field to use when loading the user full name. */
    private final String userDisplayNameAttribute;

    /** The attribute field to use when loading the user email. */
    private final String userEmailAttribute;

    /** The attribute field to use when manipulating a user password. */
    private final String userPasswordAttribute;

    /** The attribute field to use for tracking user identity across user renames. */
    private final String userUniqueIDAttribute;

    /**
     * Group schema
     */

    /** LDAP attribute objectClass value to search for when loading groups. */
    private final String groupObjectClass;

    /** The filter to use when searching group objects. */
    private final String groupObjectFilter;

    /** The attribute field to use when loading the group name. */
    private final String groupNameAttribute;

    /** The attribute field to use when loading the group description. */
    private final String groupDescriptionAttribute;

    /**
     * Membership schema
     */

    /** The attribute field to use when loading the group members from the group. */
    private final String groupMembersAttribute;

    /** The attribute field to use when loading a user's groups. */
    private final String userMembersAttribute;

    /**
     * @param props
     *            a application properties
     * @return Returns new initialized instance of {@link LdapSetting}.
     */
    @Nonnull
    public static LdapSetting create(@Nonnull final IApplicationProperties applicationProperties) {

        final SecurityProperties properties = applicationProperties.getConfiguration(SecurityProperties.class);

        final LdapSettingBuilder ldap = LdapSetting.builder();

        properties.applyDefaultValue();

        final LdapAuthenticationProperties ldapProperties = properties.currentAuthenticationConfiguration()
                .orElse(null);

        if (ldapProperties == null) {
            return ldap.build();
        }

        ldap.name = ldapProperties.getName();
        ldap.authenticationOnly = ldapProperties.isAuthenticationOnly();
        ldap.directoryType = ldapProperties.getDirectoryType();
        ldap.hostname = ldapProperties.getHostname();
        ldap.port = ldapProperties.getPort();
        ldap.username = ldapProperties.getUsername();
        ldap.password = ldapProperties.getPassword();
        if (ldapProperties.getLdapSchema() != null) {
            ldap.baseDn = ldapProperties.getLdapSchema().getBaseDn();
            ldap.additionalUserDn = ldapProperties.getLdapSchema().getAdditionalUserDn();
            ldap.additionalGroupDn = ldapProperties.getLdapSchema().getAdditionalGroupDn();
        }

        /* user schema */
        final UserLdapSchema userSchema = ldapProperties.getUserSchema();
        ldap.userObjectClass = userSchema.getUserObjectClass();
        ldap.userObjectFilter = userSchema.getUserObjectFilter();
        ldap.userNameAttribute = userSchema.getUserNameAttribute();
        ldap.userNameRdnAttribute = userSchema.getUserNameRdnAttribute();
        ldap.userFirstNameAttribute = userSchema.getUserFirstNameAttribute();
        ldap.userLastNameAttribute = userSchema.getUserLastNameAttribute();
        ldap.userDisplayNameAttribute = userSchema.getUserDisplayNameAttribute();
        ldap.userEmailAttribute = userSchema.getUserEmailAttribute();
        ldap.userPasswordAttribute = userSchema.getUserPasswordAttribute();
        ldap.userUniqueIDAttribute = userSchema.getUserUniqueIDAttribute();

        /* group schema */
        final GroupLdapSchema groupSchema = ldapProperties.getGroupSchema();
        ldap.groupObjectClass = groupSchema.getGroupObjectClass();
        ldap.groupObjectFilter = groupSchema.getGroupObjectFilter();
        ldap.groupNameAttribute = groupSchema.getGroupNameAttribute();
        ldap.groupDescriptionAttribute = groupSchema.getGroupDescriptionAttribute();

        /* membership schema */
        final MembershipLdapSchema membershipSchema = ldapProperties.getMembershipSchema();
        ldap.groupMembersAttribute = membershipSchema.getGroupMembersAttribute();
        ldap.userMembersAttribute = membershipSchema.getUserMembersAttribute();
        if (properties.getActiveDirectory() != null) {
            final ActiveDirectoryAuthenticationProperties adProperties = properties.getActiveDirectory();
            ldap.domain = adProperties.getDomain();
        }

        return ldap.build();
    }

    /**
     * @param applicationProperties
     *            application Properties.
     */
    public void save(final IApplicationProperties applicationProperties) {
        final SecurityProperties securityProperties = toSecurityConfiguration(applicationProperties);

        applicationProperties.removeConfiguration(securityProperties.getClass());
        applicationProperties.storeConfiguration(securityProperties);
    }

    public SecurityProperties toSecurityConfiguration(final IApplicationProperties applicationProperties) {
        final SecurityProperties securityProperties = applicationProperties.getConfiguration(SecurityProperties.class);

        LdapAuthenticationProperties ldapProperties = null;
        if (UserDirectory.ActiveDirectory.equals(this.directoryType)
                || UserDirectory.InternalActiveDirectory.equals(this.directoryType)) {
            ldapProperties = new ActiveDirectoryAuthenticationProperties();
            securityProperties.setActiveDirectory((ActiveDirectoryAuthenticationProperties) ldapProperties);
        } else if (UserDirectory.Ldap.equals(this.directoryType)
                || UserDirectory.InternalLdap.equals(this.directoryType)) {
            ldapProperties = new LdapAuthenticationProperties();
            securityProperties.setLdap(ldapProperties);

        } else {
            throw new RuntimeException("unknow user directory type");
        }

        ldapProperties.setName(name);
        ldapProperties.setAuthenticationOnly(authenticationOnly);
        ldapProperties.setHostname(hostname);
        ldapProperties.setPort(port);
        ldapProperties.setUsername(username);
        ldapProperties.setPassword(password);

        final LdapSchema ldapSchema = LdapSchema.builder()
                .additionalGroupDn(additionalGroupDn)
                .additionalUserDn(additionalUserDn)
                .baseDn(baseDn)
                .build();
        ldapProperties.setLdapSchema(ldapSchema);

        final UserLdapSchema userLdapSchema = UserLdapSchema.builder()
                .userDisplayNameAttribute(userDisplayNameAttribute)
                .userEmailAttribute(userEmailAttribute)
                .userFirstNameAttribute(userFirstNameAttribute)
                .userLastNameAttribute(userLastNameAttribute)
                .userNameAttribute(userNameAttribute)
                .userNameRdnAttribute(userNameRdnAttribute)
                .userObjectClass(userObjectClass)
                .userObjectFilter(userObjectFilter)
                .userPasswordAttribute(userPasswordAttribute)
                .userUniqueIDAttribute(userUniqueIDAttribute)
                .build();
        ldapProperties.setUserSchema(userLdapSchema);

        final GroupLdapSchema groupLdapSchema = GroupLdapSchema.builder()
                .groupDescriptionAttribute(groupDescriptionAttribute)
                .groupNameAttribute(groupNameAttribute)
                .groupObjectClass(groupObjectClass)
                .groupObjectFilter(groupObjectFilter)
                .build();
        ldapProperties.setGroupSchema(groupLdapSchema);

        final MembershipLdapSchema membershipLdapSchema = MembershipLdapSchema.builder()
                .groupMembersAttribute(groupMembersAttribute)
                .userMembersAttribute(userMembersAttribute)
                .build();
        ldapProperties.setMembershipSchema(membershipLdapSchema);

        if (ldapProperties instanceof ActiveDirectoryAuthenticationProperties) {
            final ActiveDirectoryAuthenticationProperties adProperties = (ActiveDirectoryAuthenticationProperties) ldapProperties;
            adProperties.setDomain(domain);
        }
        securityProperties.applyDefaultValue();
        return securityProperties;
    }

}
