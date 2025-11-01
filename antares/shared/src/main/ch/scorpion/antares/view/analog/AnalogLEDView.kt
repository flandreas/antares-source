package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Diode
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class AnalogLEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Diode = Diode()
) : AbstractDiodeView(styleProvider, model) {

    companion object {
        private val NEGATIVE_HEIGHT = hInt(4)
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawAnalogLED(this, context)
    }

    override fun modelExchanged(oldModel: Diode?) {
        super.modelExchanged(oldModel)
        // Overwrite bounds to incorporate LED arrows
        setBounds(LENGTH, -NEGATIVE_HEIGHT, SIZE, SIZE / 2 + NEGATIVE_HEIGHT)
    }
}