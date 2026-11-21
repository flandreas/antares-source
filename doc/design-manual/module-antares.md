# Module "antares"

## Overview

`antares` is an application module that uses all the Jabbah framework's modules to implement digital (and analog) circuit editing and simulation. Most of the required functionality is already provided by the `graph` module. `antares` extends the set of generic core classes to adapt them to the specific world of digital (and analog) signals.

## Circuit components

The `antares` module's circuit components are built on the `graph`'s module `Vertice` (model layer) and `VerticeView` (view layer). For example, the LED component is implemented by `LED` and `LEDView`.

## Circuit

Circuits are implemented on the model layer by `DigitalGraph`. It extends `GraphImpl` from the `graph` module mainly in order to create temporary `Nets` used for `Tunnels`, but also to introduce the `NetSignalApplierStrategy` with options "Conflict" or "WiredOR".

On the view layer, `DigitalGraphView` extends `GraphViewImpl` in order to make certain system properties like default `LightColor` or `DigitalSignalRepresentation` changeable per circuit.

## DigitalSignal

The interface `DigitalSignal` represents the object produced by digital components and then flowing across `DigitalNets` to other digital components. It consists of individual `Bits` with four possible states "Undefined", "Error", "False" or "True", and it offers typical bit-wise operations like `and()`, `flip()` or `subword`. A `DigitalSignal` has a `BitWidth` property indicating the number of `Bits` it consist of.

There are currently two implementations of `DigitalSignal`. The first one is `Word`, which is literally a list of individual `Bits`, which makes it less efficient in terms of heap memory consumption and calculation speed. The second one is `DefinedWord`, which uses a `ULong` as data property, and which is therefore more efficent, but at the cost of not supporting individual "Undefined" and "Error" bits. It is automatically used in situations where all bits are definitively defined.

## Analog circuits

One of the most recent extension of Antares, which was originally meant to be a digital circuit system only, is analog circuits, thus introducing a new value for `GraphType`, implemented by the enumeration `AntaresGraphTypes`.

This made it necessary to implement a variety of specialized implementations like `AnalogGraph`, `AnalogGraphView` or `AnalogPort`, all being instantiated by an implementation of particular factories like `GraphFactory`, which are initialized by `AntaresModelModule` and determine the proper type based on the current `GraphType`.

In addition to these new types, the simulation of analog circuits is obviously very different from that of digital circuits. Digital circuits are simulated using the `execution` module, where `antares` `Vertices` produce new output `DigitalSignals` after new `DigitalSignals` have arrived at their inputs. In contrast, analog circuits are simulated by composing a linear equation system based e.g. on Kirchhoff's laws, and solving them for electrical voltage and current, both represented as `AnalogSignals`. The module also support approximation techniques to support non-linear components like `Capacitor` or `Inductor`.

## Web editor

`antares/js/.../antares/web` hosts the browser version of the editor. `AntaresWebEditor` instantiates the same shared controllers as the desktop application (`GraphDataViewController`, `GraphFrameController` and their children) with lightweight browser views, so editing, wiring, undo/redo, sub-circuit navigation and simulation behave exactly like on the desktop. Because the desktop's property sheets are JVM-only `BeanInfo` classes based on reflection, `WebPropertyRegistry` describes the editable properties of all components with portable getters and setters. The UI itself is a TypeScript/React application in the top-level `antares-web` directory; see its README.
