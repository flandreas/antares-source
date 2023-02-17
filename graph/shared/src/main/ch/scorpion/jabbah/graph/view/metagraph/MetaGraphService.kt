package ch.scorpion.jabbah.graph.view.metagraph

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.AbstractContainerDrawingFiller
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphView

/** Domain services for [MetaGraph]. */
open class MetaGraphService(
	private val copyPasteService: CopyPasteService = EditModule.copyPasteService
) {

	fun extractMetaGraph(
		graphName: String,
		drawingView: DrawingView<GraphView>,
		componentIds: Collection<Int>,
		libraryDirectory: LibraryDirectory
	): UUID {
		val library = libraryDirectory.library!!

		val metaGraph = createMetaGraph(graphName, drawingView, componentIds)
		tailorMetaGraph(metaGraph)
		AbstractContainerDrawingFiller.fillStandard(metaGraph, addLabel = true)

		val element = library.libraryService.addContainerLibraryElement(library, metaGraph, libraryDirectory)

		replaceComponents(drawingView.drawing, componentIds, element)

		return metaGraph.uuid
	}

	protected open fun tailorMetaGraph(metaGraph: MetaGraph) {
		// empty by default
	}

	private fun createMetaGraph(graphName: String, drawingView: DrawingView<*>, componentIds: Collection<Int>): MetaGraph {
		val metaGraph = MetaGraph.withName(graphName)

		val content = copyPasteService.copy(componentIds, drawingView.drawing)
		copyPasteService.paste(content, metaGraph.graph.graphView as Drawing<Component>, Point2D.ZERO)

		return metaGraph
	}

	private fun replaceComponents(drawing: GraphView, componentIds: Collection<Int>, elem: ContainerLibraryElement) {
		val bbox = drawing.combinedBoundingBox(drawing.getWidthIds(componentIds))
		drawing.remove(componentIds)

		val subGraphVV = elem.getNewInstance<GraphElement>()
		subGraphVV.location = bbox.center

		drawing.add(subGraphVV)
	}
}