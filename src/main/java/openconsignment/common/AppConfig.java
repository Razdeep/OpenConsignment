package openconsignment.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Application settings bound from YAML once at startup. */
@Getter
@Setter
@NoArgsConstructor
public class AppConfig {
    private String organizationName;

    public static AppConfig forOrganization(String organizationName) {
        AppConfig configuration = new AppConfig();
        configuration.setOrganizationName(organizationName);
        configuration.validate();
        return configuration;
    }

    public String organizationName() {
        return organizationName;
    }

    public void validate() {
        if (organizationName == null || organizationName.isBlank()) {
            throw new IllegalArgumentException("organizationName must not be blank");
        }
        organizationName = organizationName.strip();
    }
}
