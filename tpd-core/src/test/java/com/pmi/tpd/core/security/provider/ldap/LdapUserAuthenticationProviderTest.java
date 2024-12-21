package com.pmi.tpd.core.security.provider.ldap;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;

import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.IAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.LdapSchema;
import com.pmi.tpd.core.security.configuration.MembershipLdapSchema;
import com.pmi.tpd.core.user.IGroup;

@Configuration
@ContextConfiguration(classes = { LdapUserAuthenticationProviderTest.class })
public class LdapUserAuthenticationProviderTest extends AbstractLdapIntegration {

    LdapAuthenticationProperties config;

    @BeforeEach
    public void setUp() throws Exception {
        config = IAuthenticationProperties.defaultLdap();
        config.setLdapSchema(LdapSchema.builder()
                .baseDn("dc=company,dc=com")
                .additionalUserDn("(objectClass=user)")
                .additionalGroupDn("(cn=group app*)")
                .build());
        config.setMembershipSchema(MembershipLdapSchema.builder()
                // ApacheDs not recognized memberOf
                .userMembersAttribute("member")
                .groupMembersAttribute("uniqueMember")
                .build());

    }

    @Test
    public void shouldFindUser() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final LdapUser user = (LdapUser) provider.findUserByName("joe");
        assertNotNull(user);
        assertEquals("joe", user.getUsername());
        assertEquals(UserDirectory.Ldap, user.getDirectory());
        assertEquals("Joe Smeth", user.getDisplayName());
        assertEquals("joe.smeth@company.com", user.getEmail());

        assertEquals(1, user.getMemberOf().size());
    }

    @Test
    public void shouldFindUserWithMemberOf() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final LdapUser user = (LdapUser) provider.findUserByName("ben");
        assertNotNull(user);
        assertEquals("ben", user.getUsername());
        assertEquals(UserDirectory.Ldap, user.getDirectory());
        assertEquals("Ben Alex", user.getDisplayName());
        assertEquals("ben.alex@company.com", user.getEmail());

        assertEquals(2, user.getMemberOf().size());
        assertThat(user.getMemberOf(), Matchers.containsInAnyOrder("group app developers", "group app administrators"));
    }

    @Test
    public void shouldFindUsersByUsername() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        // with null username
        {
            final Page<IUser> users = provider.findUsersByName(null, PageUtils.newRequest(0, 10));
            // bob is exclude selection because belong to objectclass=organizationalPerson instead
            // objectclass=inetOrgPerson
            assertEquals(2, users.getNumberOfElements());
        }
        {
            // with empty username
            final Page<IUser> users = provider.findUsersByName("", PageUtils.newRequest(0, 10));
            // bob is exclude selection because belong to objectclass=organizationalPerson instead
            // objectclass=inetOrgPerson
            assertEquals(2, users.getNumberOfElements());
        }

        {
            // with specific username
            final Page<IUser> users = provider.findUsersByName("joe", PageUtils.newRequest(0, 10));
            assertEquals(1, users.getNumberOfElements());
        }
    }

    @Test
    public void shouldNotFindUser() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final IUser user = provider.findUserByName("noexist");
        assertNull(user);
    }

    @Test
    public void shouldFindGroup() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final IGroup group = provider.findGroupByName("group app developers");
        assertNotNull(group);
    }

    @Test
    public void shouldFindGroups() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();
        {
            final Page<String> groups = provider.findGroupsByName(null, PageUtils.newRequest(0, 10));
            assertEquals(3, groups.getNumberOfElements());
            assertThat(groups,
                Matchers.containsInAnyOrder("group app users", "group app developers", "group app administrators"));
        }
        {
            final Page<String> groups = provider.findGroupsByName("", PageUtils.newRequest(0, 10));
            assertEquals(3, groups.getNumberOfElements());
        }
        {
            final Page<String> groups = provider.findGroupsByName("dev", PageUtils.newRequest(0, 10));
            assertEquals(1, groups.getNumberOfElements());
        }
    }

    @Test
    public void shouldNotFindGroup() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final IGroup group = provider.findGroupByName("agroupshit");
        assertNull(group);
    }

    @Test()
    public void loadUser() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final IUser user = provider.loadUser("joe");
        assertNotNull(user);
    }

    @Test
    public void loadUserDoesntExist() throws Exception {
        assertThrows(UsernameNotFoundException.class, () -> {
            final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
            provider.init();

            provider.loadUser("toto");
        });
    }

    @Test
    public void authenticate() throws Exception {
        final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
        provider.init();

        final Authentication auth = provider
                .authenticate(new UsernamePasswordAuthenticationToken("joe", "joespassword"));

        assertNotNull(auth);
        assertEquals(true, auth.isAuthenticated());
        assertThat(auth, instanceOf(UsernamePasswordAuthenticationToken.class));

        assertEquals("joe", auth.getName());
        assertNotNull(auth.getCredentials());
        assertThat(auth.getPrincipal(), instanceOf(LdapUser.class));
        assertThat(
            auth.getAuthorities().stream().map((authority) -> authority.getAuthority()).collect(Collectors.toList()),
            Matchers.containsInAnyOrder("group app users"));
    }

    @Test
    public void authenticateFailedWithBadPassword() throws Exception {
        assertThrows(InternalAuthenticationServiceException.class, () -> {
            final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
            provider.init();

            provider.authenticate(new UsernamePasswordAuthenticationToken("joe", "badpassword"));
        });
    }

    @Test
    public void authenticateFailedWithUnknownUser() throws Exception {
        assertThrows(BadCredentialsException.class, () -> {
            final LdapUserAuthenticationProvider<LdapAuthenticationProperties> provider = create(config);
            provider.init();

            provider.authenticate(new UsernamePasswordAuthenticationToken("toto", "totopassword"));
        });
    }

    private LdapUserAuthenticationProvider<LdapAuthenticationProperties> create(
        final LdapAuthenticationProperties config) {
        return new LdapUserAuthenticationProvider<>(config) {

            @Override
            protected ContextSource buildContextSource() {
                return LdapUserAuthenticationProviderTest.getContextSource();
            }
        };
    }
}
