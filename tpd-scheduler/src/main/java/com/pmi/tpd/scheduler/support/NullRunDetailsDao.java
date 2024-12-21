package com.pmi.tpd.scheduler.support;

import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.status.IRunDetails;
import com.pmi.tpd.scheduler.spi.IRunDetailsDao;

/**
 * An implementation of {@code RunDetailsDao} that silently discards all run details supplied to it.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class NullRunDetailsDao implements IRunDetailsDao {

    @Override
    public IRunDetails getLastRunForJob(final JobId jobId) {
        return null;
    }

    @Override
    public IRunDetails getLastSuccessfulRunForJob(final JobId jobId) {
        return null;
    }

    @Override
    public void addRunDetails(final JobId jobId, final IRunDetails runDetails) {
    }
}
