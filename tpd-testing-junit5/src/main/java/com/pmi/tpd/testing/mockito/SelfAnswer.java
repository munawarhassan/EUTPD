package com.pmi.tpd.testing.mockito;

import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Answer that returns the mock itself when the return type is compatible with the mock type.
 */
public class SelfAnswer implements Answer<Object> {

    private final Answer<Object> defaultAnswer;

    private final Class<?> mockType;

    public SelfAnswer(final Class<?> mockType, final Answer<Object> defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
        this.mockType = mockType;
    }

    public SelfAnswer(final Class<?> mockType) {
        this(mockType, new ReturnsEmptyValues());
    }

    @Override
    public Object answer(final InvocationOnMock invocation) throws Throwable {
        if (invocation.getMethod().getReturnType().isAssignableFrom(mockType)) {
            return invocation.getMock();
        } else {
            return defaultAnswer.answer(invocation);
        }
    }
}
