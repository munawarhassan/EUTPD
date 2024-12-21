package com.pmi.tpd.core.user;

import static com.pmi.tpd.api.paging.PageUtils.createEmptyPage;
import static com.pmi.tpd.api.paging.PageUtils.newRequest;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;

import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.user.UserProfileRequest;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.exception.IncorrectPasswordAuthenticationException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

public class DefaultUserServiceTest extends AbstractServiceTest {

    /** */
    @Mock
    private IAuthenticationContext authenticationContext;

    /** */
    @Mock()
    private IInternalAvatarService avatarService;

    /** */
    @Mock
    private INavBuilder navBuilder;

    /** */
    @Mock
    private IEmailNotifier emailNotifier;

    /** */
    @Mock
    private IEventPublisher eventPublisher;

    /** */
    @Mock
    private IPermissionService permissionService;

    @Mock
    /** */
    private IUserRepository userRepository;

    @Mock
    /** */
    private IGroupRepository groupRepository;

    @Mock
    /** */
    private IUserPreferencesManager userPreferencesManager;

    @Mock
    /** */
    private IPasswordResetHelper passwordResetHelper;

    @Mock(lenient = true)
    private Provider<IAuthenticationService> authenticationProviderService;

    /** */
    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    /** */
    @InjectMocks
    private DefaultUserService userService;

    @Mock(lenient = true)
    private IAuthenticationService authenticationService;

    public DefaultUserServiceTest() {
        super(DefaultUserService.class, IUserService.class);
    }

    @BeforeEach
    public void setUp() throws Exception {

        when(this.authenticationProviderService.get()).thenReturn(authenticationService);
        SecurityContextHolder.createEmptyContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testAuthenticateForExistingUser() {
        final String username = "existingUser";
        final String password = "password";

        final IUser actualUser = mock(IUser.class);

        when(authenticationService.authenticate(username, password)).thenReturn(actualUser);

        assertSame(actualUser, userService.authenticate(username, password));

    }

    @Test
    public void testFindGroups() {
        final Pageable pageRequest = newRequest(0, 10);
        final Page<GroupEntity> page = createEmptyPage(pageRequest);

        when(groupRepository.findGroups(same(pageRequest))).thenReturn(page);

        assertNotNull(userService.findGroups(pageRequest));

        verify(groupRepository, times(1)).findGroups(same(pageRequest));
    }

    @Test
    public void testFindGroupsByName() {
        final Pageable pageRequest = newRequest(0, 10);
        final Page<GroupEntity> page = createEmptyPage(pageRequest);

        when(groupRepository.findGroupsByName(any(), same(pageRequest))).thenReturn(page);

        // Test that findGroupsByName calls on the same method whether name is null or not
        assertNotNull(userService.findGroupsByName(null, pageRequest));
        assertNotNull(userService.findGroupsByName("name", pageRequest));

        verify(groupRepository).findGroupsByName(isNull(), same(pageRequest));
        verify(groupRepository).findGroupsByName(eq("name"), same(pageRequest));
    }

    @Test
    public void testFindUsers() throws Exception {
        final Pageable pageRequest = newRequest(0, 10);
        final Page<UserEntity> page = createEmptyPage(pageRequest);

        when(userRepository.findUsers(same(pageRequest))).thenReturn(page);

        assertNotNull(userService.findUsers(pageRequest));

        verify(userRepository, times(1)).findUsers(same(pageRequest));
    }

    @Test
    public void testGetById() throws Exception {
        final UserEntity user = mock(UserEntity.class);
        when(user.isActivated()).thenReturn(true);
        when(userRepository.getById(1L)).thenReturn(user);

        assertSame(user, userService.getUserById(1));
    }

    @Test
    public void testGetByIdWithNonexistentId() throws Exception {
        assertNull(userService.getUserById(42));
    }

    @Test
    public void testGetNormalUserById() throws Exception {
        final UserEntity user = mock(UserEntity.class);
        when(user.isActivated()).thenReturn(true);

        when(userRepository.getById(1L)).thenReturn(user);

        assertSame(user, userService.getUserById(1));
    }

    @Test
    public void testGetNormalUserByIdFiltersInactiveUsers() throws Exception {
        final UserEntity user = mock(UserEntity.class);
        when(userRepository.getById(1L)).thenReturn(user);

        assertNull(userService.getUserById(1));
    }

    @Test
    public void testGetUserForExistingUser() throws Exception {
        final String username = "existingUser";

        final UserEntity user = mock(UserEntity.class);
        when(user.isActivated()).thenReturn(true);
        when(userRepository.findByName(username)).thenReturn(user);

        assertSame(user, userService.getUserByName(username));

        verify(userRepository, never()).save(Mockito.<UserEntity> any());
    }

    @Test
    public void testGetUserWhenNotFoundIncludingDeletedUsers() {
        assertNull(userService.getUserByName("nonexistentUser", true));

        verify(userRepository).findByName(eq("nonexistentUser"));
    }

    @Test
    public void testGetUserWithDeletedUser() {
        final UserEntity user = mock(UserEntity.class);

        when(userRepository.findByName(eq("deletedUser"))).thenReturn(user);

        assertSame(user, userService.getUserByName("deletedUser", true));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        final IUser user = User.builder().username("test").build();
        final String newPassword = "newpassword";
        when(authenticationContext.getCurrentUser()).thenReturn(of(user));
        final IUser authenticatedUser = User.builder().username("test").build();
        when(authenticationService.authenticate("test", "oldpassword")).thenReturn(authenticatedUser);

        userService.updatePassword("oldpassword", newPassword);
        verify(passwordResetHelper).resetPassword(eq(authenticatedUser), eq(newPassword));
    }

    @Test
    public void testUpdatePasswordForUnknown() throws Exception {
        assertThrows(AuthorisationException.class, () -> {
            when(authenticationContext.getCurrentUser()).thenReturn(Optional.empty());
            userService.updatePassword("unknown", "newpassword");
        });
    }

    @Test
    public void testUpdatePasswordWithMismatchingPassword() throws Exception {
        assertThrows(IncorrectPasswordAuthenticationException.class, () -> {
            final KeyedMessage message = mock(KeyedMessage.class);

            final IUser user = User.builder().username("test").build();
            final String newPassword = "newpassword";
            when(authenticationContext.getCurrentUser()).thenReturn(of(user));
            final IUser authenticatedUser = User.builder().username("test").build();
            doThrow(new IncorrectPasswordAuthenticationException(message)).when(authenticationService)
                    .authenticate("test", "oldpassword");

            userService.updatePassword("oldpassword", newPassword);
            verify(passwordResetHelper).resetPassword(eq(authenticatedUser), eq(newPassword));
        });
    }

    @Test
    public void testUpdateProfile() throws Exception {
        final UserEntity user = UserEntity.builder().username("test").password("pwd").build();
        when(userRepository.findByName(user.getName())).thenReturn(user);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> (UserEntity) invocation.getArguments()[0]);
        final UserProfileRequest userToUpdate = UserProfileRequest.builder()
                .username("test")
                .displayName("newfirstname newlastname")
                .email("new@example.com")
                .build();

        final UserProfileRequest actual = userService.updateUserProfile(userToUpdate);
        assertThat(actual.getUsername(), is("test"));
        assertThat(actual.getDisplayName(), is("newfirstname newlastname"));
        assertThat(actual.getEmail(), is("new@example.com"));
    }

    @Test
    public void testUpdateProfileForUnknown() throws Exception {
        assertThrows(NoSuchUserException.class, () -> {
            final UserProfileRequest userToUpdate = UserProfileRequest.builder()
                    .username("test")
                    .displayName("newfirstname newlastname")
                    .email("new@example.com")
                    .build();
            userService.updateUserProfile(userToUpdate);
        });

    }

}
