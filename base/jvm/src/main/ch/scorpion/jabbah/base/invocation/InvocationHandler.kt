package ch.scorpion.jabbah.base.invocation

/**
 * The [InvocationHandler] processes {@link Runnable}s asynchronously.
 * @see #invoke(Runnable)
 */
abstract class InvocationHandler {

    companion object {
        val implementation: InvocationHandler = SwingInvocationHandler()

        /**
         * Invokes [Runnable.run] of the provided [Runnable] asynchronously. That is, this method returns
         * immediately and the [Runnable] will be called from another [Thread] at some point in the future.
         */
        fun invoke(doRun: Runnable) {
            implementation.invokeImpl(doRun)
        }
    }

    protected abstract fun invokeImpl(doRun: Runnable)

}