package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.net.WireTap
import io.antarescircuit.antares.view.net.WireTapView
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.properties.PropertyCommandSwing
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [PortCount] of [WireTap].
 * Uses [AntaresGraphViewService] for changing the property.
 */
class ChangeOutputCountCommandSwing(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
	newValue: PortCount,
	getterPropertyName: String = "chosenOutputCount",
	setterPropertyName: String = "chosenOutputCount",
	private val service: AntaresGraphViewService = EditModule.drawingAppService as AntaresGraphViewService
) : PropertyCommandSwing<PortCount>(
	editor,
	PortCount.INPUT_COUNT_BASE_KEY,
	beanProvider,
	beanIds,
	newValue,
	getterPropertyName,
	setterPropertyName
) {

	/** Cannot undo due to possible unconnect when reducing [PortCount]. */
	override val canUndo: Boolean get() = false

	override fun setValue(bean: Bean, value: PortCount?) {
		service.changeOutputCount(
			bean as WireTapView,
			value!!,
			editor!!.view as DrawingView<GraphView>
		)
	}
}