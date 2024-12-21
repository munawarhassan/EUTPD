package com.pmi.tpd.spring.context;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import com.pmi.tpd.spring.env.EnumerableCompositePropertySource;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * @author Dave Syer
 */
public class ConfigFileApplicationListenerTest extends TestCase {

    private final StandardEnvironment environment = new StandardEnvironment();

    @AfterEach
    public void cleanup() {
        System.clearProperty("the.property");
        System.clearProperty("spring.config.location");
        System.clearProperty("spring.main.showBanner");
    }

    @Test
    public void loadCustomResource() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, new ResourceLoader() {

            @Override
            public Resource getResource(final String location) {
                if (location.equals("classpath:/custom.properties")) {
                    return new ByteArrayResource("the.property: fromcustom".getBytes(), location) {

                        @Override
                        public String getFilename() {
                            return location;
                        }
                    };
                }
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }
        });
        loader.setSearchNames("custom");
        loader.load();
        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("fromcustom"));
    }

    @Test
    public void loadPropertiesFile() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);

        loader.setSearchNames("testproperties");
        loader.load();
        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("frompropertiesfile"));
    }

    @Test
    public void loadTwoPropertiesFile() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment,
            "spring.config.location:" + "classpath:application.properties,classpath:testproperties.properties");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();
        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("frompropertiesfile"));
    }

    @Test
    public void loadTwoPropertiesFilesWithProfiles() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment,
            "spring.config.location:" + "classpath:enableprofile.properties,classpath:enableother.properties");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();
        assertEquals("other", StringUtils.arrayToCommaDelimitedString(this.environment.getActiveProfiles()));
        final String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(property, equalTo("fromotherpropertiesfile"));
    }

    @Test
    public void loadTwoPropertiesFilesWithProfilesAndSwitchOneOff() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment,
            "spring.config.location:" + "classpath:enabletwoprofiles.properties,"
                    + "classpath:enableprofile.properties");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();
        assertEquals("myprofile", StringUtils.arrayToCommaDelimitedString(this.environment.getActiveProfiles()));
        final String property = this.environment.getProperty("the.property");
        // The value from the second file wins (no profile specific configuration is
        // actually loaded)
        MatcherAssert.assertThat(property, equalTo("frompropertiesfile"));
    }

    @Test
    public void loadTwoPropertiesFilesWithProfilesAndSwitchOneOffFromSpecificLocation() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment,
            "spring.config.name:enabletwoprofiles",
            "spring.config.location:classpath:enableprofile.properties");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();
        assertEquals("myprofile", StringUtils.arrayToCommaDelimitedString(this.environment.getActiveProfiles()));
        final String property = this.environment.getProperty("the.property");
        // The value from the second file wins (no profile specific configuration is
        // actually loaded)
        MatcherAssert.assertThat(property, equalTo("frompropertiesfile"));
    }

    @Test
    public void localFileTakesPrecedenceOverClasspath() throws Exception {
        final File localFile = new File(new File("."), "application.properties");
        MatcherAssert.assertThat(localFile.exists(), equalTo(false));
        try {
            final Properties properties = new Properties();
            properties.put("the.property", "fromlocalfile");
            final OutputStream out = new FileOutputStream(localFile);
            try {
                properties.store(out, "");
            } finally {
                out.close();
            }
            final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
            loader.load();
            final String property = this.environment.getProperty("the.property");
            MatcherAssert.assertThat(property, equalTo("fromlocalfile"));
        } finally {
            localFile.delete();
        }
    }

    // not work on eclipse, the namespace is not accessible by Classloader
    @Test
    public void moreSpecificLocationTakesPrecedenceOverRoot() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment, "spring.config.name:specific");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();
        final String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(property, equalTo("specific"));
    }

    @Test
    public void loadTwoOfThreePropertiesFile() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment,
            "spring.config.location:" + "classpath:application.properties," + "classpath:testproperties.properties,"
                    + "classpath:nonexistent.properties");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();
        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("frompropertiesfile"));
    }

    @Test
    public void loadTwoPropertiesFiles() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("moreproperties,testproperties");
        loader.load();

        final String property = this.environment.getProperty("the.property");
        // The search order has highest precedence last (like merging a map)
        MatcherAssert.assertThat(property, equalTo("frompropertiesfile"));
    }

    @Test
    public void loadYamlFile() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testyaml");
        loader.load();

        final String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(property, equalTo("fromyamlfile"));
        MatcherAssert.assertThat(this.environment.getProperty("my.array[0]"), equalTo("1"));
        MatcherAssert.assertThat(this.environment.getProperty("my.array"), nullValue(String.class));
    }

    @Test
    public void loadListYamlFile() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testlistyaml");
        loader.load();

        final String property = this.environment.getProperty("property");
        MatcherAssert.assertThat(property, equalTo("propertyName"));
        final KeyValueList v = this.environment.getProperty("my", KeyValueList.class);
        MatcherAssert.assertThat(v, isA(KeyValueList.class));

    }

    @Test
    public void commandLineWins() throws Exception {
        this.environment.getPropertySources()
                .addFirst(new SimpleCommandLinePropertySource("--the.property=fromcommandline"));
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testproperties");
        loader.load();

        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("fromcommandline"));
    }

    @Test
    public void systemPropertyWins() throws Exception {
        System.setProperty("the.property", "fromsystem");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testproperties");
        loader.load();

        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("fromsystem"));
    }

    @Test
    public void loadPropertiesThenProfilePropertiesActivatedInSpringApplication() throws Exception {
        // This should be the effect of calling
        // SpringApplication.setAdditionalProfiles("other")
        this.environment.setActiveProfiles("other");

        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();

        final String property = this.environment.getProperty("my.property");
        // The "other" profile is activated in SpringApplication so it should take
        // precedence over the default profile
        MatcherAssert.assertThat(property, equalTo("fromotherpropertiesfile"));
    }

    @Test
    public void twoProfilesFromProperties() throws Exception {
        // This should be the effect of calling
        // SpringApplication.setAdditionalProfiles("other", "dev")
        this.environment.setActiveProfiles("other", "dev");

        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();

        final String property = this.environment.getProperty("my.property");
        // The "dev" profile is activated in SpringApplication so it should take
        // precedence over the default profile
        MatcherAssert.assertThat(property, equalTo("fromdevpropertiesfile"));
    }

    @Test
    public void loadPropertiesThenProfilePropertiesActivatedInFirst() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("enableprofile");
        loader.load();

        final String property = this.environment.getProperty("the.property");
        // The "myprofile" profile is activated in enableprofile.properties so its value
        // should show up here
        MatcherAssert.assertThat(property, equalTo("fromprofilepropertiesfile"));
    }

    @Test
    public void loadPropertiesThenProfilePropertiesWithOverride() throws Exception {
        this.environment.setActiveProfiles("other");
        // EnvironmentTestUtils.addEnvironment(this.environment,
        // "spring.profiles.active:other");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("enableprofile");
        loader.load();

        String property = this.environment.getProperty("other.property");
        // The "other" profile is activated before any processing starts
        MatcherAssert.assertThat(property, equalTo("fromotherpropertiesfile"));
        property = this.environment.getProperty("the.property");
        // The "myprofile" profile is activated in enableprofile.properties and "other"
        // was not activated by setting spring.profiles.active so "myprofile" should
        // still
        // be activated
        MatcherAssert.assertThat(property, equalTo("fromprofilepropertiesfile"));
    }

    @Test
    public void profilePropertiesUsedInPlaceholders() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("enableprofile");
        loader.load();

        final String property = this.environment.getProperty("one.more");
        MatcherAssert.assertThat(property, equalTo("fromprofilepropertiesfile"));
    }

    @Test
    public void yamlProfiles() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testprofiles");

        this.environment.setActiveProfiles("dev");
        loader.load();
        String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(property, equalTo("fromdevprofile"));
        property = this.environment.getProperty("my.other");
        MatcherAssert.assertThat(property, equalTo("notempty"));
    }

    @Test
    public void yamlTwoProfiles() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testprofiles");

        this.environment.setActiveProfiles("other", "dev");
        loader.load();
        String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(property, equalTo("fromdevprofile"));
        property = this.environment.getProperty("my.other");
        MatcherAssert.assertThat(property, equalTo("notempty"));
    }

    @Test
    public void yamlSetsProfiles() throws Exception {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testsetprofiles");
        loader.load();
        assertEquals("dev", StringUtils.arrayToCommaDelimitedString(this.environment.getActiveProfiles()));
        final String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(Arrays.asList(this.environment.getActiveProfiles()), contains("dev"));
        MatcherAssert.assertThat(property, equalTo("fromdevprofile"));
        final ConfigurationPropertySources propertySource = (ConfigurationPropertySources) this.environment
                .getPropertySources()
                .get("applicationConfigurationProperties");
        final Collection<org.springframework.core.env.PropertySource<?>> sources = propertySource.getSource();
        assertEquals(2, sources.size());
        final List<String> names = new ArrayList<>();
        for (final org.springframework.core.env.PropertySource<?> source : sources) {
            if (source instanceof EnumerableCompositePropertySource) {
                for (final org.springframework.core.env.PropertySource<?> nested : ((EnumerableCompositePropertySource) source)
                        .getSource()) {
                    names.add(nested.getName());
                }
            } else {
                names.add(source.getName());
            }
        }
        MatcherAssert.assertThat(names,
            contains("applicationConfig: [classpath:/testsetprofiles.yml]#dev",
                "applicationConfig: [classpath:/testsetprofiles.yml]"));
    }

    @Test
    public void yamlProfileCanBeChanged() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment, "spring.profiles.active:prod");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.setSearchNames("testsetprofiles");
        loader.load();
        MatcherAssert.assertThat(this.environment.getActiveProfiles(), equalTo(new String[] { "prod" }));
    }

    @Test
    public void specificNameAndProfileFromExistingSource() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.environment,
            "spring.profiles.active=specificprofile",
            "spring.config.name=specificfile");
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();

        final String property = this.environment.getProperty("my.property");
        MatcherAssert.assertThat(property, equalTo("fromspecificpropertiesfile"));
    }

    @Test
    public void specificResource() throws Exception {
        final String location = "classpath:specificlocation.properties";
        EnvironmentTestUtils.addEnvironment(this.environment, "spring.config.location:" + location);
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();

        final String property = this.environment.getProperty("the.property");
        MatcherAssert.assertThat(property, equalTo("fromspecificlocation"));
        MatcherAssert.assertThat(this.environment,
            containsPropertySource("applicationConfig: " + "[classpath:specificlocation.properties]"));
        // The default property source is still there
        MatcherAssert.assertThat(this.environment,
            containsPropertySource("applicationConfig: " + "[classpath:/application.properties]"));
        MatcherAssert.assertThat(this.environment.getProperty("foo"), equalTo("bucket"));
    }

    @Test
    public void specificResourceAsFile() throws Exception {
        final String location = "file:src/test/resources/specificlocation.properties";
        EnvironmentTestUtils.addEnvironment(this.environment, "spring.config.location:" + location);
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();

        MatcherAssert.assertThat(this.environment, containsPropertySource("applicationConfig: [" + location + "]"));
    }

    @Test
    public void specificResourceDefaultsToFile() throws Exception {
        final String location = "src/test/resources/specificlocation.properties";
        EnvironmentTestUtils.addEnvironment(this.environment, "spring.config.location:" + location);
        final ConfigFileLoader loader = new ConfigFileLoader(environment, null);
        loader.load();

        MatcherAssert.assertThat(this.environment,
            containsPropertySource("applicationConfig: [file:" + location + "]"));
    }

    private static Matcher<? super ConfigurableEnvironment> containsPropertySource(final String sourceName) {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("environment containing property source ").appendValue(sourceName);
            }

            @Override
            protected boolean matchesSafely(final ConfigurableEnvironment item, final Description mismatchDescription) {
                final MutablePropertySources sources = new MutablePropertySources(item.getPropertySources());
                ConfigurationPropertySources.finishAndRelocate(sources);
                mismatchDescription.appendText("Not matched against: ").appendValue(sources);
                return sources.contains(sourceName);
            }
        };
    }

}
