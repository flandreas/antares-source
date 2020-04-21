package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView

/**
 * Supports building some composite [GraphView]s used for buildings libraries for integration testing.
 * Used [TestVerticeView] and [TestControlVerticeView] components.
 */
class CompositeTestGraphViewBuilder(
	private val graphName: String,
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory
) : GraphViewBuilder<Boolean>() {

	/**
	 * Builds a [GraphView] that contains a [TestVerticeView] and a [TestControlVerticeView],
	 * along with two [TestGraphPortView]s for input and output.
	 */
	fun buildInnerCustomComponent(): GraphView {
		val comp = addVerticeView(TestVerticeView())
		val control = addVerticeView(TestControlVerticeView())
		connect(addInput(), comp)
		split(connect(comp, addOutput()), 0, Point2D.ZERO, control)
		graph.name.value = graphName
		return graphView
	}

	/**
	 * Builds a [GraphView] that contains an inner component as built by [buildInnerCustomComponent],
	 * along with two [TestGraphPortView]s for input and output.
	 */
	fun buildOuterCustomComponent(innerComp: SubGraphVerticeView<*>): GraphView {
		graphView.add(innerComp)
		connect(addInput(), innerComp)
		connect(innerComp, addOutput())
		graph.name.value = graphName
		return graphView
	}

	fun buildMetaGraph(graphView: GraphView): MetaGraph {
		return MetaGraph(GraphStorable(graphView), createContainerDrawing(graphView))
	}

	private fun addInput(name: String = "I"): TestGraphPortView {
		val input = TestGraphPortView.input(name)
		graphView.add(input)
		return input
	}

	private fun addOutput(name: String = "O"): TestGraphPortView {
		val output = TestGraphPortView.output(name)
		graphView.add(output)
		return output
	}

	private fun createContainerDrawing(graphView: GraphView): ContainerDrawing {
		val containerDrawing = GraphViewModule.createContainerDrawing()

		containerDrawing.model.graphUUID = graphView.graph!!.uuid
		containerDrawing.model.graphName.translation = graphView.graph!!.name.translation

		for (circuitInput in graphView.graph!!.graphInputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitInput))))
		}
		for (circuitOutput in graphView.graph!!.graphOutputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitOutput))))
		}

		return containerDrawing
	}
}