package com.pmi.tpd.api.user;

import java.net.URI;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
@JsonDeserialize(builder = UserProfileRequest.class)
@Schema(name = "UserProfile", description = "Contains user profile information")
@JsonSerialize
public class UserProfileRequest {

  private String username;

  private String displayName;

  private String email;

  private String contactPhone;

  private String officeLocation;

  private URI avatarUrl;

  private boolean readOnly;

  private UserSettings settings;

  public static UserProfileRequestBuilder from(final IUserProfile userProfile) {
    return UserProfileRequest.builder()
        .displayName(userProfile.getDisplayName())
        .email(userProfile.getEmail())
        .username(userProfile.getUsername())
        .avatarUrl(userProfile.getProfilePictureUri());
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class UserProfileRequestBuilder {

  }
}
