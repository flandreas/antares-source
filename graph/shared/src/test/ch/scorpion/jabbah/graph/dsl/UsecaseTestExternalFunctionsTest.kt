package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.graph.AbstractGraphViewExecutionTest
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.TestGraphPortView
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestFailureException
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UsecaseTestExternalFunctionsTest : AbstractGraphViewExecutionTest() {

	private val issueCollector = IssueCollector()
	private lateinit var builder: GraphViewBuilder<Boolean>
	private val appModeHolder = DummyApplicationModeHolder()
	private lateinit var input: TestGraphPortView<Long>
	private lateinit var output: TestGraphPortView<Long>

	override fun setup() {
		super.setup()

		builder= GraphViewBuilder()
		input = builder.addVerticeView(TestGraphPortView(model = GraphInputImpl(name = "I")))
		output = builder.addVerticeView(TestGraphPortView(model = GraphOutputImpl(name = "O")))
		builder.connect(input, output)
	}

	override fun getGraphView(): GraphView = builder.graphView

	@Test
	fun shouldAssertOutput() {
		val usecase = UsecaseImpl("AssertOutput", "setInputAt(10000, \"I\", 42)", "assertOutputAt(20000, \"O\", 42)")
		getGraphView().usecases.add(usecase)
		val runner = UsecaseTestRunner(listOf(usecase), builder.graphView, scheduler, appModeHolder, throwFailureException = true)
		runner.run()
		proceedToNanos(30_000)

		assertEquals(0, issueCollector.size)
	}

	@Test
	fun shouldFailToAssertOutput() {
		val usecase = UsecaseImpl("AssertOutput", "setInputAt(30000, \"I\", 42)", "assertOutputAt(20000, \"O\", 42)")
		getGraphView().usecases.add(usecase)
		val runner = UsecaseTestRunner(listOf(usecase), builder.graphView, scheduler, appModeHolder, throwFailureException = true)
		runner.run()

		assertFailsWith<UsecaseTestFailureException> {
			proceedToNanos(20_000)
			assertEquals(0, issueCollector.size)
		}
	}
}