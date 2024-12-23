package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Inductor
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.Thousands
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class InductorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Inductor = Inductor()
) : AbstractAnalogVerticeView<Inductor>(styleProvider, model) {

    @Suppress("unused") // Reflection
    var inductance: Double
        get() = model.inductance
        set(value) {
            model.inductance = value
        }

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: Inductor?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), -LENGTH, 0, Direction.EAST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), -LENGTH - SymbolStyle.INDUCTOR_WIDTH.toInt(), 0, Direction.WEST))
        setBounds(
            -LENGTH.toDouble() - SymbolStyle.INDUCTOR_WIDTH, -SymbolStyle.INDUCTOR_HEIGHT_HALF,
            SymbolStyle.INDUCTOR_WIDTH, 2 * SymbolStyle.INDUCTOR_HEIGHT_HALF)
        updateLabel()
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
            getColorGradient(context) ?: styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
        } else {
            context.chooseForeground(when (AntaresViewModule.currentSymbolStyle.symbolStyle) {
                SymbolStyle.EUROPEAN,SymbolStyle.VERBOSE  -> foregroundColor
                SymbolStyle.AMERICAN -> styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
            })
        }

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawInductor(
            this,
            context,
            applicableForegroundColor,
            context.chooseBackground(backgroundColor),
            SymbolStyle.INDUCTOR_STROKE)
    }

    /** ---- [AbstractAnalogVerticeView] */

    override val mainPropertyValue: String get() = "${Thousands.convert(model.inductance / 1_000_000.0, " ")}H"

}