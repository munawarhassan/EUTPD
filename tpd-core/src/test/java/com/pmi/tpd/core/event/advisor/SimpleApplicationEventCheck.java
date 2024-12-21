package com.pmi.tpd.core.event.advisor;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.pmi.tpd.api.event.advisor.IEventAdvisorAccessor;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;

public class SimpleApplicationEventCheck extends SimpleEventCheck implements IApplicationEventCheck<ServletContext> {

    @Override
    public void check(@Nonnull final IEventAdvisorAccessor advisorAccessor, @Nonnull final ServletContext context) {
    }
}
