package com.pmi.tpd.euceg.core.task;

import com.pmi.tpd.api.exec.IRunnableTaskCanceled;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.euceg.api.EucegException;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class CanceledEucegTaskException extends EucegException implements IRunnableTaskCanceled {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CanceledEucegTaskException(final KeyedMessage message) {
        super(message);
    }
}
