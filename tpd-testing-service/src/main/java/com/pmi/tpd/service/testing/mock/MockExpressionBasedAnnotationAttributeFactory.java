package com.pmi.tpd.service.testing.mock;

import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;

public class MockExpressionBasedAnnotationAttributeFactory extends ExpressionBasedAnnotationAttributeFactory {

    public MockExpressionBasedAnnotationAttributeFactory(final MethodSecurityExpressionHandler handler) {
        super(handler);
    }

    private String convertConjunctions(final String expression) {
        if (expression == null) {
            return null;
        }
        /**
         * This is the whole reason for the existence of this class. {@link MockMethodSecurityExpressionHandler} returns
         * false for every expression in the SPEL; this is great until someone does an and, e.g.
         * "isSubmissionAccessible(#request.submission) and isSubmissionAccessible(#request.secondarySubmission)" the
         * second expression is never evaluated as the first is false. 'not's also cause a problem as a true return type
         * is considered a failure. As a result for the test we get rid of them.
         */
        return expression.replace("AND", "or").replace("and", "or").replace("not ", "").replace("NOT ", "");
    }

    @Override
    public PreInvocationAttribute createPreInvocationAttribute(final String preFilterAttribute,
        final String filterObject,
        final String preAuthorizeAttribute) {
        return super.createPreInvocationAttribute(convertConjunctions(preFilterAttribute),
            filterObject,
            convertConjunctions(preAuthorizeAttribute));
    }

    @Override
    public PostInvocationAttribute createPostInvocationAttribute(final String postFilterAttribute,
        final String postAuthorizeAttribute) {
        return super.createPostInvocationAttribute(convertConjunctions(postFilterAttribute),
            convertConjunctions(postAuthorizeAttribute));
    }
}
