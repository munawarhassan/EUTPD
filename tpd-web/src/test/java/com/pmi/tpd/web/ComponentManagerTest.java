package com.pmi.tpd.web;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ComponentManagerTest.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ComponentManagerTest extends MockitoTestCase {

    @Autowired
    private ComponentManager componentManager;

    @Bean
    public IEventPublisher eventPublisher() {
        return mock(IEventPublisher.class);
    }

    @Bean
    public ComponentManager componentManager(@Nonnull final IEventPublisher eventPublisher) {
        return new ComponentManager(eventPublisher);
    }

    @Test
    public void getInstanceReturnSameInstance() {
        assertSame(componentManager, ComponentManager.getInstance());
    }

}
