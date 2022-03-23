package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.invocation.BusyHandler
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.KeyEvent
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

	lateinit var content: T
		private set

	private var onWindowOpened: (T) -> Unit = {}

	private var onWindowClosed: (T) -> Unit = {}

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

	fun minimumSize(size: Dimension): DialogBuilder<T> {
		dialog.minimumSize = size
		return this
	}

	fun onWindowOpened(handler: (T) -> Unit): DialogBuilder<T> {
		this.onWindowOpened = handler
		return this
	}

	fun onWindowClosed(handler: (T) -> Unit): DialogBuilder<T> {
		this.onWindowClosed = handler
		return this
	}

	fun show(): DialogBuilder<T> {
		BusyHandler.register(dialog, null)
		setupWindowListener()
		setupEscapeListener()
		dialog.pack()
		dialog.setLocationRelativeTo(parent)
		dialog.isVisible = true
		return this
	}

	private fun setupWindowListener() {
		dialog.addWindowListener(object : WindowAdapter() {
			override fun windowOpened(e: WindowEvent?) {
				this@DialogBuilder.onWindowOpened(content)
			}
			override fun windowClosed(e: WindowEvent?) {
				this@DialogBuilder.onWindowClosed(content)
				BusyHandler.deregister(dialog)
			}
		})
	}

	private fun setupEscapeListener() {
		dialog.rootPane.registerKeyboardAction(
			{ dialog.dispose() },
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW)
	}
}