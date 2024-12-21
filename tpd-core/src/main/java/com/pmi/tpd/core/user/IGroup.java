package com.pmi.tpd.core.user;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.model.AbstractEntityBuilder;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * IGroup interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IGroup extends IIdentityEntity<Long> {

    /**
     * <p>
     * getName.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

    /**
     * <p>
     * getDescription.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getDescription();

    /**
     * @author Christophe Friederich
     * @param <B>
     */
    abstract class GroupBuilder<B extends AbstractEntityBuilder<Long, IGroup, B>>
            extends AbstractEntityBuilder<Long, IGroup, B> {

        /** */
        protected String name;

        /** */
        protected String description;

        // TODO to remove immutability
        public GroupBuilder() {

        }

        public GroupBuilder(final IGroup group) {
            super(group);
            this.name = Assert.checkHasText(group.getName(), "group.name");
            this.description = group.getDescription();
        }

        public B name(@Nonnull final String name) {
            this.name = Assert.checkHasText(name, "name");
            return self();
        }

        @Nonnull
        public B description(@Nullable final String description) {
            this.description = description;
            return self();
        }

    }

}
