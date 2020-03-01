package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DescriptionIntegrationTest {

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
		GraphModelModule.portFactory = TestPortFactory()
		GraphViewModule.portViewFactory = TestPortViewFactory()
	}

	@Test
	fun verticeViewShouldHaveStaticTypeDesc() {
		val vv = TestVerticeView(width = 100)

		assertEquals(TestVertice.TYPE, vv.type)
		assertEquals(TestVertice.TYPE_DESC, vv.typeDesc)
		assertNull(vv.description.value)
		assertEquals(
			"<html><strong>${TestVertice.TYPE}:&nbsp;</strong>${TestVertice.TYPE_DESC}.</html>",
			tooltipText(vv))
	}

	@Test
	fun verticeViewShouldHaveCustomDescription() {
		val vv = TestVerticeView(width = 100)

		vv.description.value = "Custom Description"

		assertEquals(TestVertice.TYPE_DESC, vv.typeDesc)
		assertEquals("Custom Description", vv.description.value)
		assertEquals(
			"<html><strong>${TestVertice.TYPE}:&nbsp;</strong>${TestVertice.TYPE_DESC}."
				+ "<br><br>"
				+ "Custom Description.</html>",
			tooltipText(vv))
	}

	// TODO Should return the type instead of the description!
	@Test
	fun subGraphVerticeViewShouldHaveMetaGraphNameAndDescAsType() {
		val libraryElement = createMetaGraph("Graph Description")

		val subGraphVerticeView = libraryElement.getNewInstance<Vertice>() as SubGraphVerticeView

		assertEquals(TestLibraryBuilder.INNER_CUSTOM_COMP, subGraphVerticeView.type)
		assertEquals("Graph Description", subGraphVerticeView.typeDesc)
		assertNull(subGraphVerticeView.description.value)
		assertEquals(
			"<html><strong>${TestLibraryBuilder.INNER_CUSTOM_COMP}:&nbsp;</strong>Graph Description.</html>",
			tooltipText(subGraphVerticeView))
	}

	@Test
	fun aubGraphVerticeViewShouldHaveCustomDescription() {
		val libraryElement = createMetaGraph("Graph Description")

		val subGraphVerticeView = libraryElement.getNewInstance<Vertice>() as SubGraphVerticeView
		subGraphVerticeView.description.value = "Custom Description"

		assertEquals(
			"<html><strong>${TestLibraryBuilder.INNER_CUSTOM_COMP}:&nbsp;</strong>Graph Description."
				+ "<br><br>"
				+ "Custom Description.</html>",
			tooltipText(subGraphVerticeView))
	}

	private fun tooltipText(vv: VerticeView<*>): String =
		vv.getTooltip(vv.boundingBox.centerX, vv.boundingBox.centerY)!!.text

	private fun createMetaGraph(desc: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library)
		metaGraph.graph.model!!.description.value = desc
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, libraryElement)
		return libraryElement
	}

}