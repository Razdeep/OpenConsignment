package openconsignment.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A separate JVM exercises the same startup, configuration and shutdown as the application. */
public class LoggingProbe {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(LoggingProbe.class);
        logger.info("slf4j-marker {}", 42);
        logger.error("error-marker", new IllegalStateException("probe-exception"));
        for (int i = 0; i < 80; i++) {
            logger.info("rotation-marker-{} {}", i, "x".repeat(200));
        }
    }
}
