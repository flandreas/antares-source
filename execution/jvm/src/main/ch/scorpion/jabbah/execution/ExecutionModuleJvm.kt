package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.FloatPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.TimedSchedulerTask

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