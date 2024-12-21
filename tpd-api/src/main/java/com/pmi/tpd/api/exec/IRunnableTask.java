package com.pmi.tpd.api.exec;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRunnableTask extends IProgressReporter, Runnable {

    /**
     * Cancel the maintenance task.
     */
    void cancel();
}
