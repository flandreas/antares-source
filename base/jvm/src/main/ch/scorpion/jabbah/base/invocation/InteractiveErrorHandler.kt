package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import java.awt.Dimension
import java.awt.Rectangle
import java.io.PrintStream
import java.io.ByteArrayOutputStream
import javax.swing.*


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
                showUserDialog()
            }
	        isHandling = false
        }
    }

	private fun showDeveloperDialog(x: Throwable) {
		val os = ByteArrayOutputStream()
		x.printStackTrace(PrintStream(os))

		val ta = JTextArea()
		ta.isEditable = false
		ta.text = os.toString()

		val sp = JScrollPane(ta)
		sp.preferredSize = Dimension(400, 300)
		ta.caretPosition = 0

		JOptionPane.showMessageDialog(
			parentFrame,
			sp,
			Translations.getString("base.unexpectedError.title"),
			JOptionPane.ERROR_MESSAGE)
	}

	private fun showUserDialog() {
		JOptionPane.showMessageDialog(
			parentFrame,
			Translations.getString("base.unexpectedError.text"),
			Translations.getString("base.unexpectedError.title"),
			JOptionPane.ERROR_MESSAGE)
	}
}