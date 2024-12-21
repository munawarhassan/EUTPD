package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "CreateAttachment", description = "Contains information to create a new attachment")
public class CreateAttachmentRequest {

  /** */
  @JsonProperty(value = "filename", required = true)
  @NonNull
  private String filename;

  /** */
  @JsonProperty(value = "contentType", required = true)
  @NonNull
  private String contentType;

  /** */
  @JsonProperty(value = "confidential", required = true)
  private boolean confidential;

}
