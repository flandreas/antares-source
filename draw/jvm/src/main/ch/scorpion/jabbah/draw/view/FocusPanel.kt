package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.Canvas
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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
        val focusBorder = BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 1)
        val nonFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1)
    }

    init {
        (view.canvas as JComponent).addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                updateFocusBorder()
                viewManager.activeView = view
            }

            override fun focusLost(e: FocusEvent?) {
                updateFocusBorder()
            }
        })
        (view.canvas as JComponent).addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                view.requestFocus()
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
        if ((view.canvas as JComponent).isFocusOwner) {
            border = focusBorder
        } else {
            border = nonFocusBorder
        }
    }
}