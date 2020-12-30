package ch.scorpion.antares.view.app

import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

class DigitalGraphViewService(
	copyPasteService: CopyPasteService = EditModule.copyPasteService,
	commandManager: CommandManager = EditModule.commandManager,
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	private val properties: Properties = BaseModule.properties
) : GraphViewAppServiceImpl(copyPasteService, commandManager, connectService) {

	companion object {
		private val LOG by logger(DigitalGraphViewService::class)
	}

	override fun customizeAddedComponent(component: Component) {
		if (component is LightEmitter) {
			component.lightColor = determineLightColor(component.parent as DigitalGraphView)
		}
	}

	fun replaceLightColor(graphView: DigitalGraphView) {
		graphView.defaultLightColor?.let { defaultLightColor ->
			LOG.info("Replace LightColor")
			graphView
				.getDrawables { it is LightEmitter }
				.map { it as LightEmitter }
				.forEach { it.lightColor = defaultLightColor }
		}
	}

	private fun determineLightColor(graphView: DigitalGraphView): LightColor =
		graphView.defaultLightColor ?: LightColor.getSystemDefault(properties)
}