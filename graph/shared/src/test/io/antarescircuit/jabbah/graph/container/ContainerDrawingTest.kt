package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerDrawingTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldCreateSubGraphVerticeView() {
		val metaGraph = createMetaGraph()
		val vv = metaGraph.containerDrawing.createSubGraphVerticeView(GenericGraphType)

		assertEquals(TestLibraryBuilder.INNER_CUSTOM_COMP, vv.type)
	}

	private fun createMetaGraph(): MetaGraph {
		val library = LibraryModule.libraryHolder.library
		return TestLibraryBuilder().addInnerCustomComponent(library)
	}
}