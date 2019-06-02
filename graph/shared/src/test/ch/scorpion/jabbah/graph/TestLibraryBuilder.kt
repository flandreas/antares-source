package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.CompositeTestGraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

/** Builds and fills a [Library] using components from [CompositeTestGraphViewBuilder] used for integration testing.*/
class TestLibraryBuilder(
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke()
) {

	companion object {
		const val INNER_CUSTOM_COMP = "InnerCustomComp"
		const val OUTER_CUSTOM_COMP = "OuterCustomComp"
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

	fun addOuterCustomComponent(library: Library): MetaGraph {
		val outerComp = CompositeTestGraphViewBuilder(OUTER_CUSTOM_COMP).buildOuterCustomComponent(createSubGraphVerticeView(INNER_CUSTOM_COMP, library))
		val containerDrawing = createContainerDrawing(outerComp)
		val metaGraph = MetaGraph(GraphStorable(outerComp), containerDrawing)
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	private fun createSubGraphVerticeView(name: String, libraryDirectory: LibraryDirectory): SubGraphVerticeViewImpl {
		return (libraryDirectory.get(name) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
	}

	private fun createContainerDrawing(graphView: GraphView<*>): ContainerDrawing {
		val containerDrawing = GraphViewModule.createContainerDrawing()

		containerDrawing.model.graphUUID = graphView.graph!!.uuid
		containerDrawing.model.name = graphView.graph!!.name.value

		for (circuitInput in graphView.graph!!.graphInputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitInput))))
		}
		for (circuitOutput in graphView.graph!!.graphOutputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitOutput))))
		}

		return containerDrawing
	}
}