package com.pmi.tpd.core.context;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.exception.InfrastructureException;
import com.pmi.tpd.spring.context.PropertySetPropertySource;

import io.atlassian.util.concurrent.ResettableLazyReference;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
public class DefaultPropertiesManager implements IPropertiesManager {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertiesManager.class);

    /** */
    private static final String SEQUENCE = "app.properties";

    /** */
    private static final Long ID = 1L;

    /** */
    private final ResettableLazyReference<IPropertyAccessor> propertySetRef;

    @Inject
    public DefaultPropertiesManager(@Nonnull final IPropertySetFactory propertySetFactory,
            final Environment environment) {
        this(new ResettableLazyReference<>() {

            @Override
            protected IPropertyAccessor create() throws Exception {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Application Properties Configuration is created...");
                }
                return propertySetFactory.buildCaching(propertySetFactory.buildPersistent(SEQUENCE, ID), Boolean.TRUE);
            }
        });
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Environment database properties Configuration is initializing...");
        }
        final MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();

        final PropertySetPropertySource propertySource = new PropertySetPropertySource("propertySet",
                () -> DefaultPropertiesManager.this);
        propertySources.addFirst(propertySource);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Environment database properties Configuration is initialized...");
        }
    }

    /**
     * @param propertySetRef
     */
    public DefaultPropertiesManager(final ResettableLazyReference<IPropertyAccessor> propertySetRef) {
        this.propertySetRef = propertySetRef;
    }

    /**
     * @return Returns the property set.
     * @throws InfrastructureException
     *             error if the {@link PropertySet} used can not retrieves value from persistent layer.
     */
    @Override
    public Optional<IPropertyAccessor> getPropertyAccessor() {
        try {
            return Optional.ofNullable(propertySetRef.get());
        } catch (final Throwable ex) {
            this.propertySetRef.reset();
            return Optional.empty();
        }
    }

    @Override
    public void flush() {
        getPropertyAccessor().ifPresent((prop) -> prop.flush());
    }

    /**
     * Refresh the properties from the database.
     */
    @Override
    public void refresh() {
        propertySetRef.reset();
    }

}
