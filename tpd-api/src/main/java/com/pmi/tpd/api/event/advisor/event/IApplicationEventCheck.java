package com.pmi.tpd.api.event.advisor.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.IEventAdvisorAccessor;

/**
 * A check that is run every time the application is started.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IApplicationEventCheck<CONTEXT> extends IEventCheck {

  /**
   * @param advisorAccessor
   * @param context
   */
  void check(@Nonnull IEventAdvisorAccessor advisorAccessor, @Nonnull CONTEXT context);
}
