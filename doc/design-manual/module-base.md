# Module "base"

Besides general purpose functionality like the 'collection' package, the main purpose is to provide abstractions that have to be implemented differently on the JVM and JS platform, like the `InputEvent`class in the `event` package.

## Packages

collection
: Contains classes currently not provided by the Kotlin library, such as `DirectedGraph`, `Stack` or `PriorityQueue` used in the simulation engine.

dsl
: Uses the `parser` package to provide basic structures for building domain-specific languages, such as `Interpreter`, `ActivationRecord` and `SymbolTable`. Used in higher-level layers for building scripting-based features.

event
: Provides Kotlin-common abstractions for various event classes such as `ActionEvent`, `InputEvent` or `MouseEvent` including binding implementations for the JVM and JS platform. Also provides an `EventBus` (whiteboard design pattern) implementation that is heavily used across the system.

geom
: Contains many geometrical abstractions like `Point2D`, `Dimension2D`, or `Rectangle2D` used for building the graphical representation of core business objects. Also contains an `AffineTransform` abstraction and a Kotlin-common implementation to be used on the JS platform.

help
: Implements a simple "Help" system you can use to define `HelpI`s in a application and link them to external documentation, such as URLs of web pages. On the JVM platform, a `HelpProvider` implementation uses the `java.awt.Desktop` class to kick off the system's web browser to browse the external help page, which is in case of the Antares manual its online user manual.

invocation
: Defines abstractions for invocating long-lasting actions by the user, which automatically disable the UI and display an hourglass pointer on the JVM platform.

math
: Extends the built-in Kotlin math library by certain mathematical functions. 

parser
: Basic classes for building a lexer that produces token consumed by a parser.

richtext
: Defines a small DSL to define text artifacts commonly used in circuit schemas, such as overline, subscript, superscript or italic. The higher-level module `draw` contains classes capable of rendering such "rich text" on the JVM and the JS platform, mainly by using the Kotlin-common `Graphics2D` abstraction.

sound
: Defines a multi-platform abstraction for playing sound.

state
: Provides base abstractions and implementations to implement `StateMachine`s, mainly used in higher-level modules to implement user interaction features like context- and state-sensitive `MouseEvent` handling.

time
: Provides a `Timer` abstraction with a special `ControlledTimer` implementation used for slowing down simulations (together with the `SystemSpeed` class), but also for unit and integration tests. 

ui
: Provides Kotlin-common abstractions for common UI functionalities, such as `Clipboard`, a simple MVC system consisting of `UIView` and `UIController`. The JVM-specific package adds many Java Swing extensions and base support for user preference management and editing, as well as for dealing with properties and translations.

## Other concepts

`Actions` are a Kotlin-common abstraction whose implementations encapsulate logic that can be executed by the user, e.g. by clicking a button or selecting a menu item. Example: "Open circuit". Platform-specific UI implementations can bind to an `Action` and get automatically disabled if their `Action` is disabled.