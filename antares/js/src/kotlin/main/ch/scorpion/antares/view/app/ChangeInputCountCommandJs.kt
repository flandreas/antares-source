package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandJs
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [InputCount] of [AbstractDigitalGateView].
 * Uses [DigitalGraphViewService] for changing the property.
 */
class ChangeInputCountCommandJs(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: InputCount?,
	getter: PropertyGetter<InputCount>,
	private val service: DigitalGraphViewService = EditModule.drawingAppService as DigitalGraphViewService
) : PropertyCommandJs<InputCount>(
	editor,
	"element.property.inputCount",
	beanProvider,
	beanIds,
	newValue,
	getter,
	{ _, _ -> }
) {
	override fun setValue(value: InputCount?) {
		service.changeInputCount(
			bean as AbstractDigitalGateView<AbstractDigitalGate>,
			value!!,
			editor!!.view as DrawingView<GraphView>)
	}
}