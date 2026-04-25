package io.antarescircuit.jabbah.graph.view.metagraph

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.CopyPasteService
import io.antarescircuit.jabbah.edit.model.DrawingService
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.AbstractContainerDrawingFiller
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/** Domain services for [MetaGraph]. */
open class MetaGraphService(
	private val drawingService: DrawingService = EditModule.drawingService,
	private val copyPasteService: CopyPasteService = EditModule.copyPasteService
) {

	companion object {
		private val LOG by logger(MetaGraphService::class)
	}

	fun extractMetaGraph(
		name: TranslatableText,
		type: GraphType,
		drawingView: DrawingView<GraphElementView<*>, GraphView>,
		componentIds: Collection<Int>,
		libraryDirectory: LibraryDirectory
	): UUID {
		try {
			val library = libraryDirectory.library!!

			val metaGraph = createMetaGraph(name, type, drawingView, componentIds)
			tailorMetaGraph(metaGraph)
			AbstractContainerDrawingFiller.fillStandard(metaGraph, addLabel = true)

			val element = library.libraryService.addContainerLibraryElement(library, metaGraph, libraryDirectory)

			replaceComponents(drawingView.drawing, componentIds, element)

			LOG.debug("Extracted Components to MetaGraph ${metaGraph.uuid}")

			return metaGraph.uuid
		} catch (e: Exception) {
			LOG.error("Error while extracting MetaGraph", e)
			throw e
		}
	}

	protected open fun tailorMetaGraph(metaGraph: MetaGraph) {
		// empty by default
	}

	private fun createMetaGraph(name: TranslatableText, type: GraphType, drawingView: DrawingView<*,*>, componentIds: Collection<Int>): MetaGraph {
		val metaGraph = MetaGraph.create(name, type)

		val content = copyPasteService.copy(componentIds, drawingView.drawing)
		copyPasteService.paste(content, metaGraph.graph.graphView as Drawing<Component>, Point2D.ZERO, drawingView)

		return metaGraph
	}

	private fun replaceComponents(drawing: GraphView, componentIds: Collection<Int>, elem: ContainerLibraryElement) {
		val bbox = Drawable.combinedBoundingBox(drawing.getWidthIds(componentIds))

		val components = drawing.getWidthIds(componentIds)
		drawingService.delete(components.toList(), drawing)

		val subGraphVV = elem.getNewInstance<GraphElement>()
		subGraphVV.location = bbox.center

		drawing.add(subGraphVV)
	}
}