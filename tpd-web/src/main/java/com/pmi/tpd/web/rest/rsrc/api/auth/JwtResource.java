package com.pmi.tpd.web.rest.rsrc.api.auth;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.JwtAuthenticationToken;
import com.pmi.tpd.web.rest.model.JwtToken;
import com.pmi.tpd.web.security.jwt.JwtConfigurer;
import com.pmi.tpd.web.security.jwt.JwtTokenProvider;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author devacfr
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/auth")
@Tag(description = "Endpoint for jwt Authentication", name = "authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JwtResource {

  /** */
  private final JwtTokenProvider tokenProvider;

  /** */
  private final IAuthenticationService authenticationService;

  @Inject
  public JwtResource(final JwtTokenProvider tokenProvider, final IAuthenticationService authenticationService) {
    this.tokenProvider = tokenProvider;
    this.authenticationService = authenticationService;
  }

  /**
   * @param auth
   * @return
   */
  @PermitAll
  @POST
  @Path("authenticate")
  @Timed
  public Response authorize(@Valid final JwtAuthenticationToken auth) {

    final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        auth.getUsername(), auth.getPassword());

    final Authentication authentication = this.authenticationService.authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    final boolean rememberMe = auth.isRememberMe();
    final String jwt = tokenProvider.createToken(authentication, rememberMe);
    return ResponseFactory.ok(new JwtToken(jwt))
        .header(JwtConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt)
        .build();
  }

}