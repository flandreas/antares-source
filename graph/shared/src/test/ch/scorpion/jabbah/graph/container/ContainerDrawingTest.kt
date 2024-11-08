package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.MemoryLibraryPersistenceService
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerDrawingTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
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