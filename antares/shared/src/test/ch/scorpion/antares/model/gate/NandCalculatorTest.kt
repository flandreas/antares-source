package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [AndCalculator]. */
class NandCalculatorTest : AbstractGateCalculatorTest(NandCalculator()) {

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertTwoInput(False, False, True)
		assertTwoInput(False, True, True)
		assertTwoInput(False, Undefined, True)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, True)
		assertTwoInput(True, True, False)
		assertTwoInput(True, Undefined, True)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, True)
		assertTwoInput(Undefined, True, True)
		assertTwoInput(Undefined, Undefined, True)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val nandGate = NandGate(bitWidth = BitWidth.BW_2)
		nandGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(True, False)), signalHandler)
		nandGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(True, True)), signalHandler)

		val result = calculator.calculateMultiBit(nandGate)

		assertEquals(Word(listOf(False, True)), result)
	}
}