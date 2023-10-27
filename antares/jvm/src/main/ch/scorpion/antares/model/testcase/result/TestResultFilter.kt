package ch.scorpion.antares.model.testcase.result

import ch.scorpion.antares.model.testcase.CombinedTestRunResult
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent

enum class TestResultFilter(
	val baseKey: String
) {
	All("antares.testcase.result.filter.all") {
		override fun accept(result: CombinedTestRunResult): Boolean = true
	},

	Failed("antares.testcase.result.filter.failed") {
		override fun accept(result: CombinedTestRunResult): Boolean = result.failed
	},

	Passed("antares.testcase.result.filter.passed") {
		override fun accept(result: CombinedTestRunResult): Boolean = !result.failed
	};

	abstract fun accept(result: CombinedTestRunResult): Boolean
}

class TestResultFilterAction(
	private val panel: TestRunResultsPanel,
	val filter: TestResultFilter
) : AbstractAction(filter.baseKey) {

	override fun execute(event: ActionEvent) {
		panel.applyFilter(filter)
	}
}