package com.pmi.tpd.core.event.publisher;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.Aware;

import com.pmi.tpd.api.event.publisher.IEventPublisher;

/**
 * Interface to be implemented by beans that wish to be aware of their owning {@link IEventPublisher}.
 * <p>
 * For example, beans can publish a event.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IEventPublisherAware extends Aware {

    /**
     * Callback that supplies the owning event publisher to a bean instance.
     * <p>
     * Invoked after the population of normal bean properties but before an initialisation callback such as
     * {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()},
     * {@link javax.annotation.PostConstruct PostConstruct} annotation or a custom init-method.
     *
     * @param eventPublisher
     *            owning BeanFactory (never <code>null</code>).
     * @throws java.lang.IllegalArgumentException
     *             if the eventPublisher is {@code null}
     */
    void setEventPublisher(@Nonnull final IEventPublisher eventPublisher);
}
