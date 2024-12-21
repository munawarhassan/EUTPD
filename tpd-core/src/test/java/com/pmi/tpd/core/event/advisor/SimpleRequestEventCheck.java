package com.pmi.tpd.core.event.advisor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.pmi.tpd.api.event.advisor.IEventAdvisorAccessor;

public class SimpleRequestEventCheck extends SimpleEventCheck implements IRequestEventCheck<HttpServletRequest> {

    @Override
    public void check(@Nonnull final IEventAdvisorAccessor advisorAccessor, @Nonnull final HttpServletRequest request) {
    }
}
