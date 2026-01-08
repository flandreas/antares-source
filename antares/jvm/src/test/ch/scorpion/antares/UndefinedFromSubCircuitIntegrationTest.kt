package ch.scorpion.antares

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.net.PullResistorView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
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