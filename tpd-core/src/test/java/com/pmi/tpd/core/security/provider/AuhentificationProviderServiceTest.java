package com.pmi.tpd.core.security.provider;

import java.util.Collections;
import java.util.Optional;

import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.ContextNotEmptyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.exception.NoSecurityConfigurationException;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.security.IAuthenticationSynchroniser;
import com.pmi.tpd.core.security.configuration.IAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.SecurityProperties;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.spring.UserAuthenticationToken;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.util.Providers;

public class AuhentificationProviderServiceTest extends MockitoTestCase {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IAuthenticationSynchroniser authenticationSynchroniser;

    @Mock
    private IPermissionService permissionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock(lenient = true)
    private ApplicationContext applicationContext;

    @Spy
    private final SecurityProperties config = new SecurityProperties();

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    private DefaultAuthentificationProviderService providerService;

    @BeforeEach
    public void setUp() throws Exception {
        providerService = spy(new DefaultAuthentificationProviderService(userRepository, groupRepository,
                Providers.of(permissionService), Providers.of(authenticationSynchroniser), passwordEncoder,
                applicationContext, config, i18nService));

        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(mock(AutowireCapableBeanFactory.class));

    }

    @Test
    public void shouldStartedForAuthentication() {
        assertThrows(BadCredentialsException.class, () -> {
            providerService.authenticate(new UsernamePasswordAuthenticationToken("user", "password"));
        });
    }

    @Test
    public void shouldGetAuthenticationProviderFailedWhenUserNotExists() {
        assertThrows(UsernameNotFoundException.class, () -> {
            final SecurityProperties config = new SecurityProperties();
            providerService.setSecurityConfiguration(config);

            providerService.configure();

            providerService.getAuthenticationProvider("test");
        });
    }

    @Test
    public void shouldconfigurateWithInternalOnly() {

        providerService.setSecurityConfiguration(new SecurityProperties());
        providerService.configure();

        assertEquals(1, providerService.getAuthenticationProviders().size(), "should have only one provider");

        final IAuthenticationProvider provider = providerService.getInternalAuthenticationProvider().orElse(null);
        assertNotNull(provider, "should create internal provider");
        assertEquals(true, provider.isInternal());
    }

    @Test
    public void shouldconfigurateWithLdapDirectory() {

        final IAuthenticationProvider ldapProvider = createLdapProvider();

        final SecurityProperties config = new SecurityProperties();
        config.setLdap(IAuthenticationProperties.defaultLdap());
        providerService.setSecurityConfiguration(config);
        providerService.configure();

        assertEquals(2, providerService.getAuthenticationProviders().size(), "should have 2 providers");

        assertThat(providerService.getAuthenticationProviders(), IsIterableContaining.hasItem(ldapProvider));
    }

    @Test
    public void shouldCreateSucccessAuthentication() {

        final IAuthenticationProvider internalProvider = createInternalProvider();
        final Authentication expectedAuth = mock(Authentication.class);

        doReturn(true).when(providerService).isStarted();

        when(internalProvider.authenticate(any())).thenReturn(expectedAuth);

        providerService.setSecurityConfiguration(new SecurityProperties());
        providerService.configure();
        final Authentication auth = providerService
                .authenticate(new UsernamePasswordAuthenticationToken("user", "password"));

        assertEquals(expectedAuth, auth);
    }

    @Test
    public void shouldCreateSucccessUserAuthentication() {
        final IUser user = User.builder().directory(UserDirectory.Ldap).build();

        final IAuthenticationProvider internalProvider = createLdapProvider();
        final Authentication expectedAuth = mock(Authentication.class);

        doReturn(true).when(providerService).isStarted();

        when(internalProvider.authenticate(any())).thenReturn(expectedAuth);

        final SecurityProperties config = new SecurityProperties();
        config.setLdap(IAuthenticationProperties.defaultLdap());

        providerService.setSecurityConfiguration(config);
        providerService.configure();

        final Authentication auth = providerService.authenticate(UserAuthenticationToken.forUser(user));

        assertNotEquals(expectedAuth, auth, "the authication should be surrogate");
    }

    /**
     * should authenticate a user that doesn't exist internally.
     * <p>
     * The synchronization should be executed wiht information of external provider.
     * </p>
     */
    @Test
    public void shouldAuthenticateNoExistUserAndSynghronizeWithExternalProvider() {

        final IAuthenticationProvider internalProvider = createInternalProvider();
        final IAuthenticationProvider ldapProvider = createLdapProvider();
        final Authentication actualAuth = new TestingAuthenticationToken("user", "password");
        final Authentication expectedAuth = new TestingAuthenticationToken("user", "password");

        doReturn(true).when(providerService).isStarted();

        when(internalProvider.authenticate(actualAuth)).thenReturn(expectedAuth);

        when(ldapProvider.authenticate(any())).thenReturn(actualAuth);

        final SecurityProperties config = new SecurityProperties();
        config.setLdap(IAuthenticationProperties.defaultLdap());
        providerService.setSecurityConfiguration(config);
        providerService.configure();
        final Authentication auth = providerService
                .authenticate(new UsernamePasswordAuthenticationToken("user", "password"));

        assertEquals(expectedAuth, auth);

        // execute synchronization for external provider without authentication delegation
        verify(this.authenticationSynchroniser).synchronise("user",
            DefaultAuthentificationProviderService.toString(actualAuth.getAuthorities()),
            ldapProvider);
    }

    @Test
    public void shouldFailedOnErrorSynghronizeWithUserNotAuthenticated() {
        assertThrows(BadCredentialsException.class, () -> {
            final IAuthenticationProvider internalProvider = createInternalProvider();
            final IAuthenticationProvider ldapProvider = createLdapProvider();
            final Authentication actualAuth = new TestingAuthenticationToken("user", "password");
            final Authentication expectedAuth = new TestingAuthenticationToken("user", "password");

            actualAuth.setAuthenticated(false);

            doReturn(true).when(providerService).isStarted();

            when(internalProvider.authenticate(actualAuth)).thenReturn(expectedAuth);

            when(ldapProvider.authenticate(any())).thenReturn(actualAuth);

            when(authenticationSynchroniser.synchronise(any(), any(), any())).thenThrow(ContextNotEmptyException.class);

            final SecurityProperties config = new SecurityProperties();
            config.setLdap(IAuthenticationProperties.defaultLdap());
            providerService.setSecurityConfiguration(config);
            providerService.configure();
            providerService.authenticate(new UsernamePasswordAuthenticationToken("user", "password"));
        });
    }

    @Test
    public void shouldStartWithExternalProvider() throws Exception {

        final SecurityProperties config = new SecurityProperties();
        config.setLdap(IAuthenticationProperties.defaultLdap());
        providerService.setSecurityConfiguration(config);

        providerService.start();

        assertEquals(true, providerService.isStarted());
        assertEquals(true, providerService.hasExternalProvider());

        providerService.shutdown();

        assertEquals(false, providerService.isStarted());
        assertEquals(false, providerService.hasExternalProvider());
    }

    @Test
    public void shoudRestartOnConfigurationChanged() throws Exception {
        final SecurityProperties config = new SecurityProperties();

        assertEquals(false, providerService.isStarted());
        providerService.onSecurityConfigurationChangedEvent(new ConfigurationChangedEvent<>(config));
        assertEquals(true, providerService.isStarted());
        verify(providerService).shutdown();
        verify(providerService).start();
    }

    @Test
    public void shouldloadUserDetailWithInternalProvider() {
        final UserEntity user = UserEntity.builder().directory(UserDirectory.Internal).build();

        final IAuthenticationProvider provider = createInternalProvider();
        final UserDetails expectedUserDetails = mock(UserDetails.class);

        doReturn(true).when(providerService).isStarted();

        when(userRepository.findByName("user")).thenReturn(user);

        when(provider.loadUserByUsername("user")).thenReturn(expectedUserDetails);

        providerService.setSecurityConfiguration(new SecurityProperties());
        providerService.configure();
        final UserDetails actualUserDetails = providerService.loadUserByUsername("user");

        assertEquals(expectedUserDetails, actualUserDetails);
        verify(provider).loadUserByUsername("user");

    }

    @Test
    public void shouldloadUserDetailWithExternalProvider() {
        final UserEntity user = UserEntity.builder()
                .username("user")
                .password("password")
                .directory(UserDirectory.Ldap)
                .build();

        final IAuthenticationProvider provider = createLdapProvider();
        final UserDetails actualUserDetails = mock(UserDetails.class);

        doReturn(true).when(providerService).isStarted();

        when(actualUserDetails.getUsername()).thenReturn("user");
        when(permissionService.getEffectivePermissions(user)).thenReturn(Collections.emptyList());

        when(userRepository.findByName("user")).thenReturn(user);

        when(provider.loadUserByUsername("user")).thenReturn(actualUserDetails);

        final SecurityProperties config = new SecurityProperties();
        config.setLdap(IAuthenticationProperties.defaultLdap());

        providerService.setSecurityConfiguration(config);
        providerService.configure();

        final UserDetails resultUserDetails = providerService.loadUserByUsername("user");

        assertNotEquals(actualUserDetails, resultUserDetails, "the authication should be surrogate");
        verify(provider).loadUserByUsername("user");
        verify(authenticationSynchroniser).synchronise("user", Collections.emptySet(), provider);
    }

    @Test
    public void shouldloadUserDetailWithDelegateAuthenticationProvider() {
        final UserEntity user = UserEntity.builder().directory(UserDirectory.InternalLdap).build();

        final IAuthenticationProvider provider = createInternalLdapProvider();
        final UserDetails expectedUserDetails = mock(UserDetails.class);

        doReturn(true).when(providerService).isStarted();

        when(userRepository.findByName("user")).thenReturn(user);

        when(provider.loadUserByUsername("user")).thenReturn(expectedUserDetails);

        providerService.setSecurityConfiguration(new SecurityProperties());
        providerService.configure();
        final UserDetails actualUserDetails = providerService.loadUserByUsername("user");

        assertEquals(expectedUserDetails, actualUserDetails);
        verify(provider).loadUserByUsername("user");
    }

    @Test
    public void testConnectionFailedMissingProvider() {

        providerService.setSecurityConfiguration(new SecurityProperties());
        providerService.configure();

        doReturn(Optional.empty()).when(providerService).createAuthenticationProvider(any(), any());

        try {
            providerService.checkConnection(IAuthenticationProperties.defaultLdap());
            fail("should failed");
        } catch (final NoSecurityConfigurationException ex) {
            assertEquals("app.security.configuration.empty", ex.getMessage());
        }

    }

    private IAuthenticationProvider createLdapProvider() {
        final IAuthenticationProvider ldapProvider = mock(IAuthenticationProvider.class, withSettings().lenient());
        doReturn(Optional.of(ldapProvider)).when(providerService)
                .createAuthenticationProvider(any(IAuthenticationProperties.class), any(IAuthenticationProvider.class));

        when(ldapProvider.isInternal()).thenReturn(false);
        when(ldapProvider.getSupportedDirectory()).thenReturn(UserDirectory.Ldap);
        when(ldapProvider.getDirectory()).thenReturn(new DefaultDirectory("directory", Collections.emptyList(), true));

        return ldapProvider;
    }

    private IAuthenticationProvider createInternalProvider() {
        final IAuthenticationProvider internalProvider = mock(IAuthenticationProvider.class, withSettings().lenient());

        doReturn(internalProvider).when(providerService).createInternalAuthenticationProvider();

        when(internalProvider.isInternal()).thenReturn(true);
        when(internalProvider.getSupportedDirectory()).thenReturn(UserDirectory.Internal);
        when(internalProvider.getDirectory())
                .thenReturn(new DefaultDirectory("directory", Collections.emptyList(), true));

        return internalProvider;
    }

    private IAuthenticationProvider createInternalLdapProvider() {
        final IAuthenticationProvider internalProvider = mock(IAuthenticationProvider.class, withSettings().lenient());

        doReturn(internalProvider).when(providerService).createInternalAuthenticationProvider();

        when(internalProvider.isInternal()).thenReturn(false);
        when(internalProvider.getSupportedDirectory()).thenReturn(UserDirectory.InternalLdap);
        when(internalProvider.getDirectory())
                .thenReturn(new DefaultDirectory("directory", Collections.emptyList(), true));

        return internalProvider;
    }

}
