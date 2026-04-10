# Module "edit"

## Overview

The `edit` module extends the `draw` module to allow a user to edit a `Drawing`, including `Component` selection,
support for undo/redo using the `Command` pattern, property editing, and a set of standard 2D `Components` like `PolylineComponent` or `TextComponent`.

## Component and Drawing

A `Component` is an editable, `Drawable` and the central element of the `edit` module. It implements `Storable` to be written to and read from external storage, and has an `id` to support references. It is by default `Movable` and `Rotatable`, and consumes its visual properties as `Styleable` from its `Style`.

`Components` are added to a `Drawing`, which is an extension of `DrawableContainer` from the `draw` module, and implements a stacking order amongst its `Components`.

## DrawingView

A `DrawingView` extends `draw.View` by not only displaying the current `Drawing`, but also additional "layers" needed to implement the visual part of a 2D graphics editor. These layers or `Containers` include (from top-most to bottom-most):

- a "Ghost" container used for temporarily drawing `Unzoomable` objects like `SelectionModels`
- an "Animation" container for rendering zoomed animations like signals flowing along a wire in Antares
- 3 containers for displaying `SelectionModels`, used depending on whether a `SelectionModels` is to be draws above, instead, or below the selected `Components`
- the displayed `Drawing`
- a "Highlight" container used for highlighting (not selecting!) `Components`, as e.g. used in Antares' "Scenarios" feature
- a "Background" container used for drawing things below everything else, such as the page format preview in Antares' "Poster" feature

## Editor

`Editor` implements support for editing a `Drawing` within a `DrawingView`. It provides `Tools` like the `SelectionTool` the user can use to select `Components`. It maintains a `CommandManager` that helps making changes in the `Drawing` undoable and redoable. Its `SnapManager` and `DragManager` assist the user when moving `Components` within the `Drawing`.   

## Selection handling

The Jabbah framework contains very versatile structures for making `Components` selectable. A `SelectionModel` not only draws a `Component` in "selected" state, it can also offer graphical hints like handles that the user can interact with to change the shape of a `Component`. The framework supports 3 different `SelectionDrawingStrategies` "Above", "Replace" and "Below", which indicate where a `SelectionModel` is drawn relative to its `Component`.

The `Editor` delegates input events to its `SelectionTool`, whose standard implementation provides a variety of ways how `Components` can be selected, including single selection by clicking, selection expansion by SHIFT-clicking, or using `RubberBand` to define a rectangular selection area.

The `SelectionTool` also delegates `MouseEvent` to the `DragManager` if a mouse click on a selected `Component` is followed by dragging the mouse.

## Undo/redo

Undo/redo of data changes are implemented according to the "Command pattern" using `Command` classes, e.g. `MoveCommand` for moving a set of `Components`. At the end of a user action, the corresponding `Command` is created (typically by an `Action`, an `InputEventHandler`, or by a service class that implements more complex updates) and handed over to the `CommandManager`, which keeps the `Commands` in an undo/redo `Stack`.

All `Commands` have a `execute()` that performs the user's change to a `Drawing`, so it can be executed again when the user wants to redo an undone change. Some simpler `Commands` also implement `Undoable`.

For some more complex `Commands`, to implement `Undoable.undo()` is difficult or even impossible. This is why the default `SourcingCommandManager` uses a "snapshot" approach for implementing "undo". The `CommandManager` creates a snapshot of the current state of the `Drawing` e.g. every 20 `Commands`. If the `Command` to be undone implements `Undoable`, its `undo()` method is called. Otherwise, the last snapshot is re-established, and all but the last `Command` are re-executed ("replay from snapshot").

The user interacts with the undo/redo system using `UndoAction` and `RedoAction` (or their corresponding UI elements like "Edit" menu items).

## Tools

`Tools` are used by the user to perform certain actions, e.g. adding a graphical `Component` like a `RectangleComponent` to a `Drawing`. `Tools` are added to an `Editor`'s `ToolBar` and equipped with a radio-button behavior, so that only one `Tool` can be active at a time. The `Editor` listens for `Tool` activation and forwards input event to the "current" `Tool`.

In addition to the all important `SelectionTool`, another example is `RectangleTool`, which uses mouse events to let the user click-and-drag the shape of the `RectangleComponent` to be created.

## Property editing

The `edit.properties` package in the JVM source set used the l2fprod library that provides a `ProperySheetPanel` which allows to edit properties based on Java Beans specification. All `Components` whose properties should be editable by the user must provide a `BeanInfo` class that lists all editable properties.

In order to make this work, `PropertySheetPanel` must know what renderers and editor to use for a particular property type class. Build-in types like `Int` or `String` are preconfigured with standard implementations. Custom types like `LightColor` in the Antares module must be registered with a `PropertyRendererRegistry` and `PropertyEditorRegistry`. This is typically done in a `Module`'s configuration part, e.g. `EditModuleJvm.configurePropertyRenderer()`.

For example, see `LEDViewBeanInfo` and how it defined an editable property for an `LEDView`'s `LightColor` color property, whose property editor is registered in `AntaresModuleJvm` as `LightColorEditor`, which displays a combobox with all predefined `LightColors`.

## Standard graphical components

The `draw.model` package contains many ready-to-use graphical components like `CubicCurveComponent`, `QuadCurveComponent`, `ImageComponent`, `PolylineComponent`, `RectangleComponent`, `EllipseComponent` and `TextComponent`.

## Snapping

Snapping is the process of forcing mouse coordinates to certain restricted locaction, either defined by a `Grid`, or by already existing `Components` that implement the `Snappable` interface. A `SnapManager` held by the `Editor` coordinates coordinate restrictions requested by possibly many `Snappers`, like when both a `Grid` and `ComponentSnapper` is active.

All `Snappers` are optional and can be disabled by the user in the UI.

## Highlighting

Highlighting is a concept similar to "Selection" in that it graphically emphasizes a `Component`, but it doesn't select it in the `Editor`. Highlighting is used by Antares' "Scenarios" feature, which highlights a `Component` by drawing a yellow background beneath the `Component` that is slightly larger than the highlighted `Component`.

However, highlights are implemented using `SelectionModels` of `SelectionDrawingStrategy.BELOW`. Highlighting is activated by using a `DrawingView`'s `Highligher` object which offers a `highlight(Component)` method.