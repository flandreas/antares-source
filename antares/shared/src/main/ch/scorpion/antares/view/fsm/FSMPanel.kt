package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.*
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.truthtable.OpenTruthTableItemRequest
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.MouseEventImpl
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.text.ComponentAtLocationTool
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.tool.InputEventHandlerTool
import ch.scorpion.jabbah.edit.view.DrawingViewImpl

interface FSMPanelView : UIView {

    /**
     * Asks the user for the name of the new [TruthTable] to be created from a [FSMDrawing].
     * @param actionName the name of the calling [Action]. Can be used as title of dialog.
     * @param truthTableName the suggested name of the [TruthTable]. Taken from the [FSMDrawing] name.
     * @return `null` if the user cancelled the action
     */
    fun askForTruthTableName(actionName: String, truthTableName: String): String?

    fun showValidationError(actionName: String, msg: String)
}

class FSMPanelController(
    private val libraryItem: FSMLibraryItem,
    private val fsmService: FSMService = AntaresModelModule.fsmService,
    private val eventBus: EventBus = BaseModule.eventBus,
) : AbstractUIController<FSMPanelView>() {

    companion object {
        private val LOG by logger(FSMPanelController::class)
    }

    private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }

    val drawingView: DrawingView<FSMDrawing> = DrawingViewImpl(FSMDrawing())

    val editor = EditEditorModule.createEditor(drawingView as DrawingView<Drawing<Component>>)

    val stateTool = ComponentAtLocationTool(editor, cursor = Cursor.CLICK, factory = this::createFSMState )

    val transitionTool = InputEventHandlerTool(
        FSMTransitionToolHandler.fsmTransitionToolHandler,
        editor
    ) {
        FSMTransitionToolHandler.use(EditInputEventContext(editor, MouseEventImpl()))
    }

    val createTruthTableAction: Action = CreateTruthTableAction()

    init {
        eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
        eventBus.post(CurrentEditorEvent(editor))

        val editable = Authorizer.isCurrentUserAuthorizedTo(Operation.Change, libraryItem.library!!)
        editor.view.editable = editable

        System.invokeLater {
            editor.active = editable
            createTruthTableAction.enabled = editor.view.editable
        }
    }

    override fun dispose() {
        super.dispose()
        editor.dispose()
        eventBus.unregister(applicationDataContentHandler)
    }

    fun createFSMState(): FSMState = fsmService.createState(editor.drawing as FSMDrawing)

    private fun handle(event: ApplicationDataContentEvent) {
       setDrawing(event.data.content as FSMDrawing)
    }

    fun setDrawing(fsmDrawing: FSMDrawing) {
        drawingView.setDrawing(fsmDrawing)
    }

    private fun createTruthTable(actionName: String) {
        try {
            LOG.userTrail("Creating truth table from FSM")

            val truthTable = FSMTruthTableCreator(drawingView.drawing).create()
            val name = view.askForTruthTableName(actionName, drawingView.drawing.name.getTranslation())

            if (name != null) {
                truthTable.name = Name(name)
                val truthTableItem = TruthTableLibraryItem(truthTable)
                val library = libraryItem.library!!
                val directory = library.libraryService.getDirectoryOf(library, libraryItem)
                library.libraryService.addLibraryItem(library, truthTableItem, directory)

                eventBus.post(OpenTruthTableItemRequest(truthTableItem))
            }
        } catch (e: FSMException) {
            view.showValidationError(actionName, e.message!!)
        }
    }

    private inner class CreateTruthTableAction : AbstractAction("antares.fsm.createTruthTable.action") {

        override val opensDialog: Boolean get() = true

        override fun execute(event: ActionEvent) {
            createTruthTable(name)
        }
    }
}