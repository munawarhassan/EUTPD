package com.pmi.tpd.core.security.provider.ldap;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.elasticsearch.common.Strings;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.IUserVisitor;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.GroupLdapSchema;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.MembershipLdapSchema;
import com.pmi.tpd.core.security.configuration.UserLdapSchema;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class LdapUser implements IUser {

    /** */
    private String username;

    /** */
    private String displayName;

    /** */
    private String emailAddress;

    /** */
    private String password;

    /** */
    private String uid;

    /** */
    private UserDirectory directory;

    /** */
    private Set<String> memberOfList = Collections.emptySet();

    /**
     *
     */
    public LdapUser() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public String getSlug() {
        return getUsername();
    }

    /**
     * @return Returns the LDAP identifier of user.
     */
    public String getUId() {
        return uid;
    }

    /**
     * @param uid
     *            the LDAP identifier of user.
     */
    public void setUid(final String uid) {
        this.uid = uid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T accept(final IUserVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActivated() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDirectory getDirectory() {
        return directory;
    }

    /**
     * @param directory
     *            the user directory.
     */
    public void setDirectory(final UserDirectory directory) {
        this.directory = directory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username
     */
    public void setLogin(final String username) {
        this.username = username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the full name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        return emailAddress;
    }

    /**
     * @param emailAddress
     *            the email address.
     */

    public void setEmail(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return Returns the list of member of.
     */
    public Set<String> getMemberOf() {
        return memberOfList;
    }

    /**
     * @param memberOfList
     *            set member of.
     */
    public void setMemberOf(final Set<String> memberOfList) {
        this.memberOfList = memberOfList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uid", uid)
                .add("username", username)
                .add("displayName", displayName)
                .add("displayName", displayName)
                .add("emailAddress", emailAddress)
                .add("memberOf", memberOfList)
                .toString();
    }

    /**
     * @author Christophe Friederich
     * @since 2.0
     */
    public static class UserContextMapper extends AbstractContextMapper<LdapUser>
            implements AuthenticatedLdapEntryContextMapper<LdapUser> {

        /** */
        private final LdapAuthenticationProperties schema;

        /** */
        private final UserDirectory userDirectory;

        /**
         * @param schema
         *            the LDAP schema
         * @param userDirectory
         *            user directory.
         */
        public UserContextMapper(final LdapAuthenticationProperties schema, final UserDirectory userDirectory) {
            this.schema = schema;
            this.userDirectory = userDirectory;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LdapUser doMapFromContext(final DirContextOperations context) {
            final MembershipLdapSchema membershipSchema = schema.getMembershipSchema();
            final UserLdapSchema userLdapSchema = schema.getUserSchema();
            final GroupLdapSchema groupLdapSchema = schema.getGroupSchema();
            final LdapUser user = new LdapUser();
            user.setDirectory(userDirectory);
            user.setUid(getGUID((byte[]) context.getObjectAttribute(userLdapSchema.getUserUniqueIDAttribute())));
            user.setLogin(context.getStringAttribute(userLdapSchema.getUserNameAttribute()));
            user.setDisplayName(context.getStringAttribute(userLdapSchema.getUserDisplayNameAttribute()));
            user.setEmail(context.getStringAttribute(userLdapSchema.getUserEmailAttribute()));
            final Object password = context.getObjectAttribute(userLdapSchema.getUserPasswordAttribute());
            if (password != null) {
                user.setPassword(password.toString());
            }
            if (Strings.isNullOrEmpty(user.getPassword())) {
                user.setPassword("NO_PASSWORD");
            }
            final Set<String> memberOf = Sets.newHashSet();
            try {
                for (final Enumeration<?> vals = context.getAttributes()
                        .get(membershipSchema.getUserMembersAttribute())
                        .getAll(); vals.hasMoreElements();) {
                    final LdapName name = new LdapName((String) vals.nextElement());
                    memberOf.add(getStringValue(name, groupLdapSchema.getGroupNameAttribute()));
                }
            } catch (final Exception ex) {

            }

            user.setMemberOf(memberOf);

            return user;
        }

        @Override
        public LdapUser mapWithContext(final DirContext ctx, final LdapEntryIdentification ldapEntryIdentification) {
            try {
                return doMapFromContext((DirContextOperations) ctx.lookup(ldapEntryIdentification.getRelativeName()));
            } catch (final NamingException e) {
                // rethrow, because we aren't allowed to throw checked exceptions.
                throw LdapUtils.convertLdapException(e);
            }
        }

    }

    private static String getGUID(final byte[] objectGUID) {
        if (objectGUID == null) {
            return null;
        }
        final StringBuilder displayStr = new StringBuilder();

        displayStr.append(prefixZeros(objectGUID[3] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[2] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[1] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[0] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros(objectGUID[5] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[4] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros(objectGUID[7] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[6] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros(objectGUID[8] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[9] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros(objectGUID[10] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[11] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[12] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[13] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[14] & 0xFF));
        displayStr.append(prefixZeros(objectGUID[15] & 0xFF));

        return displayStr.toString();
    }

    private static String getStringValue(final LdapName name, final String key) {
        final NamingEnumeration<? extends Attribute> allAttributes = getRdn(name, key).toAttributes().getAll();
        while (allAttributes.hasMoreElements()) {
            final Attribute oneAttribute = allAttributes.nextElement();
            if (key.equalsIgnoreCase(oneAttribute.getID())) {
                try {
                    return (String) oneAttribute.get();
                } catch (final javax.naming.NamingException e) {
                    throw LdapUtils.convertLdapException(e);
                }
            }
        }

        // This really shouldn't happen
        throw new NoSuchElementException("No Rdn with the requested key: '" + key + "'");
    }

    private static Rdn getRdn(final LdapName name, final String key) {
        Assert.notNull(name, "name must not be null");
        Assert.hasText(key, "key must not be blank");

        final List<Rdn> rdns = Lists.reverse(name.getRdns());
        for (final Rdn rdn : rdns) {
            final NamingEnumeration<String> ids = rdn.toAttributes().getIDs();
            while (ids.hasMoreElements()) {
                final String id = ids.nextElement();
                if (key.equalsIgnoreCase(id)) {
                    return rdn;
                }
            }
        }

        throw new NoSuchElementException("No Rdn with the requested key: '" + key + "'");
    }

    private static String prefixZeros(final int value) {
        if (value <= 0xF) {
            final StringBuilder sb = new StringBuilder("0");
            sb.append(Integer.toHexString(value));

            return sb.toString();

        } else {
            return Integer.toHexString(value);
        }
    }

}
