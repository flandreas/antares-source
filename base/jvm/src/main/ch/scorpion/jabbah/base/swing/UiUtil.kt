package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import java.awt.Color
import java.awt.EventQueue
import java.lang.reflect.InvocationTargetException
import javax.swing.*
import javax.swing.plaf.FontUIResource


/**
 * Contains various static utility methods.
 */
object UiUtil {

	/**
	 * Invokes the specified invokable on the [EventQueue] an returns immediately.
	 * The lambda returns `true` if it has dispatched the invokable on the event dispatch thread,
	 * `false` if we are already on the event dispatch thread, and this lambda did nothing.
	 *
	 * Can be replaced for testing purposes with an implementations that does nothing and always
	 * return `false`.
	 */
	var eventQueueInvoker: (invokable: () -> Unit) -> Boolean = {
		if (!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater { it.invoke() }
		}
		false
	}

    /**
     * A wrapper around [SwingUtilities.invokeAndWait] which wraps exceptions in
     * [RuntimeException]s. If called from the EDT, the runnable is simply run.
     */
    fun invokeAndWait(runnable: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run()
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable)
            } catch (e: InterruptedException) {
                // swallow
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e.cause)
            }

        }
    }

    /**
     * A wrapper around [SwingUtilities.invokeAndWait].
     */
    @Throws(InterruptedException::class, InvocationTargetException::class)
    fun invokeAndWaitThrowing(runnable: Runnable) {
        SwingUtilities.invokeAndWait(runnable)
    }

    /**
     * A wrapper around [SwingUtilities.invokeAndWait].
     */
    fun invokeLater(runnable: Runnable) {
        SwingUtilities.invokeLater(runnable)
    }

    fun setUIFont(f: FontUIResource) {
        val keys = UIManager.getDefaults().keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val value = UIManager.get(key)
            if (value != null && value is FontUIResource) {
	            UIManager.put(key, f)
            }
        }
    }

	fun decorateTextArea(textArea: JTextArea): JScrollPane {
		val scroll = JScrollPane(textArea)
		scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		val border = UIManager.getBorder("TextField.border")
		if (border != null) {
			scroll.border = border
		}
		return scroll
	}

	fun getBackgroundDivertColor(parent: JComponent): Color {
		val bg = parent.background
		return Color(bg.red - 24, bg.green - 24, bg.blue - 24)
	}

	fun getButtonPressColor(parent: JComponent): Color {
		val bg = parent.background
		return Color(bg.red - 40, bg.green - 40, bg.blue - 40)
	}

	fun createToolBarButton(action: Action): JButton {
		val button = JButton(ActionWrapperSwing(action))
		button.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
		button.icon = ImageIcon(UiUtil::class.java.getResource(action.imagePath))
		button.text = null
		button.toolTipText = action.name
		RolloverButtonEnabler(button)
		return button
	}
}