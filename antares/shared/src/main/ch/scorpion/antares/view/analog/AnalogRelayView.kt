package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogRelay
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.input.AbstractSwitchView
import ch.scorpion.antares.view.input.AbstractSwitchView.Companion.DEF_CIRCLE_RADIUS
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_HEIGHT_HALF
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_WIDTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogRelayView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: AnalogRelay = AnalogRelay()
) : AbstractAnalogVerticeView<AnalogRelay>(styleProvider, model) {

    @Suppress("unused") // Reflection
    var inductance: Double
        get() = model.inductance
        set(value) {
            model.inductance = value
        }

    @Suppress("unused") // Reflection
    var onCurrent: Double
        get() = model.onCurrent
        set(value) {
            model.onCurrent = value
        }

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: AnalogRelay?) {
        super.modelExchanged(oldModel)

        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + INDUCTOR_WIDTH.toInt(), 0, Direction.EAST))
        addPortView(AnalogPortView(styleProvider, model.getPort(3), LENGTH, 5 * Look.SCALE, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(4), LENGTH + INDUCTOR_WIDTH.toInt(), 3 * Look.SCALE, Direction.EAST))
        addPortView(AnalogPortView(styleProvider, model.getPort(5), LENGTH + INDUCTOR_WIDTH.toInt(), 7 * Look.SCALE, Direction.EAST))

        setBounds(LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF, LENGTH + INDUCTOR_WIDTH, 8.0 * Look.SCALE)
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
            getColorGradient(context, 2, 1) ?: styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
        } else {
            context.chooseForeground(
                when (AntaresViewModule.currentSymbolStyle.symbolStyle) {
                    SymbolStyle.EUROPEAN, SymbolStyle.VERBOSE -> foregroundColor
                    SymbolStyle.AMERICAN -> styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
                }
            )
        }

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawInductor(
            this,
            false,
            context,
            applicableForegroundColor,
            context.chooseBackground(backgroundColor),
            SymbolStyle.INDUCTOR_STROKE
        )

        context.translated(0.0, 5.0 * Look.SCALE) {
            AbstractSwitchView.drawThreePortRealSwitchShape(this, 3, model.isOn, context, bounds.minX, DEF_CIRCLE_RADIUS, false)
        }

        if (AntaresViewModule.currentSymbolStyle.symbolStyle == SymbolStyle.AMERICAN) {
            // Draw iron core
            context.g.color = context.chooseForeground(foregroundColor)
            context.g.fillRect(LENGTH.toDouble(), 1.7 * Look.SCALE, INDUCTOR_WIDTH, 0.4 *Look.SCALE)
        }
    }
}