package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.Canvas
import java.awt.BorderLayout
import java.awt.event.*
import java.awt.event.MouseEvent.BUTTON1
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.UIManager

/**
 * A [JPanel] that displays focus ownership, and that activates its [View] in the [ViewManager]
 * when it gets the focus.
 *
 * @param child the [JComponent] to be added as a direct child of this [FocusPanel]
 * @property view the [View] whose [Canvas] requests the focus
 */
class FocusPanel(
	child: JComponent,
	val view: View<out InputEventContext>,
	viewManager: ViewManager
) : JPanel() {

	companion object {
		private const val thickness = 1
		val focusBorder = BorderFactory.createLineBorder(UIManager.getColor("Component.focusColor"), thickness)!!
		val nonFocusBorder = BorderFactory.createEmptyBorder(thickness, thickness, thickness, thickness)!!
	}

	init {
		(view.canvas as JComponent).addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent) {
				if (!e.isTemporary) {
					updateFocusBorder()
					viewManager.activeView = view
				}
			}

			override fun focusLost(e: FocusEvent) {
				if (!e.isTemporary) {
					updateFocusBorder()
				}
			}
		})
		(view.canvas as JComponent).addMouseListener(object : MouseAdapter() {
			override fun mousePressed(e: MouseEvent) {
				if (e.button == BUTTON1) {
					view.requestFocus()
				}
			}
		})

		buildUI(child)
	}

	private fun buildUI(child: JComponent) {
		layout = BorderLayout()
		add(child, BorderLayout.CENTER)
		border = nonFocusBorder
	}

	private fun updateFocusBorder() {
		border = if ((view.canvas as JComponent).isFocusOwner) {
			focusBorder
		} else {
			nonFocusBorder
		}
	}
}