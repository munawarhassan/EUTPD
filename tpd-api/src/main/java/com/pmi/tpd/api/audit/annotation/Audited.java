package com.pmi.tpd.api.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.publisher.IEventPublisher;

/**
 * Indicates that the annotated type is an event which should be audited, added to the audit log and potentially other
 * channels
 * <p>
 * Only affects types used as arguments in {@link IEventPublisher#publish(Object)}
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Audited {

    /**
     * @return a converter to extract the details of the event to be recorded
     */
    Class<? extends AuditEntryConverter<?>> converter();

    /**
     * @return additional channels the audit details should be sent to in addition to the audit log.
     */
    String[] channels() default {};

    /**
     * @return the priority of the event
     */
    Priority priority();
}