# Module `animation`

## Overview

A simple animation framework that uses `AnimationTask` to animate a `Sequence` of values over time and to let an `AnimationTaskConsumer` consume the current value.

For example, `SynchronizedGlowAnimation` can produce a glow effect on multiple colored objects by playing an `Oscillation` (a special type of `Sequence`) over the alpha channel of an RGBA color and setting the current color on all registered colored objects.

There are a couple of predefined `Sequences` like `DoubleRange`, `PointRange` or `Repetition` that you can use to produce your desired animation effect, or you can implement your own.

`AnimationTask` defines the duration (in milliseconds) of the animation and a "size" (depending on the `Sequence`), which results in the speed the animation is played by the `Animator` instance. If desired, this speed can be influenced by the user if he changes the current `SystemSpeed`.

## Example

An interesting example worth studying is `ZoomedPointVoyage` in the `draw` module. `ZoomedPointVoyage` is a `Sequence` over `ZoomedPointTranslation` values that change a `View`'s zoom factor and pan location in order to produce the effect of a camera moving from one location to another, while zooming in or out.