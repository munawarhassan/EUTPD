package com.pmi.tpd.web.core.rs.jackson.internal;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Provider
@Consumes(MediaType.WILDCARD) // NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.WILDCARD)
public class CustomJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider {

    public CustomJacksonJaxbJsonProvider() {
        super(DEFAULT_ANNOTATIONS);
    }

    public CustomJacksonJaxbJsonProvider(final Annotations... annotationsToUse) {
        super(annotationsToUse);
    }

    public CustomJacksonJaxbJsonProvider(final ObjectMapper mapper, final Annotations[] annotationsToUse) {
        super(mapper, annotationsToUse);
    }

    @PostConstruct
    protected synchronized void initObjectMapper() {
        final ObjectMapper mapper = locateObjectMapper();
        if (mapper != null) {
            this._mapperConfig.setMapper(mapper);
            this.setAnnotationsToUse(DEFAULT_ANNOTATIONS);
        }
    }

    protected ObjectMapper locateObjectMapper() {
        return this._locateMapperViaProvider(null, null);
    }

}
