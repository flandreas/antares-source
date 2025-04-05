package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.NavigationStackViewSwing.Companion.HEIGHT
import java.awt.Container
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.SwingUtilities

/**
 * Support for in-place editing of root name.
 */
object NavigationStackViewNameEditor {

    private val LOG by logger(NavigationStackViewNameEditor::class)

    private val TEXT_EDITOR = JTextField(20)

    private lateinit var view: NavigationStackViewSwing
    private lateinit var uuid: UUID

    init {
        TEXT_EDITOR.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                stopEditor()
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

            glassPane.add(TEXT_EDITOR)
            glassPane.isVisible = true
            glassPane.repaint()

            SwingUtilities.invokeLater {
                TEXT_EDITOR.requestFocusInWindow()
            }
        }
    }

    private fun stopEditor() {
        val glassPane = (SwingUtilities.getWindowAncestor(TEXT_EDITOR) as JFrame).glassPane
        val newName = TEXT_EDITOR.text
        if (glassPane is Container) {
            glassPane.remove(TEXT_EDITOR)
            glassPane.isVisible = false
        }

        val element = LibraryModule.libraryHolder.library.getContainerLibraryElement(uuid)!!
        if (!StringUtils.equals(element.name.value)) {
            LOG.userTrail("Graph name changed in NavigationStackView to '$newName'")
            element.library!!.libraryService.renameContainerLibraryElement(element, TranslatableText(newName!!))
        } else {
            LOG.userTrail("Cancel graph name editing in NavigationStackView without change")
        }
    }
}