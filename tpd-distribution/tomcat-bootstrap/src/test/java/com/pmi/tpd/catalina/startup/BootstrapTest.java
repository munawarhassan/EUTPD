package com.pmi.tpd.catalina.startup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pmi.tpd.testing.junit5.TestCase;

public class BootstrapTest extends TestCase {

    public Path temporaryFolder;

    private String startingAppHome;

    private String startingAppSharedHome;

    @BeforeEach
    public void captureSystemProperties(@TempDir final Path path) {
        temporaryFolder = path;
        startingAppHome = System.getProperty(Bootstrap.SYS_PROP_HOME);
        startingAppSharedHome = System.getProperty(Bootstrap.SYS_PROP_SHARED_HOME);
    }

    @BeforeAll
    public static void initLog() {

        Bootstrap.initLog();
    }

    @AfterEach
    public void restoreSystemProperties() {
        if (startingAppHome == null) {
            System.clearProperty(Bootstrap.SYS_PROP_HOME);
        } else {
            System.setProperty(Bootstrap.SYS_PROP_HOME, startingAppHome);
        }
        if (startingAppHome == null) {
            System.clearProperty(Bootstrap.SYS_PROP_SHARED_HOME);
        } else {
            System.setProperty(Bootstrap.SYS_PROP_SHARED_HOME, startingAppSharedHome);
        }
    }

    @Test
    public void testEmptyArguments() throws IOException {
        assertArrayEquals(new String[] {}, new EnvVarOverridingArgumentDecorator().apply(new String[] {}));
    }

    @Test
    public void testArgumentsUntouchedIfConfigSuppliedAndPropsSet() throws IOException {
        final File serverXml = new File(temporaryFolder.toFile(), "server.xml");
        serverXml.createNewFile();
        System.setProperty(Bootstrap.SYS_PROP_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "-config", "foo", "startup" },
            new EnvVarOverridingArgumentDecorator().apply(new String[] { "-config", "foo", "startup" }));
        System.clearProperty(Bootstrap.SYS_PROP_HOME);
        System.setProperty(Bootstrap.SYS_PROP_SHARED_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "-config", "foo", "startup" },
            new EnvVarOverridingArgumentDecorator().apply(new String[] { "-config", "foo", "startup" }));
    }

    @Test
    public void testArgumentsUntouchedIfConfigSuppliedAndEnvVarsSupplied() throws IOException {
        final File serverXml = new File(temporaryFolder.toFile(), "server.xml");
        serverXml.createNewFile();
        assertArrayEquals(new String[] { "-config", "foo", "startup" },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_HOME, temporaryFolder.toFile().getCanonicalPath())
                    .apply(new String[] { "-config", "foo", "startup" }));
        assertArrayEquals(new String[] { "-config", "foo", "startup" },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_SHARED_HOME,
                    temporaryFolder.toFile().getCanonicalPath()).apply(new String[] { "-config", "foo", "startup" }));
    }

    @Test
    public void testArgumentsUntouchedIfServerXmlNotFoundAndPropsSet() throws IOException {
        System.setProperty(Bootstrap.SYS_PROP_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "startup" },
            new EnvVarOverridingArgumentDecorator().apply(new String[] { "startup" }));
        System.clearProperty(Bootstrap.SYS_PROP_HOME);
        System.setProperty(Bootstrap.ENV_VAR_SHARED_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "startup" },
            new EnvVarOverridingArgumentDecorator().apply(new String[] { "startup" }));
    }

    @Test
    public void testArgumentsUntouchedIfServerXmlNotFoundAndEnvVarsSupplied() throws IOException {
        assertArrayEquals(new String[] { "startup" },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_HOME, temporaryFolder.toFile().getCanonicalPath())
                    .apply(new String[] { "startup" }));
        assertArrayEquals(new String[] { "startup" },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_SHARED_HOME,
                    temporaryFolder.toFile().getCanonicalPath()).apply(new String[] { "startup" }));
    }

    @Test
    public void testServerXmlPrependedIfFoundAndPropsSet() throws IOException {
        final File serverXml = new File(temporaryFolder.toFile(), "server.xml");
        serverXml.createNewFile();
        System.setProperty(Bootstrap.SYS_PROP_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath(), "startup" },
            new EnvVarOverridingArgumentDecorator().apply(new String[] { "startup" }));
        System.clearProperty(Bootstrap.SYS_PROP_HOME);
        System.setProperty(Bootstrap.SYS_PROP_SHARED_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath(), "startup" },
            new EnvVarOverridingArgumentDecorator().apply(new String[] { "startup" }));
    }

    @Test
    public void testServerXmlPrependedIfFoundAndEnvVarsSupplied() throws IOException {
        final File serverXml = new File(temporaryFolder.toFile(), "server.xml");
        serverXml.createNewFile();
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath(), "startup" },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_HOME, temporaryFolder.toFile().getCanonicalPath())
                    .apply(new String[] { "startup" }));
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath(), "startup" },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_SHARED_HOME,
                    temporaryFolder.toFile().getCanonicalPath()).apply(new String[] { "startup" }));
    }

    @Test
    public void testServerXmlUsedIfFoundAndArgumentsEmptyAndPropsSet() throws IOException {
        final File serverXml = new File(temporaryFolder.toFile(), "server.xml");
        serverXml.createNewFile();
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath() },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_HOME, temporaryFolder.toFile().getCanonicalPath())
                    .apply(new String[] {}));
        System.clearProperty(Bootstrap.SYS_PROP_HOME);
        System.setProperty(Bootstrap.SYS_PROP_SHARED_HOME, temporaryFolder.toFile().getCanonicalPath());
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath() },
            new EnvVarOverridingArgumentDecorator().apply(new String[] {}));
    }

    @Test
    public void testServerXmlUsedIfFoundAndArgumentsEmptyAndEnvVarsSupplied() throws IOException {
        final File serverXml = new File(temporaryFolder.toFile(), "server.xml");
        serverXml.createNewFile();
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath() },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_HOME, temporaryFolder.toFile().getCanonicalPath())
                    .apply(new String[] {}));
        assertArrayEquals(new String[] { "-config", serverXml.getCanonicalPath() },
            new EnvVarOverridingArgumentDecorator(Bootstrap.ENV_VAR_SHARED_HOME,
                    temporaryFolder.toFile().getCanonicalPath()).apply(new String[] {}));
    }

    private static class EnvVarOverridingArgumentDecorator extends Bootstrap.ArgumentDecorator {

        private final Map<String, String> envVars = new HashMap<>();

        public EnvVarOverridingArgumentDecorator(final String... kvp) {
            for (int i = 0; i < kvp.length; i += 2) {
                envVars.put(kvp[i], kvp[i + 1]);
            }
        }

        @Override
        protected String getEnvVariable(final String name) {
            return envVars.get(name);
        }
    }
}
