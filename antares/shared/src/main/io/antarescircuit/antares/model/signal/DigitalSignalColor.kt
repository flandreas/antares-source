package io.antarescircuit.antares.model.signal

import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.Themes

/**
 * Determines the [CompositeColor] in which a [DigitalSignal] is to be drawn.
 */
object DigitalSignalColor {

	const val PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR = "antares.differentNonZeroMultiBitColor"

	private val differentNonZeroMultiBitColor = resettableLazy {
		BaseModule.properties.getBoolean(PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR)
	}

	fun reset() {
		differentNonZeroMultiBitColor.reset()
	}

	fun ofSignal(signal: DigitalSignal): CompositeColor {
		if (signal.bitWidth.width == BitWidth.BW_1.width) {
			return signal.bitAt(0).color
		}
		if (signal.hasError) {
			return Themes.get<AntaresTheme>().error
		}
		if (signal.isFullyUndefined) {
			return Themes.get<AntaresTheme>().undefined
		}
		return if (signal.isZero || !(differentNonZeroMultiBitColor.value)) {
			Themes.get<AntaresTheme>().wordZero
		} else {
			Themes.get<AntaresTheme>().word
		}
	}
}