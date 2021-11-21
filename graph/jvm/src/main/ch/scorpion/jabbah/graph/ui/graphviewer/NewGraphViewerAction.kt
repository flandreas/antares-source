package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class NewGraphViewerAction(
	private val applicationName: String,
	private val controller: LibraryTreeViewController
) : AbstractAction(
	baseName = "graph.action.newGraphViewer",
) {
	override fun execute(event: ActionEvent) {
		val metaGraph = (controller.selectedItem as ContainerLibraryElement).metaGraph
		GraphViewerFrameSwing(applicationName, metaGraph!!)
	}
}