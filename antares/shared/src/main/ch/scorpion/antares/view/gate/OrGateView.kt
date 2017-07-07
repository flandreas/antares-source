package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.OrGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of a [OrGate].
 */
class OrGateView(
    styleProvider: StyleProvider,
    currentSymbolStyle: CurrentSymbolStyle,
    orGate: OrGate
) : AbstractOrLikeGateView<OrGate>(styleProvider, currentSymbolStyle, "≥1", "library.element.OrGate", orGate) {

    constructor(styleProvider: StyleProvider, currentSymbolStyle: CurrentSymbolStyle): this(styleProvider, currentSymbolStyle, OrGate())
    constructor(orGate: OrGate): this(DrawStyleModule.styleProvider, AntaresViewModule.currentSymbolStyle, orGate)
    @Suppress("unused") constructor(): this(DrawStyleModule.styleProvider, AntaresViewModule.currentSymbolStyle)

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawOrGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawOr(this, context, foregroundColor, backgroundColor)
    }
}