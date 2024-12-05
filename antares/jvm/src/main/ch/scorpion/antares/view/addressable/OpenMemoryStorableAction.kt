package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.addressable.MemorySavable
import ch.scorpion.antares.model.addressable.OpenMemoryLibraryItemRequest
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class OpenMemoryStorableAction(
    private val graphDataViewController: GraphDataViewController,
    controller: LibraryTreeViewController
) : AbstractLibraryAction("file.action.open", Operation.View, controller) {

    private val openHandler: EventHandler<OpenMemoryLibraryItemRequest> = {
        if (!controller.applicationModeHolder.currentMode.isEdit()) {
            controller.eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
        } else {
            openAsSavable(it.item)
        }
    }

    init {
        controller.eventBus.register(OpenMemoryLibraryItemRequest::class, openHandler)
    }

    override fun dispose() {
        super.dispose()
        controller.eventBus.unregister(openHandler)
    }

    override fun calculateEnabledness(): Boolean =
        super.calculateEnabledness() && selectedItem is MemoryLibraryItem

    override fun execute(event: ActionEvent) {
        openAsSavable(controller.selectedItem as MemoryLibraryItem)
    }

    private fun openAsSavable(item: MemoryLibraryItem) {
        graphDataViewController.openLibraryItem(item, "-", name, MemorySavable(item))
    }
}