package com.pmi.tpd.euceg.core.exporter;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;

public class Generator {

    public static void main(final String[] args) throws JsonGenerationException, JsonMappingException, IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        mapper.writeValue(new File("/Users/devacfr/Development/excel-tobacco-product.yaml"),
            EucegExcelSchema.TobaccoProduct.DESCRIPTORS);

    }

}
