package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.ContainerDrawingBeanInfo
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImplBeanInfo
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImpl
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImplBeanInfo
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeViewBeanInfo
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImplBeanInfo
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImplBeanInfo
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImplBeanInfo
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImplBeanInfo
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import org.junit.Test

class GraphBeanInfoTest {

	private val view: DrawingView<GraphElementView<*>, GraphView>
	private val editor = mock<Editor>()

	init {
		GraphUITestRule.configure()
		view = DrawingViewMockBuilder().build()
		every { editor.active } returns true

		@Suppress("UNCHECKED_CAST")
		every { editor.view } returns view as DrawingView<Component, Drawing<Component>>
	}

	private fun <T: Bean> read(bean: T, beanInfo: AbstractBeanInfo<T>) {
		beanInfo
			.getProperties(bean, editor)
			.forEach { it.readFromObject(bean) }
	}

	@Test
	fun shouldReadGraphViewImpl() {
		read(GraphViewImpl(), GraphViewImplBeanInfo())
	}

	@Test
	fun shouldReadContainerDrawing() {
		read(ContainerDrawing(), ContainerDrawingBeanInfo())
	}

	@Test
	fun shouldReadEdgeView() {
		val graphView = GraphViewImpl()
		val component = EdgeViewImpl<Any>()
		graphView.add(component)

		read(component, EdgeViewImplBeanInfo())
	}

	@Test
	fun shouldReadOscilloscopeView() {
		read(OscilloscopeView(), OscilloscopeViewBeanInfo())
	}

	@Test
	fun shouldReadScenario() {
		val graphView = GraphViewImpl()
		val scenario = ScenarioImpl()

		graphView.scenarios.add(scenario)

		read(scenario, ScenarioImplBeanInfo())
	}

	@Test
	fun shouldReadScenarioStep() {
		val graphView = GraphViewImpl()
		val scenario = ScenarioImpl()
		val scenarioStep = ScenarioStepImpl()

		@Suppress("UNCHECKED_CAST")
		every { editor.drawing } returns graphView as Drawing<Component>

		graphView.scenarios.add(scenario)
		scenario.addStep(scenarioStep)

		read(scenarioStep, ScenarioStepImplBeanInfo())
	}

	@Test
	fun shouldReadUsecase() {
		read(UsecaseImpl(), UsecaseImplBeanInfo())
	}

	@Test
	fun shouldReadSubGraphVerticeView() {
		read(SubGraphVerticeViewImpl(), SubGraphVerticeViewImplBeanInfo())
	}

}