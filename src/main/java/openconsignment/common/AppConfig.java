package openconsignment.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Application settings bound from YAML once at startup. */
@Getter
@Setter
@NoArgsConstructor
public class AppConfig {
    private String organization;
    private String year;
    private Github github;

    public static AppConfig forOrganization(String organizationName) {
        AppConfig configuration = new AppConfig();
        configuration.setOrganization(organizationName);
        configuration.validate();
        return configuration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Github {
        private String repository;
        private String token;
        private String username;
        private String branch;
        private String remote;
    }

    public void validate() {
        if (organization == null || organization.isBlank()) {
            throw new IllegalArgumentException("organization must not be blank");
        }
        organization = organization.strip();
    }
}
