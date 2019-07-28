package ch.scorpion.antares.view.app

import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.app.GraphViewServiceImpl
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

class DigitalGraphViewService(
	commandManager: CommandManager = EditModule.commandManager,
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	private val properties: Properties = BaseModule.properties
) : GraphViewServiceImpl(commandManager, connectService) {

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>) {
		if (component is LightEmitter) {
			component.lightColor = determineLightColor(drawingView.drawing as DigitalGraphView<*>)
		}
		super.add(component, drawingView)
	}

	private fun determineLightColor(graphView: DigitalGraphView<*>): LightColor {
		return graphView.defaultLightColor ?: LightColor.withName(properties.getString(LightColor.PROP_DEFAULT_LIGHT_COLOR))
	}
}