package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.Word
import io.antarescircuit.jabbah.base.dsl.Memory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AntaresInterpreterComplexTest : AbstractAntaresInterpreterPortTest() {

	/** See "Register File" circuit in Tanenbaum example project.*/
	@Test
	fun shouldInterpretRegisterFile() {
		val program = """
			init {
				store fcStore
				for (i in 0 to 1) {
					fcStore[i] = 0
				}
			}

			if (ENC and ^CLK) {
				if (FC == 10 or FC <= 4) {
					fcStore[FC] = C
				}
			}

			when (FA) {
				0 : A = fcStore[0]
				1 : A = fcStore[1]
				else : A = 0x?16	
			}

			when (FB) {
				0 : B = fcStore[0]
				1 : B = fcStore[1]
				else : B = 0x?16
			}

		""".trimIndent()

		val parser = AntaresParser(AntaresLexer(program), null)

		context.preset("FA", DigitalSignalFactory.of(BitWidth.BW_4, 0UL))
		context.preset("FB", DigitalSignalFactory.of(BitWidth.BW_4, 0UL))
		context.preset("FC", DigitalSignalFactory.of(BitWidth.BW_4, 0UL))
		context.preset("ENC", DigitalSignalFactory.of(BitWidth.BW_1, 0UL))
		context.preset("CLK", DigitalSignalFactory.of(BitWidth.BW_1, 0UL))
		context.preset("A", DigitalSignalFactory.of(BitWidth.BW_16, 0UL))
		context.preset("B", DigitalSignalFactory.of(BitWidth.BW_16, 0UL))
		context.preset("C", DigitalSignalFactory.of(BitWidth.BW_16, 0UL))

		val memory = Memory(context)
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		interpreter.executionStarted()
		val fcStore = memory.getValue(variable("fcStore"))
		assertIs<HashMap<*,*>>(fcStore)
		assertEquals(2, fcStore.keys.size)
		@Suppress("UNCHECKED_CAST")
		assertEquals(0L, (fcStore as Map<Long, Long>)[0])
		assertEquals(0L, (fcStore)[1])

		setFA(0UL, interpreter)
		setFB(0UL, interpreter)
		setFC(0UL, interpreter)

		setC(17UL, interpreter)
		setENC(1UL, interpreter)
		setClk(1UL, interpreter)

		assertEquals(Word.of(BitWidth.BW_16, 17UL), memory.getValue(variable("A")))
	}

	private fun setC(value: ULong, interpreter: AntaresInterpreter) {
		setInput("C", Word.of(BitWidth.BW_16, value), interpreter)
	}

	private fun setClk(value: ULong, interpreter: AntaresInterpreter) {
		setInput("CLK", Word.of(BitWidth.BW_1, value), interpreter)
	}

	private fun setENC(value: ULong, interpreter: AntaresInterpreter) {
		setInput("ENC", Word.of(BitWidth.BW_1, value), interpreter)
	}

	private fun setFA(value: ULong, interpreter: AntaresInterpreter) {
		setInput("FA", Word.of(BitWidth.BW_4, value), interpreter)
	}

	private fun setFB(value: ULong, interpreter: AntaresInterpreter) {
		setInput("FB", Word.of(BitWidth.BW_4, value), interpreter)
	}

	private fun setFC(value: ULong, interpreter: AntaresInterpreter) {
		setInput("FC", Word.of(BitWidth.BW_4, value), interpreter)
	}
}