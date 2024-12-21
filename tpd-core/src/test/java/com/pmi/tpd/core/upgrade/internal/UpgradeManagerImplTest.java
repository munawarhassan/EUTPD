package com.pmi.tpd.core.upgrade.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants.PropertyKeys;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.model.upgrade.UpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion;
import com.pmi.tpd.core.upgrade.IUpgradeHistoryItem;
import com.pmi.tpd.core.upgrade.IUpgradeManager;
import com.pmi.tpd.core.upgrade.IUpgradeTask;
import com.pmi.tpd.core.upgrade.IUpgradeTask.SetupType;
import com.pmi.tpd.core.upgrade.MockUpgradeTask;
import com.pmi.tpd.core.versioning.BuildVersion;
import com.pmi.tpd.core.versioning.impl.BuildVersionImpl;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class UpgradeManagerImplTest extends MockitoTestCase {

    private static final Collection<MockUpgradeTask> ALL_UPGRADES = new ArrayList<>();

    static {
        // creating anonymous classes to have different class names for storing into upgrade
        // history.
        ALL_UPGRADES.add(new MockUpgradeTask("1.0.0", "15", SetupType.setupAndBuild, "short desc") {});
        ALL_UPGRADES.add(new MockUpgradeTask("1.0.1", "29", SetupType.onlySetup, "short desc2") {});
        ALL_UPGRADES.add(new MockUpgradeTask("1.2.0", "65", SetupType.onlyBuild, "short desc3") {});
        ALL_UPGRADES.add(new MockUpgradeTask("1.2.3", "70", SetupType.setupAndBuild, "short desc4") {});
        ALL_UPGRADES.add(new MockUpgradeTask("1.3.0", "80", SetupType.setupAndBuild, "short desc5") {});
        ALL_UPGRADES.add(new MockUpgradeTask("1.3.5", "90", SetupType.setupAndBuild, "short desc6") {});
        ALL_UPGRADES.add(new MockUpgradeTask("0.9.0", "13", SetupType.setupAndBuild, "short desc7") {});
        ALL_UPGRADES.add(new MockUpgradeTask("1.1.0", "30", SetupType.setupAndBuild, "short desc8") {});
    }

    private IBuildUtilsInfo buildUtilsInfo;

    private IUpgradeStore upgradeStore;

    private IApplicationProperties applicationProperties;

    private IApplicationConfiguration applicationConfiguration;

    private static final String VERSION = "4.0.0";

    @BeforeEach
    public void setUp() throws Exception {
        upgradeStore = mock(IUpgradeStore.class);
        buildUtilsInfo = mock(IBuildUtilsInfo.class, withSettings().lenient());
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("99999");

        applicationProperties = mock(IApplicationProperties.class);

        applicationConfiguration = mock(IApplicationConfiguration.class);
    }

    @Test
    public void doUpdate() {
        final IUpgradeManager man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);
        man.doUpgradeIfNeededAndAllowed();
    }

    @Test
    public void doUpgradeIfNeededAndAllowedOldForBuild() {
        when(applicationProperties.isSetup()).thenReturn(true);
        when(applicationProperties.getString(eq(PropertyKeys.PATCHED_VERSION_PROPERTY))).thenReturn(Optional.of("2"));
        // enforce failed
        when(upgradeStore.findUpgradeHistory()).thenReturn(null);

        final IUpgradeManager man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);
        final Collection<String> actualResult = man.doUpgradeIfNeededAndAllowed();

        // we should have one error
        assertEquals(1, actualResult.size());
    }

    @Test
    public void upgradesInOrder() {
        when(applicationProperties.getString(eq(PropertyKeys.PATCHED_VERSION_PROPERTY))).thenReturn(Optional.of("2"));
        // when(buildUtilsInfo.getVersion()).thenReturn(VERSION);

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);
        final SortedMap<String, IUpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<IUpgradeTask> iterator = upgrades.values().iterator();

        IUpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc7");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc2");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc8");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void upgradesSubset0_10() {
        when(applicationProperties.getString(eq(PropertyKeys.PATCHED_VERSION_PROPERTY))).thenReturn(Optional.of("14"));

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);
        final SortedMap<String, IUpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<IUpgradeTask> iterator = upgrades.values().iterator();

        IUpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc2");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc8");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void upgradesSubset1_1_2() {
        when(applicationProperties.getString(eq(PropertyKeys.PATCHED_VERSION_PROPERTY))).thenReturn(Optional.of("40"));

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);

        final SortedMap<String, IUpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<IUpgradeTask> iterator = upgrades.values().iterator();

        IUpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset1_3() {
        when(applicationProperties.getString(eq(PropertyKeys.PATCHED_VERSION_PROPERTY))).thenReturn(Optional.of("78"));

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);
        final SortedMap<String, IUpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<IUpgradeTask> iterator = upgrades.values().iterator();

        IUpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");
        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset27() {
        when(applicationProperties.getString(eq(PropertyKeys.PATCHED_VERSION_PROPERTY))).thenReturn(Optional.of("90"));

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);
        final SortedMap<String, IUpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        assertEquals(upgrades.size(), 0);
    }

    @Test
    public void getUpgradeHistoryItemFromTasksNone() throws Exception {
        when(upgradeStore.getUpgradeHistoryItemFromTasks()).thenReturn(Lists.<UpgradeHistory> newArrayList());

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);

        final IUpgradeHistoryItem result = man.getUpgradeHistoryItemFromTasks();
        assertNull(result);
    }

    @Test
    public void getUpgradeHistoryItemFromTasksHappyPath() throws Exception {
        when(upgradeStore.getUpgradeHistoryItemFromTasks()).thenReturn(Collections.<UpgradeHistory> singletonList(
            new UpgradeHistory.Builder().upgradeClass("UpgradeTask_Build106").build()));

        final BuildVersionImpl buildVersion = new BuildVersionImpl("106", "XYZ");

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES) {

            @Override
            protected BuildVersion getVersionForBuildNumber(final String upgradeClassName) {
                return buildVersion;
            }
        };
        final IUpgradeHistoryItem result = man.getUpgradeHistoryItemFromTasks();
        final IUpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "106", "XYZ", "106", null, true);
        assertEquals(result, expected);
    }

    @Test
    public void getUpgradeHistoryNoPrevious() throws Exception {
        when(upgradeStore.getUpgradeHistoryItemFromTasks()).thenReturn(Lists.<UpgradeHistory> newArrayList());
        final UpgradeHistoryVersion item = UpgradeHistoryVersion.builder()
                .targetBuildNumber("400")
                .targetVersion(VERSION)
                .build();
        when(upgradeStore.findUpgradeHistoryVersion()).thenReturn(Arrays.asList(item));

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES);

        final List<IUpgradeHistoryItem> result = man.getUpgradeHistory();
        final IUpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "400", VERSION, null, null);
        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));
    }

    @Test
    public void getUpgradeHistoryPrevious() throws Exception {
        final UpgradeHistoryVersion item = UpgradeHistoryVersion.builder()
                .targetBuildNumber("400")
                .targetVersion(VERSION)
                .build();
        when(upgradeStore.findUpgradeHistoryVersion()).thenReturn(Arrays.asList(item));

        final IUpgradeHistoryItem expected1 = new UpgradeHistoryItemImpl(null, "400", VERSION, "300", "3.0");
        final IUpgradeHistoryItem expected2 = new UpgradeHistoryItemImpl(null, "300", "3.0", null, null);

        final UpgradeManagerImpl man = new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties,
                applicationConfiguration, ALL_UPGRADES) {

            @Override
            IUpgradeHistoryItem getUpgradeHistoryItemFromTasks() {
                return expected2;
            }
        };

        final List<IUpgradeHistoryItem> result = man.getUpgradeHistory();
        assertEquals(2, result.size());
        assertEquals(expected1, result.get(0));
        assertEquals(expected2, result.get(1));

    }

}
