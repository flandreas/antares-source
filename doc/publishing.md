# Publishing Jabbah Core

The root project publishes one JVM library: `io.antarescircuit:jabbah-core`.
UI modules, test utilities and individual core modules have no Maven publications.
The previous Bytesafe publishing configuration has been removed.

The publication contains the aggregate core JAR, a sources JAR, a documentation JAR,
and a POM with the resolved external JVM dependencies. These dependencies are fetched
by the consumer; their classes are not bundled into the core JAR. The POM lists the
resolved dependency closure at compile scope, including dependencies exposed by core
APIs, and omits the project dependencies whose classes are already aggregated.
The documentation JAR contains the design manual and source documentation links;
it is not a generated API reference. The library requires Java 25.

## Versions

`libraryVersion` in the repository's `gradle.properties` controls the Maven version.
It defaults to `2.5.0-rc.1`. The independent `version_project` remains `2.5.0` for
desktop builds and native installers, which need a numeric version.

Use `2.5.0-rc.1`, `2.5.0-rc.2`, etc. while integrating Akrab, then `2.5.0` for the
release. Treat remote versions as immutable: changed code needs a new version.
The configuration accepts numeric releases and `-rc.N` candidates. Override the
checked-in version with `-PlibraryVersion=2.5.0-rc.2` or `-PlibraryVersion=2.5.0`.

## GitHub Packages authentication

The remote Maven repository is:

```text
https://maven.pkg.github.com/flandreas/antares-source
```

Publications are unsigned. No GPG key, Maven Central account, namespace
verification, or separate release approval is required. Maven packages inherit the
visibility and access permissions of the `flandreas/antares-source` repository.
GitHub requires authentication even to download public Maven packages.

Create a token while signed into the GitHub account that has access to this repository:

1. Open **Settings → Developer settings → Personal access tokens → Tokens (classic)**,
   or visit <https://github.com/settings/tokens>.
2. Choose **Generate new token → Generate new token (classic)**.
3. Give it a descriptive name and an expiration date.
4. Select `write:packages` and `read:packages` for publishing and consuming from this
   development machine. A build machine that only consumes Jabbah needs just
   `read:packages`. Neither `delete:packages` nor broad `repo` access is needed for
   this package workflow; if GitHub selects `repo` automatically, deselect it.
5. Generate the token and copy it immediately; GitHub only displays it once.
   If an organization requires SSO, authorize the token for that organization.

Use a **classic** token: GitHub Packages does not support fine-grained PATs.
The token's owner must have write access to the hosting repository to publish,
and read access to consume a private package.

Put the following in your user-local `~/.gradle/gradle.properties`, shared by the
Jabbah and Akrab builds on this machine:

```properties
gpr.user=flandreas
gpr.key=YOUR_GITHUB_CLASSIC_PERSONAL_ACCESS_TOKEN
```

Use the username of the account that owns the token if different. Keep this file
private and never add the token to either repository's `gradle.properties`.
The builds also accept `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables;
Gradle properties take precedence.

For Jabbah's own GitHub Actions workflow, the built-in `GITHUB_TOKEN` can publish
with workflow permissions `contents: read` and `packages: write`, without a PAT.
Set `GITHUB_ACTOR` and `GITHUB_TOKEN` in the publishing step's environment.
For Akrab CI, supply a read-only classic PAT as a CI secret when its workflow token
cannot access this repository's package. These credentials are needed during the
build, not by the deployed backend when its dependencies are packaged.

See GitHub's [Gradle registry documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)
and [personal access token instructions](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

## Local development and validation

With JDK 25 selected:

```sh
./gradlew publishCoreForValidation
```

This writes an unsigned Maven repository under
`build/jabbah/publishing/core-validation`. It requires no credentials and does not
contact GitHub Packages or write to `~/.m2`.

Akrab supports an exclusive local override for this module. In the Akrab checkout:

```sh
./gradlew test -PjabbahValidationRepository=/Users/andreas/Documents/scorpion2/jabbah/build/jabbah/publishing/core-validation
```

The override also applies to `build`, `run`, and IDE builds. To use it persistently
on this machine, add the following to user-local `~/.gradle/gradle.properties`:

```properties
jabbahValidationRepository=/Users/andreas/Documents/scorpion2/jabbah/build/jabbah/publishing/core-validation
```

Do not commit a machine-specific path. Remove the user-local property, or pass
`-PjabbahValidationRepository=` to Akrab, to consume GitHub Packages instead.
The local override never falls back to a remote copy of `jabbah-core`.
Use a fresh candidate number when changing code; when intentionally rebuilding the
same local version, use `--refresh-dependencies` in Akrab to refresh Gradle's cache.

## Publish to GitHub Packages

Run local publication and Akrab integration checks first, then publish with JDK 25:

```sh
./gradlew publishCore -PlibraryVersion=2.5.0-rc.1
```

This checks credentials and uploads only the `core` publication directly to GitHub
Packages. For the final release, use `-PlibraryVersion=2.5.0`. No Portal visit or
manual promotion follows. The package appears in the hosting repository's
**Packages** section after the first successful upload; no package needs to be
created manually beforehand.

The generic `publish` task publishes to all configured repositories, including
GitHub Packages and the local validation repository. Prefer `publishCore` for
remote uploads and `publishCoreForValidation` for local checks.
Old `mavenCentralUsername`, `mavenCentralPassword`, and signing properties are no
longer used by this build.

## Consume from GitHub Packages

Akrab's `settings.gradle.kts` routes `io.antarescircuit:jabbah-core` exclusively to
GitHub Packages unless the local override is set. Other dependencies continue to
resolve from Maven Central. Set Akrab's `jabbahVersion` to the version you published:

```sh
./gradlew build -PjabbahVersion=2.5.0-rc.1 -PjabbahValidationRepository=
```

Other Gradle consumers can configure:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/flandreas/antares-source")
        credentials {
            username = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
            password = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
        }
        content { includeModule("io.antarescircuit", "jabbah-core") }
    }
    mavenCentral()
}

dependencies {
    implementation("io.antarescircuit:jabbah-core:2.5.0-rc.1")
}
```
