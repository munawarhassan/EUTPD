package com.pmi.tpd.api.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonDeserialize(builder = UserSettings.UserSettingsBuilder.class)
@Schema(name = "UserSettings", description = "Contains user settings information")
@JsonSerialize
public class UserSettings {

  private String langKey;

  private AvatarSourceType avatarSource;

  @JsonPOJOBuilder(withPrefix = "")
  public static class UserSettingsBuilder {

  }
}
