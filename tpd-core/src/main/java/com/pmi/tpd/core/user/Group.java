package com.pmi.tpd.core.user;

/**
 * <p>
 * Group class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class Group implements IGroup {

    /** */
    private final Long id;

    /** */
    private final String name;

    /** */
    private final String description;

    /**
     * <p>
     * Constructor for Group.
     * </p>
     *
     * @param builder
     *            a {@link com.pmi.tpd.core.user.Group.Builder} object.
     */
    public Group(final Builder builder) {
        this.id = builder.id();
        name = builder.name;
        description = builder.description;
    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {

        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @author devacfr<christophefriederich@mac.com>
     * @since 1.0
     */
    public static class Builder extends GroupBuilder<Builder> {

        @Override
        public IGroup build() {
            return new Group(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
