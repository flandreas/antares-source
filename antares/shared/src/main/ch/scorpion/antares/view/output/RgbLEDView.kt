package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.RgbLED
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource

/** A view of an [RgbLED].*/
class RgbLEDView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: RgbLED = RgbLED(),
	square: Boolean = false,
) : AbstractLEDView<RgbLED>(styleProvider, model, square) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.RgbLEDView.iconPath"
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
		val clone = RgbLEDView(styleProvider, model, square)
		clone.isShowPortViews = false
		clone.location = Point2D(0, 0)
		copyControlViewProperties(this, clone)
		return clone
	}

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	/** ---- [RgbLEDView] */

	override fun getBulbColor(): Color = model.color

	override fun drawBulb(context: DrawContext) {
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			super.drawBulb(context)
		} else {
			drawBulb(context, Themes.get<AntaresTheme>().screen.backgroundColor)
		}
	}

}