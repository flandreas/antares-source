package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.logger
import javax.swing.JFrame
import javax.swing.JOptionPane
import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.JTextArea
import java.io.PrintStream
import java.io.ByteArrayOutputStream


class InteractiveErrorHandler : ErrorHandler() {

	companion object {
        private val LOG by logger(InteractiveErrorHandler::class)
	}

    private var frame: JFrame? = null

    override fun initializeImpl(parentFrame: JFrame) {
        this.frame = parentFrame
    }

    override fun exceptionImpl(x: Throwable) {
        LOG.error("Unexpected error: $x")

        if (frame != null) {
            if (LOG.isDebugEnabled()) {
                val os = ByteArrayOutputStream()
                x.printStackTrace(PrintStream(os))

                val ta = JTextArea()
                ta.text = os.toString()

                val sp = JScrollPane(ta)
                sp.preferredSize = Dimension(400, 300)

                JOptionPane.showMessageDialog(frame, sp, "Error Alert", JOptionPane.ERROR_MESSAGE)
            } else {
                val text = "Unexpected Error"
                JOptionPane.showMessageDialog(frame, text, "Unexpected Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }
}