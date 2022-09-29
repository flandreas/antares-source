package ch.scorpion.antares.model.signal

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes

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