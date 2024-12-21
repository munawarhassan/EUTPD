package com.pmi.tpd.euceg.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.euceg.core.BulkRequest.Filter;

public class BulkRequestTest {

    @Test
    public void shouldDeserialize() throws JsonMappingException, JsonProcessingException {
        final String json = "{\"action\":\"sendSubmission\",\"filters\":{\"productType\":[{\"values\":[\"TOBACCO\"],\"op\":\"eq\"}],\"status\":[{\"values\":[\"VALID\"],\"op\":\"eq\"}],\"productNumber\":[{\"values\":[\"POM.010118\", \"POM.010111\", \"POM.010104\"],\"op\":\"in\"}]},\"data\":{\"overrideSubmissionType\":\"1\"}}";
        final ObjectMapper mapper = new ObjectMapper();
        final BulkRequest obj = mapper.readValue(json, BulkRequest.class);

        assertThat(obj.getFilters().size(), Matchers.is(3));
        assertThat(getFilter(obj, "productNumber").getValues(),
            Matchers.containsInAnyOrder("POM.010118", "POM.010111", "POM.010104"));

        final Filters filters = obj.getPagingFilters();
        assertNotNull(filters);
        assertNotNull(filters.get("productType"));
        assertNotNull(filters.get("productNumber"));

    }

    private Filter getFilter(final BulkRequest request, final String property) {
        return request.getFilters().get(property).stream().findFirst().get();
    }
}
