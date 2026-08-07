# Repository Guidelines

## Project Structure & Module Organization

Jabbah is a Kotlin Multiplatform Gradle project. Domain code is split across modules such as `base`, `io`, `animation`, `draw`, `edit`, `execution`, `app`, and `graph`; `antares` contains the Swing desktop application. Companion `*-test-util` modules provide reusable test fixtures and mocks.

Within each module, portable code and tests live in `shared/src/main` and `shared/src/test`. JVM-specific sources and resources use `jvm/src/main`, `jvm/src/test`, and `jvm/rsc`; JavaScript code uses `js/src/kotlin`. Documentation is under `doc/`, bundled libraries under `lib/`, and sample circuits under `antares/samples/`. Consult `doc/design-manual/overview.md` before changing module boundaries.

## Build, Test, and Development Commands

- `./gradlew jvmMainClasses` compiles all JVM production sources.
- `./gradlew jvmTestClasses` compiles JVM tests without running them.
- `./gradlew jvmTest` runs the complete JVM test suite.
- `./gradlew :animation:jvmTest` runs tests for one module; replace `animation` as needed.
- `./gradlew :antares:run` builds and starts the Swing application.

The root `build` task is currently affected by a known JS/mock issue, so use the JVM tasks for routine validation. The project targets JVM 25; use a compatible JDK.

## Coding Style & Naming Conventions

Follow the existing Kotlin style: tabs for indentation, braces on the declaration line, and trailing commas in multiline argument lists. Keep packages under `io.antarescircuit.jabbah`; use `PascalCase` for types, `camelCase` for functions and properties, and descriptive interface/implementation pairs such as `Animator` and `AnimatorImpl`. Keep platform-neutral behavior in `shared` and isolate Swing/JVM dependencies in `jvm`. No repository-wide formatter is configured, so match neighboring code.

## Testing Guidelines

Tests use `kotlin.test` annotations and assertions; mocks and shared helpers are supplied by Mokkery and the test-util modules. Name test classes `*Test` and test functions as behavior statements, for example `shouldAlwaysReturnEndWhenGoingForward`. Add tests beside the relevant source set and run the narrow module task before `./gradlew jvmTest`.

## Commit & Pull Request Guidelines

Recent commits use concise, imperative summaries and often begin with an issue reference, such as `#1235: Missing view updates during simulation`. The README also prescribes `I-1234: ...` for internal/developer issues. Keep each commit focused. Pull requests should explain the problem and solution, link the issue, list validation commands, and include screenshots or recordings for visible Swing UI changes.

## Security & Configuration

Do not commit credentials. Publishing and Apple notarization values belong in the developer's local Gradle properties, not the repository's `gradle.properties`.
