package com.pmi.tpd.scheduler.status;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.scheduler.AbstractSchedulerService;
import com.pmi.tpd.scheduler.support.SimpleJobDetails;

import io.atlassian.util.concurrent.LazyReference;

/**
 * A job details implementation that waits until the first request to one of the
 * methods that needs to know the state of
 * the parameters map before trying to deserialize it.
 * {@link com.pmi.tpd.core.scheduler.ISchedulerService}
 * implementations are encouraged to prefer this implementation when returning
 * multiple {@link IJobDetails} objects, as
 * the deserialization is unnecessary if the caller is not going to use those
 * values.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LazyJobDetails extends AbstractJobDetails {

  /** */
  private final LazyReference<AbstractJobDetails> delegateRef;

  /**
   * Creates a lazy job details representation.
   * <p>
   * <strong>WARNING</strong>: For efficiency reasons, {@code byte[] parameters}
   * is <em>not</em> copied.
   * </p>
   *
   * @param jobId
   *                     the job's ID
   * @param jobRunnerKey
   *                     the job runner's key
   * @param runMode
   *                     the running mode of the job
   * @param schedule
   *                     the job's schedule
   * @param nextRunTime
   *                     the next run time for the job, if known
   * @param parameters
   *                     the parameters map in serialized form. For efficiency
   *                     reasons, this constructor uses the original
   *                     array directly without making a safe copy.
   */
  public LazyJobDetails(final AbstractSchedulerService schedulerService, final JobId jobId,
      final JobRunnerKey jobRunnerKey, final RunMode runMode, final Schedule schedule,
      @Nullable final Date nextRunTime, @Nullable final byte[] parameters) {
    super(jobId, jobRunnerKey, runMode, schedule, nextRunTime, parameters);
    Assert.checkNotNull(schedulerService, "schedulerService");

    this.delegateRef = new LazyReference<>() {

      @Nonnull
      @Override
      protected AbstractJobDetails create() throws Exception {
        Throwable cause = null;
        try {
          final IJobRunner jobRunner = schedulerService.getJobRunner(jobRunnerKey);
          if (jobRunner != null) {
            final ClassLoader classLoader = jobRunner.getClass().getClassLoader();
            final Map<String, Serializable> parametersMap = schedulerService.getParameterMapSerializer()
                .deserializeParameters(classLoader, parameters);
            return new SimpleJobDetails(jobId, jobRunnerKey, runMode, schedule, nextRunTime, parameters,
                parametersMap);
          }
        } catch (final Exception ex) {
          cause = ex;
        } catch (final LinkageError err) {
          cause = err;
        }
        return new UnusableJobDetails(jobId, jobRunnerKey, runMode, schedule, nextRunTime, parameters, cause);
      }
    };
  }

  @Nonnull
  @Override
  public Map<String, Serializable> getParameters() {
    return getDelegate().getParameters();
  }

  @Override
  public boolean isRunnable() {
    return getDelegate().isRunnable();
  }

  @Override
  protected void appendToStringDetails(final StringBuilder sb) {
    if (delegateRef.isInitialized()) {
      final AbstractJobDetails delegate = getDelegate();
      sb.append(",delegate=").append(delegate.getClass().getSimpleName());
      delegate.appendToStringDetails(sb);
    } else {
      sb.append(",delegate=(unresolved)");
    }
  }

  // Hides the misleading @Nullable taint from LazyReference.get()
  private AbstractJobDetails getDelegate() {
    return delegateRef.get();
  }

}
