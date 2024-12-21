package com.pmi.tpd.core.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.versioning.BuildUtils;

/**
 * <p>
 * Abstract AbstractUpgradeTask class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class AbstractUpgradeTask implements IUpgradeTask {

    /** */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /** */
    private final List<String> errors = new ArrayList<>();

    /** */
    private final IApplicationProperties applicationProperties;

    /** */
    private final I18nService i18nService;

    /** {@inheritDoc} */
    @Override
    public final String getVersion() {
        return BuildUtils.extractTargetVersionFromUpgradeClass(this.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final String getBuildNumber() {
        return BuildUtils.extractBuildNumberFromUpgradeClass(this.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public abstract void doUpgrade() throws Exception;

    /** {@inheritDoc} */
    @Override
    public String getShortDescription() {
        return "Upgrade to build number: " + getBuildNumber();
    }

    /**
     * <p>
     * Constructor for AbstractUpgradeTask.
     * </p>
     *
     * @param applicationProperties
     *            a {@link com.pmi.tpd.api.context.IApplicationProperties} object.
     * @param i18nService
     *            a {@link I18nService} object.
     */
    public AbstractUpgradeTask(final IApplicationProperties applicationProperties, final I18nService i18nService) {
        this.applicationProperties = applicationProperties;
        this.i18nService = i18nService;
        checkBuildNumber();
    }

    /**
     * <p>
     * checkBuildNumber.
     * </p>
     */
    protected void checkBuildNumber() {
        Assert.hasText(getBuildNumber(), "builder number is required");
    }

    /**
     * <p>
     * Getter for the field <code>applicationProperties</code>.
     * </p>
     *
     * @return a {@link com.pmi.tpd.api.context.IApplicationProperties} object.
     */
    protected IApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    /**
     * <p>
     * addError.
     * </p>
     *
     * @param error
     *            a {@link java.lang.String} object.
     */
    protected void addError(final String error) {
        errors.add(error);
    }

    /**
     * Useful for adding a bunch of errors (like from a command) with a prefix.
     *
     * @param prefix
     *            a {@link java.lang.String} object.
     * @param errors
     *            a {@link java.util.Collection} object.
     */
    public void addErrors(final String prefix, final Collection<String> errors) {
        for (final String errorMessage : errors) {
            errors.add(prefix + errorMessage);
        }
    }

    /**
     * <p>
     * addErrors.
     * </p>
     *
     * @param errors
     *            a {@link java.util.Collection} object.
     */
    public void addErrors(final Collection<String> errors) {
        addErrors("", errors);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getErrors() {
        return errors;
    }

    /**
     * <p>
     * getI18nBean.
     * </p>
     *
     * @return a {@link I18nService} object.
     */
    protected I18nService getI18nService() {
        return i18nService;
    }
}
