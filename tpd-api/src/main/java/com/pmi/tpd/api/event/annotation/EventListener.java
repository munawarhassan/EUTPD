package com.pmi.tpd.api.event.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to annotate event listener methods. Methods should be public and take one parameter which is the event to be
 * handled.
 * <p/>
 * For example, the following class implements a simple event listener:
 *
 * <pre>
 * <tt>      public class TestListener {
 *        &#64;EventListener
 *        public void onEvent(SampleEvent event) {
 *            System.out.println("Handled an event: " + event);
 *        }
 *    }
 * </tt>
 * </pre>
 *
 * @see com.pmi.tpd.core.event.spi.AnnotatedMethodsListenerHandler AnnotatedMethodsListenerHandler
 * @author Christophe Friederich
 * @since 1.0
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface EventListener {
}
