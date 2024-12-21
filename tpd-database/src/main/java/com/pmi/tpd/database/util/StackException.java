package com.pmi.tpd.database.util;

/**
 * An {@code Exception} specialized for use when logging a thread's stack trace.
 */
public class StackException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public StackException(final Thread thread) {
        super("Stack trace for " + thread.getName(), null, false, true);

        setStackTrace(thread.getStackTrace());
    }

    /**
     * Skips filling in the stack trace, since it will be overwritten in the constructor anyway.
     *
     * @return
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
