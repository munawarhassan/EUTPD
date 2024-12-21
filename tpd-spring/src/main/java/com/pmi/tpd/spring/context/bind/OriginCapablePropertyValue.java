package com.pmi.tpd.spring.context.bind;

import org.springframework.beans.PropertyValue;
import org.springframework.core.env.PropertySource;

/**
 * A {@link PropertyValue} that can provide information about its origin.
 *
 * @author Andy Wilkinson
 */
class OriginCapablePropertyValue extends PropertyValue {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private static final String ATTRIBUTE_PROPERTY_ORIGIN = "propertyOrigin";

    /** */
    private final PropertyOrigin origin;

    OriginCapablePropertyValue(final PropertyValue propertyValue) {
        this(propertyValue.getName(), propertyValue.getValue(),
                (PropertyOrigin) propertyValue.getAttribute(ATTRIBUTE_PROPERTY_ORIGIN));
    }

    OriginCapablePropertyValue(final String name, final Object value, final String originName,
            final PropertySource<?> originSource) {
        this(name, value, new PropertyOrigin(originSource, originName));
    }

    OriginCapablePropertyValue(final String name, final Object value, final PropertyOrigin origin) {
        super(name, value);
        this.origin = origin;
        setAttribute(ATTRIBUTE_PROPERTY_ORIGIN, origin);
    }

    public PropertyOrigin getOrigin() {
        return this.origin;
    }

    @Override
    public String toString() {
        final String name = this.origin != null ? this.origin.getName() : this.getName();
        final String source = this.origin != null && this.origin.getSource() != null ? this.origin.getSource().getName()
                : "unknown";
        return "'" + name + "' from '" + source + "'";
    }

    public static PropertyOrigin getOrigin(final PropertyValue propertyValue) {
        if (propertyValue instanceof OriginCapablePropertyValue) {
            return ((OriginCapablePropertyValue) propertyValue).getOrigin();
        }
        return new OriginCapablePropertyValue(propertyValue).getOrigin();
    }

}
