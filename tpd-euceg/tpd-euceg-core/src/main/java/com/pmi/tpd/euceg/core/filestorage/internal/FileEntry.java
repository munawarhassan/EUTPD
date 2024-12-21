package com.pmi.tpd.euceg.core.filestorage.internal;

import javax.annotation.concurrent.Immutable;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
@Getter
@Builder
@Jacksonized
public final class FileEntry {

  /** */
  private final String name;

}