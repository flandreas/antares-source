# Antares

Antares is a digital circuit learning platform entirely written in Kotlin.

Antares aims to provide tools to create circuits with embedded explanations. The current version consists of a Swing-based desktop application. Future versions will also be available in the browser.

The Antares application universe currently consists of the following components, which are named after stars in the star constellation "Scorpius":
- **Jabbah**: The core domain framework (contained in this project)
- **Antares Desktop**: The Java/Swing client (based on Jabbah) users can download and use for free
- **Shaula**: The upcoming Angular web app users can use to register an account to which they can upload circuits
- **Web circuit viewer**: An Angular app for displaying web-hosted Antares circuits
- **Akrab**: The server backend used by Shaula and the web circuit viewer

This repository contains only the source code for "Antares Desktop" and the core domain logic used by the other components. Shaula, Akrab and the web circuit viewer are maintained in separate GitHub projects.

This project is set up as a Kotlin multi-platform project with the following targets:
- shared
- jvm: Used for Antares Desktop and Akrab
- js: Used for Shaula and the web circuit viewer

The project uses gradle subprojects to separate the individual top-level modules like "draw", "edit" or "graph".

## Issue tracking

- Internal issues are tracked in the [private GitHub project](https://github.com/flandreas/antares-source/issues)
- Enduser-facing issues are tracked in the [public GitHub project](https://github.com/flandreas/antares/issues)

## Building

`gradlew build` currently fails due to a [JS issue with mockk](https://github.com/flandreas/antares-source/issues/2). The DEV workaround is to use `gradlew jvmMainClasses` and `gradlew run` during development.
 
## Developing

Use the following JVM program arguments to set up your environment as developer:

```
-env dev -developer -d <dir> -sl <projectRoot>/antares/jvm/rsc
```

- `-env env`: Establishes e.g. URLs to use local Akrab instance, if needed
- `-developer`: Runs Antares in developer mode (e.g. enables the "Developer" menu)
- `-d`: Path to a application data repository other than the one used by Antares installed with the downloaded
installer. Replace `<dir>` with the path to your local directory, e.g. `/Users/andreas/Antares-dev`.
- `-sl`: Path to the checked-in standard libraries. Needed when updating standard library circuits. Replace `<projectRoot>` with the absolute path to your cloned project, e.g. `/Users/andreas/Documents/scorpion2/jabbah`.

The main class to be used in run configurations (e.g. in IntelliJ) is `io.antarescircuit.antares.AntaresSwing`.

## Maintaining

See the separate [Maintainer documentation](MAINTAINER.md).
 
