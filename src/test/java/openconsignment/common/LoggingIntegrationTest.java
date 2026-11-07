package openconsignment.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoggingIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void flushesSlf4jMessagesAcrossCompressedRollovers() throws Exception {
        runProbe();

        Path logDir = tempDir.resolve("logs");
        List<Path> files;
        try (var stream = Files.list(logDir)) {
            files = stream.toList();
        }
        assertTrue(files.stream().anyMatch(path -> path.toString().endsWith(".gz")), "Expected compressed archives");
        StringBuilder logs = new StringBuilder();
        for (Path file : files) {
            if (file.toString().endsWith(".gz")) {
                try (InputStream input = new GZIPInputStream(Files.newInputStream(file))) {
                    logs.append(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
            } else if (file.getFileName().toString().equals("openconsignment.log")) {
                logs.append(Files.readString(file));
            }
        }
        String content = logs.toString();
        assertTrue(content.contains("slf4j-marker 42"));
        assertTrue(content.contains("error-marker"));
        assertTrue(content.contains("IllegalStateException: probe-exception"));
        for (int i = 0; i < 80; i++) {
            assertTrue(content.contains("rotation-marker-" + i + " "), "Missing message " + i);
        }
    }

    @Test
    void rolloverDeletesOnlyExpiredMatchingArchives() throws Exception {
        Path logDir = Files.createDirectory(tempDir.resolve("logs"));
        Path expired = Files.writeString(logDir.resolve("openconsignment-2000-01-01-1.log.gz"), "old");
        Path unrelated = Files.writeString(logDir.resolve("other.log.gz"), "keep");
        FileTime old = FileTime.from(Instant.now().minus(31, ChronoUnit.DAYS));
        Files.setLastModifiedTime(expired, old);
        Files.setLastModifiedTime(unrelated, old);

        runProbe();

        assertTrue(Files.notExists(expired), "Expired archive should be removed");
        assertTrue(Files.exists(unrelated), "Unrelated files should be preserved");
    }

    @Test
    void continuesLoggingToConsoleWhenFileCannotBeCreated() throws Exception {
        Path notADirectory = Files.writeString(tempDir.resolve("not-a-directory"), "file");
        String output = runProbe(notADirectory.resolve("logs"));
        assertTrue(output.contains("error-marker"));
        assertTrue(output.contains("slf4j-marker 42"));
        assertTrue(output.contains("ERROR"), "Expected a file initialization error on stderr");
    }

    private void runProbe() throws Exception {
        runProbe(tempDir.resolve("logs"));
    }

    private String runProbe(Path logDir) throws Exception {
        String classpath = System.getProperty("logging.test.classpath");
        String jar = System.getProperty("logging.test.jar");
        if (jar != null) {
            Path testClasses = Path.of(LoggingProbe.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            classpath = testClasses + System.getProperty("path.separator") + jar;
        }
        Path output = tempDir.resolve("process-output.txt");
        Process process = new ProcessBuilder(
                        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-Dopenconsignment.logDir=" + logDir,
                        "-Dopenconsignment.logSize=1 KB",
                        "-cp",
                        classpath,
                        LoggingProbe.class.getName())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
        assertTrue(finished, "Logging process timed out");
        assertEquals(0, process.exitValue(), Files.readString(output));
        return Files.readString(output);
    }
}
