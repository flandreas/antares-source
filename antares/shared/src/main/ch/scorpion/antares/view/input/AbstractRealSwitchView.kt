package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.AbstractRealSwitch
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

abstract class AbstractRealSwitchView<T : AbstractRealSwitch<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
) : AbstractSwitchView<T>(styleProvider, model) {

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				model.bitWidth = value
			}
		}

	init {
	    initExternalLabel(Direction.NORTH)
	}

	/** ---- [AbstractSwitchView] */

	override val circleRadius get() = if (model.bitWidth.width > 1) 3.0 else DEF_CIRCLE_RADIUS

	override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		customColor?.color ?: super.getEditPortViewColor(styleProvider)
}