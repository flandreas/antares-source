package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [InputCount] of [AbstractDigitalGateView].
 * Uses [DigitalGraphViewService] for changing the property.
 */
class ChangeInputCountCommandSwing(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: InputCount,
	private val service: DigitalGraphViewService = EditModule.drawingAppService as DigitalGraphViewService
) : PropertyCommandSwing<InputCount>(
	editor,
	"element.property.inputCount",
	beanProvider,
	beanIds,
	newValue,
	"chosenInputCount",
	"chosenInputCount"
) {

	/** Cannot undo due to possible unconnect when reducing [InputCount]. */
	override val canUndo: Boolean get() = false

	override fun setValue(value: InputCount?) {
		service.changeInputCount(
			bean as AbstractDigitalGateView<AbstractDigitalGate>,
			value!!,
			editor!!.view as DrawingView<GraphView>)
	}
}