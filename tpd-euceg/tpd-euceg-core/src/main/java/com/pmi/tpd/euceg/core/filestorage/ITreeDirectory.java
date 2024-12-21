package com.pmi.tpd.euceg.core.filestorage;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

public interface ITreeDirectory {

  @Nonnull
  Path getPath();

  @Nonnull
  Optional<Path> getParentPath();

  @Nonnull
  String getName();

  @Nonnull
  List<ITreeDirectory> getChildren();

}