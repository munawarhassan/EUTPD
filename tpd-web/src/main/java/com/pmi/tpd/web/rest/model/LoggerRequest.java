package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ch.qos.logback.classic.Logger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize
public class LoggerRequest {

    /** */
    private String name;

    /** */
    private String level;

    public LoggerRequest(final Logger logger) {
        this.name = logger.getName();
        this.level = logger.getEffectiveLevel().toString();
    }

}
