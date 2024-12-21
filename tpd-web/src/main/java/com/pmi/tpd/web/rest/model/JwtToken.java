package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author devacfr
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@JsonSerialize
public class JwtToken {

    /** */
    private final String token;

}