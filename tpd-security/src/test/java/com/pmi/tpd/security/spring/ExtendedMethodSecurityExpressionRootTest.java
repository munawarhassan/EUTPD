package com.pmi.tpd.security.spring;

import static org.mockito.ArgumentMatchers.same;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ExtendedMethodSecurityExpressionRootTest extends MockitoTestCase {

    @Mock
    private Authentication authentication;

    private final ThreadLocal<Boolean> guard = new ThreadLocal<>();

    @Mock(lenient = true)
    private IPermissionService permissionService;

    @Test
    public void testConstructorWithNullAuthentication() {
        // This is verifying a behaviour deep in the inheritance hierarchy, at the
        // super-most class.
        assertThrows(IllegalArgumentException.class, () -> new ExtendedMethodSecurityExpressionRoot(null, guard));
    }

    @Test
    public void testHasAnyPermission() {
        when(permissionService.hasAnyUserPermission(same(Permission.ADMIN))).thenReturn(false);

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                guard);
        root.setPermissionService(permissionService);
        assertFalse(root.hasAnyPermission(Permission.ADMIN));
    }

    @Test
    public void testHasGlobalPermission() {
        when(permissionService.hasGlobalPermission(same(Permission.SYS_ADMIN))).thenReturn(true);
        when(permissionService.hasGlobalPermission(same(Permission.ADMIN))).thenReturn(false);

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                guard);
        root.setPermissionService(permissionService);
        assertTrue(root.hasGlobalPermission(Permission.SYS_ADMIN));
        assertFalse(root.hasGlobalPermission(Permission.ADMIN));
    }

    @Test
    public void testIsCurrentUserWithString() {
        final UserAuthenticationToken token = mock(UserAuthenticationToken.class);
        when(token.getName()).thenReturn("bturner", "bturner", "jdoe");

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(token, guard);
        root.setPermissionService(permissionService);
        assertFalse(root.isCurrentUser((String) null)); // passed user is null
        assertTrue(root.isCurrentUser("bturner")); // bturner == bturner
        assertFalse(root.isCurrentUser("bturner")); // bturner != jdoe
    }

    @Test
    public void testIsCurrentUserWithUser() {
        final IUser user = mock(IUser.class);
        when(user.getName()).thenReturn("bturner");

        final UserAuthenticationToken token = mock(UserAuthenticationToken.class);
        when(token.getName()).thenReturn("bturner", "jdoe");

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(token, guard);
        root.setPermissionService(permissionService);
        assertFalse(root.isCurrentUser((IUser) null)); // passed user is null
        assertTrue(root.isCurrentUser(user)); // bturner == bturner
        assertFalse(root.isCurrentUser(user)); // bturner != jdoe
    }

    @Test
    public void testPermissionCheckWithGuard() {
        assertThrows(IllegalStateException.class, () -> {
            guard.set(Boolean.TRUE);
            try {
                when(permissionService.hasGlobalPermission(same(Permission.ADMIN))).thenReturn(false);

                final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(
                        authentication, guard);
                root.setPermissionService(permissionService);
                assertTrue(root.hasGlobalPermission(Permission.SYS_ADMIN));

            } finally {
                guard.remove();
            }
        });
    }

    @Test
    public void testReturnObject() {
        final Object returnObject = new Object();

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                guard);
        assertNull(root.getReturnObject());

        root.setReturnObject(returnObject);
        assertSame(returnObject, root.getReturnObject());

        root.setReturnObject(null);
        assertNull(root.getReturnObject());
    }

    @Test
    public void testUserHasGlobalPermission() throws Exception {
        final IUser user = User.builder().username("test").build();
        when(permissionService.hasGlobalPermission(eq("test"), same(Permission.SYS_ADMIN))).thenReturn(true);

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                guard);
        root.setPermissionService(permissionService);
        assertTrue(root.hasGlobalPermission(user, Permission.SYS_ADMIN));
        assertFalse(root.hasGlobalPermission(user, Permission.ADMIN));
    }

    @Test
    public void testUserHasGlobalPermissionByName() throws Exception {
        final String name = "test";
        when(permissionService.hasGlobalPermission(eq(name), same(Permission.SYS_ADMIN))).thenReturn(true);

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                guard);
        root.setPermissionService(permissionService);
        assertTrue(root.hasGlobalPermission(name, Permission.SYS_ADMIN));
        assertFalse(root.hasGlobalPermission(name, Permission.ADMIN));
    }

    @Test
    public void testUserHasGlobalPermissionForUnknown() throws Exception {
        when(permissionService.hasGlobalPermission(same(Permission.SYS_ADMIN))).thenReturn(true);

        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                guard);
        root.setPermissionService(permissionService);
        assertFalse(root.hasGlobalPermission("unknown", Permission.SYS_ADMIN));
        assertFalse(root.hasGlobalPermission("unknown", Permission.ADMIN));
    }
}
