package com.pmi.tpd.core.security;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.ApplicationException;
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
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.event.user.GroupDeletedEvent;
import com.pmi.tpd.core.event.user.GroupMembershipCreatedEvent;
import com.pmi.tpd.core.event.user.UserCreatedEvent;
import com.pmi.tpd.core.event.user.UserDeletedEvent;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.security.provider.IAuthenticationProvider;
import com.pmi.tpd.core.security.provider.IAuthenticationProviderService;
import com.pmi.tpd.core.user.IGroup;
import com.pmi.tpd.core.user.UserPreferenceKeys;
import com.pmi.tpd.core.user.preference.IPreferences;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;

/**
 * Default implementation of interface {@link IAuthenticationSynchroniser}.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
@Singleton
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultAuthenticationSynchroniser implements IAuthenticationSynchroniser, IScheduledJobSource {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationSynchroniser.class);

    /** */
    private static final JobId GROUP_DELETED_JOB_ID = JobId.of(DefaultAuthenticationSynchroniser.class.getSimpleName());

    /** */
    private static final JobRunnerKey GROUP_DELELED_JOB_RUNNER_KEY = JobRunnerKey
            .of(DefaultAuthenticationSynchroniser.class.getName());

    /** */
    private volatile IUserRepository userRepository;

    /** */
    private volatile IGroupRepository groupRepository;

    /** */
    private volatile IUserPreferencesManager userPreferencesManager;

    /** */
    private volatile IEventPublisher eventPublisher;

    /** */
    private volatile IAuthenticationProviderService authenticationProviderService;

    /**
     * Controls how frequently the job to check deleted groups is run.
     * <p>
     * Value is in MINUTES.
     * </p>
     */
    @Value("${security.synchronization.group.check.deleted.job.interval:60}")
    private long groupCheckDeletedJobInterval = 60;

    /**
     * Controls the number of groups to load in memory.
     */
    @Value("${security.synchronization.group.check.deleted.job.batch.size:100}")
    private int groupCheckDeletedJobBatchSize = 100;

    /**
     * Default constructory.
     *
     * @param authenticationProviderService
     *            the authentication provider service.
     * @param userRepository
     *            user repository.
     * @param groupRepository
     *            group repository
     * @param userPreferencesManager
     *            user preference manager.
     * @param eventPublisher
     *            the event publisher.
     */
    public DefaultAuthenticationSynchroniser(
            @Nonnull final IAuthenticationProviderService authenticationProviderService,
            @Nonnull final IUserRepository userRepository, @Nonnull final IGroupRepository groupRepository,
            @Nonnull final IUserPreferencesManager userPreferencesManager,
            @Nonnull final IEventPublisher eventPublisher) {
        super();
        this.authenticationProviderService = checkNotNull(authenticationProviderService,
            "authenticationProviderService");
        this.userRepository = checkNotNull(userRepository, "userRepository");
        this.groupRepository = checkNotNull(groupRepository, "groupRepository");
        this.userPreferencesManager = checkNotNull(userPreferencesManager, "userPreferencesManager");
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
    }

    /**
     * @param value
     *            a scheduling interval in minutes.
     * @return Returns a fluent instance.
     */
    public DefaultAuthenticationSynchroniser withGroupCheckDeletedJobInterval(final long value) {
        this.groupCheckDeletedJobInterval = value;
        return this;
    }

    /**
     * @param value
     *            the number of groups to load in memory
     * @return Returns a fluent instance.
     */
    public DefaultAuthenticationSynchroniser withGroupCheckDeletedJobBatchSize(final int value) {
        this.groupCheckDeletedJobBatchSize = value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        schedule(schedulerService,
            new GroupCheckDeletedJob(),
            GROUP_DELELED_JOB_RUNNER_KEY,
            GROUP_DELETED_JOB_ID,
            RunMode.RUN_ONCE_PER_CLUSTER,
            groupCheckDeletedJobInterval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        schedulerService.unregisterJobRunner(GROUP_DELELED_JOB_RUNNER_KEY);
    }

    @Override
    @Transactional
    public IUser synchronise(final String username,
        final Set<String> authorities,
        final IAuthenticationProvider provider) {

        if (!provider.getDirectory().isActive()) {
            return null;
        }
        // search user from directory;
        // external provider used to synchronize user.
        final IUser user = provider.findUserByName(username);

        if (user == null) {
            if (userRepository.existsUser(username)) {
                // if user exists in application but not in user directory, then delete user
                // maybe user delele cleanup user to set user as deleted.
                this.eventPublisher.publish(new UserDeletedEvent(this, username, provider.getDirectory()));
            }
        } else {
            // else synchronise
            return synchroniseInternalUser(user, authorities);
        }
        return null;
    }

    /**
     * Synchronise user with associated authorities group.
     *
     * @param user
     *            the user to synchronize.
     * @param authorities
     *            list of authorities group name.
     * @return Returns a new instance of {@link IUser} synchronised with external authorisation
     */
    protected IUser synchroniseInternalUser(@Nonnull final IUser user, final Set<String> authorities) {
        checkNotNull(user, "authenticateUser");
        final UserEntity entity = userRepository.findByName(user.getUsername());
        // skip user if internal
        if (entity != null && UserDirectory.Internal.equals(entity.getDirectory())) {
            return entity;
        }

        final DateTime lastUpdate = getLastUpdate(entity);

        // skip synchronize for duration 10 min
        if (lastUpdate != null && lastUpdate.plus(Period.minutes(10)).isAfterNow()) {
            return entity;
        }
        UserEntity returnedUser = null;
        if (entity == null) {
            // user doesn't exist internally, should create one.
            returnedUser = this.userRepository.save(UserEntity.builder()
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .activated(true)
                    .directory(user.getDirectory())
                    .username(user.getUsername())
                    .password("NO_PASSWORD")
                    .build());
            eventPublisher.publish(new UserCreatedEvent(this, returnedUser));
        } else {
            // update existing user
            returnedUser = userRepository.save(entity.copy()
                    .displayName(user.getDisplayName())
                    .email(user.getEmail())
                    .deletedDate(null) // User was maybe previously marked as deleted, remove that
                    .build());
        }

        returnedUser = this.userRepository.save(synchronizeUserInGroups(returnedUser, authorities));

        // update the last update date
        storeLastUpdate(returnedUser, DateTime.now());

        return returnedUser;
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

    /**
     * Job for doing a delayed cleanup of groups. The group cleanup is delayed so that if the group is not available due
     * to some temporary outage we won't loose the configurations associated with the group.
     */
    private class GroupCheckDeletedJob implements IJobRunner {

        @Nullable
        @Override
        public JobRunnerResponse runJob(@Nonnull final IJobRunnerRequest jobRunnerRequest) {
            checkDeletedGroups();
            return JobRunnerResponse.success();
        }
    }

    @VisibleForTesting
    void checkDeletedGroups() {
        if (!this.authenticationProviderService.isStarted()
                || !this.authenticationProviderService.hasExternalProvider()) {
            return;
        }
        Pageable request = PageUtils.newRequest(0, groupCheckDeletedJobBatchSize);
        boolean hasNext = true;
        while (hasNext) {
            final Page<GroupEntity> groups = this.groupRepository.findAll(request);
            for (final GroupEntity group : groups) {
                final UserDirectory userDirectory = group.getDirectory();
                final IAuthenticationProvider provider = authenticationProviderService
                        .getAuthenticationProvider(userDirectory)
                        .orElse(null);
                if (provider == null || provider.isInternal()) {
                    continue;
                }

                final IGroup externalGroup = provider.findGroupByName(group.getName());
                if (externalGroup == null) {
                    // the group doesn't exist in directory anymore
                    this.eventPublisher.publish(new GroupDeletedEvent(this, group.getName(), provider.getDirectory()));
                } else {
                    // the group exist in internal directory but it is inactive
                    if (!group.isActive()) {
                        this.eventPublisher.publish(
                            new GroupMembershipCreatedEvent(this, provider.getDirectory(), group.getName()));
                    }
                }
            }
            hasNext = groups.hasNext();
            if (hasNext) {
                request = request.next();
            }
        }
    }

    private UserEntity synchronizeUserInGroups(final UserEntity user, final Set<String> authorities) {
        final List<GroupEntity> toRemoveGroups = Lists.newArrayList();
        final List<GroupEntity> toAddGroups = Lists.newArrayList();
        final Set<GroupEntity> groups = user.getGroups();
        final Set<String> groupNames = from(groups).transform(grp -> grp.getName()).toSet();

        // add
        for (final String groupName : authorities) {
            if (this.groupRepository.exists(groupName) && !groupNames.contains(groupName)) {
                final GroupEntity group = this.groupRepository.findByName(groupName);
                toAddGroups.add(group);
            }
        }
        // remove
        for (final GroupEntity grp : groups) {
            // remove only external group
            if (!UserDirectory.Internal.equals(grp.getDirectory()) && !authorities.contains(grp.getName())) {
                toRemoveGroups.add(grp);
            }
        }
        for (final GroupEntity grp : toRemoveGroups) {
            user.getGroups().remove(grp);
        }
        for (final GroupEntity grp : toAddGroups) {
            user.getGroups().add(grp);
        }
        return user;
    }

    @VisibleForTesting
    protected DateTime getLastUpdate(final IUser user) {
        DateTime lastUpdate = null;
        try {
            if (user != null) {
                final Optional<IPreferences> preferences = this.userPreferencesManager.getPreferences(user);
                if (preferences.isPresent() && preferences.get().exists(UserPreferenceKeys.USER_LAST_UPDATE)) {
                    lastUpdate = preferences.get()
                            .getDate(UserPreferenceKeys.USER_LAST_UPDATE)
                            .map(DateTime::new)
                            .orElse(null);
                }
            }
        } catch (final ApplicationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return lastUpdate;
    }

    private void storeLastUpdate(final IUser user, final DateTime lastUpdate) {
        final Optional<IPreferences> pref = this.userPreferencesManager.getPreferences(user);
        try {
            if (pref.isPresent()) {
                pref.get().setDate(UserPreferenceKeys.USER_LAST_UPDATE, lastUpdate.toDate());
            }
        } catch (final ApplicationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
