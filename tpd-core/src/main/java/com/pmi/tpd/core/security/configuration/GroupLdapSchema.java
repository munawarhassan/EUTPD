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
public class GroupLdapSchema {

    /** LDAP attribute objectClass value to search for when loading groups. */
    private String groupObjectClass;

    /** The filter to use when searching group objects. */
    private String groupObjectFilter;

    /** The attribute field to use when loading the group name. */
    private String groupNameAttribute;

    /** The attribute field to use when loading the group description. */
    private String groupDescriptionAttribute;

    public void override(final GroupLdapSchema groupSchema) {
        if (groupObjectClass == null) {
            groupObjectClass = groupSchema.groupObjectClass;
        }
        if (groupObjectFilter == null) {
            groupObjectFilter = groupSchema.groupObjectFilter;
        }
        if (groupNameAttribute == null) {
            groupNameAttribute = groupSchema.groupNameAttribute;
        }
        if (groupDescriptionAttribute == null) {
            groupDescriptionAttribute = groupSchema.groupDescriptionAttribute;
        }
    }

}
