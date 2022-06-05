package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.View
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.UIManager

/**
 * A [JPanel] that displays focus ownership, and that activates its [View] in the [ContentViewManager]
 * when it gets the focus.
 *
 * @param child the [JComponent] to be added as a direct child of this [FocusPanel]
 * @property contentView the [ContentView] whose mainUI requests the focus
 */
class FocusPanel(
	child: JComponent,
	val contentView: ContentView<*>,
	private val focusSource: JComponent = contentView.mainUI as JComponent,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : JPanel() {

	companion object {
		private const val thickness = 1
		val focusBorder = BorderFactory.createLineBorder(UIManager.getColor("Component.focusColor"), thickness)!!
		val nonFocusBorder = BorderFactory.createEmptyBorder(thickness, thickness, thickness, thickness)!!
	}

	init {
		focusSource.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent) {
				if (!e.isTemporary) {
					updateFocusBorder()
					viewManager.activeView = contentView
				}
			}

			override fun focusLost(e: FocusEvent) {
				if (!e.isTemporary) {
					updateFocusBorder()
				}
			}
		})
		focusSource.addMouseListener(object : MouseAdapter() {
			override fun mousePressed(e: MouseEvent) {
				if (e.button == BUTTON1) {
					focusSource.requestFocus()
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
		border = if (focusSource.isFocusOwner) {
			focusBorder
		} else {
			nonFocusBorder
		}
	}
}