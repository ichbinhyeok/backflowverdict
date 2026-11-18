package owner.backflow.service;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LeadPhotoService {
    public static final long MAX_BYTES = 5 * 1024 * 1024;
    private final Path root;

    public LeadPhotoService(AppLeadsProperties properties) {
        root = Path.of(properties.root()).toAbsolutePath().normalize().resolve("photos");
    }

    public List<byte[]> prepare(List<MultipartFile> files) {
        var uploads = files == null ? List.<MultipartFile>of() : files.stream().filter(file -> !file.isEmpty()).toList();
        if (uploads.size() > 3) throw new IllegalArgumentException("Choose up to three photos. Remove the extras and send again.");
        var photos = new ArrayList<byte[]>();
        for (var file : uploads) {
            if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("Each photo must be 5 MB or smaller. Export a smaller JPEG or PNG and try again.");
            try {
            byte[] original = file.getBytes();
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(original))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IllegalArgumentException("Use JPEG or PNG photos. This file could not be read as an image.");
                var reader = readers.next();
                try {
                    String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                    if (!List.of("jpeg", "jpg", "png").contains(format)) throw new IllegalArgumentException("Use JPEG or PNG photos. Export HEIC, WebP or other formats first.");
                    reader.setInput(input, true, true);
                    int width = reader.getWidth(0), height = reader.getHeight(0);
                    if (width < 1 || height < 1 || (long) width * height > 16_000_000L) throw new IllegalArgumentException("Use a photo of 16 megapixels or less. Export a smaller copy and try again.");
                    var params = reader.getDefaultReadParam();
                    int sample = Math.max(1, Math.max(width, height) / 2200);
                    params.setSourceSubsampling(sample, sample, 0, 0);
                    BufferedImage decoded = reader.read(0, params);
                    double scale = Math.min(1.0, 2000.0 / Math.max(decoded.getWidth(), decoded.getHeight()));
                    var normalized = new BufferedImage(Math.max(1, (int) (decoded.getWidth() * scale)), Math.max(1, (int) (decoded.getHeight() * scale)), BufferedImage.TYPE_INT_RGB);
                    var graphics = normalized.createGraphics();
                    try {
                        graphics.setColor(Color.WHITE); graphics.fillRect(0, 0, normalized.getWidth(), normalized.getHeight());
                        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        graphics.drawImage(decoded, 0, 0, normalized.getWidth(), normalized.getHeight(), null);
                    } finally { graphics.dispose(); decoded.flush(); }
                    int orientation = jpegOrientation(original);
                    if (orientation > 1) {
                        int w = normalized.getWidth(), h = normalized.getHeight();
                        var oriented = new BufferedImage(orientation >= 5 ? h : w, orientation >= 5 ? w : h, BufferedImage.TYPE_INT_RGB);
                        var transform = switch (orientation) {
                            case 2 -> new java.awt.geom.AffineTransform(-1, 0, 0, 1, w, 0);
                            case 3 -> new java.awt.geom.AffineTransform(-1, 0, 0, -1, w, h);
                            case 4 -> new java.awt.geom.AffineTransform(1, 0, 0, -1, 0, h);
                            case 5 -> new java.awt.geom.AffineTransform(0, 1, 1, 0, 0, 0);
                            case 6 -> new java.awt.geom.AffineTransform(0, 1, -1, 0, h, 0);
                            case 7 -> new java.awt.geom.AffineTransform(0, -1, -1, 0, h, w);
                            default -> new java.awt.geom.AffineTransform(0, -1, 1, 0, 0, w);
                        };
                        var rotation = oriented.createGraphics();
                        try { rotation.drawImage(normalized, transform, null); } finally { rotation.dispose(); normalized.flush(); }
                        normalized = oriented;
                    }
                    var output = new ByteArrayOutputStream();
                    if (!ImageIO.write(normalized, "jpeg", output)) throw new IOException("JPEG writer unavailable");
                    normalized.flush();
                    photos.add(output.toByteArray());
                } finally { reader.dispose(); }
            }
            } catch (IOException exception) {
                throw new IllegalArgumentException("This photo could not be decoded. Export a fresh JPEG or PNG and try again.", exception);
            } catch (RuntimeException exception) {
                if (exception instanceof IllegalArgumentException) throw exception;
                throw new IllegalArgumentException("This photo could not be decoded. Export a fresh JPEG or PNG and try again.", exception);
            }
        }
        return List.copyOf(photos);
    }

    public void save(String leadId, List<byte[]> photos) {
        if (photos.isEmpty()) return;
        Path folder = folder(leadId);
        try {
            Files.createDirectories(folder);
            for (var photo : photos) Files.write(folder.resolve(UUID.randomUUID() + ".jpg"), photo, java.nio.file.StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            deleteAll(leadId);
            throw new IllegalStateException("Could not save request photos.", exception);
        }
    }

    public List<String> list(String leadId) {
        Path folder = folder(leadId);
        if (!Files.isDirectory(folder)) return List.of();
        try (var paths = Files.list(folder)) {
            return paths.filter(Files::isRegularFile).map(path -> path.getFileName().toString())
                    .filter(name -> name.matches("[a-f0-9-]{36}\\.jpg")).sorted().map(name -> name.substring(0, 36)).toList();
        } catch (IOException exception) { throw new IllegalStateException("Could not list request photos.", exception); }
    }

    public byte[] read(String leadId, String photoId) throws IOException {
        String safeId = UUID.fromString(photoId).toString();
        return Files.readAllBytes(folder(leadId).resolve(safeId + ".jpg"));
    }

    public void deleteAll(String leadId) {
        Path folder = folder(leadId);
        if (!Files.isDirectory(folder)) return;
        try (var paths = Files.list(folder)) {
            for (var path : paths.toList()) if (Files.isRegularFile(path)) Files.delete(path);
        } catch (IOException exception) { throw new IllegalStateException("Could not remove request photos.", exception); }
        try { Files.deleteIfExists(folder); }
        catch (IOException exception) { throw new IllegalStateException("Could not remove request photo folder.", exception); }
    }

    private Path folder(String leadId) {
        return root.resolve(UUID.fromString(leadId).toString());
    }

    private int jpegOrientation(byte[] bytes) {
        // Read only the EXIF orientation before stripping all source metadata.
        if (bytes.length < 4 || (bytes[0] & 255) != 255 || (bytes[1] & 255) != 216) return 1;
        for (int offset = 2; offset + 4 < bytes.length;) {
            if ((bytes[offset] & 255) != 255) return 1;
            int marker = bytes[offset + 1] & 255;
            if (marker == 218 || marker == 217) return 1;
            int length = ((bytes[offset + 2] & 255) << 8) | (bytes[offset + 3] & 255);
            if (length < 2 || offset + 2L + length > bytes.length) return 1;
            int base = offset + 10;
            if (marker == 225 && length >= 16 && bytes[offset + 4] == 'E' && bytes[offset + 5] == 'x' && bytes[offset + 6] == 'i' && bytes[offset + 7] == 'f' && bytes[offset + 8] == 0 && bytes[offset + 9] == 0) {
                var buffer = java.nio.ByteBuffer.wrap(bytes, base, length - 8).slice();
                boolean little = buffer.get(0) == 'I' && buffer.get(1) == 'I';
                if (!little && !(buffer.get(0) == 'M' && buffer.get(1) == 'M')) return 1;
                buffer.order(little ? java.nio.ByteOrder.LITTLE_ENDIAN : java.nio.ByteOrder.BIG_ENDIAN);
                if (buffer.getShort(2) != 42) return 1;
                long directory = Integer.toUnsignedLong(buffer.getInt(4));
                if (directory + 2 > buffer.limit()) return 1;
                int count = Short.toUnsignedInt(buffer.getShort((int) directory));
                for (int i = 0; i < Math.min(count, 1024); i++) {
                    long entry = directory + 2L + i * 12L;
                    if (entry + 12 > buffer.limit()) return 1;
                    if (Short.toUnsignedInt(buffer.getShort((int) entry)) == 274 && buffer.getShort((int) entry + 2) == 3 && buffer.getInt((int) entry + 4) == 1) {
                        int value = Short.toUnsignedInt(buffer.getShort((int) entry + 8));
                        return value >= 1 && value <= 8 ? value : 1;
                    }
                }
            }
            offset += length + 2;
        }
        return 1;
    }
}
