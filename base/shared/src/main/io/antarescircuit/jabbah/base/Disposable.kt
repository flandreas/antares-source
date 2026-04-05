package io.antarescircuit.jabbah.base

/**
 * An object that needs to be explicitly informed by calling [dispose] when it is not
 * used anymore. It can then release consumed resources or deregister as listener from
 * other objects it has registered to.
 */
interface Disposable {
    fun dispose()
}