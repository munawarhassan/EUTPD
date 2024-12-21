package com.pmi.tpd.core.security.provider.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.DirContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;

/**
 * Specialized LDAP authentication provider which uses Active Directory configuration conventions.
 * <p>
 * It will authenticate using the Active Directory
 * <a href="http://msdn.microsoft.com/en-us/library/ms680857%28VS.85%29.aspx"> {@code userPrincipalName}</a> or a custom
 * {@link #setSearchFilter(String) searchFilter} in the form {@code username@domain}. If the username does not already
 * end with the domain name, the {@code userPrincipalName} will be built by appending the configured domain name to the
 * username supplied in the authentication request. If no domain name is configured, it is assumed that the username
 * will always contain the domain name.
 * <p>
 * The user authorities are obtained from the data contained in the {@code memberOf} attribute.
 * <h3>Active Directory Sub-Error Codes</h3> When an authentication fails, resulting in a standard LDAP 49 error code,
 * Active Directory also supplies its own sub-error codes within the error message. These will be used to provide
 * additional log information on why an authentication has failed. Typical examples are
 * <ul>
 * <li>525 - user not found</li>
 * <li>52e - invalid credentials</li>
 * <li>530 - not permitted to logon at this time</li>
 * <li>532 - password expired</li>
 * <li>533 - account disabled</li>
 * <li>701 - account expired</li>
 * <li>773 - user must reset password</li>
 * <li>775 - account locked</li>
 * </ul>
 * If you set the {@link #setConvertSubErrorCodesToExceptions(boolean) convertSubErrorCodesToExceptions} property to
 * {@code true}, the codes will also be used to control the exception raised.
 *
 * @author Luke Taylor
 * @author Rob Winch
 * @since 3.1
 */
// CHECKSTYLE:OFF
public final class ActiveDirectoryLdapAuthenticationProvider extends AbstractLdapAuthenticationProvider {

    private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");

    // Error codes
    private static final int USERNAME_NOT_FOUND = 0x525;

    private static final int INVALID_PASSWORD = 0x52e;

    private static final int NOT_PERMITTED = 0x530;

    private static final int PASSWORD_EXPIRED = 0x532;

    private static final int ACCOUNT_DISABLED = 0x533;

    private static final int ACCOUNT_EXPIRED = 0x701;

    private static final int PASSWORD_NEEDS_RESET = 0x773;

    private static final int ACCOUNT_LOCKED = 0x775;

    private final String domain;

    private final String rootDn;

    private final String url;

    private final int connectTimeout = 5000;

    private final int readTimeout = 30000;

    private boolean convertSubErrorCodesToExceptions;

    private String searchFilter = "(&(objectClass=user)(userPrincipalName={0}))";

    // Only used to allow tests to substitute a mock LdapContext
    ContextFactory contextFactory = new ContextFactory();

    /**
     * @param domain
     *            the domain name (may be null or empty)
     * @param url
     *            an LDAP url (or multiple URLs)
     * @param rootDn
     *            the root DN (may be null or empty)
     */
    public ActiveDirectoryLdapAuthenticationProvider(final String domain, final String url, final String rootDn) {
        Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
        this.domain = StringUtils.hasText(domain) ? domain.toLowerCase() : null;
        this.url = url;
        this.rootDn = StringUtils.hasText(rootDn) ? rootDn.toLowerCase() : null;
    }

    /**
     * @param domain
     *            the domain name (may be null or empty)
     * @param url
     *            an LDAP url (or multiple URLs)
     */
    public ActiveDirectoryLdapAuthenticationProvider(final String domain, final String url) {
        Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
        this.domain = StringUtils.hasText(domain) ? domain.toLowerCase() : null;
        this.url = url;
        rootDn = this.domain == null ? null : rootDnFromDomain(this.domain);
    }

    @Override
    protected DirContextOperations doAuthentication(final UsernamePasswordAuthenticationToken auth) {
        final String username = auth.getName();
        final String password = (String) auth.getCredentials();

        final DirContext ctx = createContext(username, password);

        try {
            return searchForUser(ctx, username);
        } catch (final NamingException e) {
            logger.error("Failed to locate directory entry for authenticated user: " + username, e);
            throw badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    /**
     * Creates the user authority list from the values of the {@code memberOf} attribute obtained from the user's Active
     * Directory entry.
     */
    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(final DirContextOperations userData,
        final String username,
        final String password) {
        final String[] groups = userData.getStringAttributes("memberOf");

        if (groups == null) {
            logger.debug("No values for 'memberOf' attribute.");

            return AuthorityUtils.NO_AUTHORITIES;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("'memberOf' attribute values: " + Arrays.asList(groups));
        }

        final ArrayList<GrantedAuthority> authorities = new ArrayList<>(groups.length);

        for (final String group : groups) {
            final LdapName distinguishedName = LdapUtils.newLdapName(group);
            final List<Rdn> rdns = distinguishedName.getRdns();
            authorities.add(new SimpleGrantedAuthority(LdapUtils.getStringValue(distinguishedName, rdns.size() - 1)));
        }

        return authorities;
    }

    ContextSource createSourceContext(final String username, final String password) {
        final String bindPrincipal = createBindPrincipal(username);
        final DirContextSource contextSource = new DirContextSource();
        contextSource.setUrl(url);
        contextSource.setUserDn(bindPrincipal);
        contextSource.setPassword(password);
        contextSource.setBaseEnvironmentProperties(createEnvironnementProperties(username, password));
        contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    DirContext createContext(final String username, final String password) {
        try {
            return contextFactory.createContext(createEnvironnementProperties(username, password));
        } catch (final NamingException e) {
            if (e instanceof AuthenticationException || e instanceof OperationNotSupportedException) {
                final String bindPrincipal = createBindPrincipal(username);
                handleBindException(bindPrincipal, e);
                throw badCredentials(e);
            } else {
                throw LdapUtils.convertLdapException(e);
            }
        }
    }

    private Hashtable<String, Object> createEnvironnementProperties(final String username, final String password) {
        // TODO. add DNS lookup based on domain
        final String bindUrl = url;

        final Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        final String bindPrincipal = createBindPrincipal(username);
        if (bindPrincipal != null) {
            env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
        }
        env.put(Context.PROVIDER_URL, bindUrl);
        if (password != null) {
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());
        env.put("java.naming.ldap.attributes.binary", "objectGUID");

        // Specify timeout connection to be 5 seconds
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(this.connectTimeout));
        // Specify timeout to be 5 seconds
        // Specify read timeout to be 30 seconds
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(this.readTimeout));
        return env;
    }

    private void handleBindException(final String bindPrincipal, final NamingException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication for " + bindPrincipal + " failed:" + exception);
        }

        final int subErrorCode = parseSubErrorCode(exception.getMessage());

        if (subErrorCode <= 0) {
            logger.debug("Failed to locate AD-specific sub-error code in message");
            return;
        }

        logger.info("Active Directory authentication failed: " + subCodeToLogMessage(subErrorCode));

        if (convertSubErrorCodesToExceptions) {
            raiseExceptionForErrorCode(subErrorCode, exception);
        }
    }

    private int parseSubErrorCode(final String message) {
        final Matcher m = SUB_ERROR_CODE.matcher(message);

        if (m.matches()) {
            return Integer.parseInt(m.group(1), 16);
        }

        return -1;
    }

    private void raiseExceptionForErrorCode(final int code, final NamingException exception) {
        final String hexString = Integer.toHexString(code);
        final Throwable cause = new ActiveDirectoryAuthenticationException(hexString, exception.getMessage(),
                exception);
        switch (code) {
            case PASSWORD_EXPIRED:
                throw new CredentialsExpiredException(messages
                        .getMessage("LdapAuthenticationProvider.credentialsExpired", "User credentials have expired"),
                        cause);
            case ACCOUNT_DISABLED:
                throw new DisabledException(
                        messages.getMessage("LdapAuthenticationProvider.disabled", "User is disabled"), cause);
            case ACCOUNT_EXPIRED:
                throw new AccountExpiredException(
                        messages.getMessage("LdapAuthenticationProvider.expired", "User account has expired"), cause);
            case ACCOUNT_LOCKED:
                throw new LockedException(
                        messages.getMessage("LdapAuthenticationProvider.locked", "User account is locked"), cause);
            default:
                throw badCredentials(cause);
        }
    }

    private String subCodeToLogMessage(final int code) {
        switch (code) {
            case USERNAME_NOT_FOUND:
                return "User was not found in directory";
            case INVALID_PASSWORD:
                return "Supplied password was invalid";
            case NOT_PERMITTED:
                return "User not permitted to logon at this time";
            case PASSWORD_EXPIRED:
                return "Password has expired";
            case ACCOUNT_DISABLED:
                return "Account is disabled";
            case ACCOUNT_EXPIRED:
                return "Account expired";
            case PASSWORD_NEEDS_RESET:
                return "User must reset password";
            case ACCOUNT_LOCKED:
                return "Account locked";
        }

        return "Unknown (error code " + Integer.toHexString(code) + ")";
    }

    private BadCredentialsException badCredentials() {
        return new BadCredentialsException(
                messages.getMessage("LdapAuthenticationProvider.badCredentials", "Bad credentials"));
    }

    private BadCredentialsException badCredentials(final Throwable cause) {
        return (BadCredentialsException) badCredentials().initCause(cause);
    }

    private DirContextOperations searchForUser(final DirContext context, final String username) throws NamingException {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        final String bindPrincipal = createBindPrincipal(username);
        final String searchRoot = rootDn != null ? rootDn : searchRootFromPrincipal(bindPrincipal);

        try {
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(context,
                searchControls,
                searchRoot,
                searchFilter,
                new Object[] { bindPrincipal, username });
        } catch (final IncorrectResultSizeDataAccessException incorrectResults) {
            // Search should never return multiple results if properly configured - just
            // rethrow
            if (incorrectResults.getActualSize() != 0) {
                throw incorrectResults;
            }
            // If we found no results, then the username/password did not match
            final UsernameNotFoundException userNameNotFoundException = new UsernameNotFoundException(
                    "User " + username + " not found in directory.", incorrectResults);
            throw badCredentials(userNameNotFoundException);
        }
    }

    private String searchRootFromPrincipal(final String bindPrincipal) {
        final int atChar = bindPrincipal.lastIndexOf('@');

        if (atChar < 0) {
            logger.debug("User principal '" + bindPrincipal
                    + "' does not contain the domain, and no domain has been configured");
            throw badCredentials();
        }

        return rootDnFromDomain(bindPrincipal.substring(atChar + 1, bindPrincipal.length()));
    }

    private String rootDnFromDomain(final String domain) {
        final String[] tokens = StringUtils.tokenizeToStringArray(domain, ".");
        final StringBuilder root = new StringBuilder();

        for (final String token : tokens) {
            if (root.length() > 0) {
                root.append(',');
            }
            root.append("dc=").append(token);
        }

        return root.toString();
    }

    String createBindPrincipal(final String username) {
        if (Strings.isNullOrEmpty(username)) {
            return username;
        }
        if (domain == null || username.toLowerCase().endsWith(domain)) {
            return username;
        }

        return username + "@" + domain;
    }

    /**
     * By default, a failed authentication (LDAP error 49) will result in a {@code BadCredentialsException}.
     * <p>
     * If this property is set to {@code true}, the exception message from a failed bind attempt will be parsed for the
     * AD-specific error code and a {@link CredentialsExpiredException}, {@link DisabledException},
     * {@link AccountExpiredException} or {@link LockedException} will be thrown for the corresponding codes. All other
     * codes will result in the default {@code BadCredentialsException}.
     *
     * @param convertSubErrorCodesToExceptions
     *            {@code true} to raise an exception based on the AD error code.
     */
    public void setConvertSubErrorCodesToExceptions(final boolean convertSubErrorCodesToExceptions) {
        this.convertSubErrorCodesToExceptions = convertSubErrorCodesToExceptions;
    }

    /**
     * The LDAP filter string to search for the user being authenticated. Occurrences of {0} are replaced with the
     * {@code username@domain}. Occurrences of {1} are replaced with the {@code username} only.
     * <p>
     * Defaults to: {@code (&(objectClass=user)(userPrincipalName= 0}))}
     * </p>
     *
     * @param searchFilter
     *            the filter string
     * @since 3.2.6
     */
    public void setSearchFilter(final String searchFilter) {
        Assert.hasText(searchFilter, "searchFilter must have text");
        this.searchFilter = searchFilter;
    }

    static class ContextFactory {

        DirContext createContext(final Hashtable<?, ?> env) throws NamingException {
            return new InitialLdapContext(env, null);
        }
    }
}
// CHECKSTYLE:ON
