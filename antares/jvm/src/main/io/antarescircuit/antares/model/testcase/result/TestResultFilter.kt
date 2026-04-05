package io.antarescircuit.antares.model.testcase.result

import io.antarescircuit.antares.model.testcase.CombinedTestRunResult
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent

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