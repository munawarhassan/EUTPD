package com.pmi.tpd.startup;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;

import com.google.common.collect.Lists;
import com.pmi.tpd.core.context.GlobalApplicationConfiguration;
import com.pmi.tpd.core.startup.IStartupCheck;
import com.pmi.tpd.startup.check.HomeStartupCheck;
import com.pmi.tpd.web.testing.AbstractJunitTest;

public class StartupChecklistTest extends AbstractJunitTest {

    private GlobalApplicationConfiguration applicationConfiguration;

    private Environment environment;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] {});

        applicationConfiguration = new GlobalApplicationConfiguration(VersionHelper.builInfoOk(), environment);
        StartupChecklist.reset();
    }

    @Test
    public void emptyStartupCheck() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StartupChecklist(null, applicationConfiguration);
        });
    }

    @Test
    public void onApplicationShutdown() {
        final List<IStartupCheck> list = Lists.<IStartupCheck> newArrayList(FooCheck.ok,
            FooCheck.noOk,
            new HomeStartupCheck(applicationConfiguration, environment));
        final StartupChecklist checklist = new StartupChecklist(list, applicationConfiguration);
        checklist.onApplicationEvent(new ContextClosedEvent(mock(ApplicationContext.class)));
        checklist.shutdownOk();
    }

    @Test
    public void startupOK() {
        final List<IStartupCheck> list = Lists.<IStartupCheck> newArrayList(FooCheck.ok,
            new HomeStartupCheck(applicationConfiguration, environment));
        final StartupChecklist checklist = new StartupChecklist(list, applicationConfiguration);
        assertEquals(true, checklist.startupOK());
    }

    @Test
    public void startupNoOK() {
        final List<IStartupCheck> list = Lists.<IStartupCheck> newArrayList(FooCheck.noOk,
            new HomeStartupCheck(applicationConfiguration, environment));
        final StartupChecklist checklist = new StartupChecklist(list, applicationConfiguration);
        assertEquals(false, checklist.startupOK());
        assertEquals(FooCheck.noOk, StartupChecklist.getFailedStartupCheck());
    }

    @Test
    public void setFailedStartupCheck() {
        StartupChecklist.setFailedStartupCheck(FooCheck.noOk);
    }

    public static class FooCheck implements IStartupCheck {

        private final boolean isOk;

        public static FooCheck ok = new FooCheck(true);

        public static FooCheck noOk = new FooCheck(false);

        private FooCheck(final boolean isOk) {
            this.isOk = isOk;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getFaultDescription() {
            return "bad new";
        }

        @Override
        public String getHtmlFaultDescription() {
            return "<code>bad new</code>";
        }

        @Override
        public String getName() {
            return "foo" + isOk;
        }

        @Override
        public boolean isOk() {
            return isOk;
        }

    }
}
