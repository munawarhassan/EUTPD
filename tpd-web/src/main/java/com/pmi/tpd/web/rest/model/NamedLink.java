package com.pmi.tpd.web.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * An optionally-named link relating to an entity. A JSON representation of
 * {@link NamedLink}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "NamedLink", description = "An optionally-named link relating to an entity.")
public class NamedLink {

  private String href;

  private String name;

}