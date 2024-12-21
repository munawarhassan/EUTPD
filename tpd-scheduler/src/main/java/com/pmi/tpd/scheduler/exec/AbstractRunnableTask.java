package com.pmi.tpd.scheduler.exec;

import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.scheduler.ITaskFactory;

public abstract class AbstractRunnableTask implements IRunnableTask {

    /** */
    private volatile boolean canceled;

    /** */
    private ITaskFactory taskFactory;

    /**
     * @param taskFactory
     */
    public void setTaskFactory(final ITaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    /**
     * @return
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * @return
     */
    protected ITaskFactory getTaskFactory() {
        return taskFactory;
    }

    protected <T> T createInstance(final Class<T> cl) {
        return taskFactory.createInstance(cl);
    }

}
