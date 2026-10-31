# OpenConsignment

[![Build](https://github.com/razdeep/openconsignment/actions/workflows/build.yml/badge.svg)](https://github.com/razdeep/openconsignment/actions/workflows/build.yml)
[![Release](https://github.com/razdeep/openconsignment/actions/workflows/release.yml/badge.svg)](https://github.com/razdeep/openconsignment/actions/workflows/release.yml)
[![License: GPL v3](https://img.shields.io/github/license/razdeep/openconsignment)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![JavaFX 17](https://img.shields.io/badge/JavaFX-17-blue)](https://openjfx.io/)

<img src="assets/opaque_logo.png" alt="Logo" width="200">

OpenConsignment is a JavaFX desktop application for managing consignment sale
data — buyers, suppliers, transports, bills, collections, and payments — with
SQLite storage and PDF ledger reports.

## Features

- **Master data** — maintain buyers, suppliers, and transports.
- **Bill entry** — record consignment bills, including LR (lorry receipt)
  details, with an editable history view.
- **Collections & payments** — record buyer collections and supplier
  payments against pending bills, with dedicated history views.
- **Ledger reports** — generate buyer, supplier, and transport ledgers as
  PDFs (via JasperReports), including agewise-outstanding variants.
- **Local-first storage** — data is kept in a local SQLite database
  (`database.db`), with helper scripts to configure backups to a
  private Git repository (see [Backups](#backups)).

## Tech stack

- Java 17, JavaFX 17
- [Dagger 2](https://dagger.dev/) for dependency injection (see
  `src/main/java/openconsignment/di`)
- SQLite (via `sqlite-jdbc`) for storage
- JasperReports for PDF report generation
- Gradle (Kotlin DSL) with the Shadow plugin for a single runnable JAR
- JUnit 5, Mockito, and TestFX for testing; Spotless (`palantir-java-format`)
  for formatting; JaCoCo for coverage

## Getting started

### Prerequisites

- JDK 17, with `java` available on your `PATH`.
- A graphical desktop environment to run the application, and a PDF viewer
  to open generated reports.

The repository includes the Gradle wrapper; a separate Gradle installation
is not required. Gradle resolves JavaFX dependencies when running from source.

### Run from source

```shell
./gradlew run
```

Run commands from the repository root. On Windows, use `gradlew.bat` in
place of `./gradlew`.

### Build a runnable JAR

```shell
./gradlew shadowJar
```

This produces `build/libs/OpenConsignment.jar` and copies the launch scripts,
backup configuration script, and backup templates into `build/libs/`.
The launch scripts require a separate JavaFX 17 SDK matching your operating
system and CPU architecture.

On macOS/Linux, the launcher defaults to `~/javafx-sdk-17.0.19/lib`:

```shell
bash build/libs/start.sh
```

To use a different SDK location, set `JAVAFX_MODULE_PATH` to its `lib` directory:

```shell
JAVAFX_MODULE_PATH="/path/to/javafx-sdk-17/lib" bash build/libs/start.sh
```

On Windows, edit `build/libs/start.bat` to match your local Java and JavaFX
installations, then run it from `build/libs/`:

```bat
cd build\libs
start.bat
```

The batch script currently sets Java to `C:\Program Files\Java\jdk-17`
and the JavaFX module path to `%USERPROFILE%\javafx-sdk-17.0.18\lib`.
Keep the JAR and launch script together when moving the application.

## Data and reports

- The application creates and initializes `database.db` in its working
  directory using `src/main/resources/sql/init.sql`.
- Repository queries live in domain-specific folders under
  `src/main/resources/sql` (for example, `bill/get.sql`). Paths are registered
  in the `SqlQuery` enum and loaded with `SqlLoader.load(SqlQuery.BILL_GET)`,
  using UTF-8 decoding and caching. When adding a query, add its SQL file and
  enum entry; tests verify that every entry has a non-empty resource.
- Running `./gradlew run` uses the repository root; the packaged launch
  commands above use `build/libs/`. Each location has its own database.
- Reports are written to `report.pdf` in the working directory, replacing
  the previous report, and opened with the system's default PDF viewer.
- The report organization name is configured in
  `src/main/java/openconsignment/common/Constants.java`.

## Testing

```shell
./gradlew test
```

- **Unit tests** (`src/test/java/openconsignment/service`,
  `.../repository`) cover the service layer (mocked repositories, mocked
  static `Utils.generatePdf` calls) and the repository layer (real
  in-memory SQLite, schema-initialized from `src/main/resources/sql/init.sql`).
- **Functional tests** (`src/test/java/functional`) use TestFX to smoke-test
  the JavaFX UI and check bill-entry layout.

Tests run headlessly by default. To debug with visible JavaFX windows:

```shell
./gradlew test -PheadlessTests=false
```

To run a specific group of tests:

```shell
./gradlew test --tests 'openconsignment.service.*'
./gradlew test --tests 'openconsignment.repository.*'
./gradlew test --tests 'functional.*'
```

Every `test` run also produces a JaCoCo coverage report at
`build/reports/jacoco/test/html/index.html`. The test results are available at
`build/reports/tests/test/index.html`.

## Code style

Formatting is enforced by Spotless and checked on every build
(`spotlessJavaCheck`). To auto-format:

```shell
just format
# or: ./gradlew spotlessApply
```

## Project structure

```
src/main/java/openconsignment/
├── di/           Dagger modules and components (wires the object graph,
│                 including JavaFX's controller factory)
├── controller/   JavaFX FXML controllers, grouped by feature area
├── service/      Business logic, one per domain (bill, buyer, supplier, ...)
├── repository/   JDBC/SQL access, one per domain
├── entity/       Persistence-shaped data classes
├── model/        Read-model/report row classes (ledgers, etc.)
└── common/       Shared utilities (PDF generation, constants, ...)
src/main/resources/
├── fxml/         JavaFX view definitions
├── jrxml/        JasperReports report templates
├── css/          Shared and feature-specific stylesheets
├── icons/        Application and toolbar icons
└── sql/          Database schema (init.sql)
src/test/java/
├── openconsignment/  Service, repository, and dependency-injection tests
└── functional/       JavaFX UI tests
scripts/              Launchers and financial-year backup helpers
gradle/libs.versions.toml  Dependency and plugin versions
```

## Backups

`scripts/configure-backup.py` generates year-scoped `upload`/`download`
scripts (from `scripts/templates`) for backing up `database.db` to a
private, per-financial-year Git repository. It requires Python 3, Git, and
SSH access to the backup repository. The repository must already exist;
the script clones it rather than creating it.

The repository owner is currently hard-coded as `drcbabu81` in
`configure-backup.py`, with repository names such as
`Satyam-Database-2026-27`. Update the owner for your deployment.

After building the JAR, run the configuration script from `build/libs/`
so it can find `templates/` and create the helpers beside `database.db`:

```shell
cd build/libs
python3 configure-backup.py
```

Enter a financial year such as `2026-27` when prompted. On Windows, use
`python configure-backup.py` from the same directory. The script generates
`upload.sh`/`download.sh` on macOS/Linux or `upload.bat`/`download.bat` on
Windows. Run the generated helpers from that directory.

Review the templates before use: the shell upload template currently uses
a Windows-style separator in its database copy destination, which needs
adjusting for macOS/Linux. The download helper replaces the local
`database.db` with the repository copy. Backup scheduling is managed separately.

## CI/CD

GitHub Actions builds and tests every push/PR (`.github/workflows/build.yml`).
Pushing a `v*` tag builds and publishes a shadow JAR to a GitHub Release
(`.github/workflows/release.yml`).

## License

[GPL-3.0](LICENSE)
