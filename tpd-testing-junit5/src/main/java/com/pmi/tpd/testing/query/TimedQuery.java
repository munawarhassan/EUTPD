package com.pmi.tpd.testing.query;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface TimedQuery<T> extends Callable<T>, PollingQuery {

    /**
     * Evaluate this query by a timeout deemed default by this query.
     *
     * @return expected value of <tt>T</tt>, or any suitable value, if the expected value was not returned before the
     *         default timeout expired
     * @see #defaultTimeout()
     */
    T byDefaultTimeout();

    /**
     * Evaluate this query by given timeout. That is, return the expected <tt>T</tt> as soon as the query evaluates to
     * the expected value, otherwise perform any appropriate operation when the <tt>timeout</tt> expires (e.g. return
     * real value, <code>null</code>, or throw exception
     *
     * @param timeoutInMillis
     *            timeout in milliseconds (must be greater than 0)
     * @return expected value of <tt>T</tt>, or any suitable value, if the expected value was not returned before
     *         <tt>timeout</tt> expired
     */
    T by(long timeoutInMillis);

    /**
     * Evaluate this query by given timeout. That is, return the expected <tt>T</tt> as soon as the query evaluates to
     * the expected value, otherwise perform any appropriate operation when the <tt>timeout</tt> expires (e.g. return
     * real value, <code>null</code>, or throw exception
     *
     * @param timeout
     *            timeout (must be greater than 0)
     * @param unit
     *            the unit that the timeout is in
     * @return expected value of <tt>T</tt>, or any suitable value, if the expected value was not returned before
     *         <tt>timeout</tt> expired
     */
    T by(long timeout, TimeUnit unit);

    /**
     * Evaluate this query immediately.
     *
     * @return current evaluation of the underlying query.
     */
    T now();
}