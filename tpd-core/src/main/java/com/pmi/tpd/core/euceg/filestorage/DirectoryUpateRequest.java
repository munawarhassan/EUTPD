package com.pmi.tpd.core.euceg.filestorage;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 * @since 3.0
 */
@Builder
@Getter
@Jacksonized
public class DirectoryUpateRequest {

    /** */
    @NotNull
    @Size(max = 50)
    @Pattern(regexp = "^[\\w-]*$")
    @JsonProperty("name")
    private final String name;

}