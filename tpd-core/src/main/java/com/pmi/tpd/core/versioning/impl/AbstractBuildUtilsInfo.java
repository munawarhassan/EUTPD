package com.pmi.tpd.core.versioning.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.versioning.BuildUtils;

/**
 * This class gives access to build-time properties at runtime.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class AbstractBuildUtilsInfo implements IBuildUtilsInfo {

    /** */
    public static final Pattern PARSE_VERSION = Pattern.compile("([0-9]+)(\\.?)(.*)");

    /** */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy '@' HH:mm:ss z");

    /**
     * Logger for this class.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractBuildUtilsInfo.class);

    /**
     * The build properties.
     */
    private Properties buildProperties;

    /** */
    private final long minimumUpgradableBuildNumber;

    /** */
    private final String minimumUpgradableVersion;

    /** */
    private final String version;

    /** */
    private final String longVersion;

    /** */
    private final String currentBuildNumber;

    /** */
    private Date currentBuildDate;

    /**
     * Creates a new BuildUtilsInfoImpl, loading the properties from the '
     * {@value com.pmi.tpd.api.versioning.IBuildUtilsInfo#BUILD_VERSIONS_PROPERTIES}' file. file.
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     */
    public AbstractBuildUtilsInfo(final long minimumUpgradableBuildNumber,
            @Nonnull final String minimumUpgradableVersion) {
        this(minimumUpgradableBuildNumber, minimumUpgradableVersion, loadProperties());
    }

    /**
     * <p>
     * Constructor for AbstractBuildUtilsInfo.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     * @param properties
     *            a {@link java.util.Properties} object.
     */
    public AbstractBuildUtilsInfo(final long minimumUpgradableBuildNumber,
            @Nonnull final String minimumUpgradableVersion, final Properties properties) {
        Assert.hasText(minimumUpgradableVersion);
        buildProperties = properties;
        final String originalVersion = getProperty(KEY_APP_VERSION);
        final String semanticVersion = getProperty(REVISION_VERSION);
        if (StringUtils.isEmpty(semanticVersion) || semanticVersion.startsWith("${")) {
            longVersion = transformVersion(originalVersion);
            currentBuildNumber = extractBuildNumber(longVersion);
            version = cleanVersion(longVersion);
        } else {
            longVersion = semanticVersion;
            final String bn = extractBuildNumber(longVersion);
            version = cleanVersion(longVersion);
            currentBuildNumber = BuildUtils.toLong(version + "." + bn).toString();
        }

        this.minimumUpgradableBuildNumber = minimumUpgradableBuildNumber;
        this.minimumUpgradableVersion = minimumUpgradableVersion;
        try {
            final String buildTime = getProperty(KEY_BUILD_TIME);
            if (buildTime != null) {
                currentBuildDate = DATE_FORMAT.parse(buildTime);
            }
        } catch (final ParseException e) {
            LOGGER.warn("Impossible retrieve current build date : parsing error");
        }
    }

    /**
     * <p>
     * Constructor for AbstractBuildUtilsInfo.
     * </p>
     *
     * @param version
     *            a {@link java.lang.String} object.
     * @param buildTime
     *            a {@link java.util.Date} object.
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     */
    public AbstractBuildUtilsInfo(@Nonnull final String version, final Date buildTime,
            final long minimumUpgradableBuildNumber, @Nonnull final String minimumUpgradableVersion) {
        Assert.hasText(minimumUpgradableVersion);
        Assert.hasText(version);
        longVersion = version;
        currentBuildNumber = extractBuildNumber(longVersion);
        this.version = cleanVersion(longVersion);
        this.minimumUpgradableBuildNumber = minimumUpgradableBuildNumber;
        this.minimumUpgradableVersion = minimumUpgradableVersion;
        currentBuildDate = buildTime;
    }

    /** {@inheritDoc} */
    @Override
    public final String getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    @Override
    public final String getCurrentLongVersion() {
        return longVersion;
    }

    /**
     * <p>
     * getVersionNumbers.
     * </p>
     *
     * @return an array of int.
     */
    public final int[] getVersionNumbers() {
        return parseVersion(getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public final String getCurrentBuildNumber() {
        return currentBuildNumber;
    }

    /** {@inheritDoc} */
    @Override
    public final long getMinimumUpgradableBuildNumber() {
        return minimumUpgradableBuildNumber;
    }

    /** {@inheritDoc} */
    @Override
    public final Date getCurrentBuildDate() {
        return currentBuildDate;
    }

    /** {@inheritDoc} */
    @Override
    public String getBuildInformation() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getVersion());
        sb.append("#");
        sb.append(getCurrentBuildNumber());

        // the revision may be blank when building the source release where the source wasn't checked out
        // from SCM!
        if (StringUtils.isNotBlank(getCommitId())) {
            sb.append("-sha1:").append(getCommitId());
        }
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String getCommitId() {
        return getProperty(KEY_COMMIT_ID);
    }

    /** {@inheritDoc} */
    @Override
    public String getMinimumUpgradableVersion() {
        return minimumUpgradableVersion;
    }

    /** {@inheritDoc} */
    @Override
    public Properties getBuildProperties() {
        return this.buildProperties;
    }

    private String getProperty(final String key) {
        return buildProperties.getProperty(key);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getBuildInformation();
    }

    /**
     * Loads the properties from the '{@value #BUILD_VERSIONS_PROPERTIES}' file.
     *
     * @return a new Properties instance
     * @throws RuntimeException
     *             if there's a problem reading the file
     */
    private static Properties loadProperties() throws RuntimeException {
        final InputStream propsFile = BuildUtilsInfoSvnImpl.class
                .getResourceAsStream("/" + IBuildUtilsInfo.BUILD_VERSIONS_PROPERTIES);
        if (propsFile == null) {
            throw new IllegalStateException("File not found: " + IBuildUtilsInfo.BUILD_VERSIONS_PROPERTIES);
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
                LOGGER.warn(e.getMessage(), e);
            }
        }

        return result;
    }

    /** package-scope for for testing. */
    static int[] parseVersion(final String version) {
        final List<Integer> ints = new LinkedList<>();
        final Matcher m = PARSE_VERSION.matcher("");
        String rest = version;
        while (m.reset(rest).matches()) {
            final String i = m.group(1);
            ints.add(Integer.parseInt(i));

            final String dot = m.group(2);
            rest = m.group(3);
            if (!".".equals(dot)) {
                break; // no more numbers to be had
            }
        }

        while (ints.size() < 3) { // make sure there is always at least 3
            ints.add(0);
        }
        return Ints.toArray(ints);
    }

    private static String cleanVersion(String version) {
        if (StringUtils.isEmpty(version)) {
            return version;
        }
        final int index = version.indexOf('-');
        if (index >= 0) {
            version = version.substring(0, index);
        }
        return Ints.join(".", Arrays.copyOf(parseVersion(version), 3));
    }

    /**
     * Convert git version conform to {@link com.pmi.tpd.core.versioning.BuildUtils}, i.e. 1.0.1,1.0.0-SNAPSHOT.4562
     *
     * @param version
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String transformVersion(final String version) {
        return version;
    }

    /**
     * <p>
     * extractBuildNumber.
     * </p>
     *
     * @param longVersion
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String extractBuildNumber(final String longVersion) {
        if (StringUtils.isEmpty(longVersion)) {
            return StringUtils.EMPTY;
        }
        final int last = longVersion.lastIndexOf('.');
        if (last >= 0) {
            return longVersion.substring(last + 1);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.versioning.impl.AbstractBuildUtilsInfo.BuildInfoBuilder} object.
     */
    public static BuildInfoBuilder builder() {
        return new BuildInfoBuilder();
    }

    public static class BuildInfoBuilder {

        private long minimumUpgradableBuildNumber;

        private String minimumUpgradableVersion;

        private String version;

        private Date currentBuildDate;

        public BuildInfoBuilder minimumUpgradableBuildNumber(final long minimumUpgradableBuildNumber) {
            this.minimumUpgradableBuildNumber = minimumUpgradableBuildNumber;
            return this;
        }

        public BuildInfoBuilder minimumUpgradableVersion(final String minimumUpgradableVersion) {
            this.minimumUpgradableVersion = minimumUpgradableVersion;
            return this;
        }

        public BuildInfoBuilder version(final String version) {
            this.version = version;
            return this;
        }

        public BuildInfoBuilder currentBuildDate(final Date currentBuildDate) {
            this.currentBuildDate = currentBuildDate;
            return this;
        }

        public IBuildUtilsInfo build() {
            final IBuildUtilsInfo obj = new DefaultBuildUtilsInfo(version, currentBuildDate,
                    minimumUpgradableBuildNumber, minimumUpgradableVersion);
            return obj;
        }

    }
}
