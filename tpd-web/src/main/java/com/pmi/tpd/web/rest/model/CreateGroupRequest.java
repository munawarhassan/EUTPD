package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * POJO to create a new group from JSON or xml representation.
 *
 * @author Christophe Friederich
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "CreateGroup", description = "Contains information to create a new group")
public class CreateGroupRequest {

  /** */
  @NotNull
  @Size(min = 1, max = 255)
  @JsonProperty(value = "name", required = true)
  private String name;

}
