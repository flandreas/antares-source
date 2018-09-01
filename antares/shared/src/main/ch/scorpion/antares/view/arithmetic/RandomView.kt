package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.model.arithmetic.Random
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

/** A view representation of a [Random].*/
class RandomView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Random = Random()
) : AbstractDigitalGateView<Random>(styleProvider, "", "library.element.Random", model) {

	init {
		modelExchanged(null)
	}

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model!!.bitWidth
		set(value) {
			if (value != bitWidth) {
				model!!.bitWidth = value
			}
		}
}