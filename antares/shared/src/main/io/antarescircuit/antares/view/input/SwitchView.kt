package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.PredefinedColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

/**
 * A view representation of a [Switch] that supports persistent toggling between two states.
 */
class SwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Switch = Switch(),
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractPushButtonSwitchView<Switch>(styleProvider, model),
	ControlViewSource<Switch>
{

	companion object {

		const val BASE_KEY_TOGGLE = "element.property.Switch.toggle"
		const val MIN_ON_TIME = "element.property.Switch.minOnTime"
		const val CLOSED_ON_START = "element.property.Switch.closedOnStart"

		private const val TOGGLE_BASE_RESOURCE_KEY = "library.element.Toggle"
		private val TOGGLE_TYPE get() = Translations.getString("$TOGGLE_BASE_RESOURCE_KEY.name")
		private val TOGGLE_TYPE_DESC get() = Translations.getOptionalString("$TOGGLE_BASE_RESOURCE_KEY.desc")

		private const val SWITCH_BASE_RESOURCE_KEY = "library.element.Switch"
		private val SWITCH_TYPE get() = Translations.getString("$SWITCH_BASE_RESOURCE_KEY.name")
		private val SWITCH_TYPE_DESC get() = Translations.getOptionalString("$SWITCH_BASE_RESOURCE_KEY.desc")

		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.input.SwitchView.iconPath"
	}

	override var customColor: PredefinedColor?
		get() = super.customColor
		set(value) {
			if (value != super.customColor) {
				super.customColor = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	override var toggle: Boolean
		get() = super.toggle
		set(value) {
			if (value != super.toggle) {
				super.toggle = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var minOnTime: Long
		get() = model.minOnTime
		set(value) {
			model.minOnTime = value
		}

	override fun modelExchanged(oldModel: Switch?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-portView.length.toDouble(), 0.0)
		addPortView(portView)
	}

	/** ---- [ControlViewSource] */

	override val controlId: String
		get() {
			// Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
			// when ControlViews (event as part of a wrapping Component) are added to a Drawing
			return "switch:" + model.id
		}

	override val controlName: String get() = super<ControlViewSource>.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Switch> {
		val clone = SwitchView(styleProvider, model)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		clone.labelPosition = labelPosition
		copyControlViewProperties(this, clone)
		return clone
	}

	/** ---- [ControlView] */

	override fun copyControlViewProperties(
		source: AbstractPushButtonSwitchView<*>,
		dest: AbstractPushButtonSwitchView<*>
	) {
		super.copyControlViewProperties(source, dest)
		dest.toggle = source.toggle
		dest.customColor = source.customColor
	}

	/** ---- [AbstractVerticeView] */

	override val type: String
		get() = if (toggle) {
			TOGGLE_TYPE
		} else {
			SWITCH_TYPE
		}

	override val typeDesc: String?
		get() = if (toggle) {
			TOGGLE_TYPE_DESC
		} else {
			SWITCH_TYPE_DESC
		}
}