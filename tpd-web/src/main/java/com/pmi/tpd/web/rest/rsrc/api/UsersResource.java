package com.pmi.tpd.web.rest.rsrc.api;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.security.access.prepost.PreAuthorize;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserProfileRequest;
import com.pmi.tpd.api.user.UserSettings;
import com.pmi.tpd.api.user.avatar.AvatarSize;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.user.IUserAdminService;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.avatar.FilePartAvatarSupplier;
import com.pmi.tpd.web.rest.model.NamedLink;
import com.pmi.tpd.web.rest.model.UpdatePasswordRequest;
import com.pmi.tpd.web.rest.model.UpdateUserProfile;
import com.pmi.tpd.web.rest.util.AvatarSupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for managing users.
 *
 * @author pschmid<pascal.schmid@outlook.com>
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/users")
@Tag(description = "Endpoint for users management", name = "user")
public class UsersResource extends AvatarSupport {

    /** */
    private final IUserService userService;

    /** */
    private final IUserAdminService userAdminService;

    /** */
    private final IInternalAvatarService avatarService;

    /** */
    @Context
    private UriInfo uriInfo;

    /**
     * Default Constructor.
     *
     * @param userService
     *                         user service.
     * @param userAdminService
     *                         user admin service.
     * @param i18nService
     *                         i18n service.
     */
    @Inject
    public UsersResource(@Nonnull final IInternalAvatarService avatarService, @Nonnull final IUserService userService,
            @Nonnull final IUserAdminService userAdminService, @Nonnull final I18nService i18nService) {
        super(i18nService);
        this.avatarService = checkNotNull(avatarService, "avatarService");
        this.userService = checkNotNull(userService, "userService");
        this.userAdminService = checkNotNull(userAdminService, "userAdminService");
    }

    /**
     * @param username
     *                 the username to use
     * @return Returns the requested user.
     */
    @GET
    @PreAuthorize("isAuthenticated()")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the requested user",
                    content = @Content(schema = @Schema(implementation = UserProfileRequest.class))),
            @ApiResponse(responseCode = "400", description = "if username is empty"),
            @ApiResponse(responseCode = "404", description = "user doesn't exist") })
    public Response get(@Context final SecurityContext context) {
        if (context.getUserPrincipal() == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final Principal principal = context.getUserPrincipal();
        final String username = principal.getName();

        if (username == null || StringUtils.isEmpty(username)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.user.name"));
        }
        final UserProfileRequest profile = this.userService.getUserProfile(username);
        return ResponseFactory.ok(profile).build();
    }

    /**
     * POST /account -> update the current user information.
     *
     * @param user
     *             a user to update
     * @return Returns status code OK with empty response when the request has succeeded.
     * @throws UserEmailAlreadyExistsException
     *                                         if a user a same email address.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user Profile", description = "allows to change firstname,lastname and email address.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "The request has succeeded",
                            content = @Content(schema = @Schema(implementation = UserProfileRequest.class))),
                    @ApiResponse(responseCode = "400", description = "the email address already use by another user"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response updateProfile(@Parameter(description = "user to update", required = true) //
    @Valid final UpdateUserProfile user) throws UserEmailAlreadyExistsException {

        final UserProfileRequest profile = UserProfileRequest.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
        return ResponseFactory.ok(this.userService.updateUserProfile(profile)).build();
    }

    @Path("{userSlug}/settings")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user settings", description = "allows to change user settings.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "The request has succeeded",
                            content = @Content(schema = @Schema(implementation = UserProfileRequest.class))),
                    @ApiResponse(responseCode = "400", description = "the email address already use by another user"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response updateSettings(
        @Parameter(description = "userSlug", required = true) @PathParam("userSlug") final String userSlug,
        @Valid final UserSettings settings) throws ApplicationException {

        if (StringUtils.isEmpty(userSlug)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.user.name"));
        }
        return ResponseFactory.ok(this.userService.updateUserSettings(getUser(userSlug), settings)).build();
    }

    @Path("{userSlug}/language/{lang}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user language settings", description = "allows to change user language.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "The request has succeeded",
                            content = @Content(schema = @Schema(implementation = UserProfileRequest.class))),
                    @ApiResponse(responseCode = "400", description = "the email address already use by another user"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response updateLanguage(
        @Parameter(description = "userSlug", required = true) @PathParam("userSlug") final String userSlug,
        @Parameter(description = "lang", required = true) @PathParam("lang") final String language)
            throws ApplicationException {

        if (StringUtils.isEmpty(userSlug)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.user.name"));
        }
        return ResponseFactory.ok(this.userService.updateLanguage(getUser(userSlug), language)).build();
    }

    /**
     * POST /change_password -> changes the current user's password.
     *
     * @param passwordRequest
     *                        password Request containing the new password.
     * @return Returns status code OK with empty response when the request has succeeded.
     */
    @Path("change_password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Changes the password",
            responses = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response changePassword(@Parameter(description = "password Request",
            required = true) @Valid final UpdatePasswordRequest passwordRequest) {
        userService.updatePassword(passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword());
        return ResponseFactory.ok().build();
    }

    /**
     * GET /reset_password -> request to reset passwork.
     *
     * @param username
     *                 user to use.
     * @return Returns status code OK with empty response when the request has succeeded.
     */
    @Path("request_reset_password")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "request to reset the password",
            responses = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response requestPasswordReset(
        @Parameter(description = "username", required = true) @QueryParam("username") final String username) {
        userAdminService.requestPasswordReset(username);
        return ResponseFactory.ok().build();
    }

    /**
     * GET /reset_password -> Resets the password for the user associated with the specified token to the provided
     * value.
     *
     * @param token
     *                 the token identifying the user whose password should be reset
     * @param password
     *                 the new password for the user
     * @return Returns status code OK with empty response when the request has succeeded.
     */
    @Path("reset_password")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "reset the password",
            responses = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response resetPassword(
        @Parameter(description = "token", required = true) @QueryParam("token") final String token,
        @Parameter(description = "password", required = true) @QueryParam("password") final String password) {
        userAdminService.resetPassword(token, password);
        return ResponseFactory.ok().build();
    }

    /**
     * @param userSlug
     * @param size
     * @param request
     * @param response
     * @throws IOException
     * @since 2.4
     */
    @Path("{userSlug}/avatar")
    @PermitAll
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Get avatar Url",
            responses = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response getAvatarUrl(
        @Parameter(description = "userSlug", required = true) @PathParam("userSlug") final String userSlug,
        @Parameter(description = "s", required = true) @QueryParam("s") @DefaultValue("256") final int size,
        @Context final ContainerRequestContext request) throws IOException {
        final IUser user = userService.getUserBySlug(userSlug);
        if (user != null) {
            final String avatarUrl = avatarService.getUrlForPerson(user,
                new AvatarRequest(request.getSecurityContext().isSecure(), AvatarSize.valueOf(size), true,
                        this.userService.toUserSettings(user)
                                .map(settings -> settings.getAvatarSource())
                                .orElse(null)));
            return ResponseFactory.ok(NamedLink.builder().href(avatarUrl).name(user.getDisplayName()).build()).build();
        } else {
            return ResponseFactory
                    .ok(NamedLink.builder()
                            .href(uriInfo.getBaseUriBuilder()
                                    .path(this.getClass())
                                    .path("{userSlug}/avatar.png")
                                    .build(userSlug)
                                    .toString())
                            .name(userSlug)
                            .build())
                    .build();
        }
    }

    /**
     * @param userSlug
     * @param size
     * @param request
     * @param response
     * @throws IOException
     * @since 2.4
     */
    @Path("{userSlug}/avatar.png")
    @PermitAll
    @GET
    @Produces(org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    @Timed
    @Operation(summary = "Get avatar",
            responses = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                            + "which prevented it from fulfilling the request.") })
    public Response retrieveAvatar(
        @Parameter(description = "userSlug", required = true) @PathParam("userSlug") final String userSlug,
        @Parameter(description = "s", required = true) @QueryParam("s") @DefaultValue("256") final int size,
        @Context final ContainerRequestContext request) throws IOException {
        final IUser user = userService.getUserBySlug(userSlug);
        if (user == null) {
            // To allow user avatars to be shown in non-authenticated contexts (public
            // repositories), they cannot
            // be permission checked. That means the server needs to do the next best thing
            // and respond consistently
            // for nonexistent users.
            //
            // This still leaks user existence (if they have a non-default avatar, the user
            // definitely exists), but
            // it is a weaker leak because users that do not exist still return default
            // avatars.
            return streamAvatar(avatarService.getUserDefault(size), request);
        } else {
            return retrieveAvatar(user, size, request);
        }
    }

    /**
     * Update the avatar for the user with the supplied <strong>slug</strong>.
     * <p>
     * This resource accepts POST multipart form data, containing a single image in a form-field named 'avatar'.
     * </p>
     * <p>
     * There are configurable server limits on both the dimensions (1024x1024 pixels by default) and uploaded file size
     * (1MB by default). Several different image formats are supported, but <strong>PNG</strong> and
     * <strong>JPEG</strong> are preferred due to the file size limit.
     * </p>
     * <p>
     * An example <a href="http://curl.haxx.se/">curl</a> request to upload an image name 'avatar.png' would be:
     * </p>
     *
     * <pre>
     * curl -X POST -u username:password http://example.com/rest/api/users/jdoe/avatar.png -F avatar=@avatar.png
     * </pre>
     *
     * @since 2.4
     */
    @Path("{userSlug}/avatar")
    @POST
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PreAuthorize("isAuthenticated()")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Timed
    @Operation(summary = "update avatar")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
            @ApiResponse(responseCode = "500", description = "The server encountered an unexpected condition "
                    + "which prevented it from fulfilling the request.") })
    public Response uploadAvatar(
        @Parameter(description = "userSlug", required = true) @PathParam("userSlug") final String slug,
        @FormDataParam("avatar") final FormDataBodyPart file,
        @FormDataParam("avatar") final InputStream fileInputStream) {
        final IUser user = getUser(slug);
        userService.updateAvatar(user, new FilePartAvatarSupplier(file, fileInputStream));
        return ResponseFactory.created(uriInfo.getRequestUri()).build();
    }

    @DELETE
    @Path("{userSlug}/avatar")
    public Response deleteAvatar(
        @Parameter(description = "userSlug", required = true) @PathParam("userSlug") final String slug,
        @Context final SecurityContext securityContext) {
        final IUser user = getUser(slug);
        final String name = user.getDisplayName();
        userService.deleteAvatar(user);

        final String avatarUrl = avatarService.getUrlForPerson(user,
            new AvatarRequest(securityContext.isSecure(), AvatarSize.Large, true, null));
        return ResponseFactory.ok(NamedLink.builder().href(avatarUrl).name(name).build()).build();
    }

    protected Response retrieveAvatar(@Nonnull final IUser user, final int size, final ContainerRequestContext request)
            throws IOException {
        return streamAvatar(userService.getAvatar(user, size), request);
    }

    @Nonnull
    private IUser getUser(final String slug) {
        final IUser user = userService.getUserBySlug(slug);
        if (user == null) {
            throw newNoSuchUserBySlugException(slug);
        }

        return user;
    }

    @SuppressWarnings("unused")
    private static final NamedLink DELETE_AVATAR_EXAMPLE = NamedLink.builder()
            .href("http://www.gravatar.com/avatar/aa99b351245441b8ca95d54a52d2998c")
            .build();

}
