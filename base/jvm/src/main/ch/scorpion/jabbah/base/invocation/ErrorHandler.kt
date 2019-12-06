package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.logger
import javax.swing.JFrame
import ch.scorpion.jabbah.base.swing.UiUtil
import javax.swing.SwingUtilities


abstract class ErrorHandler {

	companion object {
		private val LOG by logger(Companion::class)

		val implementation: ErrorHandler = InteractiveErrorHandler()

		fun initialize(parentFrame: JFrame) {
			implementation.initializeImpl(parentFrame)
		}

		fun exception(x: Throwable) {
			if (SwingUtilities.isEventDispatchThread()) {
				implementation.exceptionImpl(x)
			} else {
				try {
					UiUtil.invokeAndWaitThrowing(Runnable { implementation.exceptionImpl(x) })
				} catch (e: Exception) {
					LOG.error("Error: $e")
				}

			}
		}
	}

	protected abstract fun initializeImpl(parentFrame: JFrame)

	protected abstract fun exceptionImpl(x: Throwable)
}