package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.model.DocumentType
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.*

class MetaGraphTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldUpdateContainerGraphName() {
		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name = Name("Changed Name")
		assertEquals("Changed Name", metaGraph.containerDrawing.model.name)
	}

	@Test
	fun shouldForwardIdInfoToGraphWhenLoading() {
		val uuid = System.createUUID()
		val name = "Some Name"
		val metaGraph = MetaGraph()
		metaGraph.containerDrawing.model.graphUUID = uuid
		metaGraph.containerDrawing.model.graphName = Name(name)

		val clone = StorableCloner.clone(metaGraph)

		assertEquals(uuid, clone.uuid)
		assertEquals(name, clone.name)
	}

	@Test
	fun shouldDuplicate() {
		val orig = MetaGraph.create(TranslatableText("Original"), GenericGraphType)
		val duplicate = orig.duplicate(TranslatableText("Duplicate"))

		assertNotSame(orig, duplicate)
		assertNotSame(orig.graph, duplicate.graph)
		assertNotSame(orig.containerDrawing, duplicate.containerDrawing)
		assertNotEquals(orig.uuid, duplicate.uuid)
		assertEquals("Duplicate", duplicate.name)
		assertEquals("Original", orig.name)
	}

	@Test
	fun shouldCloneGraphModel() {
		val builder = GraphViewBuilder<Boolean>("Test")
		val vv1 = TestVerticeView("vv1")
		val vv2 = TestVerticeView("vv2")
		builder.addVerticeView(vv1)
		builder.addVerticeView(vv2)
		builder.connect(vv1, vv2)
		val metaGraph = MetaGraph(builder.graphStorable, ContainerDrawing())

		val clonedModel = metaGraph.cloneGraphModel()

		assertNotSame(metaGraph.graph.model, clonedModel)
	}

	@Test
	fun shouldRejectEmptyName() {
		assertFailsWith<IllegalArgumentException>(Translations.getString("library.action.newGraph.emptyName.msg")) {
			MetaGraph.validateName(TranslatableText(""))
		}
	}

	@Test
	fun shouldStoreDocumentation() {
		val metaGraph = MetaGraph.create(TranslatableText("Test"), GenericGraphType)
		metaGraph.documentation = Document(DocumentType.Markdown, "#Title")

		val clone = StorableCloner.clone(metaGraph)

		assertEquals("#Title", clone.documentation!!.text)
	}
}