package com.pmi.tpd.api.scheduler;

/**
 * Indicates the status of task progress.
 *
 * @author Christophe Friederich
 * @since 1.4
 */
public interface ITaskMonitorProgress {

    /**
     * @param totalRows
     */
    void started(long totalRows);

    /**
     * Indicate the task has finished.
     */
    void finish();

    /**
     * the task has used one row out of the {@code totalRows} rows.
     */
    void increment();

    /**
     * Sets the message include in progress message.
     *
     * @param message
     *                the message used in progress message.
     */
    void setMessage(final String message);

    /**
     * Clear message.
     */
    void clearMessage();

    /**
     * Gets total rows.
     * 
     * @return
     */
    long getTotalRows();

}
