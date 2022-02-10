package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.AbstractRealSwitch
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext

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

	protected fun getPortColor(portId: Int, context: DrawContext): Color {
		return if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			val port = (model.getPort<DigitalSignal>(portId) as DigitalPort)
			port.net?.signal?.color?.foregroundColor ?: DigitalSignalFactory.undefined(BitWidth.BW_1).color.foregroundColor
		} else {
			// Draw in edge color and not in vertice color
			transparent.applyTo(context.choose(Themes.get<AntaresTheme>().edge.color).foregroundColor)
		}
	}
}