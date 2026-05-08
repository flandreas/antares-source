# Antares

Antares is a digital circuit learning platform entirely written in Kotlin.

Checkout the [project's home page](https://www.antarescircuit.io) with installers, documentation and examples.

Antares aims to provide tools to create circuits with embedded explanations. The current version consists of a Swing-based desktop application. Future versions will also be available in the browser.

The Antares application universe currently consists of the following components, which are named after stars in the star constellation "Scorpius":
- **Jabbah**: The core domain framework (contained in this project)
- **Antares Desktop**: The Java/Swing client (based on Jabbah) users can download and use for free
- **Shaula**: The upcoming Angular web app users can use to register an account to which they can upload circuits
- **Web circuit viewer**: An Angular app for displaying web-hosted Antares circuits
- **Akrab**: The server backend used by Shaula and the web circuit viewer

This repository contains only the source code for "Antares Desktop" and the core domain logic used by the other components. Shaula, Akrab and the web circuit viewer are maintained in separate (currently still private) GitHub projects.

This project is set up as a Kotlin multi-platform project with the following targets:
- shared
- jvm: Used for Antares Desktop and Akrab
- js: Used for Shaula and the web circuit viewer

The project uses Gradle subprojects to separate the individual top-level modules like "draw", "edit" or "graph".

## Issue tracking

- Internal issues are tracked in the [developer GitHub project](https://github.com/flandreas/antares-source/issues)
- Enduser-facing issues are tracked in the [homepage GitHub project](https://github.com/flandreas/antares/issues)

When committing changes to the git repository, start your commit comment with "I-" for developer/internal issues, e.g. "I-1234: Fixed compiler warnings".

## Developing

See the [Design Manual](doc/design-manual/overview.md) for a quick overview of the application's code structures. This is not a comprehensive developer's manual, but rather a list of pointers to the most important packages and domain model classes and interfaces.

## Build

`gradlew build` currently fails due to a [JS issue with mockk](https://github.com/flandreas/antares-source/issues/2). The DEV workaround is to use `gradlew jvmMainClasses` and `gradlew jvmTestClasses` during development.

## Test

`gradlew jvmTest` runs all tests on the JVM platform.
 
## Run

The Antares Desktop JVM application is run with `io.antarescircuit.antares.AntaresSwing`. 

Use the following JVM program arguments to set up your environment as developer:

```
-env dev -developer -d <dir> -sl <projectRoot>/antares/jvm/rsc
```

- `-env env`: Establishes e.g. URLs to use local Akrab instance, if needed
- `-developer`: Runs Antares in developer mode (e.g. enables the "Developer" menu). This enables the "Developer" menu with useful tools, but also avoid sending an email containing the log and a stacktrace to the developer email address if an unexpected error occurs.
- `-d`: Path to a application data repository other than the one used by Antares installed with the downloaded installer. Replace `<dir>` with the path to your local directory, e.g. `/Users/andreas/Antares-dev`.
- `-sl`: Path to the checked-in standard libraries. Needed when updating standard library circuits. Replace `<projectRoot>` with the absolute path to your cloned project, e.g. `/Users/andreas/Documents/scorpion2/jabbah`.

## Maintain

See the separate [Maintainer documentation](MAINTAINER.md).
 
