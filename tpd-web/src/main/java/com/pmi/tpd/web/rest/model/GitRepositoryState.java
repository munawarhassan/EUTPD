package com.pmi.tpd.web.rest.model;

import java.util.Properties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * This information is supplied by plugin -
 * <b>pl.project13.maven.git-commit-id-plugin</b>.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "RepositorySate", description = "Contains the current scm state.")
public class GitRepositoryState {

  /** */
  private String branch;

  /** */
  private String dirty;

  /** */
  private String tags;

  /** */
  private String describe;

  /** */
  private String shortDescribe;

  /** */
  private String commitId;

  /** */
  private String commitIdAbbrev;

  /** */
  private String buildUserName;

  /** */
  private String buildUserEmail;

  /** */
  private String buildTime;

  /** */
  private String commitUserName;

  /** */
  private String commitUserEmail;

  /** */
  private String commitMessageFull;

  /** */
  private String commitMessageShort;

  /** */
  private String commitTime;

  /**
   * Default constructor.
   *
   * @param properties
   *                   properties used to fill.
   */
  public GitRepositoryState(final Properties properties) {
    this.branch = properties.getProperty("scm.branch");
    this.dirty = properties.getProperty("scm.dirty");
    this.tags = properties.getProperty("scm.tags");
    this.describe = properties.getProperty("scm.commit.version");
    this.shortDescribe = properties.getProperty("scm.commit.short-version");
    this.commitId = properties.getProperty("scm.commit.id");
    this.commitIdAbbrev = properties.getProperty("scm.commit.id.abbrev");
    this.buildUserName = properties.getProperty("scm.build.user.name");
    this.buildUserEmail = properties.getProperty("scm.build.user.email");
    this.buildTime = properties.getProperty("scm.build.time");
    this.commitUserName = properties.getProperty("scm.commit.user.name");
    this.commitUserEmail = properties.getProperty("scm.commit.user.email");
    this.commitMessageShort = properties.getProperty("scm.commit.message.short");
    this.commitMessageFull = properties.getProperty("scm.commit.message.full");
    this.commitTime = properties.getProperty("scm.commit.time");
  }

}
