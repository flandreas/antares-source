package ch.scorpion.jabbah.graph.model.param

import kotlin.test.Test
import kotlin.test.assertEquals

class GraphParamDefinitionsTest {

	@Test
	fun shouldCopyWithNewDefinition() {
		val orig = GraphParamDefinitions().withDefinition(
			GraphParamDefinition.create("test", StringGraphParamType, "Default"))

		val copy = orig.withDefinition(
			GraphParamDefinition.create("test", StringGraphParamType, "Copy"))

		assertEquals(1, copy.size)
		assertEquals("Copy", copy.get(0).defaultValue)
	}

	@Test
	fun shouldReplaceDefinition() {
		val orig = GraphParamDefinitions().withDefinition(
			GraphParamDefinition.create("test", StringGraphParamType, "Default"))

		val copy = orig.withReplacedDefinition(
			"test",
			GraphParamDefinition.create("changed", StringGraphParamType, "Default"))

		assertEquals(1, copy.size)
		assertEquals("changed", copy.get(0).name)
	}

	@Test
	fun shouldRemoveDefinition() {
		val orig = GraphParamDefinitions().withDefinition(
			GraphParamDefinition.create("test", StringGraphParamType, "Default"))

		val copy = orig.withoutDefinition("test")

		assertEquals(0, copy.size)
	}
}