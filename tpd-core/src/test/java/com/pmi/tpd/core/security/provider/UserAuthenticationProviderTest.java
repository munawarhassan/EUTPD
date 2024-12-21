package com.pmi.tpd.core.security.provider;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.permission.IEffectivePermission;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.spring.RememberMeUserAuthenticationToken;
import com.pmi.tpd.security.spring.UserAuthenticationToken;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests {@link UserAuthenticationProvider}.
 */
public class UserAuthenticationProviderTest extends MockitoTestCase {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IPermissionService permissionService;

    @Mock(lenient = true)
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAuthenticationProvider userAuthenticationProvider;

    @Test
    public void testUserNotFound() throws Exception {

        final IUser user = mock(IUser.class);
        final UserDetails userDetails = mock(UserDetails.class);
        when(user.getName()).thenReturn("test");

        final UserAuthenticationToken token = UserAuthenticationToken.forUser(user, userDetails);
        when(userRepository.findByName(eq("test"))).thenReturn(null);

        try {
            userAuthenticationProvider.authenticate(token);
            fail("Should have thrown BadCredentialsException");
        } catch (final BadCredentialsException expected) {
        }
    }

    @Test
    public void testHideUserNotFoundExceptions() throws Exception {

        final IUser user = mock(IUser.class);
        final UserDetails userDetails = mock(UserDetails.class);
        when(user.getName()).thenReturn("test");

        final UserAuthenticationToken token = UserAuthenticationToken.forUser(user, userDetails);
        when(userRepository.findByName(eq("test"))).thenReturn(null);

        try {
            userAuthenticationProvider.hideUserNotFoundExceptions = false;
            userAuthenticationProvider.authenticate(token);
            fail("Should have thrown UsernameNotFoundException");
        } catch (final UsernameNotFoundException expected) {
        }
    }

    @Test
    public void testIgnoresClassesItDoesNotSupport() throws Exception {

        final TestingAuthenticationToken token = new TestingAuthenticationToken("user", "password", "ROLE_A");
        assertThat(userAuthenticationProvider.supports(TestingAuthenticationToken.class), equalTo(false));

        // Try it anyway
        assertThat(userAuthenticationProvider.authenticate(token), nullValue());
    }

    @Test
    public void testAuthenticatedInactiveUser() throws Exception {

        final IUser user = mock(IUser.class, withSettings().lenient());
        final UserDetails userDetails = mock(UserDetails.class);
        when(user.getName()).thenReturn("test");
        when(user.getPassword()).thenReturn("password");
        final UserEntity actualUser = UserEntity.builder()
                .username("test")
                .password("encodedPassword")
                .activated(false)
                .build();

        when(userRepository.findByName(eq("test"))).thenReturn(actualUser);

        when(permissionService.getEffectivePermissions(eq(actualUser))).thenReturn(Collections.emptyList());

        final UserAuthenticationToken token = UserAuthenticationToken.forUser(user, userDetails);
        try {
            userAuthenticationProvider.authenticate(token);
            fail("Should have thrown DisabledException");
        } catch (final DisabledException ex) {

        }

    }

    @Test
    public void testNormalOperation() throws Exception {

        final IUser user = mock(IUser.class, withSettings().lenient());
        final UserDetails userDetails = mock(UserDetails.class);
        when(user.getName()).thenReturn("test");
        when(user.getPassword()).thenReturn("password");
        final UserEntity actualUser = UserEntity.builder()
                .username("test")
                .password("encodedPassword")
                .activated(true)
                .build();

        when(userRepository.findByName(eq("test"))).thenReturn(actualUser);

        when(permissionService.getEffectivePermissions(eq(actualUser))).thenReturn(Collections.emptyList());

        final UserAuthenticationToken token = UserAuthenticationToken.forUser(user, userDetails);

        final Authentication result = userAuthenticationProvider.authenticate(token);

        assertThat(token, equalTo(result));
    }

    @Test
    public void testAuthenticateWithUsernamePasswordAuthenticationToken() throws Exception {
        final UserEntity actualUser = UserEntity.builder()
                .username("user")
                .password("encodedPassword")
                .activated(true)
                .build();

        when(userRepository.findByName(eq("user"))).thenReturn(actualUser);

        final IEffectivePermission userPermission = mock(IEffectivePermission.class);
        when(userPermission.getPermission()).thenReturn(Permission.USER);

        when(permissionService.getEffectivePermissions(eq(actualUser))).thenReturn(Lists.newArrayList(userPermission));

        when(passwordEncoder.matches(eq("password"), eq("encodedPassword"))).thenReturn(true);
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", "password",
                AuthorityUtils.createAuthorityList("role_1", "role_2"));

        final Authentication result = userAuthenticationProvider.authenticate(token);

        assertThat(result, instanceOf(UserAuthenticationToken.class));
    }

    @Test
    public void testSupports() {
        assertThat(userAuthenticationProvider.supports(UserAuthenticationToken.class), equalTo(true));
        assertThat(userAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class), equalTo(true));
        assertThat(userAuthenticationProvider.supports(RememberMeUserAuthenticationToken.class), equalTo(false));
        assertThat(userAuthenticationProvider.supports(TestingAuthenticationToken.class), equalTo(false));
    }
}
