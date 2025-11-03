package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Diode
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class DiodeView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Diode = Diode()
) : AbstractDiodeView<Diode>(styleProvider, model) {

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawDiode(this, context)
    }
}