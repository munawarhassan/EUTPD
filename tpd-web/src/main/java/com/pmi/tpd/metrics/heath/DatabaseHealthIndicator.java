package com.pmi.tpd.metrics.heath;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

/**
 * Actuator HealthIndicator check for the Database.
 */
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    private static Map<String, String> queries = Maps.newHashMap();

    static {
        queries.put("HSQL Database Engine", "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        queries.put("Oracle", "SELECT 'Hello' from DUAL");
        queries.put("Apache Derby", "SELECT 1 FROM SYSIBM.SYSDUMMY1");
        queries.put("MySQL", "SELECT 1");
        queries.put("PostgreSQL", "SELECT 1");
        queries.put("Microsoft SQL Server", "SELECT 1");
    }

    private static String DEFAULT_QUERY = "SELECT 1";

    private String query = null;

    public DatabaseHealthIndicator(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        final String product = getProduct();
        builder.up().withDetail("type", "database").withDetail("product", product);
        final String query = detectQuery(product);
        if (StringUtils.hasText(query)) {
            try {
                builder.withDetail("hello", this.jdbcTemplate.queryForObject(query, String.class));
            } catch (final Exception ex) {
                builder.down(ex);
            }
        }
    }

    private String getProduct() {
        return this.jdbcTemplate.execute(new ConnectionCallback<String>() {

            @Override
            public String doInConnection(final Connection connection) throws SQLException, DataAccessException {

                return connection.getMetaData().getDatabaseProductName();
            }
        });
    }

    protected String detectQuery(final String product) {
        String query = this.query;
        if (!StringUtils.hasText(query)) {
            query = queries.get(product);
        }
        if (!StringUtils.hasText(query)) {
            query = DEFAULT_QUERY;
        }
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }
}
