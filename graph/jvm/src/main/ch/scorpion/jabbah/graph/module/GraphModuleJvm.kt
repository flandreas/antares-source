package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.script.GraphScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptEngineJvm
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm

/**
 * Module definitions for the [ch.scorpion.jabbah.graph] module on the JVM platform.
 */
object GraphModuleJvm : AbstractModule() {

    var graphNavigationPanelFactory: GraphNavigationPanelFactory = StandardGraphNavigationPanelFactory()

    var containerTreeViewFactory : () -> ContainerTreeView = { ContainerTreeView() }

    override fun initialize() {
        BaseModuleJvm.require()

	    DrawModuleJvm.require()

	    ScriptModule.scriptEngineProvider = { ScriptEngineJvm() }
        ScriptModule.scriptGatewayProvider = { GraphScriptGateway(ScriptModule.scriptEngineProvider.invoke()) }
        ScriptModule.require()
        ExecutionModule.require()

        EditModuleJvm.require()

        GraphViewModuleJvm.require()
	    DrawModuleJvm.contextMenuProvider = GraphContextMenuProvider()

	    fillProperties(BaseModule.properties)
    }

    private fun fillProperties(properties: Properties) {
		properties.set(GraphNavigationPanel.PROP_OVERLAY_COLOR, Color(255, 255, 255, 192))
    }
}