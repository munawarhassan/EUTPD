package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.security.permission.Permission.ADMIN;
import static com.pmi.tpd.security.permission.Permission.SYS_ADMIN;
import static com.pmi.tpd.security.permission.Permission.USER;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.paging.ListPageProvider;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.core.event.permission.GlobalPermissionRevokedEvent;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.UserConverter;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.ForbiddenException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.SetPermissionRequest;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

public class DefaultPermissionAdminServiceTest extends AbstractServiceTest {

    private static final UserEntity TEST_USER = UserEntity.builder().id(1L).username("Tester").build();

    private static final UserEntity TEST_USER2 = UserEntity.builder().id(2L).username("Tester2").build();

    @Mock(lenient = true)
    private IAuthenticationContext authenticationContext;

    @Mock(lenient = true)
    private IEventPublisher eventPublisher;

    private final List<Object> eventsPublished = new ArrayList<>();

    @Mock(lenient = true)
    private IGlobalPermissionRepository globalPermissionDao;

    @Mock(lenient = true)
    private I18nService i18nService;

    @Mock(lenient = true)
    private IPermissionService permissionService;

    @Mock(lenient = true)
    private IUserService userService;

    @Mock(lenient = true)
    private IUserRepository userRepository;

    @Mock
    private IPermissionValidationService permissionValidationService;

    private DefaultPermissionAdminService permissionAdminService;

    private final List<GlobalPermissionEntity> globalPermissions = Lists.newArrayList();

    public DefaultPermissionAdminServiceTest() {
        super(DefaultPermissionAdminService.class, IPermissionAdminService.class);
    }

    @BeforeEach
    public void setUp() throws Exception {

        permissionAdminService = new DefaultPermissionAdminService(userService, new UserConverter(userRepository),
                globalPermissionDao, authenticationContext, permissionService, permissionValidationService, i18nService,
                eventPublisher);
        when(globalPermissionDao.save(isA(GlobalPermissionEntity.class))).thenAnswer(invocation -> {
            final GlobalPermissionEntity permission = (GlobalPermissionEntity) invocation.getArguments()[0];
            globalPermissions.add(permission);
            return permission;
        });

        when(globalPermissionDao.revoke(isA(GlobalPermissionEntity.class))).thenAnswer(invocation -> {
            final GlobalPermissionEntity permission = (GlobalPermissionEntity) invocation.getArguments()[0];
            return globalPermissions.remove(permission) ? 1L : 0L;
        });

        when(globalPermissionDao.hasPermissionEntry(isA(GlobalPermissionEntity.class))).thenAnswer(invocation -> {
            final GlobalPermissionEntity perm = (GlobalPermissionEntity) invocation.getArguments()[0];
            return globalPermissions.contains(perm);
        });

        when(i18nService.createKeyedMessage(anyString(), any(), any()))
                .thenReturn(new KeyedMessage("key", "localizedmessage", "rootmessage"));

        when(i18nService.createKeyedMessage(anyString(), any()))
                .thenReturn(new KeyedMessage("key", "localizedmessage", "rootmessage"));

        when(i18nService.createKeyedMessage(anyString()))
                .thenReturn(new KeyedMessage("key", "localizedmessage", "rootmessage"));

        when(authenticationContext.getCurrentUser()).thenReturn(of(TEST_USER));

        when(userService.getUserById(eq(TEST_USER.getId()))).thenReturn(TEST_USER);

        doAnswer(invocation -> {
            final Object event = invocation.getArguments()[0];
            eventsPublished.add(event);
            return null;
        }).when(eventPublisher).publish(any());
    }

    @Test
    public void testGrantingGlobalPermissionWithoutAdminPermission() throws Exception {
        assertThrows(AuthorisationException.class, () -> {
            permissionAdminService
                    .setPermission(new SetPermissionRequest.Builder().globalPermission(USER).user(TEST_USER2).build());
        });
    }

    @Test
    public void testGrantingSysadminGlobalPermissionWithAdminPermission() throws Exception {
        assertThrows(AuthorisationException.class, () -> {
            // These are superfluous, but document the intent of this test
            when(permissionService.hasGlobalPermission(eq(ADMIN))).thenReturn(true);
            when(permissionService.hasGlobalPermission(eq(SYS_ADMIN))).thenReturn(false);
            permissionAdminService.setPermission(
                new SetPermissionRequest.Builder().globalPermission(SYS_ADMIN).user(TEST_USER2).build());
        });
    }

    @Test
    public void testGrantingGlobalPermissionWithSysadminPermission() throws Exception {
        when(permissionService.hasGlobalPermission(eq(SYS_ADMIN))).thenReturn(true);
        permissionAdminService
                .setPermission(new SetPermissionRequest.Builder().globalPermission(SYS_ADMIN).user(TEST_USER2).build());
        // Doesn't throw an exception
    }

    @Test
    public void testSettingGlobalPermission() throws Exception {
        when(permissionService.hasGlobalPermission(eq(ADMIN))).thenReturn(true);
        permissionAdminService
                .setPermission(new SetPermissionRequest.Builder().globalPermission(USER).user(TEST_USER2).build());
        assertTrue(
            globalPermissions.contains(GlobalPermissionEntity.builder().permission(USER).user(TEST_USER2).build()));
    }

    @Test
    public void testSettingGlobalPermissionForGroup() throws Exception {
        final String testGroup = "TestGroup";

        when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);

        permissionAdminService
                .setPermission(new SetPermissionRequest.Builder().globalPermission(ADMIN).group(testGroup).build());
        assertEquals(1, globalPermissions.size(), "Permission was not set");
        assertTrue(
            globalPermissions
                    .contains(GlobalPermissionEntity.builder().permission(Permission.ADMIN).group(testGroup).build()),
            "Incorrect Permission stored");

        // permissionAdminService.setPermission(
        // new SetPermissionRequest.Builder().globalPermission(PROJECT_CREATE).group(testGroup).build());
        // assertTrue("Permission was not correctly set",
        // globalPermissions.contains(
        // GlobalPermissionEntity.builder().permission(Permission.PROJECT_CREATE).group(testGroup).build()));
        // assertEquals("Old permission was not correctly removed", 1, globalPermissions.size());

        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true);

        permissionAdminService
                .setPermission(new SetPermissionRequest.Builder().globalPermission(SYS_ADMIN).group(testGroup).build());
        assertTrue(
            globalPermissions.contains(
                GlobalPermissionEntity.builder().permission(Permission.SYS_ADMIN).group(testGroup).build()),
            "Permission was not correctly set");
        assertEquals(1, globalPermissions.size(), "Old permission was not correctly removed");
    }

    @Test
    public void testSettingGlobalPermissionForUser() throws Exception {
        when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);

        permissionAdminService
                .setPermission(new SetPermissionRequest.Builder().globalPermission(ADMIN).user(TEST_USER2).build());
        assertEquals(1, globalPermissions.size(), "Permission was not stored");
        assertTrue(
            globalPermissions.contains(GlobalPermissionEntity.builder().permission(ADMIN).user(TEST_USER2).build()),
            "Incorrect Permission stored");

        // permissionAdminService.setPermission(
        // new SetPermissionRequest.Builder().globalPermission(PROJECT_CREATE).user(TEST_USER2).build());
        // assertTrue("Incorrect Permission stored",
        // globalPermissions.contains(
        // new InternalGlobalPermission.Builder().permission(PROJECT_CREATE).user(TEST_USER2).build()));
        // assertEquals("Old permission was not correctly removed", 1, globalPermissions.size());

        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true);

        permissionAdminService
                .setPermission(new SetPermissionRequest.Builder().globalPermission(SYS_ADMIN).user(TEST_USER2).build());
        assertTrue(
            globalPermissions.contains(GlobalPermissionEntity.builder().permission(SYS_ADMIN).user(TEST_USER2).build()),
            "Incorrect Permission stored");
        assertEquals(1, globalPermissions.size(), "Old permission was not correctly removed");
    }

    @Test
    public void testPermissionRevokedEvent() {
        globalPermissions.add(GlobalPermissionEntity.builder().permission(SYS_ADMIN).user(TEST_USER2).build());
        permissionAdminService.revokeAllGlobalPermissions(TEST_USER2);
        boolean eventPublished = false;
        for (final Object event : eventsPublished) {
            if (event instanceof GlobalPermissionRevokedEvent) {
                assertSame(SYS_ADMIN,
                    ((GlobalPermissionRevokedEvent) event).getPermission(),
                    "Incorrect permission on event");
                assertNull(((GlobalPermissionRevokedEvent) event).getAffectedGroup(), "Group set incorrectly");
                assertSame(TEST_USER2,
                    ((GlobalPermissionRevokedEvent) event).getAffectedUser(),
                    "User set incorrectly");
                eventPublished = true;
                break;
            }
        }

        assertTrue(eventPublished, "Could not find permission granted event");
    }

    @Test
    public void testRevokeAllUserPermissions() throws Exception {
        permissionAdminService.revokeAllUserPermissions(TEST_USER);
        verify(globalPermissionDao).revokeAll(TEST_USER.getId());
    }

    @Test
    public void testRevokeAllGroupPermissions() throws Exception {
        permissionAdminService.revokeAllGroupPermissions("testrevoke");
        verify(globalPermissionDao).revokeAll("testrevoke");
    }

    @Test
    public void testCanAddUserToGroupWhenSysAdmin() throws Exception {
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true);

        permissionAdminService.canAddUserToGroup("blah");
    }

    @Test
    public void testCannotAddUserToGroupWhenAdmin() throws Exception {
        assertThrows(ForbiddenException.class, () -> {
            when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);
            when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "blah")).thenReturn(true);
            permissionAdminService.canAddUserToGroup("blah");
        });

    }

    @Test
    public void testCanAddUserToGroupWhenAdmin() throws Exception {
        when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "blah")).thenReturn(true);
        permissionAdminService.canAddUserToGroup("notblah");
    }

    @Test
    public void testCanRemoveFromSysadminGroup1() throws Exception {
        // The group has sys-admin, but user has direct sys-admin so should be ok
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(true);
        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromSysadminGroup2() throws Exception {
        // The group has sys-admin, user does not have direct but does have through another group
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
        when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet())).thenReturn(true);
        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromSysadminGroup3() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            // The group has sys-admin, user does not have sys-admin and does not have through another group
            when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
            when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
            when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
            when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet()))
                    .thenReturn(false);

            permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
        });
    }

    @Test
    public void testCanRemoveFromSysadminGroup4() throws Exception {
        // The user is not a member of the group, so should be allowed even if call should never happen
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(false);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
        when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet())).thenReturn(false);

        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromSysAdminGroup5() throws Exception {
        assertThrows(ForbiddenException.class, () -> {
            // The user removing someone else from a sys_admin group and they do not have sys admin
            when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);

            permissionAdminService.canRemoveUserFromGroup(TEST_USER2.getName(), "group");
        });
    }

    @Test
    public void testCanRemoveFromSysAdminGroup6() throws Exception {
        // The user removing someone else from a sys_admin group and they do have sys admin
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true);

        permissionAdminService.canRemoveUserFromGroup(TEST_USER2.getName(), "group");
    }

    @Test
    public void testCanRemoveFromOrdinaryGroup() throws Exception {
        // The group has neither admin, nor sys-admin so there should be no problems
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(ADMIN, "group")).thenReturn(false);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(false);
        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromAdminGroup1() throws Exception {
        // The group has admin, but user has sys-admin so ok
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(ADMIN, "group")).thenReturn(true);
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true);
        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromAdminGroup2() throws Exception {
        // The group has admin, user does not have sys-admin but does have admin directly so ok
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(ADMIN, "group")).thenReturn(true);
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false);
        when(permissionService.hasDirectGlobalUserPermission(ADMIN)).thenReturn(true);
        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromAdminGroup3() throws Exception {
        // The group has admin, user does not have sys-admin does not have admin directly
        // but is a member of another group with admin so ok
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalGroupPermission(ADMIN, "group")).thenReturn(true);
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false);
        when(permissionService.hasDirectGlobalUserPermission(ADMIN)).thenReturn(false);
        when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(ADMIN), anySet())).thenReturn(true);
        permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
    }

    @Test
    public void testCanRemoveFromAdminGroup4() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            // The group has admin, user does not have sys-admin does not have admin directly
            // is not a member of another group with admin so should throw
            when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
            when(permissionService.hasGlobalGroupPermission(ADMIN, "group")).thenReturn(true);
            when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false);
            when(permissionService.hasDirectGlobalUserPermission(ADMIN)).thenReturn(false);
            when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(ADMIN), anySet())).thenReturn(false);
            permissionAdminService.canRemoveUserFromGroup(TEST_USER.getName(), "group");
        });

    }

    @Test
    public void testDowngradingSysadminGroupPermissionAsAdmin() {
        assertThrows(IntegrityException.class, () -> {
            final String testGroup = "TestGroup";

            when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);
            when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false); // Current authenticated user
            when(permissionService.hasGlobalGroupPermission(eq(SYS_ADMIN), eq(testGroup))).thenReturn(true); // Target
            // sysadmin
            // group

            permissionAdminService
                    .setPermission(new SetPermissionRequest.Builder().globalPermission(USER).group(testGroup).build());
        });
    }

    @Test
    public void testDowngradingSysadminUserPermissionAsAdmin() {
        assertThrows(IntegrityException.class, () -> {
            when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);
            when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false); // Current authenticated user
            when(permissionService.hasGlobalPermission(eq(TEST_USER2), eq(SYS_ADMIN))).thenReturn(true); // Target
                                                                                                         // sysadmin
            // user

            permissionAdminService
                    .setPermission(new SetPermissionRequest.Builder().globalPermission(USER).user(TEST_USER2).build());
        });
    }

    @Test
    public void testRevokingSysadminGroupPermissionAsAdmin() {
        assertThrows(IntegrityException.class, () -> {
            final String testGroup = "TestGroup";
            when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false); // Current authenticated user
            when(permissionService.hasGlobalGroupPermission(eq(SYS_ADMIN), eq(testGroup))).thenReturn(true); // Target
            // sysadmin
            // group
            permissionAdminService.revokeAllGlobalPermissions(testGroup);
        });

    }

    @Test
    public void testRevokingSysadminUserPermissionAsAdmin() {
        assertThrows(IntegrityException.class, () -> {
            when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false); // Current authenticated user
            when(permissionService.hasGlobalPermission(eq(TEST_USER2), eq(SYS_ADMIN))).thenReturn(true); // Target
                                                                                                         // sysadmin
            // user
            permissionAdminService.revokeAllGlobalPermissions(TEST_USER2);
        });
    }

    @Test
    public void testRevokeGlobalUserPermissionSysAdmin1() throws Exception {
        // The user is not revoking their own privilege so ok
        when(authenticationContext.getCurrentUser()).thenReturn(of(TEST_USER2));

        permissionAdminService.revokeAllGlobalPermissions(TEST_USER);
    }

    @Test
    public void testRevokeGlobalUserPermissionSysAdmin2() throws Exception {
        // The user is working on their account but they have sys_admin privilege from another group.
        when(permissionService.hasGlobalPermissionThroughGroupMembership(any(Permission.class), anySet()))
                .thenReturn(true);

        permissionAdminService.revokeAllGlobalPermissions(TEST_USER);
    }

    @Test
    public void testRevokeGlobalUserPermissionSysAdmin3() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            // The user is removing their own sys_admin permission and do not have it through another group, which
            // should
            // fail
            globalPermissions.add(GlobalPermissionEntity.builder().permission(SYS_ADMIN).user(TEST_USER).build());
            when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet()))
                    .thenReturn(false);

            permissionAdminService.revokeAllGlobalPermissions(TEST_USER);
        });
    }

    @Test
    public void testRevokeGlobalGroupPermissionSysAdmin1() throws Exception {
        // The user is not a member of the group being revoked
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(false);

        permissionAdminService.revokeAllGlobalPermissions("group");
    }

    @Test
    public void testRevokeGlobalGroupPermissionSysAdmin2() throws Exception {
        // The user is a member of the group being revoked but have their own permission
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(isA(Permission.class))).thenReturn(true);

        permissionAdminService.revokeAllGlobalPermissions("group");
    }

    @Test
    public void testRevokeGlobalGroupPermissionSysAdmin3() throws Exception {
        // The user is a member of the group being revoked, do no have their own permission
        // but they have membership through another group
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(isA(Permission.class))).thenReturn(false);
        when(permissionService.hasGlobalPermissionThroughGroupMembership(isA(Permission.class), anySet()))
                .thenReturn(true);

        permissionAdminService.revokeAllGlobalPermissions("group");
    }

    @Test
    public void testRevokeGlobalGroupPermissionSysAdmin4() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            // The user is a member of the group being revoked and does not have his own permission
            // or privilege through another group, so should not be allowed
            globalPermissions.add(GlobalPermissionEntity.builder().permission(SYS_ADMIN).group("group").build());
            when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
            when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
            when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet()))
                    .thenReturn(false);

            permissionAdminService.revokeAllGlobalPermissions("group");
        });
    }

    @Test
    public void testRevokeGlobalGroupPermissionAdmin() throws Exception {
        // The user is a member of the group being revoked but is also a member of another group that has SYS_ADMIN
        globalPermissions.add(GlobalPermissionEntity.builder().permission(ADMIN).group("group").build());
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true); // through another group

        permissionAdminService.revokeAllGlobalPermissions("group");
    }

    @Test
    public void testRevokeGlobalGroupPermissionProjectCreator() throws Exception {
        // The user is a member of the group being revoked but has it's own ADMIN privilege
        globalPermissions.add(GlobalPermissionEntity.builder().permission(ADMIN).user(TEST_USER).build());
        when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(true); // through another group

        permissionAdminService.revokeAllGlobalPermissions(TEST_USER);
    }

    @Test
    public void testRevokeAsAnonymous() throws Exception {
        when(authenticationContext.getCurrentUser()).thenReturn(null);
        permissionAdminService.revokeAllGlobalPermissions("group");
    }

    @Test
    public void testCanDeleteGroup() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            // user is a member of a sys-admin group and doesn't have privileges any other way
            when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
            when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
            when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
            when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet()))
                    .thenReturn(false);
            permissionAdminService.canDeleteGroup("group");
        });
    }

    @Test
    public void testCanDeleteGroup1() throws Exception {
        // user is a member of a sys-admin group but does have privileges from another group
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
        when(permissionService.hasGlobalPermissionThroughGroupMembership(eq(SYS_ADMIN), anySet())).thenReturn(true);
        permissionAdminService.canDeleteGroup("group");
    }

    @Test
    public void testCanDeleteSysAdminGroup() throws Exception {
        assertThrows(ForbiddenException.class, () -> {
            // User is not a member of the group, User has admin privilege but not sys-admin privilege
            // group has sys-admin privilege
            when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(false);
            when(permissionService.hasDirectGlobalUserPermission(ADMIN)).thenReturn(true);
            when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
            when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(true);
            permissionAdminService.canDeleteGroup("group");
        });
    }

    @Test
    public void testCanDeleteAdminGroup() throws Exception {
        // User is not a member of the group, User has admin privilege but not sys-admin privilege
        // group has admin privilege
        when(userService.isUserInGroup(TEST_USER, "group")).thenReturn(false);
        when(permissionService.hasDirectGlobalUserPermission(ADMIN)).thenReturn(true);
        when(permissionService.hasDirectGlobalUserPermission(SYS_ADMIN)).thenReturn(false);
        when(permissionService.hasGlobalGroupPermission(SYS_ADMIN, "group")).thenReturn(false);
        when(permissionService.hasGlobalGroupPermission(ADMIN, "group")).thenReturn(true);
        permissionAdminService.canDeleteGroup("group");
    }

    @Test
    public void testDeleteUserMyself() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            when(userService.getUserByName("tester")).thenReturn(TEST_USER);
            permissionAdminService.canDeleteUser("tester");
        });
    }

    @Test
    public void testDeleteSysAdminUser() throws Exception {
        assertThrows(ForbiddenException.class, () -> {
            when(userService.getUserByName("tester2")).thenReturn(TEST_USER2);
            when(permissionService.hasGlobalPermission(TEST_USER2, SYS_ADMIN)).thenReturn(true);
            when(permissionService.hasGlobalPermission(SYS_ADMIN)).thenReturn(false);

            permissionAdminService.canDeleteUser("tester2");
        });
    }

    @Test
    public void testDeleteNonSysAdminUser() throws Exception {
        when(userService.getUserByName("tester2")).thenReturn(TEST_USER2);
        when(permissionService.hasGlobalPermission(TEST_USER2, ADMIN)).thenReturn(true);
        when(permissionService.hasGlobalPermission(ADMIN)).thenReturn(true);

        permissionAdminService.canDeleteUser("tester2");
    }

    @Test
    public void testDeleteNoLocalUser() throws Exception {
        permissionAdminService.canDeleteUser("unknown");
    }

    @Test
    public void testWithoutGroups() {
        final List<String> groups = ImmutableList.of("AbC", "def", "deG", "xyz");

        final Predicate<String> predicate = DefaultPermissionAdminService.withoutGroups(new ListPageProvider<>(groups));
        assertFalse(predicate.apply("ABC"));
        assertFalse(predicate.apply("abc"));
        assertTrue(predicate.apply("gh"));
        assertFalse(predicate.apply("XYZ"));
        assertTrue(predicate.apply("zzz"));
    }

    @Test
    public void testWithoutUsers() {
        final List<IUser> users = ImmutableList.<IUser> of(User.builder().username("AbC").build(),
            User.builder().username("def").build(),
            User.builder().username("deG").build(),
            User.builder().username("xyz").build());
        final Predicate<IUser> predicate = DefaultPermissionAdminService.withoutUsers(new ListPageProvider<>(users));
        assertFalse(predicate.apply(User.builder().username("ABC").build()));
        assertFalse(predicate.apply(User.builder().username("abc").build()));
        assertTrue(predicate.apply(User.builder().username("gh").build()));
        assertFalse(predicate.apply(User.builder().username("XYZ").build()));
        assertTrue(predicate.apply(User.builder().username("zzz").build()));
    }

}
