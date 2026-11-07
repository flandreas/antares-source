package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.addressable.OpenMemoryLibraryItemRequest
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.library.AbstractLibraryAction
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

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

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && selectedItem is MemoryLibraryItem

    override fun execute(event: ActionEvent) {
        openAsSavable(controller.selectedItem as MemoryLibraryItem)
    }

    private fun openAsSavable(item: MemoryLibraryItem) {
        graphDataViewController.openLibraryItem(item, "-", name)
    }
}