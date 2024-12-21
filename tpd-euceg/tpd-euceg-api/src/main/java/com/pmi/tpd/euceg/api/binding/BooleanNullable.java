package com.pmi.tpd.euceg.api.binding;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.jvnet.jaxb2_commons.lang.CopyStrategy2;
import org.jvnet.jaxb2_commons.lang.CopyTo2;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.HashCode2;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BooleanNullable", propOrder = { "value" })
public class BooleanNullable implements Serializable, Cloneable, CopyTo2, Equals2, HashCode2 {

    private final static long serialVersionUID = 1L;

    @XmlValue
    protected Boolean value;

    @XmlAttribute(name = "confidential", required = true)
    protected boolean confidential;

    /**
     * Obtient la valeur de la propriété value.
     */
    public Boolean isValue() {
        return value;
    }

    /**
     * Définit la valeur de la propriété value.
     */
    public void setValue(final Boolean value) {
        this.value = value;
    }

    /**
     * Obtient la valeur de la propriété confidential.
     */
    public boolean isConfidential() {
        return confidential;
    }

    /**
     * Définit la valeur de la propriété confidential.
     */
    public void setConfidential(final boolean value) {
        this.confidential = value;
    }

    public BooleanNullable withValue(final Boolean value) {
        setValue(value);
        return this;
    }

    public BooleanNullable withConfidential(final boolean value) {
        setConfidential(value);
        return this;
    }

    @Override
    public Object clone() {
        return copyTo(createNewInstance());
    }

    @Override
    public Object copyTo(final Object target) {
        final CopyStrategy2 strategy = JAXBCopyStrategy.INSTANCE;
        return copyTo(null, target, strategy);
    }

    @Override
    public Object copyTo(final ObjectLocator locator, final Object target, final CopyStrategy2 strategy) {
        final Object draftCopy = target == null ? createNewInstance() : target;
        if (draftCopy instanceof BooleanNullable) {
            final BooleanNullable copy = (BooleanNullable) draftCopy;
            {
                final Boolean valueShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, true);
                if (valueShouldBeCopiedAndSet == Boolean.TRUE) {
                    Boolean sourceValue;
                    sourceValue = this.isValue();
                    final Boolean copyValue = (Boolean) strategy
                            .copy(LocatorUtils.property(locator, "value", sourceValue), sourceValue, true);
                    copy.setValue(copyValue);
                }
            }
            {
                final Boolean confidentialShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, true);
                if (confidentialShouldBeCopiedAndSet == java.lang.Boolean.TRUE) {
                    boolean sourceConfidential;
                    sourceConfidential = this.isConfidential();
                    final boolean copyConfidential = strategy.copy(LocatorUtils
                            .property(locator, "confidential", sourceConfidential),
                        sourceConfidential,
                        true);
                    copy.setConfidential(copyConfidential);
                }
            }
        }
        return draftCopy;
    }

    @Override
    public Object createNewInstance() {
        return new BooleanNullable();
    }

    @Override
    public boolean equals(final ObjectLocator thisLocator,
        final ObjectLocator thatLocator,
        final Object object,
        final EqualsStrategy2 strategy) {
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final BooleanNullable that = (BooleanNullable) object;
        {
            Boolean lhsValue;
            lhsValue = this.isValue();
            Boolean rhsValue;
            rhsValue = that.isValue();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "value", lhsValue),
                LocatorUtils.property(thatLocator, "value", rhsValue),
                lhsValue,
                rhsValue,
                true,
                true)) {
                return false;
            }
        }
        {
            boolean lhsConfidential;
            lhsConfidential = this.isConfidential();
            boolean rhsConfidential;
            rhsConfidential = that.isConfidential();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "confidential", lhsConfidential),
                LocatorUtils.property(thatLocator, "confidential", rhsConfidential),
                lhsConfidential,
                rhsConfidential,
                true,
                true)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    @Override
    public int hashCode(final ObjectLocator locator, final HashCodeStrategy2 strategy) {
        int currentHashCode = 1;
        {
            boolean theValue;
            theValue = this.isValue();
            currentHashCode = strategy
                    .hashCode(LocatorUtils.property(locator, "value", theValue), currentHashCode, theValue, true);
        }
        {
            boolean theConfidential;
            theConfidential = this.isConfidential();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "confidential", theConfidential),
                currentHashCode,
                theConfidential,
                true);
        }
        return currentHashCode;
    }

    @Override
    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}