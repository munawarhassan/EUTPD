package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Maps;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;
import com.pmi.tpd.security.random.DefaultSecureTokenGenerator;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Updates any t_user rows where the name is not an appropriate slug, ensuring a valid and unique slug (computed from
 * the name) is set. If finding a free slug proves too exhausting (10 attempts are made) then the entire changeset will
 * fail and rollback will occur through the normal Liquibase mechanism. This changeset assumes that slug column for each
 * t_user row is already set to the same value as the login column.
 *
 * @since 2.4
 */
public class SetUserSlugChange implements CustomTaskChange, CustomTaskRollback {

    private static final ISecureTokenGenerator GENERATOR = DefaultSecureTokenGenerator.getInstance();

    private static final Logger log = LoggerFactory.getLogger(SetUserSlugChange.class);

    protected ResourceAccessor resourceAccessor;

    @Override
    public void execute(final Database database) throws CustomChangeException {
        final JdbcTemplate template = LiquibaseUtils.getJdbcTemplate(database);

        try {
            // It's OK to keep these all in memory - there will be very few of them
            final Map<Integer, String> needingNewSlug = Maps.newLinkedHashMap();
            template.query("select id, login from t_user order by id", resultSet -> {
                final int id = resultSet.getInt(1);
                final String name = resultSet.getString(2);
                if (name.length() > UserEntity.MAX_GENERATED_SLUG_LENGTH || !UserEntity.slugify(name).equals(name)) {
                    needingNewSlug.put(id, name);
                }
            });
            log.debug("Found {} users requiring a slug other than their login", needingNewSlug.size());

            outer: for (final Map.Entry<Integer, String> idAndName : needingNewSlug.entrySet()) {
                // Note that the generated slug is always less than MAX_SLUG_LENGTH so there is no need to check length
                final String initialSlug = UserEntity.slugify(idAndName.getValue());
                String slug = initialSlug;

                for (int retries = 0; retries <= UserEntity.MAX_SLUG_RETRY_COUNT; ++retries) {
                    if (isSlugFree(template, slug)) {
                        log.debug("Found available slug \"{}\" with ID {} and name \"{}\"",
                            slug,
                            idAndName.getKey(),
                            idAndName.getValue());
                        updateSlug(template, idAndName, slug);

                        continue outer;
                    } else {
                        log.debug("Collision detected for slug \"{}\" with ID {} and name \"{}\"",
                            slug,
                            idAndName.getKey(),
                            idAndName.getValue());
                        slug = initialSlug + retries;
                    }
                }

                slug = UserEntity.generateSlug(GENERATOR);
                log.debug("Using random token slug \"{}\" with ID {} and name \"{}\".",
                    slug,
                    idAndName.getKey(),
                    idAndName.getValue());
                updateSlug(template, idAndName, slug);
            }
        } catch (final DataAccessException e) {
            throw new CustomChangeException("Failed to initialise all slug values on t_user", e);
        }
    }

    private boolean isSlugFree(final JdbcTemplate template, final String slug) {
        return template.queryForObject("select count(id) from t_user where slug = ?", Integer.class, slug) == 0;
    }

    @Override
    public void rollback(final Database database) throws CustomChangeException {
        final JdbcTemplate template = LiquibaseUtils.getJdbcTemplate(database);
        try {
            template.update("update t_user set slug = login");
        } catch (final DataAccessException e) {
            throw new CustomChangeException("Could not rollback generation of slug values for t_user table", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "All user slug values were set";
    }

    private int updateSlug(final JdbcTemplate template, final Map.Entry<Integer, String> idAndName, final String slug) {
        return template.update("update t_user set slug = ? where id = ?", slug, idAndName.getKey());
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(final ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(final Database database) {
        return new ValidationErrors();
    }
}
