package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.XorGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of a [XorGate].
 */
class XorGateView(
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
        xorGate: XorGate = XorGate()
) : AbstractOrLikeGateView<XorGate>(styleProvider, currentSymbolStyle, "=1", "library.element.XorGate", xorGate) {

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawXorGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawXor(this, context, foregroundColor, backgroundColor)
    }
}