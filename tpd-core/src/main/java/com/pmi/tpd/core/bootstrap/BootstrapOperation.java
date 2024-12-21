package com.pmi.tpd.core.bootstrap;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.util.IUncheckedOperation;

/**
 * Loads the application properties for the automated setup process and updates the database accordingly.
 * <p>
 * Note the linear, fail fast behavior of the setup process. This makes it relatively easy to re-run the process and try
 * everything again without worrying about potentially complex inter-dependencies.
 * </p>
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class BootstrapOperation implements IUncheckedOperation<Void> {

    /**
     * @param propertiesService
     * @param userStore
     * @param applicationProperties
     * @param configurationService
     */
    @Inject
    public BootstrapOperation() {
    }

    /**
     * A template method for both {@link BootstrapOperation} and {@link DevBootstrapOperation} that defines the broad
     * progression and flow of setup common to both.
     *
     * @see Bootstrapper#start()
     */
    @Override
    @Transactional
    public Void perform() {

        return null;
    }

}
