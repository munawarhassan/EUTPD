package com.pmi.tpd.core.avatar.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.google.common.collect.Iterators;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.ByteConverter;
import com.pmi.tpd.api.util.FileUtils;
import com.pmi.tpd.core.IGlobalApplicationProperties;
import com.pmi.tpd.core.avatar.AbstractAvatarSupplier;
import com.pmi.tpd.core.avatar.AvatarException;
import com.pmi.tpd.core.avatar.AvatarLoadException;
import com.pmi.tpd.core.avatar.AvatarResizeException;
import com.pmi.tpd.core.avatar.AvatarStoreException;
import com.pmi.tpd.core.avatar.DelegatingCacheableAvatarSupplier;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;
import com.pmi.tpd.core.avatar.UnsupportedAvatarException;
import com.pmi.tpd.core.avatar.spi.AvatarType;
import com.pmi.tpd.core.avatar.spi.IAvatarRepository;

import io.atlassian.util.concurrent.ConcurrentOperationMap;
import io.atlassian.util.concurrent.ConcurrentOperationMapImpl;

/**
 * Stores avatar images on disk under {@link ApplicationSettings#getDataDir() data}/{@link #AVATARS_PATH avatars}.
 * <p>
 * All avatars are converted to PNG files, regardless of what type is uploaded. Each {@link AvatarType type} has its own
 * subdirectory. For avatars, instance-specific subdirectories beneath the type are used, with each instance-specific
 * directory containing an original PNG version of the avatar as well as a set of sized copies.
 *
 * @since 2.4
 */
@Named("avatarRepository")
public class DiskAvatarRepository implements IAvatarRepository {

    /**
     * The path beneath the {@link ApplicationSettings#getDataDir() data directory} where avatars should be stored.
     */
    public static final String AVATARS_PATH = "avatars";

    /**
     * The format in which all avatars are stored (regardless of what type of image is uploaded).
     */
    public static final String AVATAR_FORMAT = "png";

    /**
     * The file extension applied to stored avatars.
     */
    public static final String FILE_EXTENSION = "." + AVATAR_FORMAT;

    /**
     * The minimum maximum dimension for avatars. IF a user configures a value less than this, it will be ignored and
     * this value will be used instead.
     * <p>
     * The largest avatar used directly by the system is 256x256, so the system should support uploads at least that
     * size to produce high-quality large avatars. A maximum smaller than this would mean uploaded avatars would have to
     * be resized <i>larger</i>, which will always produce lower-quality results than resizing smaller.
     *
     * @see #setMaxDimension(int)
     */
    public static final int MINIMUM_MAX_DIMENSION = 256;

    /**
     * The minimum maximum file size for <i>all</i> avatars. If a user configures a value less than this, it will be
     * ignored and this value will be used instead.
     *
     * @since 3.1
     */
    public static final long MINIMUM_MAX_SIZE = 1024 * 100; // 100KB minimum

    /**
     * The filename used to store original avatars. This name is consistent to simplify retrieving the original avatar
     * for any given type and ID for use in resizing.
     */
    public static final String ORIGINAL_FILE = "original" + FILE_EXTENSION;

    /**
     * Marker constant for retrieving the original avatar as it was provided, without any resizing applied.
     */
    public static final int ORIGINAL_SIZE = -1;

    private static final Logger log = LoggerFactory.getLogger(DiskAvatarRepository.class);

    private final File avatarDir;

    private final I18nService i18nService;

    private final ConcurrentOperationMap<ResizeKey, File> operationMap;

    private final long systemTimestamp;

    private long maxDimension;

    private long maxSize;

    @Inject
    public DiskAvatarRepository(final I18nService i18nService, final IApplicationConfiguration applicationConfiguration,
            final IGlobalApplicationProperties propertiesService) {
        this.i18nService = i18nService;

        avatarDir = FileUtils.mkdir(applicationConfiguration.getDataDirectory().toFile(), AVATARS_PATH);
        operationMap = new ConcurrentOperationMapImpl<>();

        // If possible use the build timestamp for the system as the modification date for default avatars. If the
        // build timestamp is not available, fall back on making the default avatars eternal. It's unlikely they'll
        // change very often, so aggressive caching is not likely to be an issue.
        final Date timestamp = propertiesService.getBuildDate();
        systemTimestamp = timestamp == null ? ICacheableAvatarSupplier.TIMESTAMP_ETERNAL : timestamp.getTime();
    }

    /**
     * Recursively deletes all avatars for the specified {@link AvatarType type} and instance ID.
     * <p>
     * Note: There is no mechanism for deleting all avatars of a given type across all instances. This delete mechanism
     * is intended to be used, for example, when a project is deleted.
     *
     * @param type
     *             the type of avatar to cleanup
     * @param id
     *             the instance ID for which avatars should be cleaned up
     */
    @Override
    public void delete(@Nonnull final AvatarType type, @Nonnull final Long id) {
        checkNotNull(id, "id");
        checkNotNull(type, "type");

        final String avatarId = parseId(id);

        final File instanceDir = FileUtils.construct(avatarDir, type.getDirectoryName(), avatarId);
        try {
            deleteDirectory(instanceDir);
        } catch (final IOException e) {
            throw new AvatarStoreException(
                    i18nService.createKeyedMessage("app.service.avatar.store.removalfailed", type.name(), avatarId));
        }
    }

    @Override
    public boolean isStored(@Nonnull final AvatarType type, @Nonnull final Long id) {
        checkNotNull(id, "id");
        checkNotNull(type, "type");

        final String avatarId = parseId(id);

        // Determining whether an object has an avatar is done by checking for an original; specific sizes
        // may not currently exist but will be created when requested from this original
        final File original = FileUtils.construct(avatarDir, type.getDirectoryName(), avatarId, ORIGINAL_FILE);
        try (FileInputStream ignored = new FileInputStream(original)) {
            return true;
        } catch (final FileNotFoundException ignored) {
        } catch (final IOException e) {
            log.warn("Opening {} avatar {} for ID {} threw an unexpected exception",
                type,
                original.getAbsolutePath(),
                id,
                e);
        }
        return false;
    }

    @Nonnull
    @Override
    public ICacheableAvatarSupplier load(@Nonnull final AvatarType type, @Nonnull final Long id, int size) {
        checkNotNull(id, "id");
        checkNotNull(type, "type");

        final String avatarId = parseId(id);

        // Normalise the requested size to a known set. This has two primary benefits:
        // 1. Default avatars come in fixed sizes, so this ensures, when a default avatar is returned, that the size
        // requested will be one that actually exists
        // 2. For uploaded avatars, this constrains the number of sized copies produced, which is important since
        // those sized copies are stored on disk
        size = normaliseSize(size);

        // First, determine whether the requested avatar exists. We do that by checking for the original
        final File original = FileUtils.construct(avatarDir, type.getDirectoryName(), avatarId, ORIGINAL_FILE);
        try (InputStream originalStream = new FileInputStream(original)) {
            File avatar;
            if (size == ORIGINAL_SIZE) {
                // Use -1 as a marker to retrieve the original.
                avatar = original;
            } else {
                // A specific size has been requested, so the next step is to determine if it already exists. To ensure
                // there are no race conditions, this process runs through a ConcurrentOperationMap
                final ResizeKey key = new ResizeKey(type, avatarId, size);
                try {
                    avatar = operationMap.runOperation(key,
                        new ResizeAvatarCallable(i18nService, original, originalStream, size));
                } catch (final ExecutionException e) {
                    throw (AvatarException) e.getCause();
                }
            }
            return new LoadedAvatarSupplier(avatar);
        } catch (final FileNotFoundException e) {
            // fall through to default
        } catch (final IOException e) {
            throw new AvatarLoadException(i18nService.createKeyedMessage("app.service.avatar.load.unreadableimage"), e);
        }

        return defaultSupplier(type.loadDefault(avatarId, normaliseDefaultSize(size)));
    }

    @Nonnull
    @Override
    public ICacheableAvatarSupplier loadDefault(@Nonnull final AvatarType type, int size) {
        size = normaliseDefaultSize(size);

        return defaultSupplier(type.loadFixedDefault(size));
    }

    /**
     * Sets the maximum dimension, whether height or width, for an avatar's image.
     * <p>
     * Parsing image data into a {@code BufferedImage} can use significantly more heap space than the image's file size,
     * for large dimensions. This maximum is used to try to prevent pathological images from consuming all of the heap
     * space during reading.
     * <p>
     * Note: Using {@code BufferedImage} to get the dimensions, in order to check this maximum, is pointless; after the
     * image has been read the heap space has already been used, so the damage, if any, has already been done.
     *
     * @param maxDimension
     *                     the maximum dimension, height or width, for uploaded avatars
     * @see #MINIMUM_MAX_DIMENSION
     */

    public void setMaxDimension(int maxDimension) {
        if (maxDimension < MINIMUM_MAX_DIMENSION) {
            log.warn("The configured max dimension for avatars, [{}] pixels, is too small and will be ignored. "
                    + "It will be defaulted to {} pixels",
                maxDimension,
                MINIMUM_MAX_DIMENSION);
            maxDimension = MINIMUM_MAX_DIMENSION;
        }
        this.maxDimension = maxDimension;
    }

    /**
     * Sets the maximum file size for <i>all</i> avatars. Avatar image files larger than this are immediately deleted
     * from the repository.
     *
     * @param maxSize
     *                the maximum file size for avatar images
     */
    public void setMaxSize(final long maxSize) {
        if (maxSize < MINIMUM_MAX_SIZE) {
            log.warn(
                "The configured max file size for avatars, [{}], is too small and will be ignored. "
                        + "It will be defaulted to {}",
                ByteConverter.toStringByte(maxSize),
                ByteConverter.toStringByte(MINIMUM_MAX_SIZE));
        }
        this.maxSize = maxSize;
    }

    @Override
    public void store(@Nonnull final AvatarType type, @Nonnull final Long id, @Nonnull final IAvatarSupplier supplier) {
        checkNotNull(id, "id");
        checkNotNull(supplier, "supplier");
        checkNotNull(type, "type");

        final String avatarId = parseId(id);

        // Note: Since we resize the original to produce the concrete sizes used by the system, if the original is
        // oversized that is not an issue. It will produce higher-quality results for all the resized images. Hence,
        // as long as the avatar meets file size restrictions, its overall dimensions don't matter.
        final BufferedImage avatar = readAvatar(supplier);

        final File typeDir = FileUtils.mkdir(avatarDir, type.getDirectoryName());
        final File instanceDir = FileUtils.mkdir(typeDir, avatarId);
        if (instanceDir.isDirectory()) {
            try {
                cleanDirectory(instanceDir);
            } catch (final IOException e) {
                // Technically the name is going to be based on our internal name of the enum value, but that's OK for
                // this error
                throw new AvatarStoreException(
                        i18nService.createKeyedMessage("app.service.avatar.store.cleanupfailed", type.name()));
            }
        }
        writeAvatar(avatar, new File(instanceDir, ORIGINAL_FILE));
    }

    @Override
    public long getVersionId(@Nonnull final AvatarType type, @Nonnull final Long id) {
        // all sizes treated equal
        final ICacheableAvatarSupplier supplier = load(type, id, -1);
        return supplier.getTimestamp();
    }

    @Nonnull
    private ICacheableAvatarSupplier defaultSupplier(@Nonnull final IAvatarSupplier supplier) {
        return new DelegatingCacheableAvatarSupplier(checkNotNull(supplier, "supplier"), systemTimestamp);
    }

    /**
     * Converts the provided {@code Object} into a {@code String}, and ensures the resulting {@code String} is not
     * {@code null} or blank.
     *
     * @param id
     *           the ID to convert to an avatar ID
     * @return the {@code String} value of the provided {@code id}
     * @throws IllegalArgumentException
     *                                  if the {@code id} produces a {@code null} or blank {@code String}
     */
    private String parseId(final Object id) {
        final String avatarId = String.valueOf(id);
        if (StringUtils.isBlank(avatarId)) {
            throw new IllegalArgumentException("The provided ID produces a String which is blank or null");
        }
        return avatarId;
    }

    /**
     * Reads an avatar {@code BufferedImage} from the provided {@link IAvatarSupplier supplier}'s {@code InputStream}.
     * <p>
     * Note: This implementation ignores the professed {@link IAvatarSupplier#getContentType() content type} from the
     * supplier and uses AWT's {@code ImageIO} to attempt read the stream. If AWT can parse the image data the exact
     * content type is irrelevant, since all avatars are stored as PNGs.
     *
     * @param supplier
     *                 a supplier containing an input stream with image data
     * @return the avatar image
     * @throws AvatarStoreException
     *                                    if the provided {@link IAvatarSupplier supplier} does not contain an input
     *                                    stream, or if the stream cannot be read to produce an image
     * @throws UnsupportedAvatarException
     *                                    if the input stream from the {@link IAvatarSupplier supplier} contains data
     *                                    which is not an image
     */
    private BufferedImage readAvatar(final IAvatarSupplier supplier) {
        InputStream rawStream;
        try {
            rawStream = supplier.open();
        } catch (final IOException e) {
            throw new AvatarStoreException(i18nService.createKeyedMessage("app.service.avatar.store.supplierfailed"),
                    e);
        }

        // It's against the supplier's contract for this to happen, but it's worth checking.
        if (rawStream == null) {
            throw new AvatarStoreException(i18nService.createKeyedMessage("app.service.avatar.store.nostream"));
        }

        // Rather than directly copying the stream to the disk, we read it into an AWT Image. This provides 2 things:
        // 1. Validation that the image data can be understood by the JVM
        // 2. The ability to convert other image types (JPG, GIF) to PNG
        BufferedImage avatar;
        ImageReader reader = null;
        try {
            // First, construct an ImageInputStream from the raw InputStream, and then get an iterator containing,
            // hopefully, at least one ImageReader that supports the stream.
            final ImageInputStream imageStream = ImageIO.createImageInputStream(rawStream);
            final Iterator<ImageReader> readerIterator = ImageIO.getImageReaders(imageStream);

            reader = Iterators.getNext(readerIterator, null);
            if (reader == null) {
                // If there are no readers for the image data, then assume the caller provided non-image data.
                throw new UnsupportedAvatarException(
                        i18nService.createKeyedMessage("app.service.avatar.store.unsupportedcontenttype"));
            }
            reader.setInput(imageStream);

            // If a reader is available, use it to determine the height and width of the image data. This is used to
            // prevent excessive heap usage from loading avatars with large dimensions that compress well, allowing
            // them to fit within file size limits.
            final int height = reader.getHeight(0);
            final int width = reader.getWidth(0);
            if (height > maxDimension || width > maxDimension) {
                // Note: The explicit String.valueOf calls here prevent the message from turning out like this:
                // "The selected avatar is 1,920x1,080. The maximum dimensions for avatars are 1,024x1,024."
                // The commas in the numbers are added by how MessageFormat.format handles numeric arguments,
                // but for this message they're undesirable; image dimensions don't usually use commas.
                throw new UnsupportedAvatarException(
                        i18nService.createKeyedMessage("app.service.avatar.store.unsupporteddimensions",
                            String.valueOf(width),
                            String.valueOf(height),
                            String.valueOf(maxDimension)));
            }

            // If a reader is available and the image's dimensions are within supported maximums, read the avatar
            // in as a BufferedImage. This must be done _last_ to ensure reasonable memory consumption.
            avatar = reader.read(0);
        } catch (final IOException e) {
            throw new AvatarStoreException(i18nService.createKeyedMessage("app.service.avatar.store.unreadableimage"),
                    e);
        } finally {
            // If a reader was allocated, ensure it is disposed to free up its resources. The JPEG and TIFF readers
            // both allocate native resources, so this cleanup is particularly important for them.
            if (reader != null) {
                reader.dispose();
            }

            // After manipulating the avatar data, ensure the stream from the supplier is always closed. However,
            // if it can't be closed, don't propagate the exception; this is best-effort.
            Closeables.closeQuietly(rawStream);
        }

        return avatar;
    }

    /**
     * Writes the provided {@code avatar} to the specified {@code file}.
     * <p>
     * Note: This method is intended to be used for storing original avatars, and throws exceptions tailored to that
     * usage. {@link ResizeAvatarCallable#writeResized(BufferedImage, File) writeResized} is intended for use storing
     * resized avatars in response to user requests.
     *
     * @param avatar
     *               the original avatar to store
     * @param file
     *               the file to store the original avatar in
     * @throws AvatarStoreException
     *                              if the avatar cannot be written to the specified file
     */
    private void writeAvatar(final BufferedImage avatar, final File file) {
        try {
            // Force the image to be a PNG
            ImageIO.write(avatar, AVATAR_FORMAT, file);
        } catch (final IOException e) {
            throw new AvatarStoreException(i18nService.createKeyedMessage("app.service.avatar.store.writefailed"), e);
        }

        final long length = file.length();
        if (length > maxSize) {
            if (!file.delete()) {
                log.warn("Oversized avatar {} ({}) could not be deleted; will attempt to delete on exit",
                    file.getAbsolutePath(),
                    ByteConverter.toStringByte(length));
                file.deleteOnExit();
            }
            throw new AvatarStoreException(i18nService.createKeyedMessage("app.service.avatar.store.oversized",
                ByteConverter.toStringByte(length),
                ByteConverter.toStringByte(maxSize)));
        }
    }

    /**
     * Normalises the specified size to a finite set of potential sizes, normalising requests for the
     * {@link #ORIGINAL_SIZE original size} (or any other negative value) to the <i>largest available size</i>.
     *
     * @param size
     *             the requested size
     * @return the normalised size
     */
    private static int normaliseDefaultSize(int size) {
        size = normaliseSize(size);
        if (size == -1) {
            // Default avatars have no "original" size, so -1 is not valid here. Retrieve the largest size by default,
            // as this behaviour matches what happens if a request is made to REST or SpringMVC with no explicit size
            return 256;
        }
        return size;
    }

    /**
     * Normalises the specified size to a finite set of potential sizes, effectively constraining the number of resized
     * avatars that will be created from a given original.
     * <p>
     * Note: Any negative size results in {@link #ORIGINAL_SIZE}, which is a marker value used to retrieve the original
     * avatar as it was provided.
     *
     * @param size
     *             the requested size
     * @return the normalised size
     */
    private static int normaliseSize(final int size) {
        if (size < 0) {
            return ORIGINAL_SIZE;
        }
        if (size > 128) {
            return 256;
        }
        if (size > 96) {
            return 128;
        }
        if (size > 64) {
            return 96;
        }
        if (size > 48) {
            return 64;
        }
        return 48;
    }

    /**
     * Supplies an avatar loaded from a {@code File}.
     */
    private static class LoadedAvatarSupplier extends AbstractAvatarSupplier implements ICacheableAvatarSupplier {

        private final File avatar;

        private LoadedAvatarSupplier(final File avatar) {
            super(MediaType.IMAGE_PNG_VALUE);

            this.avatar = avatar;
        }

        @Nonnull
        @Override
        public InputStream open() throws IOException {
            return new FileInputStream(avatar);
        }

        /**
         * Retrieves the avatar's last modification time.
         * <p>
         * Note: {@code File.lastModified()} returns {@code 0L} (which is {@link #TIMESTAMP_UNKNOWN}) if the file's
         * modification date cannot be determined, which matches the interface's contract without extra checks.
         *
         * @return the file's modification timestamp, or {@code 0L} if it cannot be determined
         */
        @Override
        public long getTimestamp() {
            return avatar.lastModified();
        }
    }

    /**
     * Resizes an original avatar to a requested size.
     * <p>
     * This {@code Callable} is intended to be used with the {@link ConcurrentOperationMap} to ensure that, if parallel
     * requests are made for an avatar at a given size, the resize operation is only performed once. All other callers
     * will receive references to the same {@code File} containing the cached avatar.
     */
    private static class ResizeAvatarCallable implements Callable<File> {

        private final I18nService i18nService;

        private final File original;

        private final InputStream originalStream;

        private final int size;

        private ResizeAvatarCallable(final I18nService i18nService, final File original,
                final InputStream originalStream, final int size) {
            this.i18nService = i18nService;
            this.original = original;
            this.originalStream = originalStream;
            this.size = size;
        }

        /**
         * Resizes the original avatar provided during construction to the size specified at the same time. If the
         * avatar has previously been requested in the same size, it is returned as-is. Otherwise, AWT is used to resize
         * the original avatar to the requested size, whether larger or smaller. The resized avatar will be written to
         * disk for reuse on subsequent requests.
         *
         * @return a {@code File} referencing the avatar in the requested size
         * @throws AvatarException
         *                         if the original avatar cannot be read or the resized avatar cannot be stored
         */
        @Override
        public File call() throws AvatarException {
            final File sized = FileUtils.construct(original.getParentFile(), size + FILE_EXTENSION);
            try (InputStream ignored = new FileInputStream(sized)) {
                return sized;
            } catch (final FileNotFoundException ignored) {
            } catch (final IOException e) {
                throw new AvatarLoadException(i18nService.createKeyedMessage("app.service.avatar.load.unreadableimage"),
                        e);
            }

            // The desired size doesn't exist, so we need to create it. The first step is to load the original avatar
            // so that we can resize it (hopefully down, since sizing up will look unpleasant)
            BufferedImage image = readOriginal();

            // Resize the image to the requested dimensions
            image = resize(image);

            // Once we've scaled the image to the desired dimensions, save it. This way we only need to produce any
            // given size once, but we only produce sizes that are requested for any given avatar
            writeResized(image, sized);

            return sized;
        }

        /**
         * Reads in the original avatar as uploaded. For resizing, the original is the highest fidelity source, since
         * any given size (whether larger or smaller) will have been created from it. To avoid compounding pixel loss
         * from the resize process, each size is generated directly from the original.
         * <p>
         * Note: Reading the original for scaling does <i>not</i> check the dimensions or use an {@code ImageReader}. It
         * is assumed that the avatar's dimensions were tested when it was uploaded. Rather than being paranoid and
         * testing again, this implementation trusts the content on disk.
         *
         * @return the original avatar
         */
        private BufferedImage readOriginal() {
            BufferedImage image;
            try {
                image = ImageIO.read(originalStream);
            } catch (final IOException e) {
                throw new AvatarLoadException(i18nService.createKeyedMessage("app.service.avatar.load.unreadableimage"),
                        e);
            }
            return image;
        }

        /**
         * Renders the specified image with the requested dimensions, which may be larger or smaller than the source.
         * <p>
         * The {@code hint} is used to specify the interpolation used by the {@code Graphics2D} object used to render
         * the image to produce the best image possible. The {@code type} is used to preserve transparency from the
         * original image, if present.
         *
         * @param image
         *               the image to render
         * @param width
         *               the width to render the image at
         * @param height
         *               the height to render the image at
         * @param type
         *               the type of image being rendered, used to preserve transparency
         * @param hint
         *               the {@code RenderingHints.VALUE_INTERPOLATION} entry to hint at the interpolation to use
         * @return the rendered image
         */
        private BufferedImage render(final BufferedImage image,
            final int width,
            final int height,
            final int type,
            final Object hint) {
            final BufferedImage tmp = new BufferedImage(width, height, type);

            final Graphics2D graphics = tmp.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(image, 0, 0, width, height, null);
            graphics.dispose();

            return tmp;
        }

        /**
         * Resizes the provided image to the the dimensions set when this {@code ResizeAvatarCallable} was constructed.
         * The algorithm used varies depending on whether the resize is being used to make the image larger or smaller,
         * to try and produce the highest quality result while balancing the rendering cost.
         *
         * @param image
         *              the original image to resize
         * @return the resized image
         */
        private BufferedImage resize(BufferedImage image) {
            final int type = image.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
                    : BufferedImage.TYPE_INT_ARGB;
            if (size > image.getHeight() || size > image.getWidth()) {
                // Scale up directly from original size to target size with a single call
                image = render(image, size, size, type, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            } else {
                // Start with original size and scale down in multiple passes until the target size is reached
                int height = image.getHeight();
                int width = image.getWidth();
                do {
                    height = sizeDown(height);
                    width = sizeDown(width);

                    image = render(image, width, height, type, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                } while (width != size || height != size);
            }
            return image;
        }

        /**
         * Computes the next size down between the specified {@code dimension} and the requested size.
         * <p>
         * This is used to perform incremental resizing when <i>reducing</i> an image (hence the name). If the source
         * image's {@code dimension} (which may be the height or width; because avatars are square the requested size is
         * the same either way) is 512, for example, and the requested size is 64, this calculation will be used to
         * scale the dimension to:
         * <ol>
         * <li>256</li>
         * <li>128</li>
         * <li>64</li>
         * </ol>
         * If the next size down is smaller than the requested size, the requested size will be returned. This method
         * will never calculate a size which is smaller than the requested size.
         *
         * @param dimension
         *                  the current dimension, which may be either height or width
         * @return the calculated size, which will always be greater than or equal to the requested size
         */
        private int sizeDown(int dimension) {
            if (dimension > size) {
                dimension /= 2;
                if (dimension < size) {
                    dimension = size;
                }
            }
            return dimension;
        }

        /**
         * Writes the {@code resized} image to the specified {@code target}, effectively caching it so that future
         * requests for the same avatar size will not have to perform the resize again.
         *
         * @param resized
         *                the resized image
         * @param target
         *                the file on disk where the resized image should be stored
         */
        private void writeResized(final BufferedImage resized, final File target) {
            try {
                ImageIO.write(resized, AVATAR_FORMAT, target);
            } catch (final IOException e) {
                throw new AvatarResizeException(i18nService.createKeyedMessage("app.service.avatar.load.resizefailed"),
                        e);
            }
        }
    }

    /**
     * Uniquely identifies the avatar of a given type and size for a specific instance. This key is intended to be used
     * with the {@link ConcurrentOperationMap} to ensure that requests for the same avatar type and size for the same
     * instance receive the same resized avatar.
     */
    private static class ResizeKey {

        private final int[] hashes;

        private ResizeKey(final AvatarType type, final String id, final int size) {
            hashes = new int[] { type.ordinal(), id.hashCode(), size };
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof ResizeKey) {
                final ResizeKey s = (ResizeKey) o;

                return Arrays.equals(hashes, s.hashes);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hashes);
        }
    }
}
