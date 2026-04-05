package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.output.RgbLED
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource

/** A view of an [RgbLED].*/
class RgbLEDView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: RgbLED = RgbLED(),
	ledShape: LEDShape = LEDShape.Circle,
) : AbstractLEDView<RgbLED>(styleProvider, model, ledShape) {

	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.output.RgbLEDView.iconPath"
	}

	/** ---- [ControlView] */

	override val controlId: String
		get() {
			// Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
			// when ControlViews (event as part of a wrapping Component) are added to a Drawing
			return "rgbLED:${model.id}"
		}

	/** ---- [ControlViewSource] */

	override fun createControlView(): ControlView<RgbLED> {
		val clone = RgbLEDView(styleProvider, model, ledShape)
		clone.isShowPortViews = false
		clone.location = Point2D(0, 0)
		copyControlViewProperties(this, clone)
		return clone
	}

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	/** ---- [AbstractLEDView] */

	override fun getBulbExecuteColor(): Color = model.color
}