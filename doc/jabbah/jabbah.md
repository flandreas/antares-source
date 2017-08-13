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

## The name "Jabbah"

All our software projects and packages are named after a star in the constellation "Scorpion" (or "Scorpius", as the
corresponding constellation of the zodiac is properly called in english). Jabbah is a medium-sized star in the
constellation "Scorpius" and is 437 light years away from earth.

The digital circuit editor module `antares` is called after another star in the same constellation. Antares is the
brightest star in the constellation. Due to its red color and its brightness similar to mars, it is often
confused with mars, and hence the name "antares" or "anti-ares", according to the roman god "Ares"
who was associated with mars.

Note that the module `antares` should not be part of the `jabbah` framework, but should rather be a separate
project that uses `jabbah`. Because `jabbah` and `antares` are currently developed at the same time
(and the requirements for features in `jabbah` are mainly driven by `antares`), they are part of the same
project for ease of development and integration. Once both modules are enough stable, `antares` will
be removed from the `jabbah` project and put into its own project.

Candidates star names for additional, future software packages in the `ch.scorpion` domain:

* Shaula
* Sargas
* Dschubba
* Akrab
* Alniyat

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

* [**edit:**](edit/edit.md) Provides functionality for interactive editing of 2D drawings.

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
the circuits. `antares` isn't contained in the above diagram by purpose, as it will be removed from the `jabbah` project.

## Module structure

Most of `Jabbah` code is implemented in Kotlin and is intended to run on the JVM (by compiling it to Java byte code)
as well as in the browser (by compiling/transpiling it to Java Script). The majority of the code is designed such
that it can be compiled to run unchanged on both platforms. This code is contained in the main package `shared`
of every module. A `shared` package gets compiled both to Java byte code and to Java Script.

Code that is only used to be compiled for the JVM is contained in the main package `jvm` of a module. These packages
contain UI code that uses Java Swing, or implementations of core interfaces that implement a particular concept for
the JVM platform, such as `Graphics2DJvm`. Code in a `jvm` package is only compiled to Java byte code.

If a module requires that some Kotlin code must only be compiled for the Java Script platform, it is located
in an additional module whose name is derived from the original module name by expanding it with `-web`.
For example, the module `draw-web` contains all of the logical `draw` module that needs to be compiled to Java Script.

Some modules contain special code for examples or demos of features of that module. Depending on the type of
example or demo, this code can be distributed among the `shared` and `jvm` package and the `-web` module, where
the `shared` package might contain common example logic, while the other packages might contain platform-specific
UI implementations.

As an example, the structure of the `draw` module is as follows:
```
/draw
   /jvm
      /src
         /demo
            ch.scorpion.jabbah.draw
         /main
            ch.scorpion.jabbah.draw
   /shared
      /src
         /demo
            ch.scorpion.draw.hellopgraphics
         /main
            ch.scorpion.jabbah.draw
         /test
            ch.scorpion.jabbah.draw
/draw-web
   /demo
      /hellographics
         /kotlin
            ch.scorpion.jabbah.draw.hellographics
   /main
      /kotlin
         ch.scorpion.jabbah.draw
```
All other modules follow the same structure.

