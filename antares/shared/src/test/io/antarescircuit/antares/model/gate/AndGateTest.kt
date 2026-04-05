package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import kotlin.test.Test
import kotlin.test.assertEquals

class AndGateTest {

	@Test
	fun shouldCalculateTruthTableWithOneInvertedInput() {
		val andGate = NonUnaryLogicGate.andGate()
		(andGate.getInput<DigitalSignal>(1) as DigitalPort).logic = Logic.NEGATIVE
		val truthTable = andGate.calculateTruthTable()

		assertEquals(0, truthTable.outputOf(intArrayOf(0, 0))[0])
		assertEquals(1, truthTable.outputOf(intArrayOf(0, 1))[0])
		assertEquals(0, truthTable.outputOf(intArrayOf(1, 0))[0])
		assertEquals(0, truthTable.outputOf(intArrayOf(1, 1))[0])
	}

	@Test
	fun shouldCalculateTruthTableWithTwoInvertedInput() {
		val andGate = NonUnaryLogicGate.andGate()
		(andGate.getInput<DigitalSignal>(1) as DigitalPort).logic = Logic.NEGATIVE
		(andGate.getInput<DigitalSignal>(2) as DigitalPort).logic = Logic.NEGATIVE
		val truthTable = andGate.calculateTruthTable()

		assertEquals(1, truthTable.outputOf(intArrayOf(0, 0))[0])
		assertEquals(0, truthTable.outputOf(intArrayOf(0, 1))[0])
		assertEquals(0, truthTable.outputOf(intArrayOf(1, 0))[0])
		assertEquals(0, truthTable.outputOf(intArrayOf(1, 1))[0])
	}
}