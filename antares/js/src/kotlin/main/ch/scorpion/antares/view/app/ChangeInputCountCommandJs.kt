package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.view.gate.AbstractLogicGateView
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandJs
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [PortCount] of [AbstractDigitalGateView].
 * Uses [AntaresGraphViewService] for changing the property.
 */
class ChangeInputCountCommandJs(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
	newValue: PortCount?,
	getter: PropertyGetter<PortCount>,
	private val service: AntaresGraphViewService = EditModule.drawingAppService as AntaresGraphViewService
) : PropertyCommandJs<PortCount>(
	editor,
	PortCount.INPUT_COUNT_BASE_KEY,
	beanProvider,
	beanIds,
	newValue,
	getter,
	{ _, _ -> }
) {
	override fun setValue(bean: Bean, value: PortCount?) {
		service.changeInputCount(
			bean as AbstractLogicGateView<AbstractLogicGate>,
			value!!,
			editor!!.view as DrawingView<GraphView>)
	}
}