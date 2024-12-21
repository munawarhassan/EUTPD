package com.pmi.tpd.web.rest.model;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class GroupAndUsersRequest {

    /** */
    @Size(min = 1, max = 255)
    @NotNull
    private String group;

    /** */
    @NotNull
    private Set<String> users;

}