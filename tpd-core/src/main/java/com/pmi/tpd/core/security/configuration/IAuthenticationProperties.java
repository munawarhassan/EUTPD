package com.pmi.tpd.core.security.configuration;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IAuthenticationProperties {

    /**
     * Create {@link Builder builder}.
     *
     * @return Returns new instance of {@link Builder}.
     */
    public static LdapAuthenticationProperties defaultLdap() {
        final LdapAuthenticationProperties authenticationProperties = new LdapAuthenticationProperties();
        authenticationProperties.setGroupSchema(GroupLdapSchema.builder()
                .groupObjectClass("groupOfUniqueNames")
                .groupObjectFilter("(objectclass=groupOfUniqueNames)")
                .groupNameAttribute("cn")
                .groupDescriptionAttribute("description")
                .build());
        authenticationProperties.setUserSchema(UserLdapSchema.builder()
                .userObjectClass("inetorgperson")
                .userObjectFilter("(objectclass=inetorgperson)")
                .userNameAttribute("cn")
                .userNameRdnAttribute("cn")
                .userFirstNameAttribute("givenName")
                .userLastNameAttribute("sn")
                .userDisplayNameAttribute("displayName")
                .userEmailAttribute("mail")
                .userPasswordAttribute("userPassword")
                .userUniqueIDAttribute("entryUUID")
                .build());
        authenticationProperties.setMembershipSchema(
            MembershipLdapSchema.builder().groupMembersAttribute("member").userMembersAttribute("memberOf").build());
        return authenticationProperties;
    }

    /**
     * @return Returns new instance of {@link ActiveDirectoryBuilder}.
     */
    public static ActiveDirectoryAuthenticationProperties defaultActiveDirectory() {
        final ActiveDirectoryAuthenticationProperties authenticationProperties = new ActiveDirectoryAuthenticationProperties();
        authenticationProperties.setGroupSchema(GroupLdapSchema.builder()
                .groupObjectClass("group")
                .groupObjectFilter("(objectCategory=Group)")
                .groupNameAttribute("cn")
                .groupDescriptionAttribute("description")
                .build());
        authenticationProperties.setUserSchema(UserLdapSchema.builder()
                .userObjectClass("user")
                .userObjectFilter("(&(objectCategory=Person)(sAMAccountName={0}))")
                .userNameAttribute("sAMAccountName")
                .userNameRdnAttribute("cn")
                .userFirstNameAttribute("givenName")
                .userLastNameAttribute("sn")
                .userDisplayNameAttribute("displayName")
                .userEmailAttribute("mail")
                .userPasswordAttribute("unicodePwd")
                .userUniqueIDAttribute("objectGUID")
                .build());
        authenticationProperties.setMembershipSchema(MembershipLdapSchema.builder()
                .groupMembersAttribute("uniqueMember")
                .userMembersAttribute("memberOf")
                .build());
        return authenticationProperties;
    }

    public boolean empty();
}
