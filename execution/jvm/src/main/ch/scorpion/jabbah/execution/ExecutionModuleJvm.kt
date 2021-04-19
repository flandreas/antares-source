package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.execution.module.ExecutionModule

object ExecutionModuleJvm : AbstractModule() {

	const val PREF_TREE_EXECUTION = "execution.preferences.group.execution"

	override fun initialize() {
		BaseModuleJvm.require()
		ExecutionModule.require()

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_EXECUTION))

		root.getGroup(PREF_TREE_EXECUTION).add(IntPreference(
			id = IssueCollector.PROP_MAX_ISSUES_COUNT,
			nameKey = "execution.preferences.maxIssuesCount",
			minValue = 0,
			maxValue = 1_000))
	}
}