package com.pmi.tpd.database.jpa;

import javax.sql.DataSource;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class ConfigurableLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    /** */
    private final IDataSourceConfiguration dataSourceConfiguration;

    /**
     * @param dataSource
     * @param dataSourceConfiguration
     */
    public ConfigurableLocalContainerEntityManagerFactoryBean(final DataSource dataSource,
            final IDataSourceConfiguration dataSourceConfiguration) {
        this.setDataSource(dataSource);
        this.dataSourceConfiguration = dataSourceConfiguration;
    }

    /**
     * @return
     */
    public IDataSourceConfiguration getDataSourceConfiguration() {
        return dataSourceConfiguration;
    }
}
