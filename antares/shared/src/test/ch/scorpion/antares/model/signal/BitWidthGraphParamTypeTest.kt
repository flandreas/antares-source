package ch.scorpion.antares.model.signal

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.graph.model.GraphParamDefinition
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthGraphParamTypeTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldBeStorable() {
		val param = GraphParamDefinition.create(
			name = "test",
			type = BitWidthGraphParamType,
			defaultValue = BitWidth.BW_4
		)

		val clone = StorableCloner.clone(param)

		assertEquals("test", clone.name)
		assertEquals(BitWidthGraphParamType, clone.type)
		assertEquals(BitWidth.BW_4, clone.defaultValue)
	}
}