package com.pmi.tpd.web.rest.rsrc.api.admin;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageRequest;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.exception.UsernameAlreadyExistsException;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.security.provider.IAuthenticationProviderService;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.GroupRequest;
import com.pmi.tpd.core.user.IUserAdminService;
import com.pmi.tpd.core.user.UserUpdate;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.AddGroupRequest;
import com.pmi.tpd.web.rest.model.AdminPasswordUpdateRequest;
import com.pmi.tpd.web.rest.model.CreateGroupRequest;
import com.pmi.tpd.web.rest.model.CreateUserRequest;
import com.pmi.tpd.web.rest.model.GroupAndUsersRequest;
import com.pmi.tpd.web.rest.model.NameValuePair;
import com.pmi.tpd.web.rest.model.PermissionRequest;
import com.pmi.tpd.web.rest.model.UserAndGroupsRequest;
import com.pmi.tpd.web.rest.model.UserRenameRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for user administration.
 *
 * @since 2.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/admin")
@Tag(description = "Endpoint for user administration", name = "administration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserAdminResource {

    /** */
    private final IUserAdminService userAdminService;

    /** */
    private final IAuthenticationService authenticationService;

    /** */
    private final I18nService i18nService;

    /** */
    private final IPermissionService permissionService;

    /** */
    private final IAuthenticationProviderService authenticationProviderService;

    /**
     * @param i18nService
     *                              the i18n service
     * @param userAdminService
     *                              the user administration service.
     * @param authenticationService
     *                              the authentication provider service.
     * @param permissionService
     *                              the permission service.
     */
    @Inject
    public UserAdminResource(@Nonnull final I18nService i18nService, @Nonnull final IUserAdminService userAdminService,
            @Nonnull final IAuthenticationService authenticationService,
            @Nonnull final IPermissionService permissionService,
            @Nonnull final IAuthenticationProviderService authenticationProviderService) {
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.userAdminService = checkNotNull(userAdminService, "userAdminService");
        this.authenticationService = checkNotNull(authenticationService, "authenticationService");
        this.permissionService = checkNotNull(permissionService, "permissionService");
        this.authenticationProviderService = checkNotNull(authenticationProviderService,
            "authenticationProviderService");
    }

    /**
     * Retrieve a page of users.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param page
     *               zero-based page index.
     * @param size
     *               the size of the page to be returned.
     * @param sort
     *               the property to sort (can be {@literal null}).
     * @param filter
     *               the list of filter properties to use (can be {@literal null}), (see {@link PageRequest}).
     * @return Returns the requested page of users, which may be empty but never null.
     */
    @GET
    @Path("users")
    @Timed
    @Operation(summary = "Finds all users",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findAllUsers(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
        return ResponseFactory.ok(userAdminService.findUsers(pageRequest)).build();

    }

    /**
     * Retrieves full details for the user with the specified username, or null if no such user exists
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission or current user to call this resource.
     * </p>
     *
     * @param username
     *                 a user name
     * @return Returns 200 and the user content representation.
     */
    @GET
    @Path("users/{username}/details")
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    @Timed
    @Operation(summary = "get a user",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserRequest.class))) })
    public Response getUserDetails(
        @Parameter(description = "a user name", required = true) @PathParam("username") final String username) {
        if (StringUtils.isEmpty(username)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.user.name"));
        }
        return ResponseFactory.ok(this.userAdminService.getUserDetails(username)).build();
    }

    /**
     * Retrieves full details for the group with the specified groupName, or null if no such group exists.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission or current user to call this resource.
     * </p>
     *
     * @param groupName
     *                  a group name
     * @return Returns 200 and the user content representation.
     */
    @GET
    @Path("groups/{groupname}/details")
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    @Timed
    @Operation(summary = "get a group",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = GroupRequest.class))) })
    public Response getGroupDetails(
        @Parameter(description = "a group name", required = true) @PathParam("groupname") final String groupName) {
        if (StringUtils.isEmpty(groupName)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.group.name"));
        }
        return ResponseFactory.ok(this.userAdminService.getGroupDetails(groupName)).build();
    }

    /**
     * Retrieve a page of groups.
     * <p>
     * The authenticated user must have <strong>ADMIN</strong> permission or higher to call this resource.
     * </p>
     *
     * @param page
     *               zero-based page index.
     * @param size
     *               the size of the page to be returned.
     * @param sort
     *               the property to sort (can be {@literal null}).
     * @param filter
     *               if specified only group names containing the supplied string will be returned.
     * @return Returns the requested page of groups, which may be empty but never null.
     */
    @GET
    @Path("groups")
    @Timed
    @Operation(summary = "Finds all groups",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))) })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findGroups(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "if specified only group names containing the supplied string will be returned",
                required = false) @QueryParam("filter") final String filter) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);

        return ResponseFactory.ok(userAdminService.findGroups(pageRequest)).build();
    }

    /**
     * Retrieve a page of groups.
     * <p>
     * The authenticated user must have <strong>ADMIN</strong> permission or higher to call this resource.
     * </p>
     *
     * @param page
     *                  zero-based page index.
     * @param size
     *                  the size of the page to be returned.
     * @param sort
     *                  the property to sort (can be {@literal null}).
     * @param directory
     *                  the name of directory
     * @param groupName
     *                  if specified only group names containing the supplied string will be returned.
     * @return Returns the requested page of groups, which may be empty but never null.
     */
    @GET
    @Path("/directory/{directory}/groups")
    @Timed
    @Operation(summary = "Finds groups for a directory",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findGroupsForDirectory(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "if specified only group names containing the supplied string will be returned",
                required = false) @QueryParam("name") final String groupName,
        @Parameter(description = "a directory name",
                required = true) @PathParam("directory") final UserDirectory directory) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, null, null);

        return ResponseFactory.ok(userAdminService.findGroupsForDirectory(pageRequest, directory, groupName)).build();
    }

    /**
     * Retrieves a list of users that are members of a specified group.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param groupName
     *                  the group which should be used to locate members
     * @param page
     *                  zero-based page index.
     * @param size
     *                  the size of the page to be returned.
     * @param sort
     *                  the property to sort (can be {@literal null}).
     * @param filter
     *                  if specified only users with usernames, display names or email addresses containing the supplied
     *                  string will be returned
     * @return Returns the requested page of users, which may be empty but never null.
     */
    @GET
    @Path("groups/more-members")
    @Timed
    @Operation(summary = "Retrieves a list of users that are members of a specified group",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findUsersInGroup(
        @Parameter(description = "the group which should be used to locate members",
                required = true) @QueryParam("groupname") final String groupName,
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter) {
        checkContext(groupName);
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
        return ResponseFactory.ok(userAdminService.findUsersWithGroup(groupName, pageRequest)).build();
    }

    /**
     * Retrieves a list of users that are <em>not</em> members of a specified group.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param groupName
     *                  the group which should be used to locate non-members
     * @param page
     *                  zero-based page index.
     * @param size
     *                  the size of the page to be returned.
     * @param sort
     *                  the property to sort (can be {@literal null}).
     * @param filter
     *                  if specified only users with usernames, display names or email addresses containing the supplied
     *                  string will be returned
     * @return Returns the requested page of users, which may be empty but never null.
     */
    @GET
    @Path("groups/more-non-members")
    @Timed
    @Operation(summary = "Retrieves a list of users that are not members of a specified group",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findUsersNotInGroup(
        @Parameter(description = "the group which should be used to locate non-members",
                required = true) @QueryParam("groupname") final String groupName,
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @QueryParam("filter") @DefaultValue("") final String filter) {
        checkContext(groupName);
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);

        return ResponseFactory.ok(userAdminService.findUsersWithoutGroup(groupName, pageRequest)).build();
    }

    /**
     * Retrieves a list of groups the specified user is a member of.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param username
     *                 the user which should be used to locate groups
     * @param page
     *                 zero-based page index.
     * @param size
     *                 the size of the page to be returned.
     * @param sort
     *                 the property to sort (can be {@literal null}).
     * @param filter
     *                 if specified only groups with names containing the supplied string will be returned
     * @return Returns the requested page of groups, which may be empty but never null.
     */
    @GET
    @Path("users/more-members")
    @Timed
    @Operation(summary = "Retrieves a list of groups the specified user is a member of",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findGroupsForUser(
        @Parameter(description = "the user which should be used to locate groups",
                required = true) @QueryParam("username") final String username,
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @QueryParam("filter") @DefaultValue("") final String filter) {
        checkContext(username);
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
        return ResponseFactory.ok(userAdminService.findGroupsWithUser(username, null, pageRequest)).build();
    }

    /**
     * Retrieves a list of groups the specified user is <em>not</em> a member of.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param username
     *                 the user which should be used to locate groups
     * @param page
     *                 zero-based page index.
     * @param size
     *                 the size of the page to be returned.
     * @param sort
     *                 the property to sort (can be {@literal null}).
     * @param filter
     *                 if specified only groups with names containing the supplied string will be returned
     * @return Returns the requested page of groups, which may be empty but never null.
     */
    @GET
    @Path("users/more-non-members")
    @Timed
    @Operation(summary = "Retrieves a list of groups the specified user isnot a member of",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response findOtherGroupsForUser(
        @Parameter(description = "the user which should be used to locate groups",
                required = true) @QueryParam("username") final String username,
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @QueryParam("filter") @DefaultValue("") final String filter) {
        checkContext(username);
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);

        return ResponseFactory.ok(userAdminService.findGroupsWithoutUser(username, null, pageRequest)).build();
    }

    private void checkContext(final String groupName) {
        if (StringUtils.isEmpty(groupName)) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.members.no.context"));
        }
    }

    /**
     * Creates a new user from {@link CreateUserRequest} object.
     * <p>
     * The default group can be used to control initial permissions for new users, such as granting users the ability to
     * login or providing read access to certain projects or repositories. If the user is not added to the default
     * group, they may not be able to login after their account is created until explicit permissions are configured.
     * </p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     *
     * @param user
     *             a new user to create.
     * @return Returns 200 and the {@link UserRequest details} for the created user
     * @throws UserEmailAlreadyExistsException
     *                                         if email exists for another user.
     * @throws UsernameAlreadyExistsException
     *                                         if the user already exists.
     * @throws MailException
     *                                         if mail error occurs
     * @throws IntegrityException
     *                                         if the mail service isn't configured or wrong.
     */
    @POST
    @Path("users")
    @Timed
    @Operation(summary = "Creates a user", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = UserRequest.class))),
            @ApiResponse(responseCode = "201", description = "the user has been created."),
            @ApiResponse(responseCode = "401", description = "The authenticated user is not an administrator."),
            @ApiResponse(responseCode = "503",
                    description = "A mail server must be configured to create a user without specifying a password."),
            @ApiResponse(responseCode = "409",
                    description = "Another user with the same name or same email address already exists."),
            @ApiResponse(responseCode = "500",
                    description = "The server encountered an unexpected condition"
                            + "which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response createUser(
        @Parameter(description = "a user to create", required = true) @Valid final CreateUserRequest user)
            throws IntegrityException, MailException, UsernameAlreadyExistsException, UserEmailAlreadyExistsException {

        final IDirectory directory = authenticationService.findDirectoryFor(user.getDirectory());
        boolean passwordRequired = true;
        if (directory == null) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.create.no.directory", user.getDirectory()));
        }
        passwordRequired = directory.isUserCreatable() || directory.isUserUpdatable();
        final boolean sendPasswordResetEmail = user.isNotify();

        if (StringUtils.isEmpty(user.getUsername())) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.create.no.username"));
        }

        if (passwordRequired && !sendPasswordResetEmail && StringUtils.isEmpty(user.getPassword())) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.create.no.password"));
        }

        if (StringUtils.isEmpty(user.getDisplayName())) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.create.no.displayname"));
        }

        if (StringUtils.isEmpty(user.getEmailAddress())) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.service.user.create.no.email"));
        }

        if (sendPasswordResetEmail) {
            try {
                return ResponseFactory.status(Status.CREATED)
                        .entity(userAdminService.createUserWithGeneratedPassword(user.getUsername(),
                            user.getDisplayName(),
                            user.getEmailAddress(),
                            user.getDirectory()))
                        .build();
            } catch (final NoMailHostConfigurationException e) {
                throw new IntegrityException(i18nService.createKeyedMessage("app.service.user.create.no.mail.server"));
            }
        } else {
            return ResponseFactory.status(Status.CREATED)
                    .entity(userAdminService.createUser(user.getUsername(),
                        user.getPassword(),
                        user.getDisplayName(),
                        user.getEmailAddress(),
                        user.getDirectory(),
                        user.isAddToDefaultGroup()))
                    .build();
        }

    }

    /**
     * Update a user's details.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param user
     *             a user to update
     * @return Returns no content 204.
     */
    @PUT
    @Path("users")
    @Timed
    @Operation(summary = "Updates a user",
            responses = { @ApiResponse(responseCode = "202", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "409", description = "the email address already use by another user"),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an unexpected condition "
                                    + "which prevented it from fulfilling the request.") })
    @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
    public Response updateUser(
        @Parameter(description = "a user to update", required = true) @Valid final UserUpdate user) {
        try {
            this.userAdminService.updateUser(user);
        } catch (final UserEmailAlreadyExistsException e) {
            return ResponseFactory.badRequest(e.getMessage()).build();
        }
        return ResponseFactory.noContent().build();
    }

    /**
     * Rename a user.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param rename
     *               a rename request.
     * @return Returns 200 and the {@link UserRequest details} for the renamed user.
     */
    @POST
    @Path("users/rename")
    @Timed
    @Operation(summary = "Rename a user.",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserRequest.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "404", description = "The specified user does not exist.") })
    @RolesAllowed({ ApplicationConstants.Authorities.ADMIN })
    public Response renameUser(final @Valid UserRenameRequest rename) {
        try {
            final UserRequest user = userAdminService.renameUser(rename.getName(), rename.getNewName());
            return ResponseFactory.ok(user).build();
        } catch (final AuthorisationException e) {
            throw new AuthorisationException(i18nService.createKeyedMessage("app.rest.user.rename.notAuthorised"));
        }
    }

    /**
     * <p>
     * Deletes the specified user, removing them from the system. This also removes any permissions that may have been
     * granted to the user.
     * </p>
     * <p>
     * A user may not delete themselves, and a user with <strong>ADMIN</strong> permissions may not delete a user with
     * <strong>SYS_ADMIN</strong>permissions.
     * </p>
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param username
     *                 the user name identifying the user to delete
     * @return Returns 200 and the {@link UserRequest details} for the deleted user
     */
    @DELETE
    @Path("users")
    @Timed
    @Operation(summary = "delelete a user",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserRequest.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "401",
                            description = "The authenticated user does not have the ADMIN permission."),
                    @ApiResponse(responseCode = "403",
                            description = "The action was disallowed as the authenticated user has a lower"
                                    + " permission level than the user being deleted."),
                    @ApiResponse(responseCode = "404", description = "The specified user does not exist."),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response deleteUser(@QueryParam("name") final String username) {
        if (StringUtils.isEmpty(username)) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.delete.no.username"));
        }

        final UserRequest user = userAdminService.deleteUser(username);
        return ResponseFactory.ok(user).build();
    }

    /**
     * Activate or deactivate user.
     * <p>
     * A user may not deactivate themselves, and a user with <strong>ADMIN</strong> permissions may not
     * deactivate/activate a user with <strong>SYS_ADMIN</strong>permissions.
     * </p>
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param username
     *                  the user name to activate (can not be empty or {@code null}).
     * @param activated
     *                  if {@code true} activate the user, otherwise deactivate.
     * @return Returns 202 and no content.
     */
    @POST
    @Path("users/activate")
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Activates/deactivates a user",
            responses = { @ApiResponse(responseCode = "202", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "404", description = "The specified user does not exist."),
                    @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    public Response activateUser(
        @Parameter(required = true, description = "user name") @QueryParam("username") final String username,
        @Parameter(required = true,
                description = "activated (bool)") @QueryParam("activated") final boolean activated) {
        if (StringUtils.isEmpty(username)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.user.name"));
        }
        this.userAdminService.activateUser(username, activated);

        return ResponseFactory.accepted().build();
    }

    /**
     * Update a user's password.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource, and may not update
     * the password of a user with greater permissions than themselves.
     * </p>
     *
     * @param update
     *               password update request.
     * @return Returns no content 204.
     */
    @PUT
    @Path("users/credentials")
    @Timed
    @Operation(summary = "Updates a user password",
            responses = { @ApiResponse(responseCode = "204", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "404", description = "The specified user does not exist."),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response updateUserPassword(@Valid final AdminPasswordUpdateRequest update) {

        try {
            userAdminService.updatePassword(update.getName(), update.getPassword());
            return ResponseFactory.noContent().build();
        } catch (final AuthorisationException e) {
            throw new AuthorisationException(i18nService.createKeyedMessage("app.rest.user.update.notAuthorised"));
        }
    }

    /**
     * Create a new group.
     * <p>
     * The authenticated user must have <strong>ADMIN</strong> permission or higher to call this resource.
     * </p>
     *
     * @param group
     *              the group to create.
     * @return Returns 200 and the {@link GroupRequest details} for the created group
     */
    @POST
    @Path("groups")
    @Timed
    @Operation(summary = "create a group",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = GroupRequest.class))),
                    @ApiResponse(responseCode = "201", description = "The group has been created."),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "401",
                            description = "TThe currently authenticated user is not an administrator."),
                    @ApiResponse(responseCode = "409", description = "A group with this name already exists"),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response createGroup(@Valid final CreateGroupRequest group) {
        if (StringUtils.isEmpty(group.getName()) || group.getName().length() > 255) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.group.name"));
        }

        return ResponseFactory.status(Status.CREATED).entity(userAdminService.createGroup(group.getName())).build();
    }

    /**
     * Add a groups for a user directory.
     * <p>
     * The authenticated user must have <strong>ADMIN</strong> permission or higher to call this resource.
     * </p>
     *
     * @param group
     *              list of groups from user directory to add.
     * @return Returns 200 and the {@link GroupRequest details} for the created group
     */
    @POST
    @Path("/directory/groups")
    @Timed
    @Operation(summary = "add a group",
            responses = { @ApiResponse(responseCode = "200", description = "The group has been added."),
                    @ApiResponse(responseCode = "400", description = "The request was malformed."),
                    @ApiResponse(responseCode = "401",
                            description = "TThe currently authenticated user is not an administrator."),
                    @ApiResponse(responseCode = "409", description = "A group with this name already exists"),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)

    public Response addGroups(@Valid final AddGroupRequest group) {

        userAdminService.addGroups(group.getDirectory(), group.getGroups());
        return ResponseFactory.ok().build();
    }

    /**
     * Deletes the specified group, removing them from the system. This also removes any permissions that may have been
     * granted to the group.
     * <p>
     * A user may not delete the last group that is granting them administrative permissions, or a group with greater
     * permissions than themselves.
     * </p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     *
     * @param groupName
     *                  the name identifying the group to delete
     * @return Returns 200 and the {@link GroupRequest details} for the deleted group
     */
    @DELETE
    @Path("groups")
    @Timed
    @Operation(summary = "delete a group", responses = {
            @ApiResponse(responseCode = "204", description = "The request has succeeded"),
            @ApiResponse(responseCode = "400", description = "The request was malformed."),
            @ApiResponse(responseCode = "401",
                    description = "The authenticated user does not have the ADMIN permission"),
            @ApiResponse(responseCode = "403",
                    description = "The action was disallowed as the authenticated user has a lower permission"
                            + " level than the group being deleted."),
            @ApiResponse(responseCode = "404", description = "The specified group does not exist."),
            @ApiResponse(responseCode = "409",
                    description = "The action was disallowed as it would lower the authenticated user's permission level."),
            @ApiResponse(responseCode = "500",
                    description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response deleteGroup(@QueryParam("name") final String groupName) {
        if (StringUtils.isEmpty(groupName)) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.user.delete.no.groupname"));
        }

        final GroupRequest group = userAdminService.deleteGroup(groupName);
        return ResponseFactory.ok(group).build();
    }

    /**
     * Add multiple users to a group.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param data
     *             group and users request.
     * @return Returns 202 and no content.
     */
    @POST
    @Path("groups/add-users")
    @Timed
    @Operation(summary = "add users to a group",
            responses = { @ApiResponse(responseCode = "202", description = "All the users were added to the group"),
                    @ApiResponse(responseCode = "401",
                            description = "The authenticated user does not have the ADMIN permission"),
                    @ApiResponse(responseCode = "403",
                            description = "The action was disallowed as the authenticated user has a lower permission"
                                    + " level than the group being deleted."),
                    @ApiResponse(responseCode = "404", description = "The specified group or users do not exist."),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response addUsersToGroup(final GroupAndUsersRequest data) {
        userAdminService.addMembersToGroup(data.getGroup(), data.getUsers());
        return ResponseFactory.accepted().build();
    }

    /**
     * Add a user to a group. This is very similar to <code>groups/add-user</code>, but with the <em>context</em> and
     * <em>itemName</em> attributes of the supplied request entity reversed. On the face of it this may appear
     * redundant, but it facilitates a specific UI component.
     * <p>
     * In the request entity, the <em>context</em> attribute is the user and the <em>itemName</em> is the group.
     * </p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     *
     * @param groupName
     *                  a group name.
     * @param username
     *                  a user name.
     * @return Returns 202 and no content.
     */
    @POST
    @Path("groups/add-user")
    @Timed
    @Operation(summary = "add user to a group",
            responses = { @ApiResponse(responseCode = "202", description = "The request has succeeded"),
                    @ApiResponse(responseCode = "401",
                            description = "The authenticated user does not have the ADMIN permission"),
                    @ApiResponse(responseCode = "403",
                            description = "The action was disallowed as the authenticated user has a lower permission"
                                    + " level than the group being deleted."),
                    @ApiResponse(responseCode = "404", description = "The specified user or group does not exist."),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response addUserToGroup(@QueryParam("groupname") final String groupName,
        @QueryParam("username") final String username) {
        userAdminService.addUserToGroups(username, Collections.singleton(groupName));
        return ResponseFactory.accepted().build();
    }

    /**
     * Add a user to one or more groups.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @return Returns 202 and no content.
     */
    @POST
    @Path("users/add-groups")
    @Timed
    @Operation(summary = "add user to one or more groups",
            responses = { @ApiResponse(responseCode = "202", description = "he user was added to all the groups"),
                    @ApiResponse(responseCode = "401",
                            description = "The authenticated user does not have the ADMIN permission"),
                    @ApiResponse(responseCode = "403",
                            description = "The action was disallowed as the authenticated user has a lower permission"
                                    + " level than the group being deleted."),
                    @ApiResponse(responseCode = "404", description = "The specified user or groups do not exist."),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)

    public Response addUserToGroups(final UserAndGroupsRequest data) {
        userAdminService.addUserToGroups(data.getUser(), data.getGroups());
        return ResponseFactory.accepted().build();
    }

    /**
     * Remove a user from a group.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     * In the request entity, the <em>context</em> attribute is the group and the <em>itemName</em> is the user.
     *
     * @param groupName
     *                  a group name.
     * @param username
     *                  a user name.
     * @return Returns 202 and no content.
     */
    @POST
    @Path("groups/remove-user")
    @Timed
    @Operation(summary = "remove user to a group",
            responses = { @ApiResponse(responseCode = "202", description = "The user was removed from the group."),
                    @ApiResponse(responseCode = "401",
                            description = "The authenticated user does not have the ADMIN permission"),
                    @ApiResponse(responseCode = "403",
                            description = "The action was disallowed as the authenticated user has a lower permission"
                                    + " level than the group being deleted."),
                    @ApiResponse(responseCode = "404", description = "The specified user or group does not exist."),
                    @ApiResponse(responseCode = "500",
                            description = "The server encountered an  unexpected condition"
                                    + " which prevented it from fulfilling the request.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response removeUserFromGroup(@QueryParam("groupname") final String groupName,
        @QueryParam("username") final String username) {
        userAdminService.removeUserFromGroup(groupName, username);
        return ResponseFactory.accepted().build();
    }

    /**
     * Gets the highest global permission for a user.
     * <p>
     * The authenticated user must have the <strong>ADMIN</strong> permission to call this resource.
     * </p>
     *
     * @param username
     *                 the user which should be used to locate groups
     * @return Returns the requested permission for a user.
     */
    @GET
    @Path("users/highest-permission")
    @Timed
    @Operation(summary = "Gets the highest global permission for a user.",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = GroupRequest.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded."),
                    @ApiResponse(responseCode = "404", description = "The specified user has not permission") })
    @PreAuthorize("isAuthenticated()")
    public Response getHighestGlobalPermission(@QueryParam("username") final String username) {
        if (StringUtils.isEmpty(username)) {
            throw new ArgumentValidationException(i18nService.createKeyedMessage("app.bad.user.name"));
        }
        final Permission permission = permissionService.getHighestGlobalPermission(username);
        if (permission == null) {
            return ResponseFactory.notFound().build();
        }

        return ResponseFactory.ok(PermissionRequest.forPermission(permission, i18nService)).build();
    }

    /**
     * @return Returns the requested list of available user directories.
     */
    @GET
    @Path("directories")
    @Timed
    @Operation(summary = "Gets list of available user directories.",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = NameValuePair.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded.") })
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    public Response getDirectories() {
        final List<NameValuePair<String, String>> list = Lists.newArrayList();
        this.authenticationProviderService.getAuthenticationProviders()
                .forEach(provider -> list.add(NameValuePair.create(provider.getSupportedDirectory().name(),
                    provider.getSupportedDirectory().getDescription())));

        return ResponseFactory.ok(list).build();
    }

}
