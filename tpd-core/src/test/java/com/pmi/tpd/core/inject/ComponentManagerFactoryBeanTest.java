package com.pmi.tpd.core.inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.pmi.tpd.core.inject.IComponentManager.Scope;
import com.pmi.tpd.testing.AbstractJunitTest;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ComponentManagerFactoryBeanTest.Config.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ComponentManagerFactoryBeanTest extends AbstractJunitTest {

    @Autowired
    private IComponentManager componentManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void componentManagerExist() {
        assertNotNull(componentManager);

        final IComponentManager componentManager = ComponentManagerFactoryBean.getInjector();
        assertSame(this.componentManager, componentManager);
    }

    @Test
    public void componentManagerExistInFactory() {
        assertNotNull(IComponentManager.Factory.getInstance());
    }

    @Test
    public void registerComponent() {
        final Foo bean = componentManager.registerComponentImplementation(Foo.class, Scope.Singleton);
        assertNotNull(bean);
    }

    @Test
    public void registerSingletonComponentWithName() {
        final Foo bean = componentManager.registerSingletonComponentImplementation(Foo.class, "foo1");
        final Foo foo2 = componentManager.registerSingletonComponentImplementation(Foo.class, "foo2");
        assertNotNull(bean);
        final Foo foo1 = componentManager.getComponentInstanceOfType(Foo.class, "foo1");
        assertSame(bean, foo1);
        assertNotSame(foo2, foo1);
    }

    @Test
    public void registerSinglgeton() {
        final Foo bean = componentManager.registerSingletonComponentImplementation(Foo.class);
        assertNotNull(bean);
        final Foo foo1 = componentManager.getComponentInstanceOfType(Foo.class);
        assertSame(bean, foo1);
    }

    @Test
    public void registerBean() {
        final Foo bean = componentManager.registerComponentImplementation(Foo.class, Scope.Prototype);
        assertNotNull(bean);
        final Foo foo1 = componentManager.getComponentInstanceOfType(Foo.class);
        assertNotSame(bean, foo1);
    }

    public static class Foo {

    }

    @Configuration
    public static class Config {

        @Bean
        public ComponentManagerFactoryBean componentManager() {
            return new ComponentManagerFactoryBean();
        }
    }
}
