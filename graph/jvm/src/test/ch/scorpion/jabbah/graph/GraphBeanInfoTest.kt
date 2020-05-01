package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ContainerDrawingBeanInfo
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.graph.GraphViewImplBeanInfo
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImplBeanInfo
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewBeanInfo
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImplBeanInfo
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImplBeanInfo
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImplBeanInfo
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImplBeanInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class GraphBeanInfoTest {

	companion object {
		init {
			GraphUITestRule.configure()
		}
	}

	private val editor = mockk<Editor>()

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