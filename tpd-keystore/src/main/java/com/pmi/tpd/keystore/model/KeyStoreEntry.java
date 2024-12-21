package com.pmi.tpd.keystore.model;

import javax.annotation.concurrent.Immutable;

import org.joda.time.DateTime;

import com.querydsl.core.annotations.QueryEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
@QueryEntity
@Getter
@Builder
@Jacksonized
public final class KeyStoreEntry {

  /** */
  private final String alias;

  /** */
  private final EntryType type;

  /** */
  private final String algorithm;

  /** */
  private final int keySize;

  /** */
  private final DateTime expiredDate;

  /** */
  private final DateTime lastModified;

  /** {@code false} if not yet valid */
  private final boolean valid;

  /** */
  private final boolean expired;

}
