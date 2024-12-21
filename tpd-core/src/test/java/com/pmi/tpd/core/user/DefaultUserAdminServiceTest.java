package com.pmi.tpd.core.user;

import static com.pmi.tpd.api.paging.PageUtils.newRequest;
import static com.pmi.tpd.service.testing.mockito.PageAnswer.withPageOf;
import static com.pmi.tpd.testing.hamcrest.PageableMatcher.pageable;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.event.TestEventPublisher;
import com.pmi.tpd.core.event.user.GroupCleanupEvent;
import com.pmi.tpd.core.event.user.GroupDeletedEvent;
import com.pmi.tpd.core.event.user.UserCleanupEvent;
import com.pmi.tpd.core.event.user.UserDeletedEvent;
import com.pmi.tpd.core.exception.InvalidTokenException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.security.OperationType;
import com.pmi.tpd.core.security.provider.DefaultDirectory;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;
import com.pmi.tpd.service.testing.mockito.PageAnswer;
import com.pmi.tpd.testing.hamcrest.TimeMatchers;

@SuppressWarnings({ "unchecked" })
public class DefaultUserAdminServiceTest extends AbstractServiceTest {

    /** */
    private static final Answer<Object> RETURN_FIRST_ARGUMENT = invocation -> invocation.getArguments()[0];

    @Mock(lenient = true)
    private IAuthenticationService authenticationProviderService;

    @Mock
    private IUserService userService;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IEmailNotifier emailNotifier;

    @Spy
    private final TestEventPublisher eventPublisher = new TestEventPublisher();

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    private IPermissionAdminService permissionAdminService;

    @Mock
    private IPasswordResetHelper passwordHelper;

    @InjectMocks
    private DefaultUserAdminService userAdminService;

    @Mock
    private PersistentTokenRepository tokenRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private IUserRepository userRepository;

    public DefaultUserAdminServiceTest() {
        super(DefaultUserAdminService.class, IUserAdminService.class);
    }

    @BeforeEach
    public void onSetup() {
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
    }

    @AfterEach
    public void onTearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testAddUserToMultipleGroups() {
        final UserEntity user = mock(UserEntity.class);
        final GroupEntity groupA = mock(GroupEntity.class), groupB = mock(GroupEntity.class);

        when(groupRepository.findByName(eq("groupA"))).thenReturn(groupA);
        when(groupRepository.findByName(eq("groupB"))).thenReturn(groupB);
        when(userRepository.findByName(eq("user"))).thenReturn(user);

        userAdminService.addUserToGroups("user", ImmutableSet.of("groupA", "groupB"));

        verify(userRepository).addGroupMember(same(groupA), same(user));
        verify(userRepository).addGroupMember(same(groupB), same(user));
        verify(permissionAdminService).canAddUserToGroup(eq("groupA"));
        verify(permissionAdminService).canAddUserToGroup(eq("groupB"));
    }

    @Test
    public void testAddMultipleUsersToGroup() {
        final UserEntity user1 = mock(UserEntity.class), user2 = mock(UserEntity.class);
        final GroupEntity group = mock(GroupEntity.class);

        when(groupRepository.findByName(eq("group"))).thenReturn(group);
        when(userRepository.findByName(eq("user1"))).thenReturn(user1);
        when(userRepository.findByName(eq("user2"))).thenReturn(user2);

        userAdminService.addMembersToGroup("group", ImmutableSet.of("user1", "user2"));

        verify(userRepository).addGroupMember(same(group), same(user1));
        verify(userRepository).addGroupMember(same(group), same(user2));
        verify(permissionAdminService, times(2)).canAddUserToGroup(eq("group"));
    }

    @Test
    public void testCanCreateGroups() {
        final IDirectory directory = mock(IDirectory.class);
        when(directory.getAllowedOperations()).thenReturn(EnumSet.noneOf(OperationType.class),
            EnumSet.of(OperationType.CREATE_GROUP));
        when(directory.isActive()).thenReturn(false, true);

        // Three returns:
        // 1. Not active (isActive()'s first return)
        // 2. Active, no allowed operations (getAllowedOperations() first return)
        // 3. Active, allows operation
        when(authenticationProviderService.listDirectories())
                .thenReturn(Arrays.asList(directory, directory, directory));

        assertTrue(userAdminService.canCreateGroups());
    }

    @Test
    public void testCanCreateUsers() {
        final IDirectory directory = mock(IDirectory.class);
        when(directory.getAllowedOperations()).thenReturn(EnumSet.noneOf(OperationType.class),
            EnumSet.of(OperationType.CREATE_USER));
        when(directory.isActive()).thenReturn(false, true);

        // Three returns:
        // 1. Not active (isActive()'s first return)
        // 2. Active, no allowed operations (getAllowedOperations() first return)
        // 3. Active, allows operation
        when(authenticationProviderService.listDirectories())
                .thenReturn(Arrays.asList(directory, directory, directory));

        assertTrue(userAdminService.canCreateUsers());
    }

    @Test
    public void testCanDeleteGroups() {
        final IDirectory directory = mock(IDirectory.class);
        when(directory.getAllowedOperations()).thenReturn(EnumSet.noneOf(OperationType.class),
            EnumSet.of(OperationType.DELETE_GROUP));
        when(directory.isActive()).thenReturn(false, true);

        // Three returns:
        // 1. Not active (isActive()'s first return)
        // 2. Active, no allowed operations (getAllowedOperations() first return)
        // 3. Active, allows operation
        when(authenticationProviderService.listDirectories())
                .thenReturn(Arrays.asList(directory, directory, directory));

        assertTrue(userAdminService.canDeleteGroups());
    }

    @Test
    public void testCreateGroup() {
        final ArgumentCaptor<GroupEntity> groupCaptor = ArgumentCaptor.forClass(GroupEntity.class);

        when(groupRepository.save(isA(GroupEntity.class))).thenAnswer(RETURN_FIRST_ARGUMENT);
        when(authenticationProviderService.listDirectories()).thenReturn(Collections.<IDirectory> emptyList());

        final GroupRequest group = userAdminService.createGroup("group");
        assertNotNull(group);
        assertEquals("group", group.getName());
        assertFalse(group.isDeletable());

        verify(groupRepository).save(groupCaptor.capture());
        assertEquals("group", groupCaptor.getValue().getName());
    }

    @Test
    public void testCreateGroupWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createGroup(null);
        });
    }

    @Test
    public void testCreateUser() throws Exception {
        when(authenticationProviderService.findDirectoryFor(eq(UserDirectory.Internal)))
                .thenReturn(DefaultDirectory.INTERNAL);
        when(userService.createUser(isA(UserRequest.class), eq(true), eq(true))).thenAnswer(RETURN_FIRST_ARGUMENT);

        final UserRequest user = userAdminService.createUser("user", "password", "firstname lastname", "emailAddress");

        verify(userService).createUser(isA(UserRequest.class), eq(true), eq(true));

        assertNotNull(user);
        assertEquals("user", user.getUsername());
        assertEquals("firstname lastname", user.getDisplayName());
        assertEquals("emailAddress", user.getEmail());
    }

    @Test
    public void testCreateUserWithBlankEmailAddress() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUser("user", "password", "firstname lastname", "\n\t");
        });
    }

    @Test
    public void testCreateUserWithBlankPassword() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            when(authenticationProviderService.findDirectoryFor(eq(UserDirectory.Internal)))
                    .thenReturn(DefaultDirectory.INTERNAL);
            userAdminService.createUser("user", "\n\t ", "firstname lastname", "emailAddress");
        });
    }

    @Test
    public void testCreateUserWithGeneratedPassword() throws Exception {
        final ArgumentCaptor<UserRequest> userCaptor = ArgumentCaptor.forClass(UserRequest.class);

        when(userService.createUser(isA(UserRequest.class), eq(true), eq(true))).thenAnswer(RETURN_FIRST_ARGUMENT);
        when(authenticationProviderService.canResetPassword(eq("user"))).thenReturn(true);
        when(passwordHelper.addResetPasswordToken(eq("user"))).thenReturn("token");
        when(passwordHelper.generatePassword()).thenReturn("password");

        userAdminService
                .createUserWithGeneratedPassword("user", "firstname lastname", "emailAddress", UserDirectory.Internal);

        verify(userService).createUser(userCaptor.capture(), eq(true), eq(true));

        final UserRequest user = userCaptor.getValue();
        assertNotNull(user);
        assertEquals("user", user.getUsername());
        assertEquals("firstname lastname", user.getDisplayName());
        assertEquals("emailAddress", user.getEmail());

        verify(emailNotifier).validateCanSendEmails();
        verify(emailNotifier).sendCreatedUser(same(user), eq("token"));
        verify(passwordHelper).generatePassword();
        verify(passwordHelper).addResetPasswordToken(eq("user"));
    }

    @Test
    public void testCreateUserWithGeneratedPasswordWhenUserCannotUpdateIt() throws Exception {
        assertThrows(IntegrityException.class, () -> {
            when(userService.createUser(isA(UserRequest.class), eq(true), eq(true))).thenAnswer(RETURN_FIRST_ARGUMENT);
            when(authenticationProviderService.canResetPassword(eq("user"))).thenReturn(false);
            userAdminService.createUserWithGeneratedPassword("user",
                "firstname lastname",
                "emailAddress",
                UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithGeneratedPasswordAndNoMailHostConfigured() throws Exception {
        assertThrows(NoMailHostConfigurationException.class, () -> {
            doThrow(NoMailHostConfigurationException.class).when(emailNotifier).validateCanSendEmails();

            userAdminService.createUserWithGeneratedPassword("user",
                "firstname lastname",
                "emailAddress",
                UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithGeneratedPasswordAndBlankDisplayName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUserWithGeneratedPassword("user", "\n\t   ", "emailAddress", UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithGeneratedPasswordAndBlankEmailAddress() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService
                    .createUserWithGeneratedPassword("user", "firstname lastname", "\n\t ", UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithGeneratedPasswordAndNullDisplayName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUserWithGeneratedPassword("user", null, "emailAddress", UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithGeneratedPasswordAndNullEmailAddress() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService
                    .createUserWithGeneratedPassword("user", "firstname lastname", null, UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithGeneratedPasswordAndNullName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUserWithGeneratedPassword(null,
                "firstname lastname",
                "emailAddress",
                UserDirectory.Internal);
        });
    }

    @Test
    public void testCreateUserWithNullDisplayName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUser("user", "password", null, "emailAddress");
        });
    }

    @Test
    public void testCreateUserWithNullEmailAddress() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUser("user", "password", "firstname lastname", null);
        });
    }

    @Test
    public void testCreateUserWithNullName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.createUser(null, "password", "firstname lastname", "emailAddress");
        });
    }

    @Test
    public void testCreateUserWithNullPassword() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            when(authenticationProviderService.findDirectoryFor(eq(UserDirectory.Internal)))
                    .thenReturn(DefaultDirectory.INTERNAL);
            userAdminService.createUser("user", null, "firstname lastname", "emailAddress");
        });

    }

    @Test
    public void testDeleteGroup() {
        final GroupEntity group = GroupEntity.builder().name("group").build();

        when(groupRepository.findByName(eq("group"))).thenReturn(group);

        final GroupRequest actual = userAdminService.deleteGroup("group");
        assertNotNull(actual);
        assertEquals("group", actual.getName());
        assertTrue(actual.isDeletable());

        verify(groupRepository).delete(same(group));
        verify(groupRepository).findByName(eq("group"));
        verify(permissionAdminService).canDeleteGroup(eq("group"));
    }

    @Test
    public void testDeleteGroupWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.deleteGroup(null);
        });
    }

    @Test
    public void testDeleteUser() {
        final UserEntity user = UserEntity.builder().username("user").build();
        final UserRequest.Builder userRequest = UserRequest.builder();
        final IDirectory directory = mock(IDirectory.class);

        when(directory.isGroupUpdatable()).thenReturn(true);
        when(directory.isUserDeletable()).thenReturn(true);
        when(directory.isUserUpdatable()).thenReturn(true);

        when(userRepository.findByName(eq("user"))).thenReturn(user);
        when(userService.toUserRequest(eq(user))).thenReturn(userRequest);
        when(authenticationProviderService.findDirectoryFor(eq(user))).thenReturn(directory);

        final UserRequest actual = userAdminService.deleteUser("user");
        assertNotNull(actual);
        assertEquals(true, actual.isDeletable());
        assertEquals(true, actual.isUpdatable());
        assertEquals(true, actual.isGroupUpdatable());

        verify(authenticationProviderService).findDirectoryFor(eq(user));

        verify(userRepository).delete(same(user));
        verify(userRepository).findByName(eq("user"));
        verify(permissionAdminService).canDeleteUser(eq(user));
    }

    @Test
    public void testDeleteUserWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.deleteUser(null);
        });

    }

    @Test
    public void testFindGroups() {
        final Page<GroupRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = mock(Pageable.class);
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Function<GroupEntity, GroupRequest>> transformCaptor = (ArgumentCaptor) ArgumentCaptor
                .forClass(Function.class);

        final IDirectory directory = mock(IDirectory.class, withSettings().lenient());
        when(directory.getAllowedOperations()).thenReturn(EnumSet.of(OperationType.DELETE_GROUP));
        when(directory.isActive()).thenReturn(true);

        final Page<GroupEntity> groupPage = mock(Page.class);
        when(groupPage.map(isA(Function.class))).thenReturn(detailPage);

        when(groupRepository.findGroups(same(pageRequest))).thenReturn(groupPage);

        assertSame(detailPage, userAdminService.findGroups(pageRequest));

        verify(groupRepository).findGroups(same(pageRequest));
        verify(groupPage).map(transformCaptor.capture());

        final Function<GroupEntity, GroupRequest> transform = transformCaptor.getValue();
        final GroupRequest group = transform.apply(GroupEntity.builder().name("group").build());
        assertEquals("group", group.getName());
        assertFalse(group.isDeletable());
    }

    @Test
    public void testFindGroupsByName() {
        final Page<GroupRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = mock(Pageable.class);
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Function<GroupEntity, GroupRequest>> transformCaptor = (ArgumentCaptor) ArgumentCaptor
                .forClass(Function.class);

        final Page<GroupEntity> groupPage = mock(Page.class);
        when(groupPage.map(isA(Function.class))).thenReturn(detailPage);

        when(groupRepository.findGroupsByName(eq("group"), same(pageRequest))).thenReturn(groupPage);

        assertSame(detailPage, userAdminService.findGroupsByName("group", pageRequest));

        verify(groupRepository).findGroupsByName(eq("group"), same(pageRequest));
        verify(groupPage).map(transformCaptor.capture());

        final Function<GroupEntity, GroupRequest> transform = transformCaptor.getValue();
        final GroupRequest group = transform.apply(GroupEntity.builder().name("group").build());
        assertEquals("group", group.getName());
        assertFalse(group.isDeletable());
    }

    @Test
    public void testFindGroupsWithUser() {
        final Page<GroupRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = mock(Pageable.class);
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Function<GroupEntity, GroupRequest>> transformCaptor = (ArgumentCaptor) ArgumentCaptor
                .forClass(Function.class);

        final Page<GroupEntity> groupPage = mock(Page.class);
        when(groupPage.map(isA(Function.class))).thenReturn(detailPage);

        when(groupRepository.findGroupsByUser(eq("user"), eq("group"), same(pageRequest))).thenReturn(groupPage);

        assertSame(detailPage, userAdminService.findGroupsWithUser("user", "group", pageRequest));

        verify(groupRepository).findGroupsByUser(eq("user"), eq("group"), same(pageRequest));
        verify(groupPage).map(transformCaptor.capture());

        final Function<GroupEntity, GroupRequest> transform = transformCaptor.getValue();
        final GroupRequest group = transform.apply(GroupEntity.builder().name("group").build());
        assertEquals("group", group.getName());
        assertFalse(group.isDeletable());
    }

    @Test
    public void testFindGroupsWithoutUser() {
        final Page<GroupRequest> detailPage = mock(Page.class);
        final IDirectory directory = mock(IDirectory.class);
        final Pageable pageRequest = mock(Pageable.class);
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Function<GroupEntity, GroupRequest>> transformCaptor = (ArgumentCaptor) ArgumentCaptor
                .forClass(Function.class);

        final Page<GroupEntity> groupPage = mock(Page.class);

        when(groupPage.map(any(Function.class))).thenReturn(detailPage);
        when(groupRepository.findGroupsWithoutUser(eq("user"), eq("group"), same(pageRequest))).thenReturn(groupPage);
        when(authenticationProviderService.listDirectories()).thenReturn(Collections.singletonList(directory));

        assertSame(detailPage, userAdminService.findGroupsWithoutUser("user", "group", pageRequest));

        verify(groupRepository).findGroupsWithoutUser(eq("user"), eq("group"), same(pageRequest));
        verify(authenticationProviderService).listDirectories();
        verify(groupPage).map(transformCaptor.capture());

        final Function<GroupEntity, GroupRequest> transform = transformCaptor.getValue();
        final GroupRequest group = transform.apply(GroupEntity.builder().name("group").build());
        assertEquals("group", group.getName());
        assertFalse(group.isDeletable());
    }

    @Test
    public void testFindUserByPasswordResetToken() {
        final UserEntity user = UserEntity.builder().username("user").build();

        final IDirectory directory = mock(IDirectory.class);
        when(directory.isGroupUpdatable()).thenReturn(false);
        when(directory.isUserDeletable()).thenReturn(false);
        when(directory.isUserUpdatable()).thenReturn(true);
        when(directory.getName()).thenReturn("directory");

        final UserRequest.Builder userRequest = UserRequest.builder();
        when(userService.toUserRequest(eq(user))).thenReturn(userRequest);

        when(authenticationProviderService.findDirectoryFor(same(user))).thenReturn(directory);
        when(passwordHelper.findUserByResetToken(eq("token"))).thenReturn(Optional.of(user));

        final UserRequest actual = userAdminService.findUserByPasswordResetToken("token");

        assertNotNull(actual);
        assertEquals("directory", actual.getDirectoryName());
        assertFalse(actual.isDeletable());
    }

    @Test
    public void testFindUserByPasswordResetTokenWhenNotFound() {
        assertNull(userAdminService.findUserByPasswordResetToken("token"));
    }

    @Test
    public void testFindUsers() {
        final Page<UserRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = newRequest(0, 200);
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Function<IUser, UserRequest>> transformCaptor = (ArgumentCaptor) ArgumentCaptor
                .forClass(Function.class);
        final Page<UserEntity> userPage = mock(Page.class);

        when(userPage.map(any(Function.class))).thenReturn(detailPage);

        when(userRepository.findUsers(pageRequest)).thenReturn(userPage);

        assertSame(detailPage, userAdminService.findUsers(pageRequest));

        verify(userRepository).findUsers(any(Pageable.class));
        verify(userPage).map(transformCaptor.capture());

        // --------------------------------------------------------------------------
        // Begin the second half of the test, covering how the transform works.
        // This will not be duplicated in the other findUsers* tests, for brevity
        // --------------------------------------------------------------------------
        reset(userRepository);

        final IDirectory directory = mock(IDirectory.class);

        when(directory.isGroupUpdatable()).thenReturn(false);
        when(directory.isUserDeletable()).thenReturn(true);
        when(directory.isUserUpdatable()).thenReturn(true);
        when(directory.getName()).thenReturn("directory");
        when(userService.toUserRequest(any(UserEntity.class))).thenReturn(UserRequest.builder());

        final UserEntity userEntity = UserEntity.builder().username("user").build();

        when(authenticationProviderService.findDirectoryFor(same(userEntity))).thenReturn(directory);

        final Function<IUser, UserRequest> transform = transformCaptor.getValue();
        final UserRequest actual = transform.apply(userEntity);
        assertEquals("directory", actual.getDirectoryName());
        assertTrue(actual.isDeletable());

        verify(authenticationProviderService).findDirectoryFor(same(userEntity));
        verify(directory).getName();
    }

    @Test
    public void testFindUsersByName() {
        final Page<UserRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = newRequest(0, 200);
        final Page<UserEntity> userPage = mock(Page.class);

        when(userPage.map(isA(Function.class))).thenReturn(detailPage);

        when(userRepository.findByName(eq("user"), eq(newRequest(0, 200)))).thenReturn(userPage);

        assertSame(detailPage, userAdminService.findUsersByName("user", pageRequest));

        verify(userRepository).findByName(eq("user"), any(Pageable.class));
        verify(userPage).map(isA(Function.class));
    }

    @Test
    public void testFindUsersWithGroup() {
        final Page<UserRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = newRequest(0, 200);
        final Page<UserEntity> userPage = mock(Page.class);

        when(userPage.map(isA(Function.class))).thenReturn(detailPage);

        when(userRepository.findUsersWithGroup(eq("group"), eq(newRequest(0, 200)))).thenReturn(userPage);

        assertSame(detailPage, userAdminService.findUsersWithGroup("group", pageRequest));

        verify(userRepository).findUsersWithGroup(eq("group"), any(Pageable.class));
        verify(userPage).map(isA(Function.class));
    }

    @Test
    public void testFindUsersWithoutGroup() {
        final Page<UserRequest> detailPage = mock(Page.class);
        final Pageable pageRequest = newRequest(0, 200);
        final Page<UserEntity> userPage = mock(Page.class);

        when(userPage.map(isA(Function.class))).thenReturn(detailPage);

        when(userRepository.findUsersWithoutGroup(eq("group"), eq(newRequest(0, 200)))).thenReturn(userPage);

        assertSame(detailPage, userAdminService.findUsersWithoutGroup("group", pageRequest));

        verify(userRepository).findUsersWithoutGroup(eq("group"), any(Pageable.class));
        verify(userPage).map(isA(Function.class));
    }

    @Test
    public void testGetUserDetailsByName() {

        final IDirectory directory = mock(IDirectory.class);
        when(directory.isGroupUpdatable()).thenReturn(true);
        when(directory.isUserDeletable()).thenReturn(false);
        when(directory.isUserUpdatable()).thenReturn(false);
        when(directory.getName()).thenReturn("directory");

        final UserEntity user = UserEntity.builder().username("user").build();

        when(authenticationProviderService.findDirectoryFor(same(user))).thenReturn(directory);
        when(authenticationProviderService.findUser(eq("user"), anyBoolean())).thenReturn(user);
        when(userRepository.findByName(eq("user"))).thenReturn(user);
        when(userService.toUserRequest(eq(user))).thenReturn(UserRequest.builder());

        final UserRequest actual = userAdminService.getUserDetails("user");
        assertNotNull(actual);
        assertEquals("directory", actual.getDirectoryName());
        verify(userService).toUserRequest(same(user));
    }

    @Test
    public void testGetUserDetailsByNameWhenDirectoryNotFound() {
        final UserEntity user = mock(UserEntity.class);

        when(authenticationProviderService.findUser(eq("user"), anyBoolean())).thenReturn(user);
        when(userRepository.findByName(eq("user"))).thenReturn(user);
        when(userService.toUserRequest(eq(user))).thenReturn(UserRequest.builder());

        final UserRequest actual = userAdminService.getUserDetails("user");
        assertNotNull(actual);

        verify(userService).toUserRequest(same(user));
    }

    @Test
    public void testGetUserDetailsByNameWhenUserNotFound() {
        assertThrows(NoSuchUserException.class, () -> {
            userAdminService.getUserDetails("user");
        });
    }

    @Test
    public void testGetUserDetailsByUser() {
        final UserEntity user = UserEntity.builder().build();

        final IUser initialUser = mock(IUser.class);
        when(initialUser.getName()).thenReturn("user");

        when(authenticationProviderService.findUser(eq("user"), anyBoolean())).thenReturn(user);
        when(userRepository.findByName(eq("user"))).thenReturn(user);
        when(userService.toUserRequest(eq(user))).thenReturn(UserRequest.builder());

        final UserRequest actual = userAdminService.getUserDetails(initialUser);

        assertNotNull(actual);
        assertNull(actual.getDirectoryName());

        // Verify that the provided user is only used to get a name, if it's not an UserEntity
        verify(initialUser).getName();
        verifyNoMoreInteractions(initialUser);

        verify(authenticationProviderService).findDirectoryFor(same(user));
        // replace by userservice temporary
        // verify(authenticationProviderService).findUser(eq("user"), eq(true));
        verify(userRepository).findByName(eq("user"));
        verify(userService).toUserRequest(same(user));
    }

    @Test
    public void testGetUserDetailsByUserForUserEntity() {

        final IDirectory directory = mock(IDirectory.class);
        when(directory.isGroupUpdatable()).thenReturn(true);
        when(directory.isUserDeletable()).thenReturn(false);
        when(directory.isUserUpdatable()).thenReturn(false);
        when(directory.getName()).thenReturn("directory");

        final UserEntity userEntity = UserEntity.builder().username("user").build();

        when(authenticationProviderService.findDirectoryFor(same(userEntity))).thenReturn(directory);
        when(userService.toUserRequest(eq(userEntity))).thenReturn(UserRequest.builder());

        final UserRequest actual = userAdminService.getUserDetails(userEntity);
        assertNotNull(actual);
        assertEquals("directory", actual.getDirectoryName());

        verify(authenticationProviderService).findDirectoryFor(same(userEntity));
        // Should not have called findUser; it had the Crowd user already
        verify(authenticationProviderService, never()).findUser(anyString(), anyBoolean());
    }

    @Test
    public void testGetUserDetailsByUserWhenNotFound() {
        assertThrows(NoSuchUserException.class, () -> {
            final IUser initialUser = mock(IUser.class);
            when(initialUser.getName()).thenReturn("user");

            try {
                userAdminService.getUserDetails(initialUser);
            } finally {
                verify(initialUser, times(1)).getName();
            }
        });
    }

    @Test
    public void testGetUserDetailsByUserWithNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.getUserDetails((IUser) null);
        });
    }

    @Test
    public void testOnGroupDeletedFromAllDirectories() {
        final String name = "bluegrass";

        final Long deletedGroupId = 123456L;
        final GroupEntity existing = spy(
            GroupEntity.builder().name(name).directory(UserDirectory.InternalLdap).build());
        when(existing.getId()).thenReturn(deletedGroupId);

        when(groupRepository.findByName(eq(name))).thenReturn(existing);

        userAdminService.onGroupDeleted(new GroupDeletedEvent(this, name, mock(IDirectory.class)));

        final ArgumentCaptor<GroupEntity> deletedGroupCaptor = ArgumentCaptor.forClass(GroupEntity.class);
        verify(groupRepository).save(deletedGroupCaptor.capture());

        final GroupEntity deletedGroup = deletedGroupCaptor.getValue();
        assertEquals(name, deletedGroup.getName());
        assertEquals(Instant.now().toDate(), deletedGroup.getDeletedDate());

        assertThat(eventPublisher.getPublishedEvents(), hasSize(0));
    }

    @Test
    public void testOnGroupDeletedFromAllDirectoriesButAlreadyMarkedForDeletion() {
        final String name = "bluegrass";

        when(authenticationProviderService.findGroup(any(UserDirectory.class), eq(name))).thenReturn(null);

        final Long deletedGroupId = 123456L;
        final GroupEntity existing = spy(GroupEntity.builder()
                .name(name)
                .directory(UserDirectory.InternalLdap)
                .deletedDate(Instant.now().minus(TimeUnit.DAYS.toMillis(3)).toDate())
                .build());
        when(existing.getId()).thenReturn(deletedGroupId);

        when(groupRepository.findByName(name)).thenReturn(existing);

        userAdminService.onGroupDeleted(new GroupDeletedEvent(this, name, mock(IDirectory.class)));

        final ArgumentCaptor<GroupEntity> deletedGroupCaptor = ArgumentCaptor.forClass(GroupEntity.class);
        verify(groupRepository).save(deletedGroupCaptor.capture());

        final GroupEntity deletedGroup = deletedGroupCaptor.getValue();
        assertEquals(deletedGroupId, deletedGroup.getId());
        assertEquals(name, deletedGroup.getName());
        assertEquals(Instant.now().toDate(), deletedGroup.getDeletedDate());

        assertThat(eventPublisher.getPublishedEvents(), hasSize(0));
    }

    @Test
    public void testOnGroupDeletedButStillExistsInAnotherDirectory() throws Exception {
        when(authenticationProviderService.findGroup(any(UserDirectory.class), eq("bluegrass")))
                .thenReturn(mock(IGroup.class));

        verifyZeroInteractions(groupRepository);
        assertThat(eventPublisher.getPublishedEvents(), hasSize(0));
    }

    @Test
    public void testOnUserDeletedFromAllDirectories() throws Exception {
        final String name = "jimbob";
        final UserEntity jimbob = UserEntity.builder().username(name).build();

        when(authenticationProviderService.findUser(eq(name), anyBoolean())).thenReturn(null);
        when(userRepository.findByName(name)).thenReturn(jimbob);

        userAdminService.onUserDeleted(new UserDeletedEvent(this, name, mock(IDirectory.class)));

        final ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(userCaptor.capture());
        assertThat("Expected deletedDate to be set on deleted user",
            userCaptor.getValue().getDeletedDate(),
            notNullValue());
        assertThat(eventPublisher.getPublishedEvents(), hasSize(0));
    }

    @Test
    public void testOnUserDeletedButStillExistsInAnotherDirectory() {
        when(authenticationProviderService.findUser(eq("jimbob"), anyBoolean())).thenReturn(mock(IUser.class));
        userAdminService.onUserDeleted(new UserDeletedEvent(this, "jimbob", mock(IDirectory.class)));

        verifyZeroInteractions(userRepository);
        assertThat(eventPublisher.getPublishedEvents(), hasSize(0));
    }

    @Test
    public void testCleanupDeletedGroups() {
        final long jobDelay = 60;
        userAdminService.withGroupCleanupJobDelay(jobDelay);

        final Date deletedDate = Instant.now().minus(Duration.standardMinutes(jobDelay)).toDate();

        final GroupEntity deletedGroup = GroupEntity.builder().name("deleted-group").deletedDate(deletedDate).build();
        final GroupEntity existingGroup = GroupEntity.builder().name("existing-group").deletedDate(deletedDate).build();

        when(groupRepository.findByDeletedDateEarlierThan(eq(deletedDate), any()))
                .thenAnswer(PageAnswer.withPageOf(deletedGroup, existingGroup));

        final IGroup group = mock(IGroup.class);
        when(authenticationProviderService.findGroup(any(), eq(existingGroup.getName()))).thenReturn(group);

        userAdminService.cleanupDeletedGroups();

        assertThat("Expected only one group to be cleaned up", eventPublisher.getPublishedEvents(), hasSize(1));
        final GroupCleanupEvent event = (GroupCleanupEvent) eventPublisher.getPublishedEvents().get(0);
        assertEquals(event.getGroup(), "deleted-group");

        verify(groupRepository).delete(deletedGroup);
        verify(groupRepository, never()).delete(existingGroup);
    }

    @Test
    public void testCleanupDeletedGroupsWithMultipleBatches() {
        final long jobDelay = 60;
        userAdminService.withGroupCleanupJobDelay(jobDelay).withGroupCleanupJobBatchSize(1);

        final Date deletedDate = Instant.now().minus(Duration.standardMinutes(jobDelay)).toDate();

        final GroupEntity firstBatchGroup = GroupEntity.builder().name("first-group").deletedDate(deletedDate).build();
        final GroupEntity secondBatchGroup = GroupEntity.builder()
                .name("second-group")
                .deletedDate(deletedDate)
                .build();

        when(groupRepository.findByDeletedDateEarlierThan(eq(deletedDate), any(PageRequest.class)))
                .thenAnswer(withPageOf(2L, firstBatchGroup))
                .thenAnswer(withPageOf(1L, secondBatchGroup));

        userAdminService.cleanupDeletedGroups();

        assertThat("Expected only two groups to be cleaned up", eventPublisher.getPublishedEvents(), hasSize(2));
        assertEquals(((GroupCleanupEvent) eventPublisher.getPublishedEvents().get(0)).getGroup(), "first-group");
        assertEquals(((GroupCleanupEvent) eventPublisher.getPublishedEvents().get(1)).getGroup(), "second-group");

        verify(groupRepository, times(2)).findByDeletedDateEarlierThan(eq(deletedDate), any(Pageable.class));
        verify(groupRepository).delete(firstBatchGroup);
        verify(groupRepository).delete(secondBatchGroup);
    }

    @Test
    public void testCleanupDeletedUsers() {
        // Date value does not matter, but we have to check if it's cleared at the end
        final UserEntity deletedUser = UserEntity.builder().username("john").deletedDate(new Date()).build();
        final UserEntity undeletedUser = UserEntity.builder().username("jane").deletedDate(new Date()).build();

        final int batchSize = 5;
        userAdminService.withUserCleanupJobBatchSize(batchSize);

        final ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        when(userRepository.findByDeletedDateEarlierThan(dateCaptor.capture(),
            MockitoHamcrest.argThat(pageable(0, batchSize)))).thenAnswer(withPageOf(deletedUser, undeletedUser));

        final long delay = 60;
        userAdminService.withUserCleanupJobDelay(delay);

        when(authenticationProviderService.existsUser(undeletedUser.getName())).thenReturn(true);

        userAdminService.cleanupDeletedUsers();

        verify(userRepository).delete(eq(deletedUser));

        final Date actualDate = dateCaptor.getValue();
        final Date expectedDate = Instant.now().minus(Duration.standardMinutes(delay)).toDate();
        assertThat(actualDate, TimeMatchers.closeTo(expectedDate, 10, TimeUnit.SECONDS));

        assertThat("Expected only one user to be cleaned up", eventPublisher.getPublishedEvents(), hasSize(1));
        final UserCleanupEvent event = (UserCleanupEvent) eventPublisher.getPublishedEvents().get(0);
        assertThat(event.getDeletedUser().getName(), is(deletedUser.getName()));

        final ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        final UserEntity updatedUser1 = userCaptor.getAllValues().get(0);
        assertThat(updatedUser1.getDeletedDate(), is(nullValue()));
        assertThat(updatedUser1.getName(), is(deletedUser.getName()));

        final UserEntity updatedUser2 = userCaptor.getAllValues().get(1);
        assertThat(updatedUser2.getDeletedDate(), is(nullValue()));
        assertThat(updatedUser2.getName(), is(undeletedUser.getName()));
    }

    @Test
    public void testCleanupDeleteUsersWithMultipleBatches() {
        final int batchSize = 5;
        final int userCount = batchSize * 2; // Make the search page
        final Set<Long> userIds = ContiguousSet.create(Range.closedOpen(0L, Long.valueOf(userCount)),
            DiscreteDomain.longs());

        userAdminService.withUserCleanupJobBatchSize(batchSize);

        final List<UserEntity> deleteUsers = Lists.newArrayListWithCapacity(userCount);
        for (final long userId : userIds) {
            final UserEntity user = UserEntity.builder()
                    .deletedDate(new Date())
                    .id(userId)
                    .username("john-" + userId)
                    .build();
            deleteUsers.add(user);
            // 0, 4, and 8 still have a User
            if (userId % 4 == 0) {
                when(authenticationProviderService.existsUser(user.getName())).thenReturn(true);
            }
        }

        when(userRepository.findByDeletedDateEarlierThan(Mockito.notNull(),
            MockitoHamcrest.argThat(pageable(0, batchSize))))
                    // first page
                    .thenAnswer(withPageOf(Long.valueOf(userCount), deleteUsers.subList(0, batchSize)))
                    // last page
                    .thenAnswer(
                        withPageOf(Long.valueOf(userCount - batchSize), deleteUsers.subList(batchSize, userCount)));

        userAdminService.cleanupDeletedUsers();

        assertThat(eventPublisher.getPublishedEvents(), hasSize(7)); // Users backed aren't cleaned up
        verify(userRepository, times(userCount)).save(argThat(Matchers.<UserEntity> allOf(// Specialize
                                                                                          // here,
            // not on
            // hasProperty, or Java 8
            // fails at runtime
            hasProperty("id", is(in(userIds))),
            hasProperty("deletedDate", nullValue()))));
    }

    @Test
    public void testRemoveUserFromGroup() {
        final GroupEntity group = GroupEntity.builder().build();
        final UserEntity user = UserEntity.builder().username("user").build();

        when(groupRepository.findByName(eq("group"))).thenReturn(group);
        when(userRepository.findByName(eq("user"))).thenReturn(user);
        when(userRepository.removeGroupMember(same(group), same(user))).thenReturn(true);

        userAdminService.removeUserFromGroup("group", "user");

        verify(groupRepository).findByName(eq("group"));
        verify(userRepository).findByName(eq("user"));
        verify(userRepository).removeGroupMember(same(group), same(user));
        verify(authenticationProviderService, never()).findDirectoryFor(any(IUser.class));
        verify(permissionAdminService).canRemoveUserFromGroup(eq("user"), eq("group"));
    }

    @Test
    public void testRemoveUserFromGroupWhenNotPrimaryUser() {
        assertThrows(NoSuchEntityException.class, () -> {
            final GroupEntity group = GroupEntity.builder().build();
            final UserEntity user = UserEntity.builder().username("user").build();

            final IDirectory directory = mock(IDirectory.class);
            when(directory.getName()).thenReturn("directory");

            when(authenticationProviderService.findDirectoryFor(same(user))).thenReturn(directory);
            when(groupRepository.findByName(eq("group"))).thenReturn(group);
            when(userRepository.findByName(eq("user"))).thenReturn(user);

            try {
                userAdminService.removeUserFromGroup("group", "user");
            } finally {
                verify(authenticationProviderService).findDirectoryFor(same(user));
                verify(groupRepository).findByName(eq("group"));
                verify(userRepository).findByName(eq("user"));
                verify(userRepository).removeGroupMember(same(group), same(user));
                verify(directory).getName();
                verify(permissionAdminService).canRemoveUserFromGroup(eq("user"), eq("group"));
            }
        });
    }

    @Test
    public void testRemoveUserFromGroupWhenNotPrimaryUserMissingDirectoryDetails() {
        assertThrows(NoSuchEntityException.class, () -> {
            final GroupEntity group = GroupEntity.builder().build();
            final UserEntity user = UserEntity.builder().username("user").build();

            when(groupRepository.findByName(eq("group"))).thenReturn(group);
            when(userRepository.findByName(eq("user"))).thenReturn(user);

            try {
                userAdminService.removeUserFromGroup("group", "user");
            } finally {
                verify(authenticationProviderService).findDirectoryFor(same(user));
                verify(groupRepository).findByName(eq("group"));
                verify(userRepository).findByName(eq("user"));
                verify(userRepository).removeGroupMember(same(group), same(user));
                verify(permissionAdminService).canRemoveUserFromGroup(eq("user"), eq("group"));
            }
        });
    }

    @Test
    public void testRemoveUserFromGroupWithNullGroup() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.removeUserFromGroup(null, "user");
        });
    }

    @Test
    public void testRemoveUserFromGroupWithNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.removeUserFromGroup("group", null);
        });
    }

    @Test
    public void testRequestPasswordReset() {
        final UserEntity user = UserEntity.builder().username("user").build();

        when(userService.toUserRequest(same(user))).thenReturn(UserRequest.builder());
        when(userRepository.findByName(eq("user"))).thenReturn(user);
        when(passwordHelper.addResetPasswordToken(eq("user"))).thenReturn("token");

        userAdminService.requestPasswordReset("user");

        verify(userRepository).findByName(eq("user"));
        verify(emailNotifier).sendPasswordReset(any(UserRequest.class), eq("token"));
        verify(passwordHelper).addResetPasswordToken(eq("user"));
    }

    @Test
    public void testResetPassword() {
        final UserEntity user = UserEntity.builder().username("user").build();

        when(passwordHelper.findUserByResetToken(eq("token"))).thenReturn(Optional.of(user));

        userAdminService.resetPassword("token", "password");

        verify(passwordHelper).findUserByResetToken(eq("token"));
        verify(passwordHelper).resetPassword(same(user), eq("password"));
    }

    @Test
    public void testResetPasswordWhenNotFound() {
        assertThrows(InvalidTokenException.class, () -> {
            userAdminService.resetPassword("token", "password");
        });

    }

    @Test
    public void testResetPasswordWithBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.resetPassword("token", "\t\n ");
        });
    }

    @Test
    public void testResetPasswordWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.resetPassword("token", null);
        });
    }

    @Test
    public void testResetPasswordWithNullToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.resetPassword(null, "password");
        });
    }

    @Test
    public void testUpdatePassword() {
        final UserEntity user = UserEntity.builder().username("user").build();

        when(userRepository.findByName(eq("user"))).thenReturn(user);

        userAdminService.updatePassword("user", "password");

        verify(userRepository).findByName(eq("user"));
        verify(passwordHelper).resetPassword(same(user), eq("password"));
    }

    @Test
    public void testUpdatePasswordWithBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updatePassword("user", "\t\n ");
        });

    }

    @Test
    public void testUpdatePasswordWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updatePassword("user", null);
        });
    }

    @Test
    public void testUpdatePasswordWithNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updatePassword(null, "password");
        });

    }

    @Test
    public void testActivateUser() {
        final ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        final UserEntity initial = UserEntity.builder().username("user").activated(false).build();
        final UserRequest.Builder userRequest = UserRequest.builder();
        when(userService.toUserRequest(isA(UserEntity.class))).thenReturn(userRequest);

        when(userRepository.findByName(eq("user"))).thenReturn(initial);
        when(userRepository.save(isA(UserEntity.class))).thenReturn(UserEntity.builder().username("user").build());

        final UserRequest actual = userAdminService.activateUser("user", true);
        assertNotNull(actual);

        verify(userRepository).findByName(eq("user"));
        verify(userRepository).save(userCaptor.capture());

        assertEquals(true, userCaptor.getValue().isActivated());
    }

    @Test
    public void testDeactivateUser() {
        final ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        final UserEntity initial = UserEntity.builder().username("user").activated(true).build();
        final UserRequest.Builder userRequest = UserRequest.builder();
        when(userService.toUserRequest(isA(UserEntity.class))).thenReturn(userRequest);

        when(userRepository.findByName(eq("user"))).thenReturn(initial);
        when(userRepository.save(isA(UserEntity.class))).thenReturn(UserEntity.builder().username("user").build());

        final UserRequest actual = userAdminService.activateUser("user", false);
        assertNotNull(actual);

        verify(userRepository).findByName(eq("user"));
        verify(userRepository).save(userCaptor.capture());

        assertEquals(false, userCaptor.getValue().isActivated());
    }

    @Test
    public void testUpdateUser() throws Exception {
        final UserEntity updated = UserEntity.builder().username("user").build();
        final ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        final UserEntity initial = UserEntity.builder()
                .displayName("Jane User")
                .email("juser@test.com")
                .username("user")
                .activated(true)
                .build();

        final UserRequest.Builder userRequest = UserRequest.builder();

        when(userRepository.findByName(eq("user"))).thenReturn(initial);
        when(userRepository.save(isA(UserEntity.class))).thenReturn(updated);
        when(userService.toUserRequest(same(updated))).thenReturn(userRequest);

        final UserRequest actual = userAdminService.updateUser("user", "Jane Superuser", "jsuperuser@test.com");
        assertNotNull(actual);

        verify(authenticationProviderService).findDirectoryFor(same(updated));
        verify(userRepository).findByName(eq("user"));
        verify(userRepository).save(userCaptor.capture());
        verify(userService).toUserRequest(same(updated));

        final UserEntity user = userCaptor.getValue();
        assertNotSame(initial, user); // The user retrieved should not be passed to directly to update
        assertEquals("Jane Superuser", user.getDisplayName());
        assertEquals("jsuperuser@test.com", user.getEmail());
        assertEquals("user", user.getName());
        assertTrue(user.isActivated());
    }

    @Test
    public void testUpdateUserWithBlankDisplayName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updateUser("user", "\t\n ", "emailAddress");
        });
    }

    @Test
    public void testUpdateUserWithBlankEmailAddress() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updateUser("user", "firstname lastname", "\t\n ");
        });
    }

    @Test
    public void testUpdateUserWithNullDisplayName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updateUser("user", null, "emailAddress");
        });
    }

    @Test
    public void testUpdateUserWithNullEmailAddress() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updateUser("user", "firstname lastname", null);
        });
    }

    @Test
    public void testUpdateUserWithNullUser() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            userAdminService.updateUser(null, "firstname lastname", "emailAddress");
        });
    }

    @Test
    public void testSameTransactionAnnotations() throws Exception {
        testSamePreAuthorizeFor("addUserToGroup", "addUserToGroups", "addMembersToGroup", "removeUserFromGroup");
    }

    private void testSamePreAuthorizeFor(final String... methodNames) {
        final Set<String> names = ImmutableSet.copyOf(methodNames);
        final Collection<String> preAuthorize = com.google.common.collect.FluentIterable
                .from(userAdminService.getClass().getMethods())
                .filter(method -> names.contains(method.getName()))
                .transform(method -> method.getAnnotation(PreAuthorize.class).value())
                .toSet();

        MatcherAssert.assertThat("diverging pre-authorize conditions", preAuthorize, hasSize(1));
    }

}
