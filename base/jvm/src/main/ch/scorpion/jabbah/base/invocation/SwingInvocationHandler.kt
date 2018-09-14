package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.logger
import javax.swing.SwingUtilities

/**
 * Executes a [Runnable] asynchronously on the AWT dispatching thread, surrounded by [BusyHandler] treatment.
 */
class SwingInvocationHandler : InvocationHandler() {

    private val LOG by logger(SwingInvocationHandler::class)

    override fun invokeImpl(doRun: Runnable) {
        BusyHandler.increment()
        SwingUtilities.invokeLater {
            try {
                doRun.run()
            } catch (e: Throwable) {
                val developerMode = false
                if (developerMode) {
                    ErrorHandler.exception(e)
                } else {
                    LOG.error("Error in invocation handler: ${e.message}")
                }
            } finally {
                BusyHandler.decrement()
            }
        }
    }

	override fun invokeImpl(runnable: () -> Unit) {
		BusyHandler.increment()
		SwingUtilities.invokeLater {
			try {
				runnable.invoke()
			} catch (e: Throwable) {
				val developerMode = false
				if (developerMode) {
					ErrorHandler.exception(e)
				} else {
					LOG.error("Error in invocation handler: ${e.message}")
				}
			} finally {
				BusyHandler.decrement()
			}
		}
	}
}