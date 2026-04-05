package io.antarescircuit.antares

import io.antarescircuit.antares.model.net.PullDirection
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.net.PullResistorView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UndefinedFromSubCircuitIntegrationTest : AbstractJvmCircuitTest() {

	private val library get() = LibraryModule.libraryHolder.library
	private val builder = TestCircuitBuilder("test")
	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private lateinit var ledView: LEDView
	private lateinit var edgeView: EdgeView<DigitalSignal>

	override fun getCircuitView(): GraphView = builder.build()

	override fun setup() {
		super.setup()

		setupLibrary()
		TestLibraryBuilder().addInOutToInOut(library)
		subGraphVV = (library.get(TestLibraryBuilder.INOUT_TO_INOUT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>

		builder.addVerticeView(subGraphVV)
		ledView = builder.addVerticeView(LEDView())
		edgeView = builder.connect(subGraphVV, subGraphVV.model.getOutput("IO2"), ledView)
	}

	@Test
	fun shouldBeUndefinedAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(Bit.Undefined), subGraphVV.model.getOutput<DigitalSignal>("IO2").net!!.signal)
	}

	@Test
	fun shouldEstablishWeakSignalFromOutside() {
		val pullResistorView = builder.addVerticeView(PullResistorView(PullDirection.HIGH))
		builder.split(edgeView, 0, Point2D.ZERO, pullResistorView)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), subGraphVV.model.getOutput<DigitalSignal>("IO2").net!!.signal)
		assertTrue(ledView.model.isOn)
	}
}