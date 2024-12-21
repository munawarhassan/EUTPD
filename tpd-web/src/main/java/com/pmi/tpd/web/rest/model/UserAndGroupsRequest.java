package com.pmi.tpd.web.rest.model;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * POJO to add a user to one or more groups. from JSON or xml representation.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
@Getter
@AllArgsConstructor
@JsonSerialize
public class UserAndGroupsRequest {

    /** */
    @Size(min = 1, max = 255)
    @JsonProperty(value = "user", required = true)
    private final String user;

    /** */
    @NotNull
    @JsonProperty(value = "groups", required = true)
    private final Set<String> groups;

}