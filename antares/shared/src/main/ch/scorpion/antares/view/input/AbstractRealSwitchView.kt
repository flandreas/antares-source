package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.AbstractRealSwitch
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes

abstract class AbstractRealSwitchView<T : AbstractRealSwitch<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
) : AbstractSwitchView<T>(styleProvider, model) {

	protected val circleRadius get() = if (model.bitWidth.width > 1) 3.0 else 2.0

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				model.bitWidth = value
			}
		}

	/** ---- [AbstractSwitchView] */

	override fun updateLabels() { }

	/** ---- [AbstractRealSwitchView] */

	protected fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.draw(bounds)
		}
	}
}