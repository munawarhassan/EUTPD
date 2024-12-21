package com.pmi.tpd.core.upgrade;

import java.util.Collection;
import java.util.Collections;

import com.pmi.tpd.api.util.Assert;

public class MockUpgradeTask implements IUpgradeTask {

    private final String buildNumber;

    private final String shortDescription;

    private final SetupType setupType;

    private final String version;

    public MockUpgradeTask(final String version, final String buildNumber, final SetupType setupType,
            final String shortDescription) {
        Assert.notNull(version);
        Assert.notNull(buildNumber);
        this.version = version;
        this.shortDescription = shortDescription;
        this.setupType = Assert.notNull(setupType);
        this.buildNumber = buildNumber;
    }

    @Override
    public SetupType getSetup() {
        return setupType;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getBuildNumber() {
        return buildNumber;
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    @Override
    public void doUpgrade() {
    }

    @Override
    public Collection<String> getErrors() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "version: " + buildNumber + ", shortDesctription: " + shortDescription;
    }

    public String getClassName() {
        return "MockUpgradeTask" + buildNumber;
    }
}
