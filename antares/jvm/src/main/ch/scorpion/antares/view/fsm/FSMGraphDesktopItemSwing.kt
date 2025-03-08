package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMDrawing
import ch.scorpion.antares.model.fsm.FSMLibraryItem
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JToggleButton

class FSMGraphDesktopItemSwing(
    item: FSMLibraryItem,
    private val applicationDataViewController: ApplicationDataViewController,
    eventBus: EventBus = BaseModule.eventBus,
    viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractTitledGraphDesktopViewItemSwing(
    createTitleText(item.storable),
    JPanel(),
    applicationDataViewController,
    eventBus
), FSMPanelView {

    companion object {
        fun createTitleText(fsm: FSMDrawing): String =
            "${Translations.getString("library.element.fsm.name")} \"${fsm.name.getTranslation()}\""
    }

    private val fsm: FSMDrawing get() = applicationDataViewController.data!!.content as FSMDrawing

    private val controller = FSMPanelController(item, eventBus = eventBus)

    private val localToolbar = ToolBar(controller.editor)

    private val canvas = CanvasJvm(controller.drawingView)

    init {
        controller.view = this
        buildToolbar()
        buildUI(viewManager)
        controller.setDrawing(fsm)
    }

    override fun dispose() {
        canvas.dispose()
    }

    override val closeTarget: Any get() = controller.drawingView

    private fun buildUI(viewManager: ContentViewManager) {
        contentPanel.layout = BorderLayout()
        contentPanel.add(
            FocusPanel(
                controller.drawingView.canvas as JComponent,
                controller.drawingView,
                controller.drawingView.canvas as JComponent,
                viewManager
            ),
            BorderLayout.CENTER
        )
    }

    private fun buildToolbar() {
        localToolbar.addSeparator()
        localToolbar.addAction(applicationDataViewController.saveAction)
        localToolbar.addGap()

        localToolbar.addTool(controller.editor.selectionTool, "/img/pointer24.png", Translations.getString("edit.tool.select"))
        val toolLockButton = JToggleButton(ActionWrapperSwing((controller.editor as EditorImpl).toolLockAction))
        toolLockButton.text = null
        localToolbar.add(toolLockButton)
        localToolbar.addTool(controller.stateTool, "/img/oval24.png", Translations.getString("antares.fsm.state"))
        localToolbar.addTool(controller.transitionTool, "/img/polyline24.png", Translations.getString("antares.fsm.transition"))
        localToolbar.addGap()
        localToolbar.addAction(controller.createTruthTableAction)
    }

    /** ---- [AbstractTitledGraphDesktopViewItemSwing] */

    override val toolBar: Any get() = localToolbar

    override fun createHeaderText(): String = createTitleText(fsm)

    override fun displays(content: Any?): Boolean =
        applicationDataViewController.data?.content is FSMDrawing && content === fsm

    /** ---- [FSMPanelView] */

    override fun askForTruthTableName(actionName: String, truthTableName: String): String? {
        val name = JOptionPane.showInputDialog(
            Frame.getFrames()[0],
            Translations.getString("antares.fsm.createTruthTable.question"),
            actionName,
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            truthTableName
        ) as String?
        if (StringUtils.isNotBlank(name)) {
            return name
        }
        return null
    }

    override fun showValidationError(actionName: String, msg: String) {
        JOptionPane.showMessageDialog(
            Frame.getFrames()[0],
            msg,
            actionName,
            JOptionPane.ERROR_MESSAGE
        )
    }

    /** ---- [FSMGraphDesktopItemSwing] */

}