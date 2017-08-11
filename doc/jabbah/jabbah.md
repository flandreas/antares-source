# Jabbah

## Introduction

`Jabbah` is a framework for building applications with interactive 2D graphics. It contains modules for
displaying, editing and storing simple 2D graphics, as well as modules that allow to handle complex, hierarchical
graphs of graphical objects.

`Jabbah` code is designed to run both on a Java virtual machine (Java 8) as well as in the browser.
To achieve this goal, most of the code of `Jabbah` is written in Kotlin, which allows to compile to
Java byte code as well as to Java Script.

The lower level modules of `Jabbah` can be used to implement simple graphical applications, such as
static diagrams or interactive games. The higher level modules allow to develop more complex graphical applications,
such as the digital circuit simulator contained in the `antares` package.

## Module Overview

The module structure of the `Jabbah` framework puts an emphasis on reducing dependencies between the modules.
Each module is represented by a separate `gradle` module and can be built individually

![Package Overview](../model-img/svg/jabbah__Package Overview_0.svg)

* [**base:**](base/base.md) Some base concepts and classes that don't depend on other `Jabbah` packages.
Some of them provide abstractions that are implemented differently on the platforms supported by `Jabbah`, such as
`Point2D` or `MouseEvent`. Others are basic classes that are missing in the Kotlin runtime library used for
both the JVM and the JavaScript platform, such as `Stack`. The later might be replaced once they are available in
the Kotlin runtime library. The `base` packages also contains code for concepts that cannot be straight-forward
implemented on all supported platforms, such as the `module` sub-package which is a replacement for dependency
injection.

* [**io:**](io/io.md) Allows storing and loading of objects and object graphs to and from persistent storage,
 such as flat files.

* [**animation:**](animation/animation.md) A simple framework that supports repeated, fluent changing of graphical
and non-graphical values, which can be used to implement graphical animations.

* [**draw:**](draw/draw.md) Provides functionality for creating and displaying 2D drawings.

* [**edit:**](/edit/edit.md) Provides functionality for interactive editing of 2D drawings.

* [**execution:**](execution/execution.md) Provides an actor representation and functionality for asynchronous
scheduling of signals flowing between objects. This can be used for implementing simulation and animation of discrete
dynamic systems.

* [**app:**](app/app.md) A simple application framework that features an `Application` base class and support for
loading and storing application state, and for building a UI containing actions, menus and tool bars. 

* [**graph:**](graph/graph.md) A sophisticated framework for building graphs of vertices that are connected with edges.
It distinguishes properly between model and view classes, provides support for hierarchical graphs, and contains UI
classes for working with libraries of graph components.

* [**antares:**](antares/antares.md) An application for building, simulating and animating digital circuits.
It uses the `graph` package for modelling the circuit structures and the `execution` package for simulating
the circuits.