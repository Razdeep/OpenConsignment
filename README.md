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
  (`database.db`), with helper scripts to configure periodic backups to a
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

- JDK 17+

### Run from source

```shell
./gradlew run
```

### Build a runnable JAR

```shell
./gradlew shadowJar
```

This produces `build/libs/OpenConsignment.jar`. Since the shadow JAR does not
bundle the JavaFX native modules, run it with a JavaFX SDK on the module path
— `scripts/start.sh` (macOS/Linux) and `scripts/start.bat` (Windows) do this
for you, defaulting to `~/javafx-sdk-17.0.19` (override via the
`JAVAFX_MODULE_PATH` environment variable):

```shell
./scripts/start.sh
```

## Testing

```shell
./gradlew test
```

- **Unit tests** (`src/test/java/openconsignment/service`,
  `.../repository`) cover the service layer (mocked repositories, mocked
  static `Utils.generatePdf` calls) and the repository layer (real
  in-memory SQLite, schema-initialized from `src/main/resources/sql/init.sql`).
- **Functional tests** (`src/test/java/functional`) use TestFX to smoke-test
  the JavaFX UI.

Every `test` run also produces a JaCoCo coverage report at
`build/reports/jacoco/test/html/index.html`.

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
└── sql/          Database schema (init.sql)
```

## Backups

`scripts/configure-backup.py` generates year-scoped `upload`/`download`
scripts (from `scripts/templates`) for backing up `database.db` to a
private, per-financial-year Git repository. Run it once per financial year:

```shell
python3 scripts/configure-backup.py
```

## CI/CD

GitHub Actions builds and tests every push/PR (`.github/workflows/build.yml`).
Pushing a `v*` tag builds and publishes a shadow JAR to a GitHub Release
(`.github/workflows/release.yml`).

## License

[GPL-3.0](LICENSE)
