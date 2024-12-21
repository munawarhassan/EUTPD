package com.pmi.tpd.core.euceg.filestorage;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pmi.tpd.euceg.core.filestorage.ITreeDirectory;

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
public class FsTreeDirectory {

    @NotNull
    @JsonProperty(required = true)
    private String name;

    @NotNull
    @JsonProperty(required = true)
    private String path;

    @NotNull
    @JsonProperty(required = true)
    private String parentPath;

    private List<FsTreeDirectory> children;

    public static FsTreeDirectory create(final ITreeDirectory tree) {
        return FsTreeDirectory.builder()
                .name(tree.getName())
                .path(tree.getPath().toString())
                .parentPath(tree.getParentPath().map(p -> p.toString()).orElse(""))
                .children(tree.getChildren().stream().map(FsTreeDirectory::create).collect(Collectors.toList()))
                .build();
    }

}
