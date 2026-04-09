# Design overview

This document gives an overview the Antares application and the design of the Jabbah framework it is built on.

## Cross-cutting concerns

### Project structure

TODO

### Module system

TODO

### Test utility modules

TODO

## Modules

The Jabbah framework consists of a strickly layered stack of modules, implemented as separate Gradle subprojects in order to enforce dependencies pointing only downwards.

[base](module-base.md)
: Besides general purpose functionality like the `collection` package, the main purpose of the `base` module is to provide abstractions that have to be implemented differently on the JVM and JS platform, like the `InputEvent` class in the event package.

[io](module-io.md)
: TODO

[animation](module-animation.md)
: TODO

[draw](module-draw.md)
: TODO

[edit](module-edit.md)
: TODO

[execution](module-execution.md)
: TODO

[app](module-app.md)
: TODO

[base](module-graph.md)
: TODO

### Module `antares`
[antares](module-antares.md)
: TODO
