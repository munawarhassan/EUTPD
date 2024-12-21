package com.pmi.tpd.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class ProductTest {

    @Test
    public void testRetrieveInformation() {
        assertThat(Product.getClusterName(), not(is(emptyOrNullString())));
        assertThat(Product.getFullName(), not(is(emptyOrNullString())));
        assertThat(Product.getName(), not(is(emptyOrNullString())));
        assertThat(Product.getPrefix(), not(is(emptyOrNullString())));
        assertThat(Product.getFirstYearOfRelease(), Matchers.greaterThan(2000));
    }
}
