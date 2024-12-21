package com.pmi.tpd.core.euceg.filestorage;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class MoveFileRequest {

    @NotNull
    @JsonProperty(required = true)
    private String uuid;

    @NotNull
    @JsonProperty(required = true)
    private String newParentPath;
}
