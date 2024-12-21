package com.pmi.tpd.euceg.core.filestorage.internal;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.Lists;
import com.pmi.tpd.euceg.core.filestorage.ITreeDirectory;

public class DirectoriesWalkTreeVisitor extends SimpleFileVisitor<Path> {

  final TreeDirectory rootDirectories;

  final MutableObject<TreeDirectory> currentdirectory;

  final Function<Path, Path> relativize;

  public DirectoriesWalkTreeVisitor(final TreeDirectory rootDirectories, final Function<Path, Path> relativize) {
    this.rootDirectories = rootDirectories;
    this.relativize = relativize;
    this.currentdirectory = new MutableObject<>(rootDirectories);
  }

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    final Path relativePath = relativize.apply(dir);
    if (relativePath.equals(rootDirectories.getPath())) {
      return FileVisitResult.CONTINUE;
    }
    final TreeDirectory directory = new TreeDirectory(relativePath, currentdirectory.getValue());
    currentdirectory.getValue().getChildren().add(directory);
    currentdirectory.setValue(directory);
    return super.preVisitDirectory(dir, attrs);
  }

  @Override
  public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
    final Path relativePath = relativize.apply(dir);
    if (relativePath.equals(rootDirectories.getPath())) {
      return FileVisitResult.CONTINUE;
    }
    currentdirectory.setValue(currentdirectory.getValue().getParent().orElse(null));
    return super.postVisitDirectory(dir, exc);
  }

  public static class TreeDirectory implements ITreeDirectory {

    private final Path path;

    private final WeakReference<TreeDirectory> parent;

    private final List<ITreeDirectory> children = Lists.newArrayList();

    public TreeDirectory(@Nonnull final Path path, @Nullable final TreeDirectory parent) {
      this.path = checkNotNull(path, "path");
      this.parent = new WeakReference<>(parent);
    }

    @Override
    @Nonnull
    public Path getPath() {
      return path;
    }

    @Override
    @Nonnull
    public Optional<Path> getParentPath() {
      return ofNullable(path.getParent());
    }

    @Override
    @Nonnull
    public String getName() {
      return path.getFileName().toString();
    }

    @Override
    @Nonnull
    public List<ITreeDirectory> getChildren() {
      return children;
    }

    Optional<TreeDirectory> getParent() {
      return ofNullable(parent.get());
    }

  }
}
