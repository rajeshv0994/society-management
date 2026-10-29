package com.societyapp.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.usecase.StorageMeterService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalDocumentStorageTest {
    @TempDir Path root;

    @Test
    void storesImageInSocietyAndDateFoldersAndReturnsOnlyRelativePath() throws Exception {
        UUID societyId = UUID.randomUUID();
        var storage = new LocalDocumentStorage(root.toString(), 1024);
        var file = new MockMultipartFile("file", "photo.png", "image/png", new byte[] {1, 2});

        String key = storage.storeImage(societyId, file);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        assertThat(key)
                .startsWith(
                        societyId
                                + "/images/"
                                + today.getYear()
                                + "/"
                                + String.format("%02d", today.getMonthValue())
                                + "/")
                .endsWith(".png");
        assertThat(Path.of(key).isAbsolute()).isFalse();
        assertThat(Files.readAllBytes(root.resolve(key))).containsExactly(1, 2);
    }

    @Test
    void supportsVideosWithoutPersistingTheirBytesInDatabase() throws Exception {
        UUID societyId = UUID.randomUUID();
        var storage = new LocalDocumentStorage(root.toString(), 1024);
        var file = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[] {3, 4});

        String key = storage.storeVideo(societyId, file);

        assertThat(key).startsWith(societyId + "/videos/").endsWith(".mp4");
        assertThat(storage.load(key).stream().readAllBytes()).containsExactly(3, 4);
    }

    @Test
    void continuesToLoadLegacyFlatStorageKeys() throws Exception {
        UUID societyId = UUID.randomUUID();
        String key = societyId + "/legacy.jpg";
        Files.createDirectories(root.resolve(societyId.toString()));
        Files.write(root.resolve(key), new byte[] {5});
        var storage = new LocalDocumentStorage(root.toString(), 1024);

        assertThat(storage.load(key).stream().readAllBytes()).containsExactly(5);
    }

    @Test
    void rejectsUnsupportedAndOversizedFiles() throws Exception {
        var storage = new LocalDocumentStorage(root.toString(), 2);
        UUID societyId = UUID.randomUUID();

        assertThatThrownBy(
                        () ->
                                storage.store(
                                        societyId,
                                        new MockMultipartFile(
                                                "file",
                                                "program.exe",
                                                "application/octet-stream",
                                                new byte[] {1})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported");
        assertThatThrownBy(
                        () ->
                                storage.storeImage(
                                        societyId,
                                        new MockMultipartFile(
                                                "file",
                                                "large.jpg",
                                                "image/jpeg",
                                                new byte[] {1, 2, 3})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum size");
    }

    @Test
    void removesWrittenFileWhenQuotaRegistrationFails() throws Exception {
        UUID societyId = UUID.randomUUID();
        var meter = mock(StorageMeterService.class);
        doThrow(new IllegalStateException("Storage limit reached"))
                .when(meter)
                .register(
                        any(
                                com.societyapp.application.model.storage.StorageModels
                                        .RegisterCommand.class));
        var storage = new LocalDocumentStorage(root.toString(), 1024, meter);

        assertThatThrownBy(
                        () ->
                                storage.storeImage(
                                        societyId,
                                        new MockMultipartFile(
                                                "file",
                                                "photo.png",
                                                "image/png",
                                                new byte[] {1, 2, 3})))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Storage limit reached");
        assertThat(Files.walk(root).filter(Files::isRegularFile)).isEmpty();
    }
}
