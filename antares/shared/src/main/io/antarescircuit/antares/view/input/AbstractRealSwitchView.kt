package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.AbstractRealSwitch
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider

abstract class AbstractRealSwitchView<T : AbstractRealSwitch<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
) : AbstractSwitchView<T>(styleProvider, model) {

	/** ---- UI properties */

	var interactivePropagationDelay: Long
		get() = model.interactivePropagationDelay
		set(value) {
			model.interactivePropagationDelay = value
		}

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