# Module `draw`

## Overview

Provides everything necessary for displaying 2D graphics, including zooming & panning. It contains a Kotlin-common `Graphics2D` interface with drawing operations that are implemented on the JVM and JS platform for the respective `Canvas` objects, as well as interfaces for commonly used graphics primitive classes like `Color`, `Stroke`, `Font` or `Image`.

## Rendering using `Graphics2D`

The `Graphics2D` interface represents a Kotlin-common abstraction of the drawing interfaces known from Java or JavaScript programming. It provides drawing methods like `drawLine` or `fillRect` and transformation operations like `translate` or `rotate`. The goal of the `graphics` package was to enable Kotlin-common code to implement 2D drawing code that can be rendered both on the JVM and the JS platform. To do that, it also has to provide interfaces for graphics primitive classes `Color`, `Stroke`, `Font` or `Image`.

## `Drawable`

Classes that can draw themselves typically implement the `Drawable` interface. They are added to a `View`, which calls the `Drawable`'s `draw()` method whenever its region has become invalid and the `View` needs repainting. The provided `DrawContext` contains a reference to the `Graphics2D` objects used to call drawing operations.

The `DrawableContainer` abstraction uses the composite pattern to build containment hierarchies of `Drawable`s. Drawings are typically implemented as a `DrawableContainer`.  The module provides some basic `Drawable` types like `AbstractDrawable` or `AbstractRectangle` that can be used to implement more concrete `Drawable`s.

Repainting in the `draw` module is initiated indirectly. Rather that directly requesting a `Graphics2D` and start drawing when a `Drawable`'s state has changed, the `Drawable` declares itself as "invalid". A `View` that displays the `Drawable` detects this and initiates repainting of the invalid region in its `Canvas`.

## `View`

A `View` displays a `Drawable` (typically a `DrawableContainer`) by rendering it in a `Canvas`, which is provided by the concrete platform (JVM, JavaScript/Browser). Its `ViewNavigator` allows the user to chanage the displayed region by zooming and panning, including predefined actions like "Zoom to fit".

The `View` is also responsible for mediating user events like mouse and keyboard actions between the `Canvas`, where the events originate, and the displayed `Drawable`, where the events are consumed.

A `View` can delegate repainting behavior to a separate `ViewPainter` object. Its standard implementation `InvalidatableViewPainter` uses a timer to throttle repainting actions in order to balance between responsive FPS rates and acceptable CPU load.

## Coordinate spaces

The `draw` modules distinguished three different coordinate spaces.

Absolute view model coordinate space
: `Drawables` like `RectangularDrawable` are used to implement view model objects. They express their location in the absolute view model coordinate space using a `Point2D` object. When using `Graphic2D`'s drawing methods in their `draw()` method, they use coordinates expressed in this coordinate space. 

View coordinate space
: Implementations of `View` like `ViewImpl` transform view model coordinates to view coordinates by applying the necessary `AffineTransformation` to support zooming and panning, and they apply the reverse transformation when forwarding input events to the `Drawables`.

Relative view model coordinate space
: This coordinate space is relative to a `Drawable`'s (in fact, to the `Locatable`'s) location, which allows a `Drawable`'s `draw()`implementation to express its geometry in local coordinates. However, this is only used in higher-level modules like `edit`, where `AbstractComponent` provides a basic `draw` implementation that performs translation and rotation before the `draw()` logic gets called.

## Bounding box

Every `Drawable` must implement `boundingBox` and return a `RectangularShape` that defines its bound in absolute view model coordinate space. This is used by the `ViewPainter` to keep track of invalid regions and avoid unnecessary calls of `Drawable.draw()` when revalidation a `View`. As revalidation is done very often, dynamic `Drawables` should cache their bounding box instance and update it accordingly before calling `invalidate()`.

## Styles and themes

A `StyleType` defines a name like "Figure" or "Annotation" for a set of graphical properties like `Color`, `Stroke` or `Font` .A `Style` has concrete values for these properties. The `StyleRepository` allows to register `Styles` for the predefined `StyleTypes`. 
`Themes` are collections of `Styles` and allow the user e.g. to switch between "Dark mode" and "Light mode".

`Drawables` can benefit from the style system by implementing the `Stylable` interface (and its basic implementation `AbstractStyledDrawable`) to delegate requests e.g. for the foreground color to the value in the currently used `Style.

## Input event handling

`Drawables` return an `InputEventHandler` that defines how the `Drawable` reacts to user inputs like `MouseEvent` or `KeyEvent`. `InputEventHandlers` are called by the `View` to forward events originating in the platform-specific `Canvas` to the `Drawables` (after applying view-to-model coordinate transformation). 

## Unzoomable

`Unzoomables` can be implemented by those `Drawables` that are added as view slides to an `View`, but don't want to be drawn to a `Graphics2D` that already contains a scaling transformation. Instead, classes that implement `Unzoomable` will perform the necessary scaling of their geometry by themselves, thus avoiding strokes to be zoomed as well.

This concept is for example used by `ConnectionPointHighlight`, the green box displayed when hovering with the mouse over Antares pins to invite the user to start a new wire. For usability reason, this green box should always have the same size, no matter what the `View`'s current zoom factor is. Another example is `Grid` in the `edit` module: Implementing `Unzoomable` avoids that grid points (or lines) become too big (or thick) when zooming in.

## Rich text

`RichTextDrawable` is capable of rendering a `RichText` from the `base` module using `Graphics2D` drawing operations, including nested underline, overline, subscripts and superscripts.