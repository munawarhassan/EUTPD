package com.pmi.tpd.web.rest.rsrc.api.setup;

import static javax.ws.rs.core.Response.Status.OK;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.exception.ServerException;
import com.pmi.tpd.api.exec.ICompletionCallback;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.mail.IMailService;
import com.pmi.tpd.core.mail.MailProperties;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.migration.IMigrationService;
import com.pmi.tpd.core.security.IEscalatedSecurityContext;
import com.pmi.tpd.core.security.ISecurityService;
import com.pmi.tpd.core.user.IUserAdminService;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.database.DbTypeBean;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.config.DefaultDataSourceConfiguration;
import com.pmi.tpd.database.config.SimpleDataSourceConfiguration;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.SetPermissionRequest;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.DatabaseSetting;
import com.pmi.tpd.web.rest.model.GeneralSetting;
import com.pmi.tpd.web.rest.model.MailSetting;
import com.pmi.tpd.web.rest.model.UserAdminRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + SetupResource.PAGE_NAME)
@Singleton
@PermitAll
public class SetupResource {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupResource.class);

    /** */
    public static final String PAGE_NAME = "/setup";

    /** */
    public static final String FULL_PAGE_NAME = RestApplication.API_RESOURCE_PATH + PAGE_NAME;

    /** */
    @Context
    private UriInfo uriInfo;

    /** */
    private final IApplicationProperties applicationProperties;

    /** */
    private final IUserService userService;

    /** */
    private final IMailService mailService;

    /** */
    private final IMigrationService migrationService;

    /**
     * default data source configuration continued in application properties file (inject by maven).
     */
    private final IDataSourceConfiguration defaultDataSourceConfiguration;

    private final AtomicBoolean isSetUpDatabase;

    /** */
    private final AtomicBoolean isSettingUpDatabase;

    /** */
    private final IEscalatedSecurityContext asSysAdmin;

    /** */
    private final IPermissionAdminService permissionAdminService;

    /** */
    private final IPermissionService permissionService;

    /** */
    private final IUserAdminService userAdminService;

    /** */
    private final I18nService i18nService;

    /** */
    private final IPropertiesManager propertiesManager;

    /**
     *
     */
    @Inject
    public SetupResource(final IApplicationProperties applicationProperties, final IPropertiesManager propertiesManager,
            final IMailService mailService, final IUserService userService, final IMigrationService migrationService,
            final IDataSourceConfiguration defaultDataSourceConfiguration, final ISecurityService securityService,
            final IPermissionAdminService permissionAdminService, final IPermissionService permissionService,
            final IUserAdminService userAdminService, final I18nService i18nService) {
        this.applicationProperties = applicationProperties;
        this.propertiesManager = propertiesManager;
        this.userService = userService;
        this.migrationService = migrationService;
        this.mailService = mailService;
        this.defaultDataSourceConfiguration = defaultDataSourceConfiguration;
        this.isSettingUpDatabase = new AtomicBoolean(false);
        this.isSetUpDatabase = new AtomicBoolean(false);
        this.asSysAdmin = securityService.withPermission(Permission.SYS_ADMIN, "SetupResource");
        this.permissionAdminService = permissionAdminService;
        this.permissionService = permissionService;
        this.userAdminService = userAdminService;
        this.i18nService = i18nService;
    }

    /**
     * @return
     */
    @Path("progress")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getProgress() {
        return asSysAdmin.call(() -> {
            final IMaintenanceService maintenanceService = migrationService.getMaintenanceService();
            final ITaskMaintenanceMonitor task = maintenanceService.getRunningTask();
            if (task == null) {
                return ResponseFactory.notFound().build();
            }
            return ResponseFactory.status(Status.ACCEPTED).entity(task.getProgress()).build();
        });
    }

    /**
     * @return
     */
    @Path("config/general")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @PermitAll
    @Operation(summary = "Get General Configuration Settings", responses = { @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = GeneralSetting.class))) })
    public Response getGeneralConfiguration() {
        return ResponseFactory.ok(GeneralSetting.create(applicationProperties)).build();
    }

    /**
     * @return
     */
    @Path("config/mail")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @PermitAll
    @Operation(summary = "Get Mail Server Configuration Settings", responses = { @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = MailSetting.class))) })
    public Response getMailConfiguration() {
        return ResponseFactory.ok(MailSetting.create(applicationProperties)).build();
    }

    /**
     * @param setting
     *                   database setting, can be empty (NOT USE @Valid annotation).
     * @param isInternal
     * @return
     */
    @Path("database")
    @POST
    @PermitAll
    public Response configureDatabase(final DatabaseSetting setting,
        @NotNull @QueryParam("internal") final boolean isInternal) {
        checkInDefaultModeAndSetupNotComplete();

        // Set locale, even when data is not complete, to ensure it shows again
        // setLocale(locale);
        if (hasSetupDatabase()) {
            return Response.ok().build();
        }
        final DbTypeBean currentDbType = DbTypeBean.forKey(setting.getType());
        if (currentDbType == null && !isInternal) {
            return ResponseFactory.badRequest("You must select database type").build();
        }
        // default config (derby internal database)
        IDataSourceConfiguration configuration = defaultDataSourceConfiguration;
        if (isInternal) {
            markDatabaseAsSetup();
            return ResponseFactory.status(Status.CREATED).build();
        } else {
            final String jdbcUrl = currentDbType
                    .generateUrl(setting.getHostname(), setting.getDatabaseName(), setting.getPort());
            configuration = new DefaultDataSourceConfiguration(currentDbType.getDriverClassName(),
                    setting.getUsername(), setting.getPassword(), jdbcUrl);
            final ITaskMaintenanceMonitor task = performDatabaseSetup(configuration);
            return ResponseFactory.ok().entity(task).build();
        }

    }

    @Path("createAdmin")
    @POST
    @PermitAll
    public Response addFirstAdminUser(@Valid final UserAdminRequest account) throws Exception {
        LOGGER.info("creating first system administrator user '{}'", account.getLogin());
        checkSetupNotComplete();

        createAdminUser(account);
        return ResponseFactory.ok().build();
    }

    @Path("mail")
    @POST
    @PermitAll
    public Response saveMail(final MailSetting mailSetting) {

        // force to recreate new PropertySet
        this.propertiesManager.refresh();
        mailSetting.save(applicationProperties);

        return ResponseFactory.accepted().build();

    }

    @Path("complete")
    @POST
    @PermitAll
    public Response confirmComplete() {
        markSetupAsComplete();
        return ResponseFactory.accepted().build();

    }

    @Path("database/testconnection")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response databaseTestConnection(@Valid final DatabaseSetting setting) throws Exception {
        try {
            performTestDatabaseConnection(setting);
            return Response.ok().build();
        } catch (final Exception ex) {
            String message = ex.getMessage();
            if (ex.getCause() != null) {
                message = ex.getCause().getMessage();
            }
            return ResponseFactory.error(Status.BAD_REQUEST, null, message).build();
        }
    }

    @Path("mail/testconnection")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response mailTestConnection(@NotNull @QueryParam("to") final String to,
        @Valid final MailSetting mailSettings) throws Exception {
        try {
            final MailProperties mailProperties = new MailProperties();
            mailProperties.setHost(mailSettings.getHostname());
            mailProperties.setUser(mailSettings.getUsername());
            mailProperties.setPassword(mailSettings.getPassword());
            mailProperties.setPort(mailSettings.getPort());
            mailProperties.setTls(mailSettings.getTls());

            mailService.sendTest(mailProperties, mailSettings.getEmailFrom(), to);

            return Response.ok().build();
        } catch (final Exception ex) {
            String message = ex.getMessage();
            if (ex.getCause() != null) {
                message = ex.getCause().getMessage();
            }
            return ResponseFactory.error(Status.BAD_REQUEST, null, message).build();
        }
    }

    @Path("databaseTypes")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getDatabaseType() {
        return Response.status(OK).entity(DbTypeBean.ALL).build();
    }

    @Path("defaultExternalDatabase")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getDefaultExternalDatabase() {
        return Response.status(OK).entity(DbTypeBean.DEFAULT).build();
    }

    private void checkSetupNotComplete() {
        if (applicationProperties.isSetup()) {
            throw new IllegalStateException("Application is already setup!");
        }
    }

    private void checkInDefaultModeAndSetupNotComplete() {
        checkSetupNotComplete();

        // if (propertiesService.getMode() != ApplicationMode.DEFAULT) {
        // throw new IllegalStateException("Application not in default mode.");
        // }
    }

    private void markSetupAsComplete() {

        applicationProperties.setSetup(true);
        ComponentManager.getInstance().getUpgradeLauncher().checkIfUpgradeNeeded();
    }

    private void markDatabaseAsSetup() {
        isSetUpDatabase.set(true);
    }

    private boolean hasSetupDatabase() {
        return isSetUpDatabase.get();
    }

    @SuppressWarnings("unused")
    private void setLocale(final String locale) {
        // asSysAdmin.call(() -> {
        // propertiesService.setLocale(LocaleUtils.toLocale(locale));
        // return null;
        // });
    }

    @SuppressWarnings("unused")
    private boolean hasAdminUser() {
        // check if there is at least one sysadmin
        return asSysAdmin.call(() -> !permissionService.getUsersWithPermission(Permission.SYS_ADMIN).isEmpty());
    }

    private void createAdminUser(final UserAdminRequest account) throws Exception {
        // create user & grant permission as sysadmin
        asSysAdmin.call(() -> {
            userAdminService.createUser(account.getLogin(), account.getPassword(), "Administrator", account.getEmail());
            // check that the user was created
            final IUser user = userService.getUserByName(account.getLogin());
            if (user == null) {
                throw new ServerException(i18nService.createKeyedMessage("app.web.setup.cannotcreateuser",
                    account.getLogin(),
                    Product.getFullName()));
            }

            permissionAdminService.setPermission(
                new SetPermissionRequest.Builder().globalPermission(Permission.SYS_ADMIN).user(user).build());
            LOGGER.info("created first system administrator user '{}'", user.getUsername());
            return null;
        });

    }

    private ITaskMaintenanceMonitor performDatabaseSetup(final IDataSourceConfiguration configuration) {

        final ITaskMaintenanceMonitor task = asSysAdmin.call(() -> migrationService.setup(configuration));
        isSettingUpDatabase.set(true);
        // We need to add the listener after we set the atomic boolean to prevent race
        // conditions
        if (task != null) {
            task.registerCallback(new ICompletionCallback() {

                @Override
                public void onCancellation() {
                    LOGGER.info("Database setup was canceled");
                    isSettingUpDatabase.set(false);
                }

                @Override
                public void onSuccess() {
                    markDatabaseAsSetup();
                    isSettingUpDatabase.set(false);
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOGGER.error("An exception occurred while setting up the database", t);
                    isSettingUpDatabase.set(false);
                }

                @Override
                public void onCompletion() {

                }
            });
        }
        return task;
    }

    private void performTestDatabaseConnection(final DatabaseSetting setting) {
        asSysAdmin.call(() -> {
            final DbTypeBean currentDbType = DbTypeBean.forKey(setting.getType());
            final String jdbcUrl = currentDbType
                    .generateUrl(setting.getHostname(), setting.getDatabaseName(), setting.getPort());
            final IDataSourceConfiguration configuration = new SimpleDataSourceConfiguration(
                    currentDbType.getDriverClassName(), jdbcUrl, setting.getUsername(), setting.getPassword());
            migrationService.validateConfiguration(configuration);
            return null;
        });

    }

}
