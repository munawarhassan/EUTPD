package com.pmi.tpd.core.user.permission;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.testing.junit5.TestCase;

public class PermissionCriteriaTest extends TestCase {

    @Test
    public void testUserCriteriaWithInvalidResource() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            final IUser user = User.builder().id(1L).username("user").build();
            new UserPermissionCriteria.Builder(user).permission(Permission.USER).resource(new Object()).build();
        });
    }

    @Test
    public void testGroupCriteriaWithInvalidResource() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            new GroupPermissionCriteria.Builder("group").permission(Permission.USER).resource(new Object()).build();
        });

    }

}
