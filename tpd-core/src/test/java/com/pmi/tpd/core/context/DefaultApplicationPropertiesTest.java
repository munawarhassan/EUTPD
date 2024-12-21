package com.pmi.tpd.core.context;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.validation.Validator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.core.context.propertyset.PropertySetAccessor;
import com.pmi.tpd.core.event.TestEventPublisher;
import com.pmi.tpd.spring.context.ConfigFileLoader;
import com.pmi.tpd.spring.context.bind.ConfigurationPropertiesBinder;
import com.pmi.tpd.spring.context.bind.ResourceToString;
import com.pmi.tpd.spring.env.ConfigurationPropertiesBindingPostProcessor;
import com.pmi.tpd.testing.AbstractJunitTest;

import io.atlassian.util.concurrent.ResettableLazyReference;

public class DefaultApplicationPropertiesTest extends AbstractJunitTest {

    private DefaultApplicationProperties properties;

    private MockEnvironment environment;

    private TestEventPublisher eventPublisher;

    private IPropertiesManager propertiesManager;

    private BeanFactory beanFactory;

    private final DefaultConversionService conversionService = new DefaultConversionService();

    private ConfigurationPropertiesBinder binder;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        conversionService.addConverter(new ResourceToString());
        environment = new MockEnvironment();
        eventPublisher = new TestEventPublisher();
        beanFactory = mock(BeanFactory.class);
        environment.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY,
            PATH_HOME.getAbsolutePath());

        final GlobalApplicationConfiguration applicationConfiguration = new GlobalApplicationConfiguration(
                VersionHelper.builInfoOk(), environment);

        final Validator validator = new NoOpValidator();
        final ConfigurationPropertiesBindingPostProcessor postProcessor = new ConfigurationPropertiesBindingPostProcessor();
        postProcessor.setBeanFactory(beanFactory);
        postProcessor.setValidator(validator);
        postProcessor.setConversionService(conversionService);
        postProcessor.setEnvironment(environment);
        postProcessor.afterPropertiesSet();
        binder = new ConfigurationPropertiesBinder(this.beanFactory) {

            @Override
            protected ConfigurationPropertiesBindingPostProcessor getConfigurationPropertiesBindingPostProcessor() {
                return postProcessor;
            }
        };

        propertiesManager = new DefaultPropertiesManager(new ResettableLazyReference<IPropertyAccessor>() {

            @Override
            protected IPropertyAccessor create() throws Exception {
                final PropertySet ps = PropertySetManager.getInstance("properties",
                    ImmutableMap.of("file", applicationConfiguration.getHomeDirectory() + "/app.data"));
                final Map<String, Object> args = Maps.newHashMap();
                args.put("PropertySet", ps);
                args.put("bulkload", Boolean.TRUE);
                return new PropertySetAccessor(PropertySetManager.getInstance("cached", args));
            }

        });
        properties = new DefaultApplicationProperties(eventPublisher, () -> propertiesManager, environment,
                beanFactory);
        properties.setBinder(binder);
        properties.setConversionService(conversionService);
    }

    @AfterEach
    public void tearDown() {
        propertiesManager.refresh();
    }

    @Test
    public void nullEventPublisher() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultApplicationProperties(null, null, null, null);
        });
    }

    @Test()
    public void emptyConstructor() {

    }

    @Test
    public void onClearCache() throws Exception {
        // TODO [devacfr] add initialisation of properties
        properties.onClearCache(ClearCacheEvent.empty());
    }

    @Test
    public void verifyDefaultProperties() throws Exception {
        ConfigFileLoader.load(environment);

        final Collection<String> keys = properties.getDefaultKeys();
        assertNotNull(keys);
        // TODO [devacfr] find better solution to test number of default properties.
        assertTrue(keys.size() == 1, "one least proprety must exists");
    }

    @Test
    public void verifyDefaultPropertiesWithValues() throws Exception {
        final Environment environment = new MockEnvironment();
        properties = new DefaultApplicationProperties(eventPublisher, () -> this.propertiesManager, environment,
                beanFactory) {

            @Override
            protected Properties loadDefaultProperties(final Environment environment) {
                final Properties defaultProperties = new Properties();
                defaultProperties.put("key1", "value1");
                defaultProperties.put("key2", "value2");
                defaultProperties.put("keyOption", "true");
                return defaultProperties;
            }
        };

        final Collection<String> defaultKeys = properties.getDefaultKeys();
        assertNotNull(defaultKeys);
        assertEquals(3, defaultKeys.size());
        assertEquals("value1", properties.getDefaultBackedText("key1").get());
        properties.setString("key1", "newValue1");
        assertEquals("newValue1", properties.getDefaultBackedText("key1").get());

        assertEquals(true, properties.getOption("keyOption").get());

        assertEquals(true, properties.exists("key1"));
        assertEquals(false, properties.exists("key2"));

        properties.setText("key2", "newValue2");
        assertEquals("newValue2", properties.getString("key2").get());
        assertEquals("newValue2", properties.getDefaultBackedString("key2").get());
        assertEquals("newValue2", properties.getDefaultBackedText("key2").get());

        properties.flush();

        final Collection<String> keys = properties.getKeys();
        assertEquals(1, keys.size());

        final Map<String, Object> map = properties.asMap();
        assertEquals(3, map.size());

        properties.setString("key2", null);
        assertTrue(properties.getString("key2").isEmpty());
        properties.setString("key1", null);
        assertEquals(false, properties.exists("key2"));
        assertTrue(properties.getString("key1").isEmpty());

        properties.flush();

        properties.setOption("key.option1", true);
        assertEquals(true, properties.getOption("key.option1").get());

        properties.setString("key.name", "valueWithPrefix");
        final Collection<String> valuesWithPrefix = properties.getKeysWithPrefix("key");
        assertEquals(1, valuesWithPrefix.size());

    }

    @Test
    public void removeProperties() {
        properties.refresh();
        properties.setString("key2", "value1");
        properties.setString("key1", "value2");
        final Collection<String> keys = properties.asMap().keySet();
        for (final String key : keys) {
            properties.remove(key);
        }
    }

    @Test
    public void verifyEncodingProperty() {
        assertEquals("UTF-8", properties.getCharacterSet());
        properties.setString(ApplicationConstants.PropertyKeys.WEB_CHARACTER_SET, "US_ASCII");
        assertEquals("US_ASCII", properties.getCharacterSet());
    }

    @Test
    public void testDefaultLocale() {
        assertEquals(Locale.ENGLISH, properties.getDefaultLocale());
        properties.setString(ApplicationConstants.PropertyKeys.I18N_DEFAULT_LOCALE, Locale.FRENCH.toString());
        assertEquals(Locale.FRENCH, properties.getDefaultLocale());
    }

    @Test
    public void shouldStoreConfiguration() throws Exception {

        environment.setProperty("species.name", "P. concolor");
        environment.setProperty("species.animal.name", "Cougar");
        environment.setProperty("species.animal.conservationStatus", "Least Concern");
        environment.setProperty("species.animal.associateFile", "classpath:kb/cougar.zip");

        final Species configurationProperties = properties.getConfiguration(Species.class);
        properties.storeConfiguration(configurationProperties);

        final Collection<String> keys = properties.getKeysWithPrefix("species");

        assertEquals(4, keys.size());
        assertThat(keys,
            containsInAnyOrder("species.name",
                "species.animal.name",
                "species.animal.conservationStatus",
                "species.animal.associateFile"));

        assertEquals("P. concolor", properties.getString("species.name").get());
        assertEquals("Cougar", properties.getString("species.animal.name").get());
        assertEquals("Least Concern", properties.getString("species.animal.conservationStatus").get());
        assertEquals("kb/cougar.zip", properties.getString("species.animal.associateFile").get());
    }

    @Test()
    public void testRaiseChangeEvent() {
        // new
        properties.setString("app.env", "dev");
        assertThat(eventPublisher.getPublishedEvents(), hasSize(1));

        // update
        properties.setString("app.env", "qa");
        assertThat(eventPublisher.getPublishedEvents(), hasSize(2));

        // no change
        properties.setString("app.env", "qa");
        assertThat(eventPublisher.getPublishedEvents(), hasSize(2));
    }

    @ConfigurationProperties("species")
    public static class Species {

        private String name;

        private Animal animal;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Animal getAnimal() {
            return animal;
        }

        public void setAnimal(final Animal animal) {
            this.animal = animal;
        }

        public static class Animal {

            private String name;

            private String conservationStatus;

            private Resource associateFile;

            public String getName() {
                return name;
            }

            public void setName(final String name) {
                this.name = name;
            }

            public String getConservationStatus() {
                return conservationStatus;
            }

            public void setConservationStatus(final String conservationStatus) {
                this.conservationStatus = conservationStatus;
            }

            public Resource getAssociateFile() {
                return associateFile;
            }

            public void setAssociateFile(final Resource associateFile) {
                this.associateFile = associateFile;
            }
        }
    }
}
