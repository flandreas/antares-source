package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphPortView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame

class MetaGraphTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
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
	fun shouldUpdateContainerGraphPortName() {
		val graphView = GraphViewImpl()
		val graphPortView = TestGraphPortView(model = GraphInputImpl(name = "A"))
		val metaGraph = MetaGraph(GraphStorable(graphView), ContainerDrawing())
		graphView.add(graphPortView)
		metaGraph.containerDrawing.add(createPortViewComponent(graphPortView.model))

		graphPortView.model.name = "B"

		assertEquals("B", metaGraph.containerDrawing.model.getPort<Boolean>().name)
	}

	@Test
	fun shouldNotUpdateForeignContainerGraphPortName() {
		val graphView = GraphViewImpl()
		val graphPortView = TestGraphPortView(model = GraphInputImpl(name = "A"))
		val metaGraph = MetaGraph(GraphStorable(graphView), ContainerDrawing())
		graphView.add(graphPortView)
		metaGraph.containerDrawing.add(createPortViewComponent(graphPortView.model))

		val graphView2 = GraphViewImpl()
		val graphPortView2 = TestGraphPortView(model = GraphInputImpl(name = "A"))
		val metaGraph2 = MetaGraph(GraphStorable(graphView2), ContainerDrawing())
		graphView2.add(graphPortView2)
		metaGraph2.containerDrawing.add(createPortViewComponent(graphPortView.model))

		graphPortView.model.name = "B"

		assertEquals("B", metaGraph.containerDrawing.model.getPort<Boolean>().name)
		assertEquals("A", metaGraph2.containerDrawing.model.getPort<Boolean>().name)
	}

	@Test
	fun shouldUpdateContainerGraphPortType() {
		val graphView = GraphViewImpl()
		val graphPortBuilder = GraphPortMockBuilder<Boolean>().withPortType(PortType.INOUT)
		val graphPortView = TestGraphPortView(model = graphPortBuilder.build())
		val metaGraph = MetaGraph(GraphStorable(graphView), ContainerDrawing())
		graphView.add(graphPortView)
		metaGraph.containerDrawing.add(createPortViewComponent(graphPortView.model))

		graphPortBuilder.graphPort.portType = PortType.OUTPUT

		assertEquals(PortType.OUTPUT, metaGraph.containerDrawing.model.getPort<Boolean>().portType)
	}

	@Test
	fun shouldDuplicate() {
		val orig = MetaGraph.withName("Original")
		val duplicate = orig.duplicate(TranslatableText("Duplicate"))

		assertNotSame(orig, duplicate)
		assertNotSame(orig.graph, duplicate.graph)
		assertNotSame(orig.containerDrawing, duplicate.containerDrawing)
		assertNotEquals(orig.uuid, duplicate.uuid)
		assertEquals("Duplicate", duplicate.name)
		assertEquals("Original", orig.name)
	}

	private fun createPortViewComponent(graphPort: GraphPort<*>): PortViewComponent<*> {
		return GraphViewModule.portViewFactory.createPortViewComponent(
			GraphViewModule.portViewFactory.createPortView(
				GraphModelModule.portFactory.createSubGraphPort(graphPort)))
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

		val clonedModel = metaGraph.cloneGraphModel(IOModule.storableCreator)

		assertNotSame(metaGraph.graph.model, clonedModel)
	}
}