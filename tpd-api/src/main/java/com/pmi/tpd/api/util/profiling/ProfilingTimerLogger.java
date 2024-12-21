package com.pmi.tpd.api.util.profiling;

/**
 * Allows {@link ProfilingTimerStack#setLogger plugging} a custom logger into {@link ProfilingTimerStack}.
 */
public interface ProfilingTimerLogger {

    /**
     * Writes a string to the logger.
     *
     * @param s
     *            the string to write
     */
    void log(String s);
}
