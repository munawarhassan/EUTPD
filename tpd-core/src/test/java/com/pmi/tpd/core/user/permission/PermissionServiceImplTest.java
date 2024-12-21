package com.pmi.tpd.core.user.permission;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.model.user.IIterablePermissionGraph;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.spring.UserAuthenticationToken;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class PermissionServiceImplTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IAuthenticationContext authenticationContext;

    @Mock(lenient = true)
    private IEffectivePermissionRepository permissionDao;

    @Mock(lenient = true)
    private IPermissionGraphFactory graphFactory;

    @Mock(lenient = true)
    private IUserService userService;

    @InjectMocks
    private PermissionServiceImpl service;

    private UserAuthenticationToken currentToken;

    private Map<Long, IPermissionGraph> userGraphs;

    @BeforeEach
    public void setUp() throws Exception {

        when(authenticationContext.getCurrentToken()).thenAnswer(invocation -> Optional.ofNullable(currentToken));
        when(authenticationContext.getCurrentUser())
                .thenAnswer(invocationOnMock -> Optional.ofNullable(currentToken).map(token -> token.getPrincipal()));

        userGraphs = Maps.newHashMap();
        when(graphFactory.createGraph(any(IUser.class))).thenAnswer(invocationOnMock -> {
            final IUser user = (IUser) invocationOnMock.getArguments()[0];
            final IPermissionGraph graph = userGraphs.get(user.getId());
            return graph != null ? graph : mock(IIterablePermissionGraph.class);
        });
    }

    @Test
    public void testGetHighestGlobalPermission() throws Exception {
        mockGroupMemberships(anyString());

        final IUser sysAdmin = createMockUser(1, Permission.SYS_ADMIN);
        final IUser admin = createMockUser(2, Permission.ADMIN);
        final IUser user = createMockUser(4, Permission.USER);
        final IUser noAccess = createMockUser(5);

        assertEquals(null, service.getHighestGlobalPermission(noAccess));
        assertEquals(Permission.USER, service.getHighestGlobalPermission(user));
        assertEquals(Permission.ADMIN, service.getHighestGlobalPermission(admin));
        assertEquals(Permission.SYS_ADMIN, service.getHighestGlobalPermission(sysAdmin));
    }

    @Test
    public void testGetHighestGlobalGroupPermission() throws Exception {
        final Map<String, List<Permission>> permsForGroups = ImmutableMap.<String, List<Permission>> builder()
                .put("group1", Arrays.asList(Permission.SYS_ADMIN, Permission.ADMIN, Permission.USER))
                .put("group2", Arrays.asList(Permission.ADMIN, Permission.USER))
                .put("group3", Arrays.asList(Permission.USER))
                .put("group4", Collections.<Permission> emptyList())
                .build();
        when(permissionDao.isGrantedToGroup(notNull())).then(invocation -> {

            final GroupPermissionCriteria criteria = (GroupPermissionCriteria) invocation.getArguments()[0];

            final Permission requested = criteria.getPermission();

            final String group = Iterables.getOnlyElement(criteria.getGroups());

            final List<Permission> permissions = permsForGroups.get(group);
            return permissions.contains(requested);
        });

        assertEquals(null, service.getHighestGlobalGroupPermission(null));
        assertEquals(Permission.SYS_ADMIN, service.getHighestGlobalGroupPermission("group1"));
        assertEquals(Permission.ADMIN, service.getHighestGlobalGroupPermission("group2"));
        assertEquals(Permission.USER, service.getHighestGlobalGroupPermission("group3"));
        assertEquals(null, service.getHighestGlobalGroupPermission("group4"));
    }

    @Test
    public void testGetUsersWithPermission() throws Exception {
        new DirectFieldAccessor(service).setPropertyValue("maxUserPageSize", 100);
        final Permission permission = Permission.USER;

        final IUser user1 = createActiveUser("user1");
        final IUser user2 = createActiveUser("user2");
        final IUser user3 = createActiveUser("user3");
        final IUser user4 = createUser("user4"); // Not active

        when(permissionDao.findUsers(eq(permission), notNull(), notNull())).thenAnswer(invocation -> {
            final Pageable pageRequest = (Pageable) invocation.getArguments()[1];
            // only activated user
            return PageUtils.createPage(Arrays.asList(user1, user2), pageRequest);
        });

        when(permissionDao.findGroups(eq(permission), notNull())).thenAnswer(invocation -> {
            final Pageable pageRequest = (Pageable) invocation.getArguments()[1];
            return PageUtils.createPage(Arrays.asList("group1"), pageRequest);
        });

        when(userService.findUsersByGroup(eq("group1"), any())).thenAnswer(invocation -> {
            final Pageable pageRequest = (Pageable) invocation.getArguments()[1];
            return PageUtils.createPage(Arrays.asList(user2, user3, user4), pageRequest)
                    .map(user -> UserRequest.builder(user).build());
        });

        final Set<String> users = service.getUsersWithPermission(permission);
        assertThat(users, containsInAnyOrder("user1", "user2", "user3"));
        assertEquals(3, users.size(), "Number of users is incorrect");
    }

    @Test
    public void testHasGlobalGroupPermission() throws Exception {
        final IUser user = createMockUser(1, "name");
        currentToken = UserAuthenticationToken.forUser(user);
        when(userService.findGroupsByUser(eq("name"), any())).thenAnswer(invocation -> PageUtils
                .createPage(Collections.singleton("sysadmin"), (Pageable) invocation.getArguments()[1]));
        when(permissionDao.isGrantedToGroup(matchesCriteria(Permission.SYS_ADMIN, "sysadmin", null))).thenReturn(true);

        assertFalse(service.hasDirectGlobalUserPermission(Permission.SYS_ADMIN),
            "User should not have sys_admin as user");
        assertTrue(service.hasGlobalGroupPermission(Permission.SYS_ADMIN, "sysadmin"),
            "group should have sys-admin permission");
        assertFalse(service.hasGlobalGroupPermission(Permission.SYS_ADMIN, "other"),
            "other group should have sys-admin permission");
    }

    @Test
    public void testHasDirectGlobalUserPermission() throws Exception {
        final IUser user = createMockUser(1, "name");
        currentToken = UserAuthenticationToken.forUser(user);

        when(permissionDao.isGrantedToUser(matchesCriteria(Permission.SYS_ADMIN, 1))).thenReturn(true);
        mockGroupMemberships(eq("name"), "sysadmin");

        assertFalse(
            service.hasGlobalPermissionThroughGroupMembership(Permission.SYS_ADMIN, Collections.<String> emptySet()),
            "User should have not sys_admin through a group");
        assertTrue(service.hasDirectGlobalUserPermission(Permission.SYS_ADMIN),
            "User should have direct sys-admin permission");
    }

    @Test
    public void testHasGlobalPermission() {
        final IUser user = createMockUser(1, "name", Permission.USER);
        currentToken = UserAuthenticationToken.forUser(user);

        assertFalse(service.hasGlobalPermission((IUser) null, Permission.USER));
        assertTrue(service.hasGlobalPermission(user, Permission.USER));
        assertTrue(service.hasGlobalPermission("name", Permission.USER));
        assertTrue(service.hasGlobalPermission(Permission.USER));
    }

    @Test
    public void testHasSysAdminGlobalPermission() {
        final IUser user = createMockUser(1, "name", Permission.SYS_ADMIN);
        currentToken = UserAuthenticationToken.forUser(user);

        assertFalse(service.hasGlobalPermission((IUser) null, Permission.SYS_ADMIN));
        assertTrue(service.hasGlobalPermission(user, Permission.SYS_ADMIN));
        assertTrue(service.hasGlobalPermission("name", Permission.SYS_ADMIN));
        assertTrue(service.hasGlobalPermission(Permission.SYS_ADMIN));
    }

    @Test
    public void testHasGlobalPermissionThroughGroupMembership() throws Exception {
        final IUser user = createMockUser(1, "name");
        currentToken = UserAuthenticationToken.forUser(user);

        when(userService.findGroupsByUser(eq("name"), any())).thenAnswer(invocation -> PageUtils
                .createPage(Arrays.asList("sysadmin", "admin"), (Pageable) invocation.getArguments()[1]));
        when(permissionDao.isGrantedToUser(matchesCriteria(Permission.SYS_ADMIN, 1))).thenReturn(false);
        when(permissionDao
                .isGrantedToGroup(matchesCriteria(Permission.SYS_ADMIN, Arrays.asList("sysadmin", "admin"), null)))
                        .thenReturn(true);
        when(permissionDao.isGrantedToGroup(matchesCriteria(Permission.SYS_ADMIN, "sysadmin", null))).thenReturn(true);
        when(permissionDao.isGrantedToGroup(matchesCriteria(Permission.ADMIN, "admin", null))).thenReturn(true);
        when(permissionDao.isGrantedToUser(matchesCriteria(Permission.SYS_ADMIN, 1))).thenReturn(false);
        when(permissionDao.isGrantedToUser(matchesCriteria(Permission.ADMIN, 1))).thenReturn(false);

        assertFalse(service.hasDirectGlobalUserPermission(Permission.SYS_ADMIN),
            "User should not have direct sys_admin permission");
        assertFalse(service.hasDirectGlobalUserPermission(Permission.ADMIN),
            "User should not have direct admin permissions");

        assertTrue(
            service.hasGlobalPermissionThroughGroupMembership(Permission.SYS_ADMIN, Collections.<String> emptySet()),
            "User should have sys-admin permission through group membership");
        assertTrue(service.hasGlobalPermissionThroughGroupMembership(Permission.ADMIN, Collections.<String> emptySet()),
            "User should have admin permission through group membership");
        assertFalse(
            service.hasGlobalPermissionThroughGroupMembership(Permission.SYS_ADMIN, Collections.singleton("sysadmin")),
            "User should not have sys-admin permission because group excluded");
        assertFalse(service.hasGlobalPermissionThroughGroupMembership(Permission.ADMIN, Collections.singleton("admin")),
            "User should not have admin permission because group excluded");
        assertTrue(
            service.hasGlobalPermissionThroughGroupMembership(Permission.SYS_ADMIN, Collections.singleton("admin")),
            "User should have admin permission through group membership");
    }

    @Test
    public void testNonGrantableAnonymous() throws Exception {
        currentToken = UserAuthenticationToken.forUser(null);
        // This is to check that even if the DB has the permissions, they are not granted.
        when(permissionDao.isGrantedToUser(matchesCriteria(Permission.SYS_ADMIN, 1))).thenReturn(true);
        when(permissionDao.isGrantedToUser(matchesCriteria(Permission.ADMIN, 1))).thenReturn(true);

        assertFalse(service.hasGlobalPermission(Permission.SYS_ADMIN), "Anonymous should not have sysadmin permission");
        assertFalse(service.hasGlobalPermission(Permission.ADMIN), "Anonymous should not have admin permission");
    }

    @Test
    public void testRunWithPermissionAnonymous() {
        final UserAuthenticationToken anonAdminToken = currentToken = token(null, Permission.ADMIN);

        assertFalse(service.hasGlobalPermission((IUser) null, Permission.ADMIN),
            "Anonymous should not have permission");
        assertTrue(service.hasGlobalPermission(anonAdminToken, Permission.ADMIN), "Auth token should have admin");
        assertFalse(service.hasGlobalPermission(anonAdminToken, Permission.SYS_ADMIN),
            "Auth token should not have sys_admin");
    }

    @Test
    public void testUserWithLargeNumGroups1() throws Exception {
        final IUser user = createMockUser(1, "name");
        currentToken = UserAuthenticationToken.forUser(user);

        final String grantedGroup = "Group" + PermissionServiceImpl.USER_GROUP_PAGE_LIMIT;

        when(userService.findGroupsByUser(eq("name"), any())).thenAnswer(invocation -> {
            final Pageable pageRequest = (Pageable) invocation.getArguments()[1];
            final List<String> usersGroups = new ArrayList<>();
            for (int i = (int) pageRequest.getOffset(); i < PermissionServiceImpl.USER_GROUP_PAGE_LIMIT + 1; ++i) {
                usersGroups.add("Group" + i);
            }
            return PageUtils.createPage(usersGroups, pageRequest);
        });
        when(permissionDao.isGrantedToGroup(matchesCriteria(Permission.SYS_ADMIN, grantedGroup, null)))
                .thenReturn(true);

        assertFalse(service.hasDirectGlobalUserPermission(Permission.SYS_ADMIN),
            "User should not have permission directly");
        assertTrue(
            service.hasGlobalPermissionThroughGroupMembership(Permission.SYS_ADMIN, Collections.<String> emptySet()),
            "User should have permission through group");
    }

    @Test
    public void testUserWithLargeNumGroups2() throws Exception {
        final IUser user = createMockUser(1, "name");
        currentToken = UserAuthenticationToken.forUser(user);

        final String grantedGroup = "Group" + PermissionServiceImpl.USER_GROUP_PAGE_LIMIT;

        when(userService.findGroupsByUser(eq("name"), any())).thenAnswer(invocation -> {
            final Pageable pageRequest = (Pageable) invocation.getArguments()[1];
            final List<String> usersGroups = new ArrayList<>();
            for (int i = (int) pageRequest.getOffset(); i < PermissionServiceImpl.USER_GROUP_PAGE_LIMIT + 1; ++i) {
                usersGroups.add("Group" + i);
            }
            return PageUtils.createPage(usersGroups, pageRequest);
        });
        when(permissionDao.isGrantedToGroup(matchesCriteria(Permission.SYS_ADMIN, grantedGroup, null)))
                .thenReturn(true);

        assertFalse(service.hasDirectGlobalUserPermission(Permission.SYS_ADMIN),
            "User should not have permission directly");
        assertTrue(
            service.hasGlobalPermissionThroughGroupMembership(Permission.SYS_ADMIN, Collections.<String> emptySet()),
            "User should have permission through group");
    }

    private void mockGroupMemberships(final String userMatcher, final String... groups) {
        when(userService.findGroupsByUser(userMatcher, (Pageable) any())).thenAnswer(
            invocation -> PageUtils.createPage(Arrays.asList(groups), (Pageable) invocation.getArguments()[1]));
    }

    private static GroupPermissionCriteria matchesCriteria(final Permission permission,
        final String group,
        final Integer projectId) {
        return matchesCriteria(permission, Collections.singleton(group), projectId);
    }

    private static GroupPermissionCriteria matchesCriteria(final Permission permission,
        final Collection<String> groups,
        final Integer projectId) {
        return MockitoHamcrest.argThat(new TypeSafeMatcher<GroupPermissionCriteria>() {

            @Override
            public boolean matchesSafely(final GroupPermissionCriteria criteria) {
                if (permission != criteria.getPermission()) {
                    return false;
                }

                // if (ObjectUtils.notEqual(projectId, criteria.getProjectId())) {
                // return false;
                // }

                // use the same logic as the removed matchesCriteria() to match the groups
                if (groups.isEmpty()) {
                    return criteria.getGroups().isEmpty();
                }

                for (final String element : groups) {
                    if (!criteria.getGroups().contains(element)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("GroupPermissionCriteria(")
                        .appendValue(groups)
                        .appendText(", permission")
                        .appendValue(permission)
                        .appendText(", projectId=")
                        .appendValue(projectId)
                        .appendText(")");
            }
        });
    }

    private static UserPermissionCriteria matchesCriteria(final Permission permission, final long userId) {
        return MockitoHamcrest.argThat(new TypeSafeMatcher<UserPermissionCriteria>() {

            @Override
            public boolean matchesSafely(final UserPermissionCriteria criteria) {
                return permission == criteria.getPermission() && userId == criteria.getUserId();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("UserPermissionCriteria(")
                        .appendValue(userId)
                        .appendText(", permission=")
                        .appendValue(permission)
                        .appendText(", projectId=")
                        .appendText(")");
            }
        });
    }

    private IUser createMockUser(final long userId, final Permission... grantedPermissions) {
        for (final Permission permission : grantedPermissions) {
            mockGrantedPermission(userId, permission);
        }

        final IUser user = mock(IUser.class, withSettings().lenient());
        when(user.isActivated()).thenReturn(true);
        when(user.getId()).thenReturn(userId);
        return user;
    }

    private IUser createMockUser(final int userId, final String username, final Permission... grantedPermissions) {
        final IUser user = createMockUser(userId, grantedPermissions);
        when(userService.getUserByName(eq(username))).thenReturn(user);
        when(user.getName()).thenReturn(username);
        when(user.getUsername()).thenReturn(username);
        return user;
    }

    private void mockGrantedPermission(final Long userId, final Permission permission) {
        final IPermissionGraph graph = mockPermissionGraph(userId);

        recordMockGrantedPermission(graph, userId, permission);
        for (final Permission p : permission.getInheritedPermissions()) {
            recordMockGrantedPermission(graph, userId, p);
        }
    }

    private void recordMockGrantedPermission(final IPermissionGraph graph,
        final Long userId,
        final Permission permission) {
        when(permissionDao.isGrantedToUser(matchesCriteria(permission, userId))).thenReturn(true);
        // record the 'any' permission for the requested permission
        when(graph.isGranted(eq(permission), isNull())).thenReturn(true);
        // @formatter:off
        // NOT CHANGE workaround due to bug https://bugs.openjdk.java.net/browse/JDK-8233655
        when(graph.isGranted(eq(permission), MockitoHamcrest.argThat(new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(final Object o) {
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("userId(id =  " + userId + ")");
            }
        }))).thenReturn(true);
        // @formatter:on
    }

    private IPermissionGraph mockPermissionGraph(final Long userId) {
        IPermissionGraph graph = userGraphs.get(userId);
        if (graph == null) {
            graph = mock(IIterablePermissionGraph.class, withSettings().lenient());
            userGraphs.put(userId, graph);
        }
        return graph;
    }

    private IUser createActiveUser(final String name) {
        return User.builder().id(1l).username(name).activated(true).build();
    }

    private IUser createUser(final String name) {
        return User.builder().id(1l).username(name).activated(false).build();
    }

    private UserAuthenticationToken token(final IUser user, final Permission... runAsPermissions) {
        final DefaultPermissionGraph.Builder permBuilder = new DefaultPermissionGraph.Builder();
        for (final Permission permission : runAsPermissions) {
            permBuilder.add(permission, null);
        }
        return UserAuthenticationToken.builder().user(user).elevatedPermissions(permBuilder.build()).build();
    }

}
