package com.pmi.tpd.euceg.backend.core.domibus.api;

import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.ErrorLogResponse;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.MessageLogResponse;

/**
 * @author christophe friederich
 * @since 3.0.0
 */
public interface IClientRest extends IStartable, IShutdown {

  /**
   * @return
   */
  boolean isStarted();

  /**
   * @param logLevel
   */
  void setLogLevel(@Nonnull Level logLevel);

  /**
   * @param backendProperties
   */
  void setBackendProperties(BackendProperties backendProperties);

  /**
   * @return
   */
  BackendProperties getBackendProperties();

  /**
   * @return
   */
  boolean isAuthenticated();

  /**
   * Check if the domibus is available and is running.
   *
   * @param url
   *            url of server
   * @throws Exception
   *                   if healthCheck failed.
   */
  void healthCheck(@Nonnull String url) throws Exception;

  /**
   * @param conversationId
   * @param pageable
   * @return
   */
  MessageLogResponse getMessageLogs(@Nonnull String conversationId, @Nonnull Pageable pageable);

  /**
   * @return
   */
  Response getCurrentPMode();

  /**
   * @param messageId
   * @param pageable
   * @return
   */
  ErrorLogResponse getErrorLogs(@Nonnull String messageId, @Nonnull Pageable pageable);

}