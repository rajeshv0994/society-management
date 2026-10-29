package com.societyapp.documents;

import com.societyapp.application.model.BinaryContent;
import com.societyapp.application.model.storage.StorageModels;
import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.domain.type.StorageSourceType;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalDocumentStorage implements DocumentStorage {
    private static final Map<String, String> EXTENSIONS =
            Map.ofEntries(
                    Map.entry("application/pdf", ".pdf"),
                    Map.entry("image/jpeg", ".jpg"),
                    Map.entry("image/png", ".png"),
                    Map.entry("video/mp4", ".mp4"),
                    Map.entry("video/webm", ".webm"),
                    Map.entry("video/quicktime", ".mov"),
                    Map.entry("text/plain", ".txt"),
                    Map.entry("application/msword", ".doc"),
                    Map.entry(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            ".docx"),
                    Map.entry("application/vnd.ms-excel", ".xls"),
                    Map.entry(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            ".xlsx"));
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> VIDEO_TYPES =
            Set.of("video/mp4", "video/webm", "video/quicktime");
    private final Path root;
    private final long maxBytes;
    private final StorageMeterUseCase meter;

    public LocalDocumentStorage(
            @Value("${app.uploads-directory}") String root,
            @Value("${app.max-upload-bytes}") long maxBytes)
            throws IOException {
        this(root, maxBytes, null);
    }

    @Autowired
    public LocalDocumentStorage(
            @Value("${app.uploads-directory}") String root,
            @Value("${app.max-upload-bytes}") long maxBytes,
            StorageMeterUseCase meter)
            throws IOException {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        this.meter = meter;
        Files.createDirectories(this.root);
    }

    public String store(UUID scope, MultipartFile file) {
        return store(scope, uploaded(file));
    }

    @Override
    public String store(UUID scope, UploadedFile file) {
        String contentType = validate(file, EXTENSIONS.keySet(), "Unsupported file type");
        return storeInFolder(scope, category(contentType), contentType, file);
    }

    public String storeImage(UUID scope, MultipartFile file) {
        return storeImage(scope, uploaded(file));
    }

    @Override
    public String storeImage(UUID scope, UploadedFile file) {
        String contentType = validate(file, IMAGE_TYPES, "Only JPEG and PNG images are allowed");
        return storeInFolder(scope, "images", contentType, file);
    }

    public String storeVideo(UUID scope, MultipartFile file) {
        return storeVideo(scope, uploaded(file));
    }

    public String storeVideo(UUID scope, UploadedFile file) {
        String contentType =
                validate(file, VIDEO_TYPES, "Only MP4, WebM and QuickTime videos are allowed");
        return storeInFolder(scope, "videos", contentType, file);
    }

    public String storeMedia(UUID scope, MultipartFile file) {
        return storeMedia(scope, uploaded(file));
    }

    @Override
    public String storeMedia(UUID scope, UploadedFile file) {
        String contentType =
                validate(
                        file,
                        Set.of(
                                "image/jpeg",
                                "image/png",
                                "video/mp4",
                                "video/webm",
                                "video/quicktime"),
                        "Only JPEG, PNG, MP4, WebM and QuickTime media are allowed");
        return storeInFolder(scope, category(contentType), contentType, file);
    }

    private String storeInFolder(
            UUID scope, String category, String contentType, UploadedFile file) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Path folder =
                root.resolve(scope.toString())
                        .resolve(category)
                        .resolve(String.valueOf(today.getYear()))
                        .resolve(String.format(Locale.ROOT, "%02d", today.getMonthValue()))
                        .normalize();
        try {
            Files.createDirectories(folder);
            Path target =
                    folder.resolve(UUID.randomUUID() + EXTENSIONS.get(contentType)).normalize();
            if (!target.startsWith(folder) || !folder.startsWith(root)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            try (var input = file.openStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String key = root.relativize(target).toString();
            if (meter != null) {
                try {
                    meter.register(
                            new StorageModels.RegisterCommand(
                                    scope,
                                    key,
                                    category,
                                    file.originalFilename(),
                                    contentType,
                                    file.size()));
                } catch (RuntimeException exception) {
                    Files.deleteIfExists(target);
                    throw exception;
                }
            }
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store document");
        }
    }

    private String validate(UploadedFile file, Set<String> allowedTypes, String typeMessage) {
        if (file == null || file.empty()) throw new IllegalArgumentException("File is empty");
        if (file.size() > maxBytes) {
            throw new IllegalArgumentException("File exceeds maximum size");
        }
        String contentType =
                file.contentType() == null
                        ? ""
                        : file.contentType().toLowerCase(Locale.ROOT).trim();
        if (!allowedTypes.contains(contentType)) throw new IllegalArgumentException(typeMessage);
        return contentType;
    }

    private String category(String contentType) {
        if (IMAGE_TYPES.contains(contentType)) return "images";
        if (VIDEO_TYPES.contains(contentType)) return "videos";
        return "documents";
    }

    public Resource loadResource(String key) {
        try {
            Path path = root.resolve(key).normalize();
            if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable())
                throw new IllegalArgumentException("File not found");
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Invalid storage key");
        }
    }

    @Override
    public BinaryContent load(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("File not found");
        }
        try {
            return new BinaryContent(Files.newInputStream(path), Files.size(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read stored file");
        }
    }

    private UploadedFile uploaded(MultipartFile file) {
        return new UploadedFile() {
            public String originalFilename() {
                return file.getOriginalFilename();
            }

            public String contentType() {
                return file.getContentType();
            }

            public long size() {
                return file.getSize();
            }

            public boolean empty() {
                return file.isEmpty();
            }

            public java.io.InputStream openStream() throws IOException {
                return file.getInputStream();
            }
        };
    }

    public void attach(String key, String sourceType, UUID sourceId, String displayName) {
        if (meter != null) {
            meter.attach(
                    new StorageModels.AttachCommand(
                            key, StorageSourceType.from(sourceType), sourceId, displayName));
        }
    }

    public void delete(String key) {
        try {
            Path path = root.resolve(key).normalize();
            if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete stored file");
        }
    }

    public List<StoredFile> inventory() {
        var result = new ArrayList<StoredFile>();
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .forEach(
                            path -> {
                                Path relative = root.relativize(path);
                                if (relative.getNameCount() < 2) return;
                                try {
                                    UUID societyId =
                                            UUID.fromString(relative.getName(0).toString());
                                    String category =
                                            relative.getNameCount() > 2
                                                    ? relative.getName(1).toString()
                                                    : "documents";
                                    result.add(
                                            new StoredFile(
                                                    societyId,
                                                    relative.toString(),
                                                    category,
                                                    Files.probeContentType(path),
                                                    Files.size(path)));
                                } catch (IllegalArgumentException | IOException ignored) {
                                    // Files outside a society UUID folder are not managed uploads.
                                }
                            });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inventory stored files");
        }
        return result;
    }

    public record StoredFile(
            UUID societyId,
            String storageKey,
            String category,
            String contentType,
            long sizeBytes) {}
}
