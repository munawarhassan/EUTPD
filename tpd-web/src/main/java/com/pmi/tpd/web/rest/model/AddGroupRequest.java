package com.pmi.tpd.web.rest.model;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pmi.tpd.api.user.UserDirectory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * POJO to add a external group from JSON or xml representation.
 *
 * @author Christophe Friederich
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "AddGroup", description = "Contains information to add a external group")
public class AddGroupRequest {

  /** */
  @NotNull
  @JsonProperty(value = "groups", required = true)
  private Set<String> groups;

  /** */
  @NotNull
  @JsonProperty(value = "directory", required = true)
  private UserDirectory directory;

}
