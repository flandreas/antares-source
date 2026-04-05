package io.antarescircuit.jabbah.base.invocation

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea

object InteractiveErrorHandler : ErrorHandler() {

	private val LOG by logger(InteractiveErrorHandler::class)

	/** The name of the [Boolean] property in [Properties] determining whether to show a dialog. */
	const val PROP_SHOW_UNEXPECTED_ERROR = "io.antarescircuit.jabbah.base.invocation.showUnexpectedError"

    private var parentFrame: JFrame? = null
	private var versionId: String = ""
	private var isDeveloper = false
	private var isHandling = false

	private val plugins: MutableList<ErrorHandlerPlugin> = mutableListOf()

    override fun initializeImpl(parentFrame: JFrame, versionId: String, isDeveloper: Boolean) {
        this.parentFrame = parentFrame
	    this.versionId = versionId
	    this.isDeveloper = isDeveloper
    }

    override fun exceptionImpl(x: Throwable) {
	    LOG.error("Unexpected error: ${x.message}", x)
		val stackTrace = renderStackTrace(x)

		involvePlugins(x)

        if (parentFrame != null && !isHandling) {
	        isHandling = true
            if (isDeveloper) {
	            showDeveloperDialog(stackTrace)
            } else {
                showUserDialog(x)
            }
	        isHandling = false
        }
    }

	fun registerPlugin(plugin: ErrorHandlerPlugin) {
		plugins.add(plugin)
	}

	private fun involvePlugins(x: Throwable) {
		plugins.forEach {
			it.handleError(x)
		}
	}

	private fun renderStackTrace(x: Throwable): String =
		ByteArrayOutputStream().use {
			x.printStackTrace(PrintStream(it))
			it.toString()
		}

	private fun showDeveloperDialog(stackTrace: String) {
		val ta = JTextArea()
		ta.isEditable = false
		ta.text = stackTrace

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
		if (BaseModule.properties.getBoolean(PROP_SHOW_UNEXPECTED_ERROR)) {
			UnexpectedErrorPanel.showAsDialog(parentFrame!!, versionId, renderStackTrace(x))
		}
	}
}