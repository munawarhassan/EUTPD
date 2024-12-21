package com.pmi.tpd.core.versioning.impl;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.pmi.tpd.core.versioning.BuildUtils;

/**
 * This class gives access to build-time properties at runtime.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class BuildUtilsInfoGitImpl extends AbstractBuildUtilsInfo {

    /** Constant <code>VERSION_PATTERN</code>. */
    public static final Pattern VERSION_PATTERN = Pattern
            .compile("((?:(?:\\d+)\\.)?(?:(?:\\d+)\\.)?(?:\\*|\\d+))(?:(-\\w*)*)-([0-9]+)-(?:(\\w*)-)(.*)");

    /**
     * <p>
     * Constructor for BuildUtilsInfoGitImpl.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     * @param properties
     *            a {@link java.util.Properties} object.
     */
    public BuildUtilsInfoGitImpl(final long minimumUpgradableBuildNumber, final String minimumUpgradableVersion,
            final Properties properties) {
        super(minimumUpgradableBuildNumber, minimumUpgradableVersion, properties);

    }

    /**
     * <p>
     * Constructor for BuildUtilsInfoGitImpl.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     */
    public BuildUtilsInfoGitImpl(final long minimumUpgradableBuildNumber, final String minimumUpgradableVersion) {
        super(minimumUpgradableBuildNumber, minimumUpgradableVersion);
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
    protected String transformVersion(@Nonnull String version) {
        final StringBuilder ver = new StringBuilder();
        if (version.length() > 0 && !Character.isDigit(version.charAt(0))) {
            version = version.substring(1);
        }
        final Matcher m = VERSION_PATTERN.matcher(version);
        if (m.find()) {
            ver.append(m.group(1)); // version number
            ver.append('-');
            ver.append(normalizeBranch(m.group(5))); // branch
            ver.append('.');
            ver.append(normalizeBuildNumber(m.group(1), m.group(3))); // build number
        }
        return ver.toString();
    }

    private static String normalizeBuildNumber(@Nonnull final String version, @Nonnull final String increment) {
        return BuildUtils.toLong(version + "." + increment).toString();
    }

    private static String normalizeBranch(@Nullable String branchName) {
        if (branchName == null) {
            return null;
        }
        branchName = branchName.trim();
        if (branchName.length() <= 25) {
            return branchName;
        }
        final int index = branchName.indexOf('-');
        if (index < 0) {
            return branchName.substring(0, Math.min(branchName.length(), 25));
        }
        final int second = branchName.indexOf('-', index + 1);
        if (second > index) {
            return branchName.substring(0, second);
        }
        return branchName.substring(0, index);
    }

}
