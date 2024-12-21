package com.pmi.tpd.core.startup;

/**
 * Represents a generic failed StartupCheck.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public class StartupCheckImpl implements IStartupCheck {

    /** */
    private final String name;

    /** */
    private final String faultDescription;

    /** */
    private final int order;

    /**
     * <p>
     * Constructor for StartupCheckImpl.
     * </p>
     *
     * @param name
     *            a {@link java.lang.String} object.
     * @param faultDescription
     *            a {@link java.lang.String} object.
     * @param order
     *            a int.
     */
    public StartupCheckImpl(final String name, final String faultDescription, final int order) {
        this.name = name;
        this.faultDescription = faultDescription;
        this.order = order;
    }

    /** {@inheritDoc} */
    @Override
    public int getOrder() {
        return order;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOk() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getFaultDescription() {
        return faultDescription;
    }

    /** {@inheritDoc} */
    @Override
    public String getHtmlFaultDescription() {
        return faultDescription;
    }
}
