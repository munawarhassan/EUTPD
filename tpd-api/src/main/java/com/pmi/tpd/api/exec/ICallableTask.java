package com.pmi.tpd.api.exec;

import java.util.concurrent.Callable;

/**
 * Represents task to execution in a thread.
 *
 * @author Christophe Friederich
 * @param <T>
 *            the result type of method <tt>call</tt>
 */
public interface ICallableTask<T> extends Callable<T> {

}
