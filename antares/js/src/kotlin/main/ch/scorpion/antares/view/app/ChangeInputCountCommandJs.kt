package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandJs
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [PortCount] of [AbstractDigitalGateView].
 * Uses [DigitalGraphViewService] for changing the property.
 */
class ChangeInputCountCommandJs(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: PortCount?,
	getter: PropertyGetter<PortCount>,
	private val service: DigitalGraphViewService = EditModule.drawingAppService as DigitalGraphViewService
) : PropertyCommandJs<PortCount>(
	editor,
	PortCount.INPUT_COUNT_BASE_KEY,
	beanProvider,
	beanIds,
	newValue,
	getter,
	{ _, _ -> }
) {
	override fun setValue(value: PortCount?) {
		service.changeInputCount(
			bean as AbstractDigitalGateView<AbstractDigitalGate>,
			value!!,
			editor!!.view as DrawingView<GraphView>)
	}
}