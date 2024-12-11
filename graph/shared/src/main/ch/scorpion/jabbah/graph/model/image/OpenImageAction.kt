package ch.scorpion.jabbah.graph.model.image

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/** An [Action] for opening an [ImageIdentification] of a [ImageLibraryElement].*/
class OpenImageAction(
    private val graphDataViewController: GraphDataViewController,
    controller: LibraryTreeViewController
) : AbstractLibraryAction("file.action.open", Operation.View, controller) {

    private val openHandler: EventHandler<OpenImageLibraryElementRequest> = {
        if (!controller.applicationModeHolder.currentMode.isEdit()) {
            controller.eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
        } else {
            openAsSavable(it.element)
        }
    }

    init {
        controller.eventBus.register(OpenImageLibraryElementRequest::class, openHandler)
    }

    override fun dispose() {
        super.dispose()
        controller.eventBus.unregister(openHandler)
    }

    override fun execute(event: ActionEvent) {
        openAsSavable(controller.selectedItem as ImageLibraryElement)
    }

    override fun calculateEnabledness(): Boolean =
        super.calculateEnabledness() && selectedItem is ImageLibraryElement

    private fun openAsSavable(element: ImageLibraryElement) {
        graphDataViewController.openLibraryItem(element, "-", this.name, ImageIdentificationSavable(element))
    }
}