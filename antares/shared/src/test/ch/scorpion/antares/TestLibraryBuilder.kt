package ch.scorpion.antares

import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

/**
 * A test utility class that builds Antares circuits using [TestCircuitBuilder], and adds them to the [Library].
 * TODO Resolve copy/paste from [ch.scorpion.jabbah.graph.TestLibraryBuilder].
 */
class TestLibraryBuilder(
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService
) {

	companion object {
		val NOP = "NOP"
		val OUTER_NOP = "OuterNOP"
		const val CUSTOM_NOT = "CustomNOT"
		const val CUSTOM_NAND = "CustomNAND"
	}

	/** Builds (as of [TestCircuitBuilder.buildCustomNot] a custom NOP and adds it to [libraryDirectory].*/
	fun addNOP(library: Library, propagationDelay: Long = 0): MetaGraph {
		val nop = TestCircuitBuilder(NOP).buildNOP(propagationDelay)
		val metaGraph = MetaGraph(GraphStorable(nop), createContainerDrawing(nop))
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	fun addOuterNOP(library: Library): MetaGraph {
		val outerNOP = TestCircuitBuilder(OUTER_NOP).buildOuterNOP(createSubGraphVerticeView(NOP, library))
		val metaGraph = MetaGraph(GraphStorable(outerNOP), createContainerDrawing(outerNOP))
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	/**
	 * Adds a custom NOT (as of [TestCircuitBuilder.buildCustomNot]) to the specified [LibraryDirectory].
	 * @return the created [MetaGraph] that contains the custom NOT
	 */
	fun addCustomNot(library: Library): MetaGraph {
		val customNOT = TestCircuitBuilder(CUSTOM_NOT).buildCustomNot()
		val containerDrawing = createContainerDrawing(customNOT)
		val metaGraph = MetaGraph(GraphStorable(customNOT), containerDrawing)
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	fun addCustomNand(library: Library): MetaGraph {
		val myNandCircuit = TestCircuitBuilder(CUSTOM_NAND).buildCustomNAND(createSubGraphVerticeView(CUSTOM_NOT, library))
		val containerDrawing = createContainerDrawing(myNandCircuit)
		val metaGraph = MetaGraph(GraphStorable(myNandCircuit), containerDrawing)
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	private fun createSubGraphVerticeView(name: String, libraryDirectory: LibraryDirectory): SubGraphVerticeViewImpl {
		return (libraryDirectory.get(name) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
	}

	private fun createContainerDrawing(circuitView: GraphView<*>): ContainerDrawing {
		val containerDrawing = GraphViewModule.createContainerDrawing(circuitView.graph!!.name.value)

		containerDrawing.model.graphUUID = circuitView.graph!!.uuid

		for (circuitInput in circuitView.graph!!.graphInputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitInput))))
		}
		for (circuitOutput in circuitView.graph!!.graphOutputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitOutput))))
		}

		return containerDrawing
	}
}