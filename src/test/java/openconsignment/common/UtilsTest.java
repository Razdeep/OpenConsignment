package openconsignment.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class UtilsTest {

    @AfterEach
    void cleanUpGeneratedReport() throws IOException {
        Files.deleteIfExists(new File(Constants.REPORT_FILE_NAME).toPath());
    }

    @Test
    void parseDateParsesDdMmYyyy() {
        assertEquals(LocalDate.of(2026, 1, 15), Utils.parseDate("15-01-2026"));
    }

    @Test
    void parseDateThrowsForMalformedInput() {
        assertThrows(DateTimeParseException.class, () -> Utils.parseDate("2026-01-15"));
    }

    @Test
    void formatDateConvertsIsoToDdMmYyyy() {
        assertEquals("15-01-2026", Utils.formatDate("2026-01-15"));
    }

    @Test
    void formatDateReturnsInputUnchangedWhenAlreadyInOutputFormat() {
        assertEquals("15-01-2026", Utils.formatDate("15-01-2026"));
    }

    @Test
    void formatDateThrowsForUnrecognizedInput() {
        assertThrows(DateTimeParseException.class, () -> Utils.formatDate("not-a-date"));
    }

    @Test
    void generatePdfThrowsForUnknownTemplate() {
        assertThrows(JRException.class, () -> Utils.generatePdf("DoesNotExist.jrxml", Map.of(), List.of()));
    }

    @Test
    void generatePdfCompilesFillsAndExportsARealPdf() throws JRException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("voucherNo", "V1");
        payload.put("voucherDate", "01-01-2026");
        payload.put("buyerName", "Buyer A");
        payload.put("billAmount", "1000");

        Utils.generatePdf("Collection.jrxml", payload, List.of());

        File report = new File(Constants.REPORT_FILE_NAME);
        assertTrue(report.isFile());
        assertTrue(report.length() > 0);
    }

    @Test
    void launchPdfThrowsWhenFileDoesNotExist() {
        IOException ex = assertThrows(IOException.class, () -> Utils.launchPdf("does-not-exist.pdf"));
        assertTrue(ex.getMessage().contains("File does not exist"));
    }

    @Test
    void launchPdfThrowsWhenDesktopUnsupported(@TempDir Path tempDir) throws IOException {
        File pdfFile = tempDir.resolve("report.pdf").toFile();
        Files.writeString(pdfFile.toPath(), "stub");

        try (MockedStatic<Desktop> desktop = mockStatic(Desktop.class)) {
            desktop.when(Desktop::isDesktopSupported).thenReturn(false);

            IOException ex = assertThrows(IOException.class, () -> Utils.launchPdf(pdfFile.getPath()));
            assertTrue(ex.getMessage().contains("Desktop is not supported"));
        }
    }

    @Test
    void launchBrowserThrowsWhenDesktopUnsupported() {
        try (MockedStatic<Desktop> desktop = mockStatic(Desktop.class)) {
            desktop.when(Desktop::isDesktopSupported).thenReturn(false);

            assertThrows(IOException.class, () -> Utils.launchBrowser("https://example.com"));
        }
    }
}
