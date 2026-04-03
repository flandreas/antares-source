package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Inductor
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_HEIGHT_HALF
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_WIDTH
import ch.scorpion.jabbah.base.Thousands
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class InductorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Inductor = Inductor()
) : AbstractAnalogVerticeView<Inductor>(
    styleProvider,
    model,
    Direction.NORTH,
    Rectangle2D(
        LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF,
        LENGTH.toDouble() + INDUCTOR_WIDTH, 2 * INDUCTOR_HEIGHT_HALF
    )
) {

    @Suppress("unused") // Reflection
    var inductance: Double
        get() = model.inductance
        set(value) {
            model.inductance = value
        }

    override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: Inductor?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + INDUCTOR_WIDTH.toInt(), 0, Direction.EAST))
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
            getColorGradient(context, 2, 1) ?: styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
        } else {
            context.chooseForeground(foregroundColor)
        }

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawInductor(
            this,
            true,
            context,
            applicableForegroundColor,
            context.chooseBackground(backgroundColor),
            SymbolStyle.INDUCTOR_STROKE)
    }

    /** ---- [AbstractAnalogVerticeView] */

    override val mainPropertyValue: String get() = "${Thousands.convert(model.inductance, " ")}H"

}