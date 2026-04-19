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
: TODO

[jabbah.app](module-app.md)
: The "app" module implements a simple application framework. Its central `AbstractDesktopApplication` class on the JVM platform can be started with command line options, loads and stores user preferences, and defines an `ApplicationData` class and various `Actions` like `OpenFileAction` or `SaveFileAction` to make it persistent.

[jabbah.graph](module-graph.md)
: TODO

[antares](module-antares.md)
: TODO

## Cross-cutting aspects

### Module system

TODO

### Test utility modules

TODO