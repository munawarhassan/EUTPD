package com.pmi.tpd.core.versioning;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.core.upgrade.UpgradeVersion;
import com.pmi.tpd.testing.junit5.TestCase;

public class BuildUtilsTest extends TestCase {

    @Test
    public void createBuildVersionWithoutBuildNumber() {
        assertThrows(IllegalArgumentException.class, () -> BuildUtils.createBuildVersion("1.2.2"));
    }

    @Test
    public void createBuildVersion() {
        {
            final BuildVersion buildVersion = BuildUtils.createBuildVersion("1.2.2.255");
            assertEquals("255", buildVersion.getBuildNumber());
            assertEquals("1.2.2", buildVersion.getVersion());
        }
        {
            final BuildVersion buildVersion = BuildUtils.createBuildVersion("1.2.2-SNAPSHOT.4568");
            assertEquals("4568", buildVersion.getBuildNumber());
            assertEquals("1.2.2", buildVersion.getVersion());
        }
        {
            final BuildVersion buildVersion = BuildUtils.createBuildVersion("1.2.0.4568");
            assertEquals("4568", buildVersion.getBuildNumber());
            assertEquals("1.2.0", buildVersion.getVersion());
        }

    }

    @Test
    public void create() {
        {
            final String version = BuildUtils.toString(BuildUtils.createBuildVersion("1.2.2-SNAPSHOT.4568"));
            assertEquals("1.2.2.4568", version);
        }
        {
            final String version = BuildUtils.toString(BuildUtils.createBuildVersion("1.2.0.4568"));
            assertEquals("1.2.0.4568", version);
        }

    }

    @Test
    public void getDoubleBuildNumber() {
        assertEquals(Double.valueOf(1.000200004568), BuildUtils.getDoubleBuildNumber("1.2.0.4568"));
    }

    @Test
    public void toLong() {
        assertEquals(10202L, (long) BuildUtils.toLong("1.2.2"));
        assertEquals(12420L, (long) BuildUtils.toLong("1.24.20"));
        assertEquals(1242025L, (long) BuildUtils.toLong("1.24.20.25"));
        assertEquals(1242025L, (long) BuildUtils.toLong("1.24.20-develop.25"));
        assertEquals(1242025L, (long) BuildUtils.toLong("1.24.20-snapshot-develop.25"));
    }

    @Test
    public void extractBuildNumberFromUpgradeClass() {
        final String buildNumber = BuildUtils.extractBuildNumberFromUpgradeClass(UpgradClassTest.class.getName());
        assertEquals("20", buildNumber);
    }

    @Test
    public void extractBuildNumberFromNoUpgradeClass() {
        final String buildNumber = BuildUtils
                .extractBuildNumberFromUpgradeClass(UpgradClassWithoutAnnotation.class.getName());
        assertEquals("", buildNumber);
    }

    @Test
    public void extractBuildNumberFromWrongUpgradeClass() {
        assertThrows(RuntimeException.class, () -> BuildUtils.extractBuildNumberFromUpgradeClass("com.foo.Foo"));

    }

    @Test
    public void extractTargetVersionFromUpgradeClass() {
        final String version = BuildUtils.extractTargetVersionFromUpgradeClass(UpgradClassTest.class.getName());
        assertEquals("1.0.5", version);
    }

    @Test
    public void extractTargetVersionFromNoUpgradeClass() {
        final String version = BuildUtils
                .extractTargetVersionFromUpgradeClass(UpgradClassWithoutAnnotation.class.getName());
        assertEquals("", version);
    }

    @UpgradeVersion(buildNumber = "20", targetVersion = "1.0.5")
    public static class UpgradClassTest {

    }

    public static class UpgradClassWithoutAnnotation {

    }

}
