package ch.scorpion.antares

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.io.StorableCloner
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
		val input = subGraphVV.model.getGraph(LibraryModule.libraryHolder).getGraphInput<DigitalSignal>("I") as DigitalCircuitInOut
		val output = subGraphVV.model.getGraph(LibraryModule.libraryHolder).getGraphOutput<DigitalSignal>("O") as DigitalCircuitInOut

		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8))

		assertEquals(BitWidth.BW_8.width, input.bitWidth.width)
		assertEquals(BitWidth.BW_16.width, output.bitWidth.width)
	}

	@Test
	fun shouldUpdatePorts() {
		val inputPortView = subGraphVV.getPortView(subGraphVV.model.getInput())!!
		val outputPortView = subGraphVV.getPortView(subGraphVV.model.getOutput())!!

		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8))

		assertEquals(BitWidth.BW_8.width, (inputPortView.port as DigitalPort).bitWidth.width)
		assertEquals(BitWidth.BW_16.width, (outputPortView.port as DigitalPort).bitWidth.width)
	}

	@Test
	fun shouldApplyParamsWhenLoadingSubGraphVerticeView() {
		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8))

		val usingCircuitBuilder = TestCircuitBuilder("Using", styleProvider, eventBus)
		usingCircuitBuilder.addVerticeView(subGraphVV)

		val cloneGraphStorable = StorableCloner.clone(usingCircuitBuilder.graphStorable)
		val cloneVV = cloneGraphStorable.graphView.getVerticeViews().first() as SubGraphVerticeViewImpl

		val inputPortView = cloneVV.getPortView(cloneVV.model.getInput())!!
		val outputPortView = cloneVV.getPortView(cloneVV.model.getOutput())!!

		assertEquals(BitWidth.BW_8.width, (inputPortView.port as DigitalPort).bitWidth.width)
		assertEquals(BitWidth.BW_16.width, (outputPortView.port as DigitalPort).bitWidth.width)
	}
}