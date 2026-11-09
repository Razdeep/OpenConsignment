package openconsignment.di;

import dagger.Module;
import dagger.Provides;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Singleton;
import openconsignment.common.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

/** Creates the external configuration on first startup and provides validated settings from it. */
@Module
public final class ConfigurationModule {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);

    private static final String CONFIGURATION_FILE = "application.yaml";
    private static final String DEFAULTS_RESOURCE = "/" + CONFIGURATION_FILE;

    private ConfigurationModule() {}

    @Provides
    @Singleton
    static AppConfig provideAppConfig() {
        Path path = externalConfigurationPath();
        try {
            return load(path);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to load application configuration: " + path, ex);
        }
    }

    static AppConfig load(Path path) throws IOException {
        initializeConfiguration(path);
        return parseConfiguration(Files.readString(path, StandardCharsets.UTF_8), path);
    }

    private static Yaml createYamlParser() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(new Constructor(AppConfig.class, options));
    }

    private static AppConfig parseConfiguration(String content, Path path) {
        try {
            AppConfig configuration = createYamlParser().load(content);
            if (configuration == null) {
                throw new IllegalArgumentException("Configuration must not be empty");
            }
            configuration.validate();
            return configuration;
        } catch (YAMLException ex) {
            throw new YAMLException("Invalid configuration in " + path + ": " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid configuration in " + path + ": " + ex.getMessage(), ex);
        }
    }

    private static void initializeConfiguration(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }
        try (InputStream defaults = ConfigurationModule.class.getResourceAsStream(DEFAULTS_RESOURCE)) {
            if (defaults == null) {
                throw new IOException("Bundled configuration resource is missing: " + DEFAULTS_RESOURCE);
            }
            try {
                Files.copy(defaults, path);
                logger.info("Generated new application.yaml as it was missing at {}", path);
            } catch (FileAlreadyExistsException ex) {
                // Another startup created the file; preserve it and load its contents.
            }
        }
    }

    private static Path externalConfigurationPath() {
        try {
            Path location = Path.of(ConfigurationModule.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return externalConfigurationPath(location, Path.of("").toAbsolutePath());
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Unable to resolve application location", ex);
        }
    }

    static Path externalConfigurationPath(Path codeLocation, Path workingDirectory) {
        Path directory = Files.isRegularFile(codeLocation) ? codeLocation.getParent() : workingDirectory;
        return directory.resolve(CONFIGURATION_FILE).normalize();
    }
}
