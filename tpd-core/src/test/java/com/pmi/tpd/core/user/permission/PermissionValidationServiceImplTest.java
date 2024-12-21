package com.pmi.tpd.core.user.permission;

import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

/**
 * Picks up tests from AbstractServiceTest
 */
public class PermissionValidationServiceImplTest extends AbstractServiceTest {

    public PermissionValidationServiceImplTest() {
        super(PermissionValidationServiceImpl.class, IPermissionValidationService.class);
    }
}
