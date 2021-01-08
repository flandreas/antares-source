package ch.scorpion.jabbah.base.invocation

class InvocationHandlerJs : InvocationHandler() {

	override fun invokeImpl(runnable: () -> Unit) {
		runnable.invoke()
	}
}