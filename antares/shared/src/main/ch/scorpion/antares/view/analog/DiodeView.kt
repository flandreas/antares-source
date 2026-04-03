package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Diode
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

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