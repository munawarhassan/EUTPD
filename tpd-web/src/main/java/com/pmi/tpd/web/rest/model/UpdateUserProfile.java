package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonSerialize
public class UpdateUserProfile {

  @Pattern(regexp = "^[a-zA-Z0-9]*$")
  @NotNull
  @Size(min = 1, max = 20)
  private final String username;

  @NotNull
  @Size(max = 250)
  private final String displayName;

  @NotNull
  @Email
  @Size(min = 1, max = 255)
  private final String email;

  @Builder
  public UpdateUserProfile(@JsonProperty(value = "username", required = true) final String username,
      @JsonProperty(value = "displayName", required = true) final String displayName,
      @JsonProperty(value = "email", required = true) final String email) {
    this.username = username;
    this.displayName = displayName;
    this.email = email;
  }

}
