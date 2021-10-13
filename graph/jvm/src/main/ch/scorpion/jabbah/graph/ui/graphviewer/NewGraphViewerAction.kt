package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class NewGraphViewerAction(
	private val applicationName: String,
	controller: LibraryTreeViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementAction(
	actionBaseName = "graph.action.newGraphViewer",
	controller.applicationModeHolder,
	operation = Operation.View,
	controller,
	eventBus
) {
	override fun execute(event: ActionEvent) {
		val metaGraph = (controller.selectedItem as ContainerLibraryElement).metaGraph
		GraphViewerFrameSwing(applicationName, metaGraph!!)
	}
}