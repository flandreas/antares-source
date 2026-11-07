package io.antarescircuit.jabbah.base.invocation

interface ErrorHandlerPlugin {
    fun handleError(t: Throwable)
}