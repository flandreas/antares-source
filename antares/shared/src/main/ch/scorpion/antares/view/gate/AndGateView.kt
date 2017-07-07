package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.AndGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of an [AndGate].
 */
class AndGateView(
    styleProvider: StyleProvider,
    val currentSymbolStyle: CurrentSymbolStyle,
    andGate: AndGate
) : AbstractDigitalGateView<AndGate>(styleProvider, "&", "library.element.AndGate", andGate) {

    constructor(styleProvider: StyleProvider, currentSymbolStyle: CurrentSymbolStyle): this(styleProvider, currentSymbolStyle, AndGate())
    constructor(): this(DrawStyleModule.styleProvider, AntaresViewModule.currentSymbolStyle)

    init {
        modelExchanged(null)
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawAndGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawAnd(this, context, foregroundColor, backgroundColor)
    }
}