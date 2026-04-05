package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.preferences.FloatPreference
import io.antarescircuit.jabbah.base.preferences.IntPreference
import io.antarescircuit.jabbah.base.preferences.PreferenceGroup
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.execution.module.ExecutionModule
import io.antarescircuit.jabbah.execution.scheduler.TimedSchedulerTask

object ExecutionModuleJvm : AbstractModule() {

	const val PREF_TREE_EXECUTION = "execution.preferences.group.execution"

	override fun initialize() {
		BaseModuleJvm.require()
		ExecutionModule.require()

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	override fun resetDependencies() {
		BaseModuleJvm.reset()
		ExecutionModule.reset()
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