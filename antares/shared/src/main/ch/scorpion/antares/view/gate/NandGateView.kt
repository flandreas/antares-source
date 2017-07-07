package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NandGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of an [NandGate].
 */
class NandGateView(
    styleProvider: StyleProvider,
    val currentSymbolStyle: CurrentSymbolStyle,
    nandGate: NandGate
) : AbstractDigitalGateView<NandGate>(styleProvider, "&", "library.element.NandGate", nandGate) {

    constructor(styleProvider: StyleProvider, currentSymbolStyle: CurrentSymbolStyle): this(styleProvider, currentSymbolStyle, NandGate())
    @Suppress("unused") constructor(): this(DrawStyleModule.styleProvider, AntaresViewModule.currentSymbolStyle)

    init {
        modelExchanged(null)
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawAndGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawNand(this, context, foregroundColor, backgroundColor)
    }
}