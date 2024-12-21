package com.pmi.tpd.api.util.profiling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A timer stack.
 * </p>
 * Usage:
 *
 * <pre>
 * String logMessage = "Log message";
 * UtilTimerStack.push(logMessage);
 * try {
 *     // do some code
 * } finally {
 *     UtilTimerStack.pop(logMessage); // this needs to be the same text as above
 * }
 * </pre>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class ProfilingTimerStack {

    /**
     * System property that specifies by default whether this timer should be used or not. Set to "true" activates the
     * timer. Set to "false" to deactivate.
     */
    public static final String ACTIVATE_PROPERTY = "app.profile.activate";

    /**
     * System property that specifies by default whether memory should be profiled or not. Set to "true" activates the
     * timer. Set to "false" to deactivate.
     */
    public static final String ACTIVATE_MEMORY_PROPERTY = "app.profile.activate.memory";

    /**
     * System property that controls the default threshold time below which a profiled event should not be reported.
     */
    public static final String MIN_TIME = "app.profile.mintime";

    /**
     * System property that controls the default threshold time below which an entire stack of profiled events should
     * not be reported.
     */
    public static final String MIN_TOTAL_TIME = "app.profile.mintotaltime";

    /**
     * System property that controls the default maximum number of timer frames that can be reported per timer stack.
     */
    public static final String MAX_FRAME_COUNT = "app.profile.maxframecount";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilingTimerStack.class);

    /** A reference to the current ProfilingTimerBean. */
    private static ThreadLocal<ProfilingTimerBean> current = new ThreadLocal<ProfilingTimerBean>();

    /** */
    private static volatile int configuredMaxFrameCount;

    /** */
    private static volatile long configuredMinTime;

    /** */
    private static volatile long configuredMinTotalTime;

    /** */
    private static volatile boolean activeFlag = false;

    /** */
    private static volatile boolean profileMemoryFlag = false;

    // set default settings from the system properties.
    static {
        configuredMaxFrameCount = Integer.getInteger(MAX_FRAME_COUNT, 1500);
        configuredMinTime = Long.getLong(MIN_TIME, 0);
        configuredMinTotalTime = Long.getLong(MIN_TOTAL_TIME, 0);
        activeFlag = "true".equalsIgnoreCase(System.getProperty(ACTIVATE_PROPERTY, "false"));
        profileMemoryFlag = "true".equalsIgnoreCase(System.getProperty(ACTIVATE_MEMORY_PROPERTY, "false"));
    }

    private ProfilingTimerStack() {
    }

    private static ProfilingTimerLogger logger = new ProfilingTimerLogger() {

        public void log(final String s) {
            LOGGER.debug(s);
        }
    };

    public static void push(final String name) {
        if (!isActive()) {
            return;
        }

        // create a new timer and start it
        final ProfilingTimerBean newTimer = new ProfilingTimerBean(name);
        newTimer.setStartTime();

        if (isProfileMemory()) {
            newTimer.setStartMem();
        }

        // if there is a current timer - add the new timer as a child of it
        final ProfilingTimerBean currentTimer = current.get();
        if (currentTimer != null) {
            currentTimer.addChild(newTimer);
            newTimer.setFrameCount(currentTimer.getFrameCount() + 1);
        }

        // set the new timer to be the current timer
        current.set(newTimer);
    }

    public static void pop(final String name) {
        // We no longer check for isActive, as we want to cleanup the current stack of profiling beans
        final ProfilingTimerBean currentTimer = current.get();
        if (currentTimer == null) {
            return;
        }

        currentTimer.setEndMem();

        // if the timers are matched up with each other (ie push("a"); pop("a"));
        if (name != null && name.equals(currentTimer.getResource())) {
            currentTimer.setEndTime();
            final ProfilingTimerBean parent = currentTimer.getParent();
            // if we are the root timer, then print out the times
            if (parent == null) {
                if (currentTimer.getTotalTime() > getMinTotalTime()) {
                    printTimes(currentTimer);
                }

                current.remove(); // for those servers that use thread pooling
            } else {
                if (currentTimer.getTotalTime() < getMinTime() || currentTimer.getFrameCount() > getMaxFrameCount()) {
                    parent.removeChild(currentTimer);
                }

                parent.setFrameCount(currentTimer.getFrameCount());
                current.set(parent);
            }
        } else {
            // if timers are not matched up, then print what we have, and then print warning.
            printTimes(currentTimer);
            current.remove(); // prevent printing multiple times
            LOGGER.debug("Unmatched Timer.  Was expecting {}, instead got {}", currentTimer.getResource(), name);
        }
    }

    private static void printTimes(final ProfilingTimerBean currentTimer) {
        final String printable = currentTimer.getPrintable(getMinTime());
        if (printable != null && !"".equals(printable.trim())) {
            logger.log(printable);
        }
    }

    static ProfilingTimerLogger getLogger() {
        return logger;
    }

    public static void setLogger(final ProfilingTimerLogger logger) {
        ProfilingTimerStack.logger = logger;
    }

    static int getMaxFrameCount() {
        return configuredMaxFrameCount;
    }

    public static void setMaxFrameCount(final int maxFrameCount) {
        configuredMaxFrameCount = maxFrameCount;
    }

    static long getMinTime() {
        return configuredMinTime;
    }

    public static void setMinTime(final long minTime) {
        configuredMinTime = minTime;
    }

    static long getMinTotalTime() {
        return configuredMinTotalTime;
    }

    public static void setMinTotalTime(final long minTotalTime) {
        configuredMinTotalTime = minTotalTime;
    }

    public static boolean isActive() {
        return activeFlag;
    }

    public static void setActive(final boolean active) {
        activeFlag = active;
    }

    public static boolean isProfileMemory() {
        return profileMemoryFlag;
    }

    public static void setProfileMemory(final boolean active) {
        profileMemoryFlag = active;
    }

}
