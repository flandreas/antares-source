package ch.scorpion.antares.view

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.style.EditTheme
import kotlin.math.ceil

object Look {

	const val PROP_FILL_BASIC_COMPONENTS = "antares.view.fillBasicComponents"

	var FILL_BASIC_COMPONENTS = true
		private set

	const val SCALE: Int = 7
	const val GRID: Int = 1 * SCALE

	val UI_FONT = FontImpl(FontFamily.DIALOG, FontStyle.PLAIN.value, 11)
	val INT_PIN_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.5 * SCALE).toInt())
	val EXT_PIN_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.5 * SCALE).toInt())
	val ANNOTATION_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.4 * SCALE).toInt())
	val ADDRESSABLE_CONTENTS_FONT = FontImpl(FontFamily.MONOSPACED, FontStyle.PLAIN.value, (1.8 * SCALE).toInt())

	fun initialize(eventBus: EventBus) {
		eventBus.register(PreferencesChangedEvent::class) { updateFillBasicComponents() }
		updateFillBasicComponents()
	}

	private fun updateFillBasicComponents() {
		FILL_BASIC_COMPONENTS = BaseModule.properties.getBoolean(PROP_FILL_BASIC_COMPONENTS)
	}

	fun scaleToGrid(value: Int): Int {
		return GRID * ceil(value.toDouble() / GRID).toInt()
	}

	fun scaleToDoubleGrid(value: Int): Int {
		return 2 * GRID * ceil(value.toDouble() / 2 / GRID).toInt()
	}

	/**
	 * Returns the color of the rectangle drawn over [Vertice]s to indicate that they are disabled,
	 * i.e. while they are being recalculated and not able to receive user input.
	 */
	fun disabledColor(): Color {
		return Themes.get<AntaresTheme>().background.color.backgroundColor.withAlpha(192)
	}

	/**
	 * Returns the color of the rectangle drawn over [ActorViews]s to indicate that they are inactive,
	 * i.e. while they are displayed during flat simulation as a ControlView in a [SubGraphVerticeView] with a script.
	 */
	val inactiveColor: Color get() = disabledColor()

	/**
	 * Returns the color to be used for highlighting areas using the selection color.
	 */
	val highlightWithSelectionColor: Color get() =
		if (UI.isDark) {
			Themes.get<EditTheme>().selection.color.foregroundColor.withAlpha(64)
		} else {
			Themes.get<EditTheme>().selection.color.foregroundColor
		}
}