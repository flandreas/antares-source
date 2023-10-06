package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.graph.AbstractGraphViewExecutionTest
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.TestGraphPortView
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class UsecaseActionExternalFunctionsTest : AbstractGraphViewExecutionTest() {

	private val issueCollector = IssueCollector()
	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	private val appModeHolder = DummyApplicationModeHolder()
	private lateinit var input: TestGraphPortView<Long>

	override fun setup() {
		super.setup()

		input = builder.addVerticeView(TestGraphPortView(model = GraphInputImpl(name = "I", clickValue = 1L)))
		val vv = builder.addVerticeView(TestVerticeView())
		builder.connect(input, vv)
	}

	override fun getGraphView(): GraphView = builder.graphView

	@Test
	fun shouldSetGraphInput() {
		val usecase = UsecaseImpl("SetInput", "setInputAt(10000, \"I\", 42)")
		getGraphView().usecases.add(usecase)
		val runner = UsecaseRunner(usecase, builder.graphView, scheduler, appModeHolder)
		runner.run()
		proceedToNanos(10_001)

		assertEquals(0, issueCollector.size)
		assertEquals(42L, input.model.getOutput<Long>().getOutgoingSignal())
	}

	@Ignore // This doesn't work due to missing View to forward the MouseEvents
	@Test
	fun shouldClickMouse() {
		val usecase = UsecaseImpl("ClickMouse", "clickMouseAt(10000, 10, 10, 2000)")
		getGraphView().usecases.add(usecase)
		val runner = UsecaseRunner(usecase, builder.graphView, scheduler, appModeHolder)
		runner.run()
		proceedToNanos(10_001)

		assertEquals(0, issueCollector.size)
		assertEquals(1L, input.model.getOutput<Long>().getOutgoingSignal())
	}
}