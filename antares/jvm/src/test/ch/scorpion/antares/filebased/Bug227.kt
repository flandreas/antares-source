package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.BidirectionalGraphPort
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression test for GitHub bug issue #227.
 *
 * [BidirectionalGraphPort] refused to send an inside signal to the outside
 * AFTER a defined signal has been send outside->inside AND this signal
 * has been replaced outside by "undefined" in the meantime.
 */
class Bug227 : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var a: Switch
	private lateinit var i: Switch
	private lateinit var d: Switch
	private lateinit var en: Switch
	private lateinit var triStateBuffer: TriStateBufferGate
	private lateinit var subGraphVerticeRef: SubGraphVerticeRef

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("453dfbb1-aa3b-45d6-ab40-f0856fb02767"))

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		triStateBuffer = openedCircuitView.graph!!.withId(2) as TriStateBufferGate
		d = openedCircuitView.graph!!.withId(3) as Switch
		en = openedCircuitView.graph!!.withId(5) as Switch
		a = openedCircuitView.graph!!.withId(12) as Switch
		i = openedCircuitView.graph!!.withId(14) as Switch
		subGraphVerticeRef = openedCircuitView.graph!!.withId(1) as SubGraphVerticeRef
	}

	@AfterTest
	fun cleanup() {
		stopSimulation()
	}

	@Test
	fun shouldSend1ToOutsideAfterZ() {
		d.on(scheduler)
		processUntilQueueIsEmpty()

		en.on(scheduler)
		processUntilQueueIsEmpty()

		en.off(scheduler)
		processUntilQueueIsEmpty()
		assertEquals(Word.of(Bit.Undefined), triStateBuffer.getOutputPort().getOutgoingSignal())

		i.on(scheduler)
		a.on(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(Word.of(true), subGraphVerticeRef.getOutput<DigitalSignal>().net!!.signal)
	}

	@Test
	fun shouldSend1ToOutsideAfter1() {
		// 1 from outside
		d.on(scheduler)
		en.on(scheduler)
		processUntilQueueIsEmpty()

		// 1 from inside
		i.on(scheduler)
		a.on(scheduler)
		processUntilQueueIsEmpty()

		// Z from inside
		a.off(scheduler)
		processUntilQueueIsEmpty()

		// Z from outside
		en.off(scheduler)
		processUntilQueueIsEmpty()

		// 1 from inside
		a.on(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(Word.of(true), subGraphVerticeRef.getOutput<DigitalSignal>().net!!.signal)
	}
}