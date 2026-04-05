package io.antarescircuit.jabbah.graph.ui.graphviewer

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

class NewGraphViewerAction(
	private val applicationName: String,
	controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction(
	"graph.action.newGraphViewer",
	operation = Operation.View,
	controller,
	onlyEnabledInEditMode = false
) {
	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		val metaGraph = (controller.selectedItem as ContainerLibraryElement).storable
		GraphViewerFrameSwing(applicationName, metaGraph!!)
	}
}