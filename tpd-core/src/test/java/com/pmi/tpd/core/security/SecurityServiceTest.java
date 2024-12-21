package com.pmi.tpd.core.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.IUncheckedOperation;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.DefaultPermissionGraph;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.spring.UserAuthenticationToken;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

public class SecurityServiceTest extends MockitoTestCase {

    private static final String INVALID_USER = "invalid";

    private final IAuthenticationContext authenticationContext = new DefaultAuthenticationContext();

    @Mock
    private IRequestContext requestContext;

    @Mock(lenient = true)
    private IRequestManager requestManager;

    @Mock(lenient = true)
    private IUserService userService;

    private ISecurityService securityService;

    private final IUncheckedOperation<IUser> runAsUserReader = () -> authenticationContext.getCurrentUser()
            .orElse(null);

    private final IUncheckedOperation<IPermissionGraph> runWithPermissionReader = () -> authenticationContext
            .getCurrentToken()
            .map(token -> token.getElevatedPermissions())
            .orElse(null);

    @BeforeEach
    public void setup() {
        SecurityContextHolder.createEmptyContext();
        when(requestManager.getRequestContext()).thenReturn(requestContext);

        securityService = new DefaultSecurityService(new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS),
                requestManager, userService);
    }

    @AfterEach
    public void cleanupSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetCurrentUserWhenNotAuthenticated() {
        assertTrue(authenticationContext.getCurrentUser().isEmpty());
    }

    @Test
    public void testGetCurrentUserWhenAuthenticated() {
        final IUser user = mock(IUser.class);
        final UserDetails userDetails = mock(UserDetails.class);
        final UserAuthenticationToken token = UserAuthenticationToken.forUser(user, userDetails);

        SecurityContextHolder.getContext().setAuthentication(token);

        assertSame(user, authenticationContext.getCurrentUser().get());
    }

    @Test
    public void testAsAnonymousAnonymously() {
        assertTrue(authenticationContext.getCurrentUser().isEmpty());

        // run an operation anonymously
        final IUser nestedUser = securityService.anonymously("reason").call(runAsUserReader);
        assertNull(nestedUser);

        assertTrue(authenticationContext.getCurrentUser().isEmpty());
    }

    @Test
    public void testAsAnonymousWhenAuthenticated() {
        final IEscalatedSecurityContext anonymously = securityService.anonymously("reason");

        final IUser existingUser = mock(IUser.class);

        // login as a user
        mockLoggedInAsUser(existingUser);
        assertSame(existingUser, authenticationContext.getCurrentUser().get());

        // run an operation anonymously (previously created escalatedSecurityContext)
        assertNull(anonymously.call(runAsUserReader));
        // run the operation anonymously with an IEscalatedSecurityContext that was created while authenticated
        assertNull(securityService.anonymously("reason").call(runAsUserReader));

        assertSame(existingUser, authenticationContext.getCurrentUser().get());
    }

    @Test
    public void testAsUserNobodyLoggedIn() throws Exception {
        assertTrue(authenticationContext.getCurrentUser().isEmpty());

        final IUser user = mock(IUser.class);
        when(userService.getUserByName("admin")).thenReturn(user);

        // run an operation as "admin"
        final IUser nestedUser = securityService.impersonating(user, "reason").call(runAsUserReader);
        assertSame(user, nestedUser);

        assertTrue(authenticationContext.getCurrentUser().isEmpty());
    }

    @Test
    public void testAsUserWithCurrentlyLoggedInUser() throws Exception {
        // mock a 'runAs' and an existing user
        final IUser runAs = mock(IUser.class);
        final IUser existing = mock(IUser.class);

        // create a security context anonymously
        final IEscalatedSecurityContext asUser = securityService.impersonating(runAs, "reason");

        // mock an authenticated user
        mockLoggedInAsUser(existing);
        assertSame(existing, authenticationContext.getCurrentUser().get());

        // verify impersonating works with the anonymously created security context
        assertSame(runAs, asUser.call(runAsUserReader));
        // verify that impersonation works when creating a security context while authenticated as another user
        assertSame(runAs, securityService.impersonating(runAs, "reason").call(runAsUserReader));

        // verify that the original user has been restored
        assertNotNull(authenticationContext.getCurrentUser().get());
        assertSame(existing, authenticationContext.getCurrentUser().get());
    }

    @Test
    public void testWithSinglePermission() throws Exception {
        final IPermissionGraph runWithPermissions = securityService.withPermission(Permission.USER, "one permission")
                .call(runWithPermissionReader);

        assertPermissionGraph(runWithPermissions, Permission.USER);
    }

    @Test
    public void testDoAsUserWithInvalidUser() throws Exception {
        assertThrows(PreAuthenticationFailedException.class, () -> {
            securityService.doAsUser("reason", INVALID_USER, runAsUserReader);
        });
    }

    @Test
    public void testEscalateForRequestAsUser() throws Exception {
        when(requestContext.isActive()).thenReturn(true);

        final IUser user = mock(IUser.class);
        securityService.impersonating(user, "Why not?").applyToRequest();

        assertSame(user, authenticationContext.getCurrentUser().get());
    }

    @Test
    public void testEscalateForRequestAsUserWhenAuthenticated() throws Exception {
        when(requestContext.isActive()).thenReturn(true);

        final IUser existing = mock(IUser.class);
        mockLoggedInAsUser(existing);

        final IUser user = mock(IUser.class);
        securityService.impersonating(user, "Why not?").applyToRequest();

        assertSame(user, authenticationContext.getCurrentUser().get());
    }

    @Test
    public void testEscalateForRequestFailsIfNoRequest() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            when(requestContext.isActive()).thenReturn(false);
            securityService.anonymously("Why not?").withPermission(Permission.ADMIN).applyToRequest();
        });
    }

    private void assertPermissionGraph(final IPermissionGraph graph, final Permission... expectedPermissions) {
        final DefaultPermissionGraph.Builder builder = new DefaultPermissionGraph.Builder();
        for (final Permission permission : expectedPermissions) {
            builder.add(permission, null);
        }
        final IPermissionGraph expected = builder.build();
        for (final Permission permission : Permission.values()) {
            assertEquals(expected.isGranted(permission, null), graph.isGranted(permission, null), permission.name());
        }
    }

    private void mockLoggedInAsUser(final IUser user) {
        final UserDetails userDetails = mock(UserDetails.class);
        SecurityContextHolder.getContext().setAuthentication(UserAuthenticationToken.forUser(user, userDetails));
    }
}
