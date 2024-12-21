package com.pmi.tpd.api;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

/**
 * <p>
 * Config class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@SuppressWarnings("checkstyle:interfaceistype")
public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    /** Constant <code>DEFAULT_DATE_TIME_PATTERN="yyyy-MM-dd'T'HH:mm:ss"</code>. */
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    /** */
    public static final String DATE_PATTERN = "dd/MM/yyyy";

    /** */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_PATTERN);

    /** */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern(DEFAULT_DATE_TIME_PATTERN);

    /** */
    public static final String APPLICATION_KEY = "tpd";

    /** */
    public static final String HOME_ENV_VARIABLE = "TPD_HOME";

    /** */
    public static final String PROFILES_ENV_VARIABLE = "TPD_PROFILES";

    /** */
    public static final String SHARED_HOME_DIR_ENV_VARIABLE = "TPD_SHARED_HOME";

    /** */
    public static final String BOOTSTRAP_PROPERTIES_NAME = "bootstrap.properties";

    /** */
    public static final String BOOTSTRAP_PROPERTIES_RESOURCE = "classpath:" + BOOTSTRAP_PROPERTIES_NAME;

    /** */
    public static final String PROPERTIES_FILENAME = "config/application-${spring.profiles.active}.yml";

    /** */
    public static final String CONFIG_PROPERTIES_FILE_NAME = "app-config.properties";

    // /** */
    // public static final String BUILD_PROPERTIES_FILENAME = IBuildUtilsInfo.BUILD_VERSIONS_PROPERTIES;

    /**
     * Gets the default date format used.
     *
     * @return Returns a new {@link java.text.DateFormat}
     */
    public static DateFormat getDateFormat() {
        return new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * @return Returns the default {@link Locale}.
     */
    public static Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    /**
     * @return Returns the defualt {@link Charset}.
     */
    public static Charset getDefaultCharset() {
        return Charsets.UTF_8;
    }

    /**
     * @author Christophe Friederich
     */
    public static class Setup {

        /** */
        public static final String SETUP_BASE_URL = "setup.baseUrl";

        /** */
        public static final String SETUP_DISPLAY_NAME = "setup.displayName";

        /** */
        public static final String SETUP_USER_NAME = "setup.sysadmin.username";

        /** */
        public static final String SETUP_USER_PASSWORD = "setup.sysadmin.password";

        /** */
        public static final String SETUP_USER_DISPLAY_NAME = "setup.sysadmin.displayName";

        /** */
        public static final String SETUP_USER_EMAIL_ADDRESS = "setup.sysadmin.emailAddress";

        /** */
        public static final Set<String> SETUP_USER_PROPERTIES = ImmutableSet
                .of(SETUP_USER_NAME, SETUP_USER_PASSWORD, SETUP_USER_DISPLAY_NAME, SETUP_USER_EMAIL_ADDRESS);
    }

    /**
     * @author Christophe Friederich
     */
    public interface LifeCycle {

        /**
         * Bootstrap the server before anything else in the lifecycle.
         */
        int LIFECYCLE_PHASE_BOOTSTRAP = 1;

        /**
         * Start the maintenance service. This ensures that the if the node has joined a cluster and the cluster is in
         * maintenance mode, this node will be put in maintenance mode immediately after startup.
         */
        int LIFECYCLE_PHASE_MAINTENANCE_SERVICE = 200;

        /**
         * Start Config lifecycle components.
         */
        int LIFECYCLE_PHASE_CONFIG = 300;

        /**
         * Start the scheduler after the {@link #LIFECYCLE_PHASE_PLUGINS plugin framework}. Jobs scheduled before this
         * phase should be queued until the scheduler starts.
         */
        int LIFECYCLE_PHASE_SCHEDULER = 1000;

        /**
         * Start scheduled jobs after starting the {@link #LIFECYCLE_PHASE_SCHEDULER scheduler}.
         */
        int LIFECYCLE_PHASE_SCHEDULED_JOBS = 1001;

        /**
         * Update Hazelcast capabilities after all the plugins have been initialised in {@link #LIFECYCLE_PHASE_PLUGINS}
         * .
         */
        int LIFECYCLE_PHASE_HAZELCAST = 1500;

        /**
         * Start scheduled jobs after starting the {@link #LIFECYCLE_PHASE_SCHEDULER scheduler}.
         */
        int LIFECYCLE_PHASE_BACKEND_SERVICE = 2000;

        /**
         * The last phase of the startup lifecycle.
         */
        int LIFECYCLE_PHASE_SERVER_READY = Integer.MAX_VALUE;
    }

    /**
     * @author Christophe Friederich
     */
    public interface Logging {

        /**
         * The MDC variable for binding/retrieving labels associated with the request. Labels are logged as part of the
         * access log and provide more information about a request in the access logs for supportability or analysis.
         */
        String MDC_REQUEST_LABELS = "a-request-labels";
    }

    /**
     * @author Christophe Friederich
     */
    public interface Security {

        /** */
        String DEFAULT_GROUP_USER_CODE = "tpd-user";

        /** */
        String DEFAULT_GROUP_ADMINISTRATOR_CODE = "tpd-administrator";

        /** */
        String DEFAULT_GROUP_SYSTEM_ADMINISTRATOR_CODE = "tpd-sysadmin";

        /** */
        String DEFAULT_GROUP_ANONYMOUS_CODE = "Anonymous";

        /** */
        String ANONYMOUS_USER = "anonymous";

        /** */
        String[] ALL_ATHENTICATION_GROUP = { DEFAULT_GROUP_ADMINISTRATOR_CODE, DEFAULT_GROUP_USER_CODE };
    }

    /**
     * <b>note:</b> must be a class. used in ApplicationProperties to load default properties
     *
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class PropertyKeys {

        /** */
        public static final String APPLICATION_PATH_PROPETY = "app.path";

        /** */
        public static final String HOME_PATH_SYSTEM_PROPERTY = "app.home";

        /**
         * @since 1.3
         */
        public static final String SHARED_DIR_PATH_SYSTEM_PROPERTY = "app.shared.home";

        /** */
        public static final String LOG_LEVEL_PROPERTY = "app.log.loglevel";

        /** */
        public static final String PATCHED_VERSION_PROPERTY = "app.version.patched";

        /** */
        public static final String SETUP_PROPERTY = "app.setup";

        /** */
        public static final String WEB_ENCODING = "app.i18n.encoding";

        /** */
        public static final String WEB_CHARACTER_SET = "app.i18n.characterset";

        /** */
        public static final String I18N_DEFAULT_LOCALE = "app.i18n.default.locale";

        /** */
        public static final String LANGUAGE = "app.i18n.default.locale";

        /** */
        public static final String AUTO_SETUP_PROPERTY = "app.auto-setup";

        /* The active AvatarSource for the ConfigurableAvatarService */
        public static final String AVATAR_SOURCE = "app.avatar.source";

    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public interface SettingKeys {

        /** */
        String EMAIL_REPORTER = "app.config.email-reporter";
    }

    /**
     * Directory Section.
     *
     * @author Christophe Friederich
     * @since 1.0
     */
    public interface Directories {

        /** */
        String BACKUP_DIRECTORY = "backup";

        /** */
        String REPORT_DIRECTORY = "report";

        /** */
        String PLUGINS_DIRECTORY = "plugins";

        /** */
        String LOG_DIRECTORY = "logs";

        /** */
        String SHARED_DIRECTORY = "shared";

        /** */
        String ATTACHMENT_DIRECTORY = "attachments";

        /** */
        String INDEX_DIRECTORY = "index";

        /** */
        String LIB_DIRECTORY = "lib";

        /** */
        String CONFIGURATION_DIRECTORY = "conf";

        /** */
        String TRASH_DIRECTORY = "trash";

        /** */
        String WORK_DIRECTORY = "work";

        /** */
        String DATA_DIRECTORY = "data";

    }

    /**
     * @author Christophe Friederich
     */
    public interface Jpa {

        /**
         * entity manager factory bean name.
         */
        String ENTITY_MANAGER_FACTORY_NAME = "entityManagerFactory";

        /** */
        String PREFIX_MODEL_PACKAGE = "com.pmi.tpd.core.";

        /** */
        String MODEL_PACKAGE = PREFIX_MODEL_PACKAGE + "model";

        /**
         * @author Christophe Friederich
         */
        public interface Cache {

            /** */
            String ROLES = "roles";

        }

        /**
         * @author Christophe Friederich
         */
        public interface Generator {

            /** */
            String NAME = "t_generated_id";

            /** */
            String COLUMN_NAME = "key_name";

            /** */
            String COLUMN_VALUE_NAME = "key_value";

        }

    }

    /**
     * @author Christophe Friederich
     */
    public interface Liquibase {

        String LIQUIBASE_BEAN_NAME = "liquibase";

        /** */
        String CHANGE_LOG_LOCATION = "classpath:liquibase/master.xml";
    }

    /**
     */
    public interface Authorities {

        String SYS_ADMIN = "SYS_ADMIN";

        /** */
        String ADMIN = "ADMIN";

        /** */
        String USER = "USER";

        /** */
        String ANONYMOUS = ApplicationConstants.Security.DEFAULT_GROUP_ANONYMOUS_CODE;

    }

    /**
     * @author Christophe Friederich
     */
    public interface Hazelcast {

        /** */
        String COOKIE_NAME = "hazelcast.sessionId";

    }

    public interface Log {

        /** */
        String USERID_MDC_KEY = "userid";

        /** */
        String CYCLIC_BUFFER_APPENDER_NAME = "CYCLIC";
    }

}
