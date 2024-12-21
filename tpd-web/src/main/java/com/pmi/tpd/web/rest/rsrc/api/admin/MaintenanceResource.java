package com.pmi.tpd.web.rest.rsrc.api.admin;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.core.elasticsearch.IIndexerService;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.IMaintenanceStatus;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.migration.IMigrationService;
import com.pmi.tpd.database.DbTypeBean;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.config.DefaultDataSourceConfiguration;
import com.pmi.tpd.database.config.SimpleDataSourceConfiguration;
import com.pmi.tpd.scheduler.exec.IRunnableTaskStatus;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.DatabaseSetting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(RestApplication.API_RESOURCE_PATH + "/admin/maintenance")
@Tag(description = "Endpoint for Maintenance actions", name = "administration")
public class MaintenanceResource {

  /** */
  private final IMigrationService migrationService;

  /** */
  private final IIndexerService indexerService;

  /**
   * @param
   */
  @Inject
  public MaintenanceResource(@Nonnull final IMigrationService migrationService,
      @Nonnull final IIndexerService indexerService) {
    this.migrationService = checkNotNull(migrationService, "migrationService");
    this.indexerService = checkNotNull(indexerService, "indexerService");
  }

  @Path("index")
  @POST
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Operation(summary = "Re-Index the database")
  public Response index() {
    final ITaskMaintenanceMonitor task = indexerService.performIndex();
    return ResponseFactory.ok(task).build();
  }

  /**
   * @return
   */
  @Path("progress")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public Response getProgress() {
    final IMaintenanceService maintenanceService = migrationService.getMaintenanceService();
    final ITaskMaintenanceMonitor task = maintenanceService.getRunningTask();
    if (task == null) {
      final IMaintenanceStatus status = maintenanceService.getStatus();
      final IRunnableTaskStatus latestTask = status.getLatestTask();
      if (TaskState.FAILED.equals(latestTask.getState())) {
        return ResponseFactory
            .error(Status.INTERNAL_SERVER_ERROR,
                latestTask.getName(),
                "A internal error occured. See with administrator for more information")
            .build();
      } else {
        return ResponseFactory.notFound().build();
      }
    }
    return ResponseFactory.ok(task.getProgress()).build();
  }

  @Path("migration")
  @POST
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Operation(summary = "Migrate database to")
  public Response migrate(final DatabaseSetting databaseSetting) {
    final DbTypeBean currentDbType = DbTypeBean.forKey(databaseSetting.getType());
    if (currentDbType == null) {
      return ResponseFactory.badRequest("You must select database type").build();
    }
    final String jdbcUrl = currentDbType.generateUrl(databaseSetting.getHostname(),
        databaseSetting.getDatabaseName(),
        databaseSetting.getPort());
    final IDataSourceConfiguration configuration = new DefaultDataSourceConfiguration(
        currentDbType.getDriverClassName(), databaseSetting.getUsername(), databaseSetting.getPassword(),
        jdbcUrl);
    final ITaskMaintenanceMonitor task = performDatabaseMigration(configuration);
    return ResponseFactory.ok(task).build();
  }

  @Path("database/testconnection")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
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

  private void performTestDatabaseConnection(final DatabaseSetting setting) {
    final DbTypeBean currentDbType = DbTypeBean.forKey(setting.getType());
    final String jdbcUrl = currentDbType
        .generateUrl(setting.getHostname(), setting.getDatabaseName(), setting.getPort());
    final IDataSourceConfiguration configuration = new SimpleDataSourceConfiguration(
        currentDbType.getDriverClassName(), jdbcUrl, setting.getUsername(), setting.getPassword());
    migrationService.validateConfiguration(configuration);
  }

  private ITaskMaintenanceMonitor performDatabaseMigration(final IDataSourceConfiguration configuration) {
    final ITaskMaintenanceMonitor task = migrationService.migrate(configuration);
    return task;
  }
}
