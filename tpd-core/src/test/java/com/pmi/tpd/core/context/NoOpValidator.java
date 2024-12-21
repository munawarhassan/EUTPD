package com.pmi.tpd.core.context;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class NoOpValidator implements Validator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(final Object target, final Errors errors) {
    }
}