package com.pmi.tpd.core.bootstrap;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.sql.DataSource;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.concurrent.IClusterLockService;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultLockService implements IBootstrapLockService {

    /** */
    private final IClusterLockService clusterLockService;

    /** */
    private final DataSource dataSource;

    /** */
    private final I18nService i18nService;

    /**
     * @param clusterLockService
     * @param dataSource
     * @param i18nService
     */
    @Inject
    public DefaultLockService(final IClusterLockService clusterLockService, final DataSource dataSource,
            final I18nService i18nService) {
        this.clusterLockService = clusterLockService;
        this.dataSource = dataSource;
        this.i18nService = i18nService;
    }

    @Nonnull
    @Override
    public Lock getLock(@Nonnull final String lockName) {
        return clusterLockService.getLockForName(checkNotNull(lockName, "lockName"));
    }

    @Nonnull
    @Override
    public IBootstrapLock getBootstrapLock() {
        return new LiquibaseBootstrapLock(dataSource, i18nService);
    }
}
