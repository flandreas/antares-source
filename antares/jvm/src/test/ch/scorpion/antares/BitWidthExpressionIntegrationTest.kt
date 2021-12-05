package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.io.IOModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthExpressionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val library get() = LibraryModule.libraryHolder.library
	private lateinit var subGraphVV: SubGraphVerticeViewImpl

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()
		TestLibraryBuilder().addBitWidthExpressionInputOutput(library, "BW", "BW * 2")

		subGraphVV = (library.get(TestLibraryBuilder.BIT_WITH_EXPRESSION) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeViewImpl

		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		builder.addVerticeView(subGraphVV)
		circuitView = builder.build()
	}

	@Test
	fun shouldUpdateCircuit() {
		val input = subGraphVV.model.getGraph(GraphModule.metaGraphRepository, IOModule.storableCreator).getGraphInput<DigitalSignal>("I") as CircuitInOut
		val output = subGraphVV.model.getGraph(GraphModule.metaGraphRepository, IOModule.storableCreator).getGraphOutput<DigitalSignal>("O") as CircuitInOut

		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8))

		assertEquals(BitWidth.BW_8.width, input.bitWidth.width)
		assertEquals(BitWidth.BW_16.width, output.bitWidth.width)
	}
}