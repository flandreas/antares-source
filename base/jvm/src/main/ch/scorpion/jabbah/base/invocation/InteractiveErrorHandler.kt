package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea


object InteractiveErrorHandler : ErrorHandler() {

	private val LOG by logger(InteractiveErrorHandler::class)

    private var parentFrame: JFrame? = null
	private var isDeveloper = false
	private var isHandling = false

    override fun initializeImpl(parentFrame: JFrame, isDeveloper: Boolean) {
        this.parentFrame = parentFrame
	    this.isDeveloper = isDeveloper
    }

    override fun exceptionImpl(x: Throwable) {
	    LOG.error("Unexpected error: ${x.message}", x)

        if (parentFrame != null && !isHandling) {
	        isHandling = true
            if (isDeveloper) {
	            showDeveloperDialog(x)
            } else {
                showUserDialog(x)
            }
	        isHandling = false
        }
    }

	private fun renderStackTrace(x: Throwable): String =
		ByteArrayOutputStream().use {
			x.printStackTrace(PrintStream(it))
			it.toString()
		}

	private fun showDeveloperDialog(x: Throwable) {
		val ta = JTextArea()
		ta.isEditable = false
		ta.text = renderStackTrace(x)

		val sp = JScrollPane(ta)
		sp.preferredSize = Dimension(400, 300)
		ta.caretPosition = 0

		JOptionPane.showMessageDialog(
			parentFrame,
			sp,
			Translations.getString("base.unexpectedError.title"),
			JOptionPane.ERROR_MESSAGE)
	}

	private fun showUserDialog(x: Throwable) {
		UnexpectedErrorPanel.showAsDialog(parentFrame!!, renderStackTrace(x))
	}
}