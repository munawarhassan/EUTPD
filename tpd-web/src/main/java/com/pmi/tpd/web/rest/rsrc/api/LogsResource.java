package com.pmi.tpd.web.rest.rsrc.api;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.logback.web.ViewLastLogProvider;
import com.pmi.tpd.web.logback.web.ViewLastLogProvider.LogEvents;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.LoggerRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import liquibase.Liquibase;

/**
 * Endpoint for view and managing Log Level at runtime.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/logs")
@Tag(description = "Endpoint for view and managing Log Level", name = "logger")
public class LogsResource {

    /**
     *
     */
    private static final List<String> ACTIVATE_LOGGER = ImmutableList.<String> builder()
            .add("ROOT")
            .add("com.pmi.tpd.core")
            .add("com.pmi.tpd.service")
            .add("com.pmi.tpd.web")
            .add(RestApplication.class.getPackage().getName())
            .add(Liquibase.class.getPackage().getName())
            .add("org.hibernate.validator")
            .add("org.hibernate")
            .add("org.springframework")
            .add("org.springframework.core")
            .add("org.springframework.web")
            .add("org.springframework.security")
            .add("org.springframework.cache")
            .add("com.codahale.metrics")
            .build();

    /** */
    private final ViewLastLogProvider viewLastLogProvider;

    /**
     * Default constructor.
     */
    public LogsResource() {
        this.viewLastLogProvider = new ViewLastLogProvider();
    }

    /**
     * @return Returns a list of all available logger.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed()
    @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
    @Operation(summary = "Finds all available logger")
    public Response findAll() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final List<LoggerRequest> loggers = Lists.newArrayList();
        for (final ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            if (ACTIVATE_LOGGER.contains(logger.getName())) {
                loggers.add(new LoggerRequest(logger));
            }
        }
        return ResponseFactory.ok(loggers).build();
    }

    /**
     * Change the level of specific logger.
     *
     * @param loggerRequest
     *                      the request containing the new level for specific logger.
     * @return Returns status code OK with empty response when the request has succeeded.
     */
    @Timed
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
    @Operation(summary = "Sets the level for a logger")
    public Response changeLevel(final LoggerRequest loggerRequest) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(loggerRequest.getName()).setLevel(Level.valueOf(loggerRequest.getLevel()));
        return ResponseFactory.ok().build();
    }

    /**
     * @return Returns the list of {@link StatusLog}.
     */
    @Path("/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
    @Operation(summary = "Gets status log")
    public Response getStatus() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final StatusManager statusManager = context.getStatusManager();
        return ResponseFactory.ok(Lists.transform(statusManager.getCopyOfStatusList(), StatusLog::new)).build();
    }

    /**
     * @return Returns list of last log.
     */
    @Path("/last")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
    @Operation(summary = "Gets list of last log")
    public Response getLastLog() {
        final LogEvents logEvents = this.viewLastLogProvider.printLogs();
        return ResponseFactory.ok(logEvents).build();
    }

    /**
     * Wrapper of {@link Status}.
     *
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonSerialize
    public static class StatusLog {

        /** */
        private final Date date;

        /** */
        private final String level;

        /** */
        private final String message;

        /**
         * @param status
         *               the status to use.
         */
        public StatusLog(final Status status) {
            this.date = new Date(status.getDate());
            this.level = Level.toLevel(status.getLevel()).toString();
            this.message = status.getMessage();
        }

        /**
         * @return Returns the date of status log.
         */
        public Date getDate() {
            return date;
        }

        /**
         * @return Returns the level of status log
         */
        public String getLevel() {
            return level;
        }

        /**
         * @return Returns the message of status log
         */
        public String getMessage() {
            return message;
        }

    }

}
