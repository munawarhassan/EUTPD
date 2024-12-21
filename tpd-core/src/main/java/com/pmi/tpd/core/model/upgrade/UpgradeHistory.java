package com.pmi.tpd.core.model.upgrade;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.springframework.util.ObjectUtils;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.model.AbstractEntityBuilder;
import com.pmi.tpd.api.model.IIdentityEntity;

/**
 * <p>
 * UpgradeHistory class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "UpgradeHistory")
@Table(name = UpgradeHistory.TABLE_NAME)
public class UpgradeHistory implements IIdentityEntity<Long> {

    /** generator identifier. */
    private static final String ID_GEN = "upgradeHistoryIdGenerator";

    /** table name. */
    public static final String TABLE_NAME = "t_upgrade_history";

    /** Generated user id. */
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME, //
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME, //
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME, pkColumnValue = "id",
            allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
    private Long id;

    /** */
    @Column(name = "build_number", length = 50)
    private String buildNumber;

    /** */
    @Column(name = "target_build_number", length = 50)
    private String targetBuildNumber;

    /** */
    @Column(name = "upgrade_class", length = 1024)
    private String upgradeClass;

    /**
     * <p>
     * Constructor for UpgradeHistory.
     * </p>
     */
    public UpgradeHistory() {
    }

    /**
     * <p>
     * Constructor for UpgradeHistory.
     * </p>
     *
     * @param builder
     *            a {@link com.pmi.tpd.core.model.upgrade.UpgradeHistory.Builder} object.
     */
    public UpgradeHistory(final Builder builder) {
        buildNumber = builder.buildNumber;
        targetBuildNumber = builder.targetBuildNumber;
        upgradeClass = builder.upgradeClass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UpgradeHistory) {
            final UpgradeHistory that = (UpgradeHistory) o;
            return ObjectUtils.nullSafeEquals(getId(), that.getId());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(getId());
    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * <p>
     * Getter for the field <code>buildNumber</code>.
     * </p>
     *
     * @return the buildNumber
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * <p>
     * Getter for the field <code>targetBuildNumber</code>.
     * </p>
     *
     * @return the targetBuildNumber
     */
    public String getTargetBuildNumber() {
        return targetBuildNumber;
    }

    /**
     * <p>
     * Getter for the field <code>upgradeClass</code>.
     * </p>
     *
     * @return the upgradeClass
     */
    public String getUpgradeClass() {
        return upgradeClass;
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.model.upgrade.UpgradeHistory.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * UpgradeHistory builder.
     *
     * @author Christophe Friederich
     */
    public static class Builder extends AbstractEntityBuilder<Long, UpgradeHistory, Builder> {

        /** */
        private String buildNumber;

        /** */
        private String targetBuildNumber;

        /** */
        private String upgradeClass;

        /**
         *
         */
        public Builder() {
        }

        /**
         *
         */
        public Builder(@Nonnull final UpgradeHistory item) {
            super(item);
            this.buildNumber = item.buildNumber;
            this.targetBuildNumber = item.targetBuildNumber;
            this.upgradeClass = item.upgradeClass;
        }

        /**
         * @param buildNumber
         * @return
         */
        public Builder buildNumber(final String buildNumber) {
            this.buildNumber = buildNumber;
            return self();
        }

        /**
         * @param targetBuildNumber
         * @return
         */
        public Builder targetBuildNumber(final String targetBuildNumber) {
            this.targetBuildNumber = targetBuildNumber;
            return self();
        }

        /**
         * @param upgradeClass
         * @return
         */
        public Builder upgradeClass(final String upgradeClass) {
            this.upgradeClass = upgradeClass;
            return self();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UpgradeHistory build() {
            return new UpgradeHistory(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Builder self() {
            return this;
        }

    }

}
