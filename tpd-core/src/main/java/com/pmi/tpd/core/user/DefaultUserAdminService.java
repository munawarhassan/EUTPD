package com.pmi.tpd.core.user;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;
import static com.pmi.tpd.api.util.Assert.notNull;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IJobRunnerRequest;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.event.user.GroupCleanupEvent;
import com.pmi.tpd.core.event.user.GroupCreatedEvent;
import com.pmi.tpd.core.event.user.GroupDeletedEvent;
import com.pmi.tpd.core.event.user.GroupMemberAddedEvent;
import com.pmi.tpd.core.event.user.GroupMemberRemovedEvent;
import com.pmi.tpd.core.event.user.GroupMembershipCreatedEvent;
import com.pmi.tpd.core.event.user.UserCleanupEvent;
import com.pmi.tpd.core.event.user.UserDeletedEvent;
import com.pmi.tpd.core.exception.InvalidTokenException;
import com.pmi.tpd.core.exception.NoSuchGroupException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.exception.UsernameAlreadyExistsException;
import com.pmi.tpd.core.model.user.AbstractVoidUserEntityVisitor;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.security.OperationType;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.ForbiddenException;
import com.pmi.tpd.security.annotation.Unsecured;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.spring.transaction.SpringTransactionUtils;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@Transactional(readOnly = true)
@Service
public class DefaultUserAdminService implements IUserAdminService, IScheduledJobSource {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserAdminService.class);

  /** */
  private static final JobId GROUP_CLEANUP_JOB_ID = JobId.of(GroupCleanUpJob.class.getSimpleName());

  /** */
  private static final JobRunnerKey GROUP_CLEANUP_JOB_RUNNER_KEY = JobRunnerKey.of(GroupCleanUpJob.class.getName());

  /** */
  private static final JobId USER_CLEANUP_JOB_ID = JobId.of(UserCleanupJob.class.getSimpleName());

  /** */
  private static final JobRunnerKey USER_CLEANUP_JOB_RUNNER_KEY = JobRunnerKey.of(UserCleanupJob.class.getName());

  /** */
  private final IEmailNotifier emailNotifier;

  /** */
  private final IEventPublisher eventPublisher;

  /** */
  private final I18nService i18nService;

  /** */
  private final IPasswordResetHelper passwordHelper;

  /** */
  private final IPermissionAdminService permissionAdminService;

  /** */
  private final IAuthenticationService authenticationService;

  /** */
  private final IUserService userService;

  /** */
  private final IGroupRepository groupRepository;

  /** */
  private final IUserRepository userRepository;

  /** */
  // private final ICaptchaService captchaService;
  /** */
  private final TransactionTemplate requiresNewTransactionTemplate;

  /** */
  @Value("${service.admin.user.cleanup.job.batch.size:100}")
  private int userCleanupJobBatchSize = 100;

  /**
   * Controls the minimum delay that is used between a user being deleted and it
   * being cleaned up (in MINUTES).
   * Default 10080 minutes -> 7 days
   */
  @Value("${service.admin.user.cleanup.job.delay:10080}")
  private long userCleanupJobDelay = 10080;

  /** */
  @Value("${service.admin.user.cleanup.job.interval:360}")
  private long userCleanupJobInterval = 360;

  /** */
  @Value("${service.admin.group.cleanup.job.batch.size:100}")
  private int groupCleanupJobBatchSize = 100;

  /**
   * Controls the minimum delay that is used between a group being deleted and it
   * being cleaned up (in MINUTES).
   * Default 10080 minutes -> 7 days
   */
  @Value("${service.admin.group.cleanup.job.delay:10080}")
  private long groupCleanupJobDelay = 10080;

  /** */
  @Value("${service.admin.group.cleanup.job.interval:360}")
  private long groupCleanupJobInterval = 360;

  /**
   * @param permissionAdminService
   *                               a permission admin service.
   * @param userService
   *                               a user service.
   * @param emailNotifier
   *                               a email notifier.
   * @param i18nService
   *                               a i18n service
   * @param authenticationService
   *                               a authentication service.
   * @param eventPublisher
   *                               a event publisher.
   * @param userRepository
   *                               a user repository.
   * @param groupRepository
   *                               a group repository.
   * @param transactionManager
   *                               a local transaction manager
   * @param passwordHelper
   *                               a password helper.
   */
  @Inject
  public DefaultUserAdminService(final IPermissionAdminService permissionAdminService, final IUserService userService,
      final IPasswordResetHelper passwordHelper, final IEmailNotifier emailNotifier,
      final I18nService i18nService, final IAuthenticationService authenticationService,
      final IEventPublisher eventPublisher, final IUserRepository userRepository,
      final IGroupRepository groupRepository,
      /* final ICaptchaService captchaService, */ final PlatformTransactionManager transactionManager) {
    // this.captchaService = captchaService;

    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.emailNotifier = emailNotifier;
    this.eventPublisher = eventPublisher;
    this.i18nService = i18nService;

    this.permissionAdminService = permissionAdminService;
    this.passwordHelper = passwordHelper;
    // this.userHelper = userHelper;
    this.authenticationService = authenticationService;

    requiresNewTransactionTemplate = new TransactionTemplate(transactionManager,
        SpringTransactionUtils.REQUIRES_NEW);
  }

  /**
   * @param value
   *              the number of deleted groups to load in memory
   * @return Returns a fluent instance.
   */
  public DefaultUserAdminService withGroupCleanupJobBatchSize(final int value) {
    this.groupCleanupJobBatchSize = value;
    return this;
  }

  /**
   * @param value
   *              delay before delete group in minutes.
   * @return Returns a fluent instance.
   */
  public DefaultUserAdminService withGroupCleanupJobDelay(final long value) {
    this.groupCleanupJobDelay = value;
    return this;
  }

  /**
   * @param value
   *              a scheduling interval in minutes.
   * @return Returns a fluent instance.
   */
  public DefaultUserAdminService withGroupCleanupJobInterval(final long value) {
    this.groupCleanupJobInterval = value;
    return this;
  }

  /**
   * @param value
   *              the number of deleted users to load in memory
   * @return Returns a fluent instance.
   */
  public DefaultUserAdminService withUserCleanupJobBatchSize(final int value) {
    this.userCleanupJobBatchSize = value;
    return this;
  }

  /**
   * @param value
   *              delay before delete user in minutes.
   * @return Returns a fluent instance.
   */
  public DefaultUserAdminService withUserCleanupJobDelay(final long value) {
    this.userCleanupJobDelay = value;
    return this;
  }

  /**
   * @param value
   *              a scheduling interval in minutes.
   * @return Returns a fluent instance.
   */
  public DefaultUserAdminService withUserCleanupJobInterval(final long value) {
    this.userCleanupJobInterval = value;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public void addUserToGroups(@Nonnull final String username, @Nonnull final Set<String> groupNames)
      throws ForbiddenException, NoSuchGroupException, NoSuchUserException {
    checkNotNull(username, "username");
    checkNotNull(groupNames, "groupNames");
    isTrue(Iterables.all(groupNames, Predicates.notNull()), "groupNames contains a null group name");
    for (final String groupName : groupNames) {
      doAddUserToGroup(username, groupName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public void addMembersToGroup(@Nonnull final String groupName, @Nonnull final Set<String> usernames)
      throws ForbiddenException, NoSuchGroupException, NoSuchUserException {
    checkNotNull(groupName, "groupName");
    checkNotNull(usernames, "usernames");
    isTrue(Iterables.all(usernames, Predicates.notNull()), "usernames contains a null username");
    for (final String username : usernames) {
      doAddUserToGroup(username, groupName); // note: this means that canAddUserToGroup(groupName) will be checked
                                             // n times instead of one;
                                             // since that check is only done in memory (owing the permission
                                             // graph), this is deemed
                                             // preferable to duplicating the delegated method (addUserToGroup)
                                             // here
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public boolean canCreateGroups() {
    return isAllowedInAnyDirectory(OperationType.CREATE_GROUP);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public boolean canUpdateGroups() {
    return isAllowedInAnyDirectory(OperationType.UPDATE_GROUP);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public boolean canCreateUsers() {
    return isAllowedInAnyDirectory(OperationType.CREATE_USER);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public boolean canDeleteGroups() {
    return isAllowedInAnyDirectory(OperationType.DELETE_GROUP);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public boolean newUserCanResetPassword() {
    return false;
    // return crowdControl.getCapabilitiesForNewUsers().canResetPassword();
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public GroupRequest createGroup(@Nonnull final String groupName) {
    return createGroup(groupName, UserDirectory.Internal);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public GroupRequest createGroup(@Nonnull final String groupName, @Nonnull final UserDirectory directory) {
    checkNotNull(groupName, "groupName");
    checkNotNull(directory, "directory");
    final GroupEntity group = groupRepository
        .save(GroupEntity.builder().name(groupName).directory(directory).description(groupName).build());
    this.eventPublisher.publish(new GroupCreatedEvent(this, groupName));
    return transformGroup(group);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public void addGroups(@Nonnull final UserDirectory directory, @Nonnull final Set<String> groups) {
    checkNotNull(directory, "directory");
    checkNotNull(groups, "groups");
    for (final String groupName : groups) {
      // TODO test if group already exist.
      final IGroup grp = this.authenticationService.findGroup(directory, groupName);
      if (grp == null) {
        continue;
      }
      groupRepository.save(GroupEntity.builder()
          .name(grp.getName())
          .directory(directory)
          .description(grp.getDescription())
          .build());
      this.eventPublisher.publish(new GroupCreatedEvent(this, groupName));
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public UserRequest createUser(@Nonnull final String username,
      @Nonnull final String password,
      @Nonnull final String displayName,
      @Nonnull final String emailAddress) throws UsernameAlreadyExistsException, UserEmailAlreadyExistsException {
    return createUser(username, password, displayName, emailAddress, UserDirectory.Internal, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public UserRequest createUser(@Nonnull final String username,
      @Nonnull final String password,
      @Nonnull final String displayName,
      @Nonnull final String emailAddress,
      @Nonnull final UserDirectory directory,
      final boolean addToDefaultGroup) throws UsernameAlreadyExistsException, UserEmailAlreadyExistsException {
    isTrue(!checkNotNull(displayName, "displayName").trim().isEmpty(), "A non-blank display name is required");
    isTrue(!checkNotNull(emailAddress, "emailAddress").trim().isEmpty(), "A non-blank e-mail address is required");
    checkNotNull(username, "username");
    checkNotNull(directory, "directory");

    final IDirectory dir = authenticationService.findDirectoryFor(directory);
    if (dir.isUserCreatable() || dir.isUserUpdatable()) {
      isTrue(!checkNotNull(password, "password").trim().isEmpty(), "A non-blank password is required");
    }
    final UserRequest user = UserRequest.builder()
        .displayName(displayName)
        .email(emailAddress)
        .username(username)
        .password(password)
        .directory(directory)
        .build();
    return userService.createUser(user, true, addToDefaultGroup);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public UserRequest createUserWithGeneratedPassword(@Nonnull final String username,
      @Nonnull final String displayName,
      @Nonnull final String emailAddress,
      @Nonnull final UserDirectory directory) throws UsernameAlreadyExistsException, UserEmailAlreadyExistsException {
    isTrue(!checkNotNull(displayName, "displayName").trim().isEmpty(), "A non-blank display name is required");
    isTrue(!checkNotNull(emailAddress, "emailAddress").trim().isEmpty(), "A non-blank e-mail address is required");
    checkNotNull(username, "username");
    checkNotNull(directory, "directory");

    emailNotifier.validateCanSendEmails();

    UserRequest user = UserRequest.builder()
        .displayName(displayName)
        .email(emailAddress)
        .username(username)
        .directory(directory)
        .password(passwordHelper.generatePassword())
        .build();
    user = userService.createUser(user, true, true);

    // if the directory does not allow password resets, cancel the creation of the
    // user account and
    // the submission of the email because the new user will be unable to use the
    // email's reset token
    // to set his or her password
    if (!authenticationService.canResetPassword(username)) {
      throw new IntegrityException(
          i18nService.createKeyedMessage("app.service.cant.send.email.passwordReset", Product.getFullName()));
    }

    final String token = passwordHelper.addResetPasswordToken(user.getUsername());
    emailNotifier.sendCreatedUser(user, token);
    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public GroupRequest deleteGroup(@Nonnull final String groupName) {
    checkNotNull(groupName, "groupName");

    permissionAdminService.canDeleteGroup(groupName);

    final GroupEntity group = groupRepository.findByName(groupName);
    if (group == null) {
      throw new NoSuchGroupException(i18nService.createKeyedMessage("app.service.users.nosuchgroup", groupName),
          groupName);
    }
    this.eventPublisher.publish(new GroupCleanupEvent(this, group.getName()));
    groupRepository.delete(group);
    this.eventPublisher.publish(new GroupDeletedEvent(this, group.getName(), null));
    return GroupRequest.builder().deletable(true).name(groupName).build();
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public UserRequest deleteUser(@Nonnull final String username) {
    checkNotNull(username, "username");

    final UserEntity user = userRepository.findByName(username);
    if (user == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.users.nosuchuser", username),
          username);
    }
    permissionAdminService.canDeleteUser(user);

    // Delete the user, revoke all of their permissions in the system and remove any
    // tokens from the
    // remember me repository, ensuring they do not have residual access
    this.eventPublisher.publish(new UserCleanupEvent(this, user));
    userRepository.delete(user);
    this.eventPublisher.publish(new UserDeletedEvent(this, username, null));
    return transformUser(user);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public Page<GroupRequest> findGroups(@Nonnull final Pageable pageRequest) {
    return transformGroups(groupRepository.findGroups(notNull(pageRequest)));
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public Page<String> findGroupsForDirectory(@Nonnull final Pageable pageRequest,
      @Nonnull final UserDirectory directory,
      @Nonnull final String groupName) {
    return this.authenticationService.findGroups(checkNotNull(directory, "directory"),
        checkNotNull(groupName, "groupName"),
        checkNotNull(pageRequest, "pageRequest"));
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public Page<GroupRequest> findGroupsByName(final String groupName, @Nonnull final Pageable pageRequest) {
    return transformGroups(groupRepository.findGroupsByName(groupName, pageRequest));
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public Page<GroupRequest> findGroupsWithUser(@Nonnull final String username,
      final String groupName,
      @Nonnull final Pageable pageRequest) {
    return transformGroups(groupRepository.findGroupsByUser(username, groupName, pageRequest));
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public Page<GroupRequest> findGroupsWithoutUser(@Nonnull final String username,
      final String groupName,
      @Nonnull final Pageable pageRequest) {
    return transformGroups(groupRepository.findGroupsWithoutUser(username, groupName, pageRequest));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Unsecured("Password reset runs in a non-authenticated context and requires no permissions")
  @Nullable
  public UserRequest findUserByPasswordResetToken(@Nonnull final String token) {
    return passwordHelper.findUserByResetToken(token).map(this::transformUser).orElse(null);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public Page<UserRequest> findUsers(@Nonnull final Pageable pageRequest) {
    return userRepository.findUsers(pageRequest).map(this::transformUser);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public Page<UserRequest> findUsersByName(final String username, @Nonnull final Pageable pageRequest) {
    return userRepository.findByName(username, pageRequest).map(this::transformUser);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public Page<UserRequest> findUsersWithGroup(@Nonnull final String groupName, @Nonnull final Pageable pageRequest) {
    return userRepository.findUsersWithGroup(groupName, pageRequest).map(this::transformUser);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public Page<UserRequest> findUsersWithoutGroup(@Nonnull final String groupName,
      @Nonnull final Pageable pageRequest) {
    return userRepository.findUsersWithoutGroup(groupName, pageRequest).map(this::transformUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN') or isCurrentUser(#username)")
  @Transactional
  public UserRequest getUserDetails(@Nonnull final String username) {
    // final IUser user = authenticationService.findUser(username, true);
    final IUser user = this.userRepository.findByName(username);
    if (user == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", username),
          username);
    }
    return user == null ? null : transformUser(user);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  @Transactional
  public GroupRequest getGroupDetails(final @Nonnull String groupName) {
    final GroupEntity group = this.groupRepository.findByName(groupName);
    if (group == null) {
      throw new NoSuchGroupException(i18nService.createKeyedMessage("app.service.users.nosuchgroup", groupName),
          groupName);
    }
    return transformGroup(group);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @PreAuthorize("hasGlobalPermission('ADMIN') or isCurrentUser(#user)")
  @Transactional
  public UserRequest getUserDetails(@Nonnull final IUser user) {
    if (user instanceof UserEntity) {
      return transformUser(user);
    }

    final UserRequest detailedUser = getUserDetails(checkNotNull(user, "user").getName());
    if (detailedUser == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", user.getName()),
          user.getName());
    }
    return detailedUser;
  }

  /**
   * @param event
   *              the event indicating a group has been deleted.
   */
  @EventListener
  public void onGroupDeleted(final GroupDeletedEvent event) {
    final String groupName = event.getGroupName();
    final GroupEntity grp = groupRepository.findByName(groupName);
    final UserDirectory directory = grp != null ? grp.getDirectory() : null;
    if (directory != null && authenticationService.findGroup(directory, groupName) == null) {
      requiresNewTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

        @Override
        protected void doInTransactionWithoutResult(final TransactionStatus status) {
          // Mark the group for deletion. It will eventually be picked up by the
          // GroupCleanUpJob
          final GroupEntity deletedGroup = groupRepository.findByName(groupName);
          if (deletedGroup != null) {
            // The group has already been marked for deletion.
            // However if it was created and deleted again we should update the timestamp
            // so it will be removed at the appropriate time
            groupRepository.save(deletedGroup.copy().deletedDate(Instant.now().toDate()).build());
          } else {
            groupRepository.save(GroupEntity.builder()
                .name(groupName)
                .directory(directory)
                .deletedDate(Instant.now().toDate())
                .build());
          }
        }
      });
    }
  }

  /**
   * @param event
   *              the event indicating a group has been created.
   */
  @EventListener
  public void onGroupCreated(final GroupMembershipCreatedEvent event) {
    final String groupName = event.getGroupName();
    final GroupEntity grp = groupRepository.findByName(groupName);
    final UserDirectory directory = grp != null ? grp.getDirectory() : null;
    if (directory != null && authenticationService.findGroup(directory, groupName) != null) {
      requiresNewTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

        @Override
        protected void doInTransactionWithoutResult(final TransactionStatus status) {
          final GroupEntity existingGroup = groupRepository.findByName(groupName);
          if (existingGroup != null) {
            // enforce the update of existing group, can be inactive.
            groupRepository.save(
                existingGroup.copy().deletedDate(null).description(groupName).directory(directory).build());
          } else {
            groupRepository.save(
                GroupEntity.builder().name(groupName).directory(directory).description(groupName).build());
          }
        }
      });
    }
  }

  /**
   * @param event
   *              event indicating a user has been deleted.
   */
  @EventListener
  public void onUserDeleted(final UserDeletedEvent event) {
    final String username = event.getUsername();

    requiresNewTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        if (authenticationService.findUser(username, true) != null) {
          // User was not really deleted, don't mark it as such
          return;
        }

        final UserEntity user = userRepository.findByName(username);
        if (user != null) {
          final UserEntity userWithDeletedDate = user.copy().deletedDate(new Date()).build();
          userRepository.save(userWithDeletedDate);
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  @PreAuthorize("hasGlobalPermission('ADMIN')")
  public void removeUserFromGroup(@Nonnull final String groupName, @Nonnull final String username) {
    checkNotNull(groupName, "groupName");
    checkNotNull(username, "username");

    permissionAdminService.canRemoveUserFromGroup(username, groupName);

    // The 'primary' user is the first user that has the supplied name, resolved in
    // order from the user-ordered
    // list of User Directories
    final GroupEntity group = groupRepository.findByName(groupName);
    final UserEntity user = userRepository.findByName(username);
    if (!userRepository.removeGroupMember(group, user)) {
      // This will happen sometimes from actions originating in the Group view screen,
      // where we show users
      // shadowed by the primary user who belong to a group in their local directory
      // with the specified name.
      final IDirectory directory = authenticationService.findDirectoryFor(user);
      final String directoryName = directory == null ? "<Unknown>" : directory.getName();

      LOGGER.info(String.format("Failed to remove user %1$s from group %2$s. Group %2$s may not exist in the "
          + "same  directory as the primary user with name %1$s. The 'primary' user is the first user"
          + " with that  name resolved from the ordered list of User Directories.",
          username,
          groupName));
      throw new NoSuchGroupException(
          i18nService.createKeyedMessage("app.service.removeUserFromGroup.notFromGroup",
              username,
              directoryName,
              groupName),
          groupName);
    }
    this.eventPublisher.publish(new GroupMemberRemovedEvent(this, username, groupName));
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @Transactional
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or (hasGlobalPermission('ADMIN') "
      + "and not hasGlobalPermission(#currentUsername, 'SYS_ADMIN'))")
  public UserRequest renameUser(@Nonnull final String currentUsername, @Nonnull final String newUsername) {
    checkNotNull(currentUsername, "currentUserName");
    checkNotNull(newUsername, "newUserName");
    if (this.userRepository.exists(QUserEntity.userEntity.username.equalsIgnoreCase(newUsername))) {
      throw new UsernameAlreadyExistsException(
          i18nService.createKeyedMessage("app.service.user.alreadyexists", newUsername));
    }
    final UserEntity user = userRepository.findByName(currentUsername);
    if (currentUsername == null) {
      throw new NoSuchUserException(
          i18nService.createKeyedMessage("app.service.user.nosuchuser", currentUsername), currentUsername);
    }
    // check if directory is updatable.
    final IDirectory directory = this.authenticationService.findDirectoryFor(user);
    if (!directory.isUserUpdatable()) {
      throw new ForbiddenException(
          i18nService.createKeyedMessage("app.service.user.rename.not.allowed", directory.getName()));
    }
    // TODO replace userRepository.renameUser call by
    // authenticationService.renameUser
    return transformUser(userRepository.renameUser(user, newUsername));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  @Unsecured("Password reset runs in a non-authenticated context and requires no permissions")
  public void requestPasswordReset(@Nonnull final String username) throws NoSuchEntityException, MailException {
    final UserEntity user = userRepository.findByName(username);
    if (user == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", username),
          username);
    }
    emailNotifier.sendPasswordReset(transformUser(user), passwordHelper.addResetPasswordToken(username));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  @Unsecured("Password reset runs in a non-authenticated context and requires no permissions")
  public void resetPassword(@Nonnull final String token, @Nonnull final String password) {
    checkNotNull(token, "token");
    isTrue(!checkNotNull(password, "password").trim().isEmpty(), "A non-blank password is required");

    final Optional<IUser> user = passwordHelper.findUserByResetToken(token);
    if (user.isEmpty()) {
      throw new InvalidTokenException(i18nService.createKeyedMessage("app.service.invalidtoken"), token);
    }
    passwordHelper.resetPassword(user.get(), password);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or (hasGlobalPermission('ADMIN') "
      + "and not hasGlobalPermission(#username, 'SYS_ADMIN'))")
  public void updatePassword(@Nonnull final String username, @Nonnull final String newPassword) {
    checkNotNull(username, "username");
    isTrue(!checkNotNull(newPassword, "newPassword").trim().isEmpty(), "A non-blank password is required");
    final IUser user = userRepository.findByName(username);
    if (user == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", username),
          username);
    }
    passwordHelper.resetPassword(user, newPassword);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @Transactional
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or (hasGlobalPermission('ADMIN') "
      + "and not hasGlobalPermission(#user, 'SYS_ADMIN'))")
  public UserRequest updateUser(@Nonnull final UserUpdate user) throws UserEmailAlreadyExistsException {
    isTrue(!checkNotNull(user.getDisplayName(), "user.displayName").trim().isEmpty(),
        "A non-blank display name is required");
    isTrue(!checkNotNull(user.getEmail(), "emailAddress").trim().isEmpty(),
        "A non-blank e-mail address is required");
    checkNotNull(user.getName(), "name");
    if (userRepository.existsEmail(user.getEmail(), user.getName())) {
      throw new UserEmailAlreadyExistsException(
          i18nService.createKeyedMessage("app.service.user.emailalreadyexists", user.getEmail()));
    }

    UserEntity entityUser = userRepository.findByName(user.getName());
    if (entityUser == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", user.getName()),
          user.getName());
    }
    entityUser = userRepository
        .save(entityUser.copy().displayName(user.getDisplayName()).email(user.getEmail()).build());

    return transformUser(entityUser);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  @Transactional
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or (hasGlobalPermission('ADMIN') "
      + "and not hasGlobalPermission(#username, 'SYS_ADMIN'))")
  public UserRequest updateUser(final @Nonnull String username, final @Nonnull String displayName,
      final @Nonnull String emailAddress)
      throws UserEmailAlreadyExistsException {
    return updateUser(new UserUpdate(username, displayName, emailAddress));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or hasGlobalPermission('ADMIN')")
  public UserRequest activateUser(final @Nonnull String username, final boolean activated) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Activating user {}", username);
    }
    UserEntity user = userRepository.findByName(username);
    if (user == null) {
      throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", username),
          username);
    }
    permissionAdminService.canActivateUser(user, activated);

    user = userRepository.save(user.copy().activated(activated).build());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Activated user: {}", username);
    }
    return transformUser(user);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
    schedule(schedulerService,
        new GroupCleanUpJob(),
        GROUP_CLEANUP_JOB_RUNNER_KEY,
        GROUP_CLEANUP_JOB_ID,
        RunMode.RUN_ONCE_PER_CLUSTER,
        groupCleanupJobInterval);
    schedule(schedulerService,
        new UserCleanupJob(),
        USER_CLEANUP_JOB_RUNNER_KEY,
        USER_CLEANUP_JOB_ID,
        RunMode.RUN_ONCE_PER_CLUSTER,
        userCleanupJobInterval);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
    schedulerService.unregisterJobRunner(GROUP_CLEANUP_JOB_RUNNER_KEY);
    schedulerService.unregisterJobRunner(USER_CLEANUP_JOB_RUNNER_KEY);
  }

  private void doAddUserToGroup(@Nonnull final String username, @Nonnull final String groupName) {
    checkNotNull(username, "username");
    checkNotNull(groupName, "groupName");

    permissionAdminService.canAddUserToGroup(groupName);

    final GroupEntity group = groupRepository.findByName(groupName);
    final UserEntity user = userRepository.findByName(username);
    userRepository.addGroupMember(group, user);
    this.eventPublisher.publish(new GroupMemberAddedEvent(this, username, groupName));
  }

  /**
   * Iterates over all directories searching for the first one which is active
   * <i>and</i> allows the requested
   * {@code OperationType}. Directories which are not active are not tested.
   *
   * @param type
   *             the operation to check for
   * @return {@code true} if <i>any</i> directory allows the specified operation;
   *         otherwise, {@code false}
   */
  private boolean isAllowedInAnyDirectory(final OperationType type) {
    final List<IDirectory> directories = authenticationService.listDirectories();
    for (final IDirectory directory : directories) {
      if (directory.isActive() && directory.getAllowedOperations().contains(type)) {
        return true;
      }
    }

    return false;
  }

  private void schedule(final ISchedulerService schedulerService,
      final IJobRunner runner,
      final JobRunnerKey jobRunnerKey,
      final JobId jobId,
      final RunMode runMode,
      final long intervalInMinutes) throws SchedulerServiceException {
    final long interval = TimeUnit.MINUTES.toMillis(intervalInMinutes);
    schedulerService.registerJobRunner(jobRunnerKey, runner);
    schedulerService.scheduleJob(jobId,
        JobConfig.forJobRunnerKey(jobRunnerKey)
            .withRunMode(runMode)
            .withSchedule(Schedule.forInterval(interval, new Date(System.currentTimeMillis() + interval))));
  }

  private GroupRequest transformGroup(final GroupEntity group) {
    return new DetailedGroupTransform(canDeleteGroups()).apply(group);
  }

  private Page<GroupRequest> transformGroups(final Page<GroupEntity> page) {
    return page.map(new DetailedGroupTransform(canDeleteGroups()));
  }

  private UserRequest transformUser(final IUser user) {
    final UserRequest.Builder builder = this.userService.toUserRequest(user);

    user.accept(new AbstractVoidUserEntityVisitor() {

      @Override
      protected void doVisit(final @Nonnull UserEntity user) {
        convert(user);
      }

      @Override
      protected void doVisit(final User user) {
        convert(user);
      }

      @Override
      protected void doVisit(final IUser user) {
        convert(user);
      }

      private void convert(final IUser user) {
        final IDirectory directory = authenticationService.findDirectoryFor(user);
        if (directory != null) {
          builder.directoryName(directory.getName())
              .deletable(directory.isUserDeletable())
              .updatable(directory.isUserUpdatable())
              .groupUpdatable(directory.isGroupUpdatable());
        } else {
          // maybe orphelan
          builder.deletable(true); // can be deletable
        }

        final Authentication authenticationContext = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationContext != null) {
          builder.authorities(Iterables.transform(authenticationContext.getAuthorities(),
              GrantedAuthority::getAuthority));
        }
        // TODO [devacfr] add last authentication
        // final UserWithAttributes attributes =
        // crowdControl.findUserWithAttributes(user.getUsername());
        // if (attributes != null) {
        // final String value =
        // attributes.getValue(InternalUserService.ATTR_LAST_AUTHENTICATION_TIMESTAMP);
        // if (value != null) {
        // try {
        // builder.lastAuthenticationTimestamp(new Date(Long.parseLong(value)));
        // } catch (final NumberFormatException ignored) {
        // // If the timestamp isn't a number just don't set it
        // }
        // }
        // }
      }

    });

    return builder.build();
  }

  @VisibleForTesting
  void cleanupDeletedGroups() {
    final Date date = Instant.now().minus(Duration.standardMinutes(groupCleanupJobDelay)).toDate();
    final Pageable request = PageUtils.newRequest(0, groupCleanupJobBatchSize);
    boolean hasMore = true;
    while (hasMore) {
      hasMore = requiresNewTransactionTemplate.execute(status -> {
        final Page<GroupEntity> deletedGroups = groupRepository.findByDeletedDateEarlierThan(date, request);
        for (final GroupEntity deletedGroup : deletedGroups.getContent()) {
          cleanupDeletedGroup(deletedGroup);
        }

        return !deletedGroups.isLast();
      });
    }
  }

  private void cleanupDeletedGroup(final GroupEntity deletedGroup) {
    if (authenticationService.findGroup(deletedGroup.getDirectory(), deletedGroup.getName()) == null) {
      // The group still doesn't exist so we should do a clean up
      eventPublisher.publish(new GroupCleanupEvent(this, deletedGroup.getName()));
      groupRepository.delete(deletedGroup);
    } else {
      // Either way this has been restored
      groupRepository.saveAndFlush(deletedGroup.copy().deletedDate(null).build());
    }
  }

  @VisibleForTesting
  void cleanupDeletedUsers() {
    final Date date = Instant.now().minus(Duration.standardMinutes(userCleanupJobDelay)).toDate();
    final Pageable request = PageUtils.newRequest(0, userCleanupJobBatchSize);

    boolean hasMore = true;
    while (hasMore) {
      hasMore = requiresNewTransactionTemplate.execute(status -> {
        // a PageProvider is not used because we update the deleted timestamp as part of
        // the cleanup
        // (see cleanupDeletedUser(User)); as a result we can not transparently page the
        // retrieval of
        // the users using the same criteria
        final Page<UserEntity> users = userRepository.findByDeletedDateEarlierThan(date, request);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Purging {}{} users that have been removed before '{}'",
              users.getNumberOfElements(),
              users.isLast() ? "" : "+",
              date);
        }
        users.getContent().forEach(this::cleanupDeletedUser);

        return !users.isLast();
      });
    }
  }

  private void cleanupDeletedUser(final UserEntity user) {
    // no existing at least one user directory
    if (!authenticationService.existsUser(user.getUsername())) {
      // User is still deleted, do cleanup now
      eventPublisher.publish(new UserCleanupEvent(this, user.copy().build()));
      userRepository.delete(user);
    }

    // In any case, the user should not be processed again by this job, clear
    // deletedDate
    final UserEntity updatedUser = user.copy().deletedDate(null).build();
    userRepository.save(updatedUser);
  }

  /**
   * Job for doing a delayed cleanup of groups. The group cleanup is delayed so
   * that if the group is not available due
   * to some temporary outage we won't loose the configurations associated with
   * the group.
   */
  private class GroupCleanUpJob implements IJobRunner {

    @Nullable
    @Override
    public JobRunnerResponse runJob(@Nonnull final IJobRunnerRequest jobRunnerRequest) {
      cleanupDeletedGroups();
      return JobRunnerResponse.success();
    }
  }

  /**
   * Job for doing the delayed cleanup of users.
   * <p>
   * It uses the {@link UserEntity#getDeletedDate()} timestamp to check if a
   * certain delay has passed after the user
   * was deleted. After processing a user, the timestamp is cleared so that it's
   * not processed again.
   * </p>
   */
  private class UserCleanupJob implements IJobRunner {

    @Override
    public JobRunnerResponse runJob(@Nonnull final IJobRunnerRequest request) {
      cleanupDeletedUsers();
      return JobRunnerResponse.success();
    }
  }

  /**
   * @author Christophe Friederich
   */
  private static final class DetailedGroupTransform implements Function<GroupEntity, GroupRequest> {

    /** */
    private final GroupRequest.Builder builder;

    private DetailedGroupTransform(final boolean deletable) {
      builder = GroupRequest.builder().deletable(deletable);
    }

    @Override
    public GroupRequest apply(final GroupEntity group) {
      return builder.name(group.getName()).active(group.isActive()).build();
    }
  }

}
