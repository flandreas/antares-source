package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.script.GraphScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptEngine
import ch.scorpion.jabbah.graph.script.ScriptModule
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

		ScriptModule.scriptGatewayProvider = { GraphScriptGateway(ScriptEngine(BaseModule.eventBus)) }
		ScriptModule.require()
		ExecutionModule.require()

		AppModuleJvm.require()

		GraphViewModuleJvm.require()
		DrawModuleJvm.contextMenuProvider = GraphContextMenuProvider()

		fillProperties(BaseModule.properties)
	}

	@Suppress("UNUSED_PARAMETER")
	private fun fillProperties(properties: Properties) {
		// empty so far
	}
}