package com.pmi.tpd.core.user.permission;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.testing.junit5.TestCase;
import com.pmi.tpd.core.user.permission.DefaultPermissionGraph.Encoding;

public class DefaultPermissionGraphEncoderTest extends TestCase {

    private final DefaultPermissionGraph.Encoding encoding = new DefaultPermissionGraph.Encoding();

    @Test
    public void testPermissionsFitInEncoding() {
        // the encoding allocates 3 bytes for the permission encoding. In the current encoding scheme that means that
        // the highest permission id that can be encoded = 23
        for (final Permission permission : Permission.values()) {
            assertTrue(permission.getId() >= 0 && permission.getId() < 24,
                "permission.id must be in the [0, 24) range. Values outside this range cannot be encoded");
        }

    }

    @Test
    public void testGlobalPermission() {
        verifyEncodeDecode(Permission.SYS_ADMIN, null);
        verifyEncodeDecode(Permission.ADMIN, null);
        verifyEncodeDecode(Permission.USER, null);
    }

    @Test
    public void testShouldOutputOnlyPermissionNameForGlobalPermissions() throws Exception {
        assertEquals("SYS_ADMIN", encoding.toString(encoding.encode(Permission.SYS_ADMIN, null)));
    }

    private void verifyEncodeDecode(final Permission permission, final Integer resourceId) {
        final long encoded = encoding.encode(permission, resourceId);

        assertEquals(permission, Encoding.decodePermission(encoded));
        assertEquals(resourceId != null ? resourceId.longValue() : 0, Encoding.decodeResourceId(encoded));
    }
}