package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.net.WireTap
import ch.scorpion.antares.view.net.WireTapView
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Command] for changing [PortCount] of [WireTap].
 * Uses [DigitalGraphViewService] for changing the property.
 */
class ChangeOutputCountCommandSwing(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: PortCount,
	getterPropertyName: String = "chosenOutputCount",
	setterPropertyName: String = "chosenOutputCount",
	private val service: DigitalGraphViewService = EditModule.drawingAppService as DigitalGraphViewService
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

	override fun setValue(value: PortCount?) {
		service.changeOutputCount(
			bean as WireTapView,
			value!!,
			editor!!.view as DrawingView<GraphView>
		)
	}
}