package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.description.Name
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
 * Uses [TestVerticeView] and [TestControlVerticeView] components.
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
	fun buildInnerCustomComponent(inputName: String = "I", outputName: String = "O"): GraphView {
		val comp = addVerticeView(TestVerticeView())
		val control = addVerticeView(TestControlVerticeView())
		connect(addInput(inputName), comp)
		split(connect(comp, addOutput(outputName)), 0, Point2D.ZERO, control)
		graph.name = Name(graphName)
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
		graph.name = Name(graphName)
		return graphView
	}

	fun buildMetaGraph(graphView: GraphView, containerLabel: String? = null): MetaGraph =
		MetaGraph(GraphStorable(graphView), createContainerDrawing(graphView, containerLabel))

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

	private fun createContainerDrawing(graphView: GraphView, containerLabel: String? = null): ContainerDrawing {
		val containerDrawing = GraphViewModule.createContainerDrawing()

		containerDrawing.model.graphUUID = graphView.graph!!.uuid
		containerDrawing.model.graphName = graphView.graph!!.name

		for (circuitInput in graphView.graph!!.graphInputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitInput))))
		}
		for (circuitOutput in graphView.graph!!.graphOutputs) {
			containerDrawing.add(
				portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(circuitOutput))))
		}

		if (containerLabel != null) {
			containerDrawing.add(LabelComponent(containerLabel))
		}

		return containerDrawing
	}
}