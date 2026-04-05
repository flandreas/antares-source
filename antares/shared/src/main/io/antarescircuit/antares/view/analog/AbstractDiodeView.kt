package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Diode
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import kotlin.jvm.JvmStatic

abstract class AbstractDiodeView<T: Diode>(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: T,
    bounds: Rectangle2D
) : AbstractAnalogVerticeView<T>(styleProvider, model, Direction.NORTH, bounds) {

    companion object {
        @JvmStatic
        protected val SIZE = wInt(4)
    }

    override fun modelExchanged(oldModel: T?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
        updateMainPropertyLabel()
    }
}