package com.pmi.tpd.core.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * A {@code TestExecutionListener} implementation which chains one or more other listeners, applying them in configured
 * order for methods before the test is executed and in reverse order for methods executed after the test.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class ChainTestExecutionListener implements TestExecutionListener {

    // Both collections contain the same listeners, with the after list in reverse order from the before list. This is
    // necessary to honor the stack-like contract of listeners.
    private List<TestExecutionListener> afterListeners;

    private List<TestExecutionListener> beforeListeners;

    public ChainTestExecutionListener() {
    }

    public ChainTestExecutionListener(final Collection<TestExecutionListener> listeners) {
        setTestExecutionListeners(listeners);
    }

    @Override
    public void afterTestClass(final TestContext testContext) throws Exception {
        for (final TestExecutionListener listener : afterListeners) {
            listener.afterTestClass(testContext);
        }
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        for (final TestExecutionListener listener : afterListeners) {
            listener.afterTestMethod(testContext);
        }
    }

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        for (final TestExecutionListener listener : beforeListeners) {
            listener.beforeTestClass(testContext);
        }
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        for (final TestExecutionListener listener : beforeListeners) {
            listener.beforeTestMethod(testContext);
        }
    }

    @Override
    public void prepareTestInstance(final TestContext testContext) throws Exception {
        for (final TestExecutionListener listener : beforeListeners) {
            listener.prepareTestInstance(testContext);
        }
    }

    public void setTestExecutionListener(final TestExecutionListener listener) {
        setTestExecutionListeners(Collections.singleton(listener));
    }

    public void setTestExecutionListeners(final Collection<TestExecutionListener> listeners) {
        beforeListeners = new ArrayList<TestExecutionListener>(listeners);

        // Reverse the listener order for the after listeners so they are unwound like a stack
        afterListeners = new ArrayList<TestExecutionListener>(listeners);
        Collections.reverse(afterListeners);
    }
}
