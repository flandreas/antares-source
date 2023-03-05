package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.MemoryLibraryPersistenceService
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.graph.StringGraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamTypeRegistry
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeRefParamTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryService = LibraryService()
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