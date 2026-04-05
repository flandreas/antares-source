package io.antarescircuit.antares.model.signal

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthGraphParamTypeTest {

	init {
		AntaresTestRule.configure()
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