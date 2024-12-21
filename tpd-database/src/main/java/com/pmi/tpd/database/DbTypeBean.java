package com.pmi.tpd.database;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Functions;

/**
 * An adapter (wrapper) for {@link DbType} instances.
 * <p>
 * It has a bunch of methods, some of which are overridden by particular elements of the enum. The upshot of this is
 * that we need to wrap any instance of {@code DbType} that we want to use in a Soy template.
 * <p>
 * You should be careful about deleting, or changing the names of, the methods of this class; they may not be used by
 * Java code, but they are used within Soy templates
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DbTypeBean {

    /**
     * A function that produces a DbTypeBean wrapped around a {@link DbType}.
     */
    private static final Function<DbType, DbTypeBean> FROM_DB_TYPE = DbTypeBean::new;

    /**
     * The DbTypeBean that should be set as the default on forms that let the use choose a database type.
     */
    public static final DbTypeBean DEFAULT = forDbType(DbType.POSTGRES);

    /**
     * A list containing one DbTypeBean instance for each of the {@link DbType DB types}.
     */
    public static final List<DbTypeBean> ALL = DbType.AS_LIST.stream()
            .map(FROM_DB_TYPE)
            .collect(Collectors.toUnmodifiableList());

    /** */
    private final DbType adaptee;

    private DbTypeBean(final DbType adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * Wraps the given DbType in a DbTypeBean.
     */
    @Nonnull
    public static DbTypeBean forDbType(@Nonnull final DbType dbType) {
        return FROM_DB_TYPE.apply(dbType);
    }

    /**
     * Returns a DbTypeBean that wraps the DbType corresponding to the given key.
     *
     * @return a View DB Type, or null if there is no DB type for the given key
     */
    @Nullable
    public static DbTypeBean forKey(@Nonnull final String key) {
        return DbType.forKey(key).map(FROM_DB_TYPE).orElse(null);
    }

    @Nonnull
    public String generateUrl(final String hostName, final String databaseNameOrSid, final int port) {
        return adaptee.generateUrl(hostName, databaseNameOrSid, port);
    }

    @Nonnull
    public String getDefaultDatabaseName() {
        return adaptee.getDefaultDatabaseName().orElse("");
    }

    @Nonnull
    public String getDefaultHostName() {
        return adaptee.getDefaultHostName().orElse("");
    }

    @Nonnull
    public String getDefaultPort() {
        return adaptee.getDefaultPort().map(Functions.toStringFunction()).orElse("");
    }

    @Nonnull
    public String getDefaultUserName() {
        return adaptee.getDefaultUserName().orElse("");
    }

    @Nonnull
    public String getDisplayName() {
        return adaptee.getDisplayName();
    }

    @Nonnull
    public String getDriverClassName() {
        return adaptee.getDriverClassName();
    }

    @Nonnull
    public String getKey() {
        return adaptee.getKey();
    }

    @Nonnull
    public String getHelpKey() {
        return adaptee.getHelpKey();
    }

    @Nonnull
    public String getProtocol() {
        return adaptee.getProtocol();
    }

    public boolean getUsesSid() {
        return adaptee.usesSid();
    }

    public boolean isClusterable() {
        return adaptee.isClusterable();
    }

    public boolean isDriverAvailable() {
        return adaptee.isDriverAvailable();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DbTypeBean)) {
            return false;
        }
        final DbTypeBean other = (DbTypeBean) o;
        return adaptee.equals(other.adaptee);
    }

    @Override
    public int hashCode() {
        return adaptee.hashCode();
    }

    @Override
    public String toString() {
        return adaptee.toString();
    }
}
