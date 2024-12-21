package com.pmi.tpd.spring.context;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.env.EnumerablePropertySource;

import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.context.IPropertyAccessor;

/**
 * @author christophe friederich
 * @since 2.2
 */
public class PropertySetPropertySource extends EnumerablePropertySource<Provider<IPropertiesManager>> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertySetPropertySource.class);

    @Inject
    public PropertySetPropertySource(final String name, final Provider<IPropertiesManager> propertiesManager) {
        super(name, propertiesManager);
    }

    @Override
    public Object getProperty(final String name) {
        final Optional<IPropertyAccessor> accessor = getPropertyAccessor();
        if (accessor.isEmpty()) {
            LOGGER.info("Unable to get {} from PropertiesManager!", name);
        }

        return accessor.filter(ac -> ac.exists(name)).map(ac -> ac.getAsActualType(name)).orElse(null);
    }

    @Override
    public String[] getPropertyNames() {
        final Optional<IPropertyAccessor> accessor = getPropertyAccessor();
        if (accessor.isEmpty()) {
            LOGGER.info("Unable to get {} from PropertiesManager!", name);
        }

        final List<String> names = accessor.map(IPropertyAccessor::getKeys).orElse(Collections.emptyList());
        return names.toArray(new String[names.size()]);
    }

    private Optional<IPropertyAccessor> getPropertyAccessor() {
        try {
            return this.getSource().get().getPropertyAccessor();
        } catch (final BeanCreationException ex) {
            return Optional.empty();
        }
    }

}
