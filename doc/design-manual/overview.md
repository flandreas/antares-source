# Design overview

This document gives an overview of the Antares application and the design of the Jabbah framework it is built on.

Design goals:
- **Multi-platform**: The framework supports developing 2D graphics application that can run on multiple platforms (currently JVM and JavaScript/browser). This is achieved by using Kotlin Multiplatform.
- **Generic low-level framework**: The low-level 2D graphics functionality is separated so that it can be used not only for Antares, but potentially also for other 2D graphics applications.
- **Abstract graph implementation**: The graph logic is separated so that it can be used not only for Antares, but potentially also for other graph-based applications.
- **Model/view separation**. Graph elements consist of separate objects for model and view representations, which allows multiple views for the same model object. Necessary e.g. for diving into a graph during simulation.

## Repository structure

The repository consists of a strictly layered stack of modules, implemented as separate Gradle subprojects in order to enforce dependencies pointing only downwards.

The package `io.antarescircuit.jabbah` is a general-purpose framework for building 2D graphics applications. Its `graph` package uses this framework to display and edit graphs consisting of edges and vertices. `io.antarescircuit.antares` applies these graph structures to implement a digital circuit editor and simulator application.

UI companion projects (`base-ui`, `draw-ui`, `edit-ui`, `execution-ui`, `app-ui`, `graph-ui`, and `antares-ui`) extend the existing modules. Production dependencies point from each UI project to core and to lower UI projects. Each companion uses the same shared/JVM/JS directory structure.

JVM desktop implementations now reside in the UI companions throughout the stack. UI-specific bootstraps (`BaseModuleUiJvm`, `DrawModuleUiJvm`, `EditModuleUiJvm`, `ExecutionModuleUiJvm`, `AppModuleUiJvm`, and `GraphModuleUiJvm`) initialize Swing adapters, factories, editors and preferences. Antares' desktop bootstrap configures them from `antares-ui`. Core JVM bootstraps retain persistence, platform services, offscreen rendering, nonvolatile storage, HDL generation and CSV handling. Swing-only library dependencies, SVG image loading/rendering with JSVG, Ktor HTTP client implementations and dependencies, and JVM `img/` resources belong to the UI projects. Desktop packaging, native installer tasks, and installer-only assets belong to `antares-ui`; installer assets are kept outside runtime resources. Portable view code and other resources outside `img/` still await migration. See [JVM artifacts](../jabbah-artifacts.md) for packaging and migration status.

## Modules

[jabbah.base](module-base.md)
: Besides general purpose functionality like the `collection` package, the main purpose of the `base` module is to provide abstractions that have to be implemented differently on the JVM and JS platform, like the `InputEvent` class in the event package.

[jabbah.io](module-io.md)
: Allows writing and reading objects graphs from and to persistent storage like XML files, including deferred reference resolution while reading.

[jabbah.animation](module-animation.md)
: A simple animation framework that can modify a value according certain patterns in order to produce an animation effect,
such as changing the alpha value of an RGBA color to produce a glow effect.

[jabbah.draw](module-draw.md)
: Provides everything necessary for displaying 2D graphics, including zooming & panning. It contains a Kotlin-common `Graphics2D` interface with drawing operations that are implemented on the JVM and JS platform for the respective `Canvas` objects, as well as interfaces for commonly used graphics primitive classes like `Color`, `Stroke`, `Font` or `Image`.

[jabbah.edit](module-edit.md)
: The `edit` module extends the `draw` module to allow a user to edit a `Drawing`, including `Component` selection,
support for undo/redo using the `Command` pattern, property editing, and a set of standard 2D `Components` like `PolylineComponent` or `TextComponent`.

[jabbah.execution](module-execution.md)
: The `execution` module supports aynchronous, time-based execution of arbitrary objects called `Actors`, controlled by a `Scheduler` and driven by a `Timer`. The Antares application uses this module to implement digital circuit simulation. 

[jabbah.app](module-app.md)
: The `app` module implements a simple application framework. Its central `AbstractDesktopApplication` class on the JVM platform can be started with command line options, loads and stores user preferences, and defines an `ApplicationData` class and various `Actions` like `OpenFileAction` or `SaveFileAction` to make it persistent.

[jabbah.graph](module-graph.md)
: The `graph` module contains a framework for editing, drawing and executing arbitrary `Graphs` consisting of `Vertices` connected by `Nets`, while strictly separating model and view layer. The Antares application uses `Graph` and `GraphView` to implement circuits.

[antares](module-antares.md)
: `antares` is an application module that uses all the Jabbah framework's modules to implement digital (and analog) circuit editing and simulation. Most of the required functionality is already provided by the `graph` module. `antares` extends the set of generic core classes to adapt them to the specific world of digital signals.

## Cross-cutting aspects

### Module system

TODO

### Test utility modules

TODO
