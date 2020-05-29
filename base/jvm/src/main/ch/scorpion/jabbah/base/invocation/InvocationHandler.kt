package ch.scorpion.jabbah.base.invocation

/**
 * The [InvocationHandler] processes [Runnable]s asynchronously.
 * @see #invoke(Runnable)
 */
abstract class InvocationHandler {

    companion object {
        var implementation: InvocationHandler = SwingInvocationHandler()

        /**
         * Invokes [Runnable.run] of the provided [Runnable] asynchronously. That is, this method returns
         * immediately and the [Runnable] will be called from another [Thread] at some point in the future.
         */
        fun invoke(doRun: Runnable) {
            implementation.invokeImpl(doRun)
        }

	    fun invoke(runnable: () -> Unit) {
		    implementation.invokeImpl(runnable)
	    }
    }

    protected abstract fun invokeImpl(doRun: Runnable)

	protected abstract fun invokeImpl(runnable: () -> Unit)

}

/** Executes [InvocationHandler] invocables synchronously. Mainly used for testing purposes.*/
class SynchronousInvocationHandler : InvocationHandler() {

	override fun invokeImpl(doRun: Runnable) {
		doRun.run()
	}

	override fun invokeImpl(runnable: () -> Unit) {
		runnable.invoke()
	}
}