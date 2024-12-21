package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * POJO allowing created system admin user.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "Account", description = "Contains information of System Administrator account")
public class UserAdminRequest {

  /** */
  @Schema(required = true)
  @JsonProperty("login")
  private String login;

  /** */
  @Schema(required = true)
  @JsonProperty("password")
  private String password;

  /** */
  @JsonProperty("email")
  @Schema(required = true)
  private String email;

}
