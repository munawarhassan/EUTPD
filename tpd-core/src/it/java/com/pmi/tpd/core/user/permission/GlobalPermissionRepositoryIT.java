package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.testing.hamcrest.GrantedPermissionMatcher.groupPermission;
import static com.pmi.tpd.testing.hamcrest.UserMatchers.userIs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.DaoCoreConfig;
import com.pmi.tpd.core.DataSets;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.permission.IPermittedGroup;
import com.pmi.tpd.security.permission.IPermittedUser;

@Configuration
@ContextConfiguration(classes = { DaoCoreConfig.class, GlobalPermissionRepositoryIT.class })
@DatabaseSetup({ DataSets.USERS, DataSets.GLOBAL_PERMISSIONS })
public class GlobalPermissionRepositoryIT extends AbstractPermissionTest {

    @Inject
    private IGlobalPermissionRepository repository;

    @Inject
    private IUserRepository userRepository;

    @Test
    public void testCreate() throws Exception {
        final UserEntity user = userRepository.getById(3L);

        final GlobalPermissionEntity permission = GlobalPermissionEntity.builder()
                .group("trendygroup")
                .permission(Permission.ADMIN)
                .user(user)
                .build();
        final GlobalPermissionEntity created = repository.saveAndFlush(permission);

        assertNotNull(permission.getId());
        assertEquals(permission.getId(), created.getId());
        assertEquals(permission, repository.getById(created.getId()));
    }

    @Test
    public void testDelete() {
        final long permId = 20L;
        final GlobalPermissionEntity permission = repository.getById(permId);
        assertThat(permission, groupPermission(Permission.USER, "special_group"));
        repository.delete(permission);

        assertFalse(repository.findById(permId).isPresent(), "permission should be removed");
    }

    @Test
    public void testDeleteWithRevoke() {
        final long permId = 20L;
        final GlobalPermissionEntity permission = repository.getById(permId);
        assertThat(permission, groupPermission(Permission.USER, "special_group"));
        assertEquals(1L, repository.revoke(permission));

        flush();
        clear();

        assertFalse(repository.findById(permId).isPresent(), "permission should be removed");
    }

    @Test
    public void testRevokeAllForGroup() throws Exception {
        final String groupName = "app-admins";

        // find the number of explicit permissions granted to the group
        final long current = countAllPermissionsFor(groupName);
        assertThat(current, greaterThan(0L));

        // revoke them all
        assertEquals(current, repository.revokeAll(groupName));

        repository.flush();

        // check the deletion was effective
        final long remaining = countAllPermissionsFor(groupName);
        assertEquals(0, remaining);
    }

    @Test
    public void testRevokeAllForUser() throws Exception {
        final long userId = 3;

        // find the number of explicit permissions granted to the user
        final long current = countAllPermissionsFor(userId);
        assertThat(current, greaterThan(0L));

        // revoke them all
        assertEquals(current, repository.revokeAll(userId));

        repository.flush();

        // check the deletion was effective
        final long remaining = countAllPermissionsFor(userId);
        assertEquals(0, remaining);
    }

    @Test
    public void testFindUsersWithPermission() {
        final Page<IUser> users = repository.findUsersWithPermission(PageUtils.newRequest(0, 10));
        assertEquals(5, users.getTotalElements());

        final List<IUser> values = ImmutableList.copyOf(users.getContent());
        assertThat(values.get(0), userIs(10L, "access.key.1"));
        assertThat(values.get(1), userIs(2L, "admin"));
        assertThat(values.get(2), userIs(11L, "optimus.prime"));
        assertThat(values.get(3), userIs(1L, "sysadmin"));
        assertThat(values.get(4), userIs(3L, "user"));
    }

    @Test
    public void testFindHighestPermissionPerUser() throws Exception {
        Page<IPermittedUser> users = repository.findHighestPermissionPerUser(null, PageUtils.newRequest(0, 10));
        assertEquals(5, users.getNumberOfElements());

        List<IPermittedUser> values = ImmutableList.copyOf(users.getContent());
        assertThat(values.get(0).getPermission(), equalTo(Permission.ADMIN));
        assertThat(values.get(0).getUser(), userIs(10L, "access.key.1"));
        assertThat(values.get(1).getPermission(), equalTo(Permission.ADMIN));
        assertThat(values.get(1).getUser(), userIs(2L, "admin"));
        assertThat(values.get(2).getPermission(), equalTo(Permission.SYS_ADMIN));
        assertThat(values.get(2).getUser(), userIs(11L, "optimus.prime"));
        assertThat(values.get(3).getPermission(), equalTo(Permission.SYS_ADMIN));
        assertThat(values.get(3).getUser(), userIs(1L, "sysadmin"));
        assertThat(values.get(4).getPermission(), equalTo(Permission.USER));
        assertThat(values.get(4).getUser(), userIs(3L, "user"));

        // Empty string should be handled the same way
        users = repository.findHighestPermissionPerUser("", PageUtils.newRequest(0, 10));
        assertEquals(5, users.getNumberOfElements());

        values = ImmutableList.copyOf(users.getContent());
        assertThat(values.get(0).getPermission(), equalTo(Permission.ADMIN));
        assertThat(values.get(0).getUser(), userIs(10L, "access.key.1"));
        assertThat(values.get(1).getPermission(), equalTo(Permission.ADMIN));
        assertThat(values.get(1).getUser(), userIs(2L, "admin"));
        assertThat(values.get(2).getPermission(), equalTo(Permission.SYS_ADMIN));
        assertThat(values.get(2).getUser(), userIs(11L, "optimus.prime"));
        assertThat(values.get(3).getPermission(), equalTo(Permission.SYS_ADMIN));
        assertThat(values.get(3).getUser(), userIs(1L, "sysadmin"));
        assertThat(values.get(4).getPermission(), equalTo(Permission.USER));
        assertThat(values.get(4).getUser(), userIs(3L, "user"));
    }

    @Test
    public void testFindHighestPermissionPerUserWithFilter() throws Exception {
        final Page<IPermittedUser> users = repository.findHighestPermissionPerUser("admin",
            PageUtils.newRequest(0, 10));
        assertEquals(2, users.getNumberOfElements());

        final List<IPermittedUser> values = ImmutableList.copyOf(users.getContent());
        assertThat(values.get(0).getPermission(), equalTo(Permission.ADMIN));
        assertThat(values.get(0).getUser(), userIs(2L, "admin"));
        assertThat(values.get(1).getPermission(), equalTo(Permission.SYS_ADMIN));
        assertThat(values.get(1).getUser(), userIs(1L, "sysadmin"));
    }

    @Test
    public void testFindHighestPermissionPerUserWithFilterAndMoreVariations() throws Exception {
        final String[] filters = { "access", // filter should match the start of the display name
                ".1", // filter should match the end of the display name
                ".key.", // filter should match within the display name
                ".KEY.", // filter should match within the display name (case insensitivity)
                ".kEy." // filter should match within the display name (case insensitivity)
        };
        for (final String filter : filters) {
            final Page<IPermittedUser> users = repository.findHighestPermissionPerUser(filter,
                PageUtils.newRequest(0, 10));
            assertEquals(1, users.getTotalElements());

            final List<IPermittedUser> values = ImmutableList.copyOf(users.getContent());
            assertThat(values.get(0).getPermission(), equalTo(Permission.ADMIN));
            assertThat(values.get(0).getUser(), userIs(10L, "access.key.1"));
        }
    }

    @Test
    public void testFindGroupsWithPermission() throws Exception {
        final Page<String> groups = repository.findGroupsWithPermission(PageUtils.newRequest(0, 10));
        assertEquals(4, groups.getNumberOfElements());

        final List<String> values = ImmutableList.copyOf(groups.getContent());
        assertEquals("app-admins", values.get(0));
        assertEquals("app-users", values.get(1));
        assertEquals("group-with-global-and-resource-perms", values.get(2));
        assertEquals("special_group", values.get(3));
    }

    @Test
    public void testFindHighestPermissionPerGroup() throws Exception {
        final Page<IPermittedGroup> groups = repository.findHighestPermissionPerGroup(null,
            PageUtils.newRequest(0, 10));
        assertEquals(4, groups.getTotalElements());

        final List<IPermittedGroup> values = ImmutableList.copyOf(groups.getContent());
        assertThat(values.get(0).getPermission(), equalTo(Permission.SYS_ADMIN));
        assertThat(values.get(0).getGroup(), equalTo("app-admins"));
        assertThat(values.get(1).getPermission(), equalTo(Permission.USER));
        assertThat(values.get(1).getGroup(), equalTo("app-users"));
        assertThat(values.get(2).getPermission(), equalTo(Permission.USER));
        assertThat(values.get(2).getGroup(), equalTo("group-with-global-and-resource-perms"));
        assertThat(values.get(3).getPermission(), equalTo(Permission.USER));
        assertThat(values.get(3).getGroup(), equalTo("special_group"));
    }

    @Test
    public void testFindHighestPermissionPerGroupWithFilter() throws Exception {
        for (final String group : new String[] { "ER", "er", "eR" }) { // verify case insensitivity
            final Page<IPermittedGroup> groups = repository.findHighestPermissionPerGroup(group,
                PageUtils.newRequest(0, 10));
            assertEquals(2, groups.getTotalElements());

            final List<IPermittedGroup> values = ImmutableList.copyOf(groups.getContent());
            assertThat(values.get(0).getPermission(), equalTo(Permission.USER));
            assertThat(values.get(0).getGroup(), equalTo("app-users"));
            assertThat(values.get(1).getPermission(), equalTo(Permission.USER));
            assertThat(values.get(1).getGroup(), equalTo("group-with-global-and-resource-perms"));
        }
    }

    @Test
    public void testFindHighestPermissionPerGroupWithFilterAndMoreVariations() throws Exception {
        final String[] filters = { "group-with", // filter should match the start of the group name
                "perms", // filter should match the end of the group name
                "global-and-resource", // filter should match within the group name
                "WITH", // filter should match within the group name (case insensitivity)
                "WiTH" // filter should match within the group name (case insensitivity)
        };
        for (final String filter : filters) {
            final Page<IPermittedGroup> groups = repository.findHighestPermissionPerGroup(filter,
                PageUtils.newRequest(0, 10));
            assertEquals(1, groups.getTotalElements());

            final List<IPermittedGroup> values = ImmutableList.copyOf(groups.getContent());
            assertThat(values.get(0).getPermission(), equalTo(Permission.USER));
            assertThat(values.get(0).getGroup(), equalTo("group-with-global-and-resource-perms"));
        }
    }

    @Test
    public void testHasUserPermission() throws Exception {
        final UserEntity user = userRepository.findByName("user");
        assertFalse(repository
                .hasPermissionEntry(GlobalPermissionEntity.builder().permission(Permission.ADMIN).user(user).build()));
        assertTrue(repository
                .hasPermissionEntry(GlobalPermissionEntity.builder().permission(Permission.USER).user(user).build()));
    }

    @Test
    public void testHasGroupPermission() throws Exception {
        assertFalse(repository.hasPermissionEntry(
            GlobalPermissionEntity.builder().permission(Permission.ADMIN).group("special_group").build()));
        assertTrue(repository.hasPermissionEntry(
            GlobalPermissionEntity.builder().permission(Permission.USER).group("special_group").build()));
    }

    private long countAllPermissionsFor(final String groupName) {
        return repository.findAll(repository.entity().group.eq(groupName), PageUtils.newRequest(0, 50))
                .getTotalElements();
    }

    private long countAllPermissionsFor(final long userId) {
        return repository.findAll(repository.entity().user.id.eq(userId), PageUtils.newRequest(0, 50))
                .getTotalElements();
    }

}
