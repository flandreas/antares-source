package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Diode
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import kotlin.jvm.JvmStatic

abstract class AbstractDiodeView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Diode = Diode()
) : AbstractAnalogVerticeView<Diode>(styleProvider, model) {

    companion object {
        @JvmStatic
        protected val SIZE = wInt(4)
    }

    override fun modelExchanged(oldModel: Diode?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
        setBounds(LENGTH, -SIZE / 2, SIZE, SIZE)
    }
}