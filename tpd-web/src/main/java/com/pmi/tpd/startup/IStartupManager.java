package com.pmi.tpd.startup;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IProgressReporter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IStartupManager extends IProgressReporter {

    /**
     * @return
     */
    boolean isStarting();

    /**
     * @param progress
     */
    void onProgress(@Nonnull IProgress progress);
}
