package io.antarescircuit.jabbah.base.invocation

/**
 * Invokes an operation on the event thread of the system and indicates on the UI that
 * the system is busy, e.g. by showing a "wait" mouse pointer.
 */
abstract class InvocationHandler {

	companion object {
		var implementation: InvocationHandler = UnimplementedInvocationHandler()

		fun invoke(runnable: () -> Unit) {
			implementation.invokeImpl(runnable)
		}
	}

	protected abstract fun invokeImpl(runnable: () -> Unit)
}

class SynchronousInvocationHandler : InvocationHandler() {

	override fun invokeImpl(runnable: () -> Unit) {
		runnable.invoke()
	}
}

private class UnimplementedInvocationHandler : InvocationHandler() {
	override fun invokeImpl(runnable: () -> Unit) {
		throw UnsupportedOperationException("not implemented")
	}
}