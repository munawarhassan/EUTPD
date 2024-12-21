package com.pmi.tpd.core.startup;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * <p>
 * CompositeHomePathLocator class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class CompositeHomePathLocator implements IHomePathLocator {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeHomePathLocator.class);

    /** */
    private final List<IHomePathLocator> locators;

    /**
     * <p>
     * Constructor for CompositeHomePathLocator.
     * </p>
     *
     * @param locators
     *            a {@link com.pmi.tpd.core.startup.IHomePathLocator} object.
     */
    public CompositeHomePathLocator(final IHomePathLocator... locators) {
        this.locators = ImmutableList.copyOf(locators);
    }

    /** {@inheritDoc} */
    @Override
    public String getHome() {
        for (final IHomePathLocator homePathLocator : locators) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Looking for home path using " + homePathLocator);
            }
            final String home = homePathLocator.getHome();
            if (StringUtils.isNotBlank(home)) {
                LOGGER.info("home path '" + home + "' found using " + homePathLocator.getDisplayName() + '.');
                return home.trim();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return "Composite";
    }
}
