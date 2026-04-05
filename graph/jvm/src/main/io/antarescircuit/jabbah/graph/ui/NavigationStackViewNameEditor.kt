package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.graph.ui.NavigationStackViewSwing.Companion.HEIGHT
import java.awt.Component
import java.awt.Container
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.SwingUtilities

/**
 * Support for in-place editing of root name.
 */
object NavigationStackViewNameEditor {

    private val TEXT_EDITOR = JTextField(15)

    private lateinit var view: NavigationStackViewSwing
    private lateinit var uuid: UUID

    /** Issue 'Save' when user clicks on glass pane.*/
    private val clickListener = ClickListener()

    init {
        TEXT_EDITOR.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                save()
            }
        })

        TEXT_EDITOR.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_ENTER -> save()
                    KeyEvent.VK_ESCAPE -> cancel()
                }
            }
        })
    }

    fun startEditor(
        view: NavigationStackViewSwing,
        uuid: UUID,
        name: String,
        relLocation: Point2D,
        font: Font
    ) {
        this.view = view
        this.uuid = uuid

        val glassPane = (SwingUtilities.getWindowAncestor(view) as JFrame).glassPane

        if (glassPane is Container) {
            glassPane.layout = null

            val location = SwingUtilities.convertPoint(
                view,
                Graphics2DJvm.toAwtPoint(relLocation),
                glassPane
            )

            TEXT_EDITOR.text = name
            TEXT_EDITOR.setBounds(
                location.x, location.y,
                TEXT_EDITOR.preferredSize.width, HEIGHT
            )
            TEXT_EDITOR.font = Graphics2DJvm.toAwtFont(font)
            TEXT_EDITOR.selectAll()


            glassPane.addMouseListener(clickListener)
            glassPane.add(TEXT_EDITOR)
            glassPane.isVisible = true
            glassPane.repaint()

            SwingUtilities.invokeLater {
                TEXT_EDITOR.requestFocusInWindow()
            }
        }
    }

    private fun save() {
        val glassPane = (SwingUtilities.getWindowAncestor(TEXT_EDITOR) as JFrame?)?.glassPane
            ?: return

        val newName = TEXT_EDITOR.text
        if (StringUtils.isBlank(newName)) {
            cancel()
            return
        }

        stopEditing(glassPane)
        val oldName = view.controller.navigationStack.rootEntry!!.content.drawing.name.value
        if (newName != oldName) {
            view.controller.changeName(newName)
        }
    }

    private fun cancel() {
        stopEditing((SwingUtilities.getWindowAncestor(TEXT_EDITOR) as JFrame?)?.glassPane)
    }

    private fun stopEditing(glassPane: Component?) {
        if (glassPane is Container) {
            glassPane.remove(TEXT_EDITOR)
            glassPane.isVisible = false
            glassPane.removeMouseListener(clickListener)
        }
    }

    private class ClickListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            save()
        }
    }
}