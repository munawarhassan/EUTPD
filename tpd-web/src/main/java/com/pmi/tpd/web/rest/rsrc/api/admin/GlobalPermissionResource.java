package com.pmi.tpd.web.rest.rsrc.api.admin;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.SetPermissionRequest;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/admin/permissions")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Tag(description = "Endpoint for Global permission administration", name = "administration")
public class GlobalPermissionResource extends AbstractPermissionResource {

  @Inject
  public GlobalPermissionResource(final IPermissionAdminService permissionAdminService,
      final IUserService userService, final I18nService i18nService) {
    super(i18nService, permissionAdminService, userService);
  }

  /**
   * Retrieve a page of groups that have been granted at least one global
   * permission.
   * <p>
   * The authenticated user must have <strong>ADMIN</strong> permission or higher
   * to call this resource.
   * </p>
   *
   * @param page
   *               zero-based page index.
   * @param size
   *               the size of the page to be returned.
   * @param sort
   *               the property to sort (can be {@literal null}).
   * @param filter
   *               if specified only group names containing the supplied string
   *               will be returned.
   * @return Returns the requested page of
   *         {@link com.pmi.tpd.core.user.permission.IPermittedGroup}, which may
   *         be
   *         empty but never null.
   */
  @GET
  @Path("groups")
  @Timed
  @Operation(summary = "Retrieve a page of groups that have been granted at least one global permission.", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
      @ApiResponse(responseCode = "200", description = "Returns the requested page of PermittedGroup, which may be empty but never null."),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator.")
  })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response getGroupsWithAnyPermission(
      @Parameter(description = "page to load (zero-based page index)", required = false) @QueryParam("page") @DefaultValue("0") final int page,
      @Parameter(description = "size of page", required = false) @QueryParam("size") @DefaultValue("20") final int size,
      @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
      @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter) {

    final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
    return ResponseFactory.ok(permissionAdminService.findGroupsWithGlobalPermission(null, pageRequest)).build();
  }

  /**
   * Retrieve a page of groups that have no granted global permissions.
   * <p>
   * The authenticated user must have <strong>ADMIN</strong> permission or higher
   * to call this resource.
   * </p>
   *
   * @param page
   *               zero-based page index.
   * @param size
   *               the size of the page to be returned.
   * @param sort
   *               the property to sort (can be {@literal null}).
   * @param filter
   *               if specified only group names containing the supplied string
   *               will be returned.
   * @return Returns the requested page of group names, which may be empty but
   *         never null.
   */
  @GET
  @Path("groups/none")
  @Timed
  @Operation(summary = "Retrieve a page of groups that have no granted global permissions.", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
      @ApiResponse(responseCode = "200", description = "Returns the requested page of group names, which may be empty but never null."),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator.")
  })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response getGroupsWithoutAnyPermission(
      @Parameter(description = "page to load (zero-based page index)", required = false) @QueryParam("page") @DefaultValue("0") final int page,
      @Parameter(description = "size of page", required = false) @QueryParam("size") @DefaultValue("20") final int size,
      @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
      @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter) {

    final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
    return ResponseFactory.ok(permissionAdminService.findGroupsWithoutGlobalPermission(pageRequest)).build();
  }

  /**
   * Retrieve a page of users that have been granted at least one global
   * permission.
   * <p>
   * The authenticated user must have <strong>ADMIN</strong> permission or higher
   * to call this resource.
   * </p>
   *
   * @param page
   *               zero-based page index.
   * @param size
   *               the size of the page to be returned.
   * @param sort
   *               the property to sort (can be {@literal null}).
   * @param filter
   *               if specified only user names containing the supplied string
   *               will be returned.
   * @return Returns the requested page of
   *         {@link com.pmi.tpd.core.user.permission.IPermittedUser}, which may be
   *         empty
   *         but never null.
   */
  @GET
  @Path("users")
  @Timed
  @Operation(summary = "Retrieve a page of users that have been granted at least one global permission.", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
      @ApiResponse(responseCode = "200", description = "Returns the requested page of PermittedUser, which may be empty but never null."),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator.")
  })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response getUsersWithAnyPermission(
      @Parameter(description = "page to load (zero-based page index)", required = false) @QueryParam("page") @DefaultValue("0") final int page,
      @Parameter(description = "size of page", required = false) @QueryParam("size") @DefaultValue("20") final int size,
      @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
      @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter) {

    final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
    return ResponseFactory.ok(permissionAdminService.findUsersWithGlobalPermission(null, pageRequest)).build();
  }

  /**
   * Retrieve a page of users that have no granted global permissions.
   * <p>
   * The authenticated user must have <strong>ADMIN</strong> permission or higher
   * to call this resource.
   * </p>
   *
   * @param page
   *               zero-based page index.
   * @param size
   *               the size of the page to be returned.
   * @param sort
   *               the property to sort (can be {@literal null}).
   * @param filter
   *               if specified only user names containing the supplied string
   *               will be returned.
   * @return Returns the requested page of {@link com.pmi.tpd.api.user.IUser},
   *         which may be empty but never null.
   */
  @GET
  @Path("users/none")
  @Timed
  @Operation(summary = "Retrieve a page of users that have no granted global permissions.", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
      @ApiResponse(responseCode = "200", description = "Returns the requested page of IUser, which may be empty but never null."),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator.")
  })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response getUsersWithoutAnyPermission(
      @Parameter(description = "page to load (zero-based page index)", required = false) @QueryParam("page") @DefaultValue("0") final int page,
      @Parameter(description = "size of page", required = false) @QueryParam("size") @DefaultValue("20") final int size,
      @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
      @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter) {

    final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
    return ResponseFactory.ok(permissionAdminService.findUsersWithoutGlobalPermission(pageRequest)).build();
  }

  /**
   * Promote or demote a user's global permission level. Available global
   * permissions are:
   * <ul>
   * <li>USER</li>
   * <li>ADMIN</li>
   * <li>SYS_ADMIN</li>
   * </ul>
   * <p>
   * The authenticated user must have:
   * </p>
   * <ul>
   * <li><strong>ADMIN</strong> permission or higher; and</li>
   * <li>the permission they are attempting to grant or higher; and</li>
   * <li>greater or equal permissions than the current permission level of the
   * group (a user may not demote the
   * permission level of a group with higher permissions than them)</li>
   * </ul>
   * to call this resource. In addition, a user may not demote a group's
   * permission level if their own permission
   * level would be reduced as a result.
   *
   * @param permissionName
   *                       the permission to grant
   * @param groupNames
   *                       the names of the groups
   * @return Returns 202 and no content.
   */
  @PUT
  @Path("groups")
  @Timed
  @Operation(summary = "Promote or demote a user's global permission level.", responses = {
      @ApiResponse(responseCode = "202", description = "The specified permission was granted to the specified user."),
      @ApiResponse(responseCode = "400", description = "Error validation"),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator or doesn't have"
          + " the specified permission they are attempting to grant."),
      @ApiResponse(responseCode = "404", description = "The specified group does not exist."),
      @ApiResponse(responseCode = "409", description = "The action was disallowed as it would reduce the currently authenticated user's "
          + "permission level or the currently authenticated user has a lower permission "
          + "level than the group they are attempting to modify.")
  })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response setPermissionForGroups(@QueryParam("permission") final String permissionName,
      @QueryParam("name") final Set<String> groupNames) {
    final Permission permission = validatePermission(permissionName, null);
    final Set<String> groups = validateGroups(groupNames, false);

    permissionAdminService
        .setPermission(new SetPermissionRequest.Builder().globalPermission(permission).groups(groups).build());
    return ResponseFactory.status(Status.ACCEPTED).build();
  }

  /**
   * Promote or demote the global permission level of a user. Available global
   * permissions are:
   * <ul>
   * <li>USER</li>
   * <li>ADMIN</li>
   * <li>SYS_ADMIN</li>
   * </ul>
   * <p>
   * The authenticated user must have:
   * </p>
   * <ul>
   * <li><strong>ADMIN</strong> permission or higher; and</li>
   * <li>the permission they are attempting to grant; and</li>
   * <li>greater or equal permissions than the current permission level of the
   * user (a user may not demote the
   * permission level of a user with higher permissions than them)</li>
   * </ul>
   * to call this resource. In addition, a user may not demote their own
   * permission level.
   *
   * @param permissionName
   *                       the permission to grant
   * @param usernames
   *                       the names of the users
   * @return Returns 202 and no content.
   */
  @PUT
  @Path("users")
  @Timed
  @Operation(summary = "Promote or demote the global permission level of a user.", responses = {
      @ApiResponse(responseCode = "400", description = "Error validation"),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator or doesn't have "
          + "the specified permission they are attempting to grant."),
      @ApiResponse(responseCode = "404", description = "The specified user does not exist."),
      @ApiResponse(responseCode = "409", description = "The action was disallowed as it would reduce the currently authenticated user's "
          + "permission level or the currently authenticated user has a lower permission level "
          + "than the user they are attempting to modify.") })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response setPermissionForUsers(@QueryParam("permission") final String permissionName,
      @QueryParam("name") final Set<String> usernames) {
    final Permission permission = validatePermission(permissionName, null);
    final Set<IUser> users = validateUsers(usernames, false);

    permissionAdminService
        .setPermission(new SetPermissionRequest.Builder().globalPermission(permission).users(users).build());
    return ResponseFactory.status(Status.ACCEPTED).build();
  }

  /**
   * Revoke all global permissions for a group.
   * <p>
   * The authenticated user must have:
   * </p>
   * <ul>
   * <li><strong>ADMIN</strong> permission or higher; and</li>
   * <li>greater or equal permissions than the current permission level of the
   * group (a user may not demote the
   * permission level of a group with higher permissions than them)</li>
   * </ul>
   * <p>
   * to call this resource. In addition, a user may not revoke a group's
   * permissions if their own permission level
   * would be reduced as a result.
   * </p>
   *
   * @param groupName
   *                  the name of the group
   * @return Returns 202 and no content.
   */
  @DELETE
  @Path("groups")
  @Timed
  @Operation(summary = "Revoke all global permissions for a group.", responses = {
      @ApiResponse(responseCode = "202", description = "All global permissions were revoked from the group."),
      @ApiResponse(responseCode = "400", description = "Error validation"),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator."),
      @ApiResponse(responseCode = "404", description = "The specified group does not exist."),
      @ApiResponse(responseCode = "409", description = "The action was disallowed as it would reduce the currently authenticated user's"
          + " permission level or the currently authenticated user has a lower permission level"
          + " than the group they are attempting to modify.") })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response revokePermissionsForGroup(@QueryParam("name") String groupName) {
    groupName = validateGroup(groupName, true);

    permissionAdminService.revokeAllGlobalPermissions(groupName);
    return ResponseFactory.status(Status.ACCEPTED).build();
  }

  /**
   * Revoke all global permissions for a user.
   * <p>
   * The authenticated user must have:
   * <ul>
   * <li><strong>ADMIN</strong> permission or higher; and</li>
   * <li>greater or equal permissions than the current permission level of the
   * user (a user may not demote the
   * permission level of a user with higher permissions than them)</li>
   * </ul>
   * to call this resource. In addition, a user may not demote their own
   * permission level.
   *
   * @param username
   *                 the name of the user
   * @return Returns 202 and no content.
   */
  @DELETE
  @Path("users")
  @Timed
  @Operation(summary = "Revoke all global permissions for a user.", responses = {
      @ApiResponse(responseCode = "202", description = "All global permissions were revoked from the user."),
      @ApiResponse(responseCode = "400", description = "Error validation"),
      @ApiResponse(responseCode = "401", description = "The currently authenticated user is not an administrator."),
      @ApiResponse(responseCode = "404", description = "The specified user does not exist."),
      @ApiResponse(responseCode = "409", description = "The action was disallowed as it would reduce the currently authenticated user's"
          + " permission level or the currently authenticated user has a lower permission level"
          + " than the user they are attempting to modify.") })
  @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
  public Response revokePermissionsForUser(@QueryParam("name") final String username) {
    final IUser user = validateUser(username, true);

    permissionAdminService.revokeAllGlobalPermissions(user);
    return ResponseFactory.status(Status.ACCEPTED).build();
  }
}
