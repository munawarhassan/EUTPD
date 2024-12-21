package com.pmi.tpd.core.event.advisor;

import java.util.Map;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.event.IEventCheck;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;

/**
 * When implementing Event interfaces, this secondary interface can also be implemented to indicate the object needs
 * additional initialisation.
 * <p/>
 * This interface can be applied to implementations of:
 * <ul>
 * <li>{@link IContainerFactory ContainerFactory}</li>
 * <li>{@link IEventCheck EventCheck} (and its derived interfaces)</li>
 * <li>{@link ISetupConfig SetupConfig}</li>
 * </ul>
 * This interface is applied by the {@link IEventConfig} implementation which is being used. For exact details on its
 * semantics, review the documentation for that class as well.
 */
public interface IConfigurable {

    /**
     * Initialise the object, optionally drawing configuration from the provided {@code Map}. The provided map may be
     * empty, if no parameters were configured, but it will never be {@code null}.
     *
     * @param params
     *            a map of additional parameters loaded from the configuration file
     */
    void init(@Nonnull Map<String, String> params);
}
