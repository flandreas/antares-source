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
		const val NOP = "NOP"
		const val OUTER_NOP = "OuterNOP"
		const val CUSTOM_NOT = "CustomNOT"
		const val CUSTOM_NAND = "CustomNAND"
		const val INOUT_TO_OUT = "InOutToOut"
		const val INOUT_TO_INOUT = "InOutToInOut"
		const val BINARY_FUNCTION = "BinaryFunction"
		const val BIT_WITH_EXPRESSION = "BitWidthExpression"
	}

	fun addGraphView(graphView: GraphView, library: Library): MetaGraph {
		val metaGraph = MetaGraph(GraphStorable(graphView), createContainerDrawing(graphView))
		libraryService.addContainerLibraryElement(library, metaGraph, library)
		return metaGraph
	}

	/** Builds (as of [TestCircuitBuilder.buildNOP] a custom NOP and adds it to [libraryDirectory].*/
	fun addNOP(library: Library, propagationDelay: Long = 0): MetaGraph {
		val nop = TestCircuitBuilder(NOP).buildNOP(propagationDelay)
		return addGraphView(nop, library)
	}

	fun addOuterNOP(library: Library): MetaGraph {
		val outerNOP = TestCircuitBuilder(OUTER_NOP).buildOuterNOP(createSubGraphVerticeView(NOP, library))
		return addGraphView(outerNOP, library)
	}

	/**
	 * Adds a custom NOT (as of [TestCircuitBuilder.buildCustomNot]) to the specified [LibraryDirectory].
	 * @return the created [MetaGraph] that contains the custom NOT
	 */
	fun addCustomNot(library: Library): MetaGraph {
		val customNOT = TestCircuitBuilder(CUSTOM_NOT).buildCustomNot()
		return addGraphView(customNOT, library)
	}

	fun addCustomNand(library: Library): MetaGraph {
		val myNandCircuit = TestCircuitBuilder(CUSTOM_NAND).buildCustomNAND(createSubGraphVerticeView(CUSTOM_NOT, library))
		return addGraphView(myNandCircuit, library)
	}

	fun addInOutToOut(library: Library): MetaGraph {
		val inOutToOut = TestCircuitBuilder(INOUT_TO_OUT).buildInOutToOut()
		return addGraphView(inOutToOut, library)
	}

	fun addInOutToInOut(library: Library): MetaGraph {
		val inOutToInOut = TestCircuitBuilder(INOUT_TO_INOUT).buildInOutToInOut()
		return addGraphView(inOutToInOut, library)
	}

	fun addScriptedBinaryFunction(library: Library, input1Name: String, input2Name: String, outputName: String, script: String): MetaGraph {
		val binaryFunction = TestCircuitBuilder(BINARY_FUNCTION).buildScriptedBinaryFunction(input1Name, input2Name, outputName, script)
		return addGraphView(binaryFunction, library)
	}

	fun addBitWidthExpressionInputOutput(library: Library, inputExpression: String, outputExpression: String, graphScript: String? = null): MetaGraph {
		val graphView = TestCircuitBuilder(BIT_WITH_EXPRESSION).buildBitWidthExpressionInputOutput(inputExpression, outputExpression, graphScript)
		return addGraphView(graphView, library)
	}

	private fun createSubGraphVerticeView(name: String, libraryDirectory: LibraryDirectory): SubGraphVerticeViewImpl {
		return (libraryDirectory.get(name) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
	}

	private fun createContainerDrawing(circuitView: GraphView): ContainerDrawing {
		val containerDrawing = GraphViewModule.createContainerDrawing(circuitView.graph!!.name.value)

		containerDrawing.model.graphUUID = circuitView.graph!!.uuid

		for (circuitInput in circuitView.graph!!.graphInputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitInput, circuitView.graph!!.type))))
		}
		for (circuitOutput in circuitView.graph!!.graphOutputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitOutput, circuitView.graph!!.type))))
		}
		for (circuitInOut in circuitView.graph!!.graphInOuts) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitInOut, circuitView.graph!!.type))))
		}

		return containerDrawing
	}
}