package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.invocation.BusyHandler
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/**
 * Builds and shows a modal [JDialog] with the following capabilities:
 *
 * - involvement of [BusyHandler]
 * - set content in contentPane
 * - default button handling
 * - packing and centering to parent [Frame]
 *
 * @param T the type of the content
 */
class DialogBuilder<T: JComponent>(private val parent: Frame) {

	private val dialog = JDialog(parent, true)
	private lateinit var content: T

	fun content(factory: (JDialog) -> T): DialogBuilder<T> {
		content = factory.invoke(dialog)
		dialog.contentPane = content
		return this
	}

	fun defaultButton(provider: (T) -> JButton?): DialogBuilder<T> {
		provider.invoke(content)?.let { SwingUtilities.getRootPane(content).defaultButton = it }
		return this
	}

	fun title(title: String): DialogBuilder<T> {
		dialog.title = title
		return this
	}

	fun resizable(): DialogBuilder<T> {
		dialog.isResizable = true
		return this
	}

	fun nonResizable(): DialogBuilder<T> {
		dialog.isResizable = false
		return this
	}

	fun menu(menu: JMenuBar): DialogBuilder<T> {
		dialog.rootPane.jMenuBar = menu
		return this
	}

	fun preferredSize(size: Dimension): DialogBuilder<T> {
		dialog.preferredSize = size
		return this
	}

	fun show() {
		BusyHandler.register(dialog, null)
		setupWindowListener()
		dialog.pack()
		dialog.setLocationRelativeTo(parent)
		dialog.isVisible = true
	}

	private fun setupWindowListener() {
		dialog.addWindowListener(object : WindowAdapter() {
			override fun windowClosed(e: WindowEvent?) {
				BusyHandler.deregister(dialog)
			}
		})
	}
}