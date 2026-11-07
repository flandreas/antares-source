# Jabbah JVM artifacts

The repository keeps the existing modules as the eventual core modules and adds UI companion modules:

- `:base` + `:base-ui`
- `:draw` + `:draw-ui`
- `:edit` + `:edit-ui`
- `:execution` + `:execution-ui`
- `:app` + `:app-ui`
- `:graph` + `:graph-ui`
- `:antares` + `:antares-ui`

All seven UI companions now contain their JVM desktop implementations. The lower companions own Swing utilities, preferences, property sheets, canvases, inline text editing, execution controls, application windows, dialogs, and desktop actions. `graph-ui` and `antares-ui` own the graph and Antares desktop implementations.

Desktop setup is provided by `BaseModuleUiJvm`, `DrawModuleUiJvm`, `EditModuleUiJvm`, `ExecutionModuleUiJvm`, `AppModuleUiJvm`, and `GraphModuleUiJvm`. Their corresponding core JVM bootstraps retain platform services, model setup, rendering, and persistence. The desktop startup requires `AppModuleUiJvm` before opening workspace dialogs, and graph UI setup requires the lower UI bootstraps before registering graph factories, editors, preferences, and health checks.

`BaseModuleUiJvm` installs the Swing event dispatcher, Swing timers, browser dialogs, key codes, and preferences. Core JVM applications use synchronous invocation and daemon JDK timers by default. Core text creation uses `SimpleTextComponent`; the desktop editor installs `TextComponentFactoryJvm` for inline editing.

JVM file persistence, library import services, nonvolatile storage, HDL generation, CSV handling, and offscreen raster export remain in the core projects. SVG image loading/rendering and its JSVG dependency reside in `draw-ui`; `DrawModuleUiJvm` installs the SVG-capable image loader. Core retains raster image loading and an AWT image rendering interface, so offscreen rendering can draw images supplied by UI implementations. Core-only image loading rejects vector images. SVG export and its Batik dependencies reside in the UI companions. Ktor HTTP clients for error reporting, ratings, login, project upload, and OpenRouter reside in the corresponding UI companions, together with all Ktor client dependencies (including the Apache engine). Core retains the service interfaces and backend request data; desktop callers access error reporting through `BaseModuleUiJvm.unexpectedErrorService` and ratings through `AppModuleUiJvm.ratingService`. JVM `img/` resources reside in each module's UI companion, preserving their existing `/img/...` classpath paths. Installer-only icons, backgrounds, and the custom `Info.plist` reside in `antares-ui/installer`, outside runtime resources and both library JARs. Other shared, JS, and core resources retain their existing locations.

Backend consumers that need SVG rendering, such as Akrab's thumbnail service, can declare JSVG directly and wrap its document in an implementation of core's `DrawableImageJvm`. `Graphics2DJvm` draws these images through that interface. This keeps backend SVG support independent of UI modules; the bundled `SvgImageJvm` and `ImageLoaderUiJvm` implementations belong to `draw-ui`.

```text
./gradlew :base-ui:jvmJar :draw-ui:jvmJar :edit-ui:jvmJar :execution-ui:jvmJar :app-ui:jvmJar :graph-ui:jvmJar :antares-ui:jvmJar
```

Each UI companion produces its own `*-ui-jvm-<version>.jar` under `build/<module>/libs/` and depends on its corresponding core module.

The aggregate tasks produce:

- `build/jabbah/libs/jabbah-core-<version>.jar`
- `build/jabbah/libs/jabbah-ui-<version>.jar`

Build both aggregate artifacts with `./gradlew coreJar uiJar`. These contain project classes and resources, without bundling third-party dependencies. The UI aggregate contains desktop implementations from all seven companions and their `img/` resources; the core aggregate excludes those images and Antares' `test/` resources. Swing-only dependencies (including FlatLaf, SwingX, the property sheet, font chooser, Markdown viewer/editor, Swing coroutines dispatcher, Batik, and JSVG) are assigned only to UI projects. Core still contains portable view code and resources outside `img/`; it is not yet a fully UI-free backend artifact. AWT geometry and offscreen rendering remain available in core.

Only `jabbah-core` is configured for GitHub Packages publishing, as `io.antarescircuit:jabbah-core`. Its Maven version uses the separate `libraryVersion` property and defaults to `2.5.0-rc.1`. See [Publishing Jabbah Core](publishing.md) for local validation, authentication, and publishing releases. Publications are unsigned.

The backend should eventually use only the existing core modules. The Swing application uses core and UI modules. JVM tests in the original projects can depend on UI projects through test-only dependencies, without introducing a production dependency from core to UI.

Desktop build and installer tasks belong to `antares-ui`:

```text
./gradlew :antares-ui:shadowCreate
./gradlew :antares-ui:run
./gradlew :antares-ui:prepareInstallerInput
```

The executable JAR is `build/antares-ui/libs/antares-<version>-all.jar`. `prepareInstallerInput` stages only that JAR and splash images in `build/antares-ui/installer-input`, so jpackage does not bundle the other module JARs or metadata from `libs`. Native installers go to `build/antares-ui/distributions`.

`packageMac`, `signDmgMac`, `notarizeMac`, `stapleMacNotarization`, and the `distributeMac` / `distributeWindows` / `distributeLinux` tasks also live in `antares-ui`. Installer JDK paths and Apple signing/notarization values remain in the developer's local Gradle properties and are read only when the corresponding installer tasks are configured. Stapling remains a manual follow-up after notarization.

Existing `:antares:shadowCreate`, `:antares:copySplash`, and native installer task names forward to `antares-ui`. `:antares:run --args=...` remains supported and launches the desktop JAR built by `antares-ui`. The legacy `copySplash` task now delegates to installer input staging.

The remaining migration concerns portable view abstractions and resources outside `img/`. UI-specific tests reside with their implementations, and shared editor fixtures are supplied by `edit-test-util`. Production core projects have no dependency on a UI companion.
