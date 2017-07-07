package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NorGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of a [NorGate].
 */
class NorGateView(
    styleProvider: StyleProvider,
    currentSymbolStyle: CurrentSymbolStyle,
    norGate: NorGate
) : AbstractOrLikeGateView<NorGate>(styleProvider, currentSymbolStyle, "≥1", "library.element.NorGate", norGate) {

    constructor(styleProvider: StyleProvider, currentSymbolStyle: CurrentSymbolStyle): this(styleProvider, currentSymbolStyle, NorGate())
    @Suppress("unused") constructor(): this(DrawStyleModule.styleProvider, AntaresViewModule.currentSymbolStyle)

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawOrGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawNor(this, context, foregroundColor, backgroundColor)
    }
}