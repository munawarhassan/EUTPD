package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.api.paging.PageUtils.newRequest;
import static com.pmi.tpd.security.permission.Permission.ADMIN;
import static com.pmi.tpd.security.permission.Permission.SYS_ADMIN;
import static com.pmi.tpd.security.permission.Permission.USER;
import static com.pmi.tpd.service.testing.mockito.PageAnswer.withPageOf;
import static com.pmi.tpd.service.testing.mockito.PagesAnswer.withPagesUpTo;
import static com.pmi.tpd.testing.hamcrest.PageMatchers.hasSize;
import static com.pmi.tpd.testing.hamcrest.PageMatchers.hasStartPage;
import static com.pmi.tpd.testing.hamcrest.PageMatchers.isLastPage;
import static com.pmi.tpd.testing.hamcrest.PageMatchers.isLastPageOf;
import static com.pmi.tpd.testing.hamcrest.PermittedGroupMatchers.permittedGroup;
import static com.pmi.tpd.testing.hamcrest.PermittedUserMatchers.permittedUser;
import static com.pmi.tpd.testing.hamcrest.UserMatchers.userWithId;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.isNull;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.model.user.PermittedGroup;
import com.pmi.tpd.core.model.user.PermittedUser;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.IPermittedGroup;
import com.pmi.tpd.security.permission.IPermittedUser;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

/**
 * Unit tests for search API in {@link DefaultPermissionAdminService}.
 */
public class DefaultPermissionAdminServiceSearchTest extends AbstractServiceTest {

    private static final Function<Long, IUser> ACTIVE_USER_GENERATOR = DefaultPermissionAdminServiceSearchTest::createActiveUser;

    private static final Function<Long, PermittedUser> PERMITTED_USER_GENERATOR = id -> createActivePermittedUser(id,
        USER);

    // private static final Function<Long, String> GROUP_GENERATOR = id -> groupNameFor(id);

    private static final Function<Long, PermittedGroup> PERMITTED_GROUP_GENERATOR = id -> new PermittedGroup(
            groupNameFor(id), USER.getWeight());

    private static final UserEntity TEST_USER = UserEntity.builder().id(1L).username("Tester").build();

    @Mock(lenient = true)
    private IAuthenticationContext authenticationContext;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private IGlobalPermissionRepository globalPermissionDao;

    @Mock
    private I18nService i18nService;

    @Mock
    private IPermissionService permissionService;

    @Mock
    private IUserService userService;

    @Mock
    private IPermissionValidationService permissionValidationService;

    @InjectMocks
    private DefaultPermissionAdminService permissionAdminService;

    public DefaultPermissionAdminServiceSearchTest() {
        super(DefaultPermissionAdminService.class, IPermissionAdminService.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        when(authenticationContext.getCurrentUser()).thenReturn(Optional.of(TEST_USER));
    }

    @Test
    public void testFindGroupsWithGlobalPermission() {
        when(globalPermissionDao.findHighestPermissionPerGroup(isNull(), any(Pageable.class)))
                .thenAnswer(withPageOf(new PermittedGroup("one", SYS_ADMIN.getWeight()),
                    new PermittedGroup("two", ADMIN.getWeight()),
                    new PermittedGroup("three", USER.getWeight())));

        final Page<IPermittedGroup> groups = permissionAdminService.findGroupsWithGlobalPermission(null,
            newRequest(0, 10));

        assertThat(groups,
            isLastPageOf(permittedGroup("one", SYS_ADMIN),
                permittedGroup("two", ADMIN),
                permittedGroup("three", USER)));
    }

    @Test
    public void testFindGroupsWithGlobalPermissionUsingFilter() {
        when(globalPermissionDao.findHighestPermissionPerGroup(eq("foo"), any(Pageable.class)))
                .thenAnswer(withPageOf(new PermittedGroup("foo", SYS_ADMIN.getWeight()),
                    new PermittedGroup("foobar", ADMIN.getWeight()),
                    new PermittedGroup("foobarbaz", USER.getWeight())));

        final Page<IPermittedGroup> groups = permissionAdminService.findGroupsWithGlobalPermission("foo",
            newRequest(0, 10));

        assertThat(groups,
            isLastPageOf(permittedGroup("foo", SYS_ADMIN),
                permittedGroup("foobar", ADMIN),
                permittedGroup("foobarbaz", USER)));
    }

    @Test
    public void testFindGroupsWithGlobalPermissionValidatesPermission() {
        assertThrows(AuthorisationException.class, () -> {
            doThrow(newAuthorisationException()).when(permissionValidationService).validateForGlobal(ADMIN);
            permissionAdminService.findGroupsWithGlobalPermission(null, newRequest(0, 10));
        });
    }

    @Test
    public void testFindGroupsWithoutGlobalPermission() throws Exception {
        when(globalPermissionDao.findGroupsWithoutPermission(any(Pageable.class)))
                .thenAnswer(withPageOf("abc", "cde", "efg"));

        final Page<String> groups = permissionAdminService.findGroupsWithoutGlobalPermission(newRequest(0, 10));

        // filter out groups with granted permission
        assertThat(groups, isLastPageOf("abc", "cde", "efg"));
    }

    @Test
    public void testFindGroupsWithoutGlobalPermissionValidatesPermission() {
        assertThrows(AuthorisationException.class, () -> {
            doThrow(newAuthorisationException()).when(permissionValidationService).validateForGlobal(ADMIN);
            permissionAdminService.findGroupsWithoutGlobalPermission(newRequest(0, 10));
        });
    }

    @Test
    public void testFindUsersWithGlobalPermission() throws Exception {
        when(globalPermissionDao.findHighestPermissionPerUser(isNull(), any(Pageable.class)))
                .thenAnswer(withPageOf(createActivePermittedUser(1, SYS_ADMIN),
                    createActivePermittedUser(2, ADMIN),
                    createActivePermittedUser(3, USER),
                    createPermittedUser(4L, false, USER)));

        final Page<IPermittedUser> users = permissionAdminService.findUsersWithGlobalPermission(null,
            newRequest(0, 10));

        assertThat(users,
            isLastPageOf(permittedUser(1L, SYS_ADMIN),
                permittedUser(2, ADMIN),
                permittedUser(3, USER),
                permittedUser(4, USER) // inactive users should be included
            ));
    }

    @Test
    public void testFindUsersWithGlobalPermissionUsingFilter() throws Exception {
        when(globalPermissionDao.findHighestPermissionPerUser(eq("user"), any(Pageable.class)))
                .thenAnswer(withPageOf(createActivePermittedUser(1, SYS_ADMIN),
                    createActivePermittedUser(2, ADMIN),
                    createActivePermittedUser(3, USER),
                    createPermittedUser(4, false, USER)));

        final Page<IPermittedUser> users = permissionAdminService.findUsersWithGlobalPermission("user",
            newRequest(0, 10));

        assertThat(users,
            isLastPageOf(permittedUser(1, SYS_ADMIN),
                permittedUser(2, ADMIN),
                permittedUser(3, USER),
                permittedUser(4, USER) // inactive users should be included
            ));
    }

    @Test
    public void testFindUsersWithGlobalPermissionValidatesPermission() {
        assertThrows(AuthorisationException.class, () -> {
            doThrow(newAuthorisationException()).when(permissionValidationService).validateForGlobal(ADMIN);
            permissionAdminService.findUsersWithGlobalPermission(null, newRequest(0, 10));
        });
    }

    @Test
    public void testFindUsersWithoutGlobalPermission() throws Exception {
        when(globalPermissionDao.findUsersWithoutPermission(any(Pageable.class)))
                .thenAnswer(withPageOf(createActiveUser(1), createActiveUser(3), createActiveUser(5)));

        final Page<IUser> users = permissionAdminService.findUsersWithoutGlobalPermission(newRequest(0, 10));

        // filter out users with granted permission
        assertThat(users, isLastPageOf(userWithId(1L), userWithId(3L), userWithId(5L)));
    }

    @Test
    public void testFindUsersWithoutGlobalPermissionValidatesPermission() throws Exception {
        assertThrows(AuthorisationException.class, () -> {
            doThrow(newAuthorisationException()).when(permissionValidationService).validateForGlobal(ADMIN);
            permissionAdminService.findUsersWithoutGlobalPermission(newRequest(0, 10));
        });
    }

    @Test
    public void testSearchGroupsWithPermissionMaxPage() throws Exception {

        when(globalPermissionDao.findHighestPermissionPerGroup(isNull(), any(Pageable.class)))
                .thenAnswer(withPagesUpTo(1000, PERMITTED_GROUP_GENERATOR));

        final Page<IPermittedGroup> groups = permissionAdminService.searchGroups(null, newRequest(1, 200));

        assertThat(groups, not(isLastPage()));
        assertThat(groups, hasStartPage(1));
        assertThat(groups, hasSize(200));
    }

    @Test
    public void testSearchUsersWithPermissionPaging() {
        when(globalPermissionDao.findHighestPermissionPerUser(isNull(), any(Pageable.class)))
                .thenAnswer(withPagesUpTo(25, PERMITTED_USER_GENERATOR));

        Page<IPermittedUser> users = permissionAdminService.searchUsers(null, newRequest(1, 10));

        assertThat(users, not(isLastPage()));
        assertThat(users, hasStartPage(1));
        assertThat(users, hasSize(10));

        users = permissionAdminService.searchUsers(null, users.nextPageable());

        assertThat(users, isLastPage());
        assertThat(users, hasStartPage(2));
        assertThat(users, hasSize(5));
    }

    @Test
    public void testSearchUsersWithPermissionMaxPage() throws Exception {

        when(globalPermissionDao.findHighestPermissionPerUser(isNull(), any(Pageable.class)))
                .thenAnswer(withPagesUpTo(1000, PERMITTED_USER_GENERATOR));

        final Page<IPermittedUser> users = permissionAdminService.searchUsers(null, newRequest(1, 200));

        assertThat(users, not(isLastPage()));
        assertThat(users, hasStartPage(1));
        assertThat(users, hasSize(200));
    }

    @Test
    public void testSearchUsersWithoutPermissionMaxPage() throws Exception {

        when(globalPermissionDao.findUsersWithoutPermission(any(Pageable.class)))
                .thenAnswer(withPagesUpTo(200, ACTIVE_USER_GENERATOR));

        final Page<IUser> users = permissionAdminService.searchUsersLacking(newRequest(0, 100));

        assertThat(users, not(isLastPage()));
        assertThat(users, hasStartPage(0));
        assertThat(users, hasSize(100));
    }

    @Test
    public void testSearchUsersWithoutPermissionPaging() {
        when(globalPermissionDao.findUsersWithoutPermission(any(Pageable.class)))
                .thenAnswer(withPagesUpTo(650, ACTIVE_USER_GENERATOR));

        Page<IUser> users = permissionAdminService.searchUsersLacking(newRequest(20, 30));

        // should result in 50 users spread across 2 pages
        assertThat(users, not(isLastPage()));

        users = permissionAdminService.searchUsersLacking(users.nextPageable());

        assertThat(users, isLastPage());
        assertThat(users, hasStartPage(21));
        assertThat(users, hasSize(20));
    }

    private static PermittedUser createActivePermittedUser(final long i, final Permission permission) {
        return new PermittedUser(createActiveUser(i), permission.getWeight());
    }

    private static UserEntity createActiveUser(final long id) {
        return createUser(id, true);
    }

    @SuppressWarnings("unused")
    private static UserEntity createActiveUser(final long id, final String name) {
        return createUser(id, name, true);
    }

    private static IPermittedUser createPermittedUser(final long id,
        final boolean active,
        final Permission permission) {
        return createPermittedUser(createUser(id, active), permission);
    }

    private static IPermittedUser createPermittedUser(final IUser user, final Permission permission) {
        return new PermittedUser(user, permission.getWeight());
    }

    private static UserEntity createUser(final long id, final boolean active) {
        final String name = "user" + StringUtils.leftPad(Long.toString(id), 4, '0');
        return createUser(id, name, active);
    }

    private static UserEntity createUser(final long i, final String username, final boolean active) {
        return UserEntity.builder()
                .id(i)
                .username(username)
                .email(username + "@example.com")
                .activated(true)
                .displayName(username)
                .build();
    }

    private static String groupNameFor(final long id) {
        return "group" + StringUtils.leftPad(Long.toString(id), 4, '0');
    }

    private static AuthorisationException newAuthorisationException() {
        return new AuthorisationException(new KeyedMessage("test", "Not authorised", "Not authorised"));
    }
}
