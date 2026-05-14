package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Inductor
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_HEIGHT_HALF
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_WIDTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

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
    var inductance: MagnitudeValue
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

    override val mainPropertyValue: String get() = inductance.toString()
}