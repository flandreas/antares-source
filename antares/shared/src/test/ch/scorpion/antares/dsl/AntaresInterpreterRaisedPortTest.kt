package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.graph.model.InputPort
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresInterpreterRaisedPortTest : AbstractAntaresInterpreterPortTest() {

	@Test
	fun shouldRaisePositivePort() {
		val parser = AntaresParser(AntaresLexer("""
			if (^C) {
				O = D
			}
		""".trimIndent()), null)

		context.preset("C", DigitalSignalFactory.of(BitWidth.BW_1, 0UL))
		context.preset("D", DigitalSignalFactory.of(BitWidth.BW_4, 1UL))
		context.preset("O", DigitalSignalFactory.of(BitWidth.BW_4, 0UL))

		val memory = Memory(context)
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		interpreter.executionStarted()

		setInput(createPort("C", Logic.POSITIVE), Word.of(BitWidth.BW_1, 1UL), interpreter)

		assertEquals(Word.of(BitWidth.BW_4, 1UL), memory.getValue(variable("O")))
	}

	@Test
	fun shouldRaiseNegativePort() {
		val parser = AntaresParser(AntaresLexer("""
			if (^C) {
				O = D
			}
		""".trimIndent()), null)

		context.preset("C", DigitalSignalFactory.of(BitWidth.BW_1, 1UL))
		context.preset("D", DigitalSignalFactory.of(BitWidth.BW_4, 1UL))
		context.preset("O", DigitalSignalFactory.of(BitWidth.BW_4, 0UL))

		val memory = Memory(context)
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		interpreter.executionStarted()

		setInput(createPort("C", Logic.NEGATIVE), Word.of(BitWidth.BW_1, 0UL), interpreter)

		assertEquals(Word.of(BitWidth.BW_4, 1UL), memory.getValue(variable("O")))
	}
}