package com.pmi.tpd.core.context;

import java.net.URI;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.ApplicationConstants.PropertyKeys;
import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.event.publisher.IEventPublisher;

/**
 * A class to manage the interface with a single property set, used for application properties.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class DefaultApplicationProperties extends BaseApplicationProperties {

    @Inject
    public DefaultApplicationProperties(@Nonnull final IEventPublisher publisher,
            @Nonnull final Provider<IPropertiesManager> propertiesManager, @Nonnull final Environment environment,
            final BeanFactory beanFactory) {
        super(publisher, propertiesManager, environment, beanFactory);
    }

    @Override
    public void setSetup(final boolean setup) {
        setString(PropertyKeys.SETUP_PROPERTY, Boolean.toString(setup));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAutoSetup() {
        return getDefaultBackedString(PropertyKeys.AUTO_SETUP_PROPERTY).map((value) -> "true".equals(value))
                .orElse(false);
    }

    @Override
    public boolean isSetup() {
        return getString(PropertyKeys.SETUP_PROPERTY).map((value) -> "true".equals(value)).orElse(false);
    }

    @Override
    public boolean isBootstrapped() {
        return isSetup() || isAutoSetup();
    }

    @Override
    public Optional<URI> getBaseUrl() {
        return getString(ApplicationConstants.Setup.SETUP_BASE_URL).map((uri) -> URI.create(uri));
    }

    @Override
    public void setBaseURL(final URI uri) {
        setString(ApplicationConstants.Setup.SETUP_BASE_URL, uri.toString());
    }

    @Override
    public void setAvatarSource(final String avatarSource) {
        setString(ApplicationConstants.PropertyKeys.AVATAR_SOURCE, avatarSource);
    }

    @Override
    public Optional<String> getAvatarSource() {
        return getString(ApplicationConstants.PropertyKeys.AVATAR_SOURCE);
    }

    @Override
    public Optional<String> getDisplayName() {
        return getString(ApplicationConstants.Setup.SETUP_DISPLAY_NAME);
    }

    @Override
    public void setDisplayName(final String displayName) {
        setString(ApplicationConstants.Setup.SETUP_DISPLAY_NAME, displayName);
    }

}
