package openconsignment.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HashTest {
    @TempDir
    Path directory;

    @Test
    void hashesEmptyAndMultiBufferFiles() throws Exception {
        Path file = directory.resolve("data.bin");
        Files.write(file, new byte[0]);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Utils.md5sum(file));

        Files.writeString(file, "a".repeat(20000));
        assertEquals("9bb57f821953f3c232116e38badc8e96", Utils.md5sum(file));
    }

    @Test
    void missingFileProducesAnIoFailure() {
        assertThrows(IOException.class, () -> Utils.md5sum(directory.resolve("missing")));
    }
}
