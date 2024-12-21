package com.pmi.tpd.api.util.profiling;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Bean to contain information about the pages profiled.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ProfilingTimerBean implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    List<ProfilingTimerBean> children = new ArrayList<ProfilingTimerBean>();

    /** */
    ProfilingTimerBean parent = null;

    /** */
    String resource;

    /** */
    int frameCount;

    /** */
    long startTime;

    /** */
    long totalTime;

    /** */
    long startMem;

    /** */
    long totalMem;

    /** */
    boolean hasMem = false;

    public ProfilingTimerBean(final String resource) {
        this.resource = resource;
    }

    protected void addParent(final ProfilingTimerBean parent) {
        this.parent = parent;
    }

    public ProfilingTimerBean getParent() {
        return parent;
    }

    public void addChild(final ProfilingTimerBean child) {
        children.add(child);
        child.addParent(this);
    }

    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void setEndTime() {
        this.totalTime = System.currentTimeMillis() - startTime;
    }

    public void setStartMem() {
        this.startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.hasMem = true;
    }

    public void setEndMem() {
        // Only capture total memory if we captured the startMem
        if (hasMem) {
            this.totalMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - startMem;
        }
    }

    public String getResource() {
        return resource;
    }

    /**
     * Get a formatted string representing all the methods that took longer than a specified time.
     */

    public String getPrintable(final long minTime) {
        return getPrintable("", minTime);
    }

    protected String getPrintable(final String indent, final long minTime) {
        // only print the value if we are larger or equal to the min time.
        if (totalTime >= minTime) {
            final StringBuilder builder = new StringBuilder();
            builder.append(indent);
            builder.append("[").append(totalTime).append("ms] ");
            if (hasMem) {
                builder.append("[").append(totalMem / 1024).append("KB used] ");
                builder.append("[").append(Runtime.getRuntime().freeMemory() / 1024).append("KB Free] ");
            }

            builder.append("- ").append(resource);
            builder.append("\n");

            for (final ProfilingTimerBean aChildren : children) {
                builder.append(aChildren.getPrintable(indent + "  ", minTime));
            }

            return builder.toString();
        } else {
            return "";
        }
    }

    public long getTotalTime() {
        return totalTime;
    }

    int getFrameCount() {
        return frameCount;
    }

    void setFrameCount(final int frameCount) {
        this.frameCount = frameCount;
    }

    void removeChild(final ProfilingTimerBean child) {
        final ListIterator<ProfilingTimerBean> childrenIt = children.listIterator(children.size());
        while (childrenIt.hasPrevious()) {
            final ProfilingTimerBean time = childrenIt.previous();
            if (time == child) {
                childrenIt.remove();
                return;
            }
        }
    }
}
