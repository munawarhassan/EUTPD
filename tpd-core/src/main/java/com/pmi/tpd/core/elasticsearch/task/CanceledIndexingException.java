package com.pmi.tpd.core.elasticsearch.task;

import com.pmi.tpd.api.exec.IRunnableTaskCanceled;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class CanceledIndexingException extends IndexingException implements IRunnableTaskCanceled {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CanceledIndexingException(final KeyedMessage message) {
        super(message);
    }
}
