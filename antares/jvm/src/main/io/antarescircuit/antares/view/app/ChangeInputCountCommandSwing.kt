package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.properties.PropertyCommandSwing
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

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
			editor!!.view as DrawingView<GraphElementView<*>, GraphView>)
	}
}