package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.net.WireTap
import ch.scorpion.antares.view.net.WireTapView
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing
import ch.scorpion.jabbah.graph.view.GraphView

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