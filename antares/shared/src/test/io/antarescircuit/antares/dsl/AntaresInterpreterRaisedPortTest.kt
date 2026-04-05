package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.Word
import io.antarescircuit.jabbah.base.dsl.Memory
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