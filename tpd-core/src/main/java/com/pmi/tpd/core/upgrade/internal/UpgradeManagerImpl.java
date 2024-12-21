package com.pmi.tpd.core.upgrade.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.ApplicationConstants.PropertyKeys;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.model.upgrade.UpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion;
import com.pmi.tpd.core.upgrade.BuildNumComparator;
import com.pmi.tpd.core.upgrade.IUpgradeHistoryItem;
import com.pmi.tpd.core.upgrade.IUpgradeManager;
import com.pmi.tpd.core.upgrade.IUpgradeTask;
import com.pmi.tpd.core.upgrade.IUpgradeTask.SetupType;
import com.pmi.tpd.core.versioning.BuildUtils;
import com.pmi.tpd.core.versioning.BuildVersion;
import com.pmi.tpd.core.versioning.impl.BuildVersionImpl;

/**
 * <p>
 * UpgradeManagerImpl class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class UpgradeManagerImpl implements IUpgradeManager {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeManagerImpl.class);

    /** */
    private static final Comparator<String> BUILD_NUMBER_COMPARATOR = new BuildNumComparator();

    /** */
    private final IApplicationProperties applicationProperties;

    /** */
    private final IApplicationConfiguration applicationConfiguration;

    /** */
    private final IBuildUtilsInfo buildUtilsInfo;

    /** */
    private final IUpgradeStore upgradeStore;

    /** */
    private final SortedMap<String, IUpgradeTask> allUpgrades = new TreeMap<>(
            BUILD_NUMBER_COMPARATOR);

    /** */
    private final SortedMap<String, IUpgradeTask> setupUpgrades = new TreeMap<>(
            BUILD_NUMBER_COMPARATOR);

    /** */
    private Map<String, UpgradeHistory> upgradeHistoryMap;

    /**
     * <p>
     * Constructor for UpgradeManagerImpl.
     * </p>
     *
     * @param upgradeStore
     *            a {@link com.pmi.tpd.core.upgrade.internal.IUpgradeStore} object.
     * @param buildUtilsInfo
     *            a {@link com.pmi.tpd.api.versioning.IBuildUtilsInfo} object.
     * @param applicationProperties
     *            a {@link com.pmi.tpd.api.context.IApplicationProperties} object.
     * @param applicationConfiguration
     *            a {@link com.pmi.tpd.core.IGlobalApplicationProperties} object.
     * @param upgradeList
     *            a {@link java.util.List} object.
     */
    @Inject
    public UpgradeManagerImpl(@Nonnull final IUpgradeStore upgradeStore, @Nonnull final IBuildUtilsInfo buildUtilsInfo,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IApplicationConfiguration applicationConfiguration,
            @Nullable final List<IUpgradeTask> upgradeList) {
        this.buildUtilsInfo = Assert.notNull(buildUtilsInfo);
        this.upgradeStore = Assert.notNull(upgradeStore);
        this.applicationProperties = Assert.notNull(applicationProperties);
        this.applicationConfiguration = Assert.notNull(applicationConfiguration);

        // add all the upgrade tasks in here.
        addAllUpgrades(allUpgrades,
            setupUpgrades,
            upgradeList != null ? upgradeList : ImmutableList.<IUpgradeTask> of());
    }

    /**
     * For testing purposes only.
     */
    @VisibleForTesting
    UpgradeManagerImpl(@Nonnull final IUpgradeStore upgradeStore, @Nonnull final IBuildUtilsInfo buildUtilsInfo,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IApplicationConfiguration applicationConfiguration,
            @Nonnull final Collection<? extends IUpgradeTask> upgradeTasks) {
        this.buildUtilsInfo = Assert.notNull(buildUtilsInfo);
        this.upgradeStore = Assert.notNull(upgradeStore);
        this.applicationProperties = Assert.notNull(applicationProperties);
        this.applicationConfiguration = Assert.notNull(applicationConfiguration);

        for (final IUpgradeTask upgradeTask : Assert.notNull(upgradeTasks)) {
            allUpgrades.put(upgradeTask.getBuildNumber(), upgradeTask);
        }
    }

    /**
     * <p>
     * onClearCache.
     * </p>
     *
     * @param event
     *            a {@link com.pmi.tpd.api.lifecycle.ClearCacheEvent} object.
     */
    @EventListener
    public void onClearCache(final ClearCacheEvent event) {
        // TODO: NOT_THREAD_SAFE
        upgradeHistoryMap = null;
    }

    /**
     * Gets a set of all the build numbers for which upgrade tasks must be performed. It will only return numbers which
     * are greater than the current build number
     * <p/>
     * The set will be sorted by ascending build number
     */
    private SortedSet<String> getAllRelevantUpgradeBuildNumbers() {
        final SortedSet<String> numbers = new TreeSet<>(BUILD_NUMBER_COMPARATOR);

        final Map<String, IUpgradeTask> standardupgrades = getRelevantUpgradesFromList(allUpgrades);

        addUpgradeNumbersFromMap(numbers, standardupgrades);

        return numbers;
    }

    /**
     * Gets a set of all the build numbers for which upgrade tasks must be performed by Setup task.
     * <p/>
     * The set will be sorted by ascending build number
     */
    private SortedSet<String> getSetupUpgradeBuildNumbers() {
        final SortedSet<String> numbers = new TreeSet<>(BUILD_NUMBER_COMPARATOR);

        addUpgradeNumbersFromMap(numbers, setupUpgrades);

        return numbers;
    }

    /**
     * Gets a set of upgrade build numbers for a specified Map of upgradeTasks.
     *
     * @param numbers
     *            This set may be already populated with numbers. Any additional numbers from the Map of upgradeTasks
     *            will be added to this set
     * @param upgradeMap
     *            This is the map of upgradeTasks that the build numbers will be retrieved from
     */
    private void addUpgradeNumbersFromMap(final SortedSet<String> numbers, final Map<String, IUpgradeTask> upgradeMap) {
        for (final String buildNumber : upgradeMap.keySet()) {
            numbers.add(buildNumber);
        }
    }

    /**
     * Adds these upgrades the upgradeSet and setupSet accordingly with list of all the upgrades specified.
     * <p/>
     * For every <i>upgrade</i> element, load the class specified by the <i>class</i> element. Put this class into the
     * upgradeSet Map with the <i>build</i> attribute as the key.
     * <p/>
     */
    private void addAllUpgrades(@Nonnull final Map<String, IUpgradeTask> upgradeSet,
        @Nonnull final Map<String, IUpgradeTask> setupSet,
        @Nonnull final List<IUpgradeTask> tasks) {

        for (final IUpgradeTask upgradeTask : tasks) {
            if (SetupType.onlySetup.equals(upgradeTask.getSetup())
                    || SetupType.setupAndBuild.equals(upgradeTask.getSetup())) {
                setupSet.put(upgradeTask.getBuildNumber(), upgradeTask);
            } else {
                upgradeSet.put(upgradeTask.getBuildNumber(), upgradeTask);
            }

        }
    }

    /**
     * Returns true if the current build number is not equal to the build number in the database. NB - There may not be
     * any upgrades to run. However, you will need to run doUpgrade() to increment the build number in the database.
     */
    private boolean needUpgrade() {
        return !buildUtilsInfo.getCurrentBuildNumber().equals(getBuildNumber());

    }

    /**
     * {@inheritDoc} Performs the upgrade if one is required.
     */
    @Override
    public Collection<String> doUpgradeIfNeededAndAllowed() {
        return doUpgradeIfNeeded();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needSetup() {
        return !applicationProperties.isSetup() && !applicationProperties.isAutoSetup();
    }

    private Collection<String> doUpgradeIfNeeded() {
        if (needSetup() && !applicationConfiguration.isDevMode()) {
            // if need setup and application doesn't set in development mode.
            LOGGER.debug("not setup yet - not upgrading");
        } else if (needUpgrade()) {
            LOGGER.info("Detected that an upgrade is needed; existing data at build " + getBuildNumber());
            return doUpgrade();
        } else {
            LOGGER.debug("Detected that no upgrade is neccessary");
        }

        return new ArrayList<>();
    }

    /**
     * Gets all the upgrades (standard, professional and enterprise) that need to be run from the build number stored in
     * the database to the current build number
     * <p/>
     * Get the set of upgradeNumbers which are to be performed for this upgrade.
     * <p/>
     * Get the Maps of relevant upgrades for the standard, professional and enterprise using
     * {@link #getRelevantUpgradesFromList}
     * <p/>
     * Iterate over these numbers and if either of the standard, professional or enterprise maps contains an
     * {@link IUpgradeTask} with this number then do the upgrade
     * <p/>
     * If errors are found, it will cancel the upgrade, and list errors to the console.
     * <p/>
     * For each upgrade that happens successfully, it will increment the build number in the database, so that if one
     * fails, you do not have to repeat all the upgrades that have already run.
     * <p/>
     * If there are no errors from the upgrade, the build number in the database is incremented to the current build
     * number. This is because there may be no upgrades for a particular version & needUpgrade() checks build no in
     * database.
     */
    private Collection<String> doUpgrade() {
        LOGGER.info("___ Performing Upgrade ____________________");

        Collection<String> errors = new ArrayList<>();
        try {
            final Set<String> upgradeNumbers = getAllRelevantUpgradeBuildNumbers();

            // get all the relevant upgrades for both professional and enterprise
            final Map<String, IUpgradeTask> standardUpgrades = getRelevantUpgradesFromList(allUpgrades);

            errors = runUpgradeTasks(upgradeNumbers, standardUpgrades);

            // if there were no errors then set the build number to the current number
            if (errors.isEmpty()) {
                logUpgradeSuccessfulMsg();
                // there may not be any patches for this version, so increment to latest build
                // number.
                setBuildNumber(buildUtilsInfo.getCurrentBuildNumber());
            } else {
                LOGGER.error("Errors occurred during upgrade:");
                printErrors(errors);
            }
        } catch (final Throwable e) {
            LOGGER.error("Exception thrown during upgrade: " + e.getMessage(), e);
            errors.add("Exception thrown during upgrade: " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        }
        // ManagerFactory.getCacheManager()
        // .flushAll();
        return errors;
    }

    /**
     * Print errors to log4j at error level.
     *
     * @param errors
     *            A collection of strings, describing all the errors that occurred.
     */
    private void printErrors(final Collection<String> errors) {
        for (final String s : errors) {
            LOGGER.error("Upgrade Error: " + s);
        }
    }

    /**
     * Runs the given upgrade tasks for the given build numbers.
     * <p/>
     * The method iterates over the given build numbers and for each runs an upgrade tasks according to the currently
     * installed license.
     * <p/>
     * After all upgrade tasks are run for a build number build number is set to its value.
     *
     * @param upgradeNumbers
     *            the build numbers for which upgrade tasks need to be run
     * @param upgradeTasks
     *            upgrades to run with build number as key
     * @return a collection of errors that occurred during upgrade (empty collection if no errors occurred)
     */
    private Collection<String> runUpgradeTasks(final Collection<String> upgradeNumbers,
        final Map<String, IUpgradeTask> upgradeTasks) throws Exception {
        final Collection<String> errors = Lists.newArrayList();

        // get a list of any previously run upgrades so that they are not run again
        final Map<String, UpgradeHistory> upgradeHistoryMap = getPreviouslyRunUpgrades();
        boolean noErrors = true;

        for (final String number : upgradeNumbers) {
            // if there is a standard upgrade for this build then perform it
            final IUpgradeTask standardUpgradeTask = upgradeTasks.get(number);

            if (!doUpgradeTaskSucess(upgradeHistoryMap, standardUpgradeTask, errors)) {
                noErrors = false;
                break;
            }

            // if the number of the upgrade is greater than the current build number then set the
            // build number to this number
            if (BUILD_NUMBER_COMPARATOR.compare(number, getBuildNumber()) > 0) {
                LOGGER.info("Setting current build number on to " + number);
                setBuildNumber(number);
            }
        }

        // create a record of the version we are upgrading to
        if (noErrors) {
            createUpgradeVersionHistory();
        }

        return errors;
    }

    /**
     * {@inheritDoc} Performs any upgrades that may be needed as a result of the Setup procedure
     * <p/>
     * Get the set of setupUpgradeNumbers which are to be performed for this setup.
     * <p/>
     * Iterate over these numbers and if either of the standard, professional or enterprise upgrade maps contains an
     * {@link IUpgradeTask} with this number then do the upgrade
     * <p/>
     * If errors are found, it will cancel the upgrade, and return the list of errors.
     * <p/>
     * For each upgrade that happens successfully, it will increment the build number in the database, so that if one
     * fails, you do not have to repeat all the upgrades that have already run.
     * <p/>
     * If there are no errors from the upgrade, the build number in the database is incremented to the current build
     * number. This is because there may be no upgrades for a particular version & needUpgrade() checks build no in
     * database.
     */
    @Override
    public Collection<String> doSetupUpgrade() {
        Collection<String> errors = new ArrayList<>();

        try {
            final Set<String> upgradeNumbers = getSetupUpgradeBuildNumbers();
            LOGGER.info("___ Performing Setup Upgrade ____________________");

            errors = runUpgradeTasks(upgradeNumbers, setupUpgrades);

            // if there were no errors then set the build number to the current number
            if (errors.isEmpty()) {
                logUpgradeSuccessfulMsg();
                setBuildNumber(buildUtilsInfo.getCurrentBuildNumber());
            } else {
                LOGGER.error("Errors occurred during upgrade:");
                printErrors(errors);
            }
        } catch (final Throwable e) {
            LOGGER.error("Exception thrown during upgrade: " + e.getMessage(), e);
            errors.add("Exception thrown during upgrade: " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        }

        return errors;
    }

    private void logUpgradeSuccessfulMsg() {
        final String msg = "\n\n" + "***************************************************************\n"
                + "Upgrade Succeeded! Application has been upgraded to build number "
                + buildUtilsInfo.getCurrentBuildNumber() + "\n"
                + "***************************************************************\n";
        LOGGER.info(msg);
    }

    /**
     * Performs an upgrade by executing an Upgrade Task.
     *
     * @return True if the upgrade was performed without errors. False if the upgrade has errors
     */
    private boolean doUpgradeTaskSucess(final Map<String, UpgradeHistory> upgradeHistoryMap,
        final IUpgradeTask upgradeTask,
        final Collection<String> errors) throws Exception {
        if (upgradeTask != null) {
            // if the upgrade has not been run then
            if (upgradeHistoryMap.get(upgradeTask.getClass().getName()) == null) {
                LOGGER.info("Performing Upgrade Task: " + upgradeTask.getShortDescription());
                upgradeTask.doUpgrade();

                try {
                    upgradeStore.addToUpgradeHistory(upgradeTask, buildUtilsInfo);
                } catch (final DataAccessException e) {
                    LOGGER.error(
                        "Problem adding upgrade task " + upgradeTask.getShortDescription() + " to the upgrade history",
                        e);
                    errors.add("There was a problem adding Upgrade Task " + upgradeTask.getShortDescription()
                            + " to the Upgrade History");
                }

                if (!upgradeTask.getErrors().isEmpty()) {
                    LOGGER.error("Errors during Upgrade Task: " + upgradeTask.getShortDescription());
                    errors.addAll(upgradeTask.getErrors());
                    return false;
                }

                LOGGER.info("Upgrade Task: '" + upgradeTask.getShortDescription() + "' succeeded");
            } else {
                LOGGER.info("Not performing Upgrade Task: '" + upgradeTask.getShortDescription()
                        + "' as it has already been run.");
            }
        }
        return true;
    }

    /**
     * Get a list of the Upgrades that have been previously run.
     */
    private Map<String, UpgradeHistory> getPreviouslyRunUpgrades() {
        // if this list of upgrades has not been retrieved then retrieve it otherwise return it
        if (upgradeHistoryMap == null) {
            // if is dev mode
            final List<UpgradeHistory> upgradeHistoryList = this.needSetup() ? Collections.<UpgradeHistory> emptyList()
                    : upgradeStore.findUpgradeHistory();

            upgradeHistoryMap = Maps.newHashMap();

            for (final UpgradeHistory upgradeHist : upgradeHistoryList) {
                upgradeHistoryMap.put(upgradeHist.getUpgradeClass(), upgradeHist);
            }
        }
        return upgradeHistoryMap;
    }

    /**
     * For each upgrade in the upgradeMap, test whether it is needed (ie upgrade version is greater than the version in
     * the database), and then add to set.
     *
     * @return set of UpgradeTasks that need to be run.
     */
    SortedMap<String, IUpgradeTask> getRelevantUpgradesFromList(final Map<String, IUpgradeTask> upgradeMap) {
        try {
            final SortedMap<String, IUpgradeTask> unAppliedUpgrades = Maps.newTreeMap(BUILD_NUMBER_COMPARATOR);

            for (final Map.Entry<String, IUpgradeTask> entry : upgradeMap.entrySet()) {
                if (needUpgrade(entry.getKey())) {
                    final IUpgradeTask upgradeTask = entry.getValue();

                    unAppliedUpgrades.put(upgradeTask.getBuildNumber(), upgradeTask);
                }
            }
            return unAppliedUpgrades;
        } catch (final Throwable e) {
            LOGGER.error("Exception getting upgrades " + e.getMessage(), e);
            return Maps.newTreeMap();
        }
    }

    /**
     * {@inheritDoc} Get the current build number from the database. This represents the level that this application is
     * patched to. This may be different to the current version if there are patches waiting to be applied.
     */
    @Override
    public String getBuildNumber() {
        // if null in the database, we need to set to '0' so that it can be compared with
        // other versions.
        return applicationProperties.getString(PropertyKeys.PATCHED_VERSION_PROPERTY).orElseGet(() -> {
            setBuildNumber("0");
            return "0";
        });
    }

    private void setBuildNumber(final String version) {
        applicationProperties.setString(PropertyKeys.PATCHED_VERSION_PROPERTY, version);
    }

    /**
     * If the patch version is greater than the current version, then return true. Else return false.
     */
    private boolean needUpgrade(final String buildNumber) {
        return patchBuildGreaterThanCurrent(getBuildNumber(), buildNumber);
    }

    /**
     * If the patch version is greater than current version, return true. Else return false
     */
    private boolean patchBuildGreaterThanCurrent(final String currentBuild, final String patchBuild) {
        return BUILD_NUMBER_COMPARATOR.compare(currentBuild, patchBuild) < 0;
    }

    private void createUpgradeVersionHistory() {
        upgradeStore.createUpgradeVersionHistory(buildUtilsInfo);
    }

    SortedMap<String, IUpgradeTask> getAllUpgrades() {
        return allUpgrades;
    }

    /** {@inheritDoc} */
    @Override
    public List<IUpgradeHistoryItem> getUpgradeHistory() {
        final List<IUpgradeHistoryItem> upgradeHistoryItems = new ArrayList<>();

        final IUpgradeHistoryItem itemFromTasks = getUpgradeHistoryItemFromTasks();
        String previousVersion = null;
        String previousBuildNumber = null;
        if (itemFromTasks != null) {
            upgradeHistoryItems.add(itemFromTasks);
            previousVersion = itemFromTasks.getTargetVersion();
            previousBuildNumber = itemFromTasks.getTargetBuildNumber();
        }

        // select the upgrade history items that have a target build
        final List<UpgradeHistoryVersion> upgradeTasksWithBuild = upgradeStore.findUpgradeHistoryVersion();

        for (final UpgradeHistoryVersion upgradeHistoryVersion : upgradeTasksWithBuild) {
            final Date timePerformed = upgradeHistoryVersion.getTimePerformed();
            final String targetBuildNumber = upgradeHistoryVersion.getTargetBuildNumber();
            final String targetVersion = upgradeHistoryVersion.getTargetVersion();
            upgradeHistoryItems.add(new UpgradeHistoryItemImpl(timePerformed, targetBuildNumber, targetVersion,
                    previousBuildNumber, previousVersion));
            previousVersion = targetVersion;
            previousBuildNumber = targetBuildNumber;
        }

        // reverse the list since we have been adding in chronological order
        Collections.reverse(upgradeHistoryItems);
        return Collections.unmodifiableList(upgradeHistoryItems);

    }

    /**
     * Selects the most recent upgrade task that doesn't have a target build, and then extracts the build number from
     * the task class name so that we can infer a version which was upgraded to.
     *
     * @return an {@link IUpgradeHistoryItem} representing the inferred upgrade; null if all upgrade tasks have an
     *         associated target build number
     * @throws GenericEntityException
     *             when search for upgrade tasks throws exception
     */
    @Nullable
    @CheckReturnValue
    @VisibleForTesting
    /* private */IUpgradeHistoryItem getUpgradeHistoryItemFromTasks() {
        final List<UpgradeHistory> upgradeTasksWithoutBuild = upgradeStore.getUpgradeHistoryItemFromTasks();

        if (!upgradeTasksWithoutBuild.isEmpty()) {
            // take the first one - there should only be one
            final UpgradeHistory item = upgradeTasksWithoutBuild.get(0);
            final String upgradeClassName = item.getUpgradeClass();
            final BuildVersion targetVersion = getVersionForBuildNumber(upgradeClassName);
            if (StringUtils.isNotEmpty(targetVersion.getVersion())
                    && StringUtils.isNotEmpty(targetVersion.getBuildNumber())) {
                return new UpgradeHistoryItemImpl(null, targetVersion.getBuildNumber(), targetVersion.getVersion(),
                        targetVersion.getBuildNumber(), null, true);
            }
        }
        return null;
    }

    /**
     * <p>
     * getVersionForBuildNumber.
     * </p>
     *
     * @param upgradeClassName
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.core.versioning.BuildVersion} object.
     */
    protected BuildVersion getVersionForBuildNumber(final String upgradeClassName) {
        return new BuildVersionImpl(BuildUtils.extractBuildNumberFromUpgradeClass(upgradeClassName),
                BuildUtils.extractTargetVersionFromUpgradeClass(upgradeClassName));
    }

}
