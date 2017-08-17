package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.Action
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities

/**
 * An [Action] for editing the look of a individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor] in a dialog.
 */
class EditSubGraphVerticeViewAction(
    eventBus: EventBus,
    viewManager: ViewManager,
    private val libraryHolder: LibraryHolder
) : AbstractSelectionAwareAction("graph.action.editSubGraphVerticeView", viewManager, eventBus) {

    constructor(): this(BaseModule.eventBus, DrawViewModule.viewManager, LibraryModule.libraryHolder)

    private var editedVerticeView: SubGraphVerticeView<*>? = null

    private var containerPanel: ContainerPanel? = null

    private var oldActiveView: View<out InputEventContext>? = null

    override fun calculateEnabled(): Boolean {
        return getSelectionCount() == 1 && getSingleSelection() is SubGraphVerticeView<*>
    }
    override fun actionPerformed(e: ActionEvent?) {
        editedVerticeView = getSingleSelection() as SubGraphVerticeView<*>

        val frame = SwingUtilities.getRoot(getDrawingView()!!.canvas as Component) as JFrame
        val dialog = JDialog(frame, true)
        dialog.title = Translations.getString("graph.action.editSubGraphVerticeView.name")
        dialog.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(we: WindowEvent?) {
                handleClosed()
            }
            override fun windowClosed(we: WindowEvent?) {
                handleClosed()
            }
        })

        getDrawingView()!!.selectionManager.deselect(editedVerticeView!!)
        editedVerticeView!!.invalidate()

        containerPanel = ContainerPanel(
                GraphViewModule.containerEditorFactory.invoke(eventBus),
                EditModuleJvm.propertySheetPanelFactory,
                eventBus,
                viewManager)

        val panel = EditSubGraphVerticeViewPanel(
            libraryHolder,
            containerPanel!!,
            editedVerticeView!!,
            { dialog.dispose() }
        )

        dialog.contentPane.add(panel)
        dialog.pack()
        dialog.setLocationRelativeTo(frame)
        panel.initialize()

        oldActiveView = viewManager.activeView
        viewManager.registerView(containerPanel!!.editor.view)

        dialog.isVisible = true
    }

    private fun handleClosed() {
        viewManager.unregisterView(containerPanel!!.editor.view)
        viewManager.activeView = oldActiveView

        editedVerticeView!!.invalidate()
        getDrawingView()!!.selectionManager.select(editedVerticeView!!)
    }
}