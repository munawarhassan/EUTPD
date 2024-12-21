package com.pmi.tpd.web.rest.rsrc.api;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.security.Principal;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.user.IUserAdminService;
import com.pmi.tpd.web.rest.RestApplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for managing the current user's account.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/secure")
@Tag(description = "Endpoint for security and user account", name = "security")

public class SecurityResource {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityResource.class);

    /** */
    private final IUserAdminService userAdminService;

    /**
     * @param securityManager
     * @param userService
     */
    @Inject
    public SecurityResource(@Nonnull final IUserAdminService userAdminService) {
        this.userAdminService = checkNotNull(userAdminService, "userAdminService");
    }

    /**
     * @param request
     * @return
     */
    @Path("token")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    @PermitAll
    @Timed
    @Operation(summary = "Gets the current CSRF token",
            description = "Returns a string representing the csrf token associated to current session",
            responses = { @ApiResponse(responseCode = "400", description = "The current session is invalid"),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded") })
    public Response token(@Context final HttpServletRequest request) {
        final CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("csrfToken fund:{}", csrfToken.getToken());
        }
        String actualToken = request.getHeader(csrfToken.getHeaderName());
        if (actualToken == null) {
            actualToken = request.getParameter(csrfToken.getParameterName());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("actualToken fund:{}", actualToken);
        }
        if (actualToken == null || !actualToken.equals(csrfToken.getToken())) {
            actualToken = csrfToken.getToken();
        }
        if (actualToken == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        return Response.ok(actualToken).build();
    }

    /**
     * @param request
     * @return
     */
    @Path("host")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @PermitAll
    @Timed
    @Operation(summary = "Gets the current host name", description = "Returns host name server",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = HostRequest.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded") })
    public Response getHostName(@Context final HttpHeaders header, @Context final HttpServletRequest httpRequest) {
        final String host = header.getHeaderString("host");
        return Response.ok(new HostRequest(httpRequest.getScheme(), host)).build();
    }

    /**
     * GET /authenticate -> check if the user is authenticated, and return its login.
     *
     * @param context
     * @return
     */
    @Path("authenticate")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    @Timed
    @Operation(summary = "Checks whether the current user is authenticated",
            description = "Returns (200) whether the current user is authenticated, otherwise (400).",
            responses = { @ApiResponse(responseCode = "400", description = "The current user is not authenticate"),

                    @ApiResponse(responseCode = "200", description = "the user is authtenticated") })
    public Response isAuthenticated(@Context final SecurityContext context) {
        LOGGER.debug("REST request to check if the current user is authenticated");
        final Principal principal = context.getUserPrincipal();
        if (principal == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        return Response.ok(principal.getName()).build();
    }

    /**
     * GET /account -> get the current user.
     *
     * @param context
     * @return
     */
    @GET
    @Path("account")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Gets the current account", description = "Returns the current user.",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserRequest.class))),
                    @ApiResponse(responseCode = "401",
                            description = "The current user is not authenticate or anonymous"),
                    @ApiResponse(responseCode = "400", description = "The user doesn't exist"),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded") })
    public Response getAccount(@Context final SecurityContext context) {
        if (context.getUserPrincipal() == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final Principal principal = context.getUserPrincipal();
        final String username = principal.getName();
        // TODO return anonymous user
        if (username == null || ApplicationConstants.Security.ANONYMOUS_USER.equals(username)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final UserRequest userRequest = userAdminService.getUserDetails(username);
        if (userRequest == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.ok(userRequest).build();
    }

    public static class HostRequest {

        /** */
        private final String scheme;

        /** */
        private final String hostname;

        public HostRequest(final String scheme, final String hostname) {
            super();
            this.scheme = scheme;
            this.hostname = hostname;
        }

        public String getHostname() {
            return hostname;
        }

        public String getScheme() {
            return scheme;
        }

    }

}
