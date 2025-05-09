package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class OpenDocumentationViewerAction(
    private val applicationName: String,
    controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction(
    "graph.action.showDocumentationInNewWindow",
    operation = Operation.View,
    controller,
    onlyEnabledInEditMode = false
) {
    override val opensDialog: Boolean get() = true

    override fun calculateEnabledness(): Boolean =
        super.calculateEnabledness() && (controller.selectedItem as ContainerLibraryElement).storable?.documentation != null

    override fun execute(event: ActionEvent) {
        val metaGraph = (controller.selectedItem as ContainerLibraryElement).storable!!
        DocumentationViewerFrameSwing(applicationName, metaGraph.documentation!!, metaGraph.name)
    }
}