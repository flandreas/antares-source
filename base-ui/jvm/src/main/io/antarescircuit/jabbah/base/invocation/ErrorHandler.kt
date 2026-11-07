package io.antarescircuit.jabbah.base.invocation

import io.antarescircuit.jabbah.base.logger
import javax.swing.JFrame
import io.antarescircuit.jabbah.base.swing.UiUtil
import javax.swing.SwingUtilities


abstract class ErrorHandler {

	companion object {
		private val LOG by logger(Companion::class)

		val implementation: ErrorHandler get() = InteractiveErrorHandler

		fun initialize(parentFrame: JFrame, versionId: String, isDeveloper: Boolean = false) {
			implementation.initializeImpl(parentFrame, versionId, isDeveloper)
		}

		fun exception(x: Throwable) {
			if (SwingUtilities.isEventDispatchThread()) {
				implementation.exceptionImpl(x)
			} else {
				try {
					UiUtil.invokeAndWaitThrowing { implementation.exceptionImpl(x) }
				} catch (e: Exception) {
					LOG.error("Error: $e")
				}
			}
		}
	}

	/** Initializes this [ErrorHandler] with the parent [JFrame] in which the error dialog is to be shown.*/
	protected abstract fun initializeImpl(parentFrame: JFrame, versionId: String, isDeveloper: Boolean = false)

	/** Handles the specified [Throwable]. */
	protected abstract fun exceptionImpl(x: Throwable)
}