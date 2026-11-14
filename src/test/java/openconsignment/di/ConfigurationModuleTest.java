package openconsignment.di;

import static openconsignment.di.ConfigurationModule.load;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import openconsignment.common.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.error.YAMLException;

class ConfigurationModuleTest {
    @TempDir
    Path tempDir;

    @Test
    void missingFileIsCopiedFromBundledResourceAndLoaded() throws IOException {
        Path file = tempDir.resolve("application.yaml");
        String bundled;
        try (InputStream resource = ConfigurationModule.class.getResourceAsStream("/application.yaml")) {
            bundled = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }

        AppConfig configuration = load(file);

        assertEquals(bundled, Files.readString(file));
        assertEquals(configuration.getOrganization(), load(file).getOrganization());
    }

    @Test
    void emptyExistingFileIsRejectedAndPreserved() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "# Empty configuration\n");

        assertThrows(IllegalArgumentException.class, () -> load(file));
        assertEquals("# Empty configuration\n", Files.readString(file));
    }

    @Test
    void loadsUtf8OrganizationAndTrimsWhitespace() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: \"  Café Agencies  \"\n");

        assertEquals("Café Agencies", load(file).getOrganization());
    }

    @Test
    void loadedConfigurationDoesNotChangeWhenFileIsEdited() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: First\n");
        AppConfig configuration = load(file);

        Files.writeString(file, "organization: Second\n");

        assertEquals("First", configuration.getOrganization());
        assertEquals("Second", load(file).getOrganization());
    }

    @Test
    void rejectsBlankOrganization() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: \"   \"\n");

        assertThrows(IllegalArgumentException.class, () -> load(file));
    }

    @Test
    void rejectsMalformedYaml() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: [\n");

        assertThrows(YAMLException.class, () -> load(file));
    }

    @Test
    void missingOrganizationNameIsRejectedWithoutBundledFallback() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "{}\n");

        assertThrows(IllegalArgumentException.class, () -> load(file));
        assertEquals("{}\n", Files.readString(file));
    }

    @Test
    void existingConfigurationIsLoadedWithoutBeingOverwritten() throws IOException {
        String content = "# Custom settings\norganization: External Organization\n";
        Path file = Files.writeString(tempDir.resolve("application.yaml"), content);

        assertEquals("External Organization", load(file).getOrganization());
        assertEquals(content, Files.readString(file));
    }

    @Test
    void malformedConfigurationErrorIdentifiesSourceFile() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: [\n");

        YAMLException error = assertThrows(YAMLException.class, () -> load(file));

        assertTrue(error.getMessage().contains(file.toString()));
    }

    @Test
    void missingDestinationDirectoryReportsCreationFailure() {
        Path file = tempDir.resolve("missing-directory/application.yaml");

        assertThrows(IOException.class, () -> load(file));
    }

    @Test
    void rejectsUnknownProperties() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organizationNmae: Typo\n");

        assertThrows(YAMLException.class, () -> load(file));
    }

    @Test
    void packagedApplicationUsesConfigurationNextToJar() throws IOException {
        Path jar = Files.createFile(tempDir.resolve("OpenConsignment.jar"));

        assertEquals(
                tempDir.resolve("application.yaml"),
                ConfigurationModule.externalConfigurationPath(jar, tempDir.resolve("other-working-directory")));
    }

    @Test
    void developmentRunUsesConfigurationInWorkingDirectory() throws IOException {
        Path classes = Files.createDirectory(tempDir.resolve("classes"));
        Path workingDirectory = tempDir.resolve("working-directory");

        assertEquals(
                workingDirectory.resolve("application.yaml"),
                ConfigurationModule.externalConfigurationPath(classes, workingDirectory));
    }

    @Test
    void bindsNumericScalarToStringProperty() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: 123\n");

        assertEquals("123", load(file).getOrganization());
    }

    @Test
    void rejectsMappingOrganizationName() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: {name: Example}\n");

        assertThrows(YAMLException.class, () -> load(file));
    }

    @Test
    void rejectsNonMappingDocument() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "- Example\n");

        assertThrows(YAMLException.class, () -> load(file));
    }

    @Test
    void rejectsNullOrganizationName() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: null\n");

        assertThrows(IllegalArgumentException.class, () -> load(file));
    }

    @Test
    void rejectsEmptyOrganizationName() throws IOException {
        Path file = Files.writeString(tempDir.resolve("application.yaml"), "organization: \"\"\n");

        assertThrows(IllegalArgumentException.class, () -> load(file));
    }

    @Test
    void rejectsDuplicateKeys() throws IOException {
        Path file =
                Files.writeString(tempDir.resolve("application.yaml"), "organization: First\norganization: Second\n");

        assertThrows(YAMLException.class, () -> load(file));
    }

    @Test
    void reportsReadErrorsInsteadOfSilentlyUsingDefaults() {
        assertThrows(IOException.class, () -> load(tempDir));
    }
}
