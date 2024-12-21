package com.pmi.tpd.core;

import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.annotation.IntegrationTest;
import com.pmi.tpd.database.DatabaseConstants;
import com.pmi.tpd.database.config.DefaultDataSourceConfiguration;

import liquibase.integration.spring.SpringLiquibase;

/**
 * <p>
 * MemoryDatabaseConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@IntegrationTest
@Configuration
public class MemoryDatabaseConfig implements EnvironmentAware {

    /** Use only production database and specific data for test. */
    private static final String LIQUIBASE_CONTEXTS = "production,integration-test";

    /** */
    private Environment environment;

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    /**
     * <p>
     * dataSource.
     * </p>
     *
     * @return a {@link javax.sql.DataSource} object.
     * @throws java.lang.Exception
     *             if any.
     */
    @Bean(name = "dataSource")
    public DataSource dataSource() throws Exception {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.DERBY).build();
    }

    /**
     * Produces new {@link liquibase.integration.spring.SpringLiquibase} to be managed by the Spring container.
     *
     * @param dataSource
     *            The DataSource that liquibase will use to perform the migration.
     * @return Returns {@link liquibase.integration.spring.SpringLiquibase} to be managed by the Spring container.
     */
    @Bean
    public SpringLiquibase liquibase(@Named("dataSource") final DataSource dataSource) {
        final SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(ApplicationConstants.Liquibase.CHANGE_LOG_LOCATION);
        liquibase.setContexts(LIQUIBASE_CONTEXTS);
        return liquibase;
    }

    @Bean
    public DefaultDataSourceConfiguration dataSourceConfiguration() {
        // use full property name, i.e already prefixed with 'database.'
        final String driverClassName = environment.getProperty(DatabaseConstants.PROP_JDBC_DRIVER);
        final String user = environment.getProperty(DatabaseConstants.PROP_JDBC_USER);
        final String password = environment.getProperty(DatabaseConstants.PROP_JDBC_PASSWORD);
        final String url = environment.getProperty(DatabaseConstants.PROP_JDBC_URL);
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration(driverClassName, user,
                password, url);
        return configuration;
    }

}
