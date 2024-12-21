package com.pmi.tpd.core.security.provider.ldap;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.core.security.configuration.GroupLdapSchema;
import com.pmi.tpd.core.user.IGroup;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class LdapGroup implements IGroup {

    /** */
    private String name;

    /** */
    private String description;

    /**
     *
     */
    public LdapGroup() {
    }

    @Override
    public Long getId() {
        return null;
    }

    /**
     * @return Returns the name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            name of group
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Returns the description.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description of group.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("description", description).toString();
    }

    /**
     * @author Christophe Friederich
     * @since 2.0
     */
    public static class GroupContextMapper extends AbstractContextMapper<LdapGroup> {

        /** */
        private final GroupLdapSchema schema;

        /**
         * @param schema
         *            the group LDAP schema
         */
        public GroupContextMapper(final GroupLdapSchema schema) {
            this.schema = schema;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LdapGroup doMapFromContext(final DirContextOperations context) {
            final LdapGroup group = new LdapGroup();
            group.setName(context.getStringAttribute(schema.getGroupNameAttribute()));
            group.setDescription(context.getStringAttribute(schema.getGroupDescriptionAttribute()));
            return group;
        }
    }

}
