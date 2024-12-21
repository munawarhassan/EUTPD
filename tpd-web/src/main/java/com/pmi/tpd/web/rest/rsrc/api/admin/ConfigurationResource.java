package com.pmi.tpd.web.rest.rsrc.api.admin;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.core.mail.IMailService;
import com.pmi.tpd.core.mail.MailProperties;
import com.pmi.tpd.core.security.configuration.SecurityProperties;
import com.pmi.tpd.core.security.provider.ldap.IAuthenticationCheckConnection;
import com.pmi.tpd.database.DbTypeBean;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.database.spi.IDatabaseSupplier;
import com.pmi.tpd.database.spi.IDetailedDatabase;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.DomibusSetting;
import com.pmi.tpd.web.rest.model.GeneralSetting;
import com.pmi.tpd.web.rest.model.LdapSetting;
import com.pmi.tpd.web.rest.model.MailSetting;
import com.pmi.tpd.web.rest.rsrc.api.SecurityResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/admin/config")
@Tag(description = "Endpoint for application configuration", name = "administration")
public class ConfigurationResource {

  private static final String ANONYMIZED_PASSWORD = "<*******>".intern();

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityResource.class);

  /** */
  private final IApplicationProperties applicationProperties;

  /** */
  private final IMailService mailService;

  /** */
  private final IDatabaseSupplier databaseSupplier;

  /** */
  private final IDatabaseManager databaseManager;

  private final IBackendManager backendManager;

  /** */
  private final IAuthenticationCheckConnection ldapCheckConnection;

  /**
   * @param
   */
  @Inject
  public ConfigurationResource(@Nonnull final IApplicationProperties applicationProperties,
      @Nonnull final IMailService mailService, @Nonnull final IDatabaseManager databaseManager,
      @Nonnull final IDatabaseSupplier databaseSupplier,
      @Nonnull final IAuthenticationCheckConnection ldapCheckConnection, final IBackendManager backendManager) {
    this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    this.mailService = checkNotNull(mailService, "mailService");
    this.databaseManager = checkNotNull(databaseManager, "databaseManager");
    this.databaseSupplier = checkNotNull(databaseSupplier, "databaseSupplier");
    this.ldapCheckConnection = checkNotNull(ldapCheckConnection, "ldapCheckConnection");
    this.backendManager = checkNotNull(backendManager, "backendManager");
  }

  /**
   * @return
   */
  @Path("general")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Gets general setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = GeneralSetting.class)))
  })
  public Response getGeneralSetting() {
    return ResponseFactory.ok(GeneralSetting.create(applicationProperties)).build();
  }

  /**
   * @return
   */
  @Path("domibus")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Gets Domibus server setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = DomibusSetting.class)))
  })
  public Response getDomibusSetting() {
    DomibusSetting setting = DomibusSetting.create(applicationProperties);
    if (!Strings.isNullOrEmpty(setting.getPassword())) {
      // anonymized password
      setting = setting.toBuilder().password(ANONYMIZED_PASSWORD).build();
    }
    if (setting.getJmsOptions() != null && !Strings.isNullOrEmpty(setting.getJmsOptions().getPassword())) {
      // anonymized password
      setting = setting.toBuilder()
          .jmsOptions(setting.getJmsOptions().toBuilder().password(ANONYMIZED_PASSWORD).build())
          .build();
    }

    return ResponseFactory.ok(setting).build();
  }

  /**
   * @return
   */
  @Path("ldap")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Gets Ladp server setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = LdapSetting.class)))
  })
  public Response getLdapSetting() {
    LdapSetting setting = LdapSetting.create(applicationProperties);
    if (!Strings.isNullOrEmpty(setting.getPassword())) {
      // anonymized password
      setting = setting.toBuilder().password(ANONYMIZED_PASSWORD).build();
    }
    return ResponseFactory.ok(setting).build();
  }

  /**
   * @return
   */
  @Path("mail")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Gets mail server setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = MailSetting.class)))
  })
  public Response getMailSetting() {
    MailSetting setting = MailSetting.create(applicationProperties);
    if (!Strings.isNullOrEmpty(setting.getPassword())) {
      // anonymized password
      setting = setting.toBuilder().password(ANONYMIZED_PASSWORD).build();
    }
    return ResponseFactory.ok(setting).build();
  }

  /**
   * @return
   */
  @Path("database")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Get General Configuration Settings", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = IDetailedDatabase.class)))
  })
  public Response getDatabaseSetting() {
    final IDetailedDatabase detailedDatabase = databaseSupplier.get();
    return ResponseFactory.ok(detailedDatabase).build();

  }

  /**
   * @return
   */
  @Path("database/current")
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Timed
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Get current database information", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = IDataSourceConfiguration.class)))
  })
  public Response getCurrentDatabase() {
    return ResponseFactory.ok(databaseManager.getHandle().getConfiguration()).build();
  }

  /**
   * @param setting
   * @return
   */
  @Path("general")
  @POST
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Save General Configuration Setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = GeneralSetting.class))),
      @ApiResponse(responseCode = "200", description = "Save successful"),
      @ApiResponse(responseCode = "500", description = "Save failed")
  })
  public Response saveGeneralSetting(@Valid final GeneralSetting setting) {

    setting.save(applicationProperties);
    return ResponseFactory.ok().build();
  }

  /**
   * @param setting
   * @return
   */
  @Path("mail")
  @POST
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Save Mail Server Configuration Setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = MailSetting.class))),
      @ApiResponse(responseCode = "200", description = "Save successful"),
      @ApiResponse(responseCode = "500", description = "Save failed")
  })
  public Response saveMailSetting(@Valid MailSetting setting) {
    if (ANONYMIZED_PASSWORD.equals(setting.getPassword())) {
      // replace with original password
      final MailSetting current = MailSetting.create(applicationProperties);
      setting = setting.toBuilder().password(current.getPassword()).build();
    }
    setting.save(applicationProperties);
    return ResponseFactory.noContent().build();
  }

  /**
   * @param setting
   * @return
   */
  @Path("domibus")
  @POST
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Save Domibus Server Configuration Setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = DomibusSetting.class))),
      @ApiResponse(responseCode = "200", description = "Save successful"),
      @ApiResponse(responseCode = "500", description = "Save failed")
  })
  public Response saveDomibusSetting(@Valid DomibusSetting setting) {
    final DomibusSetting current = DomibusSetting.create(applicationProperties);
    if (ANONYMIZED_PASSWORD.equals(setting.getPassword())) {
      // replace with original password

      setting = setting.toBuilder().password(current.getPassword()).build();
    }
    if (setting.getJmsOptions() != null && ANONYMIZED_PASSWORD.equals(setting.getJmsOptions().getPassword())) {
      // replace with original password
      final String password = current.getJmsOptions().getPassword();
      setting = setting.toBuilder()
          .jmsOptions(setting.getJmsOptions().toBuilder().password(password).build())
          .build();
    }
    setting.save(applicationProperties);
    return ResponseFactory.ok().build();
  }

  /**
   * @param setting
   * @return
   */
  @Path("ldap")
  @POST
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Save Ldap Server Configuration Setting", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = LdapSetting.class))),
      @ApiResponse(responseCode = "200", description = "Save successful"),
      @ApiResponse(responseCode = "500", description = "Save failed")
  })
  public Response saveLdapSetting(@Valid LdapSetting setting) {
    if (ANONYMIZED_PASSWORD.equals(setting.getPassword())) {
      // replace with original password
      final LdapSetting current = LdapSetting.create(applicationProperties);
      setting = setting.toBuilder().password(current.getPassword()).build();
    }
    setting.save(applicationProperties);
    return ResponseFactory.ok().build();
  }

  /**
   * @param ldapSettings
   * @return
   * @throws Exception
   */
  @Path("ldap/test")
  @POST
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Test Ldap Connection", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = LdapSetting.class))),
      @ApiResponse(responseCode = "200", description = "The test connection is successfull"),
      @ApiResponse(responseCode = "400", description = "All values are required"),
      @ApiResponse(responseCode = "500", description = "The test connection failed")
  })
  public Response ldaptestConnection(@Valid LdapSetting ldapSettings) throws Exception {
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("ldap test connection: config:{}", ldapSettings);
      }
      if (ANONYMIZED_PASSWORD.equals(ldapSettings.getPassword())) {
        // replace with original password
        final LdapSetting current = LdapSetting.create(applicationProperties);
        ldapSettings = ldapSettings.toBuilder().password(current.getPassword()).build();
      }
      final SecurityProperties configuration = ldapSettings.toSecurityConfiguration(applicationProperties);
      configuration.currentAuthenticationConfiguration().ifPresent(c -> ldapCheckConnection.checkConnection(c));

      return Response.ok().build();
    } catch (final com.pmi.tpd.core.exception.SecurityException ex) {
      throw ex;
    } catch (final Exception ex) {
      String message = ex.getMessage();
      if (ex.getCause() != null) {
        message = ex.getCause().getMessage();
      }
      return ResponseFactory.error(Status.BAD_REQUEST, null, message).build();
    }
  }

  @Path("domibus/healthcheck")
  @GET
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN, ApplicationConstants.Authorities.ADMIN })
  @Operation(summary = "Test Domibus Connection", responses = {
      @ApiResponse(responseCode = "200", description = "The connection is successful"),
      @ApiResponse(responseCode = "400", description = "All values are required"),
      @ApiResponse(responseCode = "500", description = "The test connection failed")
  })
  public Response domibusHealthCheck(@NotNull @QueryParam("url") final String healthCheckUrl) throws Exception {
    try {

      this.backendManager.healthCheck(healthCheckUrl);

      return Response.ok().build();
    } catch (final com.pmi.tpd.core.exception.SecurityException ex) {
      throw ex;
    } catch (final Exception ex) {
      String message = ex.getMessage();
      if (ex.getCause() != null) {
        message = ex.getCause().getMessage();
      }
      return ResponseFactory.error(Status.BAD_REQUEST, null, message).build();
    }
  }

  /**
   * @param to
   * @param mailSettings
   * @return
   * @throws Exception
   */
  @Path("mail/test")
  @POST
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Test Mail Connection", responses = {
      @ApiResponse(responseCode = "200", description = "The connection is successful"),
      @ApiResponse(responseCode = "400", description = "All values are required"),
      @ApiResponse(responseCode = "500", description = "The test connection failed")
  })

  public Response mailTestConnection(@NotNull @QueryParam("to") final String to, @Valid MailSetting mailSettings)
      throws Exception {
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("mail test connection: from:{}, to:{}, config:{}",
            mailSettings.getEmailFrom(),
            to,
            mailSettings);
      }
      if (ANONYMIZED_PASSWORD.equals(mailSettings.getPassword())) {
        // replace with original password
        final MailSetting current = MailSetting.create(applicationProperties);
        mailSettings = mailSettings.toBuilder().password(current.getPassword()).build();
      }

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

  /**
   * @return
   */
  @Path("mail")
  @DELETE
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Operation(summary = "Delete Mail Server Configuration Setting", responses = {
      @ApiResponse(responseCode = "200", description = "Delete is successful"),
      @ApiResponse(responseCode = "500", description = "Delete failed")
  })
  public Response deleteMailSetting() {
    applicationProperties.removeConfiguration(MailProperties.class);
    return ResponseFactory.noContent().build();
  }

  /**
   * @return
   */
  @Path("supportedDatabaseTypes")
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Timed
  public Response getSupportedDatabaseTypes() {
    return ResponseFactory.ok(DbTypeBean.ALL).build();
  }

  /**
   * @return
   */
  @Path("defaultSupportedDatabase")
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
  @Timed
  public Response getDefaultSupportedDatabase() {
    return ResponseFactory.ok(DbTypeBean.DEFAULT).build();
  }

}
