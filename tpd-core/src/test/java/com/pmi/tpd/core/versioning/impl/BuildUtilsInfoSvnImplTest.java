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

public class BuildUtilsInfoSvnImplTest extends TestCase {

    DateFormat dateFormat = DateFormat.getInstance();

    @Test
    public void nullConstructor() {
        assertThrows(IllegalStateException.class, () -> new BuildUtilsInfoSvnImpl(0, null));

    }

    @Test
    public void emptyConstructor() {
        assertThrows(IllegalStateException.class, () -> new BuildUtilsInfoSvnImpl(0, StringUtils.EMPTY));

    }

    @Test
    public void simpleVersionNumber() {
        final Properties properties = new Properties();
        properties.put(IBuildUtilsInfo.KEY_APP_VERSION, "0.0.1.37");
        final BuildUtilsInfoSvnImpl buildUtil = new BuildUtilsInfoSvnImpl(0, "0", properties);
        assertEquals("37", buildUtil.getCurrentBuildNumber());
        assertEquals("0.0.1", buildUtil.getVersion());

    }

    @Test
    public void wrongVersionNumber() {
        final Properties properties = new Properties();
        properties.put(IBuildUtilsInfo.KEY_APP_VERSION, "-37");
        final BuildUtilsInfoSvnImpl buildUtil = new BuildUtilsInfoSvnImpl(0, "0", properties);
        assertEquals("", buildUtil.getCurrentBuildNumber());
        assertEquals("0.0.0", buildUtil.getVersion());
    }

    @Test
    public void fromPropertiesFile() throws ParseException {
        final BuildUtilsInfoSvnImpl buildUtil = new BuildUtilsInfoSvnImpl(0, "10",
                loadProperties(getPackagePath() + "/svn-version.properties"));
        assertEquals("37", buildUtil.getCurrentBuildNumber());
        assertEquals("1.0.0", buildUtil.getVersion());
        assertEquals("1.0.0#37-sha1:37", buildUtil.getBuildInformation());
        assertEquals("37", buildUtil.getCommitId());
        assertEquals("1.0.0-SNAPSHOT.37", buildUtil.getCurrentLongVersion());
        assertEquals(0L, buildUtil.getMinimumUpgradableBuildNumber());
        assertEquals("10", buildUtil.getMinimumUpgradableVersion());
        assertArrayEquals(new int[] { 1, 0, 0 }, buildUtil.getVersionNumbers());
    }

    private static Properties loadProperties(final String file) throws RuntimeException {
        final InputStream propsFile = ClassLoaderUtils.getResourceAsStream(file, BuildUtilsInfoSvnImpl.class);
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
