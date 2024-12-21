package com.pmi.tpd.api.exec;

/**
 * Describes the possible states for a {@link IRunnableTask}.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public enum TaskState {

    /**
     * Indicates the action was canceled prior to completion.
     */
    CANCELED,
    /**
     * Indicates the action failed.
     */
    FAILED,
    /**
     * Indicates the action is still running.
     */
    RUNNING,
    /**
     * Indicates the action was successfully completed.
     */
    SUCCESSFUL,
}
