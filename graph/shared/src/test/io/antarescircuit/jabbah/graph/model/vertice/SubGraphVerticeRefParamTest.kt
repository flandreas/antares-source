package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.param.StringGraphParamType
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.model.param.GraphParamTypeRegistry
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeRefParamTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		GraphParamTypeRegistry.register(StringGraphParamType.name) { StringGraphParamType }
	}

	@Test
	fun shouldSetDefaultGraphParamValues() {
		val paramDef = createParamDef()
		val metaGraph = createLibraryMetaGraph(paramDef)

		val subGraphVerticeRef = SubGraphVerticeRef.fromSubGraphVertice(GenericGraphType, metaGraph.containerDrawing.createSubGraphVertice(), LibraryModule.libraryHolder)

		assertEquals("Sepp", subGraphVerticeRef.paramValues.getValue("test")?.value)
	}

	private fun createParamDef(): GraphParamDefinition<String> {
		return GraphParamDefinition.create(
			name = "test",
			type = StringGraphParamType,
			defaultValue = "Sepp")
	}

	private fun createLibraryMetaGraph(paramDef: GraphParamDefinition<String>): MetaGraph {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library)
		metaGraph.graph.model?.let {
			it.parameterDefinitions = it.parameterDefinitions.withDefinition(paramDef)
		}
		return metaGraph
	}
}