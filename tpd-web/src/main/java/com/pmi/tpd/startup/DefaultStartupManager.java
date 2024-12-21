package com.pmi.tpd.startup;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleUtils;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultStartupManager implements IStartupManager {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStartupManager.class);

    /** */
    private final ServletContext servletContext;

    /** */
    private volatile IProgress progress;

    /**
     * @param servletContext
     */
    public DefaultStartupManager(final ServletContext servletContext) {
        this.servletContext = servletContext;

        progress = new ProgressImpl("Starting " + Product.getFullName(), 0);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return progress;
    }

    @Override
    public boolean isStarting() {
        return LifecycleUtils.isStarting(servletContext);
    }

    @Override
    public void onProgress(@Nonnull final IProgress progress) {
        final IProgress oldProgress = this.progress;
        if (progress.getPercentage() < oldProgress.getPercentage()) {
            LOGGER.debug("Progress going backwards: {} {}% -> {} {}%!",
                oldProgress.getMessage(),
                oldProgress.getPercentage(),
                progress.getMessage(),
                progress.getPercentage());
        }
        this.progress = progress;
    }
}
