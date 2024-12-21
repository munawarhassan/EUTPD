package com.pmi.tpd.core.user.permission;

import static org.hamcrest.Matchers.lessThan;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.testing.junit5.TestCase;

public class PermissionEnumTest extends TestCase {

    /**
     * Sanity test to ensure new {@link Permission} are not created with duplicate IDs by error.
     */
    @Test
    public void testForDuplicates() throws Exception {
        final Permission[] permissions = Permission.values();

        final Map<Integer, Permission> ids = new HashMap<>(permissions.length);
        for (final Permission permission : permissions) {
            final int id = permission.getId();
            if (ids.containsKey(id)) {
                fail("ID is duplicated between " + permission + " and " + ids.get(id));
            } else {
                ids.put(id, permission);
            }
        }
    }

    @Test
    public void testGetGlobalPermissions() throws Exception {
        final Set<Permission> permissions = Permission.getGlobalPermissions();
        assertEquals(3, permissions.size());
        for (final Permission permission : permissions) {
            assertTrue(permission.isGlobal());
        }
    }

    @Test
    public void testPermissionFlags() throws Exception {
        assertEquals(3, Permission.values().length);
        assertPermissionFlags(Permission.USER, true, false, false, false, true, true, false);
        assertPermissionFlags(Permission.ADMIN, true, false, false, false, true, false, false);
        assertPermissionFlags(Permission.SYS_ADMIN, true, false, false, false, true, false, false);
    }

    private void assertPermissionFlags(final Permission permission,
        final boolean isGlobal,
        final boolean isResource,
        final boolean isProject,
        final boolean isRepository,
        final boolean isGrantable,
        final boolean isGrantableToAll,
        final boolean isGrantableToAnonymous) {
        assertEquals(isGlobal,
            permission.isGlobal(),
            () -> "Permission should " + (isGlobal ? "not " : "") + "have been global");
        assertEquals(isResource,
            permission.isResource(),
            () -> "Permission should " + (isResource ? "not " : "") + "have been resource-related");

        assertEquals(isGrantable,
            permission.isGrantable(),
            () -> "Permission should " + (isGrantable ? "not " : "") + "have been grantable");
        assertEquals(isGrantableToAll,
            permission.isGrantableToAll(),
            () -> "Permission should " + (isGrantableToAll ? "not " : "") + "be grantable to all");
    }

    @Test
    public void testPermissionsInheritance() throws Exception {
        for (final Permission self : Permission.values()) {
            final Set<Permission> implyingPermissions = Sets.newEnumSet(self.getImplyingPermissions(),
                Permission.class);
            final Set<Permission> inheritingPermissions = Sets.newEnumSet(self.getInheritingPermissions(),
                Permission.class);

            assertFalse(implyingPermissions.contains(self));
            assertTrue(inheritingPermissions.contains(self));

            inheritingPermissions.remove(self);
            assertEquals(implyingPermissions, inheritingPermissions);

            for (final Permission inheritingPermission : inheritingPermissions) {
                assertTrue(inheritingPermission.getInheritedPermissions().contains(self));
            }
        }
    }

    @Test
    public void testImplyingPermissionsHaveAHigherWeight() throws Exception {
        for (final Permission permission : Permission.values()) {
            for (final Permission implyingPermission : permission.getImplyingPermissions()) {
                assertThat("", permission.getWeight(), lessThan(implyingPermission.getWeight()));
            }
        }

    }

    @Test
    public void testFromId() throws Exception {
        assertEquals(Permission.USER, Permission.fromId(3));
        assertNull(Permission.fromId(99999));
    }

    @Test
    public void testFromWeight() throws Exception {
        assertEquals(Permission.ADMIN, Permission.fromWeight(9000));
        assertNull(Permission.fromWeight(99999));
    }

    @Test
    public void testMaxWeight() throws Exception {
        assertEquals(Permission.SYS_ADMIN, Permission.max(Permission.SYS_ADMIN, Permission.USER));

    }

}
