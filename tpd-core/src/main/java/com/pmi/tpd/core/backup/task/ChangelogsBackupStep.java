package com.pmi.tpd.core.backup.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.LineProcessor;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * Includes the Liquibase changelogs which were used to create the system
 * database schema in the backup.
 * <p>
 * Including the changelogs for the version of the system which is being backed
 * up <i>greatly</i> simplifies the process
 * of restoring the backup later. Rather than attempting to verify the version
 * and somehow load the right changelog,
 * this way the changelog can be drawn directly from the backup contents. If an
 * older backup is being restored into a
 * newer version of the system, when the full system is started after restore
 * the database contents will be migrated to
 * the latest version of the schema as normal; no need for specialised logic.
 * <p>
 * Implementation note: There are two possible approaches for including the
 * changelogs. One is to parse through the
 * {@link #PATH_MASTER master} changelog and pull out its includes explicitly.
 * The other is to use Spring's resource
 * resolving to find all XML files below {@link #PATH_LIQUIBASE liquibase/}. To
 * be more certain extra files are not
 * included in the backup, this implementation parses the master changelog.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ChangelogsBackupStep extends AbstractRunnableTask {

  /**
   * Regular expression for matching {@code &lt;include/&gt;} lines in the
   * {@link #PATH_MASTER master} changelog.
   * <p>
   * Groups:
   * <ol>
   * <li>The path to the included file</li>
   * </ol>
   */
  public static final Pattern PATTERN_INCLUDE = Pattern.compile("\\s+<include file=\"([^\"]+)\"/>$");

  /**
   * The resource path, relative to the root, beneath which the XML files
   * comprising the Liquibase changelog are
   * stored.
   */
  private static final String PATH_LIQUIBASE = "liquibase/";

  /**
   * The bootstrap changelog, which includes the various changelogs that manage
   * the schema required to manage the
   * storage of the license.
   */
  private static final String PATH_BOOTSTRAP = PATH_LIQUIBASE + "bootstrap.xml";

  /**
   * The master changelog, which includes the various changelogs that apply the
   * initial schema and updated it with
   * each release's schema changes.
   */
  private static final String PATH_MASTER = PATH_LIQUIBASE + "master.xml";

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogsBackupStep.class);

  /** */
  private final I18nService i18nService;

  /** */
  private final IBackupState state;

  /** */
  private volatile int progress;

  /**
   * @param i18nService
   * @param state
   */
  public ChangelogsBackupStep(final I18nService i18nService, final IBackupState state) {
    this.i18nService = i18nService;
    this.state = state;
  }

  @Nonnull
  @Override
  public IProgress getProgress() {
    return new ProgressImpl(i18nService.getMessage("app.backup.changelog.save"), progress);
  }

  @Override
  public void run() {
    final ZipOutputStream stream = state.getBackupZipStream();
    Preconditions.checkState(stream != null, "A backup ZipOutputStream is required");

    try {
      final ZipEntry entry = new ZipEntry(IBackupState.CHANGELOGS_BACKUP_FILE);
      stream.putNextEntry(entry);

      LOGGER.debug("Backing up database changelogs to {}", IBackupState.CHANGELOGS_BACKUP_FILE);
      final ZipOutputStream zip = new ZipOutputStream(new CloseShieldOutputStream(stream));
      try {
        writeDatabaseChangelogBackup(zip);
        // Close the zip to write closing CRC data and release native resources. If this
        // close() fails, we'll
        // want
        // the backup process to also fail; that's why it's not done in a finally block
        zip.close();
      } catch (final IOException e) {
        Closeables.close(zip, true);
        throw e;
      }

      stream.closeEntry();
    } catch (final IOException e) {
      throw new BackupException(i18nService.createKeyedMessage("app.backup.changelogs.failed", Product.getName()),
          e);
    }
  }

  private void writeDatabaseChangelogBackup(final ZipOutputStream stream) throws IOException {
    stream.putNextEntry(new ZipEntry(PATH_LIQUIBASE));

    // As the lines are read through the BufferedReader returned from the supplier,
    // the bytes will be tee'd
    // directly into the zip file. When this call completes, bootstrap.xml will be
    // completely zipped
    stream.putNextEntry(new ZipEntry(PATH_BOOTSTRAP));
    final Set<String> paths = Sets.newHashSet();
    try (Reader reader = new ChangeLogInputSupplier(PATH_BOOTSTRAP, stream).getInput()) {
      paths.addAll(CharStreams.readLines(reader, new PathLineHandler()));
    }

    stream.putNextEntry(new ZipEntry(PATH_MASTER));
    try (Reader reader = new ChangeLogInputSupplier(PATH_MASTER, stream).getInput()) {
      paths.addAll(CharStreams.readLines(reader, new PathLineHandler()));
    }

    // Ensure that we don't include the bootstrap or master changelogs twice
    paths.remove(PATH_BOOTSTRAP);
    paths.remove(PATH_MASTER);

    // Note: Since this processing accounts for so little of the overall weight
    // during backup processing, there
    // is little reason to get carried away with how granular its progress is.
    int processed = 2; // 2 for master.xml and bootstrap.xml
    final int total = paths.size() + processed;
    for (final String path : paths) {
      progress = 100 * processed / total;

      stream.putNextEntry(new ZipEntry(path));
      ByteStreams.copy(new ClassPathResource(path).getInputStream(), stream);
      ++processed;
    }
    progress = 100;
  }

  private static final class ChangeLogInputSupplier {

    /** */
    private final String changeLogPath;

    /** */
    private final OutputStream outputStream;

    private ChangeLogInputSupplier(final String changeLogPath, final OutputStream outputStream) {
      this.outputStream = outputStream;
      this.changeLogPath = changeLogPath;
    }

    public BufferedReader getInput() throws IOException {
      final Resource changeLog = new ClassPathResource(changeLogPath);

      final InputStream input = new TeeInputStream(changeLog.getInputStream(), outputStream, false);
      final InputStreamReader reader = new InputStreamReader(input, Charsets.UTF_8);

      return new BufferedReader(reader);
    }
  }

  private static final class PathLineHandler implements LineProcessor<Set<String>> {

    /** */
    private final Set<String> paths;

    private PathLineHandler() {
      paths = Sets.newLinkedHashSet();
    }

    @Override
    public Set<String> getResult() {
      return paths;
    }

    @Override
    public boolean processLine(@Nonnull final String line) throws IOException {
      final Matcher matcher = PATTERN_INCLUDE.matcher(line);
      if (matcher.matches()) {
        paths.add(matcher.group(1));
      }

      return true;
    }
  }
}
