package com.pmi.tpd.core.euceg;

public interface ISendDeferredSubmissionJob {

    /**
     * Send deferred submission, ie send bulk submission.
     *
     * @see ISubmissionService#bulkSendSubmissions(List)
     */
    void sendDeferredSubmission(int batchSize);
}