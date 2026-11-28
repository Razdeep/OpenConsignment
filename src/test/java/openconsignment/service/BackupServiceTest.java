package openconsignment.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import openconsignment.common.AppConfig;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.di.DatabaseManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class BackupServiceTest {
    @TempDir
    Path repository;

    @Test
    void hashComparisonDistinguishesMatchingAndDifferentDatabases() throws Exception {
        Path local = Path.of(Constants.DATABASE_FILE_NAME);
        Path remote = repository.resolve(Constants.DATABASE_FILE_NAME);
        Files.writeString(remote, "stub");
        BackupService service = service();
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.md5sum(local)).thenReturn("local-hash");
            utils.when(() -> Utils.md5sum(remote)).thenReturn("local-hash", "other-hash");

            assertTrue(service.compareDatabaseHashes());
            assertFalse(service.compareDatabaseHashes());
        }
    }

    @Test
    void missingRemoteDatabaseIsNotInSync() throws Exception {
        Path local = Path.of(Constants.DATABASE_FILE_NAME);
        BackupService service = service();
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.md5sum(local)).thenReturn("local-hash");
            assertFalse(service.compareDatabaseHashes());
            utils.verify(() -> Utils.md5sum(local));
        }
    }

    @Test
    void hashFailureIsPropagated() throws Exception {
        Path local = Path.of(Constants.DATABASE_FILE_NAME);
        BackupService service = service();
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.md5sum(local)).thenThrow(new IOException("cannot read local database"));
            assertThrows(IOException.class, service::compareDatabaseHashes);
        }
    }

    private BackupService service() {
        AppConfig config = AppConfig.forOrganization("Test organization");
        AppConfig.Github github = new AppConfig.Github();
        github.setRepository(repository.toString());
        config.setGithub(github);
        return new BackupService(config, mock(DatabaseManager.class));
    }
}
