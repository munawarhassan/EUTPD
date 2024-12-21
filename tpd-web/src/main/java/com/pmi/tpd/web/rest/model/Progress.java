package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@Getter
@AllArgsConstructor
@Schema(name = "Progress", description = "Contains information on progress action")
@JsonSerialize
public class Progress {

  /** */
  @JsonProperty("progress")
  @Schema(required = true)
  private final IProgress progress;

  /** */
  @JsonProperty("state")
  @Schema(required = true)
  private final LifecycleState state;

}
