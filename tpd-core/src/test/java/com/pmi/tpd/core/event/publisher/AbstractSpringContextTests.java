package com.pmi.tpd.core.event.publisher;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.pmi.tpd.testing.junit5.TestCase;

@ExtendWith(SpringExtension.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public abstract class AbstractSpringContextTests extends TestCase implements ApplicationContextAware {

    /**
     * Logger available to subclasses.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The {@link ApplicationContext} that was injected into this test instance via
     * {@link #setApplicationContext(ApplicationContext)}.
     */
    protected ApplicationContext applicationContext;

    /**
     * Set the {@link ApplicationContext} to be used by this test instance, provided via {@link ApplicationContextAware}
     * semantics.
     */
    @Override
    public final void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
