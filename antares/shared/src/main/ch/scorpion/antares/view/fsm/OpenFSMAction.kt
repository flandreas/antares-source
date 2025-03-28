package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMLibraryItem
import ch.scorpion.antares.model.fsm.OpenFSMLibraryItemRequest
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class OpenFSMAction(
    private val graphDataViewController: GraphDataViewController,
    controller: LibraryTreeViewController
) : AbstractLibraryAction("file.action.open", Operation.View, controller) {

    private val openHandler: EventHandler<OpenFSMLibraryItemRequest> = {
        if (!controller.applicationModeHolder.currentMode.isEdit()) {
            controller.eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
        } else {
            openAsSavable(it.item)
        }
    }

    init {
        controller.eventBus.register(OpenFSMLibraryItemRequest::class, openHandler)
    }

    override fun dispose() {
        super.dispose()
        controller.eventBus.unregister(openHandler)
    }

    override fun execute(event: ActionEvent) {
        openAsSavable(controller.selectedItem as FSMLibraryItem)
    }

    private fun openAsSavable(item: FSMLibraryItem) {
        graphDataViewController.openLibraryItem(item, "-", this.name)
    }
}