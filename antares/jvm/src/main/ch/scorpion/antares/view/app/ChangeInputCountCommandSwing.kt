package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [PortCount] of [LogicGateView].
 * Uses [AntaresGraphViewService] for changing the property.
 */
class ChangeInputCountCommandSwing(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
	newValue: PortCount,
	private val service: AntaresGraphViewService = EditModule.drawingAppService as AntaresGraphViewService
) : PropertyCommandSwing<PortCount>(
	editor,
	PortCount.INPUT_COUNT_BASE_KEY,
	beanProvider,
	beanIds,
	newValue,
	"chosenInputCount",
	"chosenInputCount"
) {

	/** Cannot undo due to possible unconnect when reducing [PortCount]. */
	override val canUndo: Boolean get() = false

	override fun setValue(bean: Bean, value: PortCount?) {
		service.changeInputCount(
			bean as LogicGateView,
			value!!,
			editor!!.view as DrawingView<GraphView>)
	}
}