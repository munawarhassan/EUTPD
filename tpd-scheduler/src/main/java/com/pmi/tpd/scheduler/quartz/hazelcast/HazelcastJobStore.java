package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;
import org.quartz.utils.Key;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.1
 */
public class HazelcastJobStore implements JobStore {

    /** */
    public static final String MAP_QUARTZ_JOBSTORE_CALENDARS = "quartz.jobStore.calendars";

    /** */
    public static final String MAP_QUARTZ_JOBSTORE_JOBS = "quartz.jobStore.jobs";

    /** */
    public static final String MAP_QUARTZ_JOBSTORE_TRIGGERS = "quartz.jobStore.triggers";

    /** */
    private static final Function<Key<?>, String> TO_GROUP = Key::getGroup;

    /** */
    private static final Function<Trigger, TriggerKey> TO_TRIGGER_KEY = Trigger::getKey;

    /** */
    private final IMap<String, Calendar> nameToCalendar;

    /** */
    private final IMap<JobKey, JobConfig> jobKeyToConfig;

    /** */
    private final IMap<TriggerKey, AbstractTriggerConfig> triggerKeyToConfig;

    /** */
    private SchedulerSignaler signaler;

    /** */
    private long misfireThreshold = 5000L;

    private long retryInterval;

    // Quartz requires a zero arg constructor
    public HazelcastJobStore() {
        this(Hazelcast.getAllHazelcastInstances().iterator().next());
    }

    protected HazelcastJobStore(final HazelcastInstance hazelcast) {
        nameToCalendar = hazelcast.getMap(MAP_QUARTZ_JOBSTORE_CALENDARS);
        jobKeyToConfig = hazelcast.getMap(MAP_QUARTZ_JOBSTORE_JOBS);
        triggerKeyToConfig = hazelcast.getMap(MAP_QUARTZ_JOBSTORE_TRIGGERS);
    }

    @Override
    public void initialize(final ClassLoadHelper loadHelper, final SchedulerSignaler signaler)
            throws SchedulerConfigException {
        this.signaler = signaler;
    }

    @Override
    public void schedulerPaused() {
    }

    @Override
    public void schedulerResumed() {
    }

    @Override
    public void schedulerStarted() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean supportsPersistence() {
        return false;
    }

    @Override
    public long getEstimatedTimeToReleaseAndAcquireTrigger() {
        // Quartz doesn't appear to use this method
        return 0;
    }

    public long getMisfireThreshold() {
        return misfireThreshold;
    }

    @Override
    public void pauseAll() throws JobPersistenceException {
        pauseTriggers(GroupMatcher.anyTriggerGroup());
    }

    @Override
    public boolean replaceTrigger(final TriggerKey triggerKey, final OperableTrigger newTrigger)
            throws JobPersistenceException {
        if (removeTrigger(triggerKey)) {
            storeTrigger(newTrigger, false);

            return true;
        }
        return false;
    }

    @Override
    public void storeJobAndTrigger(final JobDetail newJob, final OperableTrigger newTrigger)
            throws JobPersistenceException {
        storeJob(newJob, false);
        storeTrigger(newTrigger, false);
    }

    @Override
    public void storeJobsAndTriggers(final Map<JobDetail, Set<? extends Trigger>> triggersAndJobs,
        final boolean replace) throws JobPersistenceException {
        for (final Map.Entry<JobDetail, Set<? extends Trigger>> entry : triggersAndJobs.entrySet()) {
            storeJob(entry.getKey(), replace);
            storeTrigger((OperableTrigger) entry.getValue(), replace);
        }
    }

    @Override
    public boolean removeJobs(final List<JobKey> jobKeys) throws JobPersistenceException {
        boolean removed = true;

        for (final JobKey jobKey : jobKeys) {
            removed &= removeJob(jobKey);
        }

        return removed;
    }

    @Override
    public boolean removeTriggers(final List<TriggerKey> triggerKeys) throws JobPersistenceException {
        boolean removed = true;

        for (final TriggerKey triggerKey : triggerKeys) {
            removed &= removeTrigger(triggerKey);
        }

        return removed;
    }

    @Override
    public void resumeAll() throws JobPersistenceException {
        resumeTriggers(GroupMatcher.anyTriggerGroup());
    }

    @Override
    public void setInstanceId(final String instanceId) {
    }

    @Override
    public void setThreadPoolSize(final int poolSize) {
    }

    @Override
    public void setInstanceName(final String instanceName) {
    }

    // Invoked via reflection
    public void setMisfireThreshold(final long misfireThreshold) {
        this.misfireThreshold = misfireThreshold;
    }

    @Override
    public List<OperableTrigger> acquireNextTriggers(final long noLaterThan, final int maxCount, final long timeWindow)
            throws JobPersistenceException {
        final Predicate<TriggerKey, AbstractTriggerConfig> predicate = Predicates.and(
            matchesNextFire(noLaterThan, timeWindow),
            Predicates.equal(AbstractTriggerConfig.ATTR_STATE, Trigger.TriggerState.NORMAL));

        // QuartzSchedulerThread.releaseIfScheduleChangedSignificantly stupidly decides to call .clear() on a list
        // returned before it then throws it away
        final List<OperableTrigger> acquiredTriggers = new ArrayList<>();

        for (final TriggerKey triggerKey : triggerKeyToConfig.keySet(predicate)) {
            // IMap locks are re-entrant by default, but we want to pretend like they aren't so we
            // check if it is locked before we try and lock it. It also makes unit testing a lot easier
            if (!triggerKeyToConfig.isLocked(triggerKey) && triggerKeyToConfig.tryLock(triggerKey)) {
                final OperableTrigger trigger = retrieveTrigger(triggerKey);
                if (trigger == null || applyMisfire(trigger)) {
                    // The trigger shouldn't be null but lets be defensive to make sure we
                    // don't end up with a triggerId permanently locked. The trigger may have
                    // also misfired, we need to unlock it when this happens
                    triggerKeyToConfig.unlock(triggerKey);
                } else {
                    acquiredTriggers.add(trigger);
                }
            }
        }

        return acquiredTriggers;
    }

    @Override
    public List<String> getCalendarNames() {
        return ImmutableList.copyOf(nameToCalendar.keySet());
    }

    @Override
    public List<String> getJobGroupNames() throws JobPersistenceException {
        final Set<String> groupNames = ImmutableSet.copyOf(Iterables.transform(jobKeyToConfig.keySet(), TO_GROUP));

        return ImmutableList.copyOf(groupNames);
    }

    @Override
    public Set<JobKey> getJobKeys(final GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return ImmutableSet.copyOf(jobKeyToConfig.keySet(new GroupMatcherPredicate<JobKey, JobConfig>(matcher)));
    }

    @Override
    public int getNumberOfCalendars() {
        return nameToCalendar.size();
    }

    @Override
    public int getNumberOfJobs() throws JobPersistenceException {
        return jobKeyToConfig.size();
    }

    @Override
    public int getNumberOfTriggers() {
        return triggerKeyToConfig.size();
    }

    @Override
    public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
        return ImmutableSet.copyOf(Iterables.transform(
            triggerKeyToConfig.keySet(Predicates.equal(AbstractTriggerConfig.ATTR_STATE, Trigger.TriggerState.PAUSED)),
            TO_GROUP));
    }

    @Override
    public List<String> getTriggerGroupNames() throws JobPersistenceException {
        final Set<String> groupNames = ImmutableSet.copyOf(Iterables.transform(triggerKeyToConfig.keySet(), TO_GROUP));

        return ImmutableList.copyOf(groupNames);
    }

    @Override
    public Set<TriggerKey> getTriggerKeys(final GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return ImmutableSet.copyOf(
            triggerKeyToConfig.keySet(new GroupMatcherPredicate<TriggerKey, AbstractTriggerConfig>(matcher)));
    }

    @Override
    public List<OperableTrigger> getTriggersForJob(final JobKey jobKey) throws JobPersistenceException {
        final Set<Map.Entry<TriggerKey, AbstractTriggerConfig>> entries = triggerKeyToConfig
                .entrySet(matchesJob(jobKey));

        final ImmutableList.Builder<OperableTrigger> builder = ImmutableList.builder();

        for (final Map.Entry<TriggerKey, AbstractTriggerConfig> entry : entries) {
            final TriggerKey triggerKey = entry.getKey();
            builder.add(entry.getValue().toTrigger(triggerKey));
        }

        return builder.build();
    }

    @Override
    public Trigger.TriggerState getTriggerState(final TriggerKey triggerKey) throws JobPersistenceException {
        final AbstractTriggerConfig config = triggerKeyToConfig.get(triggerKey);

        return config == null ? Trigger.TriggerState.NONE : config.getState();
    }

    @Override
    public boolean isClustered() {
        return true;
    }

    @Override
    public void pauseJob(final JobKey jobKey) throws JobPersistenceException {
        triggerKeyToConfig.executeOnEntries(new StateChangeTriggerProcessor(Trigger.TriggerState.PAUSED),
            matchesJob(jobKey));
    }

    @Override
    public Collection<String> pauseJobs(final GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return triggerKeyToConfig
                .executeOnEntries(new JobStateChangeTriggerProcessor(Trigger.TriggerState.PAUSED),
                    new JobKeyGroupMatcherPredicate(matcher))
                .values()
                .stream()
                .filter(com.google.common.base.Predicates.notNull())
                .map(key -> ((Key<?>) key).getGroup())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void pauseTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
        triggerKeyToConfig.executeOnKey(triggerKey, new StateChangeTriggerProcessor(Trigger.TriggerState.PAUSED));
    }

    @Override
    public Collection<String> pauseTriggers(final GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return triggerKeyToConfig
                .executeOnEntries(new StateChangeTriggerProcessor(Trigger.TriggerState.PAUSED),
                    new GroupMatcherPredicate<TriggerKey, AbstractTriggerConfig>(matcher))
                .keySet()
                .stream()
                .map(TO_GROUP)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void releaseAcquiredTrigger(final OperableTrigger trigger) {
        triggerKeyToConfig.unlock(trigger.getKey());
    }

    @Override
    public boolean removeCalendar(final String name) {
        return nameToCalendar.remove(name) != null;
    }

    @Override
    public boolean removeJob(final JobKey jobKey) throws JobPersistenceException {
        return jobKeyToConfig.remove(jobKey) != null;
    }

    @Override
    public boolean removeTrigger(final TriggerKey triggerKey) {
        return triggerKeyToConfig.remove(triggerKey) != null;
    }

    @Override
    public void resumeJob(final JobKey jobKey) throws JobPersistenceException {
        triggerKeyToConfig.executeOnEntries(new StateChangeTriggerProcessor(Trigger.TriggerState.NORMAL),
            matchesJob(jobKey));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> resumeJobs(final GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        // noinspection unchecked
        @SuppressWarnings("rawtypes")
        final Collection resumedGroups = triggerKeyToConfig
                .executeOnEntries(new JobStateChangeTriggerProcessor(Trigger.TriggerState.NORMAL),
                    new JobKeyGroupMatcherPredicate(matcher))
                .values();

        return ImmutableSet.copyOf(Iterables
                .transform(Iterables.filter(resumedGroups, com.google.common.base.Predicates.notNull()), TO_GROUP));
    }

    @Override
    public void resumeTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
        triggerKeyToConfig.executeOnKey(triggerKey, new StateChangeTriggerProcessor(Trigger.TriggerState.NORMAL));
    }

    @Override
    public Collection<String> resumeTriggers(final GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        final Set<TriggerKey> triggerKeys = triggerKeyToConfig
                .executeOnEntries(new StateChangeTriggerProcessor(Trigger.TriggerState.NORMAL),
                    new GroupMatcherPredicate<TriggerKey, AbstractTriggerConfig>(matcher))
                .keySet();

        return ImmutableSet.copyOf(Iterables.transform(triggerKeys, TO_GROUP));
    }

    @Override
    public Calendar retrieveCalendar(final String name) {
        return nameToCalendar.get(name);
    }

    @Override
    public JobDetail retrieveJob(final JobKey jobKey) throws JobPersistenceException {
        final JobConfig config = jobKeyToConfig.get(jobKey);

        if (config == null) {
            return null;
        }

        return config.toJobDetail(jobKey);
    }

    @Override
    public OperableTrigger retrieveTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
        final AbstractTriggerConfig config = triggerKeyToConfig.get(triggerKey);
        if (config == null) {
            return null;
        }

        return config.toTrigger(triggerKey);
    }

    @Override
    public void storeCalendar(final String name,
        final Calendar calendar,
        final boolean replaceExisting,
        final boolean updateTriggers) throws ObjectAlreadyExistsException {
        if (replaceExisting) {
            nameToCalendar.set(name, calendar);
        } else {
            if (nameToCalendar.putIfAbsent(name, calendar) != null) {
                throw new ObjectAlreadyExistsException("Calendar with name '" + name + "' already exists.");
            }
        }

        if (updateTriggers) {
            triggerKeyToConfig.executeOnEntries(new CalendarChangeTriggerProcessor(5000L, calendar),
                Predicates.equal(AbstractTriggerConfig.ATTR_CALENDAR, name));
        }
    }

    @Override
    public void storeJob(final JobDetail newJob, final boolean replaceExisting) throws JobPersistenceException {
        if (newJob.isPersistJobDataAfterExecution()) {
            throw new JobPersistenceException("Stateful jobs are not supported");
        }

        final JobConfig newConfig = new JobConfig.Builder(newJob).build();

        if (replaceExisting) {
            jobKeyToConfig.set(newJob.getKey(), newConfig);
        } else if (jobKeyToConfig.putIfAbsent(newJob.getKey(), newConfig) != null) {
            throw new JobPersistenceException("Job already exists for ID " + newJob.getKey());
        }
    }

    @Override
    public void storeTrigger(final OperableTrigger newTrigger, final boolean replaceExisting)
            throws JobPersistenceException {
        final AbstractTriggerConfig newConfig = AbstractTriggerConfig.fromTrigger(newTrigger,
            Trigger.TriggerState.NORMAL);

        if (newConfig == null) {
            throw new JobPersistenceException("Cannot store triggers of type " + newTrigger.getClass());
        }

        if (replaceExisting) {
            triggerKeyToConfig.set(newTrigger.getKey(), newConfig);
        } else if (triggerKeyToConfig.putIfAbsent(newTrigger.getKey(), newConfig) != null) {
            throw new JobPersistenceException("Trigger already exists for ID " + newTrigger.getKey());
        }
    }

    @Override
    public void triggeredJobComplete(final OperableTrigger trigger,
        final JobDetail jobDetail,
        final Trigger.CompletedExecutionInstruction triggerInstCode) {
        final TriggerKey triggerKey = trigger.getKey();
        boolean signalSchedulingChange = false;
        switch (triggerInstCode) {
            case DELETE_TRIGGER:
                if (trigger.getNextFireTime() == null) {
                    triggerKeyToConfig.executeOnKey(triggerKey, new RemoveCompleteTriggerProcessor());
                } else {
                    removeTrigger(triggerKey);
                    signalSchedulingChange = true;
                }
                break;
            case SET_TRIGGER_COMPLETE:
                triggerKeyToConfig.executeOnKey(triggerKey,
                    new StateChangeTriggerProcessor(Trigger.TriggerState.COMPLETE));
                signalSchedulingChange = true;
                break;
            case SET_TRIGGER_ERROR:
                triggerKeyToConfig.executeOnKey(triggerKey,
                    new StateChangeTriggerProcessor(Trigger.TriggerState.ERROR));
                signalSchedulingChange = true;
                break;
            case SET_ALL_JOB_TRIGGERS_COMPLETE:
                triggerKeyToConfig.executeOnEntries(new StateChangeTriggerProcessor(Trigger.TriggerState.COMPLETE),
                    matchesJob(trigger.getJobKey()));
                signalSchedulingChange = true;
                break;
            case SET_ALL_JOB_TRIGGERS_ERROR:
                triggerKeyToConfig.executeOnEntries(new StateChangeTriggerProcessor(Trigger.TriggerState.ERROR),
                    matchesJob(trigger.getJobKey()));
                signalSchedulingChange = true;
                break;
            default:
                break;
        }
        if (signalSchedulingChange) {
            signaler.signalSchedulingChange(0L);
        }
    }

    @Override
    public List<TriggerFiredResult> triggersFired(final List<OperableTrigger> triggers) throws JobPersistenceException {
        final Map<TriggerKey, OperableTrigger> triggerIdToTrigger = Maps.uniqueIndex(triggers, TO_TRIGGER_KEY);
        final ImmutableList.Builder<TriggerFiredResult> builder = ImmutableList.builder();

        for (final Map.Entry<TriggerKey, AbstractTriggerConfig> entry : triggerKeyToConfig
                .getAll(triggerIdToTrigger.keySet())
                .entrySet()) {
            final TriggerKey triggerKey = entry.getKey();
            final OperableTrigger trigger = triggerIdToTrigger.get(triggerKey);
            final AbstractTriggerConfig config = entry.getValue();

            // Ignore triggers which have been deleted or state has changed
            if (config.getState() != Trigger.TriggerState.NORMAL) {
                continue;
            }

            // If the calendar has been deleted, ignore it
            Calendar calendar = null;
            if (config.getCalendarName() != null) {
                calendar = retrieveCalendar(config.getCalendarName());
                if (calendar == null) {
                    continue;
                }
            }

            if (Boolean.FALSE.equals(triggerKeyToConfig.executeOnKey(triggerKey,
                new TriggerFiredProcessor(calendar, config.getCalendarName())))) {
                // The trigger was not successfully fired, ignore it
                continue;
            }

            // The trigger fired. Release the lock!
            releaseAcquiredTrigger(trigger);

            final Date prevFireTime = trigger.getPreviousFireTime();
            // Perform the same triggered operation on the local trigger
            trigger.triggered(calendar);

            builder.add(
                new TriggerFiredResult(new TriggerFiredBundle(retrieveJob(trigger.getJobKey()), trigger, calendar,
                        false, new Date(), trigger.getPreviousFireTime(), prevFireTime, trigger.getNextFireTime())));

        }

        return builder.build();
    }

    @Override
    public boolean checkExists(final JobKey jobKey) {
        return jobKeyToConfig.containsKey(jobKey);
    }

    @Override
    public boolean checkExists(final TriggerKey triggerKey) {
        return triggerKeyToConfig.containsKey(triggerKey);
    }

    @Override
    public void clearAllSchedulingData() throws JobPersistenceException {
        triggerKeyToConfig.clear();
        jobKeyToConfig.clear();
        nameToCalendar.clear();
    }

    @Override
    public void resetTriggerFromErrorState(final TriggerKey triggerKey) throws JobPersistenceException {
        // noop, already done
    }

    @Override
    public long getAcquireRetryDelay(final int failureCount) {
        return retryInterval;
    }

    private boolean applyMisfire(final OperableTrigger trigger) {

        long misfireTime = System.currentTimeMillis();
        if (getMisfireThreshold() > 0) {
            misfireTime -= getMisfireThreshold();
        }

        final Date nextFireTime = trigger.getNextFireTime();
        if (nextFireTime == null || nextFireTime.getTime() > misfireTime) {
            return false;
        }

        // We have a misfire!

        Calendar cal = null;
        if (trigger.getCalendarName() != null) {
            cal = retrieveCalendar(trigger.getCalendarName());
        }

        signaler.notifyTriggerListenersMisfired((Trigger) trigger.clone());
        trigger.updateAfterMisfire(cal);

        Trigger.TriggerState triggerState = Trigger.TriggerState.NORMAL;
        boolean misfired = true;

        if (trigger.getNextFireTime() == null) {
            triggerState = Trigger.TriggerState.COMPLETE;
            signaler.notifySchedulerListenersFinalized((Trigger) trigger.clone());
        } else if (nextFireTime.equals(trigger.getNextFireTime())) {
            misfired = false;
        }

        triggerKeyToConfig.set(trigger.getKey(), AbstractTriggerConfig.fromTrigger(trigger, triggerState));

        return misfired;
    }

    private static Predicate<TriggerKey, AbstractTriggerConfig> matchesJob(final JobKey jobKey) {
        return Predicates.and(matchesJobGroup(jobKey.getGroup()), matchesJobName(jobKey.getName()));
    }

    private static Predicate<TriggerKey, AbstractTriggerConfig> matchesJobName(final String jobName) {
        return Predicates.equal(AbstractTriggerConfig.ATTR_JOB_NAME, jobName);
    }

    private static Predicate<TriggerKey, AbstractTriggerConfig> matchesJobGroup(final String groupName) {
        return Predicates.equal(AbstractTriggerConfig.ATTR_JOB_GROUP, groupName);
    }

    private static Predicate<?, ?> matchesNextFire(final long noLaterThan, final long timeWindow) {
        return Predicates.and(Predicates.lessEqual(AbstractTriggerConfig.ATTR_NEXT_FIRE_TIME, noLaterThan + timeWindow),
            Predicates.notEqual(AbstractTriggerConfig.ATTR_NEXT_FIRE_TIME, AbstractTriggerConfig.NO_FIRE_TIME));
    }

}
