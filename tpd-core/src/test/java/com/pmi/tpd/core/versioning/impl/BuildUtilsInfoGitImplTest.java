package com.pmi.tpd.core.versioning.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.util.ClassLoaderUtils;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.testing.junit5.TestCase;

public class BuildUtilsInfoGitImplTest extends TestCase {

    DateFormat dateFormat = DateFormat.getInstance();

    @Test
    public void nullConstructor() {
        assertThrows(IllegalStateException.class, () -> new BuildUtilsInfoGitImpl(0, null));
    }

    @Test
    public void emptyConstructor() {
        assertThrows(IllegalStateException.class, () -> new BuildUtilsInfoGitImpl(0, StringUtils.EMPTY));
    }

    @Test
    public void simpleVersionNumber() {
        final Properties properties = new Properties();
        properties.put(IBuildUtilsInfo.KEY_APP_VERSION, "0.0.1-37-g35f4133-master");
        final BuildUtilsInfoGitImpl buildUtil = new BuildUtilsInfoGitImpl(0, "0", properties);
        assertEquals("137", buildUtil.getCurrentBuildNumber());
        assertEquals("0.0.1", buildUtil.getVersion());

    }

    @Test
    public void normalVersionNumber() {
        final Properties properties = new Properties();
        properties.put(IBuildUtilsInfo.KEY_APP_VERSION, "1.0.0-DEV1-80-geb09b31-develop");
        final BuildUtilsInfoGitImpl buildUtil = new BuildUtilsInfoGitImpl(0, "0", properties);
        assertEquals("1000080", buildUtil.getCurrentBuildNumber());
        assertEquals("1.0.0", buildUtil.getVersion());

    }

    @Test
    public void gitCommitIdVersionNumber() {
        final Properties properties = new Properties();
        properties.put(IBuildUtilsInfo.KEY_APP_VERSION, "2.5.0-SNAPSHOT-183-fd69e20-develop");
        final BuildUtilsInfoGitImpl buildUtil = new BuildUtilsInfoGitImpl(0, "0", properties);
        assertEquals("2050183", buildUtil.getCurrentBuildNumber());
        assertEquals("2.5.0", buildUtil.getVersion());

    }

    @Test
    public void wrongVersionNumber() {
        final Properties properties = new Properties();
        properties.put(IBuildUtilsInfo.KEY_APP_VERSION, "-37-g35f4133-master");
        final BuildUtilsInfoGitImpl buildUtil = new BuildUtilsInfoGitImpl(0, "0", properties);
        assertEquals("", buildUtil.getCurrentBuildNumber());
        assertEquals("", buildUtil.getVersion());

    }

    @Test
    public void fromPropertiesFile() throws ParseException {
        final BuildUtilsInfoGitImpl buildUtil = new BuildUtilsInfoGitImpl(0, "10",
                loadProperties(getPackagePath() + "/git-version.properties"));
        assertEquals("137", buildUtil.getCurrentBuildNumber());
        assertEquals("0.0.1", buildUtil.getVersion());
        assertEquals("0.0.1#137-sha1:35f4133cbce1134ed891ddb13eb323433571c82f", buildUtil.getBuildInformation());
        assertEquals("35f4133cbce1134ed891ddb13eb323433571c82f", buildUtil.getCommitId());
        assertEquals("0.0.1-master.137", buildUtil.getCurrentLongVersion());
        assertEquals(0L, buildUtil.getMinimumUpgradableBuildNumber());
        assertEquals("10", buildUtil.getMinimumUpgradableVersion());
        assertArrayEquals(new int[] { 0, 0, 1 }, buildUtil.getVersionNumbers());
    }

    @Test
    public void fromPropertiesFileWithWorkflow() throws ParseException {
        final BuildUtilsInfoGitImpl buildUtil = new BuildUtilsInfoGitImpl(0, "10",
                loadProperties(getPackagePath() + "/git-version-with-workflow.properties"));
        assertEquals("137", buildUtil.getCurrentBuildNumber());
        assertEquals("0.0.1", buildUtil.getVersion());
        assertEquals("0.0.1#137-sha1:35f4133cbce1134ed891ddb13eb323433571c82f", buildUtil.getBuildInformation());
        assertEquals("35f4133cbce1134ed891ddb13eb323433571c82f", buildUtil.getCommitId());
        assertEquals("0.0.1-beta.37", buildUtil.getCurrentLongVersion());
        assertEquals(0L, buildUtil.getMinimumUpgradableBuildNumber());
        assertEquals("10", buildUtil.getMinimumUpgradableVersion());
        assertArrayEquals(new int[] { 0, 0, 1 }, buildUtil.getVersionNumbers());
    }

    private static Properties loadProperties(final String file) throws RuntimeException {
        final InputStream propsFile = ClassLoaderUtils.getResourceAsStream(file, BuildUtilsInfoGitImpl.class);
        if (propsFile == null) {
            throw new IllegalStateException("File not found: " + file);
        }

        final Properties result = new Properties();
        try {
            result.load(propsFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                propsFile.close();
            } catch (final IOException e) {
            }
        }

        return result;
    }

}
