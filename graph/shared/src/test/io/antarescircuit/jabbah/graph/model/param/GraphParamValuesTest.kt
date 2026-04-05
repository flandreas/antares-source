package io.antarescircuit.jabbah.graph.model.param

import kotlin.test.Test
import kotlin.test.assertEquals

class GraphParamValuesTest {

	@Test
	fun shouldCopyWithNewValue() {
		val defs = GraphParamDefinitions().withDefinition(
			GraphParamDefinition.create("test", StringGraphParamType, "Default"))
		var values = GraphParamValues.withDefaults(defs)

		values = values.withValue(GraphParamValue.create("test", StringGraphParamType, "New", null))

		assertEquals(1, values.values.size)
		assertEquals("New", values.getValue("test")!!.value)
	}
}