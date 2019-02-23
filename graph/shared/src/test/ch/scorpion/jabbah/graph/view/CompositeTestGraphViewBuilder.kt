package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView

/**
 * Supports building some composite [GraphView]s used for buildings libraries for integration testing.
 * Used [TestVerticeView] and [TestControlVerticeView] components.
 */
class CompositeTestGraphViewBuilder(
	private val graphName: String
) : GraphViewBuilder<Boolean>() {

	/**
	 * Builds a [GraphView] that contains a [TestVerticeView] and a [TestControlVerticeView],
	 * along with two [TestGraphPortView]s for input and output.
	 */
	fun buildInnerCustomComponent(): GraphView<GraphElementView<out GraphElement>> {
		val comp = addVerticeView(TestVerticeView())
		val control = addVerticeView(TestControlVerticeView())
		connect(addInput(), comp)
		split(connect(comp, addOutput()),0, Point2D.ZERO, control)
		graph.name.value = graphName
		return graphView
	}

	/**
	 * Builds a [GraphView] that contains an inner component as built by [buildInnerCustomComponent],
	 * along with two [TestGraphPortView]s for input and output.
	 */
	fun buildOuterCustomComponent(innerComp: SubGraphVerticeView<*>): GraphView<GraphElementView<out GraphElement>> {
		graphView.add(innerComp)
		connect(addInput(), innerComp)
		connect(innerComp, addOutput())
		graph.name.value = graphName
		return graphView
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
}