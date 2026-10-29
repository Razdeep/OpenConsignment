package openconsignment.common;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    public static void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

    private static AlertType getAlertTypeFromCustomValue(int choice) {
        switch (choice) {
            case 0:
                return AlertType.ERROR;
            case 1:
                return AlertType.INFORMATION;
            case 2:
                return AlertType.WARNING;
            case 3:
                return AlertType.CONFIRMATION;
            default:
                showAlert("Programming Error. Select 1->error 2->info 3->warn 4->confirm");
        }
        return AlertType.NONE;
    }

    public static void showAlert(String message, int choice) {
        Alert alert = new Alert(getAlertTypeFromCustomValue(choice), message);
        alert.show();
    }

    public static void launchPdf(String filename) throws IOException {
        File pdfFile = new File(filename);

        if (!pdfFile.exists()) {
            throw new IOException("File does not exist: " + pdfFile.getAbsolutePath());
        }

        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop is not supported on this platform.");
        }

        CompletableFuture.runAsync(() -> {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error opening file: " + pdfFile.getAbsolutePath(), e);
            }
        });
    }

    public static void launchBrowser(String url) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop is not supported on this platform.");
        }

        CompletableFuture.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                logger.log(Level.WARNING, "Error opening browser link " + url, e);
            }
        });
    }

    public static String formatDate(String dateString) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);
        try {
            LocalDate date = LocalDate.parse(dateString, inputFormatter);
            return date.format(outputFormatter);
        } catch (DateTimeParseException ex) {
            try {
                // already in desired format
                LocalDate.parse(dateString, outputFormatter);
                return dateString;
            } catch (DateTimeParseException ex2) {
                logger.warning(ex2.toString());
                throw ex2;
            }
        }
    }

    public static LocalDate parseDate(String dateString) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateString, inputFormatter);
    }

    public static void generatePdf(String jrxmlFileName, Map<String, Object> payload, List<?> dataRows)
            throws JRException {
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataRows);

        String jrxmlFilePath = "/jrxml/" + jrxmlFileName;

        try {
            JasperReport jasperReport =
                    JasperCompileManager.compileReport(Utils.class.getResourceAsStream(jrxmlFilePath));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, payload, dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, Constants.REPORT_FILE_NAME);
        } catch (JRException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public static String nextVoucherNumber(String lastVoucher) {
        if (null == lastVoucher) {
            return "0";
        }
        int paddingLength = lastVoucher.length();
        String format = "%0" + paddingLength + "d";
        try {
            int number = Integer.parseInt(lastVoucher);
            return String.format(format, number + 1);
        } catch (NumberFormatException ex) {
            return "0";
        }
    }
}
