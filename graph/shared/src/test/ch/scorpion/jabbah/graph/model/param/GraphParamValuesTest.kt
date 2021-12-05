package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.graph.model.graph.StringGraphParamType
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphParamValuesTest {

	@Test
	fun shouldCopyWithNewValue() {
		val defs = GraphParamDefinitions().also {
			it.add(GraphParamDefinition.create("test", StringGraphParamType, "Default"))
		}
		var values = GraphParamValues.withDefaults(defs)

		values = values.withValue(GraphParamValue.create("test", StringGraphParamType, "New"))

		assertEquals(1, values.values.size)
		assertEquals("New", values.getValue("test")!!.value)
	}
}