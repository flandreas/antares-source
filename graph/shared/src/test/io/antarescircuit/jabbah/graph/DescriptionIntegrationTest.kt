package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionIntegrationTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun verticeViewShouldHaveStaticTypeDesc() {
		val vv = TestVerticeView(width = 100)

		assertEquals(TestVertice.TYPE, vv.type)
		assertEquals(TestVertice.TYPE_DESC, vv.typeDesc)
		assertEquals("", vv.description.value)
		assertEquals(
			"*(${TestVertice.TYPE}:) ${TestVertice.TYPE_DESC}.",
			tooltipText(vv))
	}

	@Test
	fun verticeViewShouldHaveCustomDescription() {
		val vv = TestVerticeView(width = 100)

		vv.description = Description("Custom Description")

		assertEquals(TestVertice.TYPE_DESC, vv.typeDesc)
		assertEquals("Custom Description", vv.description.value)
		assertEquals(
			"*(${TestVertice.TYPE}:) Custom Description."
				+ "\n\n"
				+ "${TestVertice.TYPE_DESC}.",
			tooltipText(vv))
	}

	// TODO Should return the type instead of the description!
	@Test
	fun subGraphVerticeViewShouldHaveMetaGraphNameAndDescAsType() {
		val libraryElement = createMetaGraph("Graph Description")

		val subGraphVerticeView = libraryElement.getNewInstance<Vertice>() as SubGraphVerticeView

		assertEquals(TestLibraryBuilder.INNER_CUSTOM_COMP, subGraphVerticeView.type)
		assertEquals("Graph Description", subGraphVerticeView.typeDesc)
		assertEquals("", subGraphVerticeView.description.value)
		assertEquals(
			"*(${TestLibraryBuilder.INNER_CUSTOM_COMP}:) Graph Description.",
			tooltipText(subGraphVerticeView))
	}

	@Test
	fun subGraphVerticeViewShouldHaveCustomDescription() {
		val libraryElement = createMetaGraph("Graph Description")

		val subGraphVerticeView = libraryElement.getNewInstance<Vertice>() as SubGraphVerticeView
		subGraphVerticeView.description = Description("Custom Description")

		assertEquals(
			"*(${TestLibraryBuilder.INNER_CUSTOM_COMP}:) Custom Description."
				+ "\n\n"
				+ "Graph Description.",
			tooltipText(subGraphVerticeView))
	}

	private fun tooltipText(vv: VerticeView<*>): String =
		vv.getTooltip(
			InputEventContext(mock(), x = vv.boundingBox.centerX, y = vv.boundingBox.centerY)
		)!!.text

	private fun createMetaGraph(desc: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library)
		metaGraph.graph.model!!.description = Description(desc)
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, metaGraph, libraryElement)
		return libraryElement
	}
}