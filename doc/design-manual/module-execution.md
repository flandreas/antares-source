# Module `execution`

## Overview

The `execution` module supports time-based execution of arbitrary objects called `Actors`. While this module is used by the Antares application for driving the simulation of digital circuits, the module is not called "simulation", as it is not only supposed to be used for simulating physical system, but also for controlling execution of arbitrary logic in a time-based way.

The basic usage pattern is to implement `Actor` objects having an `act()` method, and register them to be executed at a particular time by calling `Scheduler.requestActingAfter(delay: Long)`. The `Scheduler` implementation puts the `Actor` into a time-sorted queue, keeps track of the passing execution time, and calls the `Actor` for acting when its time has come.

Besides that, `Scheduler` implementations provide sophisticated support for slowing down execution speed, temporarily pausing execution, and various techniques for allowing `Actors` to play animations while they are executed, which Antares uses for animating signals flowing through a circuit during simulation.

## Scheduler interfaces and implementations

The `Scheduler` concept is actually split into two abstractions:

- **Scheduler**: This interface provides methods for controlling execution at runtime, such as starting, stopping or enforcing breakpoints. It is typically used by the application UI to allow the user to interact with the execution.
- **SignalHandler**: This interface is used by `Actors` (or `Actor`-related classes) during execution to request being asked to act at a later point in time.

## Actor structure

`ActorImpl` as primary implementation of `Actor` handles the `Actor`'s execution state. It delegates supporting the various execution phases to its companion `ActorSupport`, which allows to register `ActorListeners` (typically object representing the execution context, such as an Antares circuit view) that want to be informed on `Actor` execution phase changes.

Depending on the application, `Actor` implementations will delegate implementation of the central `Actor.act()` method to a base class or a common delegate if they are all structured the same way. For example, the `graph` module in the Jabbah framework has a `CalculatingVertice` base class that uses a subclass-provided `VerticeCalculator` class with a `calculate(CalculatingVertice)` method. `CalculatingVertice.act()` calls `VerticeCalculator.calculate()` and then makes sure that newly produced signals are sent out at the `Vertices`' `OutputPorts` to the connected `Nets`. The only thing e.g. an Antares NOT gate then has to bring to be executed is a `VerticeCalculator` implementation that reads the signal at the `InputPort` and sets the inverted signal at its `Output` port.

## Execution timing

`SchedulerImpl` is currently the only class implementing the `Scheduler` interface. It delegates handling the passing of "execution time" to `SchedulerTask`, whose `TimedSchedulerTask` implementation uses a `Timer` to drive execution and to slow down execution in reaction to changes of `CurrentSystemSpeedCategory`.

## Execution phases

Execution start
: The user starts the execution using the "Run" toggle button in the UI's toolbar. All `Actors` in the current context (e.g. the current circuit in Antares) are called with `executionInitialize()` followed by `executionStart()`. `Actors` can implement them e.g. to setup their initial simulation state (e.g. an LED is initially off) or already request to be acted upon after a certain setup time. All `Actors` enter state `idle`.

Actor requests acting
: The user e.g. toggles a `Switch` in an Antares circuit. The `Switch`, which a given a reference to the `SignalHandler` when handling the `MouseEvent` from the user, calls `SignalHandler.requestActingAfter()` (providing its own propagation delay like 10 ms as "delay" parameter) to ask the `Scheduler` to call it back after the specified delay. The `Scheduler` stores this request in its internal, time-sorted queue. The `Actor` goes into state "waiting". All registered `ActorListeners` are notified with `actingRequested`, giving implementations a chance to react to this state. For example, an Antares circuit view - if animations are enabled and the simulations runs in single-step mode - lets waiting `Actor` glow (or blink) while they are waiting. For Antares `Nets`, which are also `Actors` with propagation delay 0, a signal flow animation might be started.

Waiting phase
: The request waits in the `Scheduler`'s queue. The UI might keep on playing animations showing that an `Actor` is waiting to act.

Acting phase
: When execution time has proceeded so far that the `Switch`'s time to act has come, the `Scheduler` calls the `Switch`'s (i.g. the `Actor`'s ) `act()` method. The `Switch` updates its state to "acting" and delegates to its `VerticeCalculator`, which produces a new signal at the `Switch`'s output pin. Then it calls `acted()` on all registered `ActorListeners`. Implementations that are displaying "wait" visualizations (like an Antares circuit playing signal flow animations) can now remove these visualizations and then call `Actor.actingVisualized()`.

Completion phase
: The `Actor`'s `ActorSupport` waits until all its `ActorListeners` have called `actingVisualized()`, after which `ActorSupport` calls `SignalHandler.actingDone()`. The `Scheduler` removes the corresponding request from its queue, and finally calls `Actor.actingDone()`, thereby initiating the "Continuation phase".

Continuation phase
: An `Actor` implementation implements `actingDone()` to update another `Actor` that depends on the first `Actor`, thereby starting another execution cycle. For example, an Antares `Switch` will flush its `OutputPort`, which forwards the outgoing signal to a connected `Net`, which starts the same execution cycle on the `Net` as an `Actor`. This behavior is implemented generally in `GraphElements` in the `graph` module.


