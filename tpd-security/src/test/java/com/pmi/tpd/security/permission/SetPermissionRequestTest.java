package com.pmi.tpd.security.permission;

import java.util.Arrays;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import org.junit.jupiter.api.Test;

public class SetPermissionRequestTest extends MockitoTestCase {

    @Test
    public void testNoPermission() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().user(mock(IUser.class)).build());
    }

    @Test
    public void testNoUsersOrGroups() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(Permission.USER).build());
    }

    @Test
    public void testNullPermission() throws Exception {
        assertThrows(NullPointerException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(null).user(mock(IUser.class)).build());
    }

    @Test
    public void testNullUser() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(Permission.USER).user(null).build());
    }

    @Test
    public void testNullUsers() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(Permission.USER)
                .users(Arrays.asList(mock(IUser.class), null)).build());
        ;
    }

    @Test
    public void testNullGroup() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(Permission.USER).group(null).build());
    }

    @Test
    public void testEmptyGroup() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(Permission.USER).group("").build());
    }

    @Test
    public void testNullGroups() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new SetPermissionRequest.Builder().globalPermission(Permission.USER).groups(Arrays.asList("group", "")).build());
    }

}
