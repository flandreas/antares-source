package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.logger
import javax.swing.SwingUtilities

/**
 * Executes a [Runnable] asynchronously on the AWT dispatching thread, surrounded by [BusyHandler] treatment.
 */
class SwingInvocationHandler : InvocationHandler() {

	companion object {
        private val LOG by logger(SwingInvocationHandler::class)
	}

    override fun invokeImpl(doRun: Runnable) {
        BusyHandler.increment()
        SwingUtilities.invokeLater {
            try {
                doRun.run()
            } catch (e: Throwable) {
                LOG.error("Error in invocation handler: ${e.message}")
	            ErrorHandler.exception(e)
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
				LOG.error("Error in invocation handler: ${e.message}")
				ErrorHandler.exception(e)
			} finally {
				BusyHandler.decrement()
			}
		}
	}
}