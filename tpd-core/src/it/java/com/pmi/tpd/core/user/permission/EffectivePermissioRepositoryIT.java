package com.pmi.tpd.core.user.permission;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.DaoCoreConfig;
import com.pmi.tpd.core.DataSets;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.security.permission.Permission;

@Configuration
@ContextConfiguration(classes = { DaoCoreConfig.class, EffectivePermissioRepositoryIT.class })
@DatabaseSetup(value = { DataSets.USERS, DataSets.GLOBAL_PERMISSIONS })
public class EffectivePermissioRepositoryIT extends AbstractPermissionTest {

    @Inject
    private IEffectivePermissionRepository repository;

    @Test
    public void testFindByGroupNoPermissions() {
        final Pageable pageRequest = PageUtils.newRequest(0, 10);
        final Page<GrantedPermission> page = repository.findByGroup("unknown", pageRequest);

        assertNotNull(page);
        assertTrue(page.isLast());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    public void testFindByGroup() {
        for (final String group : new String[] { "app-admins", "APP-admins" }) { // verify case insentitivity
            final List<GrantedPermission> expectedPage1 = Lists.newArrayList(repository.getById(22L), // USER for
                                                                                                      // 'app-admins'
                repository.getById(23L) // ADMIN for 'app-admins'
            );

            final List<GrantedPermission> expectedPage2 = Lists.newArrayList(repository.getById(24L)); // SYS_ADMIN for
                                                                                                       // 'app-admins'

            // fetch the first page
            final Pageable pageRequest = PageUtils.newRequest(0, 2);
            Page<GrantedPermission> page = repository.findByGroup(group, pageRequest);

            // verify page 1
            assertNotNull(page);
            assertEquals(2, page.getNumberOfElements());
            assertFalse(page.isLast());
            assertEquals(expectedPage1, Lists.newArrayList(page.getContent()));

            // fetch page 2
            page = repository.findByGroup(group, page.nextPageable());

            // verify page 2
            assertNotNull(page);
            assertEquals(1, page.getNumberOfElements());
            assertTrue(page.isLast());
            assertEquals(expectedPage2, Lists.newArrayList(page.getContent()));
        }
    }

    @Test
    public void testFindByUserId() {
        // USER permission
        final GrantedPermission expected = repository.getById(6L); // user user with USER permission
        final Page<GrantedPermission> page = repository.findByUserId(3L, PageUtils.newRequest(0, 1));

        assertNotNull(page);
        assertTrue(page.isLast());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(expected, Iterables.getFirst(page.getContent(), null));

    }

    @Test
    public void testFindByUserIdNoPermissions() {
        final Pageable pageRequest = PageUtils.newRequest(0, 10);
        final Page<GrantedPermission> page = repository.findByUserId(99999L, pageRequest);

        assertNotNull(page);
        assertTrue(page.isLast());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    public void testFindGroupsWithPermission() {
        final Pageable pageRequest = PageUtils.newRequest(0, 10);
        assertGroups(repository.findGroups(Permission.SYS_ADMIN, pageRequest), "app-admins");
        assertGroups(repository.findGroups(Permission.ADMIN, pageRequest), "app-admins");
        assertGroups(repository.findGroups(Permission.USER,
            pageRequest), "app-admins", "app-users", "group-with-global-and-resource-perms", "special_group");
    }

    @Test
    public void testFindUsersWithPermission() {
        final PageRequest pageRequest = (PageRequest) PageUtils.newRequest(0, 10);
        assertUsers(repository.findUsers(Permission.SYS_ADMIN, pageRequest, null), 11L, 1L);
        assertUsers(repository.findUsers(Permission.ADMIN, pageRequest, null), 10L, 2L, 11L, 1L);
        assertUsers(repository.findUsers(Permission.USER, pageRequest, null), 10L, 2L, 11L, 1L, 3L);
    }

    @Test
    public void testIsGrantedForAdmins() throws Exception {
        final Table<Permission, Object, Boolean> tests = createTestTable(true);

        testIsGranted("app-admins", tests); // app-admins is a sysadmin group
        testIsGranted("app-ADMINS", tests); // test should be case insensitive
        testIsGranted(1L, tests); // user 1 is a sysadmin

        tests.put(Permission.SYS_ADMIN, NULL, false);
        testIsGranted(2L, tests); // user 2 is an admin
    }

    private Table<Permission, Object, Boolean> createTestTable(final boolean defaultValue) {
        final Table<Permission, Object, Boolean> tests = HashBasedTable.create();
        for (final Permission permission : Permission.values()) {
            // if (permission.isResource(Project.class)) {
            // for (final Project project : new Project[] { project1, project2, project3,
            // project4 }) {
            // tests.put(permission, project, defaultValue);
            // }
            // } else if (permission.isResource(Repository.class)) {
            // for (final Repository repository : new Repository[] { repository1,
            // repository2, repository3,
            // repository4, repository5 }) {
            // tests.put(permission, repository, defaultValue);
            // }
            // }
            tests.put(permission, NULL, defaultValue);
        }
        return tests;
    }

    private void testIsGranted(final Long userId, final Table<Permission, Object, Boolean> tests) {
        assertEquals(3, tests.size(), "there are missing combinations:"); // 4 global perms + 4 project perms x (4
                                                                          // projects + 1 null) + 3 repo perms x (5
                                                                          // repositories + 1 null)
        for (final Table.Cell<Permission, Object, Boolean> test : tests.cellSet()) {
            final Permission permission = test.getRowKey();
            final Object resource = test.getColumnKey() == NULL ? null : test.getColumnKey();
            final boolean isGranted = test.getValue();

            final UserPermissionCriteria criteria = new UserPermissionCriteria.Builder(userId).permission(permission)
                    .resource(resource)
                    .build();
            assertEquals(isGranted,
                repository.isGrantedToUser(criteria),
                () -> String.format("for user: %d, permission: %s and resource: %s", userId, permission, resource));
        }
    }

    private void testIsGranted(final String group, final Table<Permission, Object, Boolean> tests) {
        assertEquals(3, tests.size(), "there are missing combinations:");
        for (final Table.Cell<Permission, Object, Boolean> test : tests.cellSet()) {
            final Permission permission = test.getRowKey();
            final Object resource = test.getColumnKey() == NULL ? null : test.getColumnKey();
            final boolean isGranted = test.getValue();

            final GroupPermissionCriteria criteria = new GroupPermissionCriteria.Builder(group).permission(permission)
                    .resource(resource)
                    .build();
            assertEquals(isGranted,
                repository.isGrantedToGroup(criteria),
                () -> String.format("for group: %s, permission: %s and resource: %s", group, permission, resource));
        }
    }

    // guava's tables don't accept null
    private static Object NULL = new Object();

}
