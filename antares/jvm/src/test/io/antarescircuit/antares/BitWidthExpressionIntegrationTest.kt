package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthExpressionIntegrationTest : AbstractJvmCircuitTest() {

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
		val input = subGraphVV.model.getGraph().getGraphInput<DigitalSignal>("I") as DigitalCircuitInOut
		val output = subGraphVV.model.getGraph().getGraphOutput<DigitalSignal>("O") as DigitalCircuitInOut

		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8, null))

		assertEquals(BitWidth.BW_8.width, input.bitWidth.width)
		assertEquals(BitWidth.BW_16.width, output.bitWidth.width)
	}

	@Test
	fun shouldUpdatePorts() {
		val inputPortView = subGraphVV.getPortView(subGraphVV.model.getInput())!!
		val outputPortView = subGraphVV.getPortView(subGraphVV.model.getOutput())!!

		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8, null))

		assertEquals(BitWidth.BW_8.width, (inputPortView.port as DigitalPort).bitWidth.width)
		assertEquals(BitWidth.BW_16.width, (outputPortView.port as DigitalPort).bitWidth.width)
	}

	@Test
	fun shouldApplyParamsWhenLoadingSubGraphVerticeView() {
		subGraphVV.model.setParamValue(GraphParamValue.create("BW", BitWidthGraphParamType, BitWidth.BW_8, null))

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