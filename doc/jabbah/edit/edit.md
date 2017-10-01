# Edit

## Introduction

TODO

## Module Overview
![Module Overview](../../model-img/svg/jabbah__edit__Edit Package Overview_5.svg)

* **edit:** Contains all important interfaces, which allows to reduce module dependencies.

* [**command:**](edit-command.md) Provides mechanisms for implementing undo/redo functionality.

* [**snap:**](edit-snap.md) Provides mechanisms for aligning and snapping the locations of `Components` to a `Grid`,
to guidelines, or to each other.

* [**style:**](edit-style.md) Extends the `Style` and `Theme` classes of the `draw` module for the `edit` module.

* [**tool:**](edit-tool.md) Provides mechanisms for developing `Tools` to be used for editing `Drawings`.

* [**highlight:**](edit-highlight.md) Support for highlighting `Components`, which can be useful when explaining `Drawings`
to the user. In contrast to the concept of "selection", which is directly controlled by the user, highlighting is
controlled by mechanisms beyond the control of the user. 

* [**view:**](edit-view.md) Extends the `View` of the `draw` module with `Drawing`- and editing-related concepts.

* [**select:**](edit-select.md) Support for selecting `Components` while editing them. Provides various types of
`SelectionDrawingStrategies` and `SelectionModels`, for example those that use `Handles` that allow the user to
shape a `Component`.

* [**property:**](edit-property.md) Support for interactive editing of a `Component`'s properties using property sheets
and specialized property editors.

* [**editor:**](edit-editor.md) Provides the basic support for editing `Drawings`.

* [**model:**](edit-model.md) Contains various mulit-purpose `Component` implementations, such as `PolylineComponent`,
`RectangularComponent` or `TextComponent`. 


## Core Abstractions
![Core Abstractions](../../model-img/svg/jabbah__edit__Edit Core Abstractions_4.svg)

TODO Explain