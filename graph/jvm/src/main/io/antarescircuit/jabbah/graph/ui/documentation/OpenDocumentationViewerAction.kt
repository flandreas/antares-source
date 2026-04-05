package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

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

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && (controller.selectedItem as ContainerLibraryElement).storable?.documentation != null

    override fun execute(event: ActionEvent) {
        val metaGraph = (controller.selectedItem as ContainerLibraryElement).storable!!
        DocumentationViewerFrameSwing(applicationName, metaGraph.documentation!!, metaGraph.name)
    }
}