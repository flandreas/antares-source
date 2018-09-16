package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.CompositeTestGraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

/** Builds and fills a [Library] using components from [CompositeTestGraphViewBuilder] used for integration testing.*/
class TestLibraryBuilder(
	private val portFactory: PortFactory = GraphViewModule.portFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke()
) {

	companion object {
		private const val INNER_CUSTOM_COMP = "InnerCustomComp"
	}

	/**
	 * Adds a custom component (as of [CompositeTestGraphViewBuilder.buildInnerCustomComponent] to the specified [Library].
	 * @return the created [MetaGraph] that contains the created custom component
	 */
	fun addInnerCustomComponent(library: Library): MetaGraph {
		val customComp = CompositeTestGraphViewBuilder(INNER_CUSTOM_COMP).buildInnerCustomComponent()
		val containerDrawing = createContainerDrawing(customComp)
		val metaGraph = MetaGraph(GraphStorable(customComp), containerDrawing)
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	private fun createSubGraphVerticeView(name: String, libraryDirectory: LibraryDirectory): SubGraphVerticeViewImpl {
		return (libraryDirectory.get(name) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
	}

	private fun createContainerDrawing(graphView: GraphView<*>): ContainerDrawing {
		val containerDrawing = GraphViewModule.createContainerDrawing()

		containerDrawing.model.graphUUID = graphView.graph!!.uuid
		containerDrawing.model.name = graphView.graph!!.name

		for (circuitInput in graphView.graph!!.graphInputs) {
			containerDrawing.add(
				portFactory.createPortViewComponent(portFactory.createPortView(portFactory.createSubGraphPort(circuitInput))))
		}
		for (circuitOutput in graphView.graph!!.graphOutputs) {
			containerDrawing.add(
				portFactory.createPortViewComponent(portFactory.createPortView(portFactory.createSubGraphPort(circuitOutput))))
		}

		return containerDrawing
	}
}