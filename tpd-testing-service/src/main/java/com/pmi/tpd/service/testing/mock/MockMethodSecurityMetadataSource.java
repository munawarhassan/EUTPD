package com.pmi.tpd.service.testing.mock;

import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PrePostAnnotationSecurityMetadataSource;

public class MockMethodSecurityMetadataSource extends PrePostAnnotationSecurityMetadataSource {

    public MockMethodSecurityMetadataSource(final MethodSecurityExpressionHandler handler) {
        super(new MockExpressionBasedAnnotationAttributeFactory(handler));
    }
}
