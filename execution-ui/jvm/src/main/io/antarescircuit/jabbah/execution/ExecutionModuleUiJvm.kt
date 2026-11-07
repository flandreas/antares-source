package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.module.BaseModuleUiJvm
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.preferences.FloatPreference
import io.antarescircuit.jabbah.base.preferences.IntPreference
import io.antarescircuit.jabbah.base.preferences.PreferenceGroup
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.execution.scheduler.TimedSchedulerTask

object ExecutionModuleUiJvm : AbstractModule() {

	const val PREF_TREE_EXECUTION = "execution.preferences.group.execution"

	override fun initialize() {
		BaseModuleUiJvm.require()
		ExecutionModuleJvm.require()

		buildPreferencesTree(BaseModuleUiJvm.preferencesTree)
	}

	override fun resetDependencies() {
		BaseModuleUiJvm.reset()
		ExecutionModuleJvm.reset()
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_EXECUTION))

		root.getGroup(PREF_TREE_EXECUTION).add(IntPreference(
			id = IssueCollector.PROP_MAX_ISSUES_COUNT,
			nameKey = "execution.preferences.maxIssuesCount",
			minValue = 0,
			maxValue = 1_000))

		root.getGroup(PREF_TREE_EXECUTION).add(FloatPreference(
			id = TimedSchedulerTask.PROP_SLOWDOWN_FACTOR,
			nameKey = "execution.preferences.slowDownFactor",
			minValue = 0.0001f
		))
	}
}