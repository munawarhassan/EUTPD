package com.pmi.tpd.spring.context.bind;

import static org.hamcrest.Matchers.isA;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.validation.Validator;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.api.config.annotation.NoPersistent;
import com.pmi.tpd.spring.context.NoOpValidator;
import com.pmi.tpd.spring.context.bind.ConfigurationPropertiesBinder.PropertyPath;
import com.pmi.tpd.spring.env.ConfigurationPropertiesBindingPostProcessor;
import com.pmi.tpd.spring.env.ConfigurationPropertiesBindingPostProcessorRegistrar;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ConfigurationPropertiesBinderTest extends MockitoTestCase {

    @Spy
    private final ConversionService conversionService = new DefaultConversionService();

    @Spy
    private final Validator validator = new NoOpValidator();

    @Mock(lenient = true)
    private BeanFactory beanFactory;

    private final MockEnvironment environment = new MockEnvironment();

    @InjectMocks
    @Spy
    private ConfigurationPropertiesBinder binder;

    @BeforeEach
    public void setUp() throws Exception {

        environment.setProperty("species.name", "P. concolor");
        environment.setProperty("species.animal.name", "Cougar");
        environment.setProperty("species.animal.conservationStatus", "Least Concern");
        environment.setProperty("species.animal.associateFile", "classpath:kb/cougar.zip");

        final ConfigurationPropertiesBindingPostProcessor postProcessor = new ConfigurationPropertiesBindingPostProcessor();
        postProcessor.setBeanFactory(beanFactory);
        postProcessor.setValidator(validator);
        postProcessor.setConversionService(conversionService);
        postProcessor.setEnvironment(environment);
        postProcessor.afterPropertiesSet();

        when(beanFactory.getBean(ConfigurationPropertiesBindingPostProcessorRegistrar.BINDER_BEAN_NAME,
            ConfigurationPropertiesBindingPostProcessor.class)).thenReturn(postProcessor);
    }

    @Test
    public void shouldCreateAndBindConfiguration() {
        final Species properties = binder.createAndBindConfiguration(Species.class);
        assertNotNull(properties);
        assertEquals("P. concolor", properties.getName());
        assertEquals("Cougar", properties.getAnimal().getName());
        assertEquals("Least Concern", properties.getAnimal().getConservationStatus());
        assertNotNull(properties.getAnimal().getAssociateFile());
        assertThat((ClassPathResource) properties.getAnimal().getAssociateFile(), isA(ClassPathResource.class));
        assertEquals("kb/cougar.zip", ((ClassPathResource) properties.getAnimal().getAssociateFile()).getPath());
    }

    @Test
    public void shouldPropertyPathReturnExpectedValue() {
        final Species properties = binder.createAndBindConfiguration(Species.class);
        final List<PropertyPath> paths = binder.resolvePath(properties);
        assertEquals(4, paths.size());

        for (final PropertyPath propertyPath : paths) {
            final String path = propertyPath.getPath();
            switch (path) {
                case "species.name":
                    assertEquals("P. concolor", propertyPath.getPropertyValue());
                    break;
                case "species.animal.name":
                    assertEquals("Cougar", propertyPath.getPropertyValue());
                    break;
                case "species.animal.conservationStatus":
                    assertEquals("Least Concern", propertyPath.getPropertyValue());
                    break;
                case "species.animal.associateFile":
                    assertThat((ClassPathResource) propertyPath.getPropertyValue(), isA(ClassPathResource.class));
                    break;
                default:
                    fail();
                    break;
            }
        }
    }

    @Test
    public void shouldReturnOnlyPeristentPropertyPath() {
        final BeanPersistent bean = new BeanPersistent();
        final List<PropertyPath> paths = binder.resolvePath(bean);
        assertEquals(1, paths.size());

        final PropertyPath path = Iterables.getFirst(paths, null);
        assertEquals("bean.name", path.getPath());
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

    @ConfigurationProperties("bean")
    public static class BeanPersistent {

        private String name;

        @NoPersistent
        private String location;

        private String namespace;

        private String descrption;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(final String location) {
            this.location = location;
        }

        @NoPersistent
        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(final String namespace) {
            this.namespace = namespace;
        }

        public String getDescrption() {
            return descrption;
        }

        @NoPersistent
        public void setDescrption(final String descrption) {
            this.descrption = descrption;
        }
    }

}
