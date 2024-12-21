package com.pmi.tpd.core.event.advisor;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.IEventAdvisorAccessor;
import com.pmi.tpd.api.event.advisor.event.IEventCheck;

/**
 * A check that is run every request.
 *
 * @param <REQUEST>
 *            type of request.
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IRequestEventCheck<REQUEST> extends IEventCheck {

    /**
     * <p>
     * check.
     * </p>
     *
     * @param advisorAccessor
     *            a {@link IEventAdvisorAccessor} object.
     * @param request
     *            a R object.
     */
    void check(@Nonnull IEventAdvisorAccessor advisorAccessor, @Nonnull REQUEST request);
}
