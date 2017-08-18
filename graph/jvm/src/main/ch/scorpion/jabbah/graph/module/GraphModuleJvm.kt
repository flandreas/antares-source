package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.script.GraphScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptEngineJvm
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.ui.GraphNavigationPanelFactory
import ch.scorpion.jabbah.graph.ui.StandardGraphNavigationPanelFactory
import ch.scorpion.jabbah.graph.view.GraphTextComponent
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.graph] module on the JVM platform.
 */
object GraphModuleJvm : AbstractModule() {

    var graphNavigationPanelFactory: GraphNavigationPanelFactory = StandardGraphNavigationPanelFactory()

    var containerTreeViewFactory : () -> ContainerTreeView = { ContainerTreeView() }

    override fun initialize() {
        BaseModuleJvm.require()

        ScriptModule.scriptEngineProvider = { ScriptEngineJvm() }
        ScriptModule.scriptGatewayProvider = { GraphScriptGateway(ScriptModule.scriptEngineProvider.invoke()) }
        ScriptModule.require()
        ExecutionModule.require()
        EditModuleJvm.require()
        GraphViewModuleJvm.require()

        registerTypes(IOModule.typeMap)
    }

    private fun registerTypes(typeMap: TypeMap) {
        typeMap.register("graphTextComponent", GraphTextComponent::class)
    }
}