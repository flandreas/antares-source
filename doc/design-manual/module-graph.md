# Module "graph"

## Overview

The `graph` module contains a framework for drawing, editing and executing arbitrary `Graphs` consisting of `Vertices` connected by `Nets`, while strictly separating model and view layer. The Antares application uses `Graph` and `GraphView` to implement circuits.

## Vertice and Port

A `Vertice` is a `GraphElement` that can have an arbitrary set of `Ports` that allow a `Vertice` to connect to a `Net`, thus producing a connection to many other `Vertices`. A `Port` can be an `InputPort` (receives signals from a `Net`), an `OutputPort` (applies signals to a `Net`), or a `BidirectionalPort` (receives signals from and applies signals to a `Net`). `Ports` have a generic type parameter for the value they can pass along.

On the view layer, the corresponding interfaces are `GraphElementView` (which are `Components` from the `edit` module), `VerticeView` and `PortView` (which are `Drawables` from the `draw` module).   

## Net

A `Net` is a `GraphElement` to which `Ports` of `Vertices` can connect in order to send signals to other `Vertices` connected to the same `Net`. 

On the view layer, `Nets` are visualized by `NetViewElements`, which are either `EdgeViews` (between `PortViews` and/or `NodeViews`) or `NodeViews`, which are `EdgeView` junctions. `NetViewElements` feature a `NetViewStyling` which can either be "Line" (the default) or "Block" (used to designate buses in Antares microcomputer designs). `NetViewElements` are combined to `NetViews` that make sure that properties like `PredefinedColor` and `NetViewStyle` are applied to all its `NetViewElements`.

## Graph

A `Graph` is a collection of connected `GraphElements` which are either `Vertices` or `Nets`. It can contain special `Vertices` called `GraphPorts` that allow to pass signals into the `Graph` (`GraphInput`), to pass signals outside the `Graph` (`GraphOutput`), or both (`BidirectionalGraphPort`). `GraphPorts` have a generic type parameter for the value they can pass along. Every `Graph` has a `UUID` that defines a unique reference to it, as well as a translatable name and description.

On the view layer, the corresponding interfaces are `GraphView` and `GraphPortView`. `GraphView`also holds `Usecases` (executable scripts that interact with a `GraphView` during execution just like a user would) and `Scenarios` (display additional explanatory information during execution).

## Subgraph

The `graph` module allows hierarchical nesting of `Graphs` using the `SubGraphVertice`, a special type of `Vertice` that has a `UUID` to reference its inner `Graph`.

## ContainerDrawing

A `ContainerDrawing` is a purely graphical `Drawing` that contains the graphical representation of a `SubGraphVertice`, also known as the "Symbol" of a `GraphView`. It can display purely graphical objects like labels or rectangles, but also `PortViewComponents` that display a "pin" which internally connects to a `PortView` of the inner `GraphView`.

## MetaGraph

A `MetaGraph` is a `Storable` combination of a `GraphView` and its `Graph` model (combined as `GraphStorable`) and the symbol `ContainerDrawing`. Later versions also added a `Documentation` object that can contain Markdown (.md) documentation of the `MetaGraph`.

A `MetaGraphRepository` is a repository of reusable `MetaGraphs`. A `MetaGraph` containing `SubGraphVertices` will use a `MetaGraphRepository` to get access to the `MetaGraphs` it references.

In the Antares application, every circuit you build is stored as a `MetaGraph` in a myCircuit.cir file.

## Library and Project

A `Library` is a named, hierarchically structured `MetaGraphRepository` containing a cohesive set of `MetaGraph` to be used by other `Libraries` or `Projects`. A `Library` can have multiple imported `Libraries`.

A `Project` is simply an extension of `Library` that can't be imported by other `Projects` or `Libraries`. The distinction is mainly on the UI; technically `Libraries` and `Projects` are very much the same.

## Tools and services

There are many tools, actions and services necessary to allow the user to draw a `GraphView` and connect its `VerticeViews` by drawing `EdgeViews` between its `PortViews`. One example is `OutputToInputOrEdgeConnector`, and interactive tool that processes mouse events using a state machine design. At the end of the user's action, `OutputToInputOrEdgeConnector` (or rather its state machine) creates e.g. an undoable `ConnectOriginCommand` that uses methods from `GraphViewConnectService` for doing all the necessary connection bookkeeping.

## UI structure

The `graph` module contains a sophisticated UI for creating and editing `MetaGraphs`, and for diving into `SubGraphVerticeViews`.

At the heart of the UI lies `GraphNavigationView`, which displays an editable `DrawingView` for editing a `GraphView`, and combines it with a `NavigationStackView`, a breadcrumb-like UI element that displays how deep the user dove into a `GraphView`'s hierarchical structure, including the possibility to resurface to the main `GraphView`.

`GraphNavigationView` is part of a `GraphEditView`, which adds a `ScenarioView` (for working with `Scenarios`) and a `UsecaseView` (for working with `Usecases`).

`GraphEditView` is part of a `GraphPanel`, which adds a `LibraryPanel` (used for dragging `ContainerLibraryElements` into the `GraphView`), a `PropertiesPanel` (used for editing properties of the selected `GraphElementView`), a `GraphDesktopView` that supports multiple open views, and a `IssuesView` and a `LogView` for displaying additional information in expandable sidebars.

`GraphPanel` is part of a `GraphFrame`, which adds `DocumentationPanel`(used for editing a `MetaGraph`'s `Documentation`) and a `ContainerPanel` (used for editing the `MetaGraph`'s `ContainerDrawing` or "symbol"). It also displays the main menu bar and a `ToolBar`.
