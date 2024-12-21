package com.pmi.tpd.api.event.publisher;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.lifecycle.IShutdown;

/**
 * Interface to publish events. It allows the decoupling of listeners which
 * handle events and publishers which dispatch
 * events.
 *
 * @see EventListener annotation which can be used to indicate event listener
 *      methods
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IEventPublisher extends IShutdown {

  /**
   * Publish an event that will be consumed by all listeners which have registered
   * to receive it. Implementations must
   * dispatch events to listeners which have a public method annotated with
   * {@link com.pmi.tpd.api.event.annotation.EventListener} and one argument which
   * is assignable from the event type
   * (i.e. a superclass or interface implemented by the event object).
   *
   * @param event
   *              the event to publish (can not be {@code null}).
   * @throws java.lang.IllegalArgumentException
   *                                            if the {@code event} is
   *                                            {@code null}
   */
  void publish(@Nonnull final Object event);

  /**
   * Register a listener to receive events. All implementations must support
   * registration of listeners where event
   * handling methods are indicated by the
   * {@link com.pmi.tpd.api.event.annotation.EventListener} annotation.
   *
   * @param listener
   *                 The listener that is being registered (can not be
   *                 {@code null})
   * @throws java.lang.IllegalArgumentException
   *                                            if the {@code listener} is
   *                                            {@code null}
   * @see EventListener annotation which can be used to indicate event listener
   *      methods
   */
  void register(@Nonnull final Object listener);

  /**
   * Un-register a listener so that it will no longer receive events. If the given
   * listener is not registered nothing
   * will happen.
   *
   * @param listener
   *                 The listener to un-register (can not be {@code null})
   * @throws java.lang.IllegalArgumentException
   *                                            if the {@code listener} is
   *                                            {@code null}
   */
  void unregister(@Nonnull final Object listener);

  /**
   * Un-register all listeners that this publisher knows about.
   */
  void unregisterAll();
}
