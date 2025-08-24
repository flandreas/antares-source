package ch.scorpion.jabbah.base.invocation

interface ErrorHandlerPlugin {
    fun handleError(t: Throwable)
}