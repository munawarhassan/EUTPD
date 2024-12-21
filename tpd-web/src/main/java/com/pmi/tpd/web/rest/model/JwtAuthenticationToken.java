package com.pmi.tpd.web.rest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * @author devacfr
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@Jacksonized
public class JwtAuthenticationToken {

    /** */
    @NotNull
    @Size(min = 1, max = 50)
    private String username;

    /** */
    @NotNull
    private String password;

    /** */
    private boolean rememberMe;

}
