package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Diode
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider

class DiodeView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Diode = Diode()
) : AbstractDiodeView<Diode>(styleProvider, model, Rectangle2D(LENGTH, -SIZE / 2, SIZE, SIZE)) {

    override val relativeExternalLabelLocation: Point2D get() =
        Point2D(LENGTH + SIZE / 2, -SIZE / 2 - LABEL_DIST)

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        AntaresViewModule.currentSymbolStyle.symbolStyle.drawDiode(this, context)
    }
}