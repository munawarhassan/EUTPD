package com.pmi.tpd.scheduler.exec;

import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class CompositeRunableTask extends AbstractRunnableTask  {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeRunableTask.class);

    /** */
    private volatile int currentStep;

    /** */
    private final Step[] steps;

    /** */
    private final int totalWeight;

    /**
     * @param steps
     * @param totalWeight
     */
    protected CompositeRunableTask(@Nonnull final Step[] steps, final int totalWeight) {
        this.currentStep = 0;
        this.steps = Assert.checkNotNull(steps, "steps");
        this.totalWeight = totalWeight;
    }

    @Override
    public void cancel() {
        super.cancel();

        for (int i = currentStep; i < steps.length; ++i) {
            steps[i].cancel();
        }
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        final int curStep = currentStep;
        long completed = 0L;

        for (int i = 0; i < curStep; ++i) {
            // Since all previous tasks are complete and 100 by definition, we only need their weight not their progress
            completed += 100 * steps[i].getWeight();
        }

        // Although not yet started by this composite task, it's possible some tasks track external progress and already
        // have non zero percentages. So that progress doesn't jump when we finally get to them, we include their
        // progress now
        for (int i = curStep; i < steps.length; ++i) {
            completed += Math.max(0, Math.min(100, steps[i].getProgress().getPercentage()))
                    * (long) steps[i].getWeight();
        }

        IProgress stepProgress;
        if (curStep < steps.length) {
            // Regardless of what is going on in future steps re percentages, only ever take the progress message from
            // the currently executing step
            stepProgress = steps[curStep].getProgress();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Current step:'{}', progress: {}.", steps[curStep].getName(), stepProgress);
            }
        } else {
            stepProgress = steps[steps.length - 1].getProgress();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Current step:'{}', progress: {}.", steps[steps.length - 1].getName(), stepProgress);
            }
        }

        // Calculate the percentage (between 0 and 100)
        final double progressPercent = Math.ceil(completed * 1.0d / totalWeight);
        final IProgress progress = new ProgressImpl(stepProgress.getMessage(), (int) Math.round(progressPercent));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: current progress {}.", this.getClass().getSimpleName(), progress);
        }
        return progress;
    }

    @Override
    public void run() {
        for (currentStep = 0; currentStep < steps.length && !isCanceled(); ++currentStep) {
            steps[currentStep].run();
        }
    }

    /**
     * @author Christophe Friederich
     * @param <B>
     */
    protected abstract static class AbstractBuilder<B extends AbstractBuilder<B>> {

        /** */
        protected final List<Step> steps = Lists.newArrayList();

        /** */
        protected int totalWeight;

        @Nonnull
        public CompositeRunableTask build() {
            Assert.state(!steps.isEmpty(), "at least one step must be provided");
            return new CompositeRunableTask(steps.toArray(new Step[steps.size()]), totalWeight);
        }

        /**
         * @param step
         * @param weight
         * @return
         */
        public B add(@Nonnull final IRunnableTask step, final int weight) {
            Assert.checkNotNull(step, "step");
            Assert.isTrue(weight >= 0);

            steps.add(new Step(step, weight));
            totalWeight += weight;

            return self();
        }

        /**
         * @return
         */
        protected abstract B self();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Builder self() {
            return this;
        }
    }

    /**
     * @author Christophe Friederich
     */
    protected static class Step implements IRunnableTask {

        /** */
        private final IRunnableTask delegateTask;

        /** */
        private final int weight;

        /**
         * @param delegate
         * @param weight
         */
        public Step(final IRunnableTask delegate, final int weight) {
            this.delegateTask = delegate;
            this.weight = weight;
        }

        @Override
        public String getName() {
            return delegateTask.getName();
        }

        @Override
        public void cancel() {
            delegateTask.cancel();
        }

        @Nonnull
        @Override
        public IProgress getProgress() {
            return delegateTask.getProgress();
        }

        /**
         * @return
         */
        public int getWeight() {
            return weight;
        }

        @Override
        public void run() {
            delegateTask.run();
        }
    }
}
