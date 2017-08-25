# Draw

## Introduction

TODO

## Module structure
TODO

![Package Overview](../../model-img/svg/jabbah__draw__Draw Package Overview_1.svg)

* [**graphics**](graphics.md): Contains low-level graphical objects such as `Color`, `Font` or `Stroke`, and an
abstract interface of drawing methods that are implemented in the supported platforms.
* [**draw**](draw-interfaces.md): Contains interfaces used by all modules of `draw`.
* [**style**](style.md): Contains concepts that allow an application to establish coherent visual styles for its
graphical objects, and to enable the user to switch between predefined themes.
* [**drawable**](drawable.md): Contains abstract `Drawable` implementations as well as some reusable, common-purpose
`Drawables` such as `IconButton` or `RectangularDrawble`.
* [**container**](container.md): Contains `DrawableContainer` implementations.
* [**view**](view.md): Contains `View` implementations and classes for zooming and navigation within a `View`.
* [**polyline**](polyline.md): Contains interfaces and classes for polyline `Drawable`s.

## Core Abstractions

This sections gives an overview of the core abstractions of the `draw` module and how they collaborate to support
building and displaying 2D graphics. Check out the various sub-module descriptions for detailed information.

![Core Abstractions](../../model-img/svg/jabbah__draw__Draw Core Abstractions_2.svg)

[**Drawable**](drawable.md): `Drawables` represent the very core of a 2D drawing application built with `draw`.
A `Drawable` can draw itself using the drawing methods provided by a `DrawContext` and its `Graphics2D` drawing interface.
`Drawables` are also an important source of repainting activity; when a `Drawable` has changed, it declares itself
as "invalid", which eventually leads to repainting activities in the `Views` that contain this `Drawable`.

[**DrawableListener**](drawable.md): Clients of `Drawables` can register `DrawableListeners` that get called
whenever the `Drawable` needs repainting, or when its geometry has changed. `Views` use `DrawableListeners` for
their repainting processes.

[**DrawableContainer**](container.md): `DrawableContainers` are composites of `Drawable`s and maintain a
configurable stacking order of the `Drawables` they contain. A `DrawableContainer` is involded in repainting
of its `Drawables` as well as in dispatching input events to them.

[**DrawableContainerListener**](container.md): `DrawableContainerListeners` are used to get involved whenever
`Drawables` are added to or removed from a `DrawableContainer`.

[**InputEventHandler**](input-event-handling.md): An `InputEventHandler` consumes input events on behalf of its `Drawable`.

[**View**](view.md): A `View` is the gateway between `Drawables` that need to be displayed to the user, and
the `Canvas` that implements platform-specific displaying features, such as a HTML canvas in a browser or a
`JComponent` on the JVM platform. `Views`implement zooming and panning, and they are involved in input event
dispatching, incorporating view-to-model coordinate transformations.

[**Canvas**](view.md): A `Canvas` is the interface to a platform-specific rendering device. Separating `View`
from `Canvas` allows to provide complex `View` implementations (including coordinate transformations and in-`View`
navigation) in a platform-independant way.

## Cross-cutting Concerns

* [**Composition**](composition.md): TODO
* [**Repainting**](repainting.md): TODO
* [**Input Event Handling**](input-event-handling.md): Input event handling is the process of capturing input events
in a `Canvas` and dispatching them to the `InputEventHandler` of the `Drawable` (or to other objects, such as tool of
an editor) the user aims to.

