package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.execution.ExecutionModuleJvm
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.model.port.InconsistentNetError
import ch.scorpion.jabbah.graph.ui.GraphContextMenuProvider
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm

/**
 * Module definitions for the [ch.scorpion.jabbah.graph] module on the JVM platform.
 */
object GraphModuleJvm : AbstractModule() {

	var containerTreeViewFactory: () -> ContainerTreeView = { ContainerTreeView() }

	override fun initialize() {
		BaseModuleJvm.require()
		DrawModuleJvm.require()
		ExecutionModuleJvm.require()
		AppModuleJvm.require()
		GraphViewModuleJvm.require()

		DrawModuleJvm.contextMenuProvider = GraphContextMenuProvider()

		fillProperties(BaseModule.properties)

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	@Suppress("UNUSED_PARAMETER")
	private fun fillProperties(properties: Properties) {
		// empty so far
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.getGroup(ExecutionModuleJvm.PREF_TREE_EXECUTION).add(IntPreference(
			id = InconsistentNetError.PROP_ALLOWED_DURATION,
			nameKey = "graph.preferences.InconsistentNetError.allowedDuration"
		))
	}
}