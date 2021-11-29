package ch.scorpion.antares.model.signal

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.model.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.GraphParamTypeRegistry
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthGraphParamTypeTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		GraphParamTypeRegistry.clear()
	}

	@Test
	fun shouldBeStorable() {
		GraphParamTypeRegistry.register(BitWidthGraphParamType.name) { BitWidthGraphParamType }

		val param = GraphParamDefinition.create(
			name = TranslatableText("test"),
			type = BitWidthGraphParamType,
			defaultValue = BitWidth.BW_4
		)

		val clone = StorableCloner.clone(param)

		assertEquals(Name(TranslatableText("test")), clone.name)
		assertEquals(BitWidthGraphParamType, clone.type)
		assertEquals(BitWidth.BW_4, clone.defaultValue)
	}
}