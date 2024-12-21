package com.pmi.tpd;

import java.util.List;
import java.util.Set;

import org.eu.ceg.TobaccoProductTypeEnum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.fasterxml.jackson.datatype.joda.ser.LocalDateSerializer;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.web.core.rs.jackson.internal.JacksonPageSerializer;
import com.pmi.tpd.web.core.rs.jackson.internal.JaxbJsonEnumDeserializer;
import com.pmi.tpd.web.core.rs.jackson.internal.JaxbJsonEnumSerializer;

import io.swagger.v3.core.jackson.SwaggerModule;

@Configuration
public class JacksonConfig {

  @Bean
  public JodaModule jacksonJodaModule() {
    final JodaModule module = new JodaModule();
    module.addSerializer(DateTime.class,
        new DateTimeSerializer(new JacksonJodaDateFormat(ApplicationConstants.DATETIME_FORMATTER), 0));
    module.addSerializer(LocalDate.class,
        new LocalDateSerializer(
            new JacksonJodaDateFormat(ApplicationConstants.DATE_FORMATTER).withUseTimestamp(true), 0));
    return module;
  }

  @Bean
  public SwaggerModule swaggerModule() {
    return new SwaggerModule();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Bean
  public ObjectMapper getObjectMapper(final List<Module> modules) {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    if (modules != null) {
      mapper.registerModules(modules);
    }
    final SimpleModule module = new SimpleModule();

    module.addSerializer((Class) PageImpl.class, JacksonPageSerializer.construct());

    // add specific JAXB de/serializer for Euceg enum to get/set the @XmlEnumValue
    // annotation value instead the
    // ordinal
    // enum value.
    final Reflections reflections = new Reflections(TobaccoProductTypeEnum.class.getPackage().getName());
    final Set<Class<? extends Enum>> allClasses = reflections.getSubTypesOf(Enum.class);
    for (final Class<?> cl : allClasses) {
      final Class<Enum<?>> c = (Class<Enum<?>>) cl;
      module.addSerializer(c, JaxbJsonEnumSerializer.construct(c, mapper));
      module.addDeserializer(c, JaxbJsonEnumDeserializer.construct(c, mapper));
    }

    mapper.registerModule(module);

    return mapper;
  }

}
