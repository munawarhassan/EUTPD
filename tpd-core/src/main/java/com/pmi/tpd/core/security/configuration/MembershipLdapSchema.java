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
public class MembershipLdapSchema {

    /** The attribute field to use when loading the group members from the group. */
    private String groupMembersAttribute;

    /** The attribute field to use when loading a user's groups. */
    private String userMembersAttribute;

    public void override(final MembershipLdapSchema membershipSchema) {
        if (groupMembersAttribute == null) {
            groupMembersAttribute = membershipSchema.groupMembersAttribute;
        }
        if (userMembersAttribute == null) {
            userMembersAttribute = membershipSchema.userMembersAttribute;
        }
    }

}
