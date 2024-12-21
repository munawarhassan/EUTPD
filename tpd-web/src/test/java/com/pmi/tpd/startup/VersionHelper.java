package com.pmi.tpd.startup;

import java.util.Date;

import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.versioning.impl.AbstractBuildUtilsInfo;

public abstract class VersionHelper {

    public static IBuildUtilsInfo builInfoOk() {
        return AbstractBuildUtilsInfo.builder()
                .currentBuildDate(new Date())
                .minimumUpgradableBuildNumber(0L)
                .minimumUpgradableVersion("0")
                .version("1.0.0.15")
                .build();
    }
}
